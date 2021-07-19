package mindustry.game;

import arc.util.ArcAnnotate.*;
import mindustry.core.GameState.State;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.traits.BuilderTrait;
import mindustry.entities.type.*;
import mindustry.entities.units.*;
import mindustry.type.*;
import mindustry.world.Tile;

/**
 *  事件类型
 * */
public class EventType{

    //常用事件. events that occur very often
    public enum Trigger{
        /** 冲击*/
        shock,
        /** 相位偏转击中*/
        phaseDeflectHit,
        /** 冲击功率*/
        impactPower,
        /** 钍反应堆过热*/
        thoriumReactorOverheat,
        /** 物品发射*/
        itemLaunch,
        /** 灭火*/
        fireExtinguish,
        /** 新游戏*/
        newGame,
        /** 教程完成*/
        tutorialComplete,
        /** 火焰弹药*/
        flameAmmo,
        /** 炮塔冷却*/
        turretCool,
        /** 启用像素化*/
        enablePixelation,
        /** 淹没*/
        drown,
        /** 排斥死亡*/
        exclusionDeath,
        /** 自杀式炸弹*/
        suicideBomb,
        /** 打开维基*/
        openWiki,
        /** 队伍核心伤害*/
        teamCoreDamage,
        /** 插件配置更改*/
        socketConfigChanged,
        /** 更新*/
        update
    }

    /** 胜利事件*/
    public static class WinEvent{}

    /** 失败事件*/
    public static class LoseEvent{}

    /** 发射事件*/
    public static class LaunchEvent{}

    /** 发射物品事件*/
    public static class LaunchItemEvent{
        public final ItemStack stack;

        public LaunchItemEvent(Item item, int amount){
            this.stack = new ItemStack(item, amount);
        }
    }

    /** 地图创建事件*/
    public static class MapMakeEvent{}

    /** 地图发布事件*/
    public static class MapPublishEvent{}

    /** 指挥官命令事件*/
    public static class CommandIssueEvent{
        public final Tile tile;
        public final UnitCommand command;

        public CommandIssueEvent(Tile tile, UnitCommand command){
            this.tile = tile;
            this.command = command;
        }
    }

    /** 玩家聊天事件*/
    public static class PlayerChatEvent{
        public final Player player;
        public final String message;

        public PlayerChatEvent(Player player, String message){
            this.player = player;
            this.message = message;
        }
    }

    /** 当一个地图的需求满足时调用.<p/>Called when a zone's requirements are met. */
    public static class ZoneRequireCompleteEvent{
        public final Zone zoneMet, zoneForMet;
        public final Objective objective;

        public ZoneRequireCompleteEvent(Zone zoneMet, Zone zoneForMet, Objective objective){
            this.zoneMet = zoneMet;
            this.zoneForMet = zoneForMet;
            this.objective = objective;
        }
    }

    /** 当一个地图的需求得到满足时调用.<p/>Called when a zone's requirements are met. */
    public static class ZoneConfigureCompleteEvent{
        public final Zone zone;

        public ZoneConfigureCompleteEvent(Zone zone){
            this.zone = zone;
        }
    }

    /** 客户端创建事件*/
    public static class ClientCreateEvent{

    }

    /** 当客户端游戏第一次加载时调用.<p/>Called when the client game is first loaded. */
    public static class ClientLoadEvent{

    }

    /** 客户端成功连接事件*/
    public static class ClientConnectOverEvent{

    }

    /** 服务器加载事件*/
    public static class ServerLoadEvent{

    }

    /** 内容重新加载事件*/
    public static class ContentReloadEvent{

    }

    /** 配置事件*/
    public static class DisposeEvent{

    }

    /** 开始事件*/
    public static class PlayEvent{

    }

    /** 重置事件*/
    public static class ResetEvent{

    }

    /** 回合事件*/
    public static class WaveEvent{

    }

    /** 当玩家放置一行, 在移动端或桌面时调用.<p/>Called when the player places a line, mobile or desktop.*/
    public static class LineConfirmEvent{

    }

    /** 当一个炮塔接收弹药时, 但只在教程激活的状态调用.<p/>Called when a turret recieves ammo, but only when the tutorial is active! */
    public static class TurretAmmoDeliverEvent{

    }

    /**  当一个炮塔接收弹药时, 但只在教程激活的状态调用.<p/>Called when a core recieves ammo, but only when the tutorial is active! */
    public static class CoreItemDeliverEvent{

    }

    /** 玩家打开块的描述信息时调用.<p/>Called when the player opens info for a specific block.*/
    public static class BlockInfoEvent{

    }

    /** 当玩家从一个块带走物品时调用.<p/>Called when the player withdraws items from a block. */
    public static class WithdrawEvent{
        public final Tile tile;
        public final Player player;
        public final Item item;
        public final int amount;

        public WithdrawEvent(Tile tile, Player player, Item item, int amount){
            this.tile = tile;
            this.player = player;
            this.item = item;
            this.amount = amount;
        }
    }

    /** 当玩家将物品存入一个块时调用.<p/>Called when a player deposits items to a block.*/
    public static class DepositEvent{
        public final Tile tile;
        public final Player player;
        public final Item item;
        public final int amount;

        public DepositEvent(Tile tile, Player player, Item item, int amount){
            this.tile = tile;
            this.player = player;
            this.item = item;
            this.amount = amount;
        }
    }

    /** 当玩家点击一个块时调用.<p/>Called when the player taps a block. */
    public static class TapEvent{
        public final Tile tile;
        public final Player player;

        public TapEvent(Tile tile, Player player){
            this.tile = tile;
            this.player = player;
        }
    }

    /** 当玩家设置一个特定的块时调用.<p/>Called when the player sets a specific block. */
    public static class TapConfigEvent{
        public final Tile tile;
        public final Player player;
        public final int value;

        public TapConfigEvent(Tile tile, Player player, int value){
            this.tile = tile;
            this.player = player;
            this.value = value;
        }
    }

    /** 游戏结束调用*/
    public static class GameOverEvent{
        public final Team winner;

        public GameOverEvent(Team winner){
            this.winner = winner;
        }
    }

    /** 当程序加载world加载时调用.<p/>Called when a game begins and the world is loaded. */
    public static class WorldLoadEvent{

    }

    /** Tile内容变更调用, 逻辑线程调用, 不要在绘制线程调用.<p/>Called from the logic thread. Do not access graphics here! */
    public static class TileChangeEvent{
        public final Tile tile;

        public TileChangeEvent(Tile tile){
            this.tile = tile;
        }
    }

    /** 游戏状态改变调用(PS: 暂停)*/
    public static class StateChangeEvent{
        public final State from, to;

        public StateChangeEvent(State from, State to){
            this.from = from;
            this.to = to;
        }
    }

    /** Content解锁调用*/
    public static class UnlockEvent{
        public final UnlockableContent content;

        public UnlockEvent(UnlockableContent content){
            this.content = content;
        }
    }

    /** 研究解锁块时调用*/
    public static class ResearchEvent{
        public final UnlockableContent content;

        public ResearchEvent(UnlockableContent content){
            this.content = content;
        }
    }

    /** 当块放置开始建造时调用.<p/>
     * Called when block building begins by placing down the BuildBlock.
     * The tile's block will nearly always be a BuildBlock.
     */
    public static class BlockBuildBeginEvent{
        public final Tile tile;
        public final Team team;
        public final boolean breaking;

        public BlockBuildBeginEvent(Tile tile, Team team, boolean breaking){
            this.tile = tile;
            this.team = team;
            this.breaking = breaking;
        }
    }

    /** 放置块建造结束调用*/
    public static class BlockBuildEndEvent{
        public final Tile tile;
        public final Team team;
        public final @Nullable
        Player player;
        public final boolean breaking;

        public BlockBuildEndEvent(Tile tile, @Nullable Player player, Team team, boolean breaking){
            this.tile = tile;
            this.team = team;
            this.player = player;
            this.breaking = breaking;
        }
    }

    /** 当玩家或无人机开始建造时调用.<p/>
     * Called when a player or drone begins building something.
     * This does not necessarily happen when a new BuildBlock is created.
     */
    public static class BuildSelectEvent{
        public final Tile tile;
        public final Team team;
        public final BuilderTrait builder;
        public final boolean breaking;

        public BuildSelectEvent(Tile tile, Team team, BuilderTrait builder, boolean breaking){
            this.tile = tile;
            this.team = team;
            this.builder = builder;
            this.breaking = breaking;
        }
    }

    /** 在一个块被销毁之前调用.<p/>Called right before a block is destroyed.
     * The tile entity of the tile in this event cannot be null when this happens.*/
    public static class BlockDestroyEvent{
        public final Tile tile;

        public BlockDestroyEvent(Tile tile){
            this.tile = tile;
        }
    }

    /** 单位销毁时调用*/
    public static class UnitDestroyEvent{
        public final Unit unit;

        public UnitDestroyEvent(Unit unit){
            this.unit = unit;
        }
    }

    /** 单位创建时调用*/
    public static class UnitCreateEvent{
        public final BaseUnit unit;

        public UnitCreateEvent(BaseUnit unit){
            this.unit = unit;
        }
    }

    /** 重置显示尺寸时调用*/
    public static class ResizeEvent{

    }

    /** 玩家变更机甲时调用*/
    public static class MechChangeEvent{
        public final Player player;
        public final Mech mech;

        public MechChangeEvent(Player player, Mech mech){
            this.player = player;
            this.mech = mech;
        }
    }

    /** 连接中调用, 当玩家接收完world数据准备游戏.<p/>Called after connecting; when a player recieves world data and is ready to play.*/
    public static class PlayerJoin{
        public final Player player;

        public PlayerJoin(Player player){
            this.player = player;
        }
    }

    /** 当玩家连接时调用, 但还没有加入游戏.<p/>Called when a player connects, but has not joined the game yet.*/
    public static class PlayerConnect{
        public final Player player;

        public PlayerConnect(Player player){
            this.player = player;
        }
    }

    /** 玩家离开游戏调用*/
    public static class PlayerLeave{
        public final Player player;

        public PlayerLeave(Player player){
            this.player = player;
        }
    }

    /** 当玩家被屏蔽后调用*/
    public static class PlayerBanEvent{
        public final Player player;

        public PlayerBanEvent(Player player){
            this.player = player;
        }
    }

    /** 当玩家接触屏蔽后调用*/
    public static class PlayerUnbanEvent{
        public final Player player;

        public PlayerUnbanEvent(Player player){
            this.player = player;
        }
    }

    /** 玩家IP被屏蔽后调用*/
    public static class PlayerIpBanEvent{
        public final String ip;


        public PlayerIpBanEvent(String ip){
            this.ip = ip;
        }
    }

    /** 玩家IP被解除屏蔽后调用*/
    public static class PlayerIpUnbanEvent{
        public final String ip;


        public PlayerIpUnbanEvent(String ip){
            this.ip = ip;
        }
    }


    // zones add event begon
    /** 地表系统被初始化之后调用.*/
    public static class GroundSystemInitEvent {
    }

    /** 配置文件加载完毕后调用. 清除游戏初始化加载缓存事件,  以及Content内容加载完毕后初始化Caesar数据*/
    public static class ClearCacheEvent {
    }
    // zones add event end
}

