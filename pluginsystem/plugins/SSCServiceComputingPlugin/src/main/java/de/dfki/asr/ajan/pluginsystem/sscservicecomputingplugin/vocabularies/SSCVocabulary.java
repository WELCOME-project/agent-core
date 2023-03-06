/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dfki.asr.ajan.pluginsystem.sscservicecomputingplugin.vocabularies;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 *
 * @author ejara
 */
public class SSCVocabulary {
    public final static ValueFactory FACTORY = SimpleValueFactory.getInstance();
    public final static IRI SSCType = FACTORY.createIRI("http://welcome/ajan/servicecomputing#");
    public final static IRI SSCName = FACTORY.createIRI("http://www.ajan.de/behavior/scs-ns#SSCPlugin");
    public final static IRI SSCPred = FACTORY.createIRI("http://www.ajan.de/behavior/scs-ns#hasResultOffer");
    public final static IRI filteredTextDescription = FACTORY.createIRI("http://www.daml.org/services/owl-s/1.1/Profile.owl#textDescription");
    public final static IRI filteredServiceName = FACTORY.createIRI("http://www.daml.org/services/owl-s/1.1/Profile.owl#serviceName");
    public final static String profileNS = "http://www.daml.org/services/owl-s/1.1/Profile.owl#";
    public final static String serviceNS = "http://www.daml.org/services/owl-s/1.1/Service.owl#";
    public final static String processNS = "http://www.daml.org/services/owl-s/1.1/Process.owl#";
    public final static String welcomeNS = "https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#";
}
