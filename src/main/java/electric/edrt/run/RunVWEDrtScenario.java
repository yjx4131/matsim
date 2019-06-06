/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2016 by the members listed in the COPYING,        *
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

package electric.edrt.run;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.av.maxspeed.DvrpTravelTimeWithMaxSpeedLimitModule;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.contrib.edrt.optimizer.EDrtVehicleDataEntryFactory.EDrtVehicleDataEntryFactoryProvider;
import org.matsim.contrib.edrt.run.EDrtControlerCreator;
import org.matsim.contrib.ev.EvConfigGroup;
import org.matsim.contrib.ev.charging.ChargingLogic;
import org.matsim.contrib.ev.charging.ChargingWithQueueingAndAssignmentLogic;
import org.matsim.contrib.ev.charging.FastThenSlowCharging;
import org.matsim.contrib.ev.discharging.AuxDischargingHandler;
import org.matsim.contrib.ev.temperature.TemperatureChangeConfigGroup;
import org.matsim.contrib.ev.temperature.TemperatureChangeModule;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.mobsim.qsim.AbstractQSimModule;
import org.matsim.vehicles.VehicleType;
import org.matsim.vis.otfvis.OTFVisConfigGroup;

import vwExamples.utils.customEdrtModule.OperatingVehicleOutsideDepotProvider;

public class RunVWEDrtScenario {
	private static final double CHARGING_SPEED_FACTOR = 1.; // full speed
	private static final double MAX_RELATIVE_SOC = 0.8;// charge up to 80% SOC
	private static final double MIN_RELATIVE_SOC = 0.2;// send to chargers vehicles below 20% SOC
	private static final double TEMPERATURE = 20;// oC
	private static String inputPath = "D:\\BS_DRT\\input\\";

	public static void main(String[] args) {
		Config config = ConfigUtils.loadConfig(inputPath + "edrt-config.xml", new DrtConfigGroup(),
				new DvrpConfigGroup(), new OTFVisConfigGroup(), new EvConfigGroup(),
				new TemperatureChangeConfigGroup());
		config.controler().setOverwriteFileSetting(OverwriteFileSetting.overwriteExistingFiles);
		config.plans().setInputFile("population/vw219_it_1_sampleRate0.1replaceRate_bs_drt.xml.gz");
		config.controler().setLastIteration(0); // Number of simulation iterations
		config.controler().setWriteEventsInterval(1); // Write Events file every x-Iterations
		config.controler().setWritePlansInterval(1); // Write Plan file every x-Iterations
		config.network().setInputFile("network/modifiedNetwork.xml.gz");

		TemperatureChangeConfigGroup tcg = (TemperatureChangeConfigGroup)config.getModules()
				.get(TemperatureChangeConfigGroup.GROUP_NAME);
		tcg.setTempFile("temperatures.csv");

		DrtConfigGroup drt = (DrtConfigGroup)config.getModules().get(DrtConfigGroup.GROUP_NAME);

		// Use custom stop duration
		drt.setOperationalScheme("stopbased");
		drt.setMaxTravelTimeBeta(500);
		drt.setMaxTravelTimeAlpha(1.3);
		drt.setMaxWaitTime(500);
		drt.setStopDuration(15);
		drt.setTransitStopFile("virtualstops/stopsGrid_300m.xml");
		drt.setMaxWalkDistance(800.0);
		drt.setPrintDetailedWarnings(false);
		drt.setVehiclesFile("edrt/e-drt_bs_100.xml");
		drt.setIdleVehiclesReturnToDepots(true);

		String runId = "edrt-test";
		config.controler().setRunId(runId);
		config.qsim().setFlowCapFactor(0.12);
		config.qsim().setStorageCapFactor(0.24);

		config.qsim().setLinkDynamics(QSimConfigGroup.LinkDynamics.PassingQ);

		config.controler().setOutputDirectory(inputPath + "../output/" + runId);
		Controler controler = createControler(config);

		VehicleType slowAV = controler.getScenario()
				.getVehicles()
				.getFactory()
				.createVehicleType(Id.create("av", VehicleType.class));
		slowAV.setMaximumVelocity(20 / 3.6);

		controler.addOverridingModule(new DvrpTravelTimeWithMaxSpeedLimitModule(slowAV));
		controler.run();
	}

	public static Controler createControler(Config config) {
		DrtConfigGroup drtCfg = DrtConfigGroup.get(config);
		Controler controler = EDrtControlerCreator.createControler(config, false);
		controler.addOverridingModule(new TemperatureChangeModule());

		controler.addOverridingModule(new AbstractModule() {
			@Override
			public void install() {
				bind(ChargingLogic.Factory.class).toProvider(new ChargingWithQueueingAndAssignmentLogic.FactoryProvider(
						charger -> new FastThenSlowCharging(charger.getPower() * CHARGING_SPEED_FACTOR)));

				bind(EDrtVehicleDataEntryFactoryProvider.class).toInstance(
						new EDrtVehicleDataEntryFactoryProvider(MIN_RELATIVE_SOC));
			}
		});

		controler.addOverridingQSimModule(new AbstractQSimModule() {
			@Override
			protected void configureQSim() {
				this.bind(AuxDischargingHandler.VehicleProvider.class).to(OperatingVehicleOutsideDepotProvider.class);
			}
		});

		return controler;
	}
}
