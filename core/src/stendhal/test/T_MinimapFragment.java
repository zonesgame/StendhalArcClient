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
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Dialog;
import arc.scene.ui.Image;
import arc.scene.ui.Label;
import arc.scene.ui.layout.*;
import arc.util.Align;
import arc.util.Scaling;
import arc.util.Time;
import games.stendhal.client.GameObjects;
import games.stendhal.client.StendhalClient;
import games.stendhal.client.Zone;
import games.stendhal.client.entity.Blood;
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
import mindustry.game.EventType;
import mindustry.gen.*;
import mindustry.input.*;
import mindustry.ui.*;
import mindustry.ui.fragments.Fragment;

import static mindustry.Vars.*;
import static mindustry.ui.Styles.label1;

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
    Cell<MapPanel> cellPanel;
    Image img;
    private Label zoneinfo;

    public void show() {
    }

    @Override
    public void build(Group parent){
        {
            StendhalClient.get().getGameObjects().addGameObjectListener(this);
            StendhalClient.get().addZoneChangeListener(this);
            positionChangeMulticaster.add(this);
        }

        root = new Table(Tex.white9s1) {
            @Override
            public void draw() {
                super.draw();
                if (panel != null) {
//                    System.out.println(root.getX() + " " + root.getY() + "   X   " + panel.getX() + " " + panel.getY() + "   X   " + parent.getX() + " " + parent.getY() + "  X   " + panel.getWidth() + "   " + panel.getHeight());
//                    System.out.println(panel.getScaleX() + "  " + panel.getScaleY());
//                    System.out.println(panel.getWidth() + "  " + panel.getHeight());
                }
                if (img != null) {
//                    System.out.println(img.getX() + " " + img.getY() + " X " + img.getImageWidth() + " " + img.getImageHeight()
//                    + " x " + img.getMinWidth() + " " + img.getMinHeight() + " X " + img.getPrefWidth() + " " + img.getPrefHeight() + " X "
//                    + img.getMaxWidth() + " " + img.getMaxHeight());
//                    System.out.println(img.getScaleX() + " " + img.getScaleY() + " x " + img.getX() + " " + img.getY() + " x " + img.getImageX() + " " + img.getImageY()
//                    + "  X " + img.getImageWidth() + img.getImageY());
//                    if (Core.input.justTouched()) {
//                        ((TextureRegionDrawable)img.getDrawable()).getRegion().set(50, 50, 50, 100);
//                        System.out.println("ssssssssssssssssssssssssssssss");
//                    }
//                    System.out.println(img.getX() + "  " + img.getY() + "  X  " + img.getWidth() + " " + img.getHeight());
                }
            }
        };
//        root.setFillParent(true);
//        root.fill().margin(50);
        parent.addChild(root);
//        root.margin(20).setFillParent(true);
//        root.setFillParent(true);

//        root.setFillParent(true);
        root.setFillParent(true);
//        root.fill().setFillParent(true);

//        Pixmap pixmap = new Pixmap(200, 200);
//        pixmap.setColor(Color.blue);
//        pixmap.fill();
//        Texture texture = new Texture(pixmap);
//        pixmap.dispose();

//        root.addImage(new TextureRegion(texture)).size(200).bottom().left().padTop(0).growX().growY().row();
//        root.defaults().top().left();
        {
        }
        Table table = root.table(Tex.white9s1).width(210).margin(8).top().left().expand().get();

        panel = new MapPanel(mapObjects);
        cellPanel = table.add(panel).top().center().expand().size(200);

        table.row();
        zoneinfo = table.add("", label1).top().center().padTop(2).width(200).height(30).expand().get();
        zoneinfo.setWrap(true);
        zoneinfo.setAlignment(Align.center);
    }

    private void onResize(Runnable run){
        Events.on(EventType.ResizeEvent.class, event -> {
            if(root.hasParent() && root.isVisible()){
                run.run();
            }
        });
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
        // zones add begon
        else if (entity instanceof Blood) {  // 血迹
        }
        else if (User.isAdmin() || true) {
            if (entity instanceof RPEntity) {
                object = new RPEntityMapObject(entity);
            } else {
                object = new MovingMapObject(entity);
            }
        }
        // zones add end

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
        Core.app.post(() -> {
            panel.update(cd, pd);
            zoneinfo.setText(zone);
        });

        // Panel will do the relevant part in EDT.
//        panel.update(cd, pd);
//        SwingUtilities.invokeLater(new Runnable() {
//            @Override
//            public void run() {
//                infoPanel.setZoneName(zone);
//                infoPanel.setDangerLevel(dangerLevel);
//            }
//        });
    }

}
