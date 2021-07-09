package mindustry.world.modules;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import mindustry.entities.type.TileEntity;
import mindustry.type.ItemStack;
import mindustry.world.consumers.Consume;
import mindustry.world.consumers.ConsumeItems;

/**
 *  消耗模块
 * */
public class ConsumeModule extends BlockModule{
    /** 是否有效*/
    private boolean valid, /** 选择有效*/optionalValid;
    /** 绑定Tile实体*/
    private final TileEntity entity;

    public ConsumeModule(TileEntity entity){
        this.entity = entity;
    }

    /** 消耗模块更新*/
    public void update(){
        //everything is valid here
        if(entity.tile.isEnemyCheat()){
            valid = optionalValid = true;
            return;
        }

        boolean prevValid = valid();
        valid = true;
        optionalValid = true;
        boolean docons = entity.block.shouldConsume(entity.tile) && entity.block.productionValid(entity.tile);

        int level = entity.block.consumes.length == 1 ? 0 : entity.level();
        for(Consume cons : entity.block.consumes[level].all()){
            if(cons.isOptional()) continue;

            if(docons && cons.isUpdate() && prevValid && cons.valid(entity)){
                cons.update(entity);
            }

            valid &= cons.valid(entity);
        }

        for(Consume cons : entity.block.consumes[level].optionals()){
            if(docons && cons.isUpdate() && prevValid && cons.valid(entity)){
                cons.update(entity);
            }

            optionalValid &= cons.valid(entity);
        }
    }

    public void trigger(){
        for(Consume cons : entity.block.consumes[entity.level()].all()){
            cons.trigger(entity);
        }
    }

    public boolean valid(){
        return valid && entity.block.shouldConsume(entity.tile);
    }

    public boolean optionalValid(){
        return valid() && optionalValid;
    }

    @Override
    public void write(DataOutput stream) throws IOException{
        stream.writeBoolean(valid);
    }

    @Override
    public void read(DataInput stream) throws IOException{
        valid = stream.readBoolean();
    }

    // zones add begon
    /** 获取当前最需要补给的物品
     * @return null没有需要补给的物品*/
    public ItemStack getConsume() {
        ItemStack minItem = null;
        float craftUnit = Float.MAX_VALUE;     // 物品可以执行的生产单位
        for(Consume cons : entity.block.consumes[entity.level()].all()){
            if(cons.isOptional()) continue;
            if ( !(cons instanceof ConsumeItems)) continue;

            for (ItemStack itemStack : ((ConsumeItems) cons).items) {
                float craftPer = entity.items.get(itemStack.item) / (float)itemStack.amount;
                if (craftPer < craftUnit) {
                    craftUnit = craftPer;
                    minItem = itemStack;
                }
            }
        }

        if (craftUnit > 2)  minItem = null;     // 库存小于两个生产单位开始补给
        return minItem;
    }
    // zones add end
}
