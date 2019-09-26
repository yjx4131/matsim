/* *********************************************************************** *
 * project: org.matsim.*
 * Controler.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
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

package commercialtraffic;

import commercialtraffic.commercialJob.CommercialJobUtils;
import commercialtraffic.commercialJob.CommercialJobUtilsV2;
import commercialtraffic.integration.CommercialTrafficConfigGroup;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.contrib.freight.carrier.*;
import org.matsim.core.config.Config;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.population.PopulationUtils;
import org.matsim.vehicles.Vehicle;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleUtils;

import static org.matsim.core.config.ConfigUtils.createConfig;
import static org.matsim.core.scenario.ScenarioUtils.createScenario;

public class TestScenarioGeneration {


    public static Scenario generateScenario(){
        Config config = createConfig(new CommercialTrafficConfigGroup());
        Scenario scenario = createScenario(config);
        new MatsimNetworkReader(scenario.getNetwork()).readFile("input/commercialtrafficIT/grid_network.xml");
        addPopulation(scenario);
        return scenario;
    }


    public static Carriers generateCarriers() {
        Carriers carriers = new Carriers();
        Carrier pizza_1 = CarrierImpl.newInstance(Id.create("pizza_1", Carrier.class));
        Carrier pizza_2 = CarrierImpl.newInstance(Id.create("pizza_2", Carrier.class));
        Carrier shopping_1 = CarrierImpl.newInstance(Id.create("shopping_1", Carrier.class));

        VehicleType type = createLightType();

        pizza_1.getCarrierCapabilities().setFleetSize(CarrierCapabilities.FleetSize.INFINITE);
        pizza_1.getCarrierCapabilities().getVehicleTypes().add(createLightType());
        pizza_1.getCarrierCapabilities().getCarrierVehicles().add(getLightVehicle(pizza_1.getId(), type, Id.createLinkId(111), "one"));
//        pizza_1.getServices().add(CarrierService
//                .Builder.newInstance(Id.create("salamipizza", CarrierService.class),Id.createLinkId("259"))
//                .setCapacityDemand(1)
//                .setServiceDuration(180)
//                .setServiceStartTimeWindow(TimeWindow.newInstance(12*3600,13*3600))
//                .build());

        pizza_2.getCarrierCapabilities().setFleetSize(CarrierCapabilities.FleetSize.INFINITE);
        pizza_2.getCarrierCapabilities().getCarrierVehicles().add(getLightVehicle(pizza_2.getId(), type, Id.createLinkId(111), "one"));
        pizza_2.getCarrierCapabilities().getVehicleTypes().add(createLightType());

        shopping_1.getCarrierCapabilities().setFleetSize(CarrierCapabilities.FleetSize.INFINITE);
        shopping_1.getCarrierCapabilities().getCarrierVehicles().add(getLightVehicle(shopping_1.getId(), type, Id.createLinkId(111), "one"));
        shopping_1.getCarrierCapabilities().getVehicleTypes().add(createLightType());

        carriers.addCarrier(pizza_1);
        carriers.addCarrier(pizza_2);
        carriers.addCarrier(shopping_1);

        return carriers;
    }


    private static void addPopulation(Scenario scenario) {
        Person p = scenario.getPopulation().getFactory().createPerson(Id.createPersonId(1));
        Plan plan = scenario.getPopulation().getFactory().createPlan();
        p.addPlan(plan);

        Activity home = PopulationUtils.createActivityFromCoord("home", new Coord(-200, 800));
        home.setLinkId(Id.createLinkId(116));
        home.setEndTime(8 * 3600);

        plan.addActivity(home);
        plan.addLeg(PopulationUtils.createLeg(TransportMode.car));

        Activity work = PopulationUtils.createActivityFromCoord("home", new Coord(0, 0));
        work.setLinkId(Id.createLinkId(259));
        work.setEndTime(16 * 3600);

        work.getAttributes().putAttribute(CommercialJobUtilsV2.JOB_TYPE, "pizza");
        work.getAttributes().putAttribute(CommercialJobUtilsV2.JOB_DURATION, 180);
        work.getAttributes().putAttribute(CommercialJobUtilsV2.JOB_EARLIEST_START, 12 * 3600);
        work.getAttributes().putAttribute(CommercialJobUtilsV2.JOB_TIME_END, 13 * 3600);
        work.getAttributes().putAttribute(CommercialJobUtilsV2.JOB_OPERATOR, 1);
        work.getAttributes().putAttribute(CommercialJobUtilsV2.JOB_SIZE, 1);

        plan.addActivity(work);


        plan.addLeg(PopulationUtils.createLeg(TransportMode.car));


        Activity home2 = PopulationUtils.createActivityFromCoord("home", new Coord(-200, 800));
        home2.setLinkId(Id.createLinkId(116));
        plan.addActivity(home2);
        scenario.getPopulation().addPerson(p);

    }


    private static CarrierVehicle getLightVehicle(Id<?> id, VehicleType type, Id<Link> homeId, String depot) {
        CarrierVehicle.Builder vBuilder = CarrierVehicle.Builder.newInstance(Id.create((id.toString() + "_lightVehicle_" + depot), Vehicle.class), homeId);
        vBuilder.setEarliestStart(6 * 60 * 60);
        vBuilder.setLatestEnd(16 * 60 * 60);
        vBuilder.setType(type);
        vBuilder.setTypeId(type.getId());
        return vBuilder.build();
    }

    private static VehicleType createLightType() {
        VehicleType type = VehicleUtils.createVehicleType(Id.create("small", VehicleType.class));
        type.getCapacity().setOther(6);
        type.getCostInformation().setFixedCost(80.0);
        type.getCostInformation().setCostsPerMeter(0.00047);
        type.getCostInformation().setCostsPerSecond(0.008);
        return type;
    }


}
