/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2017 by the members listed in the COPYING,        *
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

/**
 * 
 */
package ft.cemdap4H.run;

import javax.inject.Inject;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.cadyts.car.CadytsCarModule;
import org.matsim.contrib.cadyts.car.CadytsContext;
import org.matsim.contrib.cadyts.general.CadytsConfigGroup;
import org.matsim.contrib.cadyts.general.CadytsScoring;
import org.matsim.contrib.ev.temperature.TemperatureChangeConfigGroup;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.CountsConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.scoring.ScoringFunction;
import org.matsim.core.scoring.ScoringFunctionFactory;
import org.matsim.core.scoring.SumScoringFunction;
import org.matsim.core.scoring.functions.CharyparNagelActivityScoring;
import org.matsim.core.scoring.functions.CharyparNagelAgentStuckScoring;
import org.matsim.core.scoring.functions.CharyparNagelLegScoring;
import org.matsim.core.scoring.functions.ScoringParameters;
import org.matsim.core.scoring.functions.ScoringParametersForPerson;
import org.matsim.core.scoring.functions.SubpopulationScoringParameters;

import ch.sbb.matsim.routing.pt.raptor.SwissRailRaptorModule;

//import ch.sbb.matsim.routing.pt.raptor.SwissRailRaptorModule;

//import ch.sbb.matsim.routing.pt.raptor.*;


/**
 * @author  jbischoff
 *
 */

public class RunCemdapBasecase {
public static void main(String[] args) {
	String runId = "vw241_cadON_timeFix";
	String pct = ".0.1";
	
	String configPath = "D:\\Thiel\\Programme\\MatSim\\01_HannoverModel_2.0\\Simulation\\config_0.1.xml"; 
		
	Config config = ConfigUtils.loadConfig(configPath, new CadytsConfigGroup());
	
	
	config.plans().setInputFile("D:\\Thiel\\Programme\\MatSim\\01_HannoverModel_2.0\\Simulation\\input\\finishedPlans_0.1_timeFIX.xml.gz");
	Scenario scenario = ScenarioUtils.loadScenario(config);
	adjustPtNetworkCapacity(scenario.getNetwork(),config.qsim().getFlowCapFactor());
	
	Controler controler = new Controler(scenario);
	controler.addOverridingModule(new CadytsCarModule());
	
	//Override cadyts params
	CadytsConfigGroup ccg = (CadytsConfigGroup) config.getModules().get(CadytsConfigGroup.GROUP_NAME);
	ccg.setStartTime(0);	
	ccg.setEndTime(24*3600);
	
	//Override Counts params
	CountsConfigGroup countsccg = (CountsConfigGroup) config.getModules().get(CountsConfigGroup.GROUP_NAME);
	countsccg.setInputFile("D:\\Thiel\\Programme\\MatSim\\01_HannoverModel_2.0\\Simulation\\input\\Counts\\counts_H_LSA.xml");
	
	
	config.controler().setOutputDirectory("D:\\Thiel\\Programme\\MatSim\\01_HannoverModel_2.0\\Simulation\\output\\"+runId+pct);
	config.controler().setRunId(runId+pct);
	config.controler().setWritePlansInterval(50);
	config.controler().setWriteEventsInterval(50);
	config.controler().setLastIteration(400); //Number of simulation iterations
	
	// tell the system to use the congested car router for the ride mode:
	controler.addOverridingModule(new AbstractModule(){
		@Override public void install() {
			addTravelTimeBinding(TransportMode.ride).to(networkTravelTime());
			addTravelDisutilityFactoryBinding(TransportMode.ride).to(carTravelDisutilityFactoryKey());		}
	});
	
	controler.addOverridingModule(new AbstractModule() {
	    @Override
	    public void install() {
	        install(new SwissRailRaptorModule());
//	        bind(RaptorRouteSelector.class).to(MyCustomRouteSelector.class);
	    }
	});
	
	
	// include cadyts into the plan scoring (this will add the cadyts corrections to the scores):
	controler.setScoringFunctionFactory(new ScoringFunctionFactory() {
		private final ScoringParametersForPerson parameters = new SubpopulationScoringParameters( scenario );
		@Inject CadytsContext cContext;
		@Override
		public ScoringFunction createNewScoringFunction(Person person) {

			final ScoringParameters params = parameters.getScoringParameters( person );
			
			SumScoringFunction scoringFunctionAccumulator = new SumScoringFunction();
			scoringFunctionAccumulator.addScoringFunction(new CharyparNagelLegScoring(params, controler.getScenario().getNetwork()));
			scoringFunctionAccumulator.addScoringFunction(new CharyparNagelActivityScoring(params)) ;
			scoringFunctionAccumulator.addScoringFunction(new CharyparNagelAgentStuckScoring(params));

			final CadytsScoring<Link> scoringFunction = new CadytsScoring<>(person.getSelectedPlan(), config, cContext);
			final double cadytsScoringWeight = 20. * config.planCalcScore().getBrainExpBeta();
			
			scoringFunction.setWeightOfCadytsCorrection(cadytsScoringWeight) ;
			scoringFunctionAccumulator.addScoringFunction(scoringFunction );

			return scoringFunctionAccumulator;
		}
	}) ;
		
	controler.run();
	
	
}
/**
 * this is useful for pt links when only a fraction of the population is simulated, but bus frequency remains the same. 
 * Otherwise, pt vehicles may get stuck.
 */
	private static void adjustPtNetworkCapacity(Network network, double flowCapacityFactor){
		if (flowCapacityFactor<1.0){
			for (Link l : network.getLinks().values()){
				if (l.getAllowedModes().contains(TransportMode.pt)){
					l.setCapacity(l.getCapacity()/flowCapacityFactor);
				}
			}
		}
	}
}
