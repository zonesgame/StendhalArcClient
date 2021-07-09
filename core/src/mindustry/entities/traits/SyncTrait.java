package mindustry.entities.traits;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import mindustry.net.Interpolator;

/**
 *  序列化属性接口.<p/>
 *  网络模块数据包使用.
 * */
public interface SyncTrait extends Entity, TypeTrait{

    /** 设置位置值数据流, 并更新插入器.<p/>Sets the position of this entity and updated the interpolator. */
    default void setNet(float x, float y){
        set(x, y);

        if(getInterpolator() != null){
            getInterpolator().target.set(x, y);
            getInterpolator().last.set(x, y);
            getInterpolator().pos.set(0, 0);
            getInterpolator().updateSpacing = 16;
            getInterpolator().lastUpdated = 0;
        }
    }

    /** 仅插入实体位置. 如果你需要插值或其他值,请重写.<p/>Interpolate entity position only. Override if you need to interpolate rotations or other values. */
    default void interpolate(){
        if(getInterpolator() == null){
            throw new RuntimeException("This entity must have an interpolator to interpolate()!");
        }

        getInterpolator().update();

        setX(getInterpolator().pos.x);
        setY(getInterpolator().pos.y);
    }

    /** 获取数据插入器.<p/>Return the interpolator used for smoothing the position. Optional. */
    default Interpolator getInterpolator(){
        return null;
    }

    /** 是否为本实体启用了同步;默认情况下是true.<p/>Whether syncing is enabled for this entity; true by default. */
    default boolean isSyncing(){
        return true;
    }

    //Read and write sync data, usually position
    void write(DataOutput data) throws IOException;

    void read(DataInput data) throws IOException;
}
