package mindustry.game;

import mindustry.annotations.Annotations.Serialize;
import arc.struct.Array;
import arc.struct.ObjectIntMap;
import arc.math.Mathf;
import mindustry.type.*;

@Serialize
public class Stats{
    /** 物品全局资源计数器.<p/>Items delivered to global resoure counter. Zones only. */
    public ObjectIntMap<Item> itemsDelivered = new ObjectIntMap<>();
    /** 敌人单位被销毁数量.<p/>Enemy (red team) units destroyed. */
    public int enemyUnitsDestroyed;
    /** 当前回合数.<p/>Total waves lasted. */
    public int wavesLasted;
    /** 在地图中持续的总时间.<p/>Total (ms) time lasted in this save/zone. */
    public long timeLasted;
    /** 友好建造者建造.<p/>Friendly buildings fully built. */
    public int buildingsBuilt;
    /** 友好建造者拆除.<p/>Friendly buildings fully deconstructed. */
    public int buildingsDeconstructed;
    /** 友好建造者销毁.<p/>Friendly buildings destroyed. */
    public int buildingsDestroyed;

    /** 计算排名*/
    @Deprecated
    public RankResult calculateRank(Zone zone, boolean launched){
        float score = 0;

        if(launched && zone.getRules().attackMode){
            score += 3f;
        }else if(wavesLasted >= zone.conditionWave){
            //each new launch period adds onto the rank 'points'
            score += (float)((wavesLasted - zone.conditionWave) / zone.launchPeriod + 1) * 1.2f;
        }

        int capacity = zone.loadout.findCore().itemCapacity[0];

        //weigh used fractions
        float frac = 0f;
        Array<Item> obtainable = Array.with(zone.resources).select(i -> i.type == ItemType.material);
        for(Item item : obtainable){
            frac += Mathf.clamp((float)itemsDelivered.get(item, 0) / capacity) / (float)obtainable.size;
        }

        score += frac * 1.6f;

        if(!launched){
            score *= 0.5f;
        }

        int rankIndex = Mathf.clamp((int)(score), 0, Rank.values().length - 1);
        Rank rank = Rank.values()[rankIndex];
        String sign = Math.abs((rankIndex + 0.5f) - score) < 0.2f || rank.name().contains("S") ? "" : (rankIndex + 0.5f) < score ? "-" : "+";

        return new RankResult(rank, sign);
    }

    /** 排名*/
    public static class RankResult{
        public final Rank rank;
        /** + or - */
        public final String modifier;

        public RankResult(Rank rank, String modifier){
            this.rank = rank;
            this.modifier = modifier;
        }
    }

    /** 成就*/
    public enum Rank{
        F, D, C, B, A, S, SS
    }
}
