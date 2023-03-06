/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dfki.asr.ajan.pluginsystem.calculateclosestdistanceplugin.extensions;

/**
 *
 * @author ejara
 */

import de.dfki.asr.ajan.behaviour.exception.ConditionEvaluationException;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.LeafStatus;
import de.dfki.asr.ajan.behaviour.nodes.common.TreeNode;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorConstructQuery;
import de.dfki.asr.ajan.common.AJANVocabulary;
import de.dfki.asr.ajan.pluginsystem.calculateclosestdistanceplugin.vocabularies.ClosestDistanceVocabulary;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pf4j.Extension;

@Extension
@RDFBean("welcomePlugin:ClosestLocation")
public class CalculateCoordinateDistance extends AbstractTDBLeafTask implements NodeExtension, TreeNode { 
    
    private static final Logger LOG = LoggerFactory.getLogger(CalculateCoordinateDistance.class);
     
    @Getter
    @Setter
    @RDFSubject
    private String url;
    
    @RDF("rdfs:label")
    @Getter @Setter
    private String label;

    @RDF("welcomePlugin:tcnLocation")
    @Getter @Setter
    private BehaviorConstructQuery tcnLocationQuery;

    @RDF("welcomePlugin:registrationOfficeLocation")
    @Getter @Setter
    private BehaviorConstructQuery registrationOfficeLocationQuery;
    
    @Override
    public Resource getType() {
        return ClosestDistanceVocabulary.ClosestDistanceNodeType;
    }
    
    @Override
    public String toString() {
        return "CalculateCoordinateDistance (" + this.getUrl() + ")";
    }
    
    @Override
    public void end() {
	LOG.info("Status (" + getStatus() + ")");
    }
    
    @Override
    public EvaluationResult.Result simulateNodeLogic(final EvaluationResult result, final Resource root) {
        return EvaluationResult.Result.UNCLEAR;
    }
    
     @Override
    public LeafStatus executeLeaf() {
        try {
            if (performWrite()) {
                String report = toString() + " SUCCEEDED";
		LOG.info(report);
		return new LeafStatus(Status.SUCCEEDED, report);
            } else {
                String report = toString() + " FAILED";
		LOG.info(report);
		return new LeafStatus(Status.FAILED, report);
            }
        } catch (ConditionEvaluationException | URISyntaxException | QueryEvaluationException ex) {  
            LOG.debug(toString() + ex);
            LOG.info(toString() + " FAILED due to evaluation errors");
            return  new LeafStatus(Status.FAILED, toString() + " FAILED due to evaluation errors");
        }
    }
    
    private boolean performWrite() throws ConditionEvaluationException, URISyntaxException, QueryEvaluationException {
        Model model = findClosestOffice();
        if (tcnLocationQuery.getTargetBase().toString().equals(AJANVocabulary.EXECUTION_KNOWLEDGE.toString())) {
            this.getObject().getExecutionBeliefs().update(model);
        } else if (tcnLocationQuery.getTargetBase().toString().equals(AJANVocabulary.AGENT_KNOWLEDGE.toString())) {
            this.getObject().getAgentBeliefs().update(model);
        }
        return true;
    }

    private Model findClosestOffice() throws URISyntaxException{
        Repository originTCNModel = BTUtil.getInitializedRepository(getObject(), tcnLocationQuery.getOriginBase());
        Repository originOfficeModel = BTUtil.getInitializedRepository(getObject(), registrationOfficeLocationQuery.getOriginBase());
        Model tcnModel = tcnLocationQuery.getResult(originTCNModel);
        Model officeModel = registrationOfficeLocationQuery.getResult(originOfficeModel);
        List<Model> officeModelList = new ArrayList<>();
        Set<Resource> subjects = officeModel.subjects();
        for (Resource subjectResource : subjects) {
            officeModelList.add(officeModel.filter(subjectResource, null, null));
        }
        Model closestOffice = getClosestOfficeModel(tcnModel, officeModelList);
        ModelBuilder result = new ModelBuilder();
        result.subject(tcnModel.subjects().iterator().next()).add(ClosestDistanceVocabulary.predClosestAddress, closestOffice.subjects().iterator().next());
        Model m = result.build();
        
        System.out.println("Closest Office Model below:");
        printModel(closestOffice);
        System.out.println("Plugin Result:" + m);
        return m;
    }
        
    private static Model getClosestOfficeModel(Model tcnModel, List<Model> officeModels) {
        int EARTH_RADIUS_KM = 6371;
        Model closestModel = null;
        double closestDistance = 99999999999d;
        double tcnLat = getLatitudeFromModel(tcnModel);
        double tcnLong = getLongitudeFromModel(tcnModel);
        for (Model m : officeModels) {
            double officeLat = getLatitudeFromModel(m);
            double officeLong = getLongitudeFromModel(m);
            double difLat = Math.toRadians(Math.abs(tcnLat- officeLat));
            double difLong = Math.toRadians(Math.abs(tcnLong - officeLong));
            double tcnLatRad = Math.toRadians(tcnLat);
            double officeLatRad = Math.toRadians(officeLat);
            double eq = haversin(difLat) + Math.cos(tcnLatRad) * Math.cos(officeLatRad) * haversin(difLong);
            double c = 2 * Math.atan2(Math.sqrt(eq), Math.sqrt(1 - eq));
            double dist = EARTH_RADIUS_KM * c; 
            if (dist < closestDistance) {
                closestDistance = dist;
                closestModel = m;
            }
        }
        return closestModel;
    }
        
    private static Double getLatitudeFromModel(Model model) {
        Set<Value> values = model.filter(null, ClosestDistanceVocabulary.predLat, null).objects();
        assert values.size() > 0;
        try {
            return Double.parseDouble((String) values.iterator().next().stringValue());
        } catch (NumberFormatException | NullPointerException ex) {
            throw ex;
        }
    }
        
    private static Double getLongitudeFromModel(Model model) {
        Set<Value> values = model.filter(null, ClosestDistanceVocabulary.predLong, null).objects();
        assert values.size() > 0;
        try {
            return Double.parseDouble((String) values.iterator().next().stringValue());
        } catch (NumberFormatException | NullPointerException ex) {
            throw ex;
    }
        }
        
    public static double haversin(double val) {
        return Math.pow(Math.sin(val / 2), 2);
    }
        
    private static void printModel(Model model) {
        Iterator<Statement> statementIterator = model.iterator();
        while (statementIterator.hasNext()) {
            System.out.println(statementIterator.next());
        }
    }
}
