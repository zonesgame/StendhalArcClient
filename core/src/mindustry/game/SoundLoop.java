package mindustry.game;

import arc.audio.*;
import arc.math.*;
import arc.util.*;

/**
 *  指定位置播放循环音频.<p/>
 * A simple class for playing a looping sound at a position.
 * */
public class SoundLoop{
    /** 衰减速度*/
    private static final float fadeSpeed = 0.05f;

    /** 音频*/
    private final Sound sound;
    /** 音频ID*/
    private int id = -1;
    /** 音量*/
    private float volume, /** 基础音量*/baseVolume;

    /** 构建循环音频*/
    public SoundLoop(Sound sound, float baseVolume){
        this.sound = sound;
        this.baseVolume = baseVolume;
    }

    /** 更新音频位置播放状态*/
    public void update(float x, float y, boolean play){
        if(baseVolume < 0) return;

        if(id < 0){
            if(play){
                id = sound.loop(sound.calcVolume(x, y) * volume * baseVolume, 1f, sound.calcPan(x, y));
            }
        }else{
            //fade the sound in or out
            if(play){
                volume = Mathf.clamp(volume + fadeSpeed * Time.delta());
            }else{
                volume = Mathf.clamp(volume - fadeSpeed * Time.delta());
                if(volume <= 0.001f){
                    sound.stop(id);
                    id = -1;
                    return;
                }
            }
            sound.setPan(id, sound.calcPan(x, y), sound.calcVolume(x, y) * volume * baseVolume);
        }
    }

    /** 停止音频播放*/
    public void stop(){
        if(id != -1){
            sound.stop(id);
            id = -1;
            volume = baseVolume = -1;
        }
    }
}
