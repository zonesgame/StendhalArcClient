package z.ai.utils;

import arc.ai.fma.FormationMember;
import arc.ai.utils.Location;
import arc.math.geom.Vec2;
import arc.util.Time;
import mindustry.Vars;
import mindustry.world.Tile;
import z.ai.components.Squad;

import static mindustry.Vars.pathfinder;
import static mindustry.Vars.world;

/**
 *  队伍成员
 */
public class SquadMember implements FormationMember<Vec2> {

    /** 成员位置*/
    private Location2 location = new Location2();

    /** 是否更新移动位置目标点*/
    private boolean updateLocation = true;
    /** 是否为队伍指挥官*/
    public boolean isCommander = false;

    /** 队伍id, value-1未加入队伍.*/
    public int squadID = -1;
    /** 成员id, 用于路径算法使用*/
    public int memberID = -1;
    @Deprecated
    /** 队伍id*/
    public int teamID = -1;

    /** 初始化队伍成员*/
    public void init(int teamid, int squadid, int memeberid) {
        this.teamID = teamid;
        this.squadID = squadid;
        this.memberID = memeberid;
        // temp code
//        setPathTarget(teamid);
    }

    /** 设置地图移动目标点*/
    public void setPathTarget(int teamID) {
        Tile tile = world.tile(location.getX(), location.getY());
        if (tile == null) {
            tile = world.tile(getSquad().getTarget().getPosition().x, getSquad().getTarget().getPosition().y);
        }
        pathfinder.squadTargetTile[teamID][squadID][memberID] = tile;
        updateLocation = true;      // temp code
        stopDelta = 0;
    }

    /** 获取团队管理器*/
    @Deprecated
    public Squad getSquad() {
        if (teamID == -1)    return null;
        return Vars.systemStrategy.getSquad(teamID, squadID);
    }

    /** 成员位置*/
    public Vec2 getPosition() {
        return location.getPosition();
    }

    /** 成员目标位置*/
    @Override
    public Location<Vec2> getTargetLocation() {
        return location;
    }


    @Deprecated
    private float stopDelta= 0, stopStep = 30f; // 0.5 second
    @Deprecated
    public void updateLocationDelta() {
        stopDelta += Time.delta();
        if (stopDelta > stopStep)
            updateLocation = false;
    }

    @Deprecated
    public boolean updateLocation() {
        return updateLocation;
    }

    @Deprecated
    public void setUpdateLocation(boolean updateLocation) {
        if (updateLocation)
            stopDelta= 0;
        this.updateLocation = updateLocation;
    }
}
