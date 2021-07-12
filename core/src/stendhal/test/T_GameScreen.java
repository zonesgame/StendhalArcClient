package stendhal.test;

import arc.util.Log;
import games.stendhal.client.StaticGameLayers;
import games.stendhal.client.StendhalClient;
import temp.java.awt.Graphics2D;

public class T_GameScreen {

    public StaticGameLayers gameLayers;

    private Graphics2D _batch;

    public T_GameScreen() {
        gameLayers = new StaticGameLayers();
    }

    public T_GameScreen(StendhalClient client) {
        gameLayers = client.getStaticGameLayers();
        if (gameLayers != null)
            Log.info(gameLayers.getWidth() + "    " + gameLayers.getAreaName() + "   " );
    }

    public void draw() {
        final String set = gameLayers.getAreaName();
        gameLayers.drawLayersZones(_batch, set, "floor_bundle", 0, 0, (int) gameLayers.getWidth(), (int) gameLayers.getHeight(),
                "blend_ground", "0_floor", "1_terrain", "2_object");
    }
}
