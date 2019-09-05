/*
 * *********************************************************************** *
 * project: org.matsim.*
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2018 by the members listed in the COPYING,        *
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

package vwExamples.utils.drtTrajectoryAnalyzer;

import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.dvrp.fleet.Fleet;
import org.matsim.contrib.dvrp.run.AbstractDvrpModeModule;
import org.matsim.contrib.ev.EvModule;
import org.matsim.contrib.ev.MobsimScopeEventHandling;
import org.matsim.contrib.ev.fleet.ElectricFleet;
import org.matsim.core.controler.IterationCounter;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.mobsim.qsim.AbstractQSimModule;

/**
 * @author saxer
 */
public class MyDrtTrajectoryAnalysisModule extends AbstractDvrpModeModule {
	private final DrtConfigGroup drtCfg;

	public MyDrtTrajectoryAnalysisModule(DrtConfigGroup drtCfg) {
		super(drtCfg.getMode());
		this.drtCfg = drtCfg;
	}

	@Override
	public void install() {
		installQSimModule(new AbstractQSimModule() {
			@Override
			protected void configureQSim() {
				bind(MyDynModeTrajectoryStats.class).toProvider(modalProvider(
						getter -> new MyDynModeTrajectoryStats(getter.get(Network.class), drtCfg,
								getter.get(ElectricFleet.class), getter.getModal(Fleet.class),
								getter.get(MobsimScopeEventHandling.class)))).asEagerSingleton();
				addQSimComponentBinding(EvModule.EV_COMPONENT).toProvider(modalProvider(
						getter -> new DrtTrajectoryStatsListener(getConfig(), drtCfg,
								getter.get(MyDynModeTrajectoryStats.class), getter.get(IterationCounter.class),
								getter.get(OutputDirectoryHierarchy.class))));
			}
		});
	}
}
