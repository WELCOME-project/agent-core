/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dfki.asr.ajan.pluginsystem.sscpddl2sparql.vocabularies;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 *
 * @author ejara
 */
public class PDDL2SPARQLVocabulary {
    public final static ValueFactory FACTORY = SimpleValueFactory.getInstance();
    public final static IRI PDDLPDDL2SPARQLType = FACTORY.createIRI("http://welcome/ajan/servicecomputing#PDDL2SPARQL");
    public final static IRI subjectTriple = FACTORY.createIRI("https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#foundService");
    public final static IRI predicateTriple = FACTORY.createIRI("https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#hasPreconditionResult");
}
