package mindustry.game;

import arc.Core;

/**
 *  回合之间的时间预设, 目前并未使用.<p/>
 * Presets for time between waves. Currently unused.
 * */
public enum Difficulty{
    /** 容易*/
    easy(1.4f),
    /** 一般*/
    normal(1f),
    /** 困难*/
    hard(0.5f),
    /** 疯狂*/
    insane(0.25f);

    /** 回合之间的时间倍增器.<p/>Multiplier of the time between waves. */
    public final float waveTime;

    /** 难度本地化名称*/
    private String value;

    Difficulty(float waveTime){
        this.waveTime = waveTime;
    }

    @Override
    public String toString(){
        if(value == null){
            value = Core.bundle.get("setting.difficulty." + name());
        }
        return value;
    }
}
