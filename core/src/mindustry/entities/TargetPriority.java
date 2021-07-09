package mindustry.entities;

/**
 *  目标优先级, 用于被单位锁定.<p/>
 *  更高的顺序意味着更高的优先级. 更高的优先级块将总是以较低的优先级为目标,而不考虑距离.<p/>
 * A higher ordinal means a higher priority. Higher priority blocks will always get targeted over those of lower priority, regardless of distance.
 * */
public enum TargetPriority{
    /** 基础*/
    base,
    /** 炮塔*/
    turret
}
