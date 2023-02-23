/*
 * Copyright (C) 2020 see AJAN-service/AUTHORS.txt (German Research Center for Artificial Intelligence, DFKI).
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

package de.dfki.asr.ajan.rest;

import de.dfki.asr.ajan.logic.agent.AgentManager;
import de.dfki.asr.ajan.model.Agent;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.UUID;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.UriBuilder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import de.dfki.asr.ajan.behaviour.nodes.action.UriGenerator;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;


@Path("/agents")
@Api("List Agents")
@Component
public class AgentsService {
	public static final String NAME = "agentName";
	public static final String AGENT_PATH = "/{" + NAME + "}";
        private static final String TEXT = "text/plain";
	private static final String TURTLE = "text/turtle";
	public static final String WELCOME_SERV_PATH = "/welcome/services";
	public static final String WELCOME_SERV_ID = "serviceID";
	public static final String SERV_PATH = WELCOME_SERV_PATH + "/{" + WELCOME_SERV_ID + "}";
        public static final String WELCOME_REQ_PATH = "/welcome/request";	
        public static final String WELCOME_REQ_ID = "requestID";	
        public static final String REQUEST_PATH = WELCOME_REQ_PATH + "/{" + WELCOME_REQ_ID + "}";
        
	@Autowired
	private AgentManager agentManager;

	@GET
	@Path("/")
	@Produces({"text/plain","text/turtle","application/ld+json"})
	@ApiOperation("List all Agents")
	public Collection<Agent> getAllAgents() {
            return agentManager.getAllAgents();
	}

	@POST
	@Path("/")
	@Produces("text/turtle")
	@ApiOperation("Add new Agent")
	public Agent createAgent(final Model model) throws URISyntaxException {
            return agentManager.createAgent(model);
	}

	@Path(AGENT_PATH)
	@ApiParam(name = NAME, value = "The name of the Agent.")
	public AgentResource getAgent(@PathParam(NAME) final String agentName) {
            Agent agent = agentManager.getAgent(agentName);
            return new AgentResource(agent, agentManager);
	}

        @Path(WELCOME_SERV_PATH)
        @GET
        @Produces({TEXT, TURTLE})
        @ApiOperation(value = "Get Welcome Services.")
        public Model getListWelcomeServices() {
            String welcomeLocationOffers = System.getenv("servicesOfferFolderPath");
            //String welcomeLocationOffers = System.getProperty("servicesOfferFolderPath");
            File PartialLocationOffersFile = new File(welcomeLocationOffers);
            File locationOffersFile = new File(PartialLocationOffersFile.toPath().normalize().toAbsolutePath().toString());
            Model responseModel = new LinkedHashModel(); 
            ValueFactory FACT = SimpleValueFactory.getInstance();
            Literal fileName;
            IRI SSCName = FACT.createIRI("https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#LSR");
            IRI SSCPred = FACT.createIRI("https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#hasFile");
            //String serviceLocation = "agents/welcomeServices/" ;
            //ClassLoader classLoader = UriGenerator.class.getClassLoader();
            //File welcomeService = new File(classLoader.getResource(serviceLocation).getPath());
                               
            if (Files.exists(locationOffersFile.toPath())){
               String[] fileList = locationOffersFile.list();  
                for (String fileList1 : fileList) {
                    fileName = FACT.createLiteral(fileList1);
                    Statement namedStatement = FACT.createStatement(SSCName, SSCPred, fileName);
                    responseModel.add(namedStatement);
                }
            }
            return responseModel;  
        }
        
        @Path(SERV_PATH)
        @GET
        @Produces({TEXT, TURTLE})
        @ApiOperation(value = "Get Single Welcome Service.")
        public String getWelcomeService(@PathParam(WELCOME_SERV_ID) final String serviceID) throws URISyntaxException, FileNotFoundException, IOException {
            String offerList = System.getenv("servicesOfferFolderPath");
            //String offerList = System.getProperty("servicesOfferFolderPath");
            String welcomeLocationOffers = offerList.concat("/").concat(serviceID);
            File PartialLocationOffersFile = new File(welcomeLocationOffers);
            File locationOffersFile = new File(PartialLocationOffersFile.toPath().normalize().toAbsolutePath().toString());
            byte[] encoded = null;
                        
            if (Files.exists(locationOffersFile.toPath()))
                encoded = Files.readAllBytes(Paths.get(locationOffersFile.toPath().toString()));                
            return new String(encoded, "UTF-8");
        }
        
        @Path(REQUEST_PATH)
        @GET
        @Produces({TEXT, TURTLE})
        @ApiOperation(value = "Get Service Request.")
        public String getRequestService(@PathParam(WELCOME_REQ_ID) final String requestID) throws URISyntaxException, FileNotFoundException, IOException {
            String request = System.getenv("servicesRequestFolderPath"); 
            //String request = System.getProperty("servicesRequestFolderPath"); 
            String welcomeRequest = request.concat("/").concat(requestID);
            File PartialLocationRequest = new File(welcomeRequest);
            File locationRequestFile = new File(PartialLocationRequest.toPath().normalize().toAbsolutePath().toString());
            byte[] encoded = null;
            if (Files.exists(locationRequestFile.toPath()))
                encoded = Files.readAllBytes(Paths.get(locationRequestFile.toPath().toString()));               
            return new String(encoded, "UTF-8");              
        }
        
	@RequiredArgsConstructor
	public static class UriGeneratorImpl implements UriGenerator {
		@NonNull
		private final UriBuilder builder;

		@Override
		public String getAgentUri() {
			return builder.clone().build().toString();
		}

		@Override
		public String getActionUri(final UUID actionID) {
			return builder.clone()
					.path(AgentResource.ACTION_PATH)
					.resolveTemplate(AgentResource.ACTION_PARAMETER, actionID.toString())
					.build().toString();
		}

		@Override
		public String getConnectionUri(final UUID connectionID) {
			return builder.clone()
					.path(AgentResource.CONNECTION_PATH)
					.resolveTemplate(AgentResource.CONNECTION_PARAMETER, connectionID.toString())
					.build().toString();
		}

		@Override
		public String getCompletionUri(final UUID connectionID, final UUID actionID) {
			return builder.clone()
					.path(AgentResource.COMPLETION_PATH)
					.resolveTemplate(AgentResource.CONNECTION_PARAMETER, connectionID.toString())
					.resolveTemplate(AgentResource.ACTION_PARAMETER, actionID.toString())
					.build().toString();
		}
	}
}
