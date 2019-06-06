package vwExamples.utils.customEV;

import org.matsim.contrib.ev.charging.ChargingStrategy;
import org.matsim.contrib.ev.fleet.Battery;
import org.matsim.contrib.ev.fleet.ElectricVehicle;

public class CustomFastThenSlowCharging implements ChargingStrategy {

	private final double chargingPower;
	private final double maxRelativeSoc;

	public CustomFastThenSlowCharging(double chargingPower,double maxRelativeSoc) {
		if (chargingPower <= 0) {
			throw new IllegalArgumentException("chargingPower must be positive");
		}
		
		if (maxRelativeSoc <= 0 || maxRelativeSoc > 1) {
			throw new IllegalArgumentException("maxRelativeSoc must be in (0,1]");
		}
		this.chargingPower = chargingPower;
		this.maxRelativeSoc = maxRelativeSoc;
	}

	@Override
	public double calcChargingPower(ElectricVehicle ev) {
		Battery b = ev.getBattery();
		double relativeSoc = b.getSoc() / b.getCapacity();
		double c = b.getCapacity() / 3600;
		if (relativeSoc <= 0.5) {
			return Math.min(chargingPower, 1.75 * c);
		} else if (relativeSoc <= 0.75) {
			return Math.min(chargingPower, 1.25 * c);
		} else {
			return Math.min(chargingPower, 0.5 * c);
		}
	}

	@Override
	public double calcRemainingEnergyToCharge(ElectricVehicle ev) {
		Battery b = ev.getBattery();
		return maxRelativeSoc * b.getCapacity() - b.getSoc();
	}

	@Override
	public double calcRemainingTimeToCharge(ElectricVehicle ev) {
		return calcRemainingEnergyToCharge(ev) / chargingPower;//TODO should consider variable charging speed
	}
}
