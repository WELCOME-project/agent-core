/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dfki.asr.ajan.pluginsystem.calculateclosestdistanceplugin.exception;

/**
 *
 * @author ejara
 */
public class URISyntaxException extends Exception {
    
    public URISyntaxException(final String message) {
        super(message);
    }

    public URISyntaxException(final String message, final Throwable cause) {
    	super(message, cause);
    }

    public URISyntaxException(final Throwable cause) {
	super(cause);
    }    
}
