package z.ai.formation;

import arc.ai.fma.FormationPattern;
import arc.ai.utils.Location;
import arc.math.geom.Vec2;

/**
 *  线队列 模式
 * */
public class LineFormationPattern implements FormationPattern<Vec2> {

	/** 成员数量*/
	private int numberOfSlots;
	/** 成员半径*/
	private float memberRadius;

	public LineFormationPattern(float memberRadius) {
		this.memberRadius = memberRadius;
	}

	/** 设置最大成员数量*/
	@Override
	public void setNumberOfSlots(int numberOfSlots) {
		this.numberOfSlots = numberOfSlots;
	}

	/** 计算指定槽位置*/
	@Override
	public Location<Vec2> calculateSlotLocation(Location<Vec2> outLocation, int slotNumber) {
		float offset = memberRadius * (numberOfSlots - 1);
		outLocation.getPosition().set(0, slotNumber * (memberRadius + memberRadius) - offset);
		outLocation.setOrientation(0);
		return outLocation;
	}

	/** 指定槽数量是否支持*/
	@Override
	public boolean supportsSlots(int slotCount) {
		return true;
	}

}
