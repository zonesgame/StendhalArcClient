package z.ai.units;

import arc.struct.Array;
import mindustry.game.Team;
import z.ai.components.Squad;

/**
 *
 */
public class GroupStrategy {

    private Team team;
    private Array<Squad> groups = new Array<>(8);

    public GroupStrategy(Team team) {
        this.team = team;
    }

    public void update() {

    }
}
