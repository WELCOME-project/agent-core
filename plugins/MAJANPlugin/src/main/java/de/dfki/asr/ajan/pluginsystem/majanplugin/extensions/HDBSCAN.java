/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dfki.asr.ajan.pluginsystem.majanplugin.extensions;
import ca.ualberta.cs.hdbscanstar.Constraint;
import ca.ualberta.cs.hdbscanstar.Constraint.CONSTRAINT_TYPE;
import ca.ualberta.cs.hdbscanstar.HDBSCANStarRunner;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.LeafStatus;
import de.dfki.asr.ajan.behaviour.nodes.common.TreeNode;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorConstructQuery;
import de.dfki.asr.ajan.common.AJANVocabulary;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.majanplugin.exceptions.CSGPSolverInputException;
import de.dfki.asr.ajan.pluginsystem.majanplugin.exceptions.ConstraintsException;
import de.dfki.asr.ajan.pluginsystem.majanplugin.exceptions.HDBSCANInputException;
import de.dfki.asr.ajan.pluginsystem.majanplugin.utils.Utils;
import de.dfki.asr.ajan.pluginsystem.majanplugin.vocabularies.MAJANVocabulary;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.text.similarity.SimilarityScore;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.repository.Repository;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pf4j.Extension;

/**
 *
 * @author Akbar
 */
@Extension
@RDFBean("bt:HDBSCAN")
public class HDBSCAN extends AbstractTDBLeafTask implements NodeExtension, TreeNode{
    
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(HDBSCAN.class);
    
    @Getter
    @Setter
    @RDFSubject
    private String url;
    
    @RDF("rdfs:label")
    @Getter @Setter
    private String label;
   
    @RDF("bt:query")
    @Getter @Setter
    private BehaviorConstructQuery constructQuery;
    
    @Override
    public Resource getType() {
        return MAJANVocabulary.HDBSCAN_TYPE;
    }
           
    @Override
    public String toString() {
        return "HDBSCAN (" + label + ")";
    }
    
    @Override
    public void end() {
        LOG.info("HDBSCAN (" + getStatus() + ")");
    }
    
    @Override
    public EvaluationResult.Result simulateNodeLogic(final EvaluationResult result, final Resource root) {
        return EvaluationResult.Result.UNCLEAR;
    }
    
    @Override
    public LeafStatus executeLeaf() {
        try {
            if (solve()) {
                String report = toString() + " SUCCEEDED";
		LOG.info(report);
		return new LeafStatus(Status.SUCCEEDED, report);
            } else {
                String report = toString() + " FAILED";
		LOG.info(report);
		return new LeafStatus(Status.FAILED, report);
            }
        } catch (URISyntaxException | HDBSCANInputException | ConstraintsException ex) {
            String report = toString() + " FAILED due to \""+ex.getMessage()+"\"";
            LOG.info(report);
            return new LeafStatus(Status.FAILED, report);
        }
    }

    public int[] runSolver(Double[][] distanceScores, int minPts, int minClSize){
        HDBSCANStarRunner hdbscanStarRunner = new HDBSCANStarRunner();
        int[] clusterLabels = hdbscanStarRunner.execute(distanceScores, null, minPts, minClSize);
        return clusterLabels;
    }

    private boolean solve() throws URISyntaxException, HDBSCANInputException, ConstraintsException {
        boolean respFlag = false;
        Double[][] distanceScores = null; 
        double perfectMatchScore = 0;
        int numOfAgents = 0;
        int minPts = 1;
        int minClSize = 2;
        //ArrayList<Constraint> constraints = null;
        // key: constraint type - {0:ML, 1:CL}
        // value: pair of agents
        ArrayList<int[]> mlList = null, clList = null;
        int[] clusterLabels = null;
        String chcProblemId = null;
        List<Value> agentNames = new ArrayList<>();

        Repository repo = BTUtil.getInitializedRepository(getObject(), constructQuery.getOriginBase());
        Model rdfInputModel = constructQuery.getResult(repo);        
        
        Utils.printRDF4JModel(rdfInputModel, LOG);

        // Extract MacProblemId from model
        Set<Value> valueSet = rdfInputModel.filter(null, MAJANVocabulary.HAS_ID, null).objects();
        if(!valueSet.isEmpty()){
            chcProblemId=valueSet.iterator().next().stringValue();
        }else{
            throw new HDBSCANInputException("MAC Problem Id is not specified (i.e. no value exists for the following predicate: "+
                    MAJANVocabulary.HAS_ID+")");
        } // end
          //  System.out.println("------------MACProblemId: "+chcProblemId);

        // Extract NumOfAgents from Model
        valueSet = rdfInputModel.filter(null, MAJANVocabulary.HAS_NUMBER_OF_AGENTS, null).objects();
        if(!valueSet.isEmpty()){
            numOfAgents=Integer.valueOf(valueSet.iterator().next().stringValue());
        }else{
            throw new HDBSCANInputException("Number of agents is not specified (i.e. no value exists for predicate "+
                    MAJANVocabulary.HAS_NUMBER_OF_AGENTS+")");
        } // end
        // System.out.println("------------NumOfAgents: "+numOfAgents);

        // Extract Min Points value from model
        valueSet = rdfInputModel.filter(null, MAJANVocabulary.HAS_MIN_POINTS_PARAM, null).objects();
        if(!valueSet.isEmpty()){
            minPts=Integer.valueOf(valueSet.iterator().next().stringValue());
        }else{
            LOG.warn("Min Points parameter for HDBSCAN is not specified. Proceeding with the default value: "+minPts);
        } // end
         
         // Extract Min Cluster Size value from model
         valueSet = rdfInputModel.filter(null, MAJANVocabulary.HAS_MIN_CLUSTER_SIZE_PARAM, null).objects();
         if(!valueSet.isEmpty()){
             minClSize=Integer.valueOf(valueSet.iterator().next().stringValue());
         }else{
             LOG.warn("Min Cluster Size parameter for HDBSCAN is not specified. Proceeding with the default value: "+minClSize);
         } // end
           
        // Extract Perfect Match Score from Model
        valueSet = rdfInputModel.filter(null, MAJANVocabulary.HAS_PERFECT_MATCH_SCORE, null).objects();
        if(!valueSet.isEmpty()){
            perfectMatchScore=Double.valueOf(valueSet.iterator().next().stringValue());
        }else{
            throw new HDBSCANInputException("Perfect match score is not specified (i.e. no value exists for predicate "+
                    MAJANVocabulary.HAS_PERFECT_MATCH_SCORE+")");
        } // end
        //System.out.println("------------perfectMatchScore: "+perfectMatchScore);

        // Extract Agent Names from Model
        valueSet=rdfInputModel.filter(null, MAJANVocabulary.HAS_PARTICIPANTS, null).objects();
        if(valueSet.size()!=numOfAgents){
            throw new HDBSCANInputException("Amount of participating agents ("+valueSet.size()+") is different "
                    + "from the given \"numberOfAgents="+numOfAgents+"\" information.");
        }
        Iterator<Value> valueIterator = valueSet.iterator();
        while(valueIterator.hasNext()){
            agentNames.add(valueIterator.next());
        } // end
        
       // for (Value agentName : agentNames) {
            //System.out.println("------------agentName: "+agentName);
       // }
       
        // Extract Must Link Connections
        mlList = Utils.getConstraints(rdfInputModel, agentNames, Utils.CONSTRAINT_TYPE.MUST_LINK);
        // end
        
        // Extract Cannot Link Connections
        clList = Utils.getConstraints(rdfInputModel, agentNames, Utils.CONSTRAINT_TYPE.CANNOT_LINK);
        // end
       
        
        // Extract Distance Scores from Model
        distanceScores = new Double[numOfAgents][numOfAgents];
        // worst distance (i.e. perfect match score) is set to all pairs initially in case some distance scores are missing in the rdf 
        // input. 
        for (Double[] distanceScore : distanceScores) {
            Arrays.fill(distanceScore, perfectMatchScore);
        }
        for (int i = 0; i < numOfAgents; i++) {
            Set<Resource> subjectsOfSubjectAgent = rdfInputModel.filter(null, MAJANVocabulary.HAS_SUBJECT_AGENT,
                    agentNames.get(i)).subjects();
            for (int j = 0; j < numOfAgents; j++) {
                if(i==j){
                    distanceScores[i][j] = 0.0;
                }else{
                    for (Resource subjectOfSubjectAgent : subjectsOfSubjectAgent) {
                        if(rdfInputModel.contains(subjectOfSubjectAgent, MAJANVocabulary.HAS_OBJECT_AGENT, agentNames.get(j))){
                            valueSet = rdfInputModel.filter(subjectOfSubjectAgent, MAJANVocabulary.HAS_DISTANCE_SCORE, null).objects();
                            
                            //valueSet = rdfInputModel.filter(subjectOfSubjectAgent, MAJANVocabulary.DistanceScorePre, null).objects();
                            if(valueSet.isEmpty()){
                              //  throw new HDBSCANInputException("No Similarity Score is specified between <"+agentNames.get(i)+"> and <" 
                                //        + agentNames.get(j));
                                LOG.warn("No Distance Score is specified between <"+agentNames.get(i)+"> and <"
                                        + agentNames.get(j) + ". Assiging " + perfectMatchScore + " as their distance to be able "
                                                + "to proceed.");
                                distanceScores[i][j] = perfectMatchScore;
                            }else{
                                double distanceScore = Double.valueOf(valueSet.iterator().next().stringValue());
                                distanceScores[i][j] = distanceScore;
                                distanceScores[j][i] = distanceScore;            
                            }
                            //System.out.println(agentNames.get(i)+" vs "+agentNames.get(j)+" distance: "+distanceScores[i][j]);
                        }
                    }
                }
            }
        }
        distanceScores = applyConstraints(distanceScores, mlList, clList);
        
        clusterLabels = runSolver(distanceScores, minPts, minClSize);        
        printClusters(clusterLabels, agentNames);
        
        // Create Response Model
        ModelBuilder builder=new ModelBuilder();
        builder.setNamespace("welcome", MAJANVocabulary.WELCOME_NAMESPACE.toString())
                .setNamespace("ajan", MAJANVocabulary.AJAN_NAMESPACE.toString());
        
        BNode clusteringBNode = MAJANVocabulary.FACTORY.createBNode();
        builder.subject(clusteringBNode)
                .add(org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, MAJANVocabulary.CLUSTERING)
                .add(MAJANVocabulary.IS_SOLUTION_OF, chcProblemId);
        
        HashMap<Integer, Resource> clusterSubjectStorage = new HashMap<>();
        
        for(int i = 0; i < clusterLabels.length; i++){
            BNode clusterBnode = null;
            // 0 as cluster label means Singleton cluster
            if(clusterLabels[i] == 0){
                clusterBnode = MAJANVocabulary.FACTORY.createBNode();
            }else{
                if(!clusterSubjectStorage.containsKey(clusterLabels[i])){
                    clusterBnode = MAJANVocabulary.FACTORY.createBNode();
                    clusterSubjectStorage.put(clusterLabels[i], clusterBnode);
                }else{
                    clusterBnode = (BNode) clusterSubjectStorage.get(clusterLabels[i]);
                }
            }
            builder.subject(clusteringBNode)
                    .add(MAJANVocabulary.HAS_MEMBERS, clusterBnode)
                    .subject(clusterBnode)
                    .add(org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, MAJANVocabulary.CLUSTER)
                    .add(MAJANVocabulary.HAS_MEMBERS, agentNames.get(i))
                    .add(MAJANVocabulary.IS_CLUSTER_OF, clusteringBNode)
                    //.subject((Resource) agentNames.get(i))
                    //.add(MAJANVocabulary.MemberOfPre, clusterBnode)
                    ;
        }
        Model responseModel = builder.build();
        //Utils.printRDF4JModel(responseModel, LOG);

        if(constructQuery.getTargetBase().toString().equals(AJANVocabulary.EXECUTION_KNOWLEDGE.toString())){
            this.getObject().getExecutionBeliefs().update(responseModel);
        }else if(constructQuery.getTargetBase().toString().equals(AJANVocabulary.AGENT_KNOWLEDGE.toString())){
            this.getObject().getAgentBeliefs().update(responseModel);
        }else if(constructQuery.getTargetBase().toString().equals(AJANVocabulary.LOCAL_AGENTS_KNOWLEDGE.toString())){
            this.getObject().getLocalAgentsBeliefs().update(responseModel);
        }else if(constructQuery.getTargetBase().toString().equals(AJANVocabulary.LOCAL_SERVICES_KNOWLEDGE.toString())){
            this.getObject().getLocalServicesBeliefs().update(responseModel);
        }

        respFlag=true;
        return respFlag;
    }
    
    private boolean executeHdbscanOld() throws URISyntaxException, HDBSCANInputException {
        boolean respFlag = false;
        Double[][] distanceScores = null; // done
        Double[][] asymSimilarityScores = null; // done
        double perfectMatchScore = 0;// done
        int numOfAgents = 0;// done
        ArrayList<Constraint> constraints = null;
        int[] clusterLabels = null;
        String chcProblemId = null;// done
        List<Value> agentNames = new ArrayList<>();// done

        Repository repo = BTUtil.getInitializedRepository(this.getObject(), constructQuery.getOriginBase());
        Model rdfInputModel = constructQuery.getResult(repo);
        
        //Utils.printRDF4JModel(rdfInputModel, LOG);

        // Extract MacProblemId from model
        Set<Value> valueSet = rdfInputModel.filter(null, MAJANVocabulary.HAS_MAC_PROBLEM_ID, null).objects();
        if(!valueSet.isEmpty()){
            chcProblemId=valueSet.iterator().next().stringValue();
        }else{
            throw new HDBSCANInputException("CHC Problem Id is not specified (i.e. no value exists for the following predicate: "+
                    MAJANVocabulary.HAS_MAC_PROBLEM_ID+")");
        } // end
        //    System.out.println("------------MACProblemId: "+chcProblemId);

        // Extract NumOfAgents from Model
        valueSet = rdfInputModel.filter(null, MAJANVocabulary.HAS_NUMBER_OF_AGENTS, null).objects();
        if(!valueSet.isEmpty()){
            numOfAgents=Integer.valueOf(valueSet.iterator().next().stringValue());
        }else{
            throw new HDBSCANInputException("Number of agents is not specified (i.e. no value exists for predicate "+
                    MAJANVocabulary.HAS_NUMBER_OF_AGENTS+")");
        } // end
        //    System.out.println("------------NumOfAgents: "+numOfAgents);

        // Extract Perfect Match Score from Model
        valueSet = rdfInputModel.filter(null, MAJANVocabulary.HAS_PERFECT_MATCH_SCORE, null).objects();
        if(!valueSet.isEmpty()){
            perfectMatchScore=Double.valueOf(valueSet.iterator().next().stringValue());
        }else{
            throw new HDBSCANInputException("Perfect match score is not specified (i.e. no value exists for predicate "+
                    MAJANVocabulary.HAS_PERFECT_MATCH_SCORE+")");
        } // end
       // System.out.println("------------perfectMatchScore: "+perfectMatchScore);

        // Extract Agent Names from Model
        valueSet=rdfInputModel.filter(null, MAJANVocabulary.HAS_PARTICIPANTS, null).objects();
        if(valueSet.size()!=numOfAgents){
            throw new HDBSCANInputException("Amount of participating agents ("+valueSet.size()+") is different "
                    + "from the given \"numberOfAgents="+numOfAgents+"\" information.");
        }
        Iterator<Value> valueIterator = valueSet.iterator();
        while(valueIterator.hasNext()){
            agentNames.add(valueIterator.next());
        } // end
        
        /*for (Value agentName : agentNames) {
            System.out.println("------------agentName: "+agentName);
        }*/
        
        // Extract Asymmetric Similarity Matrix, then compute Reciprocal Scores and then compute Distance scores
        asymSimilarityScores = new Double[numOfAgents][numOfAgents];
        distanceScores = new Double[numOfAgents][numOfAgents];
        for (int i = 0; i < numOfAgents; i++) {
            Set<Resource> subjectsOfSubjectAgent = rdfInputModel.filter(null, MAJANVocabulary.HAS_SUBJECT_AGENT, 
                    agentNames.get(i)).subjects();
            for (int j = 0; j < numOfAgents; j++) {
                if(i==j){
                    asymSimilarityScores[i][j] = perfectMatchScore;
                    distanceScores[i][j] = 0.0;
                }else{
                    for (Resource subjectOfSubjectAgent : subjectsOfSubjectAgent) {
                        if(rdfInputModel.contains(subjectOfSubjectAgent, MAJANVocabulary.HAS_OBJECT_AGENT, agentNames.get(j))){
                            valueSet = rdfInputModel.filter(subjectOfSubjectAgent, MAJANVocabulary.HAS_SIMILARITY_SCORE, null).objects();
                            if(valueSet.isEmpty()){
                              //  throw new HDBSCANInputException("No Similarity Score is specified between <"+agentNames.get(i)+"> and <" 
                                //        + agentNames.get(j));
                                LOG.warn("No Similarity Score is specified between <"+agentNames.get(i)+"> and <"
                                        + agentNames.get(j));
                                asymSimilarityScores[i][j] = 0.0;
                            }else{
                                double similarityScore = Double.valueOf(valueSet.iterator().next().stringValue());
                                                    
                                if(asymSimilarityScores[i][j]==null || asymSimilarityScores[i][j]<similarityScore){
                                    asymSimilarityScores[i][j]=similarityScore;
                                }
                            }
                            if(asymSimilarityScores[j][i]!=null){
                                double reciprocalScore = computeReciprocalScore(asymSimilarityScores[i][j], asymSimilarityScores[j][i]);
                                double distanceScore = perfectMatchScore - reciprocalScore;
                                distanceScores[i][j] = distanceScore;
                                distanceScores[j][i] = distanceScore;
                            }
                        }
                    }
                }
            }
        } // end

        // Execute HDBSCAN Algorithm to compute Cluster Labels
        HDBSCANStarRunner hdbscanStarRunner = new HDBSCANStarRunner();
        clusterLabels = hdbscanStarRunner.execute(distanceScores, constraints, 1, 2);
        printClusters(clusterLabels, agentNames);
        
        // Create Response Model
        ModelBuilder builder=new ModelBuilder();
        builder.setNamespace("welcome", MAJANVocabulary.WELCOME_NAMESPACE.toString())
                .setNamespace("ajan", MAJANVocabulary.AJAN_NAMESPACE.toString());
        
        BNode clusteringBNode = MAJANVocabulary.FACTORY.createBNode();
        builder.subject(clusteringBNode)
                .add(org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, MAJANVocabulary.CLUSTERING)
                .add(MAJANVocabulary.IS_SOLUTION_OF, chcProblemId);
        
        HashMap<Integer, Resource> clusterSubjectStorage = new HashMap<>();
        
        for(int i = 0; i < clusterLabels.length; i++){
            BNode clusterBnode = null;
            if(clusterLabels[i] == 0){
                clusterBnode = MAJANVocabulary.FACTORY.createBNode();
            }else{
                if(!clusterSubjectStorage.containsKey(clusterLabels[i])){
                    clusterBnode = MAJANVocabulary.FACTORY.createBNode();
                    clusterSubjectStorage.put(clusterLabels[i], clusterBnode);
                }else{
                    clusterBnode = (BNode) clusterSubjectStorage.get(clusterLabels[i]);
                }
            }
            builder.subject(clusteringBNode)
                    .add(MAJANVocabulary.HAS_MEMBERS, clusterBnode)
                    .subject(clusterBnode)
                    .add(org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, MAJANVocabulary.CLUSTER)
                    .add(MAJANVocabulary.HAS_MEMBERS, agentNames.get(i))
                    .add(MAJANVocabulary.IS_CLUSTER_OF, clusteringBNode)
                    .subject((Resource) agentNames.get(i))
                    .add(MAJANVocabulary.IS_MEMBER_OF, clusterBnode);
        }
        
        // Dont Delete
        // This (responseModel) model contains the clusters which are found by HDBSCAN. This model doesn't contain the Asymmetric Similarity Values. 
        // This means, Asymmetric Similarity Values will not be written to LAKR. I don't think it is necessary for anyone, plus there 
        // will be huge amount of similarity values. So if it is not necessary, then why would agents store it. 
        // However, if anyone wants to store the similarity values to LAKR, then all you need to do is to iterate over 
        // asymSimilarityScores and write each score to the responseModel. The rdf template for a similarity score is described in 
        // RDF Representation document but I give it below as well:
       /*
        _:bNodeSimilarityScore      ajan:hasSubject      ?subjectAgent;
                                    ajan:hasObject       ?objectAgent;        
                                    welcome:isComputedForProblemId       ?chcProblemId;
                                    ajan:hasSimilarityScore         ?similarityScore.
        */
        

        Model responseModel = builder.build();
        //Utils.printRDF4JModel(responseModel, LOG);

        if(constructQuery.getTargetBase().toString().equals(AJANVocabulary.EXECUTION_KNOWLEDGE.toString())){
            this.getObject().getExecutionBeliefs().update(responseModel);
        }else if(constructQuery.getTargetBase().toString().equals(AJANVocabulary.AGENT_KNOWLEDGE.toString())){
            this.getObject().getAgentBeliefs().update(responseModel);
        }else if(constructQuery.getTargetBase().toString().equals(AJANVocabulary.LOCAL_AGENTS_KNOWLEDGE.toString())){
            this.getObject().getLocalAgentsBeliefs().update(responseModel);
        }else if(constructQuery.getTargetBase().toString().equals(AJANVocabulary.LOCAL_SERVICES_KNOWLEDGE.toString())){
            this.getObject().getLocalServicesBeliefs().update(responseModel);
        }

        respFlag=true;
        return respFlag;
    }
    
    private void printClusters(int[] clustering, List<Value> agentNames) {
        // Integer: cluster label
        // Arraylist<integer>: list of agents with the label
        Map<Integer, ArrayList<Integer> > clusteringMap = new HashMap<>();
        for (int i = 0; i < clustering.length; i++) {
            if(clusteringMap.containsKey(clustering[i])) {
                ArrayList<Integer> members = clusteringMap.get(clustering[i]);
                members.add(i);
                clusteringMap.replace(clustering[i], members);
            }else {
                ArrayList<Integer> members = new ArrayList<Integer>();
                members.add(i);
                clusteringMap.put(clustering[i], members);
            }
        }
        
        System.out.println("--------------Clustering Result--------------");
        int clusterId = 1;
        for (int i : clusteringMap.keySet()) {
            if(i==0) {
                System.out.print("Singleton: [");
            }else {
                System.out.print("Group"+clusterId+": [");
                clusterId++;
            }
            for (int j : clusteringMap.get(i)) {
                //System.out.print("tcn"+(j+1)+", ");
                System.out.print(agentNames.get(j) + ", ");
            }
            System.out.println("] --> " + clusteringMap.get(i).size());
        }
        System.out.println("--------------Clustering Result END--------------");
    }
    
    private void printSimilarityAndDistanceScores(Double[][] asymSimilarityScores, Double[][] distanceScores){
        System.out.println("-------------Similarity And Distance Scores-----------Start");
        for (int i = 0; i < asymSimilarityScores.length; i++) {
            for (int j = 0; j < asymSimilarityScores[i].length; j++) {
                System.out.println("Sim - i: "+(i+1)+", j: "+(j+1)+" --> "+asymSimilarityScores[i][j]);
                System.out.println("Dist - i: "+(i+1)+", j: "+(j+1)+" --> "+distanceScores[i][j]);                
            }
        }
        System.out.println("-------------Similarity And Distance Scores-----------End");

    }
    
    private Double[][] applyConstraints(Double[][] distances, ArrayList<Constraint> constraints) {
        constraints.forEach((constraint) -> {
            if (constraint.getType().equals(CONSTRAINT_TYPE.MUST_LINK)) {
                distances[constraint.getPointA()][constraint.getPointB()] = 0.0;
                distances[constraint.getPointB()][constraint.getPointA()] = 0.0;
            }else if (constraint.getType().equals(CONSTRAINT_TYPE.CANNOT_LINK)){
                distances[constraint.getPointA()][constraint.getPointB()] = Double.POSITIVE_INFINITY;
                distances[constraint.getPointB()][constraint.getPointA()] = Double.POSITIVE_INFINITY;
            }
        });
        return distances;
    }
    
    private Double[][] applyConstraints(Double[][] distances, ArrayList<int[]> mlList, ArrayList<int[]> clList) {
        for(int[] ml : mlList) {
            distances[ml[0]][ml[1]] = 0.0;
            distances[ml[1]][ml[0]] = 0.0;
        }
        for(int[] cl : clList) {
            distances[cl[0]][cl[1]] = Double.POSITIVE_INFINITY;
            distances[cl[1]][cl[0]] = Double.POSITIVE_INFINITY;
        }
        return distances;
    }
    
    private double computeReciprocalScore(double similarityScore1, double similarityScore2) {
		double reciprocalScore = 0;
		if(similarityScore1>0 && similarityScore2>0) {
			reciprocalScore = 2/((1/similarityScore1)+(1/similarityScore2));
		}
		return reciprocalScore;
	}
	
}
