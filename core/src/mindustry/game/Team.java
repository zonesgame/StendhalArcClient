package mindustry.game;

import arc.*;
import arc.graphics.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.game.Teams.*;
import mindustry.graphics.*;
import mindustry.world.blocks.storage.CoreBlock.*;

import static mindustry.Vars.*;

/**
 *  队伍
 * */
public class Team implements Comparable<Team>{
    /** 队伍id*/
    public final byte id;
    /** 队伍颜色*/
    public final Color color;
    /** 队伍名称*/
    public String name;

    /** 所有登记队伍.<p/>All 256 registered teams. */
    private static final Team[] all = new Team[6];    // default  256
    /** 基础队伍用于编辑器使用.<p/>The 6 base teams used in the editor. */
    private static final Team[] baseTeams = new Team[6];

    /** 遗弃队伍*/
    public final static Team
        derelict = new Team(0, "derelict", Color.valueOf("4d4e58")),
        /** 公用队伍*/
        sharded = new Team(1, "sharded", Pal.accent.cpy()),
        /** 敌对队伍*/
        crux = new Team(2, "crux", Color.valueOf("e82d2d")),
        /** 绿色队伍*/
        green = new Team(3, "green", Color.valueOf("4dd98b")),
        /** 紫色队伍*/
        purple = new Team(4, "purple", Color.valueOf("9a4bdf")),
        /** 蓝色队伍*/
        blue = new Team(5, "blue", Color.royal.cpy());

    static{     // 登记所有队伍颜色
        Mathf.random.setSeed(8);
        //create the whole 256 placeholder teams
        for(int i = 6; i < all.length; i++){
            new Team(i, "team#" + i, Color.HSVtoRGB(360f * Mathf.random(), 100f * Mathf.random(0.6f, 1f), 100f * Mathf.random(0.8f, 1f), 1f));
        }
        Mathf.random.setSeed(new Rand().nextLong());
    }

    /** 获取指定ID队伍*/
    public static Team get(int id){
        return all[Pack.u((byte)id)];
    }

    /** @return 获取基础队伍列表.<p/>the 6 base team colors. */
    public static Team[] base(){
        return baseTeams;
    }

    /** @return 获取登记队伍列表.<p/>all the teams - do not use this for lookup! */
    public static Team[] all(){
        return all;
    }

    /** 构建队伍*/
    protected Team(int id, String name, Color color){
        this.name = name;
        this.color = color;
        this.id = (byte)id;

        int us = Pack.u(this.id);
        if(us < 6) baseTeams[us] = this;
        all[us] = this;
    }

    /** 敌对队伍容器*/
    public Array<Team> enemies(){
        return state.teams.enemiesOf(this);
    }

    /** 当前队伍数据*/
    public TeamData data(){
        return state.teams.get(this);
    }

    /** 队伍核心*/
    public CoreEntity core(){
        return data().core();
    }

    /** 队伍激活状态*/
    public boolean active(){
        return state.teams.isActive(this);
    }

    /** 与指定队伍是否敌对*/
    public boolean isEnemy(Team other){
        return state.teams.areEnemies(this, other);
    }

    /** 队伍核心容器*/
    public Array<CoreEntity> cores(){
        return state.teams.cores(this);
    }

    /** 队伍本地化名称*/
    public String localized(){
        return Core.bundle.get("team." + name + ".name", name);
    }

    @Override
    public int compareTo(Team team){
        return Integer.compare(id, team.id);
    }

    @Override
    public String toString(){
        return name;
    }
}
