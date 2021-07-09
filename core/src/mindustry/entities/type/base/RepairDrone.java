package mindustry.entities.type.base;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import mindustry.entities.Units;
import mindustry.entities.type.TileEntity;
import mindustry.entities.units.UnitState;
import mindustry.world.Pos;
import mindustry.world.Tile;
import mindustry.world.blocks.BuildBlock;

import static mindustry.Vars.world;

public class RepairDrone extends BaseDrone{
    public final UnitState repair = new UnitState<RepairDrone>(){
        @Override
        public void enter(RepairDrone u){
            target = null;
        }

        @Override
        public void update(RepairDrone u){

            if(retarget()){
                target = Units.findDamagedTile(team, x, y);
            }

            if(target instanceof TileEntity && ((TileEntity)target).block instanceof BuildBlock){
                target = null;
            }

            if(target != null){
                if(target.dst(RepairDrone.this) > type.range){
                    circle(type.range * 0.9f);
                }else{
                    getWeapon().update(RepairDrone.this, target.getX(), target.getY());
                }
            }else{
                //circle spawner if there's nothing to repair
                if(getSpawner() != null){
                    target = getSpawner();
                    circle(type.range * 1.5f, type.speed/2f);
                    target = null;
                }
            }
        }
    };

    @Override
    public boolean shouldRotate(){
        return target != null;
    }

    @Override
    public UnitState getStartState(){
        return repair;
    }

    @Override
    public void write(DataOutput data) throws IOException{
        super.write(data);
        data.writeInt(state.isInState(repair) && target instanceof TileEntity ? ((TileEntity)target).tile.pos() : Pos.invalid);
    }

    @Override
    public void read(DataInput data) throws IOException{
        super.read(data);
        Tile repairing = world.tile(data.readInt());

        if(repairing != null){
            target = repairing.entity;
        }
    }
}
