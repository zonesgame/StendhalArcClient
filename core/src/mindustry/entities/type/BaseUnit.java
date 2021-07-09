package mindustry.entities.type;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import arc.Core;
import arc.Events;
import arc.ai.fsm.DefaultStateMachine;
import arc.ai.fsm.State;
import arc.ai.fsm.StateMachine;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Rect;
import arc.util.ArcAnnotate.Nullable;
import arc.util.Interval;
import arc.util.Time;
import arc.z.util.ZonesAnnotate;
import mindustry.Vars;
import mindustry.annotations.Annotations.Loc;
import mindustry.annotations.Annotations.Remote;
import mindustry.content.StatusEffects;
import mindustry.ctype.ContentType;
import mindustry.entities.EntityGroup;
import mindustry.entities.Units;
import mindustry.entities.traits.ShooterTrait;
import mindustry.entities.traits.SolidTrait;
import mindustry.entities.traits.TargetTrait;
import mindustry.entities.units.UnitCommand;
import mindustry.entities.units.UnitDrops;
import mindustry.entities.units.UnitState;
import mindustry.game.EventType.Trigger;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.type.StatusEffect;
import mindustry.type.TypeID;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import mindustry.ui.Cicon;
import mindustry.world.Tile;
import mindustry.world.blocks.BuildBlock;
import mindustry.world.blocks.defense.DeflectorWall.DeflectorEntity;
import mindustry.world.blocks.units.CommandCenter.CommandCenterEntity;
import mindustry.world.blocks.units.UnitFactory.UnitFactoryEntity;
import mindustry.world.meta.BlockFlag;
import z.ai.components.Squad;
import z.ai.utils.SquadMember;
import z.debug.ZDebug;
import z.utils.ShapeRenderer;
import z.world.blocks.units.BarracksIso;

import static mindustry.Vars.content;
import static mindustry.Vars.indexer;
import static mindustry.Vars.net;
import static mindustry.Vars.player;
import static mindustry.Vars.unitGroup;
import static mindustry.Vars.world;
import static z.debug.ZDebug.enable_drawUnitCollision;

/**
 *  AI单位的基础类<p/>
 * Base class for AI units.
 * */
public abstract class BaseUnit extends Unit implements ShooterTrait{
    /** 索引计数器*/
    protected static int timerIndex = 0;

    /** 目标1计数器*/
    protected static final int timerTarget = timerIndex++;
    /** 目标2计数器*/
    protected static final int timerTarget2 = timerIndex++;
    /** 左手射击计数器*/
    protected static final int timerShootLeft = timerIndex++;
    /** 右手射击计数器*/
    protected static final int timerShootRight = timerIndex++;

    /** 装填状态*/
    protected boolean loaded;
    /** 单位类型*/
    public UnitType type;        // default protected
    /** 间隔计时器*/
    protected Interval timer = new Interval(5);
    /** 单位状态机*/
    protected StateMachine<? extends BaseUnit, State<? extends BaseUnit>> state = new DefaultStateMachine();     // defalut  new StateMachine();
    /** 单位目标*/
    public TargetTrait target;       // default protected

    /** 单位重生点*/
    protected int spawner = noSpawner;

    /** 用于序列化的构造函数, 不要使用!<p/>internal constructor used for deserialization, DO NOT USE */
    public BaseUnit(){
    }

    /** 单位死亡回调*/
    @Remote(called = Loc.server)
    public static void onUnitDeath(BaseUnit unit){
        if(unit == null) return;

        if(net.server() || !net.active()){
            UnitDrops.dropItems(unit);
        }

        unit.onSuperDeath();
        unit.type.deathSound.at(unit);

        //处理视觉效果. visual only.
        if(net.client()){
            Tile tile = world.tile(unit.spawner);
            if(tile != null){
                tile.block().unitRemoved(tile, unit);
            }

            unit.spawner = noSpawner;
        }

        //must run afterwards so the unit's group is not null when sending the removal packet
        Core.app.post(unit::remove);
    }

    @Override
    public float drag(){
        return type.drag;
    }

    @Override
    public TypeID getTypeID(){
        return type.typeID;
    }

    @Override
    public void onHit(SolidTrait entity){
        if(entity instanceof Bullet && ((Bullet)entity).getOwner() instanceof DeflectorEntity && player != null && getTeam() != player.getTeam()){
            Core.app.post(() -> {
                if(isDead()){
                    Events.fire(Trigger.phaseDeflectHit);
                }
            });
        }
    }

    /** 获取重生瓦砾*/
    public @Nullable Tile getSpawner(){
        return world.tile(spawner);
    }

    /** 是否包含指挥中心命令*/
    public boolean isCommanded(){
        return indexer.getAllied(team, BlockFlag.comandCenter).size != 0 && indexer.getAllied(team, BlockFlag.comandCenter).first().entity instanceof CommandCenterEntity;
    }

    /** 获取指挥中心命令*/
    public @Nullable UnitCommand getCommand(){
        if(isCommanded()){
            return indexer.getAllied(team, BlockFlag.comandCenter).first().<CommandCenterEntity>ent().command;
        }
        return null;
    }

    /** 接收到指挥中心的命令时回掉.<p/>Called when a command is recieved from the command center.*/
    public void onCommand(UnitCommand command){

    }

    /** 数据序列化需要, 替代构造方法, 仅执行一次.<p/>Initialize the type and team of this unit. Only call once! */
    public void init(UnitType type, Team team){
        if(this.type != null) throw new RuntimeException("This unit is already initialized!");

        this.type = type;
        this.team = team;
        {       // 状态机初始化
            ((DefaultStateMachine) state).setOwner(this);
        }
    }

    /** @return 单位是否在回合计数器中计算.<p/>whether this unit counts toward the enemy amount in the wave UI. */
    public boolean countsAsEnemy(){
        return true;
    }

    /** 单位类型*/
    public UnitType getType(){
        return type;
    }

    /** 设置重生点(防止Wave移除)*/
    public void setSpawner(Tile tile){
        this.spawner = tile.pos();
    }

    /** 转向*/
    public void rotate(float angle){
        rotation = Mathf.slerpDelta(rotation, angle, type.rotatespeed);
    }

    /** 不标点是否包含标记*/
    public boolean targetHasFlag(BlockFlag flag){
        return (target instanceof TileEntity && ((TileEntity)target).tile.block().flags.contains(flag)) ||
        (target instanceof Tile && ((Tile)target).block().flags.contains(flag));
    }

    /** 设置单位状态机状态*/
    public void setState(UnitState state){
        this.state.changeState(state);
    }

    /** 自定义状态切换*/
    @ZonesAnnotate.ZAdd
    public void setState(String stateKey){
    }

    /** 更新单位目标计数器*/
    public boolean retarget(){
        return timer.get(timerTarget, 20);
    }

    /** 指定单位target不为null时才执行.<p/>Only runs when the unit has a target. */
    public void behavior(){

    }

    /** 更新单位目标*/
    public void updateTargeting(){
        if(target == null || (target instanceof Unit && (target.isDead() || target.getTeam() == team))
        || (target instanceof TileEntity && ((TileEntity)target).tile.entity == null)){
            target = null;
        }
    }

    /** 获取临近的相同队伍的指定块*/
    public void targetClosestAllyFlag(BlockFlag flag){
        Tile target = Geometry.findClosest(x, y, indexer.getAllied(team, flag));
        if(target != null) this.target = target.entity;
    }

    /** 获取临近的敌人指定块*/
    public void targetClosestEnemyFlag(BlockFlag flag){
        Tile target = Geometry.findClosest(x, y, indexer.getEnemy(team, flag));
        if(target != null) this.target = target.entity;
    }

    /** 获取临近目标*/
    public void targetClosest(){
        TargetTrait newTarget = Units.closestTarget(team, x, y, Math.max(getWeapon().bullet.range(), type.range), u -> type.targetAir || !u.isFlying());
        if(newTarget != null){
            target = newTarget;
        }
    }

    /** 获取临近的指定同盟块的瓦砾*/
    public @Nullable Tile getClosest(BlockFlag flag){
        return Geometry.findClosest(x, y, indexer.getAllied(team, flag));
    }

    /** 获取临近出生点瓦砾*/
    public @Nullable Tile getClosestSpawner(){
        return Geometry.findClosest(x, y, Vars.spawner.getGroundSpawns());
    }

    /** 获取临近敌人核心块实体*/
    public @Nullable TileEntity getClosestEnemyCore(){
        return Vars.state.teams.closestEnemyCore(x, y, team);
    }

    /** 单位状态机起始状态*/
    public UnitState getStartState(){
        return null;
    }
    /** 单位全局状态*/
    @ZonesAnnotate.ZAdd
    public UnitState getGlobalState() {
        return null;
    }

    /** 是否为BOSS*/
    public boolean isBoss(){
        return hasEffect(StatusEffects.boss);
    }

    @Override
    public float getDamageMultipler(){
        return status.getDamageMultiplier() * Vars.state.rules.unitDamageMultiplier;
    }

    @Override
    public boolean isImmune(StatusEffect effect){
        return type.immunities.contains(effect);
    }

    @Override
    public boolean isValid(){
        return super.isValid() && isAdded();
    }

    @Override
    public Interval getTimer(){
        return timer;
    }

    @Override
    public int getShootTimer(boolean left){
        return left ? timerShootLeft : timerShootRight;
    }

    @Override
    public Weapon getWeapon(){
        return type.weapon;
    }

    @Override
    public TextureRegion getIconRegion(){
        return type.icon(Cicon.full);
    }

    @Override
    public int getItemCapacity(){
        return type.itemCapacity;
    }

    @Override
    public void interpolate(){
        super.interpolate();

        if(interpolator.values.length > 0){     // 网络数据流中插入角度
            rotation = interpolator.values[0];
        }
    }

    @Override
    public float maxHealth(){
        return type.health * Vars.state.rules.unitHealthMultiplier;
    }

    @Override
    public float mass(){
        return type.mass;
    }

    @Override
    public boolean isFlying(){
        return type.flying;
    }

    @Override
    public void update(){
        if(isDead()){       // 死亡状态移除单位
            //dead enemies should get immediately removed
            remove();
            return;
        }

        hitTime -= Time.delta();    // 更新撞击时间

        if(net.client()){
            interpolate();
            status.update(this);
            return;
        }

        if(!isFlying() && (world.tileWorld(x, y) != null && !(world.tileWorld(x, y).block() instanceof BuildBlock) && world.tileWorld(x, y).solid())){
            if ( !ZDebug.disableWaveKill)
                kill();
        }

        avoidOthers();      // 规避其它单位

        if(spawner != noSpawner && (world.tile(spawner) == null || (!(world.tile(spawner).entity instanceof UnitFactoryEntity)
                && !(world.tile(spawner).entity instanceof BarracksIso.BarracksIsoEntity))       // zones add code
        )){
            if ( !ZDebug.disableWaveKill)
                kill();
        }

        updateTargeting();      // 更新目标
        if(target != null) behavior();      // 更新单位目标行为     // zones add editor

        state.update();     // 状态更新
        updateVelocityStatus();     //  更新速度和状态效果

//        if(target != null) behavior();      // 更新单位目标行为       // zones editor

        if(!isFlying()){
            clampPosition();
        }
    }

    @Override
    public void draw(){
        if (enable_drawUnitCollision) {
            ShapeRenderer.drawDiamondUnit(x, y, type.hitsize);
            Draw.color(Color.yellow);
            ShapeRenderer.drawDiamondUnit(x, y, type.hitsizeTile);
            Draw.color();
        }
    }

    @Override
    public float maxVelocity(){
        return type.maxVelocity;
    }

    @Override
    public void removed(){
        super.removed();
        Tile tile = world.tile(spawner);
        if(tile != null && !net.client()){
            tile.block().unitRemoved(tile, this);
        }

        spawner = noSpawner;
        // zones add begon
//        getFormationTarget().getSquad().removeMember(this);
        // zones add end
    }

    @Override
    public float drawSize(){
        return type.hitsize * 10;
    }

    @Override
    public void onDeath(){
        Call.onUnitDeath(this);
    }

    @Override
    public void added(){
        state.setInitialState(getStartState());         // defualt set
        state.setGlobalState(getGlobalState());

        if(!loaded){
            health(maxHealth());
        }

        if(isCommanded()){
            onCommand(getCommand());
        }
    }

    @Override
    public void hitbox(Rect rect){
        rect.setSize(type.hitsize).setCenter(x, y);
    }

    @Override
    public void hitboxTile(Rect rect){
        rect.setSize(type.hitsizeTile).setCenter(x, y);
    }

    @Override
    public EntityGroup targetGroup(){
        return unitGroup;
    }

    @Override
    public byte version(){
        return 0;
    }

    @Override
    public void writeSave(DataOutput stream) throws IOException{
        super.writeSave(stream);
        stream.writeByte(type.id);
        stream.writeInt(spawner);
    }

    @Override
    public void readSave(DataInput stream, byte version) throws IOException{
        super.readSave(stream, version);
        loaded = true;
        byte type = stream.readByte();
        this.spawner = stream.readInt();

        this.type = content.getByID(ContentType.unit, type);
        add();
    }

    @Override
    public void write(DataOutput data) throws IOException{
        super.writeSave(data);
        data.writeByte(type.id);
        data.writeInt(spawner);
    }

    @Override
    public void read(DataInput data) throws IOException{
        float lastx = x, lasty = y, lastrot = rotation;

        super.readSave(data, version());

        this.type = content.getByID(ContentType.unit, data.readByte());
        this.spawner = data.readInt();

        interpolator.read(lastx, lasty, x, y, rotation);
        rotation = lastrot;
        x = lastx;
        y = lasty;
    }

    /** 调用父类死亡方法*/
    public void onSuperDeath(){
        super.onDeath();
    }


    // zones add begon
    public SquadMember squadMember = new SquadMember();

    /** 获取单位绑定队伍*/
    public Squad squad() {
        return squadMember.getSquad();
    }
//    public SquadMember getFormationTarget() {
//        return memberSquad;
//    }
    // zones add end
}
