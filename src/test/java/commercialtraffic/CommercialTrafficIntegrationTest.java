package commercialtraffic;

import commercialtraffic.integration.CommercialTrafficConfigGroup;
import commercialtraffic.integration.CommercialTrafficModule;
import org.junit.Test;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;


public class CommercialTrafficIntegrationTest {

    @Test
    public void runCommercialTrafficIT() {
        Config config = ConfigUtils.loadConfig("input/commercialtrafficIT/config.xml", new CommercialTrafficConfigGroup());
        Scenario scenario = ScenarioUtils.loadScenario(config);
        Controler controler = new Controler(scenario);
        controler.addOverridingModule(new CommercialTrafficModule(config));
        controler.run();
    }


}