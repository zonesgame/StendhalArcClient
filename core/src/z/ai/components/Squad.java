
package z.ai.components;

import arc.ai.fma.Formation;
import arc.ai.fma.FormationMotionModerator;
import arc.ai.fma.FormationPattern;
import arc.ai.fma.SoftRoleSlotAssignmentStrategy;
import arc.ai.utils.Location;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.Array;
import arc.z.util.ISOUtils;
import mindustry.entities.type.BaseUnit;
import mindustry.gen.Tex;
import z.ai.formation.CircleFormationPattern;
import z.ai.formation.ColumnFormationPattern;
import z.ai.formation.DistanceSlotCostProvider;
import z.ai.formation.LineFormationPattern;
import z.ai.formation.SquareFormationPattern;
import z.ai.formation.VFormationPattern;
import z.ai.formation.WedgeFormationPattern;
import z.ai.utils.Constants;
import z.ai.utils.Location2;
import z.ai.utils.SquadMember;
import z.entities.type.ai.SquadBlackboard;
import z.utils.FinalCons;

import static mindustry.Vars.tilesize;

/**
 *  队伍管理器
 * */
public class Squad<T extends BaseUnit> {	// extends Component implements Poolable {

	/**
	 *  队列类型
	 * */
	public enum FormationPatternType {
		Line, Column, Square, Ring, V, Wedge;
	}

	/** 单位填充距离*/
	private float PATTERN_SPACING = Constants.unitRadius * 0.5f / tilesize;
	/** 当前队列类型*/
	private FormationPatternType DEFAULT_PATTERN = FormationPatternType.Square;

	// 队伍成员数组. Array for formation API and ashley entity reference
	private Array<T> members = new Array<T>();
	/** 指挥官*/
	private T commander;

	// Formation
	private Formation<Vec2> formation;
	private FormationMotionModerator<Vec2> moderator;
	private FormationPattern<Vec2>[] formationPatterns = new FormationPattern[FormationPatternType.values().length];

	/** 当前队列锚点*/
//	private Steerable<Vec2> steerable;
	private Location2 location = new Location2();

	@Deprecated
	private int teamID;
	/** 队伍索引, 用于路径数据使用*/
	private int id;
	private int maxMember = FinalCons.max_member_count;
	private final Array<Integer> membersID = new Array<Integer>(FinalCons.max_member_count);

	// renderer begon
	/** 队伍是否选择状态*/
	public boolean isSelect = false;
	/** 队伍是否已使用(非缓存池中无效对象)*/
	private boolean isValid = false;
	/***/
	// renderer end

	// 自主ai扩展数据begon
	private SquadBlackboard blackboard = new SquadBlackboard();
	// 自主ai扩展数据end

	public Squad() {
		this(-1, 0);
	}

	/** Can only be created by PooledEngine */
	public Squad (int teadid, int squadid) {
		{	// debuge
			this.teamID = teadid;
			this.id = squadid;
			for (int i = maxMember; --i >= 0; ) {
				membersID.add(i);
			}
		}
		init(location);
		// private constructor
	}

	private void init (Location location) {
		formation = new Formation<Vec2>(location, getFormationPattern(DEFAULT_PATTERN), null);
		SoftRoleSlotAssignmentStrategy<Vec2> slotAssignmentStrategy = new SoftRoleSlotAssignmentStrategy<Vec2>(new DistanceSlotCostProvider(formation));
		formation.setSlotAssignmentStrategy(slotAssignmentStrategy);

		for (FormationPatternType _type : FormationPatternType.values()) {
			formationPatterns[_type.ordinal()] = getFormationPattern(_type);
		}
	}

	private void update() {
		formation.updateSlots();
		// 更新路径算法目标点
//		for (T entity : members) {
//			entity.getFormationTarget().setPathTarget(entity.getTeam().id);
//		}
	}


	/** 添加队伍成员*/
	public void addMember (T entity) {
		if (members.size >= maxMember) return;	// full array
		if (members.size == 0) {
			location.setPosition(entity.getX(), entity.getY());
		}

		members.add(entity);
		formation.addMember(entity.squadMember);
//		update();

		int nextid = membersID.pop();
		entity.squadMember.init(this.teamID, this.id, nextid);

		// 更新位置和目标点
		updateTarget();
		// 设置指挥官
		updateCommander();
	}

	/** 移除队伍成员*/
	public void removeMember (T entity) {
		members.remove(entity, true);
		formation.removeMember(entity.squadMember);
		membersID.add(entity.squadMember.memberID);// 回收成员id

//		update();
		// 跟新队伍目标点
		updateTarget();
		// 设置指挥官
		updateCommander();
	}

	/** 清空队伍成员*/
	public void clearMember () {
		for (T member : members) {
			formation.removeMember(member.squadMember);
			membersID.add(member.squadMember.memberID);// 回收成员id

			member.kill();	// 逻辑更新层移除对象
//			member.remove();
		}
		members.clear();

//		update();
		// 更新队伍目标点
		updateTarget();
		updateCommander();
	}

	/** 设置队伍队列类型*/
	public void setFormationPattern (FormationPatternType pattern) {
		FormationPattern<Vec2> formationPattern = formationPatterns[pattern.ordinal()];
		formation.changePattern(formationPattern);

		update();
	}

	public Array<T> getMembers() {
		return members;
	}

	/** 设置队伍锚点和方向*/
	public void setTarget(float tx, float ty, float angle) {
		location.setOrientation(angle);
		location.setPosition(tx, ty);

		updateTarget();
	}

	/** 设置队伍锚点*/
	public void setTarget(float tx, float ty) {
		location.setPosition(tx, ty);

		updateTarget();
	}

	private void updateTarget() {
		update();
		for (T entity : members) {
			entity.squadMember.setPathTarget(entity.getTeam().id);	// 更新路径算法位置, 开启成员单位移动状态
		}
	}

	private void updateCommander() {
		if (commander != null)
			commander.squadMember.isCommander = false;

		if (members.size > 0) {
			commander = members.first();
			commander.squadMember.isCommander = true;
		}
	}

	public Location2 getTarget() {
		return location;
	}

	/** 设置队伍选择状态*/
	public void selectState(boolean isSelect) {
	    this.isSelect = isSelect;
//		for (T unit : members) {
//			((BaseGroundUnit) unit).selectState = isSelect;
//		}
	}

	public void setValid(boolean isValid) {
		if ( !isValid)
			isSelect = false;
		this.isValid = isValid;
	}

	public boolean valid() {
		return isValid;
	}

	public void doubleClick() {
//	    System.out.println("doubleClick squad......");
	}

	private FormationPattern<Vec2> getFormationPattern (FormationPatternType pattern) {
		switch (pattern) {
			case Line:
				return new LineFormationPattern(Constants.unitRadius + PATTERN_SPACING);
			case Column:
				return new ColumnFormationPattern(Constants.unitRadius + PATTERN_SPACING);
			case Square:
				return new SquareFormationPattern(Constants.unitRadius + PATTERN_SPACING);
			default:
			case Ring:
//			return new OffensiveCircleFormationPattern<Vec2>(Constants.unitRadius + PATTERN_SPACING);
				return new CircleFormationPattern(Constants.unitRadius + PATTERN_SPACING);
			case V:
				return new VFormationPattern(60 * Mathf.degreesToRadians, Constants.unitRadius + PATTERN_SPACING);
			case Wedge:
				return new WedgeFormationPattern(Constants.unitRadius + PATTERN_SPACING);
		}
	}



	// 绘制层临时代码
	public void draw() {
	    if ( !valid())  return;

		{	// 队伍目标点绘制
			for (BaseUnit unit : getMembers()) {
				SquadMember member = unit.squadMember;

				Draw.color(Color.red);
				Vec2 vpos = ISOUtils.tileToWorldCoords(member.getTargetLocation().getPosition());
//                        vpos.set(member.getTargetLocation().getPosition());
				Lines.circle(vpos.x, vpos.y, 4);
				Draw.color();
			}
		}

        if (isSelect) {	//  队伍成员选择状态图标绘制
            for (T unit : members) {
                float x = unit.wpos.x;
                float y = unit.wpos.y;
//            Draw.color(Color.darkGray);
//            Fill.rect(x, y, drawSize() * Draw.scl * 0.7f, 8 * Draw.scl);
                Draw.color(unit.squadMember.isCommander ? Color.cyan : Color.blue, 1f);		// 设置指挥官和队员图标颜色
                Fill.poly(x, y +  (unit.type.hitsize) * tilesize  * 1.3f + 0.75f + 2 + 1.5f, 3, unit.type.hitsize * tilesize * 0.36f, -90);
                Draw.color();
            }

//            if (commander != null) {    // 绘制指挥官选择状态图标
//                float x = commander.wpos.x;
//                float y = commander.wpos.y;
//                Draw.color(Color.cyan, 0.75f);
//                Fill.poly(x, y +  (commander.type.hitsize) * tilesize  * 1.3f + 0.75f + 2 + 1.5f, 3, commander.type.hitsize * tilesize * 0.36f - 4, -90);
//                Draw.color();
//            }
        }

        // 队伍集合位置debug绘制
		{
			Draw.color(Color.red, 0.25f);
			Vec2 vpos = ISOUtils.tileToWorldCoords(getTarget().getX(), getTarget().getY());
//                    vpos.set(squad.getTarget().getX(), squad.getTarget().getY());
			Draw.rect(Tex.whiteui.getRegion(), vpos.x, vpos.y, 16, 32);
			Draw.color();
		}
	}
}
