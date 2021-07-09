package mindustry.game;

import arc.*;
import arc.util.ArcAnnotate.*;
import mindustry.type.*;
import mindustry.world.*;

/**
 *  战役地图需求的目标种类.<p/>
 * Holds objective classes.
 * */
public class Objectives{

    //TODO
    /** 当前地图完成回合目标*/
    public static class Wave implements Objective{
        /** 需要完成回合数*/
        public int wave;

        public Wave(int wave){
            this.wave = wave;
        }

        protected Wave(){}

        @Override
        public boolean complete(){
            return false;
        }

        @Override
        public String display(){
            //TODO
            return null;
        }
    }

    /** 解锁内容目标*/
    public static class Unlock implements Objective{
        /** 需要解锁的块*/
        public @NonNull Block block;

        public Unlock(Block block){
            this.block = block;
        }

        protected Unlock(){}

        @Override
        public boolean complete(){
            return block.unlocked();
        }

        @Override
        public String display(){
            return Core.bundle.format("requirement.unlock", block.localizedName);
        }
    }

    /** 指定地图完成回合数目标*/
    public static class ZoneWave extends ZoneObjective{
        public int wave;

        public ZoneWave(Zone zone, int wave){
            this.zone = zone;
            this.wave = wave;
        }

        protected ZoneWave(){}

        @Override
        public boolean complete(){
            return zone.bestWave() >= wave;
        }

        @Override
        public String display(){
            return Core.bundle.format("requirement.wave", wave, zone.localizedName);
        }
    }

    /** 当前地图完成发射目标*/
    public static class Launched extends ZoneObjective{

        public Launched(Zone zone){
            this.zone = zone;
        }

        protected Launched(){}

        @Override
        public boolean complete(){
            return zone.hasLaunched();
        }

        @Override
        public String display(){
            return Core.bundle.format("requirement.core", zone.localizedName);
        }
    }


    /**
     *  战役地图目标, 扩展基类.
     * */
    public abstract static class ZoneObjective implements Objective{
        public @NonNull Zone zone;
    }
}
