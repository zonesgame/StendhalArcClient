package test.ai.fma.formation;

import arc.ai.fma.FormationPattern;
import arc.ai.utils.Location;
import arc.math.geom.Vec2;

public class SquareFormationPattern implements FormationPattern<Vec2> {

	private int numberOfSlots;
	private float memberRadius;
	private int columns;
	
	public SquareFormationPattern (float memberRadius) {
		this.memberRadius = memberRadius;
	}
	
	@Override
	public void setNumberOfSlots (int numberOfSlots) {
		this.numberOfSlots = numberOfSlots;
		this.columns = (int)Math.sqrt(numberOfSlots);
	}

	@Override
	public Location<Vec2> calculateSlotLocation (Location<Vec2> outLocation, int slotNumber) {
		int x = slotNumber / columns;
		int y = slotNumber % columns;
		float memberDiameter = memberRadius + memberRadius;
		float offset = memberRadius * (columns - 1);
		outLocation.getPosition().set(x * memberDiameter - offset, y * memberDiameter - offset);
		outLocation.setOrientation(0);
		return outLocation;
	}

	@Override
	public boolean supportsSlots (int slotCount) {
		return true;
	}

}
