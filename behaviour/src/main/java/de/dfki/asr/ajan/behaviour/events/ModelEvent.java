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

//import de.dfki.asr.ajan.common.AgentUtil;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Model;

@RDFBean("ajan:ModelEvent")
public class ModelEvent extends DefaultEvent {

	@RDFSubject
	@Getter @Setter
	private String url;

	@RDF("rdfs:label")
	@Getter @Setter
	private String name;

	@Override
	public void setEventInformation(final Object information) {
		//this.information = AgentUtil.setNamedGraph(((Model)information).iterator(), url);
		this.information = getEventInfo(information);
		notifyListeners();
	}

	@Override
	public void setEventInformation(final String id, final Object information) {
		//this.information = AgentUtil.setNamedGraph(((Model)information).iterator(), url);
		this.information = getEventInfo(information);
		notifyListeners(id);
	}

	private ModelEventInformation getEventInfo(final Object information) {
		ModelEventInformation info = new ModelEventInformation();
		info.setEvent(url);
		info.setModel(((Model)information));
		return info;
	}
}
