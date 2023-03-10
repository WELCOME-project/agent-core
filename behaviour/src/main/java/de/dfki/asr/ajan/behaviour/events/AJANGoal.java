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

package de.dfki.asr.ajan.behaviour.events;

import de.dfki.asr.ajan.behaviour.nodes.common.Variable;
//import de.dfki.asr.ajan.common.AgentUtil;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Model;

@RDFBean("ajan:Goal")
public class AJANGoal extends ModelCallback {
// Elena's version
/*	@RDFSubject
	@Getter @Setter
	private String url;

	@RDF("rdfs:label")
	@Getter @Setter
	private String name;

	@RDF("ajan:variables")
	@Getter @Setter
	private List<Variable> variables;

	@RDF("ajan:condition")
	@Getter @Setter
	private String condition;

	@Override
	public void setEventInformation(final Producer producer, final Object information) {
		if (information instanceof GoalInformation) {
			GoalInformation gInfo = (GoalInformation)information;
//			this.information = AgentUtil.setNamedGraph(gInfo.getModel().iterator(), url);
			ModelEventInformation info = new ModelEventInformation();
			info.setEvent(url);
			info.setModel(gInfo.getModel());
			this.information = info;
			notifyListeners(producer);
		}
	}*/
    
    // Akbar's version
    @RDFSubject
	@Getter @Setter
	private String url;

	@RDF("rdfs:label")
	@Getter @Setter
	private String name;
        
	@RDF("ajan:precondition")
	@Getter @Setter
	private String precondition;
        
	@RDF("ajan:postcondition")
	@Getter @Setter
	private String postcondition;


	@Override
	public void setEventInformation(final Producer producer, final Object information) {
                        this.information = getEventInfo(information);
			notifyListeners(producer);
	}
        
        private ModelEventInformation getEventInfo(final Object information) {
		ModelEventInformation info = new ModelEventInformation();
		info.setEvent(url);
		info.setModel(((Model)information));
		return info;
	}
}
