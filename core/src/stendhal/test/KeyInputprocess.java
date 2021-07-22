package stendhal.test;

import arc.Core;
import arc.KeyBinds;
import temp.java.awt.Point;
import temp.java.awt.geom.Point2D;

import arc.input.InputProcessor;
import arc.input.KeyCode;
import arc.struct.ObjectMap;
import arc.z.util.ZonesAnnotate.ZTest;
import games.stendhal.client.entity.ActionType;
import games.stendhal.client.gui.j2d.RemovableSprite;
import games.stendhal.client.gui.j2d.entity.EntityView;
import games.stendhal.client.gui.wt.core.WtWindowManager;
import mindustry.input.Binding;

public class KeyInputprocess implements BaseInput {

    private ObjectMap<KeyBinds.KeybindValue, Boolean> keyPressState = new ObjectMap<>(128);

    public boolean isKeyTap(KeyBinds.KeyBind key) {
        return keyPressState.get(key.defaultValue(null), false);
    }

    @Override
    public boolean keyDown(KeyCode keycode) {
        keyPressState.put(keycode, true);
        return false;
    }

    @Override
    public boolean keyUp(KeyCode keycode) {
        keyPressState.put(keycode, false);
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }

    public void update() {

    }


    private boolean ignoreClick;
    /**mouseClick初始点击位置*/
    private float xOnMousePressed, yOnMousePressed;

    @ZTest
    protected boolean onMouseClick(Point point) {
        // Context menu detection
        if (ignoreClick) {
            ignoreClick = false;
            return false;
        }
        // on MS Windows releasing the mouse after a drag&drop action is
        // counted as mouse click: https://sourceforge.net/support/tracker.php?aid=2976895
        if ((Math.abs(point.getX() - xOnMousePressed) > 10)
                || (Math.abs(point.getY() - yOnMousePressed) > 10)) {
            return false;
        }

        T_GameScreen2 screen = T_GameScreen2.get();

        // get clicked entity
        final Point2D location = screen.convertScreenViewToWorld(point);

        // for the clicked entity....
        final EntityView<?> view = screen.getEntityViewAt(location.getX(), location.getY());
        boolean doubleClick = WtWindowManager.getInstance().getPropertyBoolean("ui.doubleclick", false);
        if ((view != null) && view.isInteractive()) {
            if (isKeyTap(Binding.s_ctrl)) {         // isCtrlDown
                view.onAction();
                return true;
            } else if (isKeyTap(Binding.s_shift)) {            // isShiftDown
                view.onAction(ActionType.LOOK);
                return true;
            } else if (!doubleClick) {
                return view.onHarmlessAction();
            }
        } else if (!Core.scene.hasMouse() && !isKeyTap(Binding.s_ctrl)) {            // windowWasActiveOnMousePressed && !isCtrlDown()
            if (!doubleClick) {
                createAndSendMoveToAction(location, false);
                // let it pass "unhandled", so that the possible double click
                // move can be sent to server as well
            }
        }

        return false;
    }

    @ZTest
    protected boolean onMouseDoubleClick(Point point) {
        T_GameScreen2 screen = T_GameScreen2.get();
        final Point2D location = screen.convertScreenViewToWorld(point);

        final EntityView<?> view = screen.getEntityViewAt(location.getX(), location.getY());

        if ((view != null) && view.isInteractive()) {
            // ... do the default action
            view.onAction();
            return true;
        } else {
            createAndSendMoveToAction(location, true);
            return true;
        }
    }
}
