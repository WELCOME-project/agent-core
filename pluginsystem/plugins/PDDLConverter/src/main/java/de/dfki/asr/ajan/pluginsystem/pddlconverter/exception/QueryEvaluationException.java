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
public class QueryEvaluationException extends Exception {    
    public QueryEvaluationException(final String message) {
        super(message);
    }

    public QueryEvaluationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public QueryEvaluationException(final Throwable cause) {
        super(cause);
    }    
}
