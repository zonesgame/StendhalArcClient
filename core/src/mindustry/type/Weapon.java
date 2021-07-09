package mindustry.type;

import arc.Core;
import arc.audio.Sound;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.util.ArcAnnotate.NonNull;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.annotations.Annotations.Loc;
import mindustry.annotations.Annotations.Remote;
import mindustry.content.Fx;
import mindustry.entities.Effects;
import mindustry.entities.Effects.Effect;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.traits.ShooterTrait;
import mindustry.entities.traits.TargetTrait;
import mindustry.entities.type.Bullet;
import mindustry.entities.type.Player;
import mindustry.gen.Call;
import mindustry.gen.Sounds;

import static mindustry.Vars.net;
import static z.debug.ZDebug.enable_isoInput;

/**
 *  Unit的机甲Mech武器
 * */
public class Weapon{
    public String name;

    /** 左右射击最小交叉角度.<p/>minimum cursor distance from player, fixes 'cross-eyed' shooting. */
    protected static float minPlayerDist = 20f;
    /** 序列编号*/
    protected static int sequenceNum = 0;
    /** 射击子弹类型.<p/>bullet shot */
    public @NonNull BulletType bullet;
    /** 弹壳效果.<p/>shell ejection effect */
    public Effect ejectEffect = Fx.none;
    /** 武器装填频率.<p/>weapon reload in frames */
    public float reload;
    /** 子弹数量.<p/>amount of shots per fire */
    public int shots = 1;
    /** 在多个镜头之间的间距,如果适用的话.<p/>spacing in degrees between multiple shots, if applicable */
    public float spacing = 12f;
    /** 射击误差.<p/>inaccuracy of degrees of each shot */
    public float inaccuracy = 0f;
    /** 屏幕抖动强度和时间.<p/>intensity and duration of each shot's screen shake */
    public float shake = 0f;
    /** 显示武器作用力.<p/>visual weapon knockback. */
    public float recoil = 1.5f;
    /** 射击枪管Y轴偏移量.<p/>shoot barrel y offset */
    public float length = 3f;
    /** 射击枪管X轴偏移量.<p/>shoot barrel x offset. */
    public float width = 4f;
    /** 随机速度分数.<p/>fraction of velocity that is random */
    public float velocityRnd = 0f;
    /** 是否要一个接一个地在不同的手臂上发射武器,而不是立即发射.<p/>whether to shoot the weapons in different arms one after another, rather than all at once */
    public boolean alternate = false;
    /** 射距随机化.<p/>randomization of shot length */
    public float lengthRand = 0f;
    /** 射击之间延时.<p/>delay in ticks between shots */
    public float shotDelay = 0;
    /** 是否忽略射击旋转.<p/>whether shooter rotation is ignored when shooting. */
    public boolean ignoreRotation = false;
    /** if turnCursor is false for a mech, how far away will the weapon target. */
    public float targetDistance = 1f;

    /** 射击音频*/
    public Sound shootSound = Sounds.pew;

    /** 武器纹理*/
    public TextureRegion region;

    // zones add begon
    /** 禁用左右射击,三国专用属性*/
    public boolean disableAlternate = false;
    // zones add end

    protected Weapon(String name){
        this.name = name;
    }

    public Weapon(){
        //no region
        this.name = "";
    }

    @Remote(targets = Loc.server, called = Loc.both, unreliable = true)
    public static void onPlayerShootWeapon(Player player, float x, float y, float rotation, boolean left){

        if(player == null) return;
        //clients do not see their own shoot events: they are simulated completely clientside to prevent laggy visuals
        //messing with the firerate or any other stats does not affect the server (take that, script kiddies!)
        if(net.client() && player == Vars.player){
            return;
        }

        shootDirect(player, x, y, rotation, left);
    }

    @Remote(targets = Loc.server, called = Loc.both, unreliable = true)
    public static void onGenericShootWeapon(ShooterTrait shooter, float x, float y, float rotation, boolean left){
        if(shooter == null) return;
        shootDirect(shooter, x, y, rotation, left);
    }

    /** 直接射击*/
    public static void shootDirect(ShooterTrait shooter, float offsetX, float offsetY, float rotation, boolean left){
        // zones add begon
//        final Vec2 _pos = new Vec2();
//        _pos.set(shooter);
//        if (enable_isoInput) {
//            _pos.set(ISOUtils.tileToWorldCoords(shooter));
//        }
        //        float x = _pos.getX() + offsetX;         // zones editor
//        float y = _pos.getY() + offsetY;
//        float baseX = _pos.getX(), baseY = _pos.getY();
        // zones add end
        float x = shooter.getX() + offsetX;
        float y = shooter.getY() + offsetY;
        float baseX = shooter.getX(), baseY = shooter.getY();

        Weapon weapon = shooter.getWeapon();
        weapon.shootSound.at(x, y, Mathf.random(0.8f, 1.0f));

        sequenceNum = 0;
        if(weapon.shotDelay > 0.01f){
            Angles.shotgun(weapon.shots, weapon.spacing, rotation, f -> {
                Time.run(sequenceNum * weapon.shotDelay, () -> weapon.bullet(shooter, x + shooter.getX() - baseX, y + shooter.getY() - baseY, f + Mathf.range(weapon.inaccuracy)));
//                Time.run(sequenceNum * weapon.shotDelay, () -> weapon.bullet(shooter, x + _pos.getX() - baseX, y + _pos.getY() - baseY, f + Mathf.range(weapon.inaccuracy))); // zones add
                sequenceNum++;
            });
        }else{
            Angles.shotgun(weapon.shots, weapon.spacing, rotation, f -> weapon.bullet(shooter, x, y, f + Mathf.range(weapon.inaccuracy)));
        }

        BulletType ammo = weapon.bullet;

        Tmp.v1.trns(rotation + 180f, ammo.recoil);

        shooter.velocity().add(Tmp.v1);

        Tmp.v1.trns(rotation, 3f);

        Effects.shake(weapon.shake, weapon.shake, x, y);
        Effects.effect(weapon.ejectEffect, x, y, rotation * -Mathf.sign(left));
        Effects.effect(ammo.shootEffect, x + Tmp.v1.x, y + Tmp.v1.y, rotation, shooter);
        Effects.effect(ammo.smokeEffect, x + Tmp.v1.x, y + Tmp.v1.y, rotation, shooter);

        //reset timer for remote players
        shooter.getTimer().get(shooter.getShootTimer(left), weapon.reload);
    }

    public void load(){
        region = Core.atlas.find(name + "-equip", Core.atlas.find(name, Core.atlas.find("clear")));
    }

    /** 目标点射击
     * @param pointerX  目标X轴方向
     * @param pointerY  目标Y轴方向
     * */
    public void update(ShooterTrait shooter, float pointerX, float pointerY) {
        if (enable_isoInput) {
            boolean left = false;

            Tmp.v1.set(pointerX, pointerY).sub(shooter.getX(), shooter.getY());
            if (Tmp.v1.len() < minPlayerDist) Tmp.v1.setLength(minPlayerDist);

            float cx = Tmp.v1.x + shooter.getX(), cy = Tmp.v1.y + shooter.getY();

            float ang = Tmp.v1.angle();
            Tmp.v1.trns(ang - 90, 0, -3);

            update(shooter, shooter.getX() + Tmp.v1.x, shooter.getY() + Tmp.v1.y, Angles.angle(shooter.getX() + Tmp.v1.x, shooter.getY() + Tmp.v1.y, cx, cy), left);
            return;
        }

        for(boolean left : Mathf.booleans){
            if (disableAlternate) {   // zones add code
                left = true;
            }
            Tmp.v1.set(pointerX, pointerY).sub(shooter.getX(), shooter.getY());
            if(Tmp.v1.len() < minPlayerDist) Tmp.v1.setLength(minPlayerDist);

            float cx = Tmp.v1.x + shooter.getX(), cy = Tmp.v1.y + shooter.getY();

            float ang = Tmp.v1.angle();
            if (enable_isoInput) {
                Tmp.v1.trns(ang - 90, 0, -3);
            } else {
                Tmp.v1.trns(ang - 90, disableAlternate ? 0 : (width * Mathf.sign(left) ), length + Mathf.range(lengthRand));
            }

            update(shooter, shooter.getX() + Tmp.v1.x, shooter.getY() + Tmp.v1.y, Angles.angle(shooter.getX() + Tmp.v1.x, shooter.getY() + Tmp.v1.y, cx, cy), left);
        }
    }

    public void update(ShooterTrait shooter, float mountX, float mountY, float angle, boolean left){
        if(shooter.getTimer().get(shooter.getShootTimer(left), reload)){
            if(alternate){
                shooter.getTimer().reset(shooter.getShootTimer(!left), reload / 2f);
            }

            shoot(shooter, mountX - shooter.getX(), mountY - shooter.getY(), angle, left);
        }
    }

    /** 武器射击作用力*/
    public float getRecoil(ShooterTrait player, boolean left){
        return (1f - Mathf.clamp(player.getTimer().getTime(player.getShootTimer(left)) / reload)) * recoil;
    }

    /**
     * @param x 子弹创建X轴位置
     * @param y 子弹创建Y轴位置
     * @param angle 子弹角度
     * @param left 是否左手射击
     * */
    public void shoot(ShooterTrait p, float x, float y, float angle, boolean left){
        if(net.client()){
            //call it directly, don't invoke on server
            shootDirect(p, x, y, angle, left);
        }else{
            if(p instanceof Player){ //players need special weapon handling logic
                Call.onPlayerShootWeapon((Player)p, x, y, angle, left);
            }else{
                Call.onGenericShootWeapon(p, x, y, angle, left);
            }
        }
    }

    /** 创建子弹*/
    void bullet(ShooterTrait owner, float x, float y, float angle){
        if(owner == null) return;

        Tmp.v1.trns(angle, 3f);
        Bullet.create(bullet,
                owner, owner.getTeam(), x + Tmp.v1.x, y + Tmp.v1.y, angle, (1f - velocityRnd) + Mathf.random(velocityRnd));
    }


    // zones add begon
    /**
     *  近战攻击子弹计算使用
     * @param shooter   攻击者
     * @param target    攻击目标对象
     * */
    private void update(ShooterTrait shooter, TargetTrait target) {
        if (true) {
            update(shooter, target.getX(), target.getY(), 0, false);
            return;
        }
//        for(boolean left : Mathf.booleans){
//            if (disableAlternate) {   // zones add code
//                left = true;
//            }
//            Tmp.v1.set(pointerX, pointerY).sub(shooter.getX(), shooter.getY());
//            if(Tmp.v1.len() < minPlayerDist) Tmp.v1.setLength(minPlayerDist);
//
//            float cx = Tmp.v1.x + shooter.getX(), cy = Tmp.v1.y + shooter.getY();
//
//            float ang = Tmp.v1.angle();
//            if (enable_isoInput) {
//                Tmp.v1.trns(ang - 90, 0, -3);
//            } else {
//                Tmp.v1.trns(ang - 90, disableAlternate ? 0 : (width * Mathf.sign(left) ), length + Mathf.range(lengthRand));
//            }
//
//            update(shooter, shooter.getX() + Tmp.v1.x, shooter.getY() + Tmp.v1.y, Angles.angle(shooter.getX() + Tmp.v1.x, shooter.getY() + Tmp.v1.y, cx, cy), left);
//        }

        for(boolean left : Mathf.booleans){
            if (disableAlternate) {   // zones add code
                left = true;
            }
//            Tmp.v1.set(pointerX, pointerY).sub(shooter.getX(), shooter.getY());
//            if(Tmp.v1.len() < minPlayerDist) Tmp.v1.setLength(minPlayerDist);
//
//            float cx = Tmp.v1.x + shooter.getX(), cy = Tmp.v1.y + shooter.getY();
//
//            float ang = Tmp.v1.angle();
//            Tmp.v1.trns(ang - 90, disableAlternate ? 0 : (width * Mathf.sign(left) ), length + Mathf.range(lengthRand));

            {   // test data

            }
            update(shooter, target.getX(), target.getY(), 0, left);
        }
    }

    /** 装填时间百分比*/
    public float getReloadPerl(ShooterTrait player){
        return ( Mathf.clamp(player.getTimer().getTime(player.getShootTimer(false)) / (reload / 2)) );
    }
    // zones add end
}
