/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dfki.asr.ajan.pluginsystem.pddlconverter.vocabularies;

/**
 *
 * @author ejara
 */

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class PDDLConverterVocabulary {
    
    public final static ValueFactory FACTORY = SimpleValueFactory.getInstance();
    public final static IRI PDDLConverterNodeType = FACTORY.createIRI("http://welcome/ajan/servicecomputing#PDDLConverter");
    public final static IRI servicesFilter = FACTORY.createIRI("http://www.daml.org/services/owl-s/1.1/Service.owl#Service");
    public final static String WELCOME_NS = "http://www.semanticweb.org/welcome";
}
