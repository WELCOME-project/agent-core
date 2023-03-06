/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dfki.asr.ajan.pluginsystem.sscpddl2sparql.extensions;

import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.LeafStatus;
import de.dfki.asr.ajan.behaviour.nodes.common.TreeNode;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorAskQuery;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorConstructQuery;
import de.dfki.asr.ajan.common.AJANVocabulary;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.sscpddl2sparql.vocabularies.PDDL2SPARQLVocabulary;
import java.io.IOException;
//import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.repository.Repository;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pf4j.Extension;

/**
 *
 * @author ejara
 */
@Extension
@RDFBean("ssc:PDDL2SPARQL")
public class SSCPDDL2SPARQL extends AbstractTDBLeafTask implements NodeExtension, TreeNode {
     private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SSCPDDL2SPARQL.class);
    
    @Getter
    @Setter
    @RDFSubject
    private String url;
    
    @RDF("rdfs:label")
    @Getter @Setter
    private String label;
   
    @RDF("ssc:servicePrecondition")
    @Getter @Setter
    private BehaviorConstructQuery servicePreconditionQuery;
    
    @Override
    public Resource getType() {
        return PDDL2SPARQLVocabulary.PDDLPDDL2SPARQLType;
    }
           
    @Override
    public String toString() {
        return "PDDL2SPARQLConverter (" + this.getUrl() + ")";
    }
    
    @Override
    public void end() {
        LOG.info("PDDL2SPARQL (" + getStatus() + ")");
    }
    
    @Override
    public EvaluationResult.Result simulateNodeLogic(final EvaluationResult result, final Resource root) {
        return EvaluationResult.Result.UNCLEAR;
    }
    
    @Override
    public LeafStatus executeLeaf() {
        try {
            if (converterPDDL2SPARQL()) {
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
            LOG.info(toString() + " FAILED due to conversion errors");
            return  new LeafStatus(Status.FAILED, toString() + " FAILED due to conversion errors");
        }
    }
    
    private boolean converterPDDL2SPARQL() throws URISyntaxException, IOException {
        boolean respFlag = false;
        Repository repo = BTUtil.getInitializedRepository(this.getObject(), servicePreconditionQuery.getOriginBase());
        Model modelResult = servicePreconditionQuery.getResult(repo);
        Iterator<Statement> itmodelResult = modelResult.iterator();
       if(itmodelResult.hasNext()){
           Statement singleStm = itmodelResult.next();
           //We are sure that the precondition is a literal. The expressionBody only accepts String
           Literal precon = (Literal)singleStm.getObject(); 
           System.out.println("The precondition sent to the converter is " + precon.toString());
           String finalInput;
           if(precon.toString().contains("\"")){
               finalInput = precon.toString().replace("\"", "");
           }
           else 
               finalInput = precon.toString();
           String SPARQL = de.dfki.asr.ajan.plugin.converter.PDDL2SPARQL.pddl2sparql(finalInput);
           System.out.println("The output of the converter is \n" + SPARQL);
           
           //Query the LAKR with the output of the converter
           BehaviorAskQuery query = new BehaviorAskQuery();
           query.setSparql(SPARQL);
           Repository agentBeliefbase = BTUtil.getInitializedRepository(this.getObject(), servicePreconditionQuery.getTargetBase());
            //URI agentBeliefbase = new URI(AJANVocabulary.AGENT_KNOWLEDGE.toString());
           boolean servicePrecondEval = query.getResult(agentBeliefbase);           
           System.out.println("The result of the ASK query is " + servicePrecondEval);
           //Literal SPARQLobject = PDDL2SPARQLVocabulary.FACTORY.createLiteral(SPARQL);  
           
           Literal resultTripleObject = PDDL2SPARQLVocabulary.FACTORY.createLiteral(servicePrecondEval);
           Statement nameStatement = PDDL2SPARQLVocabulary.FACTORY.createStatement(PDDL2SPARQLVocabulary.subjectTriple, PDDL2SPARQLVocabulary.predicateTriple, resultTripleObject);
           Model responseModel = new LinkedHashModel();   
           responseModel.add(nameStatement);
           System.out.println("The model added to the LAKR is " + responseModel);
           if (servicePreconditionQuery.getTargetBase().toString().equals(AJANVocabulary.EXECUTION_KNOWLEDGE.toString())) {
                this.getObject().getExecutionBeliefs().update(responseModel);
           } else if (servicePreconditionQuery.getTargetBase().toString().equals(AJANVocabulary.AGENT_KNOWLEDGE.toString())) {
		this.getObject().getAgentBeliefs().update(responseModel);
           } else {
                this.getObject().getLocalServicesBeliefs().update(responseModel);
           }
           
           respFlag = true; 
       }
       else 
           respFlag = false;
       return respFlag;
        
    }
}
