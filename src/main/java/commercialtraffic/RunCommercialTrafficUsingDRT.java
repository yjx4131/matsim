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

package commercialtraffic;/*
 * created by jbischoff, 03.05.2019
 */

import commercialtraffic.integration.CommercialTrafficConfigGroup;
import commercialtraffic.integration.CommercialTrafficModule;
import commercialtraffic.replanning.ChangeDeliveryServiceOperator;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.drt.run.DrtControlerCreator;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.core.config.Config;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.config.groups.StrategyConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.replanning.strategies.DefaultPlanStrategiesModule;

import java.util.HashMap;

import static org.matsim.core.config.ConfigUtils.createConfig;
import static org.matsim.core.scenario.ScenarioUtils.loadScenario;

public class RunCommercialTrafficUsingDRT {
    public static void main(String[] args) {

        String inputDir = "input/commercialtrafficIt/";

        DrtConfigGroup drtCfg = new DrtConfigGroup();
        drtCfg.setMaxWaitTime(30*60);
        drtCfg.setMaxTravelTimeAlpha(2);
        drtCfg.setMaxTravelTimeBeta(15);
        drtCfg.setStopDuration(60);
        drtCfg.setVehiclesFile(inputDir + "drtVehicles.xml");

        CommercialTrafficConfigGroup commercialTrafficConfigGroup = new CommercialTrafficConfigGroup();
        commercialTrafficConfigGroup.setCarriersFile(inputDir + "test-carriers.xml");
        commercialTrafficConfigGroup.setCarriersVehicleTypesFile(inputDir + "carriertypes.xml");
        commercialTrafficConfigGroup.setFirstLegTraveltimeBufferFactor(1.5);

        Config config = createConfig(drtCfg, new DvrpConfigGroup(), commercialTrafficConfigGroup);

        config.qsim().setSimStarttimeInterpretation(QSimConfigGroup.StarttimeInterpretation.onlyUseStarttime);

        StrategyConfigGroup.StrategySettings changeExpBeta = new StrategyConfigGroup.StrategySettings();
        changeExpBeta.setStrategyName(DefaultPlanStrategiesModule.DefaultSelector.ChangeExpBeta);
        changeExpBeta.setWeight(0.5);
        config.strategy().addStrategySettings(changeExpBeta);

        StrategyConfigGroup.StrategySettings changeServiceOperator = new StrategyConfigGroup.StrategySettings();
        changeServiceOperator.setStrategyName(ChangeDeliveryServiceOperator.SELECTOR_NAME);
        changeServiceOperator.setWeight(0.5);
        config.strategy().addStrategySettings(changeServiceOperator);

        config.strategy().setFractionOfIterationsToDisableInnovation(.8);
        PlanCalcScoreConfigGroup.ActivityParams home = new PlanCalcScoreConfigGroup.ActivityParams("home");
        home.setTypicalDuration(14 * 3600);
        config.planCalcScore().addActivityParams(home);
        PlanCalcScoreConfigGroup.ActivityParams work = new PlanCalcScoreConfigGroup.ActivityParams("work");
        work.setTypicalDuration(14 * 3600);
        work.setOpeningTime(8 * 3600);
        work.setClosingTime(8 * 3600);
        config.planCalcScore().addActivityParams(work);
        config.controler().setLastIteration(10);
        config.controler().setWriteEventsInterval(1);
        config.controler().setOutputDirectory("output/commercialtraffictestrun");
        config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);
        config.network().setInputFile(inputDir + "grid_network.xml");
        config.plans().setInputFile(inputDir + "testpop.xml");


        //so far, we want to have only one drt mode.
        // TODO: Eventually, we might want to distinguish at least 2 drt modes, one for passenger transport and one for freight.
        Controler controler = DrtControlerCreator.createControlerWithSingleModeDrt(config,false);

        controler.addOverridingModule(new CommercialTrafficModule());

        controler.run();


    }
}
