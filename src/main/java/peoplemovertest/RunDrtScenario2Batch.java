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

package peoplemovertest;

import java.util.Arrays;
import java.util.List;

//import org.matsim.contrib.av.robotaxi.run.RunRobotaxiExample;
import org.matsim.contrib.drt.run.DrtConfigConsistencyChecker;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.drt.run.DrtControlerCreator;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.contrib.taxi.run.TaxiConfigGroup;
import org.matsim.core.config.*;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.vis.otfvis.OTFVisConfigGroup;

import peoplemover.ClosestStopBasedDrtRoutingModule;

/**
 * @author axer
 */
public class RunDrtScenario2Batch {
	//Class to create the controller
	public static Controler createControler(Config config, boolean otfvis) {
		config.addConfigConsistencyChecker(new DrtConfigConsistencyChecker());
		config.checkConsistency();
		return DrtControlerCreator.createControler(config, otfvis);
	}

	public static void main(String[] args) {
		//Define Iteration list
		List<String> strings = Arrays.asList("0.5");
//		List<String> strings = Arrays.asList("0.1", "0.3","0.5");


		for (String Element : strings){
			//Define the path to the config file and enable / disable otfvis
			//Basis configuration
			final Config config = ConfigUtils.loadConfig("D:/Axer/MatsimDataStore/WOB_PM_ServiceQuality/config.xml",new DrtConfigGroup(), new DvrpConfigGroup(), new OTFVisConfigGroup());
			boolean otfvis = false;
			
	
			//Overwrite existing configuration parameters
			config.controler().setLastIteration(4);
			config.controler().setWriteEventsInterval(1);
			config.controler().setWritePlansInterval(1);
			config.controler().setOutputDirectory("D:/Axer/MatsimDataStore/WOB_PM_ServiceQuality/drt_"+Element.toString()+"_nextStation_default/output/");
			config.plans().setInputFile("D:/Axer/MatsimDataStore/WOB_PM_ServiceQuality/population/run124.100.output_plans_DRT"+Element.toString()+".xml.gz");
			DrtConfigGroup drt = (DrtConfigGroup) config.getModules().get(DrtConfigGroup.GROUP_NAME);
			drt.setkNearestVehicles(90);
			//Initialize the controller
			Controler controler = createControler(config, otfvis);
			
			controler.addOverridingModule(new AbstractModule() {
				@Override
				public void install() {
					addRoutingModuleBinding(DvrpConfigGroup.get(config).getMode())
							.to(ClosestStopBasedDrtRoutingModule.class);
					DvrpConfigGroup.get(config).setTravelTimeEstimationAlpha(0.3);
					
				}
			});
	
			controler.run();

	}
	}
}
