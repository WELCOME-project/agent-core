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

package de.dfki.asr.ajan.behaviour.nodes.event;

import de.dfki.asr.ajan.behaviour.events.ModelEventInformation;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorConstructQuery;
import de.dfki.asr.ajan.common.AJANVocabulary;
import java.net.URI;
import java.util.Queue;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;

@RDFBean("bt:HandleQueueEvent")
public class HandleModelQueueEvent extends HandleModelEvent {

    // ELENA's Version
	/*@RDFSubject
	@Getter @Setter
	private String url;

	@RDF("bt:validate")
	@Getter @Setter
	private BehaviorConstructQuery query;

	@Override
	protected boolean checkEventGoalMatching() {
		Object info = this.getObject().getEventInformation();
		if (info instanceof Queue) {
			Queue modelQueue = (Queue) this.getObject().getEventInformation();
			if (modelQueue.peek() != null && modelQueue.peek() instanceof ModelEventInformation) {
				return checkQueueItem(((ModelEventInformation)modelQueue.poll()).getEvent());
			}
		}
		return false;
	}

	private boolean checkQueueItem(final String eventUrl) {
		boolean bothNull = getEvent() == null && getGoal() == null;
		boolean eventMatching = getEvent() != null && getEvent().toString().equals(eventUrl);
		boolean goalMatching = getGoal() != null && getGoal().toString().equals(eventUrl);
		return bothNull || eventMatching || goalMatching;
	}

	@Override
	protected Model getEventModel() {
		constructQuery = query;
		Object info = this.getObject().getEventInformation();
		Model model = new LinkedHashModel();
//		if (this.getObject().getEventInformation() instanceof Queue) {
		if (info instanceof Queue) {
			Queue modelQueue = (Queue) this.getObject().getEventInformation();
//			if (modelQueue.peek() != null && modelQueue.peek() instanceof Model) {
//				model = constructQuery.getResult((Model)modelQueue.poll());
			if (modelQueue.peek() != null && modelQueue.peek() instanceof ModelEventInformation) {
				model = constructQuery.getResult(((ModelEventInformation)modelQueue.poll()).getModel());
			}
		}
		return model;
	}

	@Override
	public String toString() {
		return "HandleQueueEvent (" + getLabel() + ")";
	}*/
    
    // AKBAR's version
    @RDFSubject
	@Getter @Setter
	private String url;

        @RDF("rdfs:label")
	@Getter @Setter
	private String label;

	@RDF("ajan:event")
	@Getter @Setter
	private URI event;
        
	@RDF("bt:validate")
	@Getter @Setter
	private BehaviorConstructQuery query;

         
        @Override
	protected boolean checkEventGoalMatching() {
		Object info = this.getObject().getEventInformation();
               // System.out.println("HandleModelQueueEvent: checkEventGoalMatching - info - "+info);

		if (info instanceof Queue) {
			Queue modelQueue = (Queue) this.getObject().getEventInformation();
                       // System.out.println("HandleModelQueueEvent: checkEventGoalMatching - modelQueue - "+modelQueue);
                        
                        for(Object element: modelQueue){
                            if(element != null && element instanceof ModelEventInformation){
                                return checkQueueItem(((ModelEventInformation)element).getEvent());
                            }
                        }
                        
			/*if (modelQueue.peek() != null && modelQueue.peek() instanceof ModelEventInformation) {
				return checkQueueItem(((ModelEventInformation)modelQueue.peek()).getEvent());
			}*/
		}
		return false;
	}

	private boolean checkQueueItem(final String eventUrl) {
		boolean eventMatching = getEvent() != null && getEvent().toString().equals(eventUrl);
		boolean allEvents = getEvent() != null && getEvent().toString().equals(AJANVocabulary.ALL.toString());
                boolean result = getEvent() == null || eventMatching || allEvents;
                //System.out.println("HandleModelQueueEvent: checkQueueItem: result "+result);

                return result;
//		return getEvent() == null || eventMatching || allEvents;
	}

	@Override
	protected Model getEventModel() {
            //System.out.println("HandleModelQueueEvent: getEventModel ");
            constructQuery = query;
           // System.out.println("HandleModelQueueEvent: constructQuery "+constructQuery);

		Object info = this.getObject().getEventInformation();
		Model model = new LinkedHashModel();
		if (info instanceof Queue) {
                   // System.out.println("HandleModelQueueEvent: info "+info);

			Queue modelQueue = (Queue) this.getObject().getEventInformation();
                      //  System.out.println("HandleModelQueueEvent: modelQueue.peek() "+modelQueue.peek());

                        for(Object element : modelQueue){
                          //  System.out.println("getEventModel-element:" + element);
                            if(element != null && element instanceof ModelEventInformation){
                                model = ((ModelEventInformation)element).getModel();
                               // System.out.println("HandleModelQueueEvent: model"+model);
                                modelQueue.remove(element);
				if (constructQuery == null || constructQuery.getSparql().isEmpty()) {
					return model;
				} else {
					return constructQuery.getResult(model);
				}
                            }
                        }
                        
                        
			/*if (modelQueue.peek() != null && modelQueue.peek() instanceof ModelEventInformation) {
				model = ((ModelEventInformation)modelQueue.poll()).getModel();
                                System.out.println("HandleModelQueueEvent: model"+model);

				if (constructQuery == null || constructQuery.getSparql().isEmpty()) {
					return model;
				} else {
					return constructQuery.getResult(model);
				}
			}*/
		}
		return model;
	}
	@Override
	public String toString() {
		return "HandleQueueEvent (" + getLabel() + ")";
	}
}
