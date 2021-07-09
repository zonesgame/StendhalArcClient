package test.ai.fma.formation;

import arc.ai.fma.FormationPattern;
import arc.ai.utils.Location;
import arc.math.geom.Vec2;

public class ColumnFormationPattern implements FormationPattern<Vec2> {

	private int numberOfSlots;
	private float memberRadius;

	public ColumnFormationPattern(float memberRadius) {
		this.memberRadius = memberRadius;
	}

	@Override
	public void setNumberOfSlots(int numberOfSlots) {
		this.numberOfSlots = numberOfSlots;
	}

	@Override
	public Location<Vec2> calculateSlotLocation(Location<Vec2> outLocation, int slotNumber) {
		outLocation.getPosition().set(- slotNumber * (memberRadius + memberRadius), 0);
		outLocation.setOrientation(0);
		return outLocation;
	}

	@Override
	public boolean supportsSlots(int slotCount) {
		return true;
	}

}
