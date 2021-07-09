package mindustry.entities.type;

import arc.math.geom.Vec2;
import arc.z.util.ISOUtils;
import mindustry.Vars;
import mindustry.entities.EntityGroup;
import mindustry.entities.traits.Entity;

import static z.debug.ZDebug.enable_isoInput;

/**
 *  基础实体
 * */
public abstract class BaseEntity implements Entity{
    private static int lastid;
    /** 不要修改, 用于数据序列化.<p/>Do not modify. Used for network operations and mapping. */
    public int id;
    /** 实体位置, tile坐标.*/
    public float x, y;
    /** 实体所在群组*/
    protected transient EntityGroup group;

    public BaseEntity(){
        id = lastid++;
    }

    /** 所在瓦砾X轴位置*/
    public int tileX(){
        if (enable_isoInput) {
            return Math.round(x);
        }
        return Vars.world.toTile(x);
    }

    /** 所在瓦砾Y轴位置*/
    public int tileY(){
        if (enable_isoInput) {
            return Math.round(y);
        }
        return Vars.world.toTile(y);
    }

    @Override
    public int getID(){
        return id;
    }

    @Override
    public void resetID(int id){
        this.id = id;
    }

    @Override
    public EntityGroup getGroup(){
        return group;
    }

    @Override
    public void setGroup(EntityGroup group){
        this.group = group;
    }

    /** world坐标X轴位置*/
    @Override
    public float getX(){
//        if (enable_isoInput)
//            return wpos.x;
        return x;
    }

    /** tile坐标X轴位置*/
    @Override
    public void setX(float x){
        this.x = x;
    }

    /** world坐标Y轴位置*/
    @Override
    public float getY(){
//        if (enable_isoInput)
//            return wpos.y;
        return y;
    }

    /** tile坐标Y轴位置*/
    @Override
    public void setY(float y){
        this.y = y;
    }

    @Override
    public String toString(){
        return getClass() + " " + id;
    }

    /** Increments this entity's ID. Used for pooled entities.*/
    public void incrementID(){
        id = lastid++;
    }


    // zones add begon
    /** 绘制坐标位置(世界坐标)*/
    public Vec2 wpos = new Vec2();

    /** 更细绘制坐标绘制*/
    public Vec2 drawPosition() {
        return ISOUtils.tileToWorldCoords(x, y, wpos);
    }
    // zones add end
}
