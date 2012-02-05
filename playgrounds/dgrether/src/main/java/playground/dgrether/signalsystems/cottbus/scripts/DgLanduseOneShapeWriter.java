/* *********************************************************************** *
 * project: org.matsim.*
 * DgLanduseKmlWriter
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2012 by the members listed in the COPYING,        *
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
package playground.dgrether.signalsystems.cottbus.scripts;

import java.util.Set;

import org.geotools.feature.Feature;
import org.matsim.core.utils.collections.Tuple;
import org.matsim.core.utils.gis.ShapeFileWriter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import playground.dgrether.signalsystems.cottbus.commuterdemand.DgLanduseReader;


/**
 * @author dgrether
 *
 */
public class DgLanduseOneShapeWriter {


	public static void main(String[] args) {
		DgLanduseReader landuseReader = new DgLanduseReader();
		Tuple<Set<Feature>, CoordinateReferenceSystem> homeLanduse = landuseReader.readLanduseDataHome();
		ShapeFileWriter.writeGeometries(homeLanduse.getFirst(), "/media/data/work/repos/shared-svn/studies/dgrether/cottbus/cottbus_feb_fix/cb_spn_gemeinde_nachfrage_landuse/shapes/landuse_home.shp");
	
		Tuple<Set<Feature>, CoordinateReferenceSystem> workLanduse = landuseReader.readLanduseDataWork();
		for (Feature ft : workLanduse.getFirst()){
		}
		
		ShapeFileWriter.writeGeometries(workLanduse.getFirst(), "/media/data/work/repos/shared-svn/studies/dgrether/cottbus/cottbus_feb_fix/cb_spn_gemeinde_nachfrage_landuse/shapes/landuse_work.shp");
		
	}


}
