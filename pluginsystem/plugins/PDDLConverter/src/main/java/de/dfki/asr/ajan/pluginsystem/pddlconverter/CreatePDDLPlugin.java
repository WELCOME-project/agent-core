/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dfki.asr.ajan.pluginsystem.pddlconverter;

/**
 *
 * @author ejara
 */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pf4j.Plugin;
import ro.fortsoft.pf4j.PluginWrapper;
import ro.fortsoft.pf4j.RuntimeMode;

public class CreatePDDLPlugin extends Plugin {
    private static final Logger LOG = LoggerFactory.getLogger(CreatePDDLPlugin.class);

    public CreatePDDLPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        System.out.println("CreatePDDLPlugin.start()");
        if (RuntimeMode.DEVELOPMENT.equals(wrapper.getRuntimeMode())) {
            LOG.debug("CreatePDDLPlugin");
        }
    }

    @Override
    public void stop() {
        System.out.println("CreatePDDLPlugin.stop()");
    }
}
