package z.ai.formation;

import arc.ai.fma.FormationPattern;
import arc.ai.utils.Location;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import z.ai.utils.Vector2Utils;

/**
 *  "V"型队列 模式
 * */
public class VFormationPattern implements FormationPattern<Vec2> {

	/** 成员槽数量*/
	private int numberOfSlots;
	/** 成员半径*/
	private float memberRadius;
	/** 队列角度*/
	private float angle;

	/** 第一面向量*/
	private Vec2 side1 = new Vec2();
	/** 第二面向量*/
	private Vec2 side2 = new Vec2();

	public VFormationPattern(float angle, float memberRadius) {
		this.memberRadius = memberRadius;
		setAngle(angle);
	}

	/** 成员半径*/
	public float getMemberRadius() {
		return memberRadius;
	}

	/** 设置成员半径*/
	public void setMemberRadius(float memberRadius) {
		this.memberRadius = memberRadius;
	}

	/** 队列角度*/
	public float getAngle() {
		return angle;
	}

	/** 设置队列角度*/
	public void setAngle(float angle) {
		this.angle = angle;
		Vector2Utils.angleToVector(side1, angle / 2 + 90 * Mathf.degreesToRadians);
		Vector2Utils.angleToVector(side2, -angle / 2 + 90 * Mathf.degreesToRadians);
	}

	/** 指定槽数量是否支持*/
	@Override
	public boolean supportsSlots(int slotCount) {
		return true;
	}

	/** 设置成员数量*/
	@Override
	public void setNumberOfSlots(int numberOfSlots) {
		this.numberOfSlots = numberOfSlots;
	}

	/** 计算指定槽位置*/
	@Override
	public Location<Vec2> calculateSlotLocation(Location<Vec2> outLocation, int slotNumber) {
		Vec2 side = ((slotNumber + 1) % 2) == 0 ? side1 : side2;
		float radius = ((slotNumber + 1) / 2) * (memberRadius + memberRadius);
		outLocation.getPosition().set(side).scl(radius);
		outLocation.setOrientation(0);
		return outLocation;
	}

}
