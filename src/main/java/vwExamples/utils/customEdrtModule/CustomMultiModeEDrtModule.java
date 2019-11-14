/*
 * *********************************************************************** *
 * project: org.matsim.*
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2019 by the members listed in the COPYING,        *
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
 * *********************************************************************** *
 */

package vwExamples.utils.customEdrtModule;

import org.matsim.contrib.drt.analysis.DrtModeAnalysisModule;
import org.matsim.contrib.drt.routing.MultiModeDrtMainModeIdentifier;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.drt.run.DrtModeModule;
import org.matsim.contrib.drt.run.MultiModeDrtConfigGroup;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.router.MainModeIdentifier;

import com.google.inject.Inject;

/**
 * @author axer
 */
public class CustomMultiModeEDrtModule extends AbstractModule {

	@Inject
	private MultiModeDrtConfigGroup multiModeDrtCfg;

	@Inject
	private PlansCalcRouteConfigGroup plansCalcRouteCfg;

	@Override
	public void install() {
		for (DrtConfigGroup drtCfg : multiModeDrtCfg.getModalElements()) {
			install(new DrtModeModule(drtCfg, plansCalcRouteCfg));
			installQSimModule(new CustomEDrtModeQSimModule(drtCfg));
			install(new DrtModeAnalysisModule(drtCfg));
		}

		bind(MainModeIdentifier.class).toInstance(new MultiModeDrtMainModeIdentifier(multiModeDrtCfg));
	}
}