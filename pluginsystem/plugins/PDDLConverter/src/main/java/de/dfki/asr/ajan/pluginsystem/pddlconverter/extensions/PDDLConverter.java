/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dfki.asr.ajan.pluginsystem.pddlconverter.extensions;

import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.LeafStatus;
import de.dfki.asr.ajan.behaviour.nodes.common.TreeNode;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorSelectQuery;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.pddlconverter.vocabularies.PDDLConverterVocabulary;
import de.dfki.owls2pddxml_2_0.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.Rio;
import org.semanticweb.owl.model.OWLException;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pf4j.Extension;

/**
 *
 * @author ejara
 */
@Extension
@RDFBean("ssc:PDDLConverter")
public class PDDLConverter extends AbstractTDBLeafTask implements NodeExtension, TreeNode {
    
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(PDDLConverter.class);
    
    @Getter
    @Setter
    @RDFSubject
    private String url;
    
    @RDF("rdfs:label")
    @Getter @Setter
    private String label;
   
    @RDF("ssc:LocalServicesTriples")
    @Getter @Setter
    private BehaviorSelectQuery listServicesQuery;
    
    @Override
    public Resource getType() {
        return PDDLConverterVocabulary.PDDLConverterNodeType;
    }
           
    @Override
    public String toString() {
        return "PDDLConverter (" + this.getUrl() + ")";
    }
    
    @Override
    public void end() {
        LOG.info("PDDLConverter (" + getStatus() + ")");
    }
    
    @Override
    public EvaluationResult.Result simulateNodeLogic(final EvaluationResult result, final Resource root) {
        return EvaluationResult.Result.UNCLEAR;
    }
    
    @Override
    public LeafStatus executeLeaf() {
        try {
            if (converterOWL()) {
                String report = toString() + " SUCCEEDED";
		LOG.info(report);
		return new LeafStatus(Status.SUCCEEDED, report);
            } else {
                String report = toString() + " FAILED";
		LOG.info(report);
		return new LeafStatus(Status.FAILED, report);
            }
        } catch (IOException | QueryEvaluationException | URISyntaxException | OWLException ex) {  
            LOG.debug(toString() + ex);
            LOG.info(toString() + " FAILED due to conversion errors");
            return  new LeafStatus(Status.FAILED, toString() + " FAILED due to conversion errors");
        }
    }
    
    /*private boolean converterOWL() throws QueryEvaluationException, URISyntaxException, IOException {
        boolean result = true;
       
        getFilesfromTriples();    
        //if(getFilesfromTriples()){
                    //call the converter
            //} else {
                    //There is no triple in the LSR
                    //return a predicate that the agent does not know the LSR
                    
            //}                
        return result;
            
    }*/
    
    private boolean converterOWL() throws QueryEvaluationException, URISyntaxException, IOException, OWLException {
        
        List<Model> fullModelList;
        Model singleModel;
        Repository repo = BTUtil.getInitializedRepository(this.getObject(), listServicesQuery.getOriginBase());
        //Model model = listServicesQuery.getResult(repo);
        List<BindingSet> listTriples = listServicesQuery.getResult(repo);
        System.out.println("There are " + listTriples.size() + " triples in the SELECT query result" );
        if(listTriples.isEmpty()){
            return false;
        }
        else {
            singleModel = getModelfromBindingSet(listTriples);
            fullModelList = getModelsByService(singleModel);
            System.out.println(fullModelList.size() + " models were sent");
            createPDDL(fullModelList);
        }        
        return true;
    }
    
    private Model getModelfromBindingSet(List<BindingSet> listTriples){ 
        Iterator<BindingSet> itrListTriples = listTriples.iterator();
        Set<String> usedBindings;
        Iterator<String> itPosSingleTriple; 
        List<Value> tripleResult;
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
           
            IRI subject = PDDLConverterVocabulary.FACTORY.createIRI(tripleResult.get(0).toString());
            IRI predicate = PDDLConverterVocabulary.FACTORY.createIRI(tripleResult.get(1).toString());
            IRI context = PDDLConverterVocabulary.FACTORY.createIRI(tripleResult.get(3).toString());
            if(tripleResult.get(2) instanceof Literal){
                Literal object = PDDLConverterVocabulary.FACTORY.createLiteral(tripleResult.get(2).toString());
                //tripleStatement = PDDLConverterVocabulary.FACTORY.createStatement(subject, predicate, object);
                builder.namedGraph(context).subject(subject).add(predicate, object);                
            }
            else{
                IRI object = PDDLConverterVocabulary.FACTORY.createIRI(tripleResult.get(2).toString());
                builder.namedGraph(context).subject(subject).add(predicate, object);
                //tripleStatement = PDDLConverterVocabulary.FACTORY.createStatement(subject, predicate, object);
            }
            //modelFromBinding.add(tripleStatement);
          
        }
        modelFromBinding = builder.build();
        System.out.println("There are ".concat(Integer.toString(modelFromBinding.size())).concat(" statements in the model"));
        return modelFromBinding;
    }
    
    /*private List<Model> getModelsByService(Model model){               
        List<Model> fullModelList;
        fullModelList = new ArrayList<>();
        
        //Extract the resources of type ontology and store them as a list
        List<String> ontoList = new ArrayList<>();
        Model ontoModelSeg = model.filter(null, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, org.eclipse.rdf4j.model.vocabulary.OWL.ONTOLOGY);
        if(!ontoModelSeg.isEmpty()){
            for (Statement stOnto: ontoModelSeg) {
                IRI ontoName = (IRI)stOnto.getSubject();
                ontoList.add(ontoName.toString());
            }
            System.out.println("\n\n ******* the list of onto elements are ****** " + ontoList);
        }
        
        //Obtain the list of services from the model
        Model modelServices;        
        modelServices = model.filter(null, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, PDDLConverterVocabulary.servicesFilter);
        Model segmentedModel;        
        String serviceNamespace;
        for (Statement st: modelServices) {
            segmentedModel = new LinkedHashModel();
            IRI obj = (IRI)st.getSubject();
            serviceNamespace = obj.getNamespace();
            System.out.println("The services are " + obj);
            System.out.println("The namespace of the service is " + serviceNamespace);
            Model modelOnto;
            int pos = findPosition(ontoList, serviceNamespace);
            if(pos >= 0){
                IRI ontoDescrip = FACTORY.createIRI(ontoList.get(pos));
                modelOnto = model.filter(ontoDescrip, null, null);
                segmentedModel.addAll(modelOnto);
                modelOnto.getNamespaces().stream().forEach(segmentedModel::setNamespace);
            
                Model servDescrip = model.filter(obj, null , null);
                segmentedModel.addAll(servDescrip);
                servDescrip.getNamespaces().stream().forEach(segmentedModel::setNamespace);
                //Retrieve the object of the statements "describedBy" and "presents"
                for (Statement stServices: servDescrip) {
                    IRI objServ = (IRI)stServices.getObject();
                    if(!stServices.getPredicate().toString().contains(org.eclipse.rdf4j.model.vocabulary.RDF.TYPE.toString())){
                        Model modelProPro = model.filter(objServ, null , null);
                        segmentedModel.addAll(modelProPro);
                        modelProPro.getNamespaces().stream().forEach(segmentedModel::setNamespace);
                        for (Statement stProProStatement: modelProPro) {
                            Value paramTyp = stProProStatement.getObject();
                            System.out.println("The value of paramTyp is " + paramTyp);
                            if(paramTyp instanceof IRI){
                                IRI rsc = (IRI)paramTyp;
                                Model modelParam = model.filter(rsc, null , null);
                                segmentedModel.addAll(modelParam);
                                modelParam.getNamespaces().stream().forEach(segmentedModel::setNamespace);
                            }
                        }
                    }               
                }
            }
            System.out.println("The final resulted model is ");
            segmentedModel.forEach(System.out::println);
            System.out.println("The number of triples for the service ".concat(obj.toString()).concat(" is ").concat(Integer.toString(segmentedModel.size())));
            fullModelList.add(segmentedModel);
        }
        System.out.println("I created ".concat(Integer.toString(fullModelList.size())).concat(" models to create the files"));
        return fullModelList;
    }*/
    
    private List<Model> getModelsByService(Model model){ 
       List<Model> fullModelList = new ArrayList<>();
       Set<Resource> ctxList = model.contexts();
       System.out.println("The list of context are " + ctxList);
       Iterator<Resource> itCtx = ctxList.iterator();
       
       while(itCtx.hasNext()){
           Resource ctx = itCtx.next();
           Model segmented = model.filter(null, null, null, ctx);
           fullModelList.add(segmented);
           System.out.println("For the context " + ctx.toString() + " the model is");
           //segmented.forEach(System.out::println);
       }
       return fullModelList;
    }
    
    /*private int findPosition(List<String> nsList, String serviceNS){
        int pos = -1;
        for(int i = 0; i < nsList.size(); i++){
            if(serviceNS.startsWith(nsList.get(i))){
                pos = i;
                System.out.println("The position is " + pos);
                break;
            }
        }     
        return pos;
    }*/
    
    private void createPDDL(List<Model> genModel) throws IOException, URISyntaxException, OWLException {
        //Create the file
        String myPath = getClass().getResource("/").toURI().toString();
        String tempDirPDDL = "/tempDirPDDL/";
	URI myuri = new URI(myPath.concat(tempDirPDDL));
	Path mypathFolder = Paths.get(myuri);
	Files.createDirectories(mypathFolder);
        File myAbsoluteFile = null;  
        FileOutputStream outFile = null;
        int indexList = 0;
        Iterator<Model> itModelList = genModel.iterator();
        System.out.println(genModel.size() + " models were received");
        while(itModelList.hasNext()){
            Model currentModel = itModelList.next();
            myAbsoluteFile = new File(mypathFolder.toString().concat("/exportedFile".concat(Integer.toString(indexList)).concat(".owl")));
            outFile = new FileOutputStream(myAbsoluteFile);
            RDFHandler writer = Rio.createWriter(RDFFormat.RDFXML , outFile);
            Rio.write(currentModel, writer);
            indexList++;
        }    
        outFile.close();
        
        //invoke the converter
        /*OWLS2PDDL converter = new OWLS2PDDL("domainWelcome");
	File[] files = new File(mypathFolder.toString()).listFiles();
	for (File file : files) {
            if (!file.isDirectory() && file.getName().contains(".owl")) {
		converter.addServices(file.toURI());
		System.out.println("I am adding the file for the converter " + file.getName());
            }
	}
        // Get the files as strings		
	String pddlVersion = converter.getDomain().makePDDLTextDocument(false, converter.getFilesWithPddl());
	System.out.println("The Domain is \n" + pddlVersion);
        
        //Store the triple
        ModelBuilder builder = new ModelBuilder();
        builder.setNamespace("welcome", PDDLConverterVocabulary.WELCOME_NS);
        builder.subject("welcome:welcomeServices").add("welcome:hasPDDL", pddlVersion);
        Model pddlModel = builder.build();
        this.getObject().getLocalServicesBeliefs().update(pddlModel);  */     
    }
}

