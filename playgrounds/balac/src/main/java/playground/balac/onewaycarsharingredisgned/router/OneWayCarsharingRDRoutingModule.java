package playground.balac.onewaycarsharingredisgned.router;


/**
 * @author balacm
 * 
 **/


import java.util.ArrayList;
import java.util.List;

import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.api.experimental.facilities.Facility;
import org.matsim.core.population.LegImpl;
import org.matsim.core.population.routes.GenericRouteImpl;
import org.matsim.core.population.routes.LinkNetworkRouteImpl;
import org.matsim.core.router.EmptyStageActivityTypes;
import org.matsim.core.router.RoutingModule;
import org.matsim.core.router.StageActivityTypes;

public class OneWayCarsharingRDRoutingModule implements RoutingModule{
	public OneWayCarsharingRDRoutingModule() {		
		
	}
	@Override
	public List<? extends PlanElement> calcRoute(Facility fromFacility,
			Facility toFacility, double departureTime, Person person) {
		
		final List<PlanElement> trip = new ArrayList<PlanElement>();
		final Leg leg = new LegImpl( "walk_ow_sb" );
		GenericRouteImpl route = new GenericRouteImpl(fromFacility.getLinkId(), toFacility.getLinkId());
		leg.setRoute(route);
		trip.add( leg );
		
		final Leg leg1 = new LegImpl( "onewaycarsharing" );
		LinkNetworkRouteImpl route1 = new LinkNetworkRouteImpl(fromFacility.getLinkId(), toFacility.getLinkId());
		leg1.setRoute(route1);
		trip.add( leg1 );	
		
		final Leg leg2 = new LegImpl( "walk_ow_sb" );
		GenericRouteImpl route2 = new GenericRouteImpl(fromFacility.getLinkId(), toFacility.getLinkId());
		leg2.setRoute(route2);
		trip.add( leg2 );
		
		return trip;
	}

	@Override
	public StageActivityTypes getStageActivityTypes() {
		
		return EmptyStageActivityTypes.INSTANCE;
	}
}
