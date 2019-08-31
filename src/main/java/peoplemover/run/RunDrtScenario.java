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

package peoplemover.run;

import org.matsim.contrib.drt.run.DrtControlerCreator;
import org.matsim.contrib.drt.run.MultiModeDrtConfigGroup;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.vis.otfvis.OTFVisConfigGroup;

/**
 * @author michalm
 */
public class RunDrtScenario {
	public static void run(String configFile, boolean otfvis) {
		final Config config = ConfigUtils.loadConfig(configFile, new MultiModeDrtConfigGroup(), new DvrpConfigGroup(),
				new OTFVisConfigGroup());
		Controler controler = createControler(config, otfvis);
		controler.run();
	}

	public static Controler createControler(Config config, boolean otfvis) {
		return DrtControlerCreator.createControlerWithSingleModeDrt(config, otfvis);
	}

	public static void main(String[] args) {
		if (args.length != 1) {
			throw new IllegalArgumentException("RunDrtScenario needs one argument: path to the configuration file");
		}
		RunDrtScenario.run(args[0], false);
	}
}
