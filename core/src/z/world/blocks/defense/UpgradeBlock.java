package z.world.blocks.defense;

import arc.Core;
import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.struct.ObjectMap;
import arc.z.util.ZonesAnnotate.ZMethod;
import arc.z.util.ZonesAnnotate.ZAdd;
import arc.z.util.ZonesAnnotate.ZField;
import mindustry.annotations.Annotations;
import mindustry.entities.type.TileEntity;
import mindustry.game.Team;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.ui.Bar;
import mindustry.world.Tile;
import mindustry.world.blocks.storage.CoreBlock;
import z.debug.Strs;
import z.entities.traits.UpgradeTrait;
import z.entities.type.UpgradeTileEntity;
import z.world.blocks.OperationAction;

import static mindustry.Vars.player;
import static mindustry.Vars.state;
import static mindustry.Vars.systemItems;

/**
 * 权倾天下, 使用的可升级块
 */
public class UpgradeBlock extends EditorBlock {

    /** 升级操作执行完毕回调, 完成或中断.*/
    @ZAdd
    @Annotations.Remote(called = Annotations.Loc.server)
    public static void onUpgradeFinish(TileEntity tileEntity, Team team, boolean isbreak){
        if ( isbreak) { // 取消升级, 返还资源
            UpgradeTileEntity upgradeTileEntity = (UpgradeTileEntity) tileEntity;
            upgradeTileEntity.progress = 0; // 立即完成保留数据
            if ( !state.rules.infiniteResources) {
                ((UpgradeBlock) tileEntity.block).unUpgrade(tileEntity);
            }
        }
        else {  // 升级完成
//            UpgradeTileEntity upgradeTileEntity = (UpgradeTileEntity) tileEntity;
//            upgradeTileEntity.addLevel();
            ((UpgradeBlock) tileEntity.block).attributeUpgrade(tileEntity);

            ((UpgradeTileEntity) tileEntity).progress = 0;
            tileEntity.levelBy(1);
        }
    }



    /** 块最大等级*/
//    public int maxLevel = 0;
    /** 升级绘制状态图标. 扩展增加休息图标*/
    private TextureRegion[] upgradeIcos = new TextureRegion[4];

    // new content begon
    /** 升级消耗时间*/
//    @ZField
//    public float[] buildCostExtend = {};
    /**生命值升级扩展*/
//    @ZField
//    public int[] healthExtend = {};
    /** 物品容量升级扩展*/
//    @ZField
//    public int[] itemCapacityExtend = {};
    /** 块升级消耗成本 */
    @ZField
    public ItemStack[][] requirementsExtend = {};
    //  new content end

    public UpgradeBlock(String name) {
        super(name);
        layer2 = Layer.power;
    }

    /** 需要升级的数据进行初始化, 测试function(主要针对@ZField数组)<p/>
     * 构造方法之后调用
     * */
//    @ZMethod
//    public void initMindustry() {
//        this.buildCost = buildCostExtend.length != 0 ? buildCostExtend[0] : buildCost;
//        this.health = healthExtend.length != 0 ? healthExtend : health;
//        this.itemCapacity = itemCapacityExtend.length != 0 ? itemCapacityExtend : itemCapacity;
//        this.requirements = requirementsExtend.length != 0 ? requirementsExtend[0] : requirements;      // Block  requirements(Category cat, ItemStack[] stacks, boolean unlocked) 已初始化
//    }

//    @Override
//    public ItemStack[] getRequirements() {
//        if (levRequirements_.length == 0)
//            return super.getRequirements();
//        return levRequirements_[0];
//    }

    /** 获取升级到指定等级所需物品*/
//    public ItemStack[] getRequirements(int lev) {
//        return requirementsExtend[lev];
//    }

//    /** 设置升级所需物品*/
//    @ZMethod
//    public void setRequirements(ItemStack[][] requirements) {
//        this.levRequirements_ = requirements;
////        this.maxLevel = requirements.length - 1;
//    }

    @Override
    public void load() {
        super.load();   // 禁止加载先前无用资源
        for (int i = upgradeIcos.length; --i >= 0; ) {
            upgradeIcos[i] = Core.atlas.find("upgrade" + (i+1));
        }
    }

    @ZMethod
    @Override
    public void requirements(Category cat, ItemStack[][] stacks, Boolean unlocked) {
        super.requirements(cat, stacks[0], unlocked);
        this.requirementsExtend = stacks;
    }

    @Override
    public void setBars(){
        bars.add("level", entity -> new Bar(() -> Core.bundle.format(Strs.str22, entity.level() + 1), () -> Pal.lancerLaser, entity::levelf).blink(Color.white));
        super.setBars();
    }

    @Override
    public void drawLayer(Tile tile) {
        super.drawLayer(tile);
    }

    /** 绘制升级状态图标*/
    @Override
    public void drawLayer2(Tile tile) {
        super.drawLayer2(tile);

        // 绘制升级状态图标 begon
        UpgradeTileEntity entity = tile.ent();
        if (entity == null || !entity.isAddedRequest) return;

        TextureRegion region1, region2;
        if (entity.isUpgrade) {
            region1 = upgradeIcos[0];
            region2 = upgradeIcos[1];
        }
        else {
            region1 = upgradeIcos[2];
            region2 = upgradeIcos[3];
        }

        float dx = tile.drawxIso();
        float dy = tile.drawyIso();
        float width1 = region1.getWidth() * Draw.scl;
        float height1 = region1.getHeight() * Draw.scl;;
        float width2 = region2.getWidth() * Draw.scl;
        float height2 = region2.getHeight() * Draw.scl;;
        Draw.rectGdx(region1, dx - width1 / 2f, dy - height1 / 2f, width1, height1);
        Draw.rectGdx(region2, dx - width2 / 2f, dy - height2 / 2f, width2, height2);
        // 绘制升级状态图标 end
    }

    @ZAdd
    @Override
    public <T extends OperationAction> ObjectMap<OperationAction, Cons<T>> getOperationActions(TileEntity tileEntity) {
        ObjectMap<OperationAction, Cons<T>> operationActions = super.getOperationActions(tileEntity);
        CoreBlock.CoreEntity core = (CoreBlock.CoreEntity) player.getClosestCore();

        //upgrade event operation begon
        UpgradeTileEntity upgradeEntity = (UpgradeTileEntity) tileEntity;
        if ( !upgradeEntity.isLevelFull()) {    // 未达到最大等级
            if (upgradeEntity.isAddedRequest) { // 已添加入升级列表
                operationActions.put(OperationAction.UNUPGRADE, actionUnUpgrade -> {    // 取消升级事件
                    core.addUpgradeRequest(new UpgradeTrait.UpgradeRequest(upgradeEntity, true), true);
                });
//                operationActions.put(OperationAction.ACCOMPLISH, actionAccomplish -> {    // 立即完成升级事件
//                    upgradeEntity.progress = 1;
//                    core.addUpgradeRequest(new UpgradeTrait.UpgradeRequest(upgradeEntity), false);
//                });
            }
            else {  // 未添加入升级列表
                operationActions.put(OperationAction.UPGRADE, actionUpgrade -> {
                    if (upgradeCondition(tileEntity)) {
                        if (!state.rules.infiniteResources) {   // 使用消耗资源
                            upgrade(tileEntity);
                        }
                        core.addUpgradeRequest(new UpgradeTrait.UpgradeRequest(upgradeEntity), true);
                    }
                });
            }
        }
        // upgrade event operation end

        return operationActions;
    }

    /** 升级*/
    public boolean upgradeCondition(TileEntity entity){
        boolean needCondition = player.getClosestCore() != null && // 核心存在
                (state.rules.infiniteResources || // 开发无限资源模式
                        systemItems.allItems.has(requirementsExtend[entity.copyLevelBy(1)], state.rules.buildCostMultiplier)); // 拥有升级需要资源
        return needCondition;
    }

    /** 取消升级, 返还资源*/
    private void unUpgrade(TileEntity entity){

    }

    /** 升级, 消耗资源*/
    private void upgrade(TileEntity entity){

    }

    /** 升级属性数据*/
    @ZAdd
    protected void attributeUpgrade(TileEntity entity) {
        int nextLev = entity.copyLevelBy(1);
        if(nextLev == entity.level())   return;

        float healthPercent = entity.healthf();
        entity.health(health[nextLev] * healthPercent);
    }
}
