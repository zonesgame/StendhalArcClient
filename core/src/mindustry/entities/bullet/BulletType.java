package mindustry.entities.bullet;

import arc.audio.*;
import arc.math.*;
import arc.util.Tmp;
import arc.z.util.ISOUtils;
import arc.z.util.ZonesAnnotate.ZField;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.entities.*;
import mindustry.entities.Effects.*;
import mindustry.entities.effect.*;
import mindustry.entities.traits.*;
import mindustry.entities.type.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;

import static mindustry.Vars.tilesize;
import static z.debug.ZDebug.enable_isoInput;

/**
 *  弹药类型
 * */
public abstract class BulletType extends Content{
    /** 存活时间*/
    public float lifetime;
    /** 速度*/
    public float speed;
    /** 伤害*/
    public float damage;
    /** 碰撞尺寸*/
    public float hitSize = enable_isoInput ? 4f / tilesize : 4;
    /** 绘制尺寸*/
    public float drawSize = 40f;
    /** 子弹阻力(value 0-1, no continue)*/
    public float drag = 0f;
    /** 是否可穿透目标*/
    public boolean pierce;
    /** 撞击效果*/
    public Effect hitEffect, /**销毁效果*/despawnEffect;

    /** 射击效果.<p/>Effect created when shooting. */
    public Effect shootEffect = Fx.shootSmall;
    /** 烟雾效果.<p/>Extra smoke effect created when shooting. */
    public Effect smokeEffect = Fx.shootSmallSmoke;
    /** 撞击音频.<p/>Sound made when hitting something or getting removed.*/
    public Sound hitSound = Sounds.none;
    /** 射击误差.<p/>Extra inaccuracy when firing. */
    public float inaccuracy = 0f;
    /** (物品提供弹药子弹数量)弹药子弹数量.<p/>How many bullets get created per ammo item/liquid. */
    public float ammoMultiplier = 2f;
    /** 装填速度倍数.<p/>Multiplied by turret reload speed to get final shoot speed. */
    public float reloadMultiplier = 1f;
    /** 反作用力.<p/>Recoil from shooter entities. */
    public float recoil;
    /** 是否在开枪时杀死射击者.为自杀式炸弹袭击者.<p/>Whether to kill the shooter when this is shot. For suicide bombers. */
    public boolean killShooter;
    /** 是否立即使子弹消失.<p/>Whether to instantly make the bullet disappear. */
    public boolean instantDisappear;
    /** 溅射伤害,值0禁用.<p/>Damage dealt in splash. 0 to disable.*/
    public float splashDamage = 0f;
    /** 减速速度.<p/>Knockback in velocity. */
    public float knockback;
    /** 子弹是否击中瓦砾.<p/>Whether this bullet hits tiles. */
    public boolean hitTiles = true;
    /** 撞击状态效果.<p/>Status effect applied on hit. */
    public StatusEffect status = StatusEffects.none;
    /** 状态持续时间.<p/>Intensity of applied status effect in terms of duration. */
    public float statusDuration = 60 * 10f;
    /** 子弹是否可与瓦砾发生碰撞.<p/>Whether this bullet type collides with tiles. */
    public boolean collidesTiles = true;
    /** 子弹是否可与相同队伍瓦砾发生碰撞.<p/>Whether this bullet type collides with tiles that are of the same team. */
    public boolean collidesTeam = false;
    /** 这颗子弹是否与空气单位相撞.<p/>Whether this bullet type collides with air units. */
    public boolean collidesAir = true;
    /** 这颗子弹是否完全与任何东西碰撞.<p/>Whether this bullet types collides with anything at all. */
    public boolean collides = true;
    /** 速度是否从枪手继承.<p/>Whether velocity is inherited from the shooter. */
    public boolean keepVelocity = true;

    // 附加效果. additional effects

    public int fragBullets = 9;
    /***/
    public float fragVelocityMin = 0.2f, fragVelocityMax = 1f;
    /** 溅射弹片子弹*/
    public BulletType fragBullet = null;

    /** 使用负值来禁用飞溅伤害.<p/>Use a negative value to disable splash damage. */
    public float splashDamageRadius = -1f;

    /** 燃烧数量*/
    public int incendAmount = 0;
    /** 燃烧扩散值(燃烧范围)*/
    public float incendSpread = 8f;
    /** 燃烧几率*/
    public float incendChance = 1f;

    /** 自导能力*/
    public float homingPower = 0f;
    /** 自导半径*/
    public float homingRange = 50f;

    /** 亮度*/
    public int lightining;
    /** 亮度长度*/
    public int lightningLength = 5;

    /** 撞击抖动*/
    public float hitShake = 0f;

    // zones add begon
    /** 攻击距离扩展使用(主要适用于近战单位, 通过配置文件初始化, !禁止在代码中初始化)*/
    @ZField
    public float range;
    // zones add end

    public BulletType(float speed, float damage){
        this.speed = speed;
        this.damage = damage;
        lifetime = 40f;
        hitEffect = Fx.hitBulletSmall;
        despawnEffect = Fx.hitBulletSmall;
    }

    /** 子弹最大移动距离.<p/>Returns maximum distance the bullet this bullet type has can travel. */
    public float range(){
        return speed * lifetime * (1f - drag);
    }

    /** 碰撞检测*/
    public boolean collides(Bullet bullet, Tile tile){
        return true;
    }

    /** 撞击瓦砾*/
    public void hitTile(Bullet b, Tile tile){
        hit(b);
    }

    /** 撞击事件*/
    public void hit(Bullet b){
        hit(b, b.x, b.y);
    }

    /** 撞击事件*/
    public void hit(Bullet b, float x, float y){
        Effects.effect(hitEffect, x, y, b.rot());
        if (enable_isoInput) {
            hitSound.at(Tmp.v11.set(ISOUtils.tileToWorldCoords(b)));
        } else {
            hitSound.at(b);
        }

        Effects.shake(hitShake, hitShake, b);

        if(fragBullet != null){
            for(int i = 0; i < fragBullets; i++){
                float len = Mathf.random(1f, 7f);
                float a = Mathf.random(360f);
                Bullet.create(fragBullet, b, x + Angles.trnsx(a, len), y + Angles.trnsy(a, len), a, Mathf.random(fragVelocityMin, fragVelocityMax));
            }
        }

        if(Mathf.chance(incendChance)){
            Damage.createIncend(x, y, incendSpread, incendAmount);
        }

        if(splashDamageRadius > 0){
            Damage.damage(b.getTeam(), x, y, splashDamageRadius, splashDamage * b.damageMultiplier());
        }
    }

    /** 销毁*/
    public void despawned(Bullet b){
        Effects.effect(despawnEffect, b.x, b.y, b.rot());
        hitSound.at(b);

        if(fragBullet != null || splashDamageRadius > 0){
            hit(b);
        }

        for(int i = 0; i < lightining; i++){
            Lightning.createLighting(Lightning.nextSeed(), b.getTeam(), Pal.surge, damage, b.x, b.y, Mathf.random(360f), lightningLength);
        }
    }

    /** 绘制*/
    public void draw(Bullet b){
    }

    /** 初始化*/
    public void init(Bullet b){
        if(killShooter && b.getOwner() instanceof HealthTrait){
            ((HealthTrait)b.getOwner()).kill();
        }

        if(instantDisappear){
            b.time(lifetime);
        }
    }

    /** 更新*/
    public void update(Bullet b){

        if(homingPower > 0.0001f){
            TargetTrait target = Units.closestTarget(b.getTeam(), b.x, b.y, homingRange, e -> !e.isFlying() || collidesAir);
            if(target != null){
                b.velocity().setAngle(Mathf.slerpDelta(b.velocity().angle(), b.angleTo(target), 0.08f));
            }
        }
    }

    @Override
    public ContentType getContentType(){
        return ContentType.bullet;
    }
}
