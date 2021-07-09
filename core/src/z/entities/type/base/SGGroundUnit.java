package z.entities.type.base;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.util.Time;
import mindustry.Vars;
import mindustry.ai.Pathfinder.PathTarget;
import mindustry.entities.Predict;
import mindustry.entities.TargetPriority;
import mindustry.entities.Units;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.type.BaseUnit;
import mindustry.entities.type.TileEntity;
import mindustry.entities.units.UnitCommand;
import mindustry.entities.units.UnitState;
import mindustry.game.Team;
import mindustry.type.Weapon;
import mindustry.world.Tile;
import mindustry.world.blocks.Floor;
import mindustry.world.meta.BlockFlag;
import z.utils.FinalCons;

import static mindustry.Vars.pathfinder;
import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;

public class SGGroundUnit extends BaseUnit{
    protected static Vec2 vec = new Vec2();

    protected float walkTime;
    protected float stuckTime;
    protected float baseRotation;

    public final UnitState

            // zones add begon

            /** AI控制系统状态, 测试状态*/
            testState = new UnitState<SGGroundUnit>(){
        @Override
        public void enter(SGGroundUnit u){
//            target = null;
        }

        @Override
        public void exit(SGGroundUnit u) {
        }

        @Override
        public void update(SGGroundUnit u){
            Vec2 target = squadMember.getPosition();

            if(stateSprite != State.ATTACK ){
                if (squadMember.updateLocation() && dst(target) > 4f / 2f) {
                    moveTo(squadMember.squadID, squadMember.memberID);
                    {
                        Time.run(30, ()->{
                            if (velocity.len() < 0.1f / tilesize) {
                                setAniState(State.IDLE);
                                squadMember.setUpdateLocation(false);
                            }
                        });
                    }
                } else {
                    setAniState(State.IDLE);
                    squadMember.setUpdateLocation(false);
                }
            }

            if (velocity.len() > 0.1f / tilesize && stateSprite != State.ATTACK) {
                setAniState(State.WALK);
            }
        }
    },

            waveRally = new UnitState<SGGroundUnit>(){
                @Override
        public void enter(SGGroundUnit u){
            target = null;
        }

        @Override
        public void update(SGGroundUnit u){
            if (stateSprite == State.ATTACK && target == null)
                setAniState(State.WALK);
//            if (stateSprite == State.IDLE) {
//                setAniState(State.WALK);
//                stateSprite = State.WALK;
//            }

            if (stateSprite == State.WALK) {
//                moveToCore(PathTarget.waveRallyPoints);
            }
        }
    },

    // zones add end

            attack = new UnitState<SGGroundUnit>(){
                @Override
        public void enter(SGGroundUnit u){
            target = null;
        }

        @Override
        public void update(SGGroundUnit u){
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
            rally = new UnitState<SGGroundUnit>(){
                @Override
                public void update(SGGroundUnit u){
                    Tile target = getClosest(BlockFlag.rally);

                    if(target != null && dst(target) > 80f){
                        moveToCore(PathTarget.rallyPoints);
                    }
                }
            },
            retreat = new UnitState<SGGroundUnit>(){
                @Override
                public void enter(SGGroundUnit u){
                    target = null;
                }

                public void update(SGGroundUnit u){
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
        // zones add begon
//        return waveRally;
        return testState;
        // zones add end
//        return attack;
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
        }
        {   //  打印测试
//            System.out.println("队伍移动目标点-----" +  formationTarget.getTargetLocation().getPosition().toString());
        }
    }

    @Override
    public Weapon getWeapon(){
        return type.weapon;
    }

    @Override
    public void draw(){
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

        // zones add begon
        if (selectState) {
            Draw.color(Color.darkGray);
            Fill.rect(x, y, drawSize() * Draw.scl * 0.7f, 8 * Draw.scl);
            Draw.color(Color.scarlet);
            Fill.rect(x, y, drawSize() * Draw.scl * 0.7f, 8 * Draw.scl);
        }
        // zones add end

        Draw.mixcol();
    }

    @Override
    public void behavior(){
        if(!Units.invalidateTarget(target, getTeam(), x, y, type.range )){      // 检测目标是否有效
            if(dst(target) < getWeapon().bullet.range()){       // 目标是否在有效攻击范围内

                rotate(angleTo(target));

                if(Angles.near(angleTo(target), rotation, 13f)){
                    BulletType ammo = getWeapon().bullet;

                    Vec2 to = Predict.intercept(SGGroundUnit.this, target, ammo.speed);

//                    getWeapon().update(SGGroundUnit.this, to.x, to.y);
                    getWeapon().update(SGGroundUnit.this, to.x, to.y);
                    // zones add begon
                    {   // 设置攻击动画状态和自动关闭动画状态
                        Time.run(getWeapon().reload, () -> {
                            attackIndex--;
                            if (attackIndex == 0) {
                                setAniState(State.IDLE);
                                squadMember.setUpdateLocation(true);
                            }
                        });
                        attackIndex++;
                        setAniState(State.ATTACK);
                    }
                    // zones add edn
                }
            }
            else if(dst(target) < getWeapon().bullet.range() * 2) {  // 移动到攻击目标
//                System.out.println("移动设置有效");
                velocity.add(vec.trns(angleTo(target), type.speed * Time.delta()));
                if(Units.invalidateTarget(target, this)){
                    rotation = Mathf.slerpDelta(rotation, baseRotation, type.rotatespeed);
                }
            }
            else {
                squadMember.setUpdateLocation(true);
            }
        }

//        if(!Units.invalidateTarget(target, this)){      // 检测目标是否有效
//            if(dst(target) < getWeapon().bullet.range()){       // 目标是否在有效攻击范围内
//
//                rotate(angleTo(target));
//
//                if(Angles.near(angleTo(target), rotation, 13f)){
//                    BulletType ammo = getWeapon().bullet;
//
//                    Vec2 to = Predict.intercept(SGGroundUnit.this, target, ammo.speed);
//
//                    getWeapon().update(SGGroundUnit.this, to.x, to.y);
//                    // zones add begon
//                    {   // 设置攻击动画状态和自动关闭动画状态
//                        Time.run(getWeapon().reload, () -> {
//                            attackIndex--;
//                            if (attackIndex == 0)
//                                setAniState(State.IDLE);
//                        });
//                        attackIndex++;
//                        setAniState(State.ATTACK);
//                    }
//                    // zones add edn
//                }
//            }
//        }
    }

    @Override
    public void updateTargeting(){
        super.updateTargeting();

        if(Units.invalidateTarget(target, team, x, y, Float.MAX_VALUE)){
            target = null;
        }

        if(retarget()){
            targetClosest();
        }
    }

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

    protected void circle(float circleLength){
        if(target == null) return;

        vec.set(target.getX() - x, target.getY() - y);

        if(vec.len() < circleLength){
            vec.rotate((circleLength - vec.len()) / circleLength * 180f);
        }

        vec.setLength(type.speed * Time.delta());

        velocity.add(vec);
    }

    protected void moveToCore(PathTarget path){
        Tile tile = world.tileWorld(x, y);
        if(tile == null) return;
        Tile targetTile = pathfinder.getTargetTile(tile, team, path);

        if(tile == targetTile) return;

        velocity.add(vec.trns(FinalCons.angleTo(getX(), getY(), targetTile), type.speed * Time.delta()));
        if(Units.invalidateTarget(target, this)){
            rotation = Mathf.slerpDelta(rotation, baseRotation, type.rotatespeed);
        }
    }

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
        if(tile == null) return;
        Tile targetTile = pathfinder.getTargetTile(tile, enemy, PathTarget.enemyCores);
        TileEntity core = getClosestCore();

        if(tile == targetTile || core == null || dst(core) < 120f) return;

        velocity.add(vec.trns(FinalCons.angleTo(getX(), getY(), targetTile), type.speed * Time.delta()));
        rotation = Mathf.slerpDelta(rotation, baseRotation, type.rotatespeed);
    }

    // zones add begon
//    @Nullable
//    protected Tile getClosestWaveRally(){
//        return Geometry.findClosest(x, y, Vars.spawner.getRallyPoints());
//    }

    /** 绘制动画使用状态与Unit状态无关*/
    protected float stateDeltatime = 0;
    protected State stateSprite = State.IDLE;

    protected enum State {
        IDLE, WALK, ATTACK,
    }

    private void setAniState(State aniState) {
        if (aniState != stateSprite) {
            stateSprite = aniState;
            if (aniState == State.ATTACK)
            stateDeltatime = 0;
        }
    }

    public void select(boolean select) {
        this.selectState = select;
    }

//    @Deprecated
//    public void setMoveTarget(Tile targetTile) {
////        targetAStar = targetTile;
//    }

    protected void moveTo(int squadID, int memberID){
        Tile tile = world.tileWorld(x, y);
        if(tile == null) return;
        Position targetTile = pathfinder.getTargetMember(tile, team, squadID, memberID);

        if(tile.equalsPos(targetTile)) return;

        velocity.add(vec.trns(FinalCons.angleTo(this.getX(), this.getY(), targetTile), type.speed * Time.delta()));
        if(Units.invalidateTarget(target, this)){
            rotation = Mathf.slerpDelta(rotation, baseRotation, type.rotatespeed);
        }
    }

    @Override
    public void removed() {
        super.removed();
        squadMember.getSquad().removeMember(this);
    }

    /** 单位选择状态*/
    protected boolean selectState = false;
//    @ArcAnnotate.Nullable
//    @Deprecated
//    private Tile targetAStar = null;     // or TargetTrait   Tile
    /** 移动延时步长*/
    @Deprecated
    private float moveDeltaStep = 0;
    @Deprecated
    private TargetPriority attackTarget = null;
    @Deprecated
    private int attackIndex = 0;

    // zones add end
}
