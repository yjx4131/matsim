/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2017 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

/**
 * 
 */
package robotaxi.preparation;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.utils.objectattributes.ObjectAttributesXmlWriter;

/**
 * @author  jbischoff
 *
 */
/**
 *
 */
public class CreateTaxiUserSubpopulation {

	public static void main(String[] args) {
		String inputPlansFile = "C:/Users/Joschka/Documents/shared-svn/projects/vw_rufbus/projekt2/input/population/initial_plans1.0.xml.gz";
		String outputPersonAttributes = "C:/Users/Joschka/Documents/shared-svn/projects/vw_rufbus/projekt2/input/population/drtCustomers.xml";
		String identifier = "WB_WB";
		String subpopulation = "drtCustomer";
		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new PopulationReader(scenario).readFile(inputPlansFile);
		for (Person p : scenario.getPopulation().getPersons().values()){
			if (p.getId().toString().startsWith(identifier)){
				scenario.getPopulation().getPersonAttributes().putAttribute(p.getId().toString(), "subpopulation", subpopulation);
			}
		}
		new ObjectAttributesXmlWriter(scenario.getPopulation().getPersonAttributes()).writeFile(outputPersonAttributes);
	}
	
}
