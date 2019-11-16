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

package uam.run;

import static org.matsim.contrib.drt.run.DrtModeModule.Direction;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.drt.run.DrtConfigs;
import org.matsim.contrib.drt.run.DrtControlerCreator;
import org.matsim.contrib.drt.run.MultiModeDrtConfigGroup;
import org.matsim.contrib.drt.run.MultiModeDrtModule;
import org.matsim.contrib.dvrp.router.DvrpRoutingNetworkProvider;
import org.matsim.contrib.dvrp.run.AbstractDvrpModeModule;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.contrib.dvrp.run.DvrpModule;
import org.matsim.contrib.dvrp.run.DvrpQSimComponents;
import org.matsim.contrib.dvrp.run.ModalProviders;
import org.matsim.contrib.otfvis.OTFVisLiveModule;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.network.algorithms.TransportModeNetworkFilter;
import org.matsim.core.router.RoutingModule;
import org.matsim.core.router.TripRouter;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.vis.otfvis.OTFVisConfigGroup;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.multibindings.MapBinder;

/**
 * @author Michal Maciejewski (michalm)
 */
public class RunUamDrtScenario {
	public static void run(Config config, boolean otfvis, int lastIteration) {
		config.controler().setLastIteration(lastIteration);

		MultiModeDrtConfigGroup multiModeDrtCfg = MultiModeDrtConfigGroup.get(config);
		Map<String, DrtConfigGroup> drtCfgMap = multiModeDrtCfg.getModalElements()
				.stream()
				.collect(Collectors.toMap(DrtConfigGroup::getMode, cfg -> cfg));
		DrtConfigGroup drtCfg = drtCfgMap.get("drt");
		DrtConfigGroup uamCfg = drtCfgMap.get("uam");
		drtCfgMap.values()
				.forEach(cfg -> DrtConfigs.adjustDrtConfig(cfg, config.planCalcScore(), config.plansCalcRoute()));

		config.plansCalcRoute().setInsertingAccessEgressWalk(true);

		Scenario scenario = DrtControlerCreator.createScenarioWithDrtRouteFactory(config);
		ScenarioUtils.loadScenario(scenario);

		Controler controler = new Controler(scenario);
		controler.addOverridingModule(new DvrpModule());
		controler.addOverridingModule(new MultiModeDrtModule());
		controler.configureQSimComponents(DvrpQSimComponents.activateModes(drtCfg.getMode(), uamCfg.getMode()));

		controler.addOverridingModule(new AbstractDvrpModeModule(drtCfg.getMode()) {
			@Override
			public void install() {
				DvrpRoutingNetworkProvider.checkUseModeFilteredSubnetworkAllowed(getConfig(), "car");
				bindModal(Network.class).toProvider(ModalProviders.createProvider(getMode(), getter -> {
					Network subnetwork = NetworkUtils.createNetwork();
					new TransportModeNetworkFilter(
							getter.getNamed(Network.class, DvrpRoutingNetworkProvider.DVRP_ROUTING)).
							filter(subnetwork, Collections.singleton("car"));
					new NetworkCleaner().run(subnetwork);
					return subnetwork;
				})).asEagerSingleton();
			}
		});

		controler.addOverridingModule(new AbstractDvrpModeModule(uamCfg.getMode()) {
			@Override
			public void install() {
				MapBinder<Direction, RoutingModule> mapBinder = modalMapBinder(Direction.class, RoutingModule.class);
				mapBinder.addBinding(Direction.ACCESS).toProvider(new UamAccessEgressRoutingModuleProvider("drt"));
				mapBinder.addBinding(Direction.EGRESS).toProvider(new UamAccessEgressRoutingModuleProvider("car"));
			}
		});

		if (otfvis) {
			controler.addOverridingModule(new OTFVisLiveModule()); // OTFVis visualisation
		}

		// run simulation
		controler.run();
	}

	private static class UamAccessEgressRoutingModuleProvider implements Provider<RoutingModule> {
		@Inject
		private Injector injector;

		private final String mode;

		private UamAccessEgressRoutingModuleProvider(String mode) {
			this.mode = mode;
		}

		@Override
		public RoutingModule get() {
			return (fromFacility, toFacility, departureTime, person) -> injector.getInstance(TripRouter.class)
					.calcRoute(mode, fromFacility, toFacility, departureTime, person);
		}
	}

	public static void main(String[] args) {
		Config config = ConfigUtils.loadConfig("input/uam/uam_drt_config_epsilon_2000.xml",
				new MultiModeDrtConfigGroup(), new DvrpConfigGroup(), new OTFVisConfigGroup());
		run(config, false, 0);
	}
}
