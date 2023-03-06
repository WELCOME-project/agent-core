/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dfki.asr.ajan.pluginsystem.sscservicecomputingplugin.extensions;

import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.LeafStatus;
import de.dfki.asr.ajan.behaviour.nodes.common.TreeNode;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorConstructQuery;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorSelectQuery;
import de.dfki.asr.ajan.common.AJANVocabulary;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.sscservicecomputingplugin.vocabularies.SSCVocabulary;
import de.dfki.fastdownwardcaller.*;
import de.dfki.isem.*;
import de.dfki.s2m2.MatchingResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.binary.Base64;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.Rio;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pf4j.Extension;

/**
 *
 * @author ejara
 */
@Extension
@RDFBean("ssc:ServiceComputing")
public class SSCServiceComputing extends AbstractTDBLeafTask implements NodeExtension, TreeNode {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SSCServiceComputing.class);
    
    @Getter
    @Setter
    @RDFSubject
    private String url;
    
    @RDF("rdfs:label")
    @Getter @Setter
    private String label;
   
    @RDF("ssc:serviceRequest")
    @Getter @Setter
    private BehaviorConstructQuery serviceRequestQuery;
   
    @RDF("ssc:serviceOffer")
    @Getter @Setter
    private BehaviorSelectQuery serviceOfferQuery;
    
    @RDF("ssc:filterModel")
    @Getter @Setter
    private String selectedFilter;
    
    @RDF("ssc:domainDefinition")
    @Getter @Setter
    private BehaviorConstructQuery domainDefinitionQuery;
    
    @RDF("ssc:problemInit")
    @Getter @Setter
    private BehaviorConstructQuery problemInitQuery;
    
    @RDF("ssc:problemGoal")
    @Getter @Setter
    private BehaviorConstructQuery problemGoalQuery;
    
    private String welcomeLocationOffers;
    private String welcomeLocationRequest;
    private String requestFileName;
    
    @Override
    public Resource getType() {
        return SSCVocabulary.SSCType;
    }
           
    @Override
    public String toString() {
        return "SSC Plugin (" + this.getUrl() + ")";
    }
    
    @Override
    public void end() {
        LOG.info("SSC Plugin (" + getStatus() + ")");
    }
    
    @Override
    public EvaluationResult.Result simulateNodeLogic(final EvaluationResult result, final Resource root) {
        return EvaluationResult.Result.UNCLEAR;
    }
    
    @Override
    public LeafStatus executeLeaf() {
        try {
            if (executeServiceComputing()) {
                String report = toString() + " SUCCEEDED";
		LOG.info(report);
		return new LeafStatus(Status.SUCCEEDED, report);
            } else {
                String report = toString() + " FAILED";
		LOG.info(report);
		return new LeafStatus(Status.FAILED, report);
            }
        } catch (URISyntaxException | IOException ex) {  
            LOG.debug(toString() + ex);
            LOG.info(toString() + " FAILED due to service selection errors");
            return  new LeafStatus(Status.FAILED, toString() + " FAILED due to service selection errors");
        }
    }
    
    private boolean executeServiceComputing() throws URISyntaxException, IOException {
        boolean flag;
        if(selectedFilter.isEmpty()){
            executePlanner();
            flag = true;
        } else if (!selectedFilter.isEmpty()){
            executeMatchmaker();
            flag = true;
        } else
            flag=false;
        return flag;
    }
    
    @SuppressWarnings("null")
    private void executeMatchmaker() throws URISyntaxException, IOException {
    
        //Store results
        Map<URI, Vector<MatchingResult>> matchingResult = new HashMap<>();
        Vector<MatchingResult> ranking = new Vector<MatchingResult>();
        
        //Create the final model for the request
        Repository repoRequest = BTUtil.getInitializedRepository(this.getObject(), serviceRequestQuery.getOriginBase());
        Model requestModel = serviceRequestQuery.getResult(repoRequest);        
	Model serviceRequestModel = createServiceRequest(requestModel);
		
        //Create the final model for the offers
        Repository repoOffer = BTUtil.getInitializedRepository(this.getObject(), serviceOfferQuery.getOriginBase());
	List<BindingSet>  tripleServOffer = serviceOfferQuery.getResult(repoOffer);
	Model mergedModel = getModelfromBindingResult(tripleServOffer);
	List<Model> offerModel = createServiceOffer(mergedModel); 
		
	//Save offers and request
        welcomeLocationOffers = System.getenv("servicesOfferFolderPath");
        welcomeLocationRequest = System.getenv("servicesRequestFolderPath"); 
        //welcomeLocationOffers = System.getProperty("servicesOfferFolderPath");
        //welcomeLocationRequest = System.getProperty("servicesRequestFolderPath"); 
        
        File PartialLocationOffersFile = new File(welcomeLocationOffers);
        File PartialLocationRequestFile = new File(welcomeLocationRequest);
        String proposedOffer = "";
        MatchingResult result;
               
        File locationOffersFile = new File(PartialLocationOffersFile.toPath().normalize().toAbsolutePath().toString());
        File locationRequestFile = new File(PartialLocationRequestFile.toPath().normalize().toAbsolutePath().toString());
        
        if (!locationOffersFile.exists()){
            locationOffersFile.mkdirs();
        } else if(locationOffersFile.list().length != 0) {
            deleteOldServices(locationOffersFile.toPath().normalize().toAbsolutePath());           
        }
        
        if (!locationRequestFile.exists()){
            locationRequestFile.mkdirs();
        } else if(locationRequestFile.list().length != 0) {
            deleteOldServices(locationRequestFile.toPath().normalize().toAbsolutePath());
        }
        
        if(Files.exists(locationOffersFile.toPath().normalize().toAbsolutePath()) && Files.exists(locationRequestFile.toPath().normalize().toAbsolutePath())){
            //System.out.println("The offer path is " + locationOffersFile.toPath().normalize().toAbsolutePath() + " and the request path is " + locationRequestFile.toPath().normalize().toAbsolutePath());        
            //Call the matchmaker
            if (saveOffersRequest(offerModel, serviceRequestModel, locationOffersFile.toPath().normalize().toAbsolutePath(), locationRequestFile.toPath().normalize().toAbsolutePath())) {
                String filterModelName = "filter/isem/" + this.getSelectedFilter();
                String modelFileLocation = "default_model.xml";
                String[] pathnamesRequest;  
                String[] pathnamesOffer;
                List<String> requestList = new ArrayList<>();
                List<String> offerList = new ArrayList<>();
                File f_RequestServices = new File(locationRequestFile.toPath().toString());
                int nbRequest = f_RequestServices.list().length ;
                pathnamesRequest = new String[nbRequest];
                pathnamesRequest = f_RequestServices.list();
                for (String pathname : pathnamesRequest) {
                    File shortName = new File(pathname);
                    String myFileName = "http://localhost:8060/welcome/integration/coordination/ajan/agents/welcome/request/".concat(shortName.getName());
                    System.out.println(" The request is " + myFileName);
                    requestList.add(myFileName);
                }                
                File f_Offers = new File(locationOffersFile.toPath().toString());
                int nbOffers = f_Offers.list().length ;
                pathnamesOffer = new String[nbOffers];
                pathnamesOffer = f_Offers.list();
                for (String pathnameOff : pathnamesOffer) {
                    File shortNameOffer = new File(pathnameOff);
                    String myFileNameOffer = "http://localhost:8060/welcome/integration/coordination/ajan/agents/welcome/services/".concat(shortNameOffer.getName());
                    System.out.println("The offers is " + myFileNameOffer);
                    offerList.add(myFileNameOffer);
                }
                   
                System.out.println("I have added " + offerList.size() + " offers, and " + requestList.size() + " requests");
                //String[] isemParam = {locationRequestFile.toPath().normalize().toAbsolutePath().toString(), locationOffersFile.toPath().normalize().toAbsolutePath().toString(), modelFileResources.getAbsolutePath(), filterModelFileResources.getAbsolutePath()};
                int sizeParam = requestList.size() + offerList.size() + 2;
                String[] isemParam = new String[sizeParam];
                isemParam[0] = requestList.get(0) ;
                int indexIsem = 1;
                for(int i=0; i < offerList.size() ; i++){
                    isemParam[indexIsem] = offerList.get(i);
                    indexIsem++;
                }
                isemParam[indexIsem] = filterModelName;
                isemParam[indexIsem+1] = modelFileLocation;
                matchingResult = ISeMCLITool.setParameters(isemParam);
                //Only one request is expected by matching 
                deleteOldServices(locationRequestFile.toPath().normalize().toAbsolutePath());
                deleteOldServices(locationOffersFile.toPath().normalize().toAbsolutePath());
                if(!matchingResult.keySet().isEmpty()){
                    URI resultURI = matchingResult.keySet().iterator().next();
                    System.out.println("The found service for the request " + resultURI.toString() + " is: ");
                    ranking = matchingResult.get(resultURI);               
                    if(ranking.isEmpty())
                        proposedOffer = "https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#Unknown";
                    else{
                        result = ranking.get(0);
                        proposedOffer = result.getServiceOffer().toString();
                        System.out.println("\n The proposed offer is " + proposedOffer);
                    }
                }
            }
            else {
                    proposedOffer = "https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#Unknown";
                    //deleteOldServices(locationRequestFile.toPath().normalize().toAbsolutePath());
                    //deleteOldServices(locationOffersFile.toPath().normalize().toAbsolutePath());
            }
        }
        else {
            proposedOffer = "https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#Unknown";
            //deleteOldServices(locationRequestFile.toPath().normalize().toAbsolutePath());
            //deleteOldServices(locationOffersFile.toPath().normalize().toAbsolutePath());
        }
        
        
	//Create the triple result
	IRI offer_proposal = SSCVocabulary.FACTORY.createIRI(proposedOffer);   
	Statement resultStatement = SSCVocabulary.FACTORY.createStatement(SSCVocabulary.SSCName, SSCVocabulary.SSCPred, offer_proposal); 
	Model responseModel = new LinkedHashModel();   
        responseModel.add(resultStatement);
	if (serviceRequestQuery.getTargetBase().toString().equals(AJANVocabulary.EXECUTION_KNOWLEDGE.toString())) {
            this.getObject().getExecutionBeliefs().update(responseModel);
        } else if (serviceRequestQuery.getTargetBase().toString().equals(AJANVocabulary.AGENT_KNOWLEDGE.toString())) {
            this.getObject().getAgentBeliefs().update(responseModel);
        } else {
            this.getObject().getLocalServicesBeliefs().update(responseModel);
        }
    }

    private void deleteOldServices(Path serviceDirectory){
        File servicePath = new File(serviceDirectory.toString());
        String[] services = servicePath.list();
        for (String servicesIte : services) {            
            File singleService = new File(serviceDirectory.toString().concat("/").concat(servicesIte));                     
            try {
                Files.deleteIfExists(singleService.toPath());
            }
            catch (NoSuchFileException e) {
                System.out.println("No such file/directory exists");
            }
            catch (DirectoryNotEmptyException e) {
                System.out.println("Directory is not empty.");
            }
            catch (IOException e) {
                System.out.println("Invalid permissions.");
            }
            if (singleService.exists())
                System.out.println("The file " + singleService.toPath().toString() + " exists.");
            else
            System.out.println("Does not Exists. Deletion successful for the file . " + singleService.toPath().toString());
        }
    }
        
    
    private Model createServiceRequest(Model inputRequestModel){
		
	Model requestModel = new LinkedHashModel(); 
        ModelBuilder builder = new ModelBuilder();
	builder.setNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        builder.setNamespace("owl", "http://www.w3.org/2002/07/owl#");
	builder.setNamespace("service", SSCVocabulary.serviceNS);
	builder.setNamespace("process", SSCVocabulary.processNS);
	builder.setNamespace("profile", SSCVocabulary.profileNS); 
	builder.setNamespace("welcome", SSCVocabulary.welcomeNS); 
        builder.setNamespace("base", "http://localhost:8060/welcome/integration/coordination/ajan/agents/welcome/services/ServiceRequest.owl"); 
		
	builder.subject("http://localhost:8060/welcome/integration/coordination/ajan/agents/welcome/services/ServiceRequest.owl#serviceRequest").add(org.eclipse.rdf4j.model.vocabulary.RDF.TYPE , org.eclipse.rdf4j.model.vocabulary.OWL.ONTOLOGY)
				.add("owl:imports", SSCVocabulary.FACTORY.createIRI(SSCVocabulary.serviceNS))
				.add("owl:imports", SSCVocabulary.FACTORY.createIRI(SSCVocabulary.processNS))
				.add("owl:imports", SSCVocabulary.FACTORY.createIRI(SSCVocabulary.profileNS));
	
        builder.add("http://localhost:8060/welcome/integration/coordination/ajan/agents/welcome/services/ServiceRequest.owl#myRequestProfile", org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, SSCVocabulary.FACTORY.createIRI("http://www.daml.org/services/owl-s/1.1/Profile.owl#Profile"));
        builder.add("http://localhost:8060/welcome/integration/coordination/ajan/agents/welcome/services/ServiceRequest.owl#myRequestService", org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, SSCVocabulary.FACTORY.createIRI("http://www.daml.org/services/owl-s/1.1/Service.owl#Service"));
        builder.add("http://localhost:8060/welcome/integration/coordination/ajan/agents/welcome/services/ServiceRequest.owl#myRequestProcess", org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, SSCVocabulary.FACTORY.createIRI("http://www.daml.org/services/owl-s/1.1/Process.owl#AtomicProcess"));
        
        builder.add("http://localhost:8060/welcome/integration/coordination/ajan/agents/welcome/services/ServiceRequest.owl#myRequestService", "service:presents", SSCVocabulary.FACTORY.createIRI("http://localhost:8060/welcome/integration/coordination/ajan/agents/welcome/services/ServiceRequest.owl#myRequestProfile"));
        builder.add("http://localhost:8060/welcome/integration/coordination/ajan/agents/welcome/services/ServiceRequest.owl#myRequestService", "service:describedBy", SSCVocabulary.FACTORY.createIRI("http://localhost:8060/welcome/integration/coordination/ajan/agents/welcome/services/ServiceRequest.owl#myRequestProcess"));
        
        builder.add("http://localhost:8060/welcome/integration/coordination/ajan/agents/welcome/services/ServiceRequest.owl#myRequestProcess", "service:describes", SSCVocabulary.FACTORY.createIRI("http://localhost:8060/welcome/integration/coordination/ajan/agents/welcome/services/ServiceRequest.owl#myRequestService"));
         
        builder.add("http://localhost:8060/welcome/integration/coordination/ajan/agents/welcome/services/ServiceRequest.owl#myRequestProfile", "service:presentedBy", SSCVocabulary.FACTORY.createIRI("http://localhost:8060/welcome/integration/coordination/ajan/agents/welcome/services/ServiceRequest.owl#myRequestService"));
        
        Set<org.eclipse.rdf4j.model.Value> textDescriptionValue = inputRequestModel.filter(null, SSCVocabulary.filteredTextDescription, null).objects();
        Set<org.eclipse.rdf4j.model.Value> serviceNameValue = inputRequestModel.filter(null,SSCVocabulary.filteredTextDescription, null).objects();
	         
        if(!textDescriptionValue.isEmpty() ){
            Literal textDescription = (Literal)textDescriptionValue.iterator().next();
            builder.add("http://localhost:8060/welcome/integration/coordination/ajan/agents/welcome/services/ServiceRequest.owl#myRequestProfile", "http://www.daml.org/services/owl-s/1.1/Profile.owl#textDescription", textDescription);
        }
        if(!serviceNameValue.isEmpty()){
            Literal serviceName = (Literal)serviceNameValue.iterator().next();
            builder.add("http://localhost:8060/welcome/integration/coordination/ajan/agents/welcome/services/ServiceRequest.owl#myRequestProfile", "http://www.daml.org/services/owl-s/1.1/Profile.owl#serviceName", serviceName);
        }      
        requestModel = builder.build();
        
	System.out.println("The final model used for the serv request is ");
        requestModel.forEach(System.out::println);
		
	return requestModel;		
    }
	
    private List<Model> createServiceOffer(Model model){ 
       List<Model> fullModelList = new ArrayList<>();       
       Set<Resource> ctxList = model.contexts();
       System.out.println("The list of context are " + ctxList);
       Iterator<Resource> itCtx = ctxList.iterator();
             
       while(itCtx.hasNext()){
           Resource ctx = itCtx.next();
           Model segmented = model.filter(null, null, null, ctx);           
           fullModelList.add(segmented);
           System.out.println("For the context " + ctx.toString() + " there are " + Integer.toString(segmented.size()) + " triples.");
       }
              
        for (Model temp : fullModelList) {
            Model tempCopy = new LinkedHashModel();
            tempCopy.addAll(temp);
            temp.setNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
            temp.setNamespace("owl", "http://www.w3.org/2002/07/owl#");
            temp.setNamespace("service", SSCVocabulary.serviceNS);
            temp.setNamespace("process", SSCVocabulary.processNS);
            temp.setNamespace("profile", SSCVocabulary.profileNS); 
            temp.setNamespace("welcome", SSCVocabulary.welcomeNS); 
            temp.remove(null, SSCVocabulary.FACTORY.createIRI("https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#isOptional"), null);
            temp.remove(null, SSCVocabulary.FACTORY.createIRI("https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#hasTemplateId"), null);
            temp.remove(null, SSCVocabulary.FACTORY.createIRI("https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#hasDIP"), null);
            temp.remove(null, SSCVocabulary.FACTORY.createIRI("https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#hasSlotType"), null);
            temp.remove(null, SSCVocabulary.FACTORY.createIRI("https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#dependsOn"), null);
            temp.remove(null, SSCVocabulary.FACTORY.createIRI("http://www.daml.org/services/owl-s/1.1/Process.owl#parameterType"), null);
            temp.remove(null, SSCVocabulary.filteredServiceName, null);
            temp.remove(null, SSCVocabulary.filteredTextDescription, null);
            temp.remove(null, SSCVocabulary.FACTORY.createIRI("http://www.daml.org/services/owl-s/1.1/generic/Expression.owl#expressionBody"),null);
            temp.remove(null, SSCVocabulary.FACTORY.createIRI("http://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/PDDLExpression.owl#PDDL-Expression"),null);
            temp.remove(null, SSCVocabulary.FACTORY.createIRI("http://www.daml.org/services/owl-s/1.1/Profile.owl#hasResult"),null);
            temp.remove(null, SSCVocabulary.FACTORY.createIRI("http://www.daml.org/services/owl-s/1.1/Process.owl#hasResult"),null);
            temp.remove(null, SSCVocabulary.FACTORY.createIRI("http://www.daml.org/services/owl-s/1.1/Process.owl#hasEffect"),null);
            temp.remove(null, SSCVocabulary.FACTORY.createIRI("http://www.daml.org/services/owl-s/1.1/Profile.owl#hasPrecondition"),null);
            temp.remove(null, SSCVocabulary.FACTORY.createIRI("http://www.daml.org/services/owl-s/1.1/Process.owl#hasPrecondition"),null);
            temp.remove(null, SSCVocabulary.FACTORY.createIRI("http://www.daml.org/services/owl-s/1.1/Process.owl#hasInput"),null);
            temp.remove(null, SSCVocabulary.FACTORY.createIRI("http://www.daml.org/services/owl-s/1.1/Process.owl#hasLocal"),null);
            temp.remove(null, SSCVocabulary.FACTORY.createIRI("http://www.daml.org/services/owl-s/1.1/Profile.owl#hasInput"),null);
            temp.remove(null, SSCVocabulary.FACTORY.createIRI("http://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/PDDLExpression.owl#PDDL-Expression"), null);
            temp.remove(null, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, SSCVocabulary.FACTORY.createIRI("http://www.daml.org/services/owl-s/1.1/Process.owl#Local"));
            temp.remove(null, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, SSCVocabulary.FACTORY.createIRI("http://www.daml.org/services/owl-s/1.1/Process.owl#Input"));
            temp.remove(null, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, SSCVocabulary.FACTORY.createIRI("http://www.w3.org/2002/07/owl#Class"));
            temp.remove(null, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, SSCVocabulary.FACTORY.createIRI("http://www.daml.org/services/owl-s/1.1/Process.owl#Result"));
            temp.remove(null, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, SSCVocabulary.FACTORY.createIRI("http://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/PDDLExpression.owl#PDDL-Expression"));
            temp.remove(null, SSCVocabulary.FACTORY.createIRI("http://www.daml.org/services/owl-s/1.1/Profile.owl#contactInformation"), null);
            
            /*Set<Resource> inputList = tempCopy.filter(null, SSCVocabulary.FACTORY.createIRI("http://www.daml.org/services/owl-s/1.1/Process.owl#parameterType"), null).subjects();
            Iterator<Resource> inputListIterator= inputList.iterator();
            while(inputListIterator.hasNext()) {
                Resource serviceInput = inputListIterator.next();
                Set<Value> paramTypeValueSet = tempCopy.filter(serviceInput, SSCVocabulary.FACTORY.createIRI("http://www.daml.org/services/owl-s/1.1/Process.owl#parameterType"),null).objects();
                Value paramTypeValue = paramTypeValueSet.iterator().next();
                if(paramTypeValue.stringValue().contains("^^")){
                    String paramType = paramTypeValue.stringValue();
                    int pos = paramType.indexOf("^");
                    String correctedValue = paramType.substring(1, pos-1);
                    Literal correctedLiteral = SSCVocabulary.FACTORY.createLiteral(correctedValue, SSCVocabulary.FACTORY.createIRI("http://www.w3.org/2001/XMLSchema#anyURI"));
                    temp.add(serviceInput, SSCVocabulary.FACTORY.createIRI("http://www.daml.org/services/owl-s/1.1/Process.owl#parameterType"), correctedLiteral);
                }                
            }*/
            
            String textDescriptionOffer = tempCopy.filter(null,SSCVocabulary.filteredTextDescription,null).objects().iterator().next().stringValue();
            String serviceNameOffer = tempCopy.filter(null, SSCVocabulary.filteredServiceName, null).objects().iterator().next().stringValue();
            if(textDescriptionOffer.contains("\"")){
                textDescriptionOffer = textDescriptionOffer.replace("\"", "");
            }
            if(serviceNameOffer.contains("\"")){
                serviceNameOffer = serviceNameOffer.replace("\"", "");
            }
            Resource profileOffer = tempCopy.filter(null, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, SSCVocabulary.FACTORY.createIRI("http://www.daml.org/services/owl-s/1.1/Profile.owl#Profile")).subjects().iterator().next();
            Literal correctedTextDescription = SSCVocabulary.FACTORY.createLiteral(textDescriptionOffer, SSCVocabulary.FACTORY.createIRI("http://www.w3.org/2001/XMLSchema#string"));
            temp.add(profileOffer, SSCVocabulary.filteredTextDescription, correctedTextDescription);
            Literal correctedServiceName = SSCVocabulary.FACTORY.createLiteral(serviceNameOffer, SSCVocabulary.FACTORY.createIRI("http://www.w3.org/2001/XMLSchema#string"));
            temp.add(profileOffer, SSCVocabulary.filteredServiceName, correctedServiceName);
           
        }
        
        return fullModelList;
    }
	
    private Model getModelfromBindingResult(List<BindingSet> listTriples){ 
        Iterator<BindingSet> itrListTriples = listTriples.iterator();
        Set<String> usedBindings;
        Iterator<String> itPosSingleTriple; 
        List<org.eclipse.rdf4j.model.Value> tripleResult;
        BindingSet singleTriple;
        Model modelFromBinding;
        ModelBuilder builder = new ModelBuilder();
        while(itrListTriples.hasNext()){
            singleTriple =  itrListTriples.next();
            usedBindings = singleTriple.getBindingNames();
            itPosSingleTriple = usedBindings.iterator();
            tripleResult = new ArrayList<>();
            
            while(itPosSingleTriple.hasNext()){
                tripleResult.add(singleTriple.getValue(itPosSingleTriple.next()));                    
            }
           
            IRI subject = SSCVocabulary.FACTORY.createIRI(tripleResult.get(0).toString());
            IRI predicate = SSCVocabulary.FACTORY.createIRI(tripleResult.get(1).toString());
            IRI context = SSCVocabulary.FACTORY.createIRI(tripleResult.get(3).toString());
            if(tripleResult.get(2) instanceof Literal){
                Literal object = SSCVocabulary.FACTORY.createLiteral(tripleResult.get(2).toString());
                builder.namedGraph(context).subject(subject).add(predicate, object);                
            }
            else{
                IRI object = SSCVocabulary.FACTORY.createIRI(tripleResult.get(2).toString());
                builder.namedGraph(context).subject(subject).add(predicate, object);
            }          
        }
        modelFromBinding = builder.build();
        System.out.println("There are ".concat(Integer.toString(modelFromBinding.size())).concat(" statements in the model"));
        return modelFromBinding;
    }
	
    private boolean saveOffersRequest (List<Model> serviceOffers, Model serviceRequest, Path pathFolderOffer, Path pathFolderRequest) throws IOException {
		
	if(!serviceOffers.isEmpty() && !serviceRequest.isEmpty()){			
            //Files.createDirectories(pathFolderOffer);
            //Files.createDirectories(pathFolderRequest);
            File offerFile = null; 
            File requestFile = null;			
            FileOutputStream outFile = null;
            Model tempModel;
            String serviceOfferFileName;			
			
            Iterator<Model> itModelList = serviceOffers.iterator();				
            while(itModelList.hasNext()){
                tempModel = itModelList.next();
		Set<Resource> ctxList = tempModel.contexts();
		serviceOfferFileName = ctxList.iterator().next().toString().substring(19) ;
                offerFile = new File(pathFolderOffer.toString().concat("/").concat(serviceOfferFileName));
                //System.out.println("I am storing the file " + serviceOfferFileName + " in " + offerFile.getAbsolutePath());
                outFile = new FileOutputStream(offerFile);
                Rio.write(tempModel, outFile, RDFFormat.RDFXML);
            }
            long millis = System.currentTimeMillis(); 
            requestFileName = "ServiceRequest".concat(Long.toString(millis)).concat(".owl");
            requestFile = new File(pathFolderRequest.toString().concat("/").concat(requestFileName));
            outFile = new FileOutputStream(requestFile);
            Rio.write(serviceRequest, outFile, RDFFormat.RDFXML);
			
            outFile.close();
            return true;
	}
	else 
            return false;
    }
    
    private void executePlanner() throws URISyntaxException, IOException{
        //Get the Domain
        Repository repoDomain = BTUtil.getInitializedRepository(this.getObject(), domainDefinitionQuery.getOriginBase());
        Model domainModel = domainDefinitionQuery.getResult(repoDomain);
        
        repoDomain = BTUtil.getInitializedRepository(this.getObject(), problemInitQuery.getOriginBase());
        Model problemInitModel = problemInitQuery.getResult(repoDomain);
        
        repoDomain = BTUtil.getInitializedRepository(this.getObject(), problemGoalQuery.getOriginBase());
        Model problemGoalModel = problemGoalQuery.getResult(repoDomain);
        
        //Create the folder
        String myPath = getClass().getResource("/").toURI().toString();
        String plannerFolder = "/planner/"; 
        URI plannerFolderURI = new URI(myPath.concat(plannerFolder));
        Path plannerFolderPath = Paths.get(plannerFolderURI);
        Files.createDirectories(plannerFolderPath);
        FileOutputStream outFile;
        
        //Decode the problemFile
        Base64 decoder = new Base64(); 
        for(Statement st:domainModel){
            //This loop should be run only once
            System.out.println("This message should appear only once. One PDDL domain in the LSR");
            Literal encodedObject = (Literal)st.getObject();
            byte[] encodedDomain = encodedObject.toString().getBytes();
            byte[] imgBytes = decoder.decode(encodedDomain);            
            File myAbsoluteFile = new File(plannerFolderPath.toString().concat("/WelcomeDomain.pddl"));  
            outFile = new FileOutputStream(myAbsoluteFile);
            outFile.write(imgBytes);
            outFile.flush();
            outFile.close();
        }
        
        //Create the Init
        Iterator<Statement> itProblemModel = problemInitModel.iterator();
        while(itProblemModel.hasNext()){
            File initFile = new File(plannerFolderPath.toString().concat("/WelcomeInit.pddl"));
            outFile = new FileOutputStream(initFile);
            RDFHandler writer = Rio.createWriter(RDFFormat.RDFXML, outFile);
            Rio.write(itProblemModel.next(), writer);
        } 
        
        //Create the Goal
        Iterator<Statement> itGoalModel = problemGoalModel.iterator();
        while(itGoalModel.hasNext()){
            File goalFile = new File(plannerFolderPath.toString().concat("/WelcomeGoal.pddl"));
            outFile = new FileOutputStream(goalFile);
            RDFHandler writer = Rio.createWriter(RDFFormat.RDFXML, outFile);
            Rio.write(itGoalModel.next(), writer);
        }  
    }
}