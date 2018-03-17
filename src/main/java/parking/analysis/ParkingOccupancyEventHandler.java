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

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.mutable.MutableInt;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.handler.PersonArrivalEventHandler;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.core.utils.misc.Time;

import parking.ParkingZone;
import parking.ZonalLinkParkingInfo;
import parking.capacityCalculation.LinkParkingCapacityCalculator;

public class ParkingOccupancyEventHandler implements PersonArrivalEventHandler, PersonDepartureEventHandler {

	private ZonalLinkParkingInfo parkingInfo;
	private LinkParkingCapacityCalculator calculator;
	private Network network;
	private Map<Id<ParkingZone>,int[]> zoneoccupancyPerBin = new HashMap<>();  
	private int bins;
	public ParkingOccupancyEventHandler(ZonalLinkParkingInfo parkingInfo, LinkParkingCapacityCalculator calculator,
			Network network, double simEndTime) {
		this.parkingInfo = parkingInfo;
		this.calculator = calculator;
		this.network = network;
		bins = (int) (simEndTime / 900);
		for (Id<ParkingZone> zone : parkingInfo.getParkingZones().keySet()) {
			zoneoccupancyPerBin.put(zone,new int[bins]);
		}
		}

	@Override
	public void handleEvent(PersonDepartureEvent event) {
		if (event.getLegMode().equals(TransportMode.car)) {
			ParkingZone zone = parkingInfo.getParkingZone(network.getLinks().get(event.getLinkId()));
			if (zone!=null) {
				this.zoneoccupancyPerBin.get(zone.getId())[getBin(event.getTime())]++;
			}
		}
	}

	@Override
	public void handleEvent(PersonArrivalEvent event) {
		if (event.getLegMode().equals(TransportMode.car)) {
			ParkingZone zone = parkingInfo.getParkingZone(network.getLinks().get(event.getLinkId()));
			if (zone!=null) {
				this.zoneoccupancyPerBin.get(zone.getId())[getBin(event.getTime())]--;
			}
		}
		
	}
	
	private int getBin(double time) {
		return Math.floorDiv((int) time, 900);
		
	}
	
	public void writeParkingOccupancyStats(String file) {
		BufferedWriter bw = IOUtils.getBufferedWriter(file);
		try {
			bw.write("Zone;Capacity");
			for (int i = 0; i<bins; i++) {
				bw.write(";"+Time.writeTime(i*900));
			}
			for (Entry<Id<ParkingZone>, int[]> e : this.zoneoccupancyPerBin.entrySet()) {
				bw.newLine();
				final MutableInt parkingCapacity = new MutableInt();
				this.parkingInfo.getParkingZones().get(e.getKey()).getLinksInZone().forEach(lid -> parkingCapacity.add(calculator.getLinkCapacity(network.getLinks().get(lid))));
				bw.write(e.getKey()+";"+parkingCapacity.intValue());
				int sum = 0;
				for (int i = 0 ; i< e.getValue().length;i++) {
					sum+=e.getValue()[i];
					bw.write(";"+sum);
				}
			}
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
