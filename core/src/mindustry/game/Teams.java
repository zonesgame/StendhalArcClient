package mindustry.game;

import arc.func.Boolf;
import arc.func.Cons;
import arc.math.geom.Geometry;
import arc.struct.Array;
import arc.struct.Queue;
import arc.util.ArcAnnotate.Nullable;
import arc.util.Pack;
import mindustry.entities.type.TileEntity;
import mindustry.world.blocks.storage.CoreBlock.CoreEntity;

import static mindustry.Vars.indexer;
import static mindustry.Vars.state;

/**
 *  基于队伍和单位的实用类.<p/>
 * Class for various team-based utilities.
 * */
public class Teams{
    /** 地图队伍id数据.<p/>Maps team IDs to team data. */
    private TeamData[] map = new TeamData[256];
    /** 激活队伍容器.<p/>Active teams. */
    private Array<TeamData> active = new Array<>();

    /** 构建队伍管理器*/
    public Teams(){
        active.add(get(Team.crux));
    }

    /**  距离指定队伍最近的敌对核心*/
    public @Nullable CoreEntity closestEnemyCore(float x, float y, Team team){
        for(TeamData data : active){
            if(areEnemies(team, data.team)){
                CoreEntity tile = Geometry.findClosest(x, y, data.cores);
                if(tile != null){
                    return tile;
                }
            }
        }
        return null;
    }

    /** 距离指定队伍最近的我方核心*/
    public @Nullable CoreEntity closestCore(float x, float y, Team team){
        return Geometry.findClosest(x, y, get(team).cores);
    }

    /** 指定队伍的敌对队伍容器*/
    public Array<Team> enemiesOf(Team team){
        return get(team).enemies;
    }

    /** 遍历指定队伍敌对队伍核心, 并检测是否有满足指定条件*/
    public boolean eachEnemyCore(Team team, Boolf<CoreEntity> ret){
        for(TeamData data : active){
            if(areEnemies(team, data.team)){
                for(CoreEntity tile : data.cores){
                    if(ret.get(tile)){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /** 遍历指定队伍敌对队伍, 并执行指定代码*/
    public void eachEnemyCore(Team team, Cons<TileEntity> ret){
        for(TeamData data : active){
            if(areEnemies(team, data.team)){
                for(TileEntity tile : data.cores){
                    ret.get(tile);
                }
            }
        }
    }

    /** 返回指定队伍, 队伍数据.<p/>Returns team data by type. */
    public TeamData get(Team team){
        if(map[Pack.u(team.id)] == null){
            map[Pack.u(team.id)] = new TeamData(team);
        }
        return map[Pack.u(team.id)];
    }

    /** 玩家核心容器*/
    public Array<CoreEntity> playerCores(){
        return get(state.rules.defaultTeam).cores;
    }

    /** 指定队伍核心容器, 不要修改.<p/>Do not modify! */
    public Array<CoreEntity> cores(Team team){
        return get(team).cores;
    }

    /** 返回指定队伍是否活动, PS: 是否有任何核心.<p/>Returns whether a team is active, e.g. whether it has any cores remaining. */
    public boolean isActive(Team team){
        //the enemy wave team is always active
        return get(team).active();
    }

    /** 返回指定队伍是否敌对.<p/>Returns whether {@param other} is an enemy of {@param #team}. */
    public boolean areEnemies(Team team, Team other){
        return team != other;
    }

    /** 返回指定队伍是否可交互*/
    public boolean canInteract(Team team, Team other){
        return team == other || other == Team.derelict;
    }

    /** 获取活动队伍容器, 不要修改.<p/>Do not modify. */
    public Array<TeamData> getActive(){
        active.removeAll(t -> !t.active());
        return active;
    }

    /** 登记核心*/
    public void registerCore(CoreEntity core){
        TeamData data = get(core.getTeam());
        //add core if not present
        if(!data.cores.contains(core)){
            data.cores.add(core);
        }

        //register in active list if needed
        if(data.active() && !active.contains(data)){
            active.add(data);
            updateEnemies();
            indexer.updateTeamIndex(data.team);
        }
    }

    /** 解除核心登记*/
    public void unregisterCore(CoreEntity entity){
        TeamData data = get(entity.getTeam());
        //remove core
        data.cores.remove(entity);
        //unregister in active list
        if(!data.active()){
            active.remove(data);
            updateEnemies();
        }
    }

    /** 更新敌人*/
    private void updateEnemies(){
        if(state.rules.waves && !active.contains(get(state.rules.waveTeam))){
            active.add(get(state.rules.waveTeam));
        }

        for(TeamData data : active){
            data.enemies.clear();
            for(TeamData other : active){
                if(areEnemies(data.team, other.team)){
                    data.enemies.add(other.team);
                }
            }
        }
    }

    // zones add begon
    /** 距离指定队伍最近的我方核心*/
//    public @Nullable SpawnPlayerOverlayFloorEntity closestSpawnPlayerPoint(float x, float y, Team team){
//        return Geometry.findClosest(x, y, get(team).cores);
//    }
    // zones add end


    /**
     *  队伍数据
     * */
    public class TeamData{
        /** 队伍核心容器*/
        public final Array<CoreEntity> cores = new Array<>();
        /** 敌对队伍容器*/
        public final Array<Team> enemies = new Array<>();
        /** 绑定队伍*/
        public final Team team;
        /** 销毁块队列*/
        public Queue<BrokenBlock> brokenBlocks = new Queue<>();

        /** 构建队伍数据*/
        public TeamData(Team team){
            this.team = team;
        }

        /** 队伍活动状态*/
        public boolean active(){
            return (team == state.rules.waveTeam && state.rules.waves) || cores.size > 0;
        }

        /** 队伍是否拥有核心*/
        public boolean hasCore(){
            return cores.size > 0;
        }

        /** 核心是否为空*/
        public boolean noCores(){
            return cores.isEmpty();
        }

        /** 获取第一个核心*/
        public CoreEntity core(){
            return cores.first();
        }

        @Override
        public String toString(){
            return "TeamData{" +
            "cores=" + cores +
            ", team=" + team +
            '}';
        }
    }

    /**  代表由这个在地图上被摧毁的团队所建造的块. 这不包括团队拆除的块.<p/>
     * Represents a block made by this team that was destroyed somewhere on the map.
     * This does not include deconstructed blocks.*/
    public static class BrokenBlock{
        /** 位置, 角度和块ID*/
        public final short x, y, rotation, block;
        /** 块配置数据*/
        public final int config;

        public BrokenBlock(short x, short y, short rotation, short block, int config){
            this.x = x;
            this.y = y;
            this.rotation = rotation;
            this.block = block;
            this.config = config;
        }
    }
}
