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

import org.matsim.contrib.drt.run.DrtConfigConsistencyChecker;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.drt.run.DrtControlerCreator;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.core.config.*;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.vis.otfvis.OTFVisConfigGroup;

import peoplemover.ClosestStopBasedDrtRoutingModule;

/**
 * @author michalm
 */
public class RunDrtScenario2 {
	public static void run(String configFile, boolean otfvis) {
		final Config config = ConfigUtils.loadConfig(configFile, new DrtConfigGroup(), new DvrpConfigGroup(),new OTFVisConfigGroup());
		Controler controler = createControler(config, otfvis);
		controler.addOverridingModule(new AbstractModule() {
		
			@Override
			public void install() {
				addRoutingModuleBinding(DvrpConfigGroup.get(config).getMode()).to(ClosestStopBasedDrtRoutingModule.class);
			}
		});
		controler.run();
		
	}

	public static Controler createControler(Config config, boolean otfvis) {
		config.addConfigConsistencyChecker(new DrtConfigConsistencyChecker());
		config.checkConsistency();
		
		config.controler().setLastIteration(2);
		config.controler().setWriteEventsInterval(1);
		config.controler().setWritePlansInterval(1);
		config.controler().setOutputDirectory("D:/Axer/MatsimDataStore/WOB_PM_ServiceQuality/drt_0.1_nextStation_default/output/");
		config.plans().setInputFile("D:/Axer/MatsimDataStore/WOB_PM_ServiceQuality/population/run120.100.WOB_taxi_0.1.xml.gz");
		return DrtControlerCreator.createControler(config, otfvis);
	}

	public static void main(String[] args) {
		RunDrtScenario2.run("D:/Axer/MatsimDataStore/WOB_PM_ServiceQuality/config.xml", false);
	}
}
