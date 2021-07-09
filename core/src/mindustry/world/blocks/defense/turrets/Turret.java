package mindustry.world.blocks.defense.turrets;

import arc.Core;
import arc.audio.Sound;
import arc.func.Cons2;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.Array;
import arc.struct.EnumSet;
import arc.util.Time;
import arc.util.Tmp;
import arc.z.util.ISOUtils;
import mindustry.content.Fx;
import mindustry.entities.Effects;
import mindustry.entities.Effects.Effect;
import mindustry.entities.Predict;
import mindustry.entities.TargetPriority;
import mindustry.entities.Units;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.traits.TargetTrait;
import mindustry.entities.type.Bullet;
import mindustry.entities.type.TileEntity;
import mindustry.gen.Sounds;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.meta.BlockFlag;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.BlockStat;
import mindustry.world.meta.StatUnit;

import static mindustry.Vars.tilesize;
import static z.debug.ZDebug.enable_isoInput;

/**
 *  炮塔块
 * */
public abstract class Turret extends Block{
    /** 目标计时器*/
    public final int timerTarget = timers++;
    /** 更新目标间隔*/
    public int targetInterval = 20;

    /** 过热颜色*/
    public Color heatColor = Pal.turretHeat;
    /** 射击效果*/
    public Effect shootEffect = Fx.none;
    /** 烟雾效果*/
    public Effect smokeEffect = Fx.none;
    /** 使用弹壳效果*/
    public Effect ammoUseEffect = Fx.none;
    /** 射击音频*/
    public Sound shootSound = Sounds.shoot;

    /***/
    public int ammoPerShot = 1;
    /***/
    public float ammoEjectBack = 1f;
    /** 攻击范围*/
    public float range = enable_isoInput ? 50f / tilesize : 50f;
    /** 装填速度*/
    public float reload = 10f;
    /** 误差*/
    public float inaccuracy = 0f;
    /** 射击数量*/
    public int shots = 1;
    /** 扩散值*/
    public float spread = 4f;
    /** 反作用力*/
    public float recoil = 1f;
    /** 反作用力恢复*/
    public float restitution = 0.02f;
    /** 冷却*/
    public float cooldown = 0.02f;
    /** 旋转速度*/
    public float rotatespeed = 5f; //in degrees per tick
    /** 射击锥形*/
    public float shootCone = 8f;
    /** 射击抖动*/
    public float shootShake = 0f;
    /***/
    public float xRand = 0f;
    /** 攻击空中目标*/
    public boolean targetAir = true;
    /** 攻击地面目标*/
    public boolean targetGround = true;

    protected Vec2 tr = new Vec2();
    protected Vec2 tr2 = new Vec2();

    public TextureRegion baseRegion, heatRegion;

    /** 绘制器*/
    public Cons2<Tile, TurretEntity> drawer = (tile, entity) -> {
        // zones add begon
        float rotation = entity.rotation;
        if (enable_isoInput) {
            rotation = 360 - entity.rotation + 45;
        }
        // zones add end
        Draw.rect(region, tile.drawx() + tr2.x, tile.drawy() + tr2.y, rotation - 90);
    };
    /** 过热绘制器*/
    public Cons2<Tile, TurretEntity> heatDrawer = (tile, entity) -> {
        if(entity.heat <= 0.00001f) return;
        Draw.color(heatColor, entity.heat);
        Draw.blend(Blending.additive);
        Draw.rect(heatRegion, tile.drawx() + tr2.x, tile.drawy() + tr2.y, entity.rotation - 90);
        Draw.blend();
        Draw.color();
    };

    public Turret(String name){
        super(name);
        priority = TargetPriority.turret;
        update = true;
        solid = true;
        layer = Layer.turret;
        group = BlockGroup.turrets;
        flags = EnumSet.of(BlockFlag.turret);
        outlineIcon = true;
        entityType = TurretEntity::new;
    }

    @Override
    public boolean outputsItems(){
        return false;
    }

    @Override
    public void load(){
        super.load();

        region = Core.atlas.find(name);
        baseRegion = Core.atlas.find("block-" + size);
        heatRegion = Core.atlas.find(name + "-heat");
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.add(BlockStat.shootRange, range / tilesize, StatUnit.blocks);     // 射击范围
        stats.add(BlockStat.inaccuracy, (int)inaccuracy, StatUnit.degrees);         // 误差
        stats.add(BlockStat.reload, 60f / reload, StatUnit.none);       // 装填
        stats.add(BlockStat.shots, shots, StatUnit.none);           // 射击数量
        stats.add(BlockStat.targetsAir, targetAir);         // 攻击空中目标
        stats.add(BlockStat.targetsGround, targetGround);       // 攻击地面目标
    }

    @Override
    public void draw(Tile tile){
        Draw.rect(baseRegion, tile.drawx(), tile.drawy());
        Draw.color();
    }

    @Override
    public void drawLayer(Tile tile){
        TurretEntity entity = tile.ent();

        tr2.trns(entity.rotation, -entity.recoil);

        drawer.get(tile, entity);

        if(heatRegion != Core.atlas.find("error")){
            heatDrawer.get(tile, entity);
        }
    }

    @Override
    public TextureRegion[] generateIcons(){
        return new TextureRegion[]{Core.atlas.find("block-" + size), Core.atlas.find(name)};
    }

    @Override
    public void drawSelect(Tile tile){
        Drawf.dashCircle(tile.drawx(), tile.drawy(), range * tilesize, tile.getTeam().color);
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        if (enable_isoInput) {
            Vec2 pos = ISOUtils.tileToWorldCoords(x + offset(), y + offset());
            Drawf.dashCircle( pos.x, pos.y, range * tilesize, Pal.placing);
        } else {
            Drawf.dashCircle(x * tilesize + offset(), y * tilesize + offset(), range * tilesize, Pal.placing);
        }
    }

    @Override
    public void update(Tile tile){
        TurretEntity entity = tile.ent();

        if(!validateTarget(tile)) entity.target = null;

        entity.recoil = Mathf.lerpDelta(entity.recoil, 0f, restitution);
        entity.heat = Mathf.lerpDelta(entity.heat, 0f, cooldown);

        if(hasAmmo(tile)){

            if(entity.timer.get(timerTarget, targetInterval)){
                findTarget(tile);
            }

            if(validateTarget(tile)){

                BulletType type = peekAmmo(tile);
                float speed = type.speed;
                if(speed < 0.1f) speed = 9999999f;

                Vec2 result = Predict.intercept(entity, entity.target, speed);
                if(result.isZero()){
                    result.set(entity.target.getX(), entity.target.getY());
                }

//                float targetRot = result.sub(tile.drawx(), tile.drawy()).angle();       // zones editor  tile.drawx(), tile.drawy()
                float targetRot ;
                if (enable_isoInput) {
                    targetRot = result.sub(tile.getX(), tile.getY()).angle();
                } else {
                    targetRot = result.sub(tile.drawx(), tile.drawy()).angle();
                }

                if(Float.isNaN(entity.rotation)){
                    entity.rotation = 0;
                }

                if(shouldTurn(tile)){
                    turnToTarget(tile, targetRot);
                }

                if(Angles.angleDist(entity.rotation, targetRot) < shootCone){
                    updateShooting(tile);
                }
            }
        }
    }

    /** 验证目标是否有效(tile坐标系统)*/
    protected boolean validateTarget(Tile tile){
        TurretEntity entity = tile.ent();
        if (enable_isoInput) {
            return !Units.invalidateTarget(entity.target, tile.getTeam(), tile.getX(), tile.getY());
        } else {
            return !Units.invalidateTarget(entity.target, tile.getTeam(), tile.drawx(), tile.drawy());
        }
    }

    /** 获取目标*/
    protected void findTarget(Tile tile){
        // zones add begon
        float x = tile.drawx();
        float y = tile.drawy();
        if (enable_isoInput) {
            x = tile.getX();
            y = tile.getY();
        }
        // zones add end
        TurretEntity entity = tile.ent();

        if(targetAir && !targetGround){
            entity.target = Units.closestEnemy(tile.getTeam(), x, y, range, e -> !e.isDead() && e.isFlying());
        }else{
            entity.target = Units.closestTarget(tile.getTeam(), x, y, range, e -> !e.isDead() && (!e.isFlying() || targetAir) && (e.isFlying() || targetGround));
        }
    }

    /** 旋转向目标*/
    protected void turnToTarget(Tile tile, float targetRot){
        TurretEntity entity = tile.ent();

        entity.rotation = Angles.moveToward(entity.rotation, targetRot, rotatespeed * entity.delta() * baseReloadSpeed(tile));
    }

    /** 是否旋转*/
    public boolean shouldTurn(Tile tile){
        return true;
    }

    /** 使用弹药并返回类型.<p/>Consume ammo and return a type. */
    public BulletType useAmmo(Tile tile){
        if(tile.isEnemyCheat()) return peekAmmo(tile);

        TurretEntity entity = tile.ent();
        AmmoEntry entry = entity.ammo.peek();
        entry.amount -= ammoPerShot;
        if(entry.amount == 0) entity.ammo.pop();
        entity.totalAmmo -= ammoPerShot;
        Time.run(reload / 2f, () -> ejectEffects(tile));
        return entry.type();
    }

    /** 获取将使用弹药类型.<p/>Get the ammo type that will be returned if useAmmo is called.*/
    public BulletType peekAmmo(Tile tile){
        TurretEntity entity = tile.ent();
        return entity.ammo.peek().type();
    }

    /** 是否拥有弹药.<p/>Returns whether the turret has ammo.*/
    public boolean hasAmmo(Tile tile){
        TurretEntity entity = tile.ent();
        return entity.ammo.size > 0 && entity.ammo.peek().amount >= ammoPerShot;
    }

    /** 更新射击*/
    protected void updateShooting(Tile tile){
        TurretEntity entity = tile.ent();

        if(entity.reload >= reload){
            BulletType type = peekAmmo(tile);

            shoot(tile, type);

            entity.reload = 0f;
        }else{
            entity.reload += tile.entity.delta() * peekAmmo(tile).reloadMultiplier * baseReloadSpeed(tile);
        }
    }

    /** 射击*/
    protected void shoot(Tile tile, BulletType type){
        TurretEntity entity = tile.ent();

        entity.recoil = recoil;
        entity.heat = 1f;

        tr.trns(entity.rotation, size * tilesize / 2f, Mathf.range(xRand));

        for(int i = 0; i < shots; i++){
            bullet(tile, type, entity.rotation + Mathf.range(inaccuracy + type.inaccuracy) + (i - shots / 2) * spread);
        }

        effects(tile);
        useAmmo(tile);
    }

    /** 子弹*/
    protected void bullet(Tile tile, BulletType type, float angle){
        if (enable_isoInput) {
            Bullet.create(type, tile.entity, tile.getTeam(), tile.getX() + tr.x, tile.getY() + tr.y, angle);
        } else {
            Bullet.create(type, tile.entity, tile.getTeam(), tile.drawx() + tr.x, tile.drawy() + tr.y, angle);
        }
    }

    /** 射击效果*/
    protected void effects(Tile tile){
        Effect shootEffect = this.shootEffect == Fx.none ? peekAmmo(tile).shootEffect : this.shootEffect;
        Effect smokeEffect = this.smokeEffect == Fx.none ? peekAmmo(tile).smokeEffect : this.smokeEffect;

        TurretEntity entity = tile.ent();

        if ( enable_isoInput) {
            Effects.effect(shootEffect, tile.getX() + tr.x, tile.getY() + tr.y, entity.rotation);
            Effects.effect(smokeEffect, tile.getX() + tr.x, tile.getY() + tr.y, entity.rotation);
            shootSound.at(Tmp.v11.set(ISOUtils.tileToWorldCoords(tile)), Mathf.random(0.9f, 1.1f));
        } else {
            Effects.effect(shootEffect, tile.drawx() + tr.x, tile.drawy() + tr.y, entity.rotation);
            Effects.effect(smokeEffect, tile.drawx() + tr.x, tile.drawy() + tr.y, entity.rotation);
            shootSound.at(tile, Mathf.random(0.9f, 1.1f));
        }

        if(shootShake > 0){
            Effects.shake(shootShake, shootShake, tile.entity);
        }

        entity.recoil = recoil;
    }

    /** 枪口火焰效果*/
    protected void ejectEffects(Tile tile){
        if(!isTurret(tile)) return;
        TurretEntity entity = tile.ent();

        Effects.effect(ammoUseEffect, tile.drawx() - Angles.trnsx(entity.rotation, ammoEjectBack),
        tile.drawy() - Angles.trnsy(entity.rotation, ammoEjectBack), entity.rotation);
    }

    /** 基础装填速度*/
    protected float baseReloadSpeed(Tile tile){
        return 1f;
    }

    /** 瓦砾是否为炮塔*/
    protected boolean isTurret(Tile tile){
        return (tile.entity instanceof TurretEntity);
    }


    /**
     *  接收弹药
     * */
    public static abstract class AmmoEntry{
        /** 数量*/
        public int amount;

        /** 弹药类型*/
        public abstract BulletType type();
    }

    /**
     *  炮塔瓦砾实体
     * */
    public static class TurretEntity extends TileEntity{
        /** 接收弹药类型容器*/
        public Array<AmmoEntry> ammo = new Array<>();
        /** 总弹药数量*/
        public int totalAmmo;
        /** 装填*/
        public float reload;
        /** 旋转*/
        public float rotation = 90;
        /** 反作用力*/
        public float recoil = 0f;
        /** 温度*/
        public float heat;
        /** 射击数量*/
        public int shots;
        /** 目标*/
        public TargetTrait target;
    }
}
