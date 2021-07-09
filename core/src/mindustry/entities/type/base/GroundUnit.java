package mindustry.entities.type.base;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Time;
import mindustry.Vars;
import mindustry.ai.Pathfinder.PathTarget;
import mindustry.entities.Predict;
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

import static mindustry.Vars.pathfinder;
import static mindustry.Vars.world;
import static z.debug.ZDebug.enable_isoInput;

/**
 *  地面单位实体
 * */
public class GroundUnit extends BaseUnit{
    protected static Vec2 vec = new Vec2();

    /** 行走时间*/
    protected float walkTime;
    /** 被动时间*/
    protected float stuckTime;
    /** 基础角度*/
    protected float baseRotation;

    /** 单位状态机状态*/
    public final UnitState

    attack = new UnitState<GroundUnit>(){
        @Override
        public void enter(GroundUnit u){
            target = null;
        }

        @Override
        public void update(GroundUnit u){
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
    rally = new UnitState<GroundUnit>(){
        @Override
        public void update(GroundUnit u){
            Tile target = getClosest(BlockFlag.rally);

            if(target != null && dst(target) > 80f){
                moveToCore(PathTarget.rallyPoints);
            }
        }
    },
    retreat = new UnitState<GroundUnit>(){
        @Override
        public void enter(GroundUnit u){
            target = null;
        }

        @Override
        public void update(GroundUnit u){
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
        return attack;
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

        if(!Units.invalidateTarget(target, this)){
            if(dst(target) < getWeapon().bullet.range()){

                rotate(angleTo(target));

                if(Angles.near(angleTo(target), rotation, 13f)){
                    BulletType ammo = getWeapon().bullet;

                    Vec2 to = Predict.intercept(GroundUnit.this, target, ammo.speed);

                    getWeapon().update(GroundUnit.this, to.x, to.y);
                }
            }
        }
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
    protected void moveToCore(PathTarget path){
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
}
