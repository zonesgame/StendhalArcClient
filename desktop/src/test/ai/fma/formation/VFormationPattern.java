package test.ai.fma.formation;

import arc.ai.fma.FormationPattern;
import arc.ai.utils.Location;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import z.ai.utils.Vector2Utils;

public class VFormationPattern implements FormationPattern<Vec2> {

	private int numberOfSlots;
	private float memberRadius;
	private float angle;

	private Vec2 side1 = new Vec2();
	private Vec2 side2 = new Vec2();

	public VFormationPattern(float angle, float memberRadius) {
		this.memberRadius = memberRadius;
		setAngle(angle);
	}

	public float getMemberRadius() {
		return memberRadius;
	}

	public void setMemberRadius(float memberRadius) {
		this.memberRadius = memberRadius;
	}

	public float getAngle() {
		return angle;
	}

	public void setAngle(float angle) {
		this.angle = angle;
		Vector2Utils.angleToVector(side1, angle / 2 + 90 * Mathf.degreesToRadians);
		Vector2Utils.angleToVector(side2, -angle / 2 + 90 * Mathf.degreesToRadians);
	}

	@Override
	public boolean supportsSlots(int slotCount) {
		return true;
	}

	@Override
	public void setNumberOfSlots(int numberOfSlots) {
		this.numberOfSlots = numberOfSlots;
	}

	@Override
	public Location<Vec2> calculateSlotLocation(
			Location<Vec2> outLocation, int slotNumber) {
		Vec2 side = ((slotNumber + 1) % 2) == 0 ? side1 : side2;
		float radius = ((slotNumber + 1) / 2) * (memberRadius + memberRadius);
		outLocation.getPosition().set(side).scl(radius);
		outLocation.setOrientation(0);
		return outLocation;
	}

}
