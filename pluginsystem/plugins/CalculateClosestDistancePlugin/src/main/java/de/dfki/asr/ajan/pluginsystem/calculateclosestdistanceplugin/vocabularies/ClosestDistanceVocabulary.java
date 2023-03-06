/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dfki.asr.ajan.pluginsystem.calculateclosestdistanceplugin.vocabularies;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 *
 * @author ejara
 */
public class ClosestDistanceVocabulary {
    public final static ValueFactory FACTORY = SimpleValueFactory.getInstance();
    public final static IRI ClosestDistanceNodeType = FACTORY.createIRI("http://www.ajan.de/behavior/bt-ns#ClosestLocation");
    public final static IRI predLong = FACTORY.createIRI("https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#long");
    public final static IRI predLat = FACTORY.createIRI("https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#lat");
    public final static IRI predClosestAddress = FACTORY.createIRI("https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#hasClosestRegistrationOffice");
}
