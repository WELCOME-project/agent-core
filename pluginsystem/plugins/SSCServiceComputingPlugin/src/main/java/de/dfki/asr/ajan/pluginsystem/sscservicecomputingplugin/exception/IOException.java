/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dfki.asr.ajan.pluginsystem.sscservicecomputingplugin.exception;

/**
 *
 * @author ejara
 */
public class IOException extends Exception {
    public IOException(final String message) {
		super(message);
	}

	public IOException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public IOException(final Throwable cause) {
		super(cause);
	} 
    
}
