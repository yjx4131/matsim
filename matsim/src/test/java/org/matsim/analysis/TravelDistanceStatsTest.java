/**
 * 
 */
package org.matsim.analysis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.IdMap;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Route;
import org.matsim.core.config.groups.ControlerConfigGroup;
import org.matsim.core.config.groups.ControlerConfigGroup.CompressionType;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.population.routes.RouteUtils;
import org.matsim.testcases.MatsimTestUtils;

/**
 * @author Aravind
 *
 */
public class TravelDistanceStatsTest {

	HashMap<String, Integer> person3modes = new HashMap<String, Integer>();
	HashMap<String, Integer> person1modes = new HashMap<String, Integer>();
	HashMap<String, Integer> person2modes = new HashMap<String, Integer>();
	private static int avglegdis;
	private static int avgtripdis;
	private Double planlegsum;
	private Double plan2legsum;
	private Double plan3legsum;
	private long planTotalleg;
	private long plan2Totalleg;
	private long plan3Totalleg;
	Person person3 = PopulationUtils.getFactory().createPerson(Id.create(3, Person.class));
	Person person = PopulationUtils.getFactory().createPerson(Id.create(1, Person.class));
	Person person2 = PopulationUtils.getFactory().createPerson(Id.create(2, Person.class));

	@Rule
	public MatsimTestUtils utils = new MatsimTestUtils();

	@Test
	public void testTravelDistanceStats() {

		final IdMap<Person, Plan> map = new IdMap<>(Person.class);

		/* ########Person 1######### --- creating person 1 */
		final Plan plan = PopulationUtils
				.createPlan(PopulationUtils.getFactory().createPerson(Id.create("1", Person.class)));
		person1modes.put(TransportMode.walk, 0);
		person1modes.put(TransportMode.car, 0);
		person1modes.put(TransportMode.pt, 0);

		final Id<Link> link1 = Id.create(10723, Link.class);
		final Id<Link> link2 = Id.create(123160, Link.class);
		final Id<Link> link3 = Id.create(130181, Link.class);
		final Id<Link> link4 = Id.create(139117, Link.class);
		final Id<Link> link5 = Id.create(139100, Link.class);

		Activity act1 = PopulationUtils.createActivityFromLinkId("home", link1);
		plan.addActivity(act1);
		Leg leg1 = PopulationUtils.createLeg(TransportMode.walk);
		Route route = RouteUtils.createGenericRouteImpl(link1, link1);
		route.setDistance(100);
		leg1.setRoute(route);
		plan.addLeg(leg1);
		person1modes.put(TransportMode.walk, person1modes.get(TransportMode.walk) + 1);
		Activity act2 = PopulationUtils.createActivityFromLinkId("leisure", link1);// main mode walk
		plan.addActivity(act2);
		Leg leg2 = PopulationUtils.createLeg(TransportMode.walk);
		Route route2 = RouteUtils.createGenericRouteImpl(link1, link1);
		route2.setDistance(150);
		leg2.setRoute(route2);
		plan.addLeg(leg2);
		Activity act3 = PopulationUtils.createActivityFromLinkId("car interaction", link1);
		plan.addActivity(act3);
		Leg leg3 = PopulationUtils.createLeg(TransportMode.car);
		Route route3 = RouteUtils.createGenericRouteImpl(link1, link2);
		route3.setDistance(5000);
		leg3.setRoute(route3);
		plan.addLeg(leg3);
		Activity act4 = PopulationUtils.createActivityFromLinkId("car interaction", link2);
		plan.addActivity(act4);
		Leg leg4 = PopulationUtils.createLeg(TransportMode.walk);
		Route route4 = RouteUtils.createGenericRouteImpl(link2, link2);
		route4.setDistance(300);
		leg4.setRoute(route4);
		plan.addLeg(leg4);
		person1modes.put(TransportMode.car, person1modes.get(TransportMode.car) + 1);
		Activity act5 = PopulationUtils.createActivityFromLinkId("work", link2);// main mode car
		plan.addActivity(act5);
		Leg leg5 = PopulationUtils.createLeg(TransportMode.walk);
		Route route5 = RouteUtils.createGenericRouteImpl(link2, link2);
		route5.setDistance(300);
		leg5.setRoute(route5);
		plan.addLeg(leg5);
		Activity act6 = PopulationUtils.createActivityFromLinkId("car interaction", link2);
		plan.addActivity(act6);
		Leg leg6 = PopulationUtils.createLeg(TransportMode.car);
		Route route6 = RouteUtils.createGenericRouteImpl(link2, link3);
		route6.setDistance(7000);
		leg6.setRoute(route6);
		plan.addLeg(leg6);
		Activity act7 = PopulationUtils.createActivityFromLinkId("car interaction", link3);
		plan.addActivity(act7);
		Leg leg7 = PopulationUtils.createLeg(TransportMode.walk);
		Route route7 = RouteUtils.createGenericRouteImpl(link3, link3);
		route7.setDistance(150);
		leg7.setRoute(route7);
		plan.addLeg(leg7);
		person1modes.put(TransportMode.car, person1modes.get(TransportMode.car) + 1);
		Activity act8 = PopulationUtils.createActivityFromLinkId("leisure", link3);// main mode car
		plan.addActivity(act8);
		Leg leg8 = PopulationUtils.createLeg(TransportMode.walk);
		leg8.setRoute(route7);
		plan.addLeg(leg8);
		Activity act9 = PopulationUtils.createActivityFromLinkId("car interaction", link3);
		plan.addActivity(act9);
		Leg leg9 = PopulationUtils.createLeg(TransportMode.car);
		Route route9 = RouteUtils.createGenericRouteImpl(link3, link4);
		route9.setDistance(6000);
		leg9.setRoute(route9);
		plan.addLeg(leg9);
		Activity act10 = PopulationUtils.createActivityFromLinkId("car interaction", link4);
		plan.addActivity(act10);
		Leg leg10 = PopulationUtils.createLeg(TransportMode.walk);
		Route route10 = RouteUtils.createGenericRouteImpl(link4, link4);
		route10.setDistance(400);
		leg10.setRoute(route10);
		plan.addLeg(leg10);
		person1modes.put(TransportMode.car, person1modes.get(TransportMode.car) + 1);
		Activity act11 = PopulationUtils.createActivityFromLinkId("shopping", link4);// main mode car
		plan.addActivity(act11);
		Leg leg11 = PopulationUtils.createLeg(TransportMode.walk);
		Route route11 = RouteUtils.createGenericRouteImpl(link4, link4);
		route11.setDistance(300);
		leg11.setRoute(route11);
		plan.addLeg(leg11);
		Activity act12 = PopulationUtils.createActivityFromLinkId("pt interaction", link4);
		plan.addActivity(act12);
		Leg leg12 = PopulationUtils.createLeg(TransportMode.pt);
		Route route12 = RouteUtils.createGenericRouteImpl(link4, link5);
		route12.setDistance(1000);
		leg12.setRoute(route12);
		plan.addLeg(leg12);
		Activity act13 = PopulationUtils.createActivityFromLinkId("pt interaction", link5);
		plan.addActivity(act13);
		Leg leg13 = PopulationUtils.createLeg(TransportMode.walk);
		Route route13 = RouteUtils.createGenericRouteImpl(link5, link5);
		route13.setDistance(200);
		leg13.setRoute(route13);
		plan.addLeg(leg13);
		person1modes.put(TransportMode.pt, person1modes.get(TransportMode.pt) + 1);
		Activity act14 = PopulationUtils.createActivityFromLinkId("shopping", link5);// main mode pt
		plan.addActivity(act14);
		Leg leg14 = PopulationUtils.createLeg(TransportMode.walk);
		leg14.setRoute(route13);
		plan.addLeg(leg14);
		Activity act15 = PopulationUtils.createActivityFromLinkId("pt interaction", link5);
		plan.addActivity(act15);
		Leg leg15 = PopulationUtils.createLeg(TransportMode.pt);
		leg15.setRoute(route12);
		plan.addLeg(leg15);
		Activity act16 = PopulationUtils.createActivityFromLinkId("pt interaction", link4);
		plan.addActivity(act16);
		Leg leg16 = PopulationUtils.createLeg(TransportMode.walk);
		Route route16 = RouteUtils.createGenericRouteImpl(link4, link4);
		route16.setDistance(300);
		leg16.setRoute(route16);
		plan.addLeg(leg16);
		Activity act17 = PopulationUtils.createActivityFromLinkId("car interaction", link4);
		plan.addActivity(act17);
		Leg leg17 = PopulationUtils.createLeg(TransportMode.car);
		Route route17 = RouteUtils.createGenericRouteImpl(link4, link1);
		route17.setDistance(6000);
		leg17.setRoute(route17);
		plan.addLeg(leg17);
		Activity act18 = PopulationUtils.createActivityFromLinkId("car interaction", link1);
		plan.addActivity(act18);
		Leg leg18 = PopulationUtils.createLeg(TransportMode.walk);
		leg18.setRoute(route);
		plan.addLeg(leg18);
		person1modes.put(TransportMode.car, person1modes.get(TransportMode.car) + 1);
		Activity act19 = PopulationUtils.createActivityFromLinkId("home", link1);// main mode car
		plan.addActivity(act19);

		planlegsum = plan.getPlanElements().stream().filter(Leg.class::isInstance)
				.collect(Collectors.summingDouble(l -> {
					Leg leg = (Leg) l;
					return leg.getRoute() != null ? leg.getRoute().getDistance() : 0;
				}));
		planTotalleg = plan.getPlanElements().stream().filter(Leg.class::isInstance).count();
		map.put(person.getId(), plan);

		/* ########Person 2######### --- creating person 2 */
		final Plan plan2 = PopulationUtils
				.createPlan(PopulationUtils.getFactory().createPerson(Id.create("2", Person.class)));
		person2modes.put(TransportMode.walk, 0);
		person2modes.put(TransportMode.car, 0);

		Activity actp2_1 = PopulationUtils.createActivityFromLinkId("home", link1);
		plan2.addActivity(actp2_1);
		Leg legp2_1 = PopulationUtils.createLeg(TransportMode.walk);
		Route route2_1 = RouteUtils.createGenericRouteImpl(link1, link1);
		route2_1.setDistance(100);
		legp2_1.setRoute(route2_1);
		plan2.addLeg(legp2_1);
		Activity actp2_2 = PopulationUtils.createActivityFromLinkId("car interaction", link1);
		plan2.addActivity(actp2_2);
		Leg legp2_2 = PopulationUtils.createLeg(TransportMode.car);
		Route route2_2 = RouteUtils.createGenericRouteImpl(link1, link4);
		route2_2.setDistance(6000);
		legp2_2.setRoute(route2_2);
		plan2.addLeg(legp2_2);
		Activity actp2_3 = PopulationUtils.createActivityFromLinkId("car interaction", link4);
		plan2.addActivity(actp2_3);
		Leg legp2_3 = PopulationUtils.createLeg(TransportMode.walk);
		Route route2_3 = RouteUtils.createGenericRouteImpl(link4, link4);
		route2_3.setDistance(200);
		legp2_3.setRoute(route2_3);
		plan2.addLeg(legp2_3);
		person2modes.put(TransportMode.car, person2modes.get(TransportMode.car) + 1);
		Activity actp2_4 = PopulationUtils.createActivityFromLinkId("work", link4);// main mode car
		plan2.addActivity(actp2_4);
		Leg legp2_4 = PopulationUtils.createLeg(TransportMode.walk);
		Route route2_4 = RouteUtils.createGenericRouteImpl(link4, link4);
		route2_4.setDistance(250);
		legp2_4.setRoute(route2_4);
		plan2.addLeg(legp2_4);
		person2modes.put(TransportMode.walk, person2modes.get(TransportMode.walk) + 1);
		Activity actp2_5 = PopulationUtils.createActivityFromLinkId("leisure", link4);// main mode walk
		plan2.addActivity(actp2_5);
		Leg legp2_5 = PopulationUtils.createLeg(TransportMode.walk);
		legp2_5.setRoute(route2_4);
		plan2.addLeg(legp2_5);
		person2modes.put(TransportMode.walk, person2modes.get(TransportMode.walk) + 1);
		Activity actp2_6 = PopulationUtils.createActivityFromLinkId("work", link4);// main mode walk
		plan2.addActivity(actp2_6);
		Leg legp2_6 = PopulationUtils.createLeg(TransportMode.walk);
		legp2_6.setRoute(route2_3);
		plan2.addLeg(legp2_6);
		Activity actp2_7 = PopulationUtils.createActivityFromLinkId("car interaction", link4);
		plan2.addActivity(actp2_7);
		Leg legp2_7 = PopulationUtils.createLeg(TransportMode.car);
		legp2_7.setRoute(route2_2);
		plan2.addLeg(legp2_7);
		Activity actp2_8 = PopulationUtils.createActivityFromLinkId("car interaction", link1);
		plan2.addActivity(actp2_8);
		Leg legp2_8 = PopulationUtils.createLeg(TransportMode.walk);
		legp2_8.setRoute(route2_1);
		plan2.addLeg(legp2_8);
		person2modes.put(TransportMode.car, person2modes.get(TransportMode.car) + 1);
		Activity actp2_9 = PopulationUtils.createActivityFromLinkId("home", link1);// main mode car
		plan2.addActivity(actp2_9);

		plan2legsum = plan2.getPlanElements().stream().filter(Leg.class::isInstance)
				.collect(Collectors.summingDouble(l -> {
					Leg leg = (Leg) l;
					return leg.getRoute() != null ? leg.getRoute().getDistance() : 0;
				}));
		plan2Totalleg = plan2.getPlanElements().stream().filter(Leg.class::isInstance).count();
		map.put(person2.getId(), plan2);

		/* ########Person 3######### --- creating person 3 */
		final Plan plan3 = PopulationUtils
				.createPlan(PopulationUtils.getFactory().createPerson(Id.create("3", Person.class)));
		person3modes.put(TransportMode.walk, 0);
		person3modes.put(TransportMode.car, 0);

		Activity actp3_1 = PopulationUtils.createActivityFromLinkId("home", link1);
		plan3.addActivity(actp3_1);
		Leg legp3_1 = PopulationUtils.createLeg(TransportMode.walk);
		Route route3_1 = RouteUtils.createGenericRouteImpl(link1, link1);
		route3_1.setDistance(100);
		legp3_1.setRoute(route3_1);
		plan3.addLeg(legp3_1);
		Activity actp3_2 = PopulationUtils.createActivityFromLinkId("car interaction", link1);
		plan3.addActivity(actp3_2);
		Leg legp3_2 = PopulationUtils.createLeg(TransportMode.car);
		Route route3_2 = RouteUtils.createGenericRouteImpl(link1, link5);
		route3_2.setDistance(8000);
		legp3_2.setRoute(route3_2);
		plan3.addLeg(legp3_2);
		Activity actp3_3 = PopulationUtils.createActivityFromLinkId("car interaction", link5);
		plan3.addActivity(actp3_3);
		Leg legp3_3 = PopulationUtils.createLeg(TransportMode.walk);
		Route route3_3 = RouteUtils.createGenericRouteImpl(link5, link5);
		route3_3.setDistance(300);
		legp3_3.setRoute(route3_3);
		plan3.addLeg(legp3_3);
		person3modes.put(TransportMode.car, person3modes.get(TransportMode.car) + 1);
		Activity actp3_4 = PopulationUtils.createActivityFromLinkId("shopping", link5);// main mode car
		plan3.addActivity(actp3_4);
		Leg legp3_4 = PopulationUtils.createLeg(TransportMode.walk);
		legp3_4.setRoute(route3_3);
		plan3.addLeg(legp3_4);
		Activity actp3_5 = PopulationUtils.createActivityFromLinkId("car interaction", link5);
		plan3.addActivity(actp3_5);
		Leg legp3_5 = PopulationUtils.createLeg(TransportMode.car);
		legp3_5.setRoute(route3_2);
		plan3.addLeg(legp3_5);
		Activity actp3_6 = PopulationUtils.createActivityFromLinkId("car interaction", link1);
		plan3.addActivity(actp3_6);
		Leg legp3_6 = PopulationUtils.createLeg(TransportMode.walk);
		legp3_6.setRoute(route3_1);
		plan3.addLeg(legp3_6);
		person3modes.put(TransportMode.car, person3modes.get(TransportMode.car) + 1);
		Activity actp3_8 = PopulationUtils.createActivityFromLinkId("home", link1);// main mode car
		plan3.addActivity(actp3_8);

		plan3legsum = plan3.getPlanElements().stream().filter(Leg.class::isInstance)
				.collect(Collectors.summingDouble(l -> {
					Leg leg = (Leg) l;
					return leg.getRoute() != null ? leg.getRoute().getDistance() : 0;
				}));
		plan3Totalleg = plan3.getPlanElements().stream().filter(Leg.class::isInstance).count();
		plan3.getPlanElements().stream().filter(Leg.class::isInstance).count();
		map.put(person3.getId(), plan3);

		performTest(map, utils.getOutputDirectory() + "/TravelDistanceStat");
	}

	private void performTest(IdMap<Person, Plan> map, String outputDirectory) {

		ControlerConfigGroup controlerConfigGroup = new ControlerConfigGroup();
		OutputDirectoryHierarchy controlerIO = new OutputDirectoryHierarchy(outputDirectory,
				OverwriteFileSetting.overwriteExistingFiles, CompressionType.gzip);
		controlerConfigGroup.setCreateGraphs(true);
		controlerConfigGroup.setFirstIteration(0);
		controlerConfigGroup.setLastIteration(10);
		TravelDistanceStats travelDistanceStats = new TravelDistanceStats(controlerConfigGroup, controlerIO);
		travelDistanceStats.addIteration(0, map);
		readAndValidateValues(0, planlegsum + plan2legsum + plan3legsum, 12,
				planTotalleg + plan2Totalleg + plan3Totalleg);
		map.remove(person2.getId());
		travelDistanceStats.addIteration(1, map);
		readAndValidateValues(1, planlegsum + plan3legsum, 8, planTotalleg + plan3Totalleg);
		map.remove(person3.getId());
		travelDistanceStats.addIteration(2, map);
		readAndValidateValues(2, planlegsum, 6, planTotalleg);
		travelDistanceStats.close();
	}

	private void readAndValidateValues(int itr, Double legSum, int totalTrip, long totalLeg) {

		String file = utils.getOutputDirectory() + "/TravelDistanceStat" + "/traveldistancestats.txt";
		BufferedReader br;
		String line;
		try {
			br = new BufferedReader(new FileReader(file));
			String firstRow = br.readLine();
			String[] columnNames = firstRow.split("	");
			decideColumns(columnNames);
			int iteration = 0;
			while ((line = br.readLine()) != null) {
				if (iteration == itr) {
					String[] column = line.split("	");
					// checking if column number in greater than 0, because 0th column is always
					// 'Iteration' and we don't need that --> see decideColumns() method
					Double avgLegvalue = (avglegdis > 0) ? Double.valueOf(column[avglegdis]) : 0;
					Double avgTripvalue = (avgtripdis > 0) ? Double.valueOf(column[avgtripdis]) : 0;

					Assert.assertEquals("avg. Average Trip distance does not match", (legSum / totalTrip), avgTripvalue,
							0);
					Assert.assertEquals("avg. Average Leg distance does not match", (legSum / totalLeg), avgLegvalue,
							0);

					break;
				}
				iteration++;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void decideColumns(String[] columnNames) {

		Integer i = 0;
		while (i < columnNames.length) {
			String name = columnNames[i];
			switch (name) {

			case "avg. Average Leg distance":
				avglegdis = i;
				break;

			case "avg. Average Trip distance":
				avgtripdis = i;
				break;

			}
			i++;
		}
	}
}
