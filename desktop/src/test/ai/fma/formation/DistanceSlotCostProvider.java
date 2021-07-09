package test.ai.fma.formation;

import arc.ai.fma.Formation;
import arc.ai.fma.FormationMember;
import arc.ai.fma.SoftRoleSlotAssignmentStrategy;
import arc.math.geom.Vec2;

public class DistanceSlotCostProvider implements SoftRoleSlotAssignmentStrategy.SlotCostProvider<Vec2> {

	private final Formation formation;

	public DistanceSlotCostProvider(Formation format) {
		this.formation = format;
	}
	
	@Override
	public float getCost(FormationMember<Vec2> member, int slotNumber) {
//		UnitComponent unitComp = (UnitComponent) member;
//		SquadComponent squadComp = Components.SQUAD.get(unitComp.getSquad());
//
//		Vector2 targetPosition = squadComp.formation.getSlotAssignmentAt(slotNumber).member.getTargetLocation().getPosition();
//
//		// The cost is the square distance between current position and target position
//		return unitComp.getBody().getPosition().dst2(targetPosition);

		Vec2 targetPosition = (Vec2) formation.getSlotAssignmentAt(slotNumber).member.getTargetLocation().getPosition();
		return member.getTargetLocation().getPosition().dst2(targetPosition);
	}
}
