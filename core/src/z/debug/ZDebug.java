package z.debug;

import arc.Events;
import arc.math.Mathf;
import arc.math.geom.Position;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Effects;
import mindustry.entities.type.BaseUnit;
import mindustry.game.EventType;
import mindustry.type.UnitType;
import mindustry.world.Tile;
import z.ai.components.Squad;

import static mindustry.Vars.player;
import static mindustry.Vars.systemStrategy;
import static mindustry.Vars.tilesize;

/**
 *
 */
public class ZDebug {

    public static boolean d_input = true;
    /** 禁用回合多余单位死亡*/
    public static boolean disableWaveKill = true;
    public static boolean debug_blockdraw = true;
    /** 禁用单位规避碰撞*/
    public static boolean disable_avoidOthers = false;

    /** 关闭阴影绘制和迷雾绘制*/
    public static boolean disable_draw = true;
    /** 关闭地图边界移动限制*/
    public static boolean disable_worldBoundCheck = true;
    /** 关闭原版地板绘制*/
    public static boolean disable_floorRender = false;
    /** 关闭地图静态墙体绘制*/
    public static boolean disable_staticWallRender = false;
    /** 开启斜45地板开发绘制*/
    public static boolean enable_floorDebug = false;
    /** 开启权倾天下背景绘制*/
    public static boolean enable_qqtxbackImg = false;
    /** 禁止单位超出地图最大极限范围死亡*/
    public static boolean disable_unitOutboundDead = true;
    /** 禁止原版块排序*/
    public static boolean disable_blockSort = true;
    /** 使用斜45输入事件处理*/
    public static boolean enable_isoInput = true;
    /** 建筑使用自定义图标*/
    public static boolean enable_customIco = false;
    /** 禁用敌人出生点施加作用力*/
    public static boolean disable_spawnslimit = true;
    /** 禁用原版建造时间消耗初始化*/
    public static boolean disable_buildcost = true;
    /** 开启所有实体绘制*/
    public static boolean enable_drawAllEntity = true;
    /** 禁用出生点死亡警告*/
    public static boolean disable_spawnWarn = true;
    /** 禁用使用弹药(无限弹药)*/
    public static boolean disable_ammo = true;
    /** 禁止随机矿石生成*/
    public static boolean disable_randomOre = true;
    /** 使用自定义建造时间消耗*/
    public static boolean enable_customBuildCost = true;
    /** 禁用物品过滤*/
    public static boolean disable_itemFilter = true;
    /** 临时代码禁用缓存绘制*/
    public static boolean disable_cacheDraw = false;
    /** 禁用原版生命闪烁状态条*/
    public static boolean disable_lifebar = true;
    /** 强制添加物品入空余槽*/
    public static boolean forceAddItem = false;
    /** 禁用单位携带物品*/
    public static boolean disable_carryItem = true;
    /** 自定义显示状态条*/
    public static boolean enable_customBar = true;
    /** 立即完成Block升级进度*/
    public static boolean enable_accomplish = false;
    /** 使用默认生成单位数据*/
    public static boolean enable_defaultWave = true;
    /**开启单位碰撞绘制*/
    public static boolean enable_drawUnitCollision = false;
    /** 开启子弹碰撞绘制*/
    public static boolean enable_drawBulletCollision = true;
    /** 开启最上图层调试绘制*/
    public static boolean enable_drawDebug = true;
    /** 所有瓦砾均可通行, 不区分队伍和永久静态块*/
    public static boolean enable_allPassable = true;
    /** 加载xml配置文件科技树*/
    public static boolean enable_xmlTechTree = true;
    /** 禁止加载qqtx数据包,提高开发运行速度*/
    public static boolean disable_packLoad = false;
    /** 开启编辑器斜45绘制*/
    public static boolean enable_editorIso = true;
    /** 禁止mindustry游戏结束检测*/
    public static boolean disable_mindustryEndCheck = true;
    /** 隐藏mindustry玩家角色*/
    public static boolean disable_mindustryPlayer = true;
    /** 调试绘制Player位置*/
    public static boolean debug_drawPlayer = true;
    /** 开启脚本文件加载*/
    public static boolean enable_scriptLoader = true;
    /** 开启阴影绘制*/
    public static boolean enable_shadowDraw = true;
    /** 使用纹理倾斜绘制阴影(暗黑2阴影绘制)*/
    public static boolean use_shadowTrans = true;
    /** 禁用Mindustry退出保存*/
    public static boolean disable_exitSave = true;
    /** 开启移除所有游戏存档文件*/
    public static boolean enable_removeSaves = false;

    // 文件创建调试布尔begon
    /** 科技树xml文件创建*/
    public static boolean create_TechTreeXmlFile = false;
    // 文件创建调试布尔end

    // 路径数据 begon
    public static String techTreeFile = "F:\\Develop\\workspace\\libgdx\\zones\\Public\\DiabloTown\\SanGuoTD\\core\\assets-raw\\zonesAdd\\createFile\\techTree.xml";
    // 路径数据 end

    /**
     *  测试方法指定位置添加单位
     * */
    public static void addUnit(float x, float y, UnitType unitType) {
        if (Vars.state.launched || Vars.state.gameOver) {
            return;
        }

        Effects.shake(2f, 3f, new Position() {
            @Override
            public float getX() {
                return x;
            }

            @Override
            public float getY() {
                return y;
            }
        });
        Effects.effect(Fx.producesmoke, x, y);

        Tile createTile = Vars.world.ltileWorld(x, y);
        if (createTile == null) return;

        BaseUnit unit = unitType.create(Vars.player.getTeam());
        unit.setSpawner(createTile);
//        unit.setAnimationData(Core.assets.get("debug/worker/worker.paper2dsprites", AniData.class));
        if (enable_isoInput) {
            unit.set(createTile.getX() + Mathf.range(4f / tilesize), createTile.getY() + Mathf.range(4f / tilesize));
        } else {
            unit.set(createTile.drawxIso() + Mathf.range(4), createTile.drawyIso() + Mathf.range(4));
        }
        unit.add();
//        unit.velocity().y = factory.launchVelocity;
        Events.fire(new EventType.UnitCreateEvent(unit));

        {   // 队伍数据
            Squad<BaseUnit> mySquad = systemStrategy.getSquad(player.getTeam(), 0);
//            if (mySquad == null) {
//                mySquad = new Squad<BaseUnit>();
//
//                Vec2 targetPos = mySquad.getTarget().getPosition();
//                Tile selected = world.tileWorld(targetPos.x, targetPos.y);
////                indexer.moveIndexer = selected;
//            }
            mySquad.addMember(unit);
        }
    }


    // temp test code begon
//    public static Squad<BaseUnit> mySquad;
    // temp test code end

}
