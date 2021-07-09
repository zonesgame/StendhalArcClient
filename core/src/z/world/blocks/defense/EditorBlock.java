package z.world.blocks.defense;

import arc.func.Cons;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.z.util.ZonesAnnotate.ZAdd;
import mindustry.entities.type.TileEntity;
import mindustry.world.Tile;
import z.ui.BlockSelection;
import z.world.blocks.OperationAction;
import z.world.blocks.storage.BlockIso;

import static mindustry.Vars.control;

/**
 *  构建提供选择Action事件块.
 *  权倾天下基础块, 全部都是可移动的.
 */
public class EditorBlock extends BlockIso {

//    private final Array<OperationAction> defaultActions = new Array<OperationAction>(new OperationAction[]{ OperationAction.MOVE});
//    private final ObjectMap<OperationAction, Cons<T>> defaultActions = new ObjectMap<>();
    /** 是否显示扩展信息*/
//    protected boolean extendedInformation = false;

    public EditorBlock(String name) {
        super(name);
        this.configurable = true;   // 开启配置选项
    }

    //  构建点击Action事件UI
    @Override
    public void buildConfiguration(Tile tile, Table table){
        super.buildConfiguration(tile, table);
//        table.clearChildren();
        BlockSelection.buildTable(tile, table, getOperationActions(tile.ent()));
//        if (extendedInformation) {  // 添加仓库等块的扩展信息
//        }
    }

    /** 块可以执行的操作Action列表. ps: 移动, 升级..*/
    @ZAdd
    public <T extends OperationAction> ObjectMap<OperationAction, Cons<T>> getOperationActions(TileEntity tileEntity) {
        ObjectMap<OperationAction, Cons<T>> defaultActions = new ObjectMap<>();

        defaultActions.put(OperationAction.MOVE, actionMove -> {    // 移动操作
            control.input.block = this;
        });

        if(configurable && hasItems)    // 显示物品数量
            defaultActions.put(OperationAction.EXTENDEDINFORMATION, null);

        return defaultActions;
    }

}
