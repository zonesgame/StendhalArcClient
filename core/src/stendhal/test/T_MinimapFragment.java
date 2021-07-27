package stendhal.test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.SwingUtilities;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.input.*;
import arc.math.*;
import arc.scene.*;
import arc.scene.event.*;
import arc.scene.ui.Dialog;
import arc.scene.ui.layout.*;
import arc.util.Align;
import arc.util.Time;
import games.stendhal.client.GameObjects;
import games.stendhal.client.StendhalClient;
import games.stendhal.client.Zone;
import games.stendhal.client.entity.DomesticAnimal;
import games.stendhal.client.entity.EntityChangeListener;
import games.stendhal.client.entity.FlyOverArea;
import games.stendhal.client.entity.HousePortal;
import games.stendhal.client.entity.IEntity;
import games.stendhal.client.entity.Player;
import games.stendhal.client.entity.Portal;
import games.stendhal.client.entity.RPEntity;
import games.stendhal.client.entity.User;
import games.stendhal.client.entity.WalkBlocker;
import games.stendhal.client.entity.Wall;
import games.stendhal.client.gui.map.DomesticAnimalMapObject;
import games.stendhal.client.gui.map.FlyOverAreaMapObject;
import games.stendhal.client.gui.map.MapObject;
import games.stendhal.client.gui.map.MapPanel;
import games.stendhal.client.gui.map.MovingMapObject;
import games.stendhal.client.gui.map.PlayerMapObject;
import games.stendhal.client.gui.map.PortalMapObject;
import games.stendhal.client.gui.map.RPEntityMapObject;
import games.stendhal.client.gui.map.WalkBlockerMapObject;
import games.stendhal.client.gui.map.WallMapObject;
import games.stendhal.client.listener.PositionChangeListener;
import games.stendhal.common.CollisionDetection;
import mindustry.Vars;
import mindustry.gen.*;
import mindustry.input.*;
import mindustry.ui.*;
import mindustry.ui.fragments.Fragment;

import static mindustry.Vars.*;

public class T_MinimapFragment extends Fragment implements GameObjects.GameObjectListener, PositionChangeListener, StendhalClient.ZoneChangeListener {
    private boolean shown;
    private float panx, pany, zoom = 1f, lastZoom = -1;
    private float baseSize = Scl.scl(5f);
    private Element elem;

    private static final boolean supermanMode = true;   // (System.getProperty("stendhal.superman") != null);
    private volatile boolean needsRefresh;
    private double x, y;
    private final Map<IEntity, MapObject> mapObjects = new ConcurrentHashMap<IEntity, MapObject>();
    private MapPanel panel;

    private Table root;
    Cell<Element> tt;

    public void show() {
    }

    @Override
    public void build(Group parent){
        {
            StendhalClient.get().getGameObjects().addGameObjectListener(this);
            StendhalClient.get().addZoneChangeListener(this);
            positionChangeMulticaster.add(this);
        }

        root = new Table() {
            @Override
            public void draw() {
                super.draw();
                if (panel != null) {
                    System.out.println(root.getX() + " " + root.getY() + "   X   " + panel.getX() + " " + panel.getY() + "   X   " + parent.getX() + " " + parent.getY() + "  X   " + panel.getWidth() + "   " + panel.getHeight());

                }
            }
        };
//        root.setFillParent(true);
//        root.fill().margin(50);
        parent.addChild(root);
//        root.margin(20).setFillParent(true);
//        root.setFillParent(true);

        root.margin(0).setFillParent(true);
//        root.fill().setFillParent(true);



        panel = new MapPanel(mapObjects);
//        tt = root.add(panel).top().left().grow();
        root.addImage(atlasS.find("StendhalSplash")).size(500, 200).pad(0).top().left().growY().growX().fillX().fillY().align(Align.center);
        root.addImage(atlasS.find("StendhalSplash")).size(500, 200).pad(0).top().left().growY().growX().fillX().fillY().align(Align.center);
        root.addImage(atlasS.find("StendhalSplash")).size(500, 200).pad(0).top().left().growY().growX().fillX().fillY().align(Align.center);
        root.pack();
//
//        elem = parent.fill((x, y, w, h) -> {
//            w = Core.graphics.getWidth();
//            h = Core.graphics.getHeight();
//            float size = baseSize * zoom * world.width();
//
//            Draw.color(Color.black);
//            Fill.crect(x, y, w, h);
//
//            if(renderer.minimap.getTexture() != null){
//                Draw.color();
//                float ratio = (float)renderer.minimap.getTexture().getHeight() / renderer.minimap.getTexture().getWidth();
//                TextureRegion reg = Draw.wrap(renderer.minimap.getTexture());
//                Draw.rect(reg, w/2f + panx*zoom, h/2f + pany*zoom, size, size * ratio);
//                renderer.minimap.drawEntities(w/2f + panx*zoom - size/2f, h/2f + pany*zoom - size/2f * ratio, size, size * ratio, zoom, true);
//            }
//
//            Draw.reset();
//        });
//
//        elem.visible(() -> shown);
//        elem.update(() -> {
//            elem.requestKeyboard();
//            elem.requestScroll();
//            elem.setFillParent(true);
//            elem.setBounds(0, 0, Core.graphics.getWidth(), Core.graphics.getHeight());
//
//            if(Core.input.keyTap(Binding.menu)){
//                shown = false;
//            }
//        });
//        elem.touchable(Touchable.enabled);
//
//        elem.addListener(new ElementGestureListener(){
//
//            @Override
//            public void zoom(InputEvent event, float initialDistance, float distance){
//                if(lastZoom < 0){
//                    lastZoom = zoom;
//                }
//
//                zoom = Mathf.clamp(distance / initialDistance * lastZoom, 0.25f, 10f);
//            }
//
//            @Override
//            public void pan(InputEvent event, float x, float y, float deltaX, float deltaY){
//                panx += deltaX / zoom;
//                pany += deltaY / zoom;
//            }
//
//            @Override
//            public void touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
//                super.touchDown(event, x, y, pointer, button);
//            }
//
//            @Override
//            public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button){
//                lastZoom = zoom;
//            }
//        });
//
//        elem.addListener(new InputListener(){
//
//            @Override
//            public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY){
//                zoom = Mathf.clamp(zoom - amountY / 10f * zoom, 0.25f, 10f);
//                return true;
//            }
//        });
//
//        parent.fill(t -> {
//            t.setFillParent(true);
//            t.visible(() -> shown);
//            t.update(() -> t.setBounds(0, 0, Core.graphics.getWidth(), Core.graphics.getHeight()));
//
//            t.add("$minimap").style(Styles.outlineLabel).pad(10f);
//            t.row();
//            t.add().growY();
//            t.row();
//            t.addImageTextButton("$back", Icon.leftOpen, () -> shown = false).size(220f, 60f).pad(10f);
//        });
    }

    public boolean shown(){
        return shown;
    }

    public void toggle(){
        shown = !shown;
    }


    /**Add an entity to the map, if it should be displayed to the user. This method is thread safe.
     * @param entity the added entity
     */
    @Override
    public void addEntity(final IEntity entity) {
        MapObject object = null;

        if (entity instanceof Player) {
            object = new PlayerMapObject(entity);
        } else if (entity instanceof Portal) {
            final Portal portal = (Portal) entity;

            if (!portal.isHidden()) {
                mapObjects.put(entity, new PortalMapObject(entity));
            }
        } else if (entity instanceof HousePortal) {
            object = new PortalMapObject(entity);
        } else if (entity instanceof WalkBlocker) {
            object = new WalkBlockerMapObject(entity);
        } else if (entity instanceof FlyOverArea) {
            object = new FlyOverAreaMapObject(entity);
        } else if (entity instanceof Wall) {
            object = new WallMapObject(entity);
        } else if (entity instanceof DomesticAnimal) {
            // Only own pets and sheep are drawn but this is checked in the map object so the user status is always up to date
            object = new DomesticAnimalMapObject((DomesticAnimal) entity);
        } else if (supermanMode && User.isAdmin()) {
            if (entity instanceof RPEntity) {
                object = new RPEntityMapObject(entity);
            } else {
                object = new MovingMapObject(entity);
            }
        }

        if (object != null) {
            mapObjects.put(entity, object);

            // changes to objects that should trigger a refresh
            if (object instanceof MovingMapObject) {
                entity.addChangeListener(new EntityChangeListener<IEntity>() {
                    @Override
                    public void entityChanged(final IEntity entity, final Object property) {
                        if ((property == IEntity.PROP_POSITION)
                                || (property == RPEntity.PROP_GHOSTMODE)
                                || (property == RPEntity.PROP_GROUP_MEMBERSHIP)) {
                            needsRefresh = true;
                        }
                    }
                });
            }
            needsRefresh = true;
        }
    }

    /**Remove an entity from the map entity list.
     * @param entity the entity to be removed
     */
    @Override
    public void removeEntity(final IEntity entity) {
        if (mapObjects.remove(entity) != null) {
            needsRefresh = true;
        }
    }

    /** The player's position changed.
     * @param x The X coordinate (in world units).
     * @param y The Y coordinate (in world units).
     */
    @Override
    public void positionChanged(final double x, final double y) {
        /*
         * The client gets occasionally spurious events.
         * Suppress repainting unless the position actually changed
         */
        if ((this.x != x) || (this.y != y)) {
            this.x = x;
            this.y = y;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    panel.positionChanged(x, y);
                    /*
                     * Set the refresh flag after the map offset has been
                     * actually updated. The position listener for moving map
                     * objects sets it, but it happens in the game loop thread
                     * and may be unset before the map panel has actually got
                     * the correct map offset.
                     */
//                    setNeedsRefresh(true);
                }
            });
        }
    }

    @Override
    public void onZoneChange(Zone zone) {
    }

    @Override
    public void onZoneChangeCompleted(Zone zone) {
        update(zone.getCollision(), zone.getProtection(), zone.getReadableName(), zone.getDangerLevel());
    }

    @Override
    public void onZoneUpdate(Zone zone) {
        update(zone.getCollision(), zone.getProtection(), zone.getReadableName(), zone.getDangerLevel());
    }

    /**
     * Update the map with new data.
     * @param cd The collision map.
     * @param pd The protection map.
     * @param zone The zone name.
     * @param dangerLevel zone danger level
     */
    private void update(final CollisionDetection cd, final CollisionDetection pd,
                        final String zone, final double dangerLevel) {
        // Panel will do the relevant part in EDT.
        panel.update(cd, pd);
//        SwingUtilities.invokeLater(new Runnable() {
//            @Override
//            public void run() {
//                infoPanel.setZoneName(zone);
//                infoPanel.setDangerLevel(dangerLevel);
//            }
//        });
    }

}
