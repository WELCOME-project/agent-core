/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dfki.asr.ajan.pluginsystem.pddlconverter.exception;

/**
 *
 * @author ejara
 */
public class OWLException extends Exception {
    	public OWLException(final String message) {
		super(message);
	}

	public OWLException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public OWLException(final Throwable cause) {
		super(cause);
	}    
}
