package test.ai.fma.formation;

import arc.ai.fma.FormationPattern;
import arc.ai.utils.Location;
import arc.math.geom.Vec2;

public class WedgeFormationPattern implements FormationPattern<Vec2> {

	private int numberOfSlots;
	private float memberRadius;

	public WedgeFormationPattern(float memberRadius) {
		this.memberRadius = memberRadius;
	}

	@Override
	public void setNumberOfSlots(int numberOfSlots) {
		this.numberOfSlots = numberOfSlots;
	}

	@Override
	public Location<Vec2> calculateSlotLocation(
			Location<Vec2> outLocation, int slotNumber) {
		int row = calculateRow(slotNumber);
		float col = calculateColumn(slotNumber, row);
		float memberDiameter = memberRadius + memberRadius;
		outLocation.getPosition().set(-row * memberDiameter, -col * memberDiameter);
		outLocation.setOrientation(0);
		return outLocation;
	}

	@Override
	public boolean supportsSlots(int slotCount) {
		return true;
	}

	private int calculateRow(int slotNumber) {
		double r = (Math.sqrt(1 + 8 * (slotNumber+1)) - 1) * .5;
		return (int)Math.ceil(r) - 1;
	}
	
	private float calculateColumn(int slotNumber, int row) {
		int r = row * (row + 1) / 2;
		return slotNumber - r - row * .5f;
	}

}
