package mindustry.type;

import arc.Core;
import arc.audio.Sound;
import arc.func.Prov;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Rect;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.ObjectSet;
import arc.util.ArcAnnotate.NonNull;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.ctype.ContentType;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.type.BaseUnit;
import mindustry.game.Team;
import mindustry.gen.Sounds;
import mindustry.ui.ContentDisplay;
import z.tools.serialize.XmlSerialize;

import static mindustry.Vars.tilesize;
import static z.debug.ZDebug.enable_isoInput;

/**
 *  Unit类型
 * */
public class UnitType extends UnlockableContent{
    public @NonNull TypeID typeID;
    public @NonNull Prov<? extends BaseUnit> constructor;

    /** 生命*/
    public float health = 60;
    /** 碰撞范围*/
    public float hitsize = enable_isoInput ? 7f / tilesize : 7f;
    /** 瓦砾碰撞范围*/
    public float hitsizeTile = enable_isoInput ? 4f / tilesize : 4f;
    /** 速度*/
    public float speed = enable_isoInput ? 0.4f / tilesize : 0.4f;
    /** 范围*/
    public float range = 0, /** 攻击距离*/attackLength = enable_isoInput ? 150f / tilesize : 150f;
    /** 旋转速度*/
    public float rotatespeed = 0.2f;
    /** 基础旋转速度*/
    public float baseRotateSpeed = 0.1f;
    /** 射击锥形*/
    public float shootCone = 15f;
    /** 质量*/
    public float mass = 1f;
    /** 飞行状态*/
    public boolean flying;
    /** 无目标*/
    public boolean targetAir = true;
    /** 旋转武器*/
    public boolean rotateWeapon = false;
    /** 阻力*/
    public float drag = 0.1f;
    /** 最大速度*/
    public float maxVelocity = enable_isoInput ? 5f / tilesize : 5f;
    /** 撤退百分比*/
    public float retreatPercent = 0.6f;
    /** 物品容量*/
    public int itemCapacity = 30;
    /** 开采物品*/
    public ObjectSet<Item> toMine = ObjectSet.with(Items.lead, Items.copper);
    /** 建造速度等级*/
    public float buildPower = 0.3f, /** 开采速度等级*/minePower = 0.7f;
    /** 武器*/
    public @NonNull Weapon weapon;
    /** 武器偏移值*/
    public float weaponOffsetY, /** 动力偏移值*/engineOffset = 6f, /** 动力尺寸*/engineSize = 2f;
    /** 免疫效果*/
    public ObjectSet<StatusEffect> immunities = new ObjectSet<>();
    /** 死亡音频*/
    public Sound deathSound = Sounds.bang;

    public TextureRegion legRegion, baseRegion, region;

    public <T extends BaseUnit> UnitType(String name, Prov<T> mainConstructor){
        this(name);
        create(mainConstructor);
    }

    public UnitType(String name){
        super(name);
    }

    public <T extends BaseUnit> void create(Prov<T> mainConstructor){
        this.constructor = mainConstructor;
        this.description = Core.bundle.getOrNull("unit." + name + ".description");
        this.typeID = new TypeID(name, mainConstructor);
    }

    @Override
    public void displayInfo(Table table){
        ContentDisplay.displayUnit(table, this);
    }

    @Override
    public void load(){
        weapon.load();
        region = Core.atlas.find(name);
        legRegion = Core.atlas.find(name + "-leg");
        baseRegion = Core.atlas.find(name + "-base");
    }

    @Override
    public ContentType getContentType(){
        return ContentType.unit;
    }

    public BaseUnit create(Team team){
        BaseUnit unit = constructor.get();
        unit.init(this, team);
        return unit;
    }

    // zones add begon
    private TextureRegion[][][] spriteRegions;
    private Rect[][][] spriteRects;

    public <T extends BaseUnit> UnitType(String name, Prov<T> mainConstructor, String configFile){
        this(name);
        create(mainConstructor);    // default end

//        loadConfigFile(configFile);
        ObjectMap<String, Object> tempPool = Vars.xmlSerialize.loadUnitAnimation(configFile);
        if (tempPool == null)   return;
        spriteRegions = (TextureRegion[][][]) tempPool.get(XmlSerialize.qqtxRegions);
        spriteRects = (Rect[][][]) tempPool.get(XmlSerialize.qqtxRects);
        tempPool.clear();   // no must
    }

    public int getFrameLength(int state, int dir) {
        return spriteRegions[state][dir].length;
    }

    public TextureRegion getFrameRegion(int state, int dir, int frame) {
        return spriteRegions[state][dir][frame];
    }

    public Rect getFrameRect(int state, int dir, int frame) {
        return spriteRects[state][dir][frame];
    }
    // zones add end
}
