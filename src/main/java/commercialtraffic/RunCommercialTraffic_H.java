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

package commercialtraffic;/*
							* created by jbischoff, 03.05.2019
							*/

import commercialtraffic.integration.CommercialTrafficConfigGroup;
import commercialtraffic.integration.CommercialTrafficModule;
import commercialtraffic.replanning.ChangeDeliveryServiceOperator;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.config.groups.QSimConfigGroup.VehiclesSource;
import org.matsim.core.config.groups.StrategyConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.replanning.strategies.DefaultPlanStrategiesModule;

import ch.sbb.matsim.routing.pt.raptor.SwissRailRaptorModule;

import static org.matsim.core.config.ConfigUtils.createConfig;
import static org.matsim.core.scenario.ScenarioUtils.loadScenario;

public class RunCommercialTraffic_H {
	public static void main(String[] args) {
		String runId = "CT_251_noJsprit";
		String pct = ".1.0";

		String inputDir = "D:\\Thiel\\Programme\\WVModell\\01_MatSimInput\\vw251_1.0\\";

		Config config = ConfigUtils.loadConfig(inputDir + "config_1.0.xml", new CommercialTrafficConfigGroup());

		StrategyConfigGroup.StrategySettings changeExpBeta = new StrategyConfigGroup.StrategySettings();
		changeExpBeta.setStrategyName(DefaultPlanStrategiesModule.DefaultSelector.ChangeExpBeta);
		changeExpBeta.setWeight(0.5);
		config.strategy().addStrategySettings(changeExpBeta);
		config.controler().setWriteEventsInterval(1);
		config.controler().setOutputDirectory("D:\\Thiel\\Programme\\WVModell\\02_MatSimOutput\\" + runId + pct);
		config.controler()
				.setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);
		// config.qsim().setVehiclesSource(VehiclesSource.defaultVehicle);

		config.network().setInputFile(inputDir + "Network\\network_editedPt.xml.gz");
		config.plans().setInputFile(inputDir + "Population\\populationWithCTdemand.xml.gz");
		
		CommercialTrafficConfigGroup ctcg = (CommercialTrafficConfigGroup) config.getModules().get(CommercialTrafficConfigGroup.GROUP_NAME);
		ctcg.setCarriersFile(inputDir+"Carrier\\carrier_definition.xml");
		ctcg.setCarriersVehicleTypesFile(inputDir+"Carrier\\carrier_vehicletypes.xml");
		
		
		config.controler().setLastIteration(3);
		config.strategy().setFractionOfIterationsToDisableInnovation(0.00); // Fraction to disable Innovation
		Scenario scenario = loadScenario(config);
		adjustPtNetworkCapacity(scenario.getNetwork(), config.qsim().getFlowCapFactor());

		Controler controler = new Controler(scenario);
		config.controler().setRunId(runId + pct);
		controler.addOverridingModule(new AbstractModule() {
			@Override
			public void install() {
				addTravelTimeBinding(TransportMode.ride).to(networkTravelTime());
				addTravelDisutilityFactoryBinding(TransportMode.ride).to(carTravelDisutilityFactoryKey());
			}
		});

		controler.addOverridingModule(new SwissRailRaptorModule());

		controler.addOverridingModule(new CommercialTrafficModule(config, (carrierId -> 0)));

		controler.run();

	}

	private static void adjustPtNetworkCapacity(Network network, double flowCapacityFactor) {
		if (flowCapacityFactor < 1.0) {
			for (Link l : network.getLinks().values()) {
				if (l.getAllowedModes().contains(TransportMode.pt)) {
					l.setCapacity(l.getCapacity() / flowCapacityFactor);
				}
			}
		}
	}
}
