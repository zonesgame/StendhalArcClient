package stendhal.test;

import arc.Core;
import arc.input.GestureDetector;
import arc.math.geom.Vec2;
import temp.java.awt.geom.Point2D;

import arc.input.InputProcessor;
import games.stendhal.client.StaticGameLayers;
import games.stendhal.client.StendhalClient;
import games.stendhal.common.Direction;
import marauroa.common.game.RPAction;
import temp.Debug;

import static games.stendhal.client.IGameScreen.SIZE_UNIT_PIXELS;

/**
 *  移动端和PC通用处理事件
 * */
public interface BaseInput extends InputProcessor/*, GestureDetector.GestureListener*/ {

    /**
     * Send a move to command to the server.
     *
     * @param point destination
     * @param doubleClick <code>true</code> if the action was created with a
     * 	double click, <code>false</code> otherwise
     */
    default void createAndSendMoveToAction(final Point2D point, boolean doubleClick) {
        final RPAction action = new RPAction();
        action.put("type", "moveto");
        action.put("x", (int) point.getX());
        action.put("y", (int) point.getY());
        if (doubleClick) {
            action.put("double_click", "");
        }

        Direction dir = calculateZoneChangeDirection(point);
        if (dir != null) {
            action.put("extend", dir.ordinal());
        }

        if (Debug.NOTE1)
            ;
        StendhalClient.get().send(action);      //  client.send(action);
    }

    /**
     * Calculates whether the click was close enough to a zone border to trigger
     * a zone change.
     *
     * @param point click point in world coordinates
     * @return Direction of the zone to change to, <code>null</code> if no zone change should happen
     */
    default Direction calculateZoneChangeDirection(Point2D point) {
        StaticGameLayers layers = StendhalClient.get().getStaticGameLayers();
        double x = point.getX();
        double y = point.getY();
        double width = layers.getWidth();
        double height = layers.getHeight();
        if (x < 0.333) {
            return Direction.LEFT;
        }
        if (x > width - 0.333) {
            return Direction.RIGHT;
        }
        if (y < 0.333) {
            return Direction.UP;
        }
        if (y > height - 0.4) {
            return Direction.DOWN;
        }
        return null;
    }

    /** 鼠标X轴位置*/
    default float getMouseX(){
        return Core.input.mouseX();
    }

    /** 鼠标Y轴位置*/
    default float getMouseY(){
        return Core.input.mouseY();
    }

    /** 鼠标Y轴笛卡尔坐标*/
//    default double getMouseYDescartes() {
////        Vec2 pos = Vec2.TEMP.set(Core.input.mouseWorld(screenX, screenY));
////        Point2D tilePos = new Point2D.Double(pos.x / 32f, pos.y / 32f);
//////        System.out.println((int)tilePos.getX() + "   X    " + (int)tilePos.getY());
////        tilePos.setLocation(tilePos.getX(), StendhalClient.get().getStaticGameLayers().getHeight() - tilePos.getY() );
//        return (StendhalClient.get().getStaticGameLayers().getHeight() * SIZE_UNIT_PIXELS - getMouseY());
//    }

    default void update() {
    }



}
