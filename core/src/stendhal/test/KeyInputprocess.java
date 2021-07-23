package stendhal.test;

import com.sun.xml.internal.rngom.parse.host.Base;

import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import arc.Core;
import arc.Graphics;
import arc.KeyBinds;
import arc.input.GestureDetector;
import arc.util.Log;
import games.stendhal.client.StaticGameLayers;
import games.stendhal.client.StendhalClient;
import games.stendhal.client.entity.IEntity;
import games.stendhal.client.gui.styled.cursor.StendhalCursor;
import games.stendhal.client.gui.wt.EntityViewCommandList;
import mindustry.Vars;
import mindustry.input.InputHandler;
import mindustry.ui.fragments.OverlayFragment;
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
    private Point tmpP1 = new Point();

    public final OverlayFragment frag = new OverlayFragment();

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
//            return false;
        }

        T_GameScreen2 screen = T_GameScreen2.get();

        // get clicked entity
        final Point2D location = screen.convertScreenViewToWorldDescartes(point.x, point.y);       // default   convertScreenViewToWorld()

        // for the clicked entity....
        final EntityView<?> view = screen.getEntityViewAt(location.getX(), location.getY());
        boolean doubleClick = WtWindowManager.getInstance().getPropertyBoolean("ui.doubleclick", false);
        if ((view != null) && view.isInteractive()) {
            if (Core.input.keyDown(Binding.s_ctrl)) {         // isCtrlDown
                view.onAction();
                return true;
            } else if (Core.input.keyDown(Binding.s_shift)) {            // isShiftDown
                view.onAction(ActionType.LOOK);
                return true;
            } else if (!doubleClick) {
                return view.onHarmlessAction();
            }
        } else if (Core.scene.hasMouse() && !Core.input.keyDown(Binding.s_ctrl)) {            // windowWasActiveOnMousePressed && !isCtrlDown()
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

    @ZTest
    protected void onMouseRightClick(Point point) {
        T_GameScreen2 screen = T_GameScreen2.get();
        ignoreClick = false;
        final Point2D location = screen.convertScreenViewToWorldDescartes(point.x, point.y);
        final EntityView<?> view = screen.getEntityViewAt(location.getX(), location.getY());

        frag.actions.showFor(view);

//        if (view != null) {
//            // ... show context menu (aka command list)
//            final String[] actions = view.getActions();
//
//            if (actions.length > 0) {
//                final IEntity entity = view.getEntity();
//
//                JPopupMenu menu = new EntityViewCommandList(entity.getType(), actions, view);
//                menu.show(ground.getCanvas(), point.x - MENU_OFFSET, point.y - MENU_OFFSET);
//                contextMenuFlag = true;
//                /*
//                 * Tricky way to detect recent popup menues. We need the
//                 * information to prevent walking when hiding the menu.
//                 */
//                menu.addPopupMenuListener(new PopupMenuListener() {
//                    @Override
//                    public void popupMenuCanceled(PopupMenuEvent e) {
//                        //ignore
//                    }
//                    @Override
//                    public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
//                        /*
//                         *  Hidden. inform onMouseClick; unfortunately this gets
//                         *  called before onMousePressed, so we need to push it
//                         *  pack to the event queue
//                         */
//                        SwingUtilities.invokeLater(new Runnable() {
//                            @Override
//                            public void run() {
//                                contextMenuFlag = false;
//                            }
//                        });
//                    }
//                    @Override
//                    public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
//                        // ignore
//                    }
//                });
//            }
//        }
    }

    public Graphics.Cursor getCursor(Point point) {
        StendhalCursor cursor = null;
        T_GameScreen2 screen = T_GameScreen2.get();

        // is the cursor aiming at a text box?
        final RemovableSprite text = screen.getTextAt(point.x, point.y);
        if (text != null) {
            return Vars.ui.stendhalCursors.get(StendhalCursor.NORMAL.arcKey);
        }

        Point2D point2 = screen.convertScreenViewToWorld(point);
        final EntityView<?> view = screen.getEntityViewAt(point2.getX(), point2.getY());
        // is the cursor aiming at an entity?
        if (view != null) {
            cursor = view.getCursor();
        }

        // is the cursor pointing on the ground?
        if (cursor == null) {
            cursor = StendhalCursor.WALK;
            StaticGameLayers layers = StendhalClient.get().getStaticGameLayers();
            if ((layers.getCollisionDetection() != null) && layers.getCollisionDetection().collides((int) point2.getX(), (int) point2.getY())) {
                cursor = StendhalCursor.STOP;
            } else if (calculateZoneChangeDirection(point2) != null) {
                cursor = StendhalCursor.WALK_BORDER;
            }
        }
        return Vars.ui.stendhalCursors.get(cursor.arcKey);
    }



    protected GestureDetector detector;

    public BaseInput add(){
        Core.input.getInputProcessors().remove(i -> i instanceof InputHandler || (i instanceof GestureDetector && ((GestureDetector)i).getListener() instanceof InputHandler));
//        Core.input.addProcessor(detector = new GestureDetector(20, 0.5f, 0.3f, 0.15f, this));
        Core.input.addProcessor(this);
        frag.add();
        {
            frag.actions.doAction = (command, entityView) -> {
                // tell the entity what happened
                ActionType action = ActionType.getbyRep(command);
                if (action == null) {
                    Log.err("Unknown command: '" + command + "'");
                    return;
                }
                entityView.onAction(ActionType.getbyRep(command));
            };
        }
        return this;
    }

    public BaseInput remove(){
        Core.input.removeProcessor(this);
        frag.remove();
        if(detector != null){
//            Core.input.removeProcessor(detector);
        }
        return this;
    }

    @Override
    public void update() {
//        if ( Core.input.getKeyboard().keyDown((KeyCode) Binding.s_mouse_left.defaultValue(null))) {
//            System.out.println("Key Down................");
//        }
//        if (Core.input.keyDown(Binding.s_mouse_left)) {
//            System.out.println("22222222222");
//        }
        if (Core.input.getKeyboard().isKeyTapped( (KeyCode) Binding.s_mouse_left.defaultValue(null))) {
//            System.out.println("Mouse up.,.......   " + getMouseX() + "   " + getMouseY());
            tmpP1.setLocation(getMouseX(), getMouseY());
            onMouseClick(tmpP1);
        }

        if (Core.input.getKeyboard().isKeyTapped( (KeyCode) Binding.s_mouse_right.defaultValue(null))) {
            tmpP1.setLocation(getMouseX(), getMouseY());
            onMouseRightClick(tmpP1);
        }

//        if (Core.input.getKeyboard().touchDown()) {
//
//        }
    }
}
