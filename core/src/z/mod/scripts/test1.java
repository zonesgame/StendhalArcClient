package z.mod.scripts;

import arc.Events;
import arc.math.Mathf;
import mindustry.Vars;
import mindustry.content.UnitTypes;
import mindustry.entities.type.BaseUnit;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.world.Tile;
import z.ai.components.Squad;
import z.ai.units.GroupStrategy;

import static mindustry.Vars.net;
import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;

/**
 *  Scripts execute
 */
public class test1 {

    public test1() {
        init();
    }

    private void init() {
        if(!net.client()){
            Tile tile = world.tile(14, 14);
            int count = 1;

            Team enemyTeam = Team.crux;
            GroupStrategy enemyGroupStrategy = new GroupStrategy(enemyTeam);
            Vars.systemStrategy.teamsGroupStrategy[enemyTeam.id].add(enemyGroupStrategy);
            Squad enemySquad = Vars.systemStrategy.popSquad(enemyTeam);

            while (--count >= 0) {
                BaseUnit unit = UnitTypes.testGroundUse.create(enemyTeam);
//                ((BaseGroundUnit) unit).startState = ((BaseGroundUnit) unit).machineState;
                unit.setSpawner(tile);
//            unit.set(tile.drawx() + Mathf.range(4), tile.drawy() + Mathf.range(4));     // default
                unit.set(tile.getX() + Mathf.range(4 / (float)tilesize), tile.getY() + Mathf.range(4 / (float)tilesize));
                unit.add();
//            unit.velocity().y = factory.launchVelocity;
                // new add begon
//                Squad squad = Vars.systemStrategy.getSquad(Team.green.id, count);
//                squad.setValid(true);
                enemySquad.addMember(unit);
                // new add end
                Events.fire(new EventType.UnitCreateEvent(unit));
            }

            tile = world.tile(28, 28);  // default  28
            count = 1;

            Team ownTeam = Team.sharded;
            GroupStrategy ownGroupStrategy = new GroupStrategy(ownTeam);
            Vars.systemStrategy.teamsGroupStrategy[ownTeam.id].add(ownGroupStrategy);
            Squad ownSquad = Vars.systemStrategy.popSquad(ownTeam);

            while (--count >= 0) {
                BaseUnit unit = UnitTypes.testGroundUse.create(ownTeam);
//                ((BaseGroundUnit) unit).startState = ((BaseGroundUnit) unit).squadState;
                unit.setSpawner(tile);
//            unit.set(tile.drawx() + Mathf.range(4), tile.drawy() + Mathf.range(4));     // default
                unit.set(tile.getX() + Mathf.range(4 / (float)tilesize), tile.getY() + Mathf.range(4 / (float)tilesize));
                unit.add();
//            unit.velocity().y = factory.launchVelocity;
                // new add begon
//                Squad squad = Vars.systemStrategy.getSquad(Team.sharded.id, count);
//                squad.setValid(true);
//                squad.addMember(unit);
                ownSquad.addMember(unit);
                // new add end
                Events.fire(new EventType.UnitCreateEvent(unit));
            }
        }
    }

}
