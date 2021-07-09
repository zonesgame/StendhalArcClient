package z.ai.formation;

import arc.ai.fma.FormationPattern;
import arc.ai.utils.Location;
import arc.math.geom.Vec2;

/**
 *  正方形队列 模式
 * */
public class SquareFormationPattern implements FormationPattern<Vec2> {

	/** 成员数量*/
	private int numberOfSlots;
	/** 成员距离半径*/
	private float memberRadius;
	/** 列数量*/
	private int columns;
	
	public SquareFormationPattern(float memberRadius) {
		this.memberRadius = memberRadius;
	}

	/** 设置最大成员数量*/
	@Override
	public void setNumberOfSlots (int numberOfSlots) {
		this.numberOfSlots = numberOfSlots;
		this.columns = (int)Math.sqrt(numberOfSlots);
	}

	/** 指定槽位置*/
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

	/** 指定槽数量是否支持*/
	@Override
	public boolean supportsSlots (int slotCount) {
		return true;
	}

}
