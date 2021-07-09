package mindustry.world.blocks;

import mindustry.world.Block;
import mindustry.world.meta.BlockGroup;

/**
 *  电力块
 * */
public abstract class PowerBlock extends Block{

    public PowerBlock(String name){
        super(name);
        update = true;
        solid = true;
        hasPower = true;
        group = BlockGroup.power;
    }
}
