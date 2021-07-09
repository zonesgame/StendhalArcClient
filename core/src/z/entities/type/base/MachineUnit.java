package z.entities.type.base;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.util.Time;
import arc.z.util.ISOUtils;
import arc.z.util.ZonesAnnotate;
import mindustry.Vars;
import mindustry.ai.Pathfinder.PathTarget;
import mindustry.core.Renderer;
import mindustry.entities.Predict;
import mindustry.entities.Units;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.type.BaseUnit;
import mindustry.entities.type.TileEntity;
import mindustry.entities.units.UnitCommand;
import mindustry.entities.units.UnitState;
import mindustry.game.Team;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import mindustry.world.Tile;
import mindustry.world.blocks.Floor;
import mindustry.world.meta.BlockFlag;
import z.entities.type.ai.state.AttackSLG1;
import z.entities.type.ai.state.GlobalSLG1;
import z.entities.type.ai.state.IdleSLG1;
import z.entities.type.ai.state.MoveSLG1;

import static mindustry.Vars.pathfinder;
import static mindustry.Vars.player;
import static mindustry.Vars.world;
import static z.debug.ZDebug.enable_isoInput;

/**
 *  地面单位实体
 * */
public class MachineUnit extends BaseUnit{
    // ai use function begon
    public enum TeamStrategy {
        /** damage触发转换为Atack状态*/
//        S_IDEL,
        /** damage, <attack distance, 转换Attack状态*/
        S_ATTACK,
        /** 仅执行Move状态, 如果状态faild切换到Idle状态(当前并入了Idle状态)*/
        S_RETREAT,
    }

    /**默认队伍策略*/
    public TeamStrategy defStrategy = TeamStrategy.S_ATTACK;

    public static UnitState globalSLG1 = new GlobalSLG1();
    public static UnitState idleSLG1 = new IdleSLG1();
    public static UnitState moveSLG1 = new MoveSLG1();
    public static UnitState attackSLG1 = new AttackSLG1();

//    public TeamStrategy getStrategy() {
//        return curStrategy;
//    }

//    public void setStrategy(TeamStrategy strategy) {
//        if (curStrategy != strategy) {
//            preStrategy = curStrategy;
//            curStrategy = strategy;
//        }
//    }

//    public void setPreStrategy() {
//        if (preStrategy != null) {
//            curStrategy = preStrategy;
//            preStrategy = null;
//        }
//    }
    // ai use function end
    // test begon
    public boolean selectState = false;

    /** 起始状态*/
    @Deprecated
    public UnitState startState;
    /** ai通用数据*/
//    public SquadBlackboard blackboard;

//    public UnitState squadState, attackCustom, /** 任务简单意识状态*/machineState;

    public MachineUnit() {
    }

    @Override
    public void init(UnitType type, Team team) {
        super.init(type, team);

//        this.squadState = new SquadState(this);
//        this.attackCustom = new AttackCustomState(this);
//        this.machineState = new MachineState(this);

        if (team.isEnemy(player.getTeam())) { // debug code
            this.startState = idleSLG1;
        }
    }

    @ZonesAnnotate.ZAdd
    @Override
    public void setState(String stateKey) {

    }

    /** 单位移动到团队指定位置*/
    public void moveTo(int squadID, int memberID){
        Tile tile = world.tile(x, y);
        if(tile == null) return;
        Position targetTile = pathfinder.getTargetMember(tile, team, squadID, memberID);
//        Tile targetTile = pathfinder.getTargetTile(tile, team, PathTarget.enemyCores);
//        Position targetTile = pathfinder.getTargetTile(tile, team, PathTarget.enemyCores);

        if(tile.equalsPos(targetTile)) return;

//        velocity.add(vec.trns(FinalCons.angleTo(this.getX(), this.getY(), targetTile), type.speed * Time.delta()));
//        if(Units.invalidateTarget(target, this)){
//            rotation = Mathf.slerpDelta(rotation, baseRotation, type.rotatespeed);
//        }

        velocity.add(vec.trns(angleTo(targetTile), type.speed * Time.delta()));
        if(Units.invalidateTarget(target, this)){
            rotation = Mathf.slerpDelta(rotation, baseRotation, type.rotatespeed);
        }

        if (true) {
            Renderer.addDebugDraw((nulltile) -> {
                Draw.color(Color.yellow);
                Vec2 vpos = ISOUtils.tileToWorldCoords(targetTile.getX(), targetTile.getY());
                Lines.poly(vpos.x, vpos.y, 5, 4);
                Draw.color();
            }, Float.MIN_VALUE);        // minValue execute onece
        }
    }
    // test end

    protected static Vec2 vec = new Vec2();

    /** 行走时间*/
    protected float walkTime;
    /** 被动时间*/
    protected float stuckTime;
    /** 基础角度*/
    protected float baseRotation;

    /** 单位状态机状态*/
    public final UnitState

//            /** 编队起始ai*/
//            squadStartAI = new UnitState() {
//        public void entered() {
//            target = null;
//        }
//
//        public void update() {
//            TileEntity core = getClosestEnemyCore();
//
//            if (type.weapon.getRecoil(BaseGroundUnit.this, false) != 0) {       // attack中禁止移动
////                continue;
//            }
//            else if (core == null) {    // player创建单位ai
//                Tile closestSpawn = getClosestSpawner();
//                if (closestSpawn == null || !withinDst(closestSpawn, Vars.state.rules.dropZoneRadius + 85f)) {
//                    moveToCore(PathTarget.enemyCores);
//                }
//            } else {    // 回合spawn单位ai
//                float dst = dst(core);
//
//                if (dst < getWeapon().bullet.range() / 1.1f) {
//                    target = core;
//                }
//
//                if (dst > getWeapon().bullet.range() * 0.5f) {
//                    moveToCore(PathTarget.enemyCores);
//                }
//            }
//        }
//    },

            /**AI Squad状态测试*/
//            testSquad = new UnitState() {
//        @Override
//        public void entered(){
//            target = null;          // zones add
//        }
//
//        @Override
//        public void exited() {
//        }
//
//        @Override
//        public void update(){
//            TileEntity core = getClosestEnemyCore();
//
//            if (type.weapon.getRecoil(BaseGroundUnit.this, false) != 0) {       // attack中禁止移动
////                continue;
//            }
//            else if (core == null) {    // player创建单位ai
////                Tile closestSpawn = getClosestSpawner();
////                if (closestSpawn == null || !withinDst(closestSpawn, Vars.state.rules.dropZoneRadius + 85f)) {
////                    moveToCore(PathTarget.enemyCores);
////                }
//                Vec2 target = memberSquad.getPosition();
//
//                if (memberSquad.updateLocation() && dst(target) > 4f / 4f / tilesize) {
//                    moveTo(memberSquad.squadID, memberSquad.memberID);
//                    if (true) {
////                        Time.run(30, ()->{
//                            if (velocity.len() < 0.05f / tilesize) {
////                                setAniState(SGGroundUnit.State.IDLE);
//                                memberSquad.updateLocationDelta();
//                            }
////                        });
//                    }
//                } else {
////                    setAniState(SGGroundUnit.State.IDLE);
//                    memberSquad.setUpdateLocation(false);
//                }
//            } else {    // 回合spawn单位ai
//                float dst = dst(core);
//
//                if (dst < getWeapon().bullet.range() / 1.1f) {
//                    target = core;
//                }
//
//                if (dst > getWeapon().bullet.range() * 0.5f) {
//                    moveToCore(PathTarget.enemyCores);
//                }
//            }
//
////            if(stateSprite != SGGroundUnit.State.ATTACK ){
////                if (memberSquad.updateLocation && dst(target) > 4f / 2f) {
////                    moveTo(memberSquad.squadID, memberSquad.memberID);
////                    {
////                        Time.run(30, ()->{
////                            if (velocity.len() < 0.1f / tilesize) {
////                                setAniState(SGGroundUnit.State.IDLE);
////                                memberSquad.updateLocation = false;
////                            }
////                        });
////                    }
////                } else {
////                    setAniState(SGGroundUnit.State.IDLE);
////                    memberSquad.updateLocation = false;
////                }
////            }
////
////            if (velocity.len() > 0.1f / tilesize && stateSprite != SGGroundUnit.State.ATTACK) {
////                setAniState(SGGroundUnit.State.WALK);
////            }
//        }
//    },

//            attackCustom = new UnitState() {
//        public void entered() {
//            target = null;
//        }
//
//        public void update() {
//            TileEntity core = getClosestEnemyCore();
//
//            if (type.weapon.getRecoil(BaseGroundUnit.this, false) != 0) {       // attack中禁止移动
////                continue;
//            }
//            else if (core == null) {    // player创建单位ai
//                Tile closestSpawn = getClosestSpawner();
//                if (closestSpawn == null || !withinDst(closestSpawn, Vars.state.rules.dropZoneRadius + 85f)) {
//                    moveToCore(PathTarget.enemyCores);
//                }
//            } else {    // 回合spawn单位ai
//                float dst = dst(core);
//
//                if (dst < getWeapon().bullet.range() / 1.1f) {
//                    target = core;
//                }
//
//                if (dst > getWeapon().bullet.range() * 0.5f) {
//                    moveToCore(PathTarget.enemyCores);
//                }
//            }
//        }
//    },

            attack = new UnitState<MachineUnit>(){
                @Override
        public void enter(MachineUnit unit){
            target = null;
        }

        @Override
        public void update(MachineUnit unit){
            TileEntity core = getClosestEnemyCore();

            if(core == null){
                Tile closestSpawn = getClosestSpawner();
                if(closestSpawn == null || !withinDst(closestSpawn, Vars.state.rules.dropZoneRadius + 85f)){
                    moveToCore(PathTarget.enemyCores);
                }
            }else{
                float dst = dst(core);

                if(dst < getWeapon().bullet.range() / 1.1f){
                    target = core;
                }

                if(dst > getWeapon().bullet.range() * 0.5f){
                    moveToCore(PathTarget.enemyCores);
                }
            }
        }
    },
            rally = new UnitState<MachineUnit>(){
                @Override
                public void update(MachineUnit u){
                    Tile target = getClosest(BlockFlag.rally);

                    if(target != null && dst(target) > 80f){
                        moveToCore(PathTarget.rallyPoints);
                    }
                }
            },
            retreat = new UnitState<MachineUnit>(){
                @Override
                public void enter(MachineUnit u){
                    target = null;
                }

                @Override
                public void update(MachineUnit u){
                    moveAwayFromCore();
                }
            };

    @Override
    public void onCommand(UnitCommand command){
        state.changeState(command == UnitCommand.retreat ? retreat :
                command == UnitCommand.attack ? attack :
                        command == UnitCommand.rally ? rally :
                                null);
    }

    @Override
    public void interpolate(){
        super.interpolate();

        if(interpolator.values.length > 1){
            baseRotation = interpolator.values[1];
        }
    }

    @Override
    public void move(float x, float y){
        float dst = Mathf.dst(x, y);
        if(dst > 0.01f){
            baseRotation = Mathf.slerp(baseRotation, Mathf.angle(x, y), type.baseRotateSpeed * (dst / type.speed));
        }
        super.move(x, y);
    }

    @Override
    public UnitState getStartState(){
        return startState == null ? idleSLG1 : startState;      // default attack     attackCustom
    }

    @Override
    @ZonesAnnotate.ZAdd
    public UnitState getGlobalState() {
        return globalSLG1;
    }

    @Override
    public void update(){
        super.update();

        stuckTime = !vec.set(x, y).sub(lastPosition()).isZero(0.0001f) ? 0f : stuckTime + Time.delta();

        if(!velocity.isZero()){
            baseRotation = Mathf.slerpDelta(baseRotation, velocity.angle(), 0.05f);
        }

        if(stuckTime < 1f){
            walkTime += Time.delta();
        } else {
            walkTime = 0;
        }
    }

    @Override
    public Weapon getWeapon(){
        return type.weapon;
    }

    @Override
    public void draw(){
        //zones add begon
        float x = this.x;
        float y = this.y;
        float baseRotation = this.baseRotation;
        float rotation = this.rotation;
        if (enable_isoInput) {
            x = wpos.x;
            y = wpos.y;
            baseRotation = 360 - this.baseRotation + 45;
            rotation = 360 - this.rotation + 45;
        }
        // zones add end
        Draw.mixcol(Color.white, hitTime / hitDuration);

        float ft = Mathf.sin(walkTime * type.speed * 5f, 6f, 2f + type.hitsize / 15f);

        Floor floor = getFloorOn();

        if(floor.isLiquid){
            Draw.color(Color.white, floor.color, 0.5f);
        }

        for(int i : Mathf.signs){
            Draw.rect(type.legRegion,
                    x + Angles.trnsx(baseRotation, ft * i),
                    y + Angles.trnsy(baseRotation, ft * i),
                    type.legRegion.getWidth() * i * Draw.scl, type.legRegion.getHeight() * Draw.scl - Mathf.clamp(ft * i, 0, 2), baseRotation - 90);
        }

        if(floor.isLiquid){
            Draw.color(Color.white, floor.color, drownTime * 0.4f);
        }else{
            Draw.color(Color.white);
        }

        Draw.rect(type.baseRegion, x, y, baseRotation - 90);

        Draw.rect(type.region, x, y, rotation - 90);

        for(int i : Mathf.signs){
            float tra = rotation - 90, trY = -type.weapon.getRecoil(this, i > 0) + type.weaponOffsetY;
            float w = -i * type.weapon.region.getWidth() * Draw.scl;
            Draw.rect(type.weapon.region,
                    x + Angles.trnsx(tra, getWeapon().width * i, trY),
                    y + Angles.trnsy(tra, getWeapon().width * i, trY), w, type.weapon.region.getHeight() * Draw.scl, rotation - 90);
        }

        Draw.mixcol();

        super.draw();
    }

    @Override
    public void behavior(){     //  更新攻击行为
        if (true)   return;

        if(!Units.invalidateTarget(target, this)){
            if(dst(target) < getWeapon().bullet.range()){

                rotate(angleTo(target));

                if(Angles.near(angleTo(target), rotation, 13f)){
                    BulletType ammo = getWeapon().bullet;

                    Vec2 to = Predict.intercept(MachineUnit.this, target, ammo.speed);

                    getWeapon().update(MachineUnit.this, to.x, to.y);
                }
            }

            squadMember.setUpdateLocation(true);
        }
    }

    @Override
    public void updateTargeting(){
        if (true) return;

        super.updateTargeting();

        if(Units.invalidateTarget(target, team, x, y, Float.MAX_VALUE)){
            target = null;
        }

        if(retarget()){
            targetClosest();
        }
    }

    /** 巡逻*/
    protected void patrol(){
        vec.trns(baseRotation, type.speed * Time.delta());
        velocity.add(vec.x, vec.y);
        vec.trns(baseRotation, type.hitsizeTile * 5);
        Tile tile = world.tileWorld(x + vec.x, y + vec.y);
        if((tile == null || tile.solid() || tile.floor().drownTime > 0 || tile.floor().isLiquid) || stuckTime > 10f){
            baseRotation += Mathf.sign(id % 2 - 0.5f) * Time.delta() * 3f;
        }

        rotation = Mathf.slerpDelta(rotation, velocity.angle(), type.rotatespeed);
    }

    /** 单位圆形旋转*/
    protected void circle(float circleLength){
        if(target == null) return;

        vec.set(target.getX() - x, target.getY() - y);

        if(vec.len() < circleLength){
            vec.rotate((circleLength - vec.len()) / circleLength * 180f);
        }

        vec.setLength(type.speed * Time.delta());

        velocity.add(vec);
    }

    /** 移动到指定目标点*/
    public void moveToCore(PathTarget path){     // default protected
        Tile tile = world.tileWorld(x, y);
        if (enable_isoInput) {
            tile = world.tile(x, y);
        }
        if(tile == null) return;
        Tile targetTile = pathfinder.getTargetTile(tile, team, path);

        if(tile == targetTile) return;

        velocity.add(vec.trns(angleTo(targetTile), type.speed * Time.delta()));
        if(Units.invalidateTarget(target, this)){
            rotation = Mathf.slerpDelta(rotation, baseRotation, type.rotatespeed);
        }
    }

    /** 远离核心*/
    protected void moveAwayFromCore(){
        Team enemy = null;
        for(Team team : team.enemies()){
            if(team.active()){
                enemy = team;
                break;
            }
        }

        if(enemy == null){
            for(Team team : team.enemies()){
                enemy = team;
                break;
            }
        }

        if(enemy == null) return;

        Tile tile = world.tileWorld(x, y);
        if (enable_isoInput) {
            tile = world.tile(x, y);
        }
        if(tile == null) return;
        Tile targetTile = pathfinder.getTargetTile(tile, enemy, PathTarget.enemyCores);
        TileEntity core = getClosestCore();

        if(tile == targetTile || core == null || dst(core) < 120f) return;

        velocity.add(vec.trns(angleTo(targetTile), type.speed * Time.delta()));
        rotation = Mathf.slerpDelta(rotation, baseRotation, type.rotatespeed);
    }

    // zones add
    @Override
    public void removed() {
        super.removed();
        if (squadMember.getSquad() != null)
            squadMember.getSquad().removeMember(this);
    }
}
