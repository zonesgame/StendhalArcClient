package stendhal.test;

import arc.graphics.g2d.Draw;
import arc.util.Log;
import games.stendhal.client.EntityViewManager;
import games.stendhal.client.GameObjects;
import games.stendhal.client.StaticGameLayers;
import games.stendhal.client.StendhalClient;
import games.stendhal.client.Zone;
import games.stendhal.client.entity.IEntity;
import temp.java.awt.Graphics2D;
import temp.java.awt.Rectangle;

public class T_GameScreen {

    public StaticGameLayers gameLayers;
    private Graphics2D _batch;

    private EntityViewManager viewManager;
    private GameObjects.GameObjectListener objectListener;
    private StendhalClient.ZoneChangeListener zoneChangeListener;

    private Rectangle clip = new Rectangle();

    public T_GameScreen() {
        gameLayers = new StaticGameLayers();
    }

    public T_GameScreen(StendhalClient client) {
        _batch = new Graphics2D();
        gameLayers = client.getStaticGameLayers();

        initListener();
    }

    public void draw() {
        final String set = gameLayers.getAreaName();
        gameLayers.drawLayersZones(_batch, set, "floor_bundle", 0, 0, (int) gameLayers.getWidth(), (int) gameLayers.getHeight(),
                "blend_ground", "0_floor", "1_terrain", "2_object");
        Draw.flush();

        {       // entity draw
            clip.setBounds(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
            viewManager.prepareViews(clip, false);
            viewManager.draw(_batch);
            viewManager.drawTop(_batch);
        }
        Draw.flush();

        gameLayers.drawLayersZones(_batch, set, "roof_bundle", 0, 0,
                (int) gameLayers.getWidth(), (int) gameLayers.getHeight(), "blend_roof", "3_roof", "4_roof_add");
    }


    private void initListener() {
        viewManager = new EntityViewManager();

        objectListener = new GameObjects.GameObjectListener() {
            @Override
            public void addEntity(IEntity entity) {
                viewManager.addEntity(entity);
            }

            @Override
            public void removeEntity(IEntity entity) {
                viewManager.removeEntity(entity);
            }
        };

        zoneChangeListener = new StendhalClient.ZoneChangeListener() {
            @Override
            public void onZoneChange(Zone zone) {

            }

            @Override
            public void onZoneChangeCompleted(Zone zone) {

            }

            @Override
            public void onZoneUpdate(Zone zone) {
                viewManager.resetViews();
            }
        };

        StendhalClient.get().addZoneChangeListener(zoneChangeListener);
        StendhalClient.get().getGameObjects().addGameObjectListener(objectListener);
    }
}
