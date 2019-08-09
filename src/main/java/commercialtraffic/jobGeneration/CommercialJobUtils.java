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


import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.CarrierService;
import org.matsim.contrib.freight.carrier.Carriers;
import org.matsim.core.gbl.MatsimRandom;

import java.util.*;
import java.util.stream.Collectors;

public class CommercialJobUtils {

//    public static final String JOB_SIZE = "jobAmount";
//    public static final String JOB_TYPE = "jobType";
//    public static final String JOB_DURATION = "jobDuration";
//    public static final String JOB_EARLIEST_START = "jobTimeStart";
//    public static final String JOB_OPERATOR = "operator";
//    public static final String JOB_TIME_END = "jobTimeEnd";
    public static final String JOB_ID = "jobId";
    public static final String JOB_ID_REGEX = ";";
    public static final String CARRIERSPLIT = "_";

    public static Id<CarrierService> getRandomServiceFromActivity (Activity activity, Random random){
        if(! activityExpectsServices(activity)) throw new IllegalArgumentException("can not retrieve service from activity " + activity);
        String[] jobIds = String.valueOf(activity.getAttributes().getAttribute(CommercialJobUtils.JOB_ID)).split(CommercialJobUtils.JOB_ID_REGEX);
        return Id.create(jobIds[random.nextInt(jobIds.length)],CarrierService.class);
    }

    public static Id<Carrier> getCarrierIdFromDriver(Id<Person> personId) {
        return Id.create(personId.toString().split(CARRIERSPLIT)[1] + CARRIERSPLIT + personId.toString().split(CARRIERSPLIT)[2], Carrier.class);
    }

    public static boolean planExpectsDeliveries(Plan plan) {
        return plan.getPlanElements().stream()
                .filter(Activity.class::isInstance)
                .anyMatch(planElement -> planElement.getAttributes().getAsMap().containsKey(JOB_ID));
    }

    public static boolean activityExpectsServices (Activity activity){
        return activity.getAttributes().getAsMap().containsKey(CommercialJobUtils.JOB_ID);
    }

    public static String getCarrierMarket(Id<Carrier> carrierId) {
        return carrierId.toString().split(CARRIERSPLIT)[0];
    }


    public static Map<String, Set<Id<Carrier>>> splitCarriersByMarket(Carriers carriers) {
        Map<String, Set<Id<Carrier>>> carriersSplitByMarket = new HashMap<>();
        for (Id<Carrier> carrierId : carriers.getCarriers().keySet()) {
            String market = getCarrierMarket(carrierId);
            Set<Id<Carrier>> carriersForMarket = carriersSplitByMarket.getOrDefault(market, new HashSet<>());
            carriersForMarket.add(carrierId);
            carriersSplitByMarket.put(market, carriersForMarket);
        }
        return carriersSplitByMarket;
    }

}
