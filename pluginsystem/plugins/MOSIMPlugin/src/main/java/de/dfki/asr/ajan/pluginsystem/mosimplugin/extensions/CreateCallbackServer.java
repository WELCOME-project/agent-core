/*
 * Copyright (C) 2020 Andre Antakli (German Research Center for Artificial Intelligence, DFKI).
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

package de.dfki.asr.ajan.pluginsystem.mosimplugin.extensions;

import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.LeafStatus;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.mosimplugin.endpoint.MCoSimulationEventCallbackHandler;
import de.dfki.asr.ajan.pluginsystem.mosimplugin.endpoint.ThriftPluginServer;
import static de.dfki.asr.ajan.pluginsystem.mosimplugin.endpoint.ThriftPluginServer.handler;
import de.dfki.asr.ajan.pluginsystem.mosimplugin.exceptions.PortExistingException;
import de.dfki.asr.ajan.pluginsystem.mosimplugin.exceptions.SetupCallbackServerException;
//import static de.dfki.asr.ajan.pluginsystem.mosimplugin.extensions.AbortInstruction.LOG;
import de.mosim.mmi.cosim.MCoSimulationEventCallback;
import java.util.logging.Level;
import lombok.Getter;
import lombok.Setter;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ro.fortsoft.pf4j.Extension;

@Extension
@Component
@RDFBean("bt-mosim:CreateCallbackServer")
public class CreateCallbackServer extends AbstractTDBLeafTask implements NodeExtension {
	@RDFSubject
	@Getter @Setter
	private String url;

	@RDF("rdfs:label")
	@Getter @Setter
	private String label;

	@RDF("bt-mosim:callback")
	@Getter @Setter
	private int callback;

	private TServer coSimServer;
	private static MCoSimulationEventCallbackHandler coSimHandler;
	private static MCoSimulationEventCallback.Processor coSimProcessor;

	protected static final Logger LOG = LoggerFactory.getLogger(CreateCallbackServer.class);

	@Override
	public Resource getType() {
		return vf.createIRI("http://www.ajan.de/behavior/mosim-ns#CreateCallbackServer");
	}

	@Override
	public LeafStatus executeLeaf() {
		startCoSimCallbackServer();
		String report = toString() + " SUCCEEDED";
		LOG.info(report);
		return new LeafStatus(Status.SUCCEEDED, report);
	}

	public void startCoSimCallbackServer() {
		coSimHandler = new MCoSimulationEventCallbackHandler(handler.getActions());
		coSimProcessor = new MCoSimulationEventCallback.Processor(coSimHandler);
		Runnable simpleMCoSim = () -> {
			try {
				start(coSimProcessor);
			} catch (TException | PortExistingException | SetupCallbackServerException ex) {
				try {
					throw new SetupCallbackServerException("Problems in starting the CoSimCallback server.");
				} catch (SetupCallbackServerException ex1) {
					java.util.logging.Logger.getLogger(CreateCallbackServer.class.getName()).log(Level.SEVERE, null, ex1);
				}
			}
		};
		new Thread(simpleMCoSim).start();
	}

	private void start(MCoSimulationEventCallback.Processor coSimProcessor) throws TException, PortExistingException, SetupCallbackServerException {
		try {
			TServerTransport serverTransport = new TServerSocket(callback);
			coSimServer = new TSimpleServer(new TServer.Args(serverTransport)
					.processor(coSimProcessor)
					.protocolFactory(new TCompactProtocol.Factory()));
			if (!ThriftPluginServer.add(callback, coSimServer)) {
				throw new PortExistingException("Port: " + callback + " is already reserved!");
			}
			LOG.info("Starting the CoSimCallback server...");
			coSimServer.serve();
		} catch (TTransportException e) {
			throw new SetupCallbackServerException("Caught TTransportException in start().");
		}
    }

	@Override
	public void end() {
		LOG.info("Status (" + getStatus() + ")");
	}

	@Override
	public String toString() {
		return "CreateCallbackServer (" + getLabel() + ")";
	}
	
	@Override
	public EvaluationResult.Result simulateNodeLogic(final EvaluationResult result, final Resource root) {
		return EvaluationResult.Result.UNCLEAR;
	}

	@Override
	public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode) {
		return super.getModel(model, root, mode);
	}
}
