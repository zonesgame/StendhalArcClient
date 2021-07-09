package z.ai.units;

import arc.struct.Array;
import arc.struct.IntArray;
import mindustry.entities.type.BaseUnit;
import mindustry.game.Team;
import z.ai.components.Squad;
import z.utils.FinalCons;

import static mindustry.Vars.player;

/**
 *  队伍策略管理系统, 和黑板数据保存
 */
public class StrategySystem {

    /** 游戏队伍管理器*/
    private Squad<BaseUnit>[][] squadGroup;
    /** 队伍编队ID*/
    private IntArray[] squadIDPro;

    /** 队伍ai策略*/
    public Array<GroupStrategy>[] teamsGroupStrategy;

    public StrategySystem() {
      init();
    }

    public void init() {
        IntArray tempIntArray = new IntArray();
        for (int value = FinalCons.max_squad_count; --value >=0; ) {
            tempIntArray.add(value);
        }

        squadIDPro = new IntArray[FinalCons.max_squad_count];
        for (int i = squadIDPro.length; --i >= 0; ) {
            squadIDPro[i] = new IntArray(tempIntArray);
        }

        squadGroup = new Squad[Team.all().length][FinalCons.max_squad_count];
        for (int t = 0; t < squadGroup.length; t++) {       // temp code
            for (int s = 0, len = squadGroup[t].length; s < len; s++) {
                int teamid = Team.all()[t].id;
                squadGroup[teamid][s] = new Squad(teamid, s);
            }
        }

        teamsGroupStrategy = new Array[FinalCons.max_squad_count];
        for (int i = teamsGroupStrategy.length; --i >= 0; ) {
            teamsGroupStrategy[i] = new Array<>();
        }
    }

    /** 更新队伍策略*/
    public void updateSystem() {
        for (Array<GroupStrategy> groupStrategies : teamsGroupStrategy) {
            for (GroupStrategy strategy : groupStrategies) {
                strategy.update();
            }
        }
    }

    public Squad popSquad() {
        if (player == null)
            return null;

        return popSquad(player.getTeam());
    }

    public Squad popSquad(Team team) {
        if (squadIDPro[team.id].size == 0)
            return null;

        int squadID = squadIDPro[team.id].pop();
        Squad squad = getSquad(team.id, squadID);
        squad.setValid(true);

        return squad;
//            blackboard = new SquadBlackboard();
    }

    public void free(Squad squad) {
        if (player == null) return;

        freeSquad(player.getTeam(), squad);
    }

    public void freeSquad(Team team, Squad squad) {
        Squad[] teamSquads = squadGroup[team.id];

        for (int i = teamSquads.length; --i >= 0; ) {
            if (teamSquads[i] == squad) {
                squad.clearMember();
                squad.setValid(false);
                squadIDPro[team.id].add(i);
                break;
            }
        }
    }

    public Squad getSquad(Team team, int squadid) {
        return squadGroup[team.id][squadid];
    }

    public Squad getSquad(int teamid, int squadid) {
        return squadGroup[teamid][squadid];
    }

    public Squad getSquad(int squadid) {
        return squadGroup[player.getTeam().id][squadid];
    }

    public Squad[] getTeamSquads(Team team) {
        return squadGroup[team.id];
    }

    public Squad[] getTeamSquads(int teamid) {
        return squadGroup[teamid];
    }

}
