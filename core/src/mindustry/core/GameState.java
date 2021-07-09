package mindustry.core;

import arc.*;
import mindustry.entities.type.*;
import mindustry.game.EventType.*;
import mindustry.game.*;

import static mindustry.Vars.*;

/**
 *  游戏状态数据
 * */
public class GameState{
    /** 当前回合数<p/>Current wave number, can be anything in non-wave modes. */
    public int wave = 1;
    /** 回合冷却计时器<p/>Wave countdown in ticks. */
    public float wavetime;
    /** 游戏结束和下水状态<p/>Whether the game is in game over state. */
    public boolean gameOver = false, launched = false;
    /** 当前游戏规则<p/>The current game rules. */
    public Rules rules = new Rules();
    /** 游戏的统计数据,用于保存游戏后显示游戏状态使用.<p/>Statistics for this save/game. Displayed after game over. */
    public Stats stats = new Stats();
    /** 队伍数据<p/>Team data. Gets reset every new game. */
    public Teams teams = new Teams();
    /** 敌人数量;用于服务器使用<p/>Number of enemies in the game; only used clientside in servers. */
    public int enemies;
    /** 当前游戏状态<p/>Current game state. */
    private State state = State.menu;

    public BaseUnit boss(){
        return unitGroup.find(u -> u.isBoss() && u.getTeam() == rules.waveTeam);
    }

    public void set(State astate){
        Events.fire(new StateChangeEvent(state, astate));
        state = astate;
    }

    public boolean isEditor(){
        return rules.editor;
    }

    public boolean isPaused(){
        return (is(State.paused) && !net.active()) || (gameOver && !net.active());
    }

    public boolean is(State astate){
        return state == astate;
    }

    public State getState(){
        return state;
    }


    /**
     *  app中的状态PS: 暂停, 游戏, 界面等.
     * */
    public enum State{
        paused, playing, menu
    }
}
