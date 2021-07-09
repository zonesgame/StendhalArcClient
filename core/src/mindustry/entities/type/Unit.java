package mindustry.entities.type;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Scl;
import arc.struct.Array;
import arc.util.Align;
import arc.util.ArcAnnotate.Nullable;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.content.Blocks;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.entities.Damage;
import mindustry.entities.Effects;
import mindustry.entities.effect.ScorchDecal;
import mindustry.entities.traits.DamageTrait;
import mindustry.entities.traits.DrawTrait;
import mindustry.entities.traits.SaveTrait;
import mindustry.entities.traits.SolidTrait;
import mindustry.entities.traits.SyncTrait;
import mindustry.entities.traits.TargetTrait;
import mindustry.entities.traits.TeamTrait;
import mindustry.entities.units.Statuses;
import mindustry.game.EventType.Trigger;
import mindustry.game.EventType.UnitDestroyEvent;
import mindustry.game.Team;
import mindustry.gen.Sounds;
import mindustry.graphics.Pal;
import mindustry.net.Interpolator;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.StatusEffect;
import mindustry.type.Weapon;
import mindustry.ui.Cicon;
import mindustry.ui.Fonts;
import mindustry.world.Pos;
import mindustry.world.Tile;
import mindustry.world.blocks.Floor;

import static mindustry.Vars.content;
import static mindustry.Vars.finalWorldBounds;
import static mindustry.Vars.itemSize;
import static mindustry.Vars.net;
import static mindustry.Vars.player;
import static mindustry.Vars.playerGroup;
import static mindustry.Vars.renderer;
import static mindustry.Vars.spawner;
import static mindustry.Vars.state;
import static mindustry.Vars.tilesize;
import static mindustry.Vars.tileunit;
import static mindustry.Vars.unitGroup;
import static mindustry.Vars.world;
import static mindustry.Vars.worldBounds;
import static z.debug.ZDebug.disable_avoidOthers;
import static z.debug.ZDebug.disable_lifebar;
import static z.debug.ZDebug.disable_mindustryPlayer;
import static z.debug.ZDebug.disable_spawnslimit;
import static z.debug.ZDebug.disable_unitOutboundDead;
import static z.debug.ZDebug.disable_worldBoundCheck;
import static z.debug.ZDebug.enable_isoInput;

/**
 *  游戏内实体单位扩展类
 * */
public abstract class Unit extends DestructibleEntity implements SaveTrait, TargetTrait, SyncTrait, DrawTrait, TeamTrait{
    /** 撞击闪烁持续时间.<p/>Total duration of hit flash effect */
    public static final float hitDuration = 9f;
    /** 速度百分比相对与瓦砾块, 比如值2为 1/2=0.5个单位块.<p/>Percision divisor of velocity, used when writing. For example a value of '2' would mean the percision is 1/2 = 0.5-size chunks. */
    public static final float velocityPercision = 8f;
    /** 最大速度向量的绝对值.<p/>Maximum absolute value of a velocity vector component. */
    public static final float maxAbsVelocity = 127f / velocityPercision;
    /** 重生点无*/
    public static final int noSpawner = Pos.get(-1, 1);

    /** 移动向量*/
    private static final Vec2 moveVector = new Vec2();

    /** 角度*/
    public float rotation;

    /***/
    protected final Interpolator interpolator = new Interpolator();
    /** 状态管理器*/
    protected final Statuses status = new Statuses();
    /** 物品堆栈*/
    protected final ItemStack item = new ItemStack(content.item(0), 0);

    /** 队伍*/
    protected Team team = Team.sharded;
    /** 淹没时间*/
    protected float drownTime, /** 撞击时间*/hitTime;

    @Override
    public boolean collidesGrid(int x, int y){
        return !isFlying();
    }

    @Override
    public Team getTeam(){
        return team;
    }

    @Override
    public void interpolate(){
        interpolator.update();

        x = interpolator.pos.x;
        y = interpolator.pos.y;

        if(interpolator.values.length > 0){
            rotation = interpolator.values[0];
        }
    }

    @Override
    public Interpolator getInterpolator(){
        return interpolator;
    }

    @Override
    public void damage(float amount){
        if(!net.client()){
            super.damage(calculateDamage(amount));
        }
        hitTime = hitDuration;
    }

    @Override
    public boolean collides(SolidTrait other){
        if(isDead()) return false;

        if(other instanceof DamageTrait){
            return other instanceof TeamTrait && (((TeamTrait)other).getTeam()).isEnemy(team);
        }else{
            return other instanceof Unit && ((Unit)other).isFlying() == isFlying();
        }
    }

    @Override
    public void onDeath(){
        float explosiveness = 2f + item.item.explosiveness * item.amount;
        float flammability = item.item.flammability * item.amount;
        Damage.dynamicExplosion(x, y, flammability, explosiveness, 0f, getSize() / 2f, Pal.darkFlame);        // zones editor

        ScorchDecal.create(x, y);     // zones editor
        Effects.effect(Fx.explosion, this);
        Effects.shake(2f, 2f, this);

        if (enable_isoInput)
            Sounds.bang.at(wpos);
        else
            Sounds.bang.at(this);
        item.amount = 0;
        drownTime = 0f;
        status.clear();
        Events.fire(new UnitDestroyEvent(this));

        if(explosiveness > 7f && this == player){
            Events.fire(Trigger.suicideBomb);
        }
    }

    @Override
    public Vec2 velocity(){
        return velocity;
    }

    @Override
    public void move(float x, float y){
        if(!isFlying()){
            super.move(x, y);
        }else{
            moveBy(x, y);
        }
    }

    @Override
    public boolean isValid(){
        return !isDead() && isAdded();
    }

    @Override
    public void writeSave(DataOutput stream) throws IOException{
        writeSave(stream, false);
    }

    @Override
    public void readSave(DataInput stream, byte version) throws IOException{
        byte team = stream.readByte();
        boolean dead = stream.readBoolean();
        float x = stream.readFloat();
        float y = stream.readFloat();
        byte xv = stream.readByte();
        byte yv = stream.readByte();
        float rotation = stream.readShort() / 2f;
        int health = stream.readShort();
        byte itemID = stream.readByte();
        short itemAmount = stream.readShort();

        this.status.readSave(stream, version);
        this.item.amount = itemAmount;
        this.item.item = content.item(itemID);
        this.dead = dead;
        this.team = Team.get(team);
        this.health = health;
        this.x = x;
        this.y = y;
        this.velocity.set(xv / velocityPercision, yv / velocityPercision);
        this.rotation = rotation;
    }

    public void writeSave(DataOutput stream, boolean net) throws IOException{
        if(item.item == null) item.item = Items.copper;

        stream.writeByte(team.id);
        stream.writeBoolean(isDead());
        stream.writeFloat(net ? interpolator.target.x : x);
        stream.writeFloat(net ? interpolator.target.y : y);
        stream.writeByte((byte)(Mathf.clamp(velocity.x, -maxAbsVelocity, maxAbsVelocity) * velocityPercision));
        stream.writeByte((byte)(Mathf.clamp(velocity.y, -maxAbsVelocity, maxAbsVelocity) * velocityPercision));
        stream.writeShort((short)(rotation * 2));
        stream.writeShort((short)health);
        stream.writeByte(item.item.id);
        stream.writeShort((short)item.amount);
        status.writeSave(stream);
    }

    /** 限制单位位置在地图范围内*/
    protected void clampPosition(){
        if (enable_isoInput) {
            x = Mathf.clamp(x, 0, world.width() - tileunit);
            y = Mathf.clamp(y, 0, world.height() - tileunit);
        } else {
            x = Mathf.clamp(x, 0, world.width() * tilesize - tilesize);
            y = Mathf.clamp(y, 0, world.height() * tilesize - tilesize);
        }
    }

    /** 是否免疫状态*/
    public boolean isImmune(StatusEffect effect){
        return false;
    }

    /** 是否在地图边界之外*/
    public boolean isOutOfBounds(){
        if (enable_isoInput)
            return x < -worldBounds || y < -worldBounds || x > world.width() + worldBounds || y > world.height() + worldBounds;
        else
            return x < -worldBounds || y < -worldBounds || x > world.width() * tilesize + worldBounds || y > world.height() * tilesize + worldBounds;
    }

    /** 计算伤害*/
    public float calculateDamage(float amount){
        return amount * Mathf.clamp(1f - status.getArmorMultiplier() / 100f);
    }

    /** 伤害倍数*/
    public float getDamageMultipler(){
        return status.getDamageMultiplier();
    }

    /** 是否包含指定状态效果*/
    public boolean hasEffect(StatusEffect effect){
        return status.hasEffect(effect);
    }

    /** 规避其它单位*/
    public void avoidOthers(){
        if ( disable_avoidOthers)
            return;
        float radScl = 1.5f;
        float fsize = getSize() / radScl;
        moveVector.setZero();
        float cx = x - fsize/2f, cy = y - fsize/2f;
        avoid(unitGroup.intersect(cx, cy, fsize, fsize));
        if(!(this instanceof Player) && !disable_mindustryPlayer){
            avoid(playerGroup.intersect(cx, cy, fsize, fsize));
        }
        velocity.add(moveVector.x / mass() * Time.delta(), moveVector.y / mass() * Time.delta());
    }

    /** 规避指定其它单位*/
    private void avoid(Array<? extends Unit> arr){
        float radScl = 1.5f;

        for(Unit en : arr){
            if(en.isFlying() != isFlying() || (en instanceof Player && en.getTeam() != getTeam()) || (this instanceof Player && en.isFlying())) continue;
            float dst = dst(en);
            float scl = Mathf.clamp(1f - dst / (getSize()/(radScl*2f) + en.getSize()/(radScl*2f)));
            moveVector.add(Tmp.v1.set((x - en.x) * scl, (y - en.y) * scl).limit(0.4f));
        }
    }

    /** 获取临近队伍核心*/
    public @Nullable TileEntity getClosestCore(){
        return state.teams.closestCore(x, y, team);     // 世界坐标 原版 x y    zones editor
    }

    /** 获取单位所在地板Block*/
    public Floor getFloorOn(){
        if (enable_isoInput) {
            Tile tile = world.tile((int) x, (int) y);
            return tile == null ? (Floor)Blocks.air : tile.floor();
        }
        Tile tile = world.tileWorld(x, y);
        return tile == null ? (Floor)Blocks.air : tile.floor();
    }

    /** 获取所在的瓦砾*/
    public @Nullable Tile tileOn(){
        if (enable_isoInput) {
            return world.tile( x, y);
        }
        return world.tileWorld(x, y);
    }

    /** 指定位置重生*/
    public void onRespawn(Tile tile){
    }

    /** 更新速度和状态效果.<p/>Updates velocity and status effects. */
    public void updateVelocityStatus(){
        Floor floor = getFloorOn();     // 获取单位所在块

        Tile tile = world.tileWorld(x, y);  // 获取单位所在瓦砾
        if (enable_isoInput) {
            tile = world.tile( x, y);
        }

        status.update(this);    // 更新状态数据
        item.amount = Mathf.clamp(this.item.amount, 0, getItemCapacity());  // 更新物品数量

        velocity.limit(maxVelocity()).scl(1f + (status.getSpeedMultiplier() - 1f) * Time.delta());      // 限制单位最大速度

        if(x < -finalWorldBounds || y < -finalWorldBounds || x >= world.width() * tilesize + finalWorldBounds || y >= world.height() * tilesize + finalWorldBounds){
            if ( !disable_unitOutboundDead)kill();
        }   // 超出地图边界移除单位

        //在敌人出生点应用作用力效果. apply knockback based on spawns
        if(getTeam() != state.rules.waveTeam){
            float relativeSize = state.rules.dropZoneRadius + getSize()/2f + 1f;
            for(Tile spawn : spawner.getGroundSpawns()){
                if(withinDst(spawn.worldx(), spawn.worldy(), relativeSize)){        // 出生点范围内单位施加作用力
                    if( !disable_spawnslimit) velocity.add(Tmp.v1.set(this).sub(spawn.worldx(), spawn.worldy()).setLength(0.1f + 1f - dst(spawn) / relativeSize).scl(0.45f * Time.delta()));
                }
            }
        }

        //单位能超出世界边界的范围. repel player out of bounds
        final float warpDst = 180f;
        if ( !disable_worldBoundCheck) {
            if(x < 0) velocity.x += (-x/warpDst);
            if(y < 0) velocity.y += (-y/warpDst);
            if(x > world.unitWidth()) velocity.x -= (x - world.unitWidth())/warpDst;
            if(y > world.unitHeight()) velocity.y -= (y - world.unitHeight())/warpDst;
        }


        if(isFlying()){     // 飞行状态
            drownTime = 0f;
            move(velocity.x * Time.delta(), velocity.y * Time.delta());
        }else{      // 地面移动状态
            boolean onLiquid = floor.isLiquid;  // 是否在流体表面

            if(tile != null){
                tile.block().unitOn(tile, this);
                if(tile.block() != Blocks.air){
                    onLiquid = false;
                }
            }

            if(onLiquid && velocity.len() > 0.4f && Mathf.chance((velocity.len() * floor.speedMultiplier) * 0.06f * Time.delta())){     // 添加流体行走效果
                Effects.effect(floor.walkEffect, floor.color, x, y);        // zones editor
            }

            if(onLiquid){   // 应用流体效果和伤害
                status.handleApply(this, floor.status, floor.statusDuration);

                if(floor.damageTaken > 0f){
                    damagePeriodic(floor.damageTaken);
                }
            }

            if(onLiquid && floor.drownTime > 0){
                drownTime += Time.delta() * 1f / floor.drownTime;
                if(Mathf.chance(Time.delta() * 0.05f)){
                    Effects.effect(floor.drownUpdateEffect, floor.color, x, y);         // zones edirot
                }
            }else{
                drownTime = Mathf.lerpDelta(drownTime, 0f, 0.03f);
            }

            drownTime = Mathf.clamp(drownTime);

            if(drownTime >= 0.999f && !net.client()){       // 执行单位淹没死亡并添加效果
                damage(health + 1);
                if(this == player){
                    Events.fire(Trigger.drown);
                }
            }

            float px = x, py = y;
            move(velocity.x * floor.speedMultiplier * Time.delta(), velocity.y * floor.speedMultiplier * Time.delta());     // 执行移动处理
            if(Math.abs(px - x) <= 0.0001f) velocity.x = 0f;
            if(Math.abs(py - y) <= 0.0001f) velocity.y = 0f;
        }

        velocity.scl(Mathf.clamp(1f - drag() * (isFlying() ? 1f : floor.dragMultiplier) * Time.delta()));
    }

    /** 是否接收指定物品*/
    public boolean acceptsItem(Item item){
        return this.item.amount <= 0 || (this.item.item == item && this.item.amount <= getItemCapacity());
    }

    /** 添加物品*/
    public void addItem(Item item){
        addItem(item, 1);
    }

    /** 添加物品*/
    public void addItem(Item item, int amount){
        this.item.amount = this.item.item == item ? this.item.amount + amount : amount;
        this.item.item = item;
        this.item.amount = Mathf.clamp(this.item.amount, 0, getItemCapacity());
    }

    /** 清除物品*/
    public void clearItem(){
        item.amount = 0;
    }

    /** 获取物品堆栈*/
    public ItemStack item(){
        return item;
    }

    /** 可以接收指定物品最大数量*/
    public int maxAccepted(Item item){
        return this.item.item != item && this.item.amount > 0 ? 0 : getItemCapacity() - this.item.amount;
    }

    /** 应用指定效果*/
    public void applyEffect(StatusEffect effect, float duration){
        if(dead || net.client()) return; //effects are synced and thus not applied through clients
        status.handleApply(this, effect, duration);
    }

    /** 周期性伤害*/
    public void damagePeriodic(float amount){
        damage(amount * Time.delta(), hitTime <= -20 + hitDuration);
    }

    /** 伤害*/
    public void damage(float amount, boolean withEffect){
        float pre = hitTime;

        damage(amount);

        if(!withEffect){
            hitTime = pre;
        }
    }

    /** 绘制下层*/
    public void drawUnder(){
    }

    /** 绘制上层*/
    public void drawOver(){
    }

    /** 绘制状态*/
    public void drawStats(){
        // zones add begon
        float x = this.x;
        float y = this.y;
        if (enable_isoInput) {
            x = wpos.x;
            y = wpos.y;
        }
        // zones add end
        if ( !disable_lifebar) {
            Draw.color(Color.black, team.color, healthf() + Mathf.absin(Time.time(), Math.max(healthf() * 5f, 1f), 1f - healthf()));
            Draw.rect(getPowerCellRegion(), x, y, rotation - 90);       // zones editor
            Draw.color();
        }

        drawBackItems(item.amount > 0 ? 1f : 0f, false);

        drawLight();
    }

    /** 绘制光源*/
    public void drawLight(){
        // zones add begon
        float x = this.x;
        float y = this.y;
        if (enable_isoInput) {
            x = wpos.x;
            y = wpos.y;
        }
        // zones add end
        renderer.lights.add(x, y, 50f, Pal.powerLight, 0.6f);       // zones editor
    }

    /** 绘制背部物品*/
    public void drawBackItems(float itemtime, boolean number){
        // zones add begon
        float x = this.x;
        float y = this.y;
        if (enable_isoInput) {
            x = wpos.x;
            y = wpos.y;
        }
        // zones add end
        //draw back items
        if(itemtime > 0.01f && item.item != null){
            float backTrns = 5f;
            float size = (itemSize + Mathf.absin(Time.time(), 5f, 1f)) * itemtime;

            Draw.mixcol(Pal.accent, Mathf.absin(Time.time(), 5f, 0.5f));
            Draw.rect(item.item.icon(Cicon.medium),
                x + Angles.trnsx(rotation + 180f, backTrns),
                y + Angles.trnsy(rotation + 180f, backTrns),
                size, size, rotation);      // zones editor

            Draw.mixcol();

            Lines.stroke(1f, Pal.accent);
            Lines.circle(
                x + Angles.trnsx(rotation + 180f, backTrns),
                y + Angles.trnsy(rotation + 180f, backTrns),
                (3f + Mathf.absin(Time.time(), 5f, 1f)) * itemtime);        // zones editor

            if(number){
                Fonts.outline.draw(item.amount + "",
                    x + Angles.trnsx(rotation + 180f, backTrns),
                    y + Angles.trnsy(rotation + 180f, backTrns) - 3,
                    Pal.accent, 0.25f * itemtime / Scl.scl(1f), false, Align.center
                );      // zones editor
            }
        }

        Draw.reset();
    }

    /** 获取电力块纹理*/
    public TextureRegion getPowerCellRegion(){
        return Core.atlas.find("power-cell");
    }

    /** 绘制全部*/
    public void drawAll(){
        if(!isDead()){
            {   // zones add code 更新绘制坐标位置
                if (enable_isoInput)
                    drawPosition();
            }
            draw();
            drawStats();
        }
    }

    /** 飞行单位, 绘制阴影*/
    public void drawShadow(float offsetX, float offsetY){
        // zones add begon
        float x = this.x;
        float y = this.y;
        float rotation = this.rotation;
        if (enable_isoInput) {
            x = wpos.x;
            y = wpos.y;
            rotation = (360 - this.rotation + 45);
        }
        // zones add end
        Draw.rect(getIconRegion(), x + offsetX, y + offsetY, rotation - 90);        // zones edirot
    }

    /** 获取单位撞击的最大尺寸*/
    public float getSize(){
        hitbox(Tmp.r1);
        return Math.max(Tmp.r1.width, Tmp.r1.height) * 2f;
    }

    /** 图标纹理*/
    public abstract TextureRegion getIconRegion();

    /** 武器*/
    public abstract Weapon getWeapon();

    /** 物品背包*/
    public abstract int getItemCapacity();

    /** 质量*/
    public abstract float mass();

    /** 飞行状态*/
    public abstract boolean isFlying();
}
