/* *********************************************************************** *
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
 * *********************************************************************** */
  
package parking.analysis;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;

import parking.ZonalLinkParkingInfo;
import parking.capacityCalculation.LinkLengthBasedCapacityCalculator;

public class ParkingOccupancyAnalyzer {
public static void main(String[] args) {
	String basefolder = "D:/runs-svn/vw_rufbus/";
	String runId = "vw220park10";
	String eventsFile = basefolder+runId+"/"+runId+".output_events.xml.gz";
	String parkingOccupancyOutputFile = basefolder+runId+"/"+runId+".output_parkingOccupancy.csv";
	String networkFile = basefolder+runId+"/"+runId+".output_network.xml.gz";
	String shapeFile = "C:/Users/Joschka/Documents/shared-svn/projects/vw_rufbus/projekt2/parking/bc-run/shp/parking-bs.shp";
	String shapeString = "NO";
	double endTime = 30*3600;
	
	Network network = NetworkUtils.createNetwork();
	new MatsimNetworkReader(network).readFile(networkFile);
	LinkLengthBasedCapacityCalculator  linkLengthBasedCapacityCalculator = new LinkLengthBasedCapacityCalculator();
	ZonalLinkParkingInfo zonalLinkParkingInfo = new ZonalLinkParkingInfo(shapeFile, shapeString, 0.3, network, linkLengthBasedCapacityCalculator);
	ParkingOccupancyEventHandler parkingOccupancyEventHandler = new ParkingOccupancyEventHandler(zonalLinkParkingInfo, linkLengthBasedCapacityCalculator, network, endTime);
	
	EventsManager events = EventsUtils.createEventsManager();
	events.addHandler(parkingOccupancyEventHandler);
	new MatsimEventsReader(events).readFile(eventsFile);
	parkingOccupancyEventHandler.writeParkingOccupancyStats(parkingOccupancyOutputFile);
}
}
