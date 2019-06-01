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

package electric.edrt.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.fleet.DvrpVehicleSpecification;
import org.matsim.contrib.dvrp.fleet.FleetWriter;
import org.matsim.contrib.dvrp.fleet.ImmutableDvrpVehicleSpecification;
import org.matsim.contrib.ev.EvUnits;
import org.matsim.contrib.ev.fleet.ElectricFleetWriter;
import org.matsim.contrib.ev.fleet.ElectricVehicle;
import org.matsim.contrib.ev.fleet.ElectricVehicleSpecification;
import org.matsim.contrib.ev.fleet.ImmutableElectricVehicleSpecification;
import org.matsim.contrib.ev.infrastructure.Charger;
import org.matsim.contrib.ev.infrastructure.ChargerImpl;
import org.matsim.contrib.ev.infrastructure.ChargerSpecification;
import org.matsim.contrib.ev.infrastructure.ChargerWriter;
import org.matsim.contrib.ev.infrastructure.ImmutableChargerSpecification;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;

/**
 * @author jbischoff
 * This is an example script to create electric drt vehicle files. The vehicles are distributed along defined depots.
 */
public class CreateEDRTVehiclesAndChargers {

	static final int BATTERY_CAPACITY_KWH = 30;
	static final int MIN_START_CAPACITY_KWH = 10;
	static final int MAX_START_CAPACITY_KWH = 30;

	static final int CHARGINGPOWER_KW = 50;

	static final int SEATS = 8;

	static final String NETWORKFILE = "C:/Users/Joschka/Documents/shared-svn/projects/vw_rufbus/projekt2/drt_test_Scenarios/BS_DRT/input/network/modifiedNetwork.xml.gz";
	static final String E_VEHICLE_FILE = "C:/Users/Joschka/Documents/shared-svn/projects/vw_rufbus/projekt2/drt_test_Scenarios/BS_DRT/input/edrt/e-vehicles_bs_100.xml";
	static final String DRT_VEHICLE_FILE = "C:/Users/Joschka/Documents/shared-svn/projects/vw_rufbus/projekt2/drt_test_Scenarios/BS_DRT/input/edrt/e-drt_bs_100.xml";
	static final String CHARGER_FILE = "C:/Users/Joschka/Documents/shared-svn/projects/vw_rufbus/projekt2/drt_test_Scenarios/BS_DRT/input/edrt/chargers_bs_100.xml";

	static final double OPERATIONSTARTTIME = 0.; //t0
	static final double OPERATIONENDTIME = 30 * 3600.;    //t1

	static final double FRACTION_OF_CHARGERS_PER_DEPOT = 0.4; //relative number of chargers to numbers of vehicle at location

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Map<Id<Link>, Integer> depotsAndVehicles = new HashMap<>();
		depotsAndVehicles.put(Id.createLinkId(40158), 25); //BS HBF
		depotsAndVehicles.put(Id.createLinkId(8097), 25); //Zentrum SO
		depotsAndVehicles.put(Id.createLinkId(13417), 25); //Zentrum N
		depotsAndVehicles.put(Id.createLinkId(14915), 25); //Flugplatz
		new CreateEDRTVehiclesAndChargers().run(depotsAndVehicles);

	}

	private void run(Map<Id<Link>, Integer> depotsAndVehicles) {
		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());

		List<DvrpVehicleSpecification> vehicles = new ArrayList<>();
		List<ElectricVehicleSpecification> eVehicles = new ArrayList<>();
		List<ChargerSpecification> chargers = new ArrayList<>();
		Random random = MatsimRandom.getLocalInstance();
		new MatsimNetworkReader(scenario.getNetwork()).readFile(NETWORKFILE);
		for (Entry<Id<Link>, Integer> e : depotsAndVehicles.entrySet()) {
			Link startLink;
			startLink = scenario.getNetwork().getLinks().get(e.getKey());
			if (!startLink.getAllowedModes().contains(TransportMode.car)) {
				throw new RuntimeException("StartLink " + startLink.getId().toString() + " does not allow car mode.");
			}
			for (int i = 0; i < e.getValue(); i++) {

				DvrpVehicleSpecification v = ImmutableDvrpVehicleSpecification.newBuilder()
						.id(Id.create("taxi_" + startLink.getId().toString() + "_" + i, DvrpVehicle.class))
						.startLinkId(startLink.getId())
						.capacity(SEATS)
						.serviceBeginTime(OPERATIONSTARTTIME)
						.serviceEndTime(OPERATIONENDTIME)
						.build();
				vehicles.add(v);
				double initialSoc_kWh = MIN_START_CAPACITY_KWH + random.nextDouble() * (MAX_START_CAPACITY_KWH
						- MIN_START_CAPACITY_KWH);
				ElectricVehicleSpecification ev = ImmutableElectricVehicleSpecification.newBuilder()
						.id(Id.create(v.getId(), ElectricVehicle.class))
						.batteryCapacity(EvUnits.kWh_to_J(BATTERY_CAPACITY_KWH))
						.initialSoc(EvUnits.kWh_to_J(initialSoc_kWh))
						.build();
				eVehicles.add(ev);

			}
			int chargersPerDepot = (int)(e.getValue() * FRACTION_OF_CHARGERS_PER_DEPOT);
			ChargerSpecification charger = ImmutableChargerSpecification.newBuilder()
					.id(Id.create("charger_" + startLink.getId(), Charger.class))
					.maxPower(CHARGINGPOWER_KW * EvUnits.W_PER_kW)
					.plugCount(chargersPerDepot)
					.linkId(startLink.getId())
					.chargerType(ChargerImpl.DEFAULT_CHARGER_TYPE)
					.build();
			chargers.add(charger);

		}
		new FleetWriter(vehicles.stream()).write(DRT_VEHICLE_FILE);
		new ElectricFleetWriter(eVehicles.stream()).write(E_VEHICLE_FILE);
		new ChargerWriter(chargers.stream()).write(CHARGER_FILE);
	}

}
