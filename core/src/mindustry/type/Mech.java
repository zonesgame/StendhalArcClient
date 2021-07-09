package mindustry.type;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.ui.layout.Table;
import arc.util.ArcAnnotate.*;
import arc.util.Time;
import mindustry.ctype.ContentType;
import mindustry.entities.type.Player;
import mindustry.ctype.UnlockableContent;
import mindustry.graphics.Pal;
import mindustry.ui.ContentDisplay;

import static mindustry.Vars.tilesize;
import static z.debug.ZDebug.enable_isoInput;

/**
 *  机甲
 * */
public class Mech extends UnlockableContent{
    /** 飞行状态*/
    public boolean flying;
    /** 速度*/
    public float speed = enable_isoInput ? 1.1f / tilesize : 1.1f;
    /** 最大速度*/
    public float maxSpeed = enable_isoInput ? 10f / tilesize : 10f;
    /** 推进速度*/
    public float boostSpeed = enable_isoInput ? 0.75f/ tilesize : 0.75f;
    /** 阻力*/
    public float drag = 0.4f;
    /** 质量*/
    public float mass = 1f;
    /** 抖动*/
    public float shake = 0f;
    /** 生命*/
    public float health = 200f;

    /** 碰撞尺寸*/
    public float hitsize = enable_isoInput ? 6f / tilesize : 6f;
    /***/
    public float cellTrnsY = 0f;
    /** 开采速度*/
    public float mineSpeed = 1f;
    /** 钻探等级*/
    public int drillPower = -1;
    /** 建造速度等级*/
    public float buildPower = 1f;
    /** 动力颜色*/
    public Color engineColor = Pal.boostTo;
    /** 物品容量*/
    public int itemCapacity = 30;
    /** 跟随光标旋转*/
    public boolean turnCursor = true;
    /** 是否能治愈*/
    public boolean canHeal = false;
    /***/
    public float compoundSpeed, compoundSpeedBoost;

    /** 绘制生命和队伍图标.<p/>draw the health and team indicator */
    public boolean drawCell = true;
    /** 绘制背部物品.<p/>draw the items on its back */
    public boolean drawItems = true;
    /** 绘制推进动力光源.<p/>draw the engine light if it's flying/boosting */
    public boolean drawLight = true;

    /** 武器偏移位置*/
    public float weaponOffsetX, weaponOffsetY, /** 动力偏移位置*/engineOffset = 5f, /** 动力尺寸*/engineSize = 2.5f;
    public @NonNull Weapon weapon;

    public TextureRegion baseRegion, legRegion, region;

    public Mech(String name, boolean flying){
        super(name);
        this.flying = flying;
    }

    public Mech(String name){
        this(name, false);
    }

    public void updateAlt(Player player){
    }

    public void draw(Player player){
    }

    public void drawStats(Player player){
        if(drawCell){
            // zones add begon
            float x = player.x;
            float y = player.y;
            float rotation = player.rotation;
            if (enable_isoInput) {
                x = player.wpos.x;
                y = player.wpos.y;
                rotation = (360 - player.rotation + 45);
            }
            // zones add edn
            float health = player.healthf();
            Draw.color(Color.black, player.getTeam().color, health + Mathf.absin(Time.time(), health * 5f, 1f - health));
            Draw.rect(player.getPowerCellRegion(),
                x + Angles.trnsx(rotation, cellTrnsY, 0f),        // zones edirot player.x    player.rotation
                y + Angles.trnsy(rotation, cellTrnsY, 0f),
                rotation - 90);
            Draw.reset();
        }
        if(drawItems){
            player.drawBackItems();
        }
        if(drawLight){
            player.drawLight();
        }
    }

    public float getExtraArmor(Player player){
        return 0f;
    }

    public float spreadX(Player player){
        return 0f;
    }

    public float getRotationAlpha(Player player){
        return 1f;
    }

    public boolean canShoot(Player player){
        return true;
    }

    public void onLand(Player player){
    }

    @Override
    public void init(){
        super.init();

        for(int i = 0; i < 500; i++){
            compoundSpeed *= (1f - drag);
            compoundSpeed += speed;
        }

        for(int i = 0; i < 500; i++){
            compoundSpeedBoost *= (1f - drag);
            compoundSpeedBoost += boostSpeed;
        }
    }

    @Override
    public void displayInfo(Table table){
        ContentDisplay.displayMech(table, this);
    }

    @Override
    public ContentType getContentType(){
        return ContentType.mech;
    }

    @Override
    public void load(){
        weapon.load();
        if(!flying){
            legRegion = Core.atlas.find(name + "-leg");
            baseRegion = Core.atlas.find(name + "-base");
        }

        region = Core.atlas.find(name);
    }

    @Override
    public String toString(){
        return localizedName;
    }
}
