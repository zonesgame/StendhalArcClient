package z.entities.traits;

import arc.struct.Array;
import arc.struct.Queue;
import arc.util.ArcAnnotate.NonNull;
import arc.util.ArcAnnotate.Nullable;
import arc.util.Time;
import arc.z.util.ZonesAnnotate.ZAdd;
import mindustry.entities.traits.Entity;
import mindustry.entities.traits.TeamTrait;
import mindustry.gen.Call;
import z.entities.type.UpgradeTileEntity;
import z.world.blocks.defense.UpgradeBlock;

import static mindustry.Vars.state;
import static z.debug.ZDebug.enable_accomplish;

/**
 *  可执行升级操作属性接口.<p/>
 * Interface for units that build things.
 * */
public interface UpgradeTrait extends Entity, TeamTrait {

    /** 可执行更新数量*/
    int upgradeCount = 1;
    /** 移除队列容器*/
    Array<UpgradeRequest> removePool = new Array<>(8);
    /** 事件处理容器*/
    Array<UpgradeRequest> eventPool = new Array<>(8);

    /** 单位更新建造块.<p/>Updates building mechanism for this unit.*/
    default void updateUpgrade(){
        if(buildQueue().size == 0)  return;

        // 计算更新基础倍数
        float baseUpgradeMultiplier =  getUpgradePower() * state.rules.buildSpeedMultiplier;

        for (int i = 0, len = buildQueue().size; i < len; i++) {
            UpgradeRequest upgradeRequest = buildQueue().get(i);
            if (upgradeRequest.upgrade(i < upgradeCount ? baseUpgradeMultiplier : 0))
                removePool.add(upgradeRequest);
        }

        // 事件容器处理
//        for (UpgradeRequest upgrade : eventPool) {
//
//        }

        if (removePool.size != 0) {
            for (UpgradeRequest upgrade : removePool) {
                removeRequest(upgrade);
            }
            removePool.clear();
        }
    }

    /** 执行升级操作列表.<p/>Returns the queue for storing build requests. */
    Queue<UpgradeRequest> buildQueue();

    /** 为TileEntity添加一个升级请求.<p/>Add another build requests to the queue, if it doesn't exist there yet.
     * @param tail true添加入容器末尾
     *  */
    default void addUpgradeRequest(UpgradeRequest request, boolean tail){
        if (request.breaking) {  // 取消升级请求
            for (UpgradeRequest temp : buildQueue()) {
                if (temp.blockEntity.id == request.blockEntity.id) {
                    removeRequest(temp);
                    Call.onUpgradeFinish(temp.blockEntity, null, true);
                    break;
                }
            }
            return;
        }

        // 升级请求begon
        if ( !tail) {   // 立即完成升级
            for (UpgradeRequest temp : buildQueue()) {
                if (temp.blockEntity.id == request.blockEntity.id) {
                    removeRequest(temp);
                    break;
                }
            }
        }

        if(tail){
            buildQueue().addLast(request);
        }else{
            buildQueue().addFirst(request);
        }
        request.blockEntity.isAddedRequest = true;
        //升级请求end
    }

    /** 移除升级请求*/
    default void removeRequest(UpgradeRequest request){
        UpgradeTileEntity upgradeTileEntity = request.blockEntity;
        upgradeTileEntity.isAddedRequest = false;
        upgradeTileEntity.setUpgrade(false);
        buildQueue().remove(request);
    }

    /** 获取一个升级操作
     * Return the build requests currently active, or the one at the top of the queue.
     * May return null.
     */
    default @Nullable
    UpgradeRequest buildRequest(){
        return buildQueue().size == 0 ? null : buildQueue().first();
    }

    /** 升级倍数*/
    @ZAdd
    float getUpgradePower();


    /**
     *  用于存储建造请求的类, 可以是一个放置或移除请求.<p/>
     * Class for storing build requests. Can be either a place or remove request. */
    class UpgradeRequest{

        /** 是否为中断请求.*/
        public boolean breaking;
        /** 升级块实体.*/
        public @NonNull UpgradeTileEntity blockEntity;
        /** 更新进度.*/
//        public float progress;
        /** 目标等级索引*/
//        private int levTarget;
        /** 块建造消耗*/
        private float levBuildCost;


        /** 创建一个更新请求.*/
        public UpgradeRequest(UpgradeTileEntity blockentity, boolean isbreak){
            this.blockEntity = blockentity;
            this.breaking = isbreak;

            UpgradeBlock upgradeBlock = (UpgradeBlock) blockentity.block;
            int levTarget = blockentity.copyLevelBy(1);
            levBuildCost = upgradeBlock.buildCost[levTarget];
        }

        /** 创建一个更新请求. */
        public UpgradeRequest(UpgradeTileEntity blockentity){
            this(blockentity, false);
        }

        /** 更新进度*/
        private boolean upgrade(float amount){
            amount = 1f / levBuildCost * Time.delta() * amount;
            blockEntity.setUpgrade(amount != 0);

            float progress = blockEntity.setProgress(amount);
            if(progress >= 1 || enable_accomplish){     //  || state.rules.infiniteResources     // 关闭无限资源立即完成升级
                Call.onUpgradeFinish(blockEntity, null, false);
                return true;
            }
            return false;
        }
    }
}
