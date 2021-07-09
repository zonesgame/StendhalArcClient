package mindustry.world.blocks.power;

import mindustry.world.blocks.PowerBlock;

/**
 *  电力配送器
 * */
public class PowerDistributor extends PowerBlock{

    public PowerDistributor(String name){
        super(name);
        consumesPower = false;
        outputsPower = true;
    }
}
