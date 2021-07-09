package mindustry.world.modules;

import java.io.*;

/**
 *  一个代表分隔的瓦实体状态的类.<p/>
 * A class that represents compartmentalized tile entity state.
 * */
public abstract class BlockModule{
    public abstract void write(DataOutput stream) throws IOException;

    public abstract void read(DataInput stream) throws IOException;
}
