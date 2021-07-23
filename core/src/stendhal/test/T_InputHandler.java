package stendhal.test;

import arc.Core;
import arc.input.GestureDetector;
import arc.input.InputProcessor;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.event.Touchable;
import arc.scene.ui.layout.Table;
import arc.scene.ui.layout.WidgetGroup;
import arc.util.Log;
import games.stendhal.client.StaticGameLayers;
import games.stendhal.client.StendhalClient;
import games.stendhal.client.Zone;
import games.stendhal.common.Direction;
import marauroa.common.game.RPAction;
import mindustry.input.Binding;
import mindustry.input.InputHandler;
import temp.java.awt.Point;
import temp.java.awt.geom.Point2D;

import static mindustry.Vars.player;
import static mindustry.Vars.ui;

public class T_InputHandler  implements InputProcessor, GestureDetector.GestureListener {

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, KeyCode button) {
//        Vec2 pos = Vec2.TEMP.set(Core.input.mouseWorld(screenX, screenY));
//        Vec2 pos2 = Vec2.TEMP2.set(Core.input.mouseScreen(screenX, screenY));
//        System.out.println(screenX + "  X  " + screenY + "  :  " + button.toString() );
//        System.out.println((int)pos.x + " " + (int)pos.y + "         X        " + (int)pos2.x + " " + (int)pos2.y);
//        System.out.println("_____________________________________________" + Core.camera.width + "  " + Core.camera.height);
        Vec2 pos = Vec2.TEMP.set(Core.input.mouseWorld(screenX, screenY));
        Point2D tilePos = new Point2D.Double(pos.x / 32f, pos.y / 32f);
//        System.out.println((int)tilePos.getX() + "   X    " + (int)tilePos.getY());
        tilePos.setLocation(tilePos.getX(), StendhalClient.get().getStaticGameLayers().getHeight() - tilePos.getY() );
//        System.out.println((int)tilePos.getX() + "   XXX    " + (int)tilePos.getY());
        createAndSendMoveToAction(tilePos, false);

        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }


    public void createAndSendMoveToAction(final Point2D point, boolean doubleClick) {
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

        StendhalClient.get().send(action);
    }

    public Direction calculateZoneChangeDirection(Point2D point) {
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



    // temp begon
    public void update() {
       
    }

    @Override
    public boolean keyDown(KeyCode keycode) {
        System.out.println("KeyDown...............");
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, KeyCode button) {
//        super.
        System.out.println("TouchDown............k");
        return false;
    }

    protected GestureDetector detector;

    public void add(){
        Core.input.getInputProcessors().remove(i -> i instanceof InputHandler || (i instanceof GestureDetector && ((GestureDetector)i).getListener() instanceof InputHandler));
        Core.input.addProcessor(detector = new GestureDetector(20, 0.5f, 0.3f, 0.15f, this));
        Core.input.addProcessor(this);
    }

    public void remove(){
        Core.input.removeProcessor(this);
        if(detector != null){
            Core.input.removeProcessor(detector);
        }
    }
    // temp end

}
