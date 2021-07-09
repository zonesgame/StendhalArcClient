package mindustry.world.blocks.storage;

/**
 *  仓库
 * */
public class Vault extends StorageBlock{

    public Vault(String name){
        super(name);
        solid = true;
        update = false;
        destructible = true;
    }

}
