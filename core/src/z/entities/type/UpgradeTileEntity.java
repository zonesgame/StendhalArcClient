package z.entities.type;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import arc.math.Mathf;
import mindustry.entities.type.TileEntity;
import mindustry.world.Tile;
import z.world.blocks.defense.UpgradeBlock;

/**
 *
 */
public class UpgradeTileEntity extends TileEntity {
    /** 等级数据*/
    private int curLevel = 0, maxLevel = 0;
    /** 是否注入升级请求*/
    public boolean isAddedRequest;
    /** 升级执行状态*/
    public boolean isUpgrade;
    /** 升级执行进度*/
    public float progress;

    /** 当前变体或方向*/
    public int curVariant = 0;
    /**Move 状态*/
    public boolean isDrag = false;

    @Override
    public TileEntity init(Tile tile, boolean shouldAdd) {
        super.init(tile, shouldAdd);

        if(block instanceof UpgradeBlock)
        this.maxLevel = Math.max( ((UpgradeBlock) this.block).buildCost.length - 1, 0);
        return this;
    }

//    public void addLevel() {
//        ++curLevel;
//        progress = 0;
//        isAddedRequest = isUpgrade = false;
//    }

    /** 设置更新进度*/
    public float setProgress(float amount) {
        return progress = Mathf.clamp(progress + amount);
    }

    /** 设置更新状态*/
    public void setUpgrade(boolean upgrade) {
        if (upgrade != isUpgrade) {
            isUpgrade = upgrade;
            // 执行状态更改操作
        }
    }

    @Override
    public void write(DataOutput stream) throws IOException {
        super.write(stream);
        stream.writeInt(curLevel);
        stream.writeInt(curVariant);
    }

    @Override
    public void read(DataInput stream, byte revision) throws IOException{
        super.read(stream, revision);
        curLevel = stream.readInt();
        curVariant = stream.readInt();
    }

    // LevelTrait begon
    @Override
    public void level(int lev) {        // develop
        this.curLevel = lev;
    }

    @Override
    public int level() {   // develop
        return curLevel;
    }

    @Override
    public int maxLevel() {    // develop
        return maxLevel;
    }

//    @Override
//    public void levelBy(int amount){
//        super.levelBy(amount);
//    }
    // LevelTrait end
}
