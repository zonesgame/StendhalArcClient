package mindustry.game;

import mindustry.annotations.Annotations.*;
import arc.struct.*;
import arc.graphics.*;
import mindustry.content.*;
import mindustry.io.*;
import mindustry.type.*;
import mindustry.world.*;

/**
 *  定义游戏应该如何运行的当前规则.
 *  不存储游戏状态,只是配置. <p/>
 * Defines current rules on how the game should function.
 * Does not store game state, just configuration.
 */
@Serialize
public class Rules{
    /** 玩家是否有无限的资源.<p/>Whether the player has infinite resources. */
    public boolean infiniteResources;
    /** 是否自动显示计时器. 如果不是,当播放按钮被按下时, 回合计时器就会出现.<p/>Whether the waves come automatically on a timer. If not, waves come when the play button is pressed. */
    public boolean waveTimer = true;
    /** <p/>Whether waves are spawnable at all. */
    public boolean waves;
    /** AI是否有无限资源PS:建筑, 炮塔等.<p/>Whether the enemy AI has infinite resources in most of their buildings and turrets. */
    public boolean enemyCheat;
    /** 游戏目标是否为PvP. 注意,这使自动托管.<p/>Whether the game objective is PvP. Note that this enables automatic hosting. */
    public boolean pvp;
    /** 敌人的单位是否会把随机的物品扔到死亡的地方.<p>Whether enemy units drop random items on death. */
    public boolean unitDrops = true;
    /** 反应堆是否可以爆炸和破坏其他块.<p/>Whether reactors can explode and damage other blocks. */
    public boolean reactorExplosions = true;
    /** 单位建造速度基础倍数.<p/>How fast unit pads build units. */
    public float unitBuildSpeedMultiplier = 1f;
    /** 单位修复治疗的基础倍数.<p/>How much health units start with. */
    public float unitHealthMultiplier = 1f;
    /** 玩家修复治疗基础倍数.<p/>How much health players start with. */
    public float playerHealthMultiplier = 1f;
    /** 方块修复治疗基础倍数.<p/>How much health blocks start with. */
    public float blockHealthMultiplier = 1f;
    /** 玩家伤害基础倍数.<p/>How much damage player mechs deal. */
    public float playerDamageMultiplier = 1f;
    /** 单位伤害基础倍数.<p/>How much damage any other units deal. */
    public float unitDamageMultiplier = 1f;
    /** 建造花费基础倍数.<p/>Multiplier for buildings for the player. */
    public float buildCostMultiplier = 1f;
    /** 基础建造速度倍数.<p/>Multiplier for building speed. */
    public float buildSpeedMultiplier = 1f;
    /** Multiplier for percentage of materials refunded when deconstructing */
    public float deconstructRefundMultiplier = 0.5f;
    /** 敌人核心禁止建造半径.<p/>No-build zone around enemy core radius. */
    public float enemyCoreBuildRadius = 400f;
    /** 单位倾泻物品半径.<p/>Radius around enemy wave drop zones.*/
    public float dropZoneRadius = 300f;
    /** 玩家重生计时步长.<p/>Player respawn time in ticks. */
    public float respawnTime = 60 * 4;
    /** 回合重生计时步长.<p/>Time between waves in ticks. */
    public float waveSpacing = 60 * 60 * 2;
    /** BOSS回合的计时步长.<p/>How many times longer a boss wave takes. */
    public float bossWaveMultiplier = 3f;
    /** 回合发射步长.<p/>How many times longer a launch wave takes. */
    public float launchWaveMultiplier = 2f;
    /** Zone for saves that have them.*/
    public Zone zone;
    /** Spawn layout. */
    public Array<SpawnGroup> spawns = new Array<>();
    /** 是否限制重生.<p/>Determines if there should be limited respawns. */
    public boolean limitedRespawns = false;
    /** 在一个回合中玩家可以重生多少次.<p/>How many times player can respawn during one wave. */
    public int respawns = 5;
    /** 是否停止回合计时直到所有敌人都被消灭.<p/>Hold wave timer until all enemies are destroyed. */
    public boolean waitForWaveToEnd = false;
    /** 定义游戏类型是否为攻击模式.<p/>Determinates if gamemode is attack mode */
    public boolean attackMode = false;
    /** 是否为编辑游戏模式.<p/>Whether this is the editor gamemode. */
    public boolean editor = false;
    /** 是否启用了教程. 默认是fase.<p/>Whether the tutorial is enabled. False by default. */
    public boolean tutorial = false;
    /** 自定义中设置游戏结束是否可触发.<p/>Whether a gameover can happen at all. Set this to false to implement custom gameover conditions. */
    public boolean canGameOver = true;
    /** 起始核心物品.<p/>Starting items put in cores */
    public Array<ItemStack> loadout = Array.with(ItemStack.with(Items.copper, 100));
    /** 不能放置的块.<p/>Blocks that cannot be placed. */
    public ObjectSet<Block> bannedBlocks = new ObjectSet<>();
    /** 是否开启游戏内光源设置, 实验阶段.<p/>Whether everything is dark. Enables lights. Experimental. */
    public boolean lighting = false;
    /** 环境光色, 光源使用.<p/>Ambient light color, used when lighting is enabled. */
    public Color ambientLight = new Color(0.01f, 0.01f, 0.04f, 0.99f);
    /** 太阳能电池板输出功率倍数,  如果启用了照明,则使用环境光alpha值.<p/>
     * Multiplier for solar panel power output.
    negative = use ambient light if lighting is enabled. */
    public float solarPowerMultiplier = -1f;
    /** 玩家默认队伍.<p/>team of the player by default */
    public Team defaultTeam = Team.sharded;
    /** 回合敌人队伍.<p/>team of the enemy in waves/sectors */
    public Team waveTeam = Team.crux;
    /** 附加信息的特殊标签.<p/>special tags for additional info */
    public StringMap tags = new StringMap();

    /** Copies this ruleset exactly. Not very efficient at all, do not use often. */
    public Rules copy(){
        return JsonIO.copy(this);
    }

    /** Returns the gamemode that best fits these rules.*/
    public Gamemode mode(){
        return Gamemode.bestFit(this);
    }
}
