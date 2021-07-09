package z.ai.formation;

import arc.ai.fma.FormationPattern;
import arc.ai.utils.Location;
import arc.math.geom.Vec2;

/**
 *  楔形队列 模式
 * */
public class WedgeFormationPattern implements FormationPattern<Vec2> {

	/** 成员数量*/
	private int numberOfSlots;
	/** 成员半径*/
	private float memberRadius;

	public WedgeFormationPattern(float memberRadius) {
		this.memberRadius = memberRadius;
	}

	/** 设置成员数量*/
	@Override
	public void setNumberOfSlots(int numberOfSlots) {
		this.numberOfSlots = numberOfSlots;
	}

	/** 计算指定槽位置*/
	@Override
	public Location<Vec2> calculateSlotLocation(Location<Vec2> outLocation, int slotNumber) {
		int row = calculateRow(slotNumber);
		float col = calculateColumn(slotNumber, row);
		float memberDiameter = memberRadius + memberRadius;
		outLocation.getPosition().set(-row * memberDiameter, -col * memberDiameter);
		outLocation.setOrientation(0);
		return outLocation;
	}

	/** 指定槽数量是否支持*/
	@Override
	public boolean supportsSlots(int slotCount) {
		return true;
	}

	/** 计算指定槽所在行*/
	private int calculateRow(int slotNumber) {
		double r = (Math.sqrt(1 + 8 * (slotNumber+1)) - 1) * .5;
		return (int)Math.ceil(r) - 1;
	}

	/** 计算指定槽所在列*/
	private float calculateColumn(int slotNumber, int row) {
		int r = row * (row + 1) / 2;
		return slotNumber - r - row * .5f;
	}

}
