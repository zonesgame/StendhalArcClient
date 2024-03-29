package mindustry.entities.type.base;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import arc.math.Mathf;
import arc.util.Structs;
import mindustry.content.Blocks;
import mindustry.entities.traits.MinerTrait;
import mindustry.entities.type.TileEntity;
import mindustry.entities.units.UnitState;
import mindustry.gen.Call;
import mindustry.type.Item;
import mindustry.type.ItemType;
import mindustry.world.Pos;
import mindustry.world.Tile;

import static mindustry.Vars.indexer;
import static mindustry.Vars.world;

/**
 *  仅能执行采集的无人机.<p/>
 * A drone that only mines.
 * */
public class MinerDrone extends BaseDrone implements MinerTrait{
    /** 目标物品*/
    protected Item targetItem;
    /** 采集瓦砾*/
    protected Tile mineTile;

    public final UnitState

    mine = new UnitState<MinerDrone>(){
        @Override
        public void enter(MinerDrone u){
            target = null;
        }

        @Override
        public void update(MinerDrone u){
            TileEntity entity = getClosestCore();

            if(entity == null) return;

            findItem();

            //core full of the target item, do nothing
            if(targetItem != null && entity.block.acceptStack(targetItem, 1, entity.tile, MinerDrone.this) == 0){
                MinerDrone.this.clearItem();
                return;
            }

            //if inventory is full, drop it off.
            if(item.amount >= getItemCapacity() || (targetItem != null && !acceptsItem(targetItem))){
                setState(drop);
            }else{
                if(retarget() && targetItem != null){
                    target = indexer.findClosestOre(x, y, targetItem);
                }

                if(target instanceof Tile){
                    moveTo(type.range / 1.5f);

                    if(dst(target) < type.range && mineTile != target){
                        setMineTile((Tile)target);
                    }

                    if(((Tile)target).block() != Blocks.air){
                        setState(drop);
                    }
                }else{
                    //nothing to mine anymore, core full: circle spawnpoint
                    if(getSpawner() != null){
                        target = getSpawner();

                        circle(40f);
                    }
                }
            }
        }

        @Override
        public void exit(MinerDrone u){
            setMineTile(null);
        }
    },

    drop = new UnitState<MinerDrone>(){
        @Override
        public void enter(MinerDrone u){
            target = null;
        }

        @Override
        public void update(MinerDrone u){
            if(item.amount == 0 || item.item.type != ItemType.material){
                clearItem();
                setState(mine);
                return;
            }

            target = getClosestCore();

            if(target == null) return;

            TileEntity tile = (TileEntity)target;

            if(dst(target) < type.range){
                if(tile.tile.block().acceptStack(item.item, item.amount, tile.tile, MinerDrone.this) > 0){
                    Call.transferItemTo(item.item, item.amount, x, y, tile.tile);
                }

                clearItem();
                setState(mine);
            }

            circle(type.range / 1.8f);
        }
    };

    @Override
    public UnitState getStartState(){
        return mine;
    }

    @Override
    public void update(){
        super.update();

        updateMining();
    }

    @Override
    protected void updateRotation(){
        if(mineTile != null && shouldRotate() && mineTile.dst(this) < type.range){
            rotation = Mathf.slerpDelta(rotation, angleTo(mineTile), 0.3f);
        }else{
            rotation = Mathf.slerpDelta(rotation, velocity.angle(), 0.3f);
        }
    }

    @Override
    public boolean shouldRotate(){
        return isMining();
    }

    @Override
    public void drawOver(){
        drawMining();
    }

    @Override
    public boolean canMine(Item item){
        return type.toMine.contains(item);
    }

    @Override
    public float getMinePower(){
        return type.minePower;
    }

    @Override
    public Tile getMineTile(){
        return mineTile;
    }

    @Override
    public void setMineTile(Tile tile){
        mineTile = tile;
    }

    @Override
    public void write(DataOutput data) throws IOException{
        super.write(data);
        data.writeInt(mineTile == null || !state.isInState(mine) ? Pos.invalid : mineTile.pos());
    }

    @Override
    public void read(DataInput data) throws IOException{
        super.read(data);
        mineTile = world.tile(data.readInt());
    }

    protected void findItem(){
        TileEntity entity = getClosestCore();
        if(entity == null){
            return;
        }
        targetItem = Structs.findMin(type.toMine, indexer::hasOre, (a, b) -> -Integer.compare(entity.items.get(a), entity.items.get(b)));
    }
}
