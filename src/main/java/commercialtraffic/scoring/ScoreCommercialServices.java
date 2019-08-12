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

package commercialtraffic.scoring;/*
 * created by jbischoff, 17.06.2019
 */

import com.google.inject.Inject;
import commercialtraffic.jobGeneration.CommercialJobManager;
import commercialtraffic.jobGeneration.CommercialJobUtils;
import commercialtraffic.jobGeneration.DeliveryGenerator;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.ActivityStartEvent;
import org.matsim.api.core.v01.events.PersonMoneyEvent;
import org.matsim.api.core.v01.events.handler.ActivityEndEventHandler;
import org.matsim.api.core.v01.events.handler.ActivityStartEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.CarrierService;
import org.matsim.contrib.freight.carrier.FreightConstants;
import org.matsim.core.api.experimental.events.EventsManager;

import java.util.*;
import java.util.stream.Collectors;

public class ScoreCommercialServices implements ActivityStartEventHandler, ActivityEndEventHandler {



    private final DeliveryScoreCalculator scoreCalculator;
    private final EventsManager eventsManager;
    private final CommercialJobManager jobManager;

    private final Set<Id<Person>> activeDeliveryAgents = new HashSet<>();
    private final Map<Id<Link>, Set<ExpectedDelivery>> currentExpectedDeliveriesPerLink = new HashMap<>();
    private final List<DeliveryLogEntry> logEntries = new ArrayList<>();

    private Map<Id<CarrierService>, CarrierService> carrierServicesForThisIteration;

    @Inject
    public ScoreCommercialServices(CommercialJobManager manager, DeliveryScoreCalculator scoreCalculator, EventsManager eventsManager) {
        this.jobManager = manager;
        this.scoreCalculator = scoreCalculator;
        this.eventsManager = eventsManager;
        this.eventsManager.addHandler(this);
    }

    @Override
    public void handleEvent(ActivityStartEvent event) {
        if (activeDeliveryAgents.contains(event.getPersonId())) {
            handleFreightActivityStart(event);
        }

    }


//    public void prepareTourArrivalsForDay() {
//        currentExpectedDeliveriesPerLink.clear();
//        Set<Plan> plans = population.getPersons().values().stream()
//                .map(p -> p.getSelectedPlan())
//                .filter(plan -> CommercialJobUtils.planExpectsDeliveries(plan)).collect(Collectors.toSet());
//        for (Plan plan : plans) {
//            plan.getPlanElements().stream().filter(Activity.class::isInstance).forEach(pe -> {
//                Activity activity = (Activity) pe;
//                if (CommercialJobUtils.activityExpectsServices(activity)) {
//                    ExpectedDelivery expectedDelivery = new ExpectedDelivery((String) activity.getAttributes().getAttribute(CommercialJobUtils.JOB_TYPE)
//                            , CommercialJobUtils.getCarrierId(activity)
//                            , plan.getPerson().getId()
//                            , Double.valueOf(String.valueOf(activity.getAttributes().getAttribute(CommercialJobUtils.JOB_DURATION)))
//                            , Double.valueOf(String.valueOf(activity.getAttributes().getAttribute(CommercialJobUtils.JOB_EARLIEST_START)))
//                            , Double.valueOf(String.valueOf(activity.getAttributes().getAttribute(CommercialJobUtils.JOB_TIME_END))));
//                    Set<ExpectedDelivery> set = currentExpectedDeliveriesPerLink.getOrDefault(activity.getLinkId(), new HashSet<>());
//                    set.add(expectedDelivery);
//                    currentExpectedDeliveriesPerLink.put(activity.getLinkId(), set);
//
//                }
//
//            });
//        }
//        Logger.getLogger(getClass()).info(currentExpectedDeliveriesPerLink.size() + " links expect deliveries");
//
//    }


    private void handleFreightActivityStart(ActivityStartEvent event) {
        if (event.getActType().equals(FreightConstants.END)) {
            activeDeliveryAgents.remove(event.getPersonId());
        } else if (event.getActType().contains(FreightConstants.DELIVERY)) {
            Id<CarrierService> serviceId = DeliveryGenerator.getServiceIdFromActivityType(event.getActType());
            CarrierService service = this.carrierServicesForThisIteration.remove(serviceId);
            if(service == null) throw new IllegalStateException("no service with id " + serviceId + " expected. already started???");
            Id<Carrier> carrier = jobManager.getCurrentCarrierOfService(serviceId);

                double timeDifference = calcDifference(service, event.getTime());
                double score = scoreCalculator.calcScore(timeDifference);

                // Ich muss die KundenId zum service mappen! Das sollte im JobManager passieren!

                //Job Manager und deliveryGenerator sollten mehr zusammengefasst werden (entweder TourPlanningskript + manager oder nur manager)



                eventsManager.processEvent(new PersonMoneyEvent(event.getTime(), deliveryCandidate.getPersonId(), score));



                logEntries.add(new DeliveryLogEntry(deliveryCandidate.getPersonId(), deliveryCandidate.getCarrier(), event.getTime(), score, event.getLinkId(), timeDifference, event.getPersonId()));

            } else {
                Logger.getLogger(getClass()).warn("No available deliveries expected at link " + event.getLinkId());
            }
    }

    private double calcDifference(CarrierService service, double time) {
        if (time < service.getServiceStartTimeWindow().getStart()) return (service.getServiceStartTimeWindow().getStart() - time);
        else if (time >= service.getServiceStartTimeWindow().getStart() && time <= service.getServiceStartTimeWindow().getEnd()) return 0;
        else return (time - service.getServiceStartTimeWindow().getEnd());
    }

    @Override
    public void reset(int iteration) {
        this.carrierServicesForThisIteration = new HashMap<>(this.jobManager.getCarrierServicesMap());
        activeDeliveryAgents.clear();
        logEntries.clear();

    }


    @Override
    public void handleEvent(ActivityEndEvent event) {
        if (event.getActType().equals(FreightConstants.START)) {
            activeDeliveryAgents.add(event.getPersonId());
        }
    }

    public List<DeliveryLogEntry> getLogEntries() {
        return logEntries;
    }

    static class ExpectedDelivery {
        private final String type;
        private final Id<Carrier> carrier;

        private final Id<Person> personId;
        private final double deliveryDuration;
        private final double startTime;
        private final double endTime;

        ExpectedDelivery(String type, Id<Carrier> carrier, Id<Person> personId, double deliveryDuration, double startTime, double endTime) {
            this.type = type;
            this.carrier = carrier;
            this.personId = personId;
            this.deliveryDuration = deliveryDuration;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public String getType() {
            return type;
        }

        public Id<Carrier> getCarrier() {
            return carrier;
        }

        public Id<Person> getPersonId() {
            return personId;
        }

        public double getDeliveryDuration() {
            return deliveryDuration;
        }

        public Double getStartTime() {
            return startTime;
        }

        public double getEndTime() {
            return endTime;
        }

        @Override
        public String toString(){
            return "[person=" + personId +";" + "type=" + type +";" + "carrier=" + carrier + ";" + "start=" + startTime + ";" + "end=" + endTime + "]";
        }
    }


    public static class DeliveryLogEntry {
        private final Id<Person> personId;
        private final Id<Carrier> carrierId;
        private final double time;
        private final double score;
        private final Id<Link> linkId;
        private final double timeDifference;
        private final Id<Person> driverId;

        public DeliveryLogEntry(Id<Person> personId, Id<Carrier> carrierId, double time, double score, Id<Link> linkId, double timeDifference, Id<Person> driverId) {
            this.personId = personId;
            this.carrierId = carrierId;
            this.time = time;
            this.score = score;
            this.linkId = linkId;
            this.timeDifference = timeDifference;
            this.driverId = driverId;
        }

        public Id<Person> getPersonId() {
            return personId;
        }

        public Id<Carrier> getCarrierId() {
            return carrierId;
        }

        public double getTime() {
            return time;
        }

        public double getScore() {
            return score;
        }

        public Id<Link> getLinkId() {
            return linkId;
        }

        public double getTimeDifference() {
            return timeDifference;
        }

        public Id<Person> getDriverId() {
            return driverId;
        }
    }
}
