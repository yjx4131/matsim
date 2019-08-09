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

package commercialtraffic.jobGeneration;/*
 * created by jbischoff, 11.04.2019
 */

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.SchrimpfFactory;
import com.graphhopper.jsprit.core.algorithm.termination.VariationCoefficientTermination;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.util.Solutions;
import commercialtraffic.integration.CarrierMode;
import commercialtraffic.integration.CommercialTrafficChecker;
import commercialtraffic.integration.CommercialTrafficConfigGroup;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.*;
import org.matsim.contrib.freight.carrier.*;
import org.matsim.contrib.freight.jsprit.MatsimJspritFactory;
import org.matsim.contrib.freight.jsprit.NetworkBasedTransportCosts;
import org.matsim.contrib.freight.jsprit.NetworkRouter;
import org.matsim.core.controler.events.AfterMobsimEvent;
import org.matsim.core.controler.events.BeforeMobsimEvent;
import org.matsim.core.controler.listener.AfterMobsimListener;
import org.matsim.core.controler.listener.BeforeMobsimListener;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.population.routes.RouteUtils;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.trafficmonitoring.FreeSpeedTravelTime;
import org.matsim.core.utils.misc.Time;
import org.matsim.vehicles.Vehicle;

import javax.inject.Inject;
import java.util.*;

/**
 * Generates carriers and tours depending on next iteration's freight demand
 */
public class DeliveryGenerator implements BeforeMobsimListener, AfterMobsimListener {



    private final double firsttourTraveltimeBuffer;
    private final int maxIterations;
    private final CarrierMode carrierMode;

    private Scenario scenario;
    private Population population;
    private final CommercialJobManager jobManager;

    private final TravelTime carTT;

    private Carriers carriersForIteration;
    private Set<Id<Person>> freightDrivers = new HashSet<>();
    private Set<Id<Vehicle>> freightVehicles = new HashSet<>();

    private final static Logger log = Logger.getLogger(DeliveryGenerator.class);

    @Override
    public void notifyBeforeMobsim(BeforeMobsimEvent event) {
//        generateIterationServices();
        this.carriersForIteration = jobManager.getCarriersWithServicesForIteration();
        runTourPlanning();
        createFreightAgents();
        writeCarriersFileForIteration(event);
    }

    @Inject
    public DeliveryGenerator(Scenario scenario, Map<String, TravelTime> travelTimes, CarrierMode carrierMode, CommercialJobManager jobManager) {
        CommercialTrafficConfigGroup ctcg = CommercialTrafficConfigGroup.get(scenario.getConfig());
        firsttourTraveltimeBuffer = ctcg.getFirstLegTraveltimeBufferFactor();
        this.carrierMode = carrierMode;
        this.scenario = scenario;
        this.population = scenario.getPopulation();
        this.jobManager = jobManager;
        maxIterations = ctcg.getJspritIterations();
        carTT = travelTimes.get(TransportMode.car);
        if (CommercialTrafficChecker.hasMissingAttributes(population)) {
            throw new RuntimeException("Not all agents expecting deliveries contain all required attributes fo receival. Please check the log for DeliveryConsistencyChecker. Aborting.");
        }

    }

    /**
     * Test only
     */
    DeliveryGenerator(Scenario scenario,  CommercialJobManager jobManager) {
        this.population = scenario.getPopulation();
        this.scenario = scenario;
        this.jobManager = jobManager;
        firsttourTraveltimeBuffer = 2;
        maxIterations = 100;
        carTT = new FreeSpeedTravelTime();
        carrierMode = (m -> TransportMode.car);
    }

//    private void generateIterationServices() {
//        hullcarriers.getCarriers().values().forEach(carrier -> carrier.getServices().clear());
//
//        Map<Person,Set<PlanElement>> person2ActsWithServices = new HashMap();
//
//        population.getPersons().values().forEach(p ->
//        {
//            Set<PlanElement> activitiesWithServices = new HashSet<>(p.getSelectedPlan().getPlanElements().stream()
//                    .filter(Activity.class::isInstance)
//                    .filter(a -> a.getAttributes().getAsMap().containsKey(CommercialJobUtils.JOB_TYPE))
//                    .collect(Collectors.toSet()));
//            person2ActsWithServices.put(p,activitiesWithServices);
//        });
//        for(Person personWithDelivieries : person2ActsWithServices.keySet()){
//            int i = 0;
//            for (PlanElement pe : person2ActsWithServices.get(personWithDelivieries)) {
//                Activity activity = (Activity) pe;
//                CarrierService.Builder serviceBuilder = CarrierService.Builder.newInstance(createCarrierServiceIdXForCustomer(personWithDelivieries,i), activity.getLinkId());
//                serviceBuilder.setCapacityDemand(Integer.valueOf(String.valueOf(activity.getAttributes().getAttribute(CommercialJobUtils.JOB_SIZE))));
//                serviceBuilder.setServiceDuration(Integer.valueOf(String.valueOf(activity.getAttributes().getAttribute(CommercialJobUtils.JOB_DURATION))));
//                serviceBuilder.setServiceStartTimeWindow(TimeWindow.newInstance(Double.valueOf(String.valueOf(activity.getAttributes().getAttribute(CommercialJobUtils.JOB_EARLIEST_START))), Double.valueOf(String.valueOf(activity.getAttributes().getAttribute(CommercialJobUtils.JOB_TIME_END)))));
//                i++;
//                Id<Carrier> carrierId = CommercialJobUtils.getCarrierId(activity);
//                if (hullcarriers.getCarriers().containsKey(carrierId)) {
//                    Carrier carrier = hullcarriers.getCarriers().get(carrierId);
//                    carrier.getServices().add(serviceBuilder.build());
//                } else {
//                    throw new RuntimeException("Carrier Id does not exist: " + carrierId.toString());
//                }
//            }
//        }
//
//    }
    private static String createServiceActTypeFromServiceId(Id<CarrierService> id) {
        return FreightConstants.DELIVERY + CommercialJobUtils.CARRIERSPLIT + id;
    }

    public static Id<Person> getCustomerIdFromDeliveryActivityType(String actType){
        return Id.createPersonId(actType.substring(actType.indexOf(CommercialJobUtils.CARRIERSPLIT)+1,actType.length()));
    }

    private void runTourPlanning() {
        Set<CarrierVehicleType> vehicleTypes = new HashSet<>();
        carriersForIteration.getCarriers().values().forEach(carrier -> vehicleTypes.addAll(carrier.getCarrierCapabilities().getVehicleTypes()));
        NetworkBasedTransportCosts.Builder netBuilder = NetworkBasedTransportCosts.Builder.newInstance(scenario.getNetwork(), vehicleTypes);
        netBuilder.setTimeSliceWidth(900); // !!!! otherwise it will not do anything.
        netBuilder.setTravelTime(carTT);
        final NetworkBasedTransportCosts netBasedCosts = netBuilder.build();
        carriersForIteration.getCarriers().values().parallelStream().forEach(carrier -> {
                    //Build VRP
                    VehicleRoutingProblem.Builder vrpBuilder = MatsimJspritFactory.createRoutingProblemBuilder(carrier, scenario.getNetwork());
                    //            vrpBuilder.setRoutingCost(netBasedCosts);
                    // this is too expansive for the size of the problem
                    VehicleRoutingProblem problem = vrpBuilder.build();
                    // get the algorithm out-of-the-box, search solution and get the best one.
                    VehicleRoutingAlgorithm algorithm = new SchrimpfFactory().createAlgorithm(problem);
                    algorithm.setMaxIterations(maxIterations);
                    // variationCoefficient = stdDeviation/mean. so i set the threshold rather soft
                    algorithm.addTerminationCriterion(new VariationCoefficientTermination(5, 0.1));
                    Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();
                    VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);
                    //get the CarrierPlan
                    CarrierPlan carrierPlan = MatsimJspritFactory.createPlan(carrier, bestSolution);
                    NetworkRouter.routePlan(carrierPlan, netBasedCosts);
                    carrier.setSelectedPlan(carrierPlan);
                }
        );



    }

    private void createFreightAgents() {
        for (Carrier carrier : carriersForIteration.getCarriers().values()) {
            int nextId = 0;
            for (ScheduledTour scheduledTour : carrier.getSelectedPlan().getScheduledTours()) {

                CarrierVehicle carrierVehicle = scheduledTour.getVehicle();

                Id<Person> driverId = Id.createPersonId("freight_" + carrier.getId() + "_veh_" + carrierVehicle.getVehicleId() + "_" + nextId);
                nextId++;

                Person driverPerson = createDriverPerson(driverId);
                Plan plan = PopulationUtils.createPlan();
                Activity startActivity = PopulationUtils.createActivityFromLinkId(FreightConstants.START, scheduledTour.getVehicle().getLocation());
                plan.addActivity(startActivity);
                Activity lastTourElementActivity = null;
                Leg lastTourLeg = null;


                for (Tour.TourElement tourElement : scheduledTour.getTour().getTourElements()) {
                    if (tourElement instanceof org.matsim.contrib.freight.carrier.Tour.Leg) {


                        org.matsim.contrib.freight.carrier.Tour.Leg tourLeg = (org.matsim.contrib.freight.carrier.Tour.Leg) tourElement;
                        Route route = tourLeg.getRoute();
                        route.setDistance(RouteUtils.calcDistance((NetworkRoute) route, 1.0, 1.0, scenario.getNetwork()));
                        if (route == null)
                            throw new IllegalStateException("missing route for carrier " + carrier.getId());
                        route.setTravelTime(tourLeg.getExpectedTransportTime());
                        Leg leg = PopulationUtils.createLeg(carrierMode.getCarrierMode(carrier.getId()));
                        leg.setRoute(route);
                        leg.setDepartureTime(tourLeg.getExpectedDepartureTime());
                        leg.setTravelTime(tourLeg.getExpectedTransportTime());
                        leg.setTravelTime(tourLeg.getExpectedDepartureTime() + tourLeg.getExpectedTransportTime() - leg.getDepartureTime());
                        plan.addLeg(leg);
                        if (lastTourElementActivity != null) {
                            lastTourElementActivity.setEndTime(tourLeg.getExpectedDepartureTime());
                            if (Time.isUndefinedTime(startActivity.getEndTime())) {
                                startActivity.setEndTime(lastTourElementActivity.getEndTime() - lastTourElementActivity.getMaximumDuration() - lastTourLeg.getTravelTime() * firsttourTraveltimeBuffer);
                                lastTourElementActivity.setMaximumDuration(Time.getUndefinedTime());
                            }
                        }
                        lastTourLeg = leg;

                    } else if (tourElement instanceof Tour.TourActivity) {
                        if(! (tourElement instanceof  Tour.ServiceActivity)) throw new RuntimeException("currently only services (not shipments) are supported!");

                        Tour.ServiceActivity act = (Tour.ServiceActivity) tourElement;

                        String actType = createServiceActTypeFromServiceId(act.getService().getId());

                        //here we would pass over the service Activity type containing the customer id...
                        Activity tourElementActivity = PopulationUtils.createActivityFromLinkId(actType, act.getLocation());
                        plan.addActivity(tourElementActivity);
                        if (lastTourElementActivity == null) {
                            tourElementActivity.setMaximumDuration(act.getDuration());
                        }
                        lastTourElementActivity = tourElementActivity;
                    }
                }
                Activity endActivity = PopulationUtils.createActivityFromLinkId(FreightConstants.END, scheduledTour.getVehicle().getLocation());
                plan.addActivity(endActivity);
                driverPerson.addPlan(plan);
                plan.setPerson(driverPerson);

                scenario.getPopulation().addPerson(driverPerson);
                try {
                    scenario.getVehicles().addVehicleType(carrierVehicle.getVehicleType());
                } catch (IllegalArgumentException e) {
                }
                Id<Vehicle> vid = Id.createVehicleId(driverPerson.getId());
                scenario.getVehicles().addVehicle(scenario.getVehicles().getFactory().createVehicle(vid, carrierVehicle.getVehicleType()));
                freightVehicles.add(vid);
                freightDrivers.add(driverPerson.getId());
            }
        }
    }

    private Person createDriverPerson(Id<Person> driverId) {
        final Id<Person> id = driverId;
        Person person = PopulationUtils.getFactory().createPerson(id);
        return person;
    }

    @Override
    public void notifyAfterMobsim(AfterMobsimEvent event) {
        removeFreightAgents();
    }

    private void removeFreightAgents() {
        freightDrivers.forEach(d -> scenario.getPopulation().removePerson(d));
        freightVehicles.forEach(vehicleId -> scenario.getVehicles().removeVehicle(vehicleId));
        CarrierVehicleTypes.getVehicleTypes(carriersForIteration).getVehicleTypes().keySet().forEach(vehicleTypeId -> scenario.getVehicles().removeVehicleType(vehicleTypeId));
//        carriersForIteration.getCarriers().values().forEach(carrier -> {
//            carrier.getServices().clear();
//            carrier.getShipments().clear();
//            carrier.clearPlans();
//        });
    }

    private void writeCarriersFileForIteration(BeforeMobsimEvent event) {
        String dir = event.getServices().getConfig().controler().getOutputDirectory() + "/ITERS/it." + event.getIteration() + "/";
        log.info("writing carrier file of iteration " + event.getIteration() + " to " + dir);
        CarrierPlanXmlWriterV2 planWriter = new CarrierPlanXmlWriterV2(carriersForIteration);
        planWriter.write(dir + "carriers_it" + event.getIteration() + ".xml");
    }
}
