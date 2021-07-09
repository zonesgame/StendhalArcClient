package mindustry.entities.type.base;

import arc.math.Mathf;
import arc.math.geom.Geometry;
import mindustry.entities.units.*;
import mindustry.world.Tile;
import mindustry.world.meta.BlockFlag;

import static mindustry.Vars.*;

/**
 *  基础无人机
 * */
public abstract class BaseDrone extends FlyingUnit{
    /** 撤退状态*/
    public final UnitState retreat = new UnitState<BaseDrone>(){
        @Override
        public void enter(BaseDrone u){
            target = null;
        }

        @Override
        public void update(BaseDrone u){
            if(health >= maxHealth()){
                state.changeState(getStartState());
            }else if(!targetHasFlag(BlockFlag.repair)){
                if(retarget()){
                    Tile repairPoint = Geometry.findClosest(x, y, indexer.getAllied(team, BlockFlag.repair));
                    if(repairPoint != null){
                        target = repairPoint;
                    }else{
                        setState(getStartState());
                    }
                }
            }else{
                circle(40f);
            }
        }
    };

    public boolean countsAsEnemy(){
        return false;
    }

    @Override
    public void onCommand(UnitCommand command){
        //do nothing, normal commands are not applicable here
    }

    @Override
    protected void updateRotation(){
        if(target != null && shouldRotate() && target.dst(this) < type.range){
            rotation = Mathf.slerpDelta(rotation, angleTo(target), 0.3f);
        }else{
            rotation = Mathf.slerpDelta(rotation, velocity.angle(), 0.3f);
        }
    }

    @Override
    public void behavior(){
        if(health <= maxHealth() * type.retreatPercent && !state.isInState(retreat) && Geometry.findClosest(x, y, indexer.getAllied(team, BlockFlag.repair)) != null){
            setState(retreat);
        }
    }

    public boolean shouldRotate(){
        return state.isInState(getStartState());
    }

    @Override
    public abstract UnitState getStartState();

}
