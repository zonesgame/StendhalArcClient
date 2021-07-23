package stendhal.test;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;

import arc.ApplicationListener;
import arc.Core;
import arc.Events;
import arc.files.Fi;
import arc.graphics.Color;
import arc.graphics.Texture;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.input.InputProcessor;
import arc.math.geom.Vec2;
import arc.util.Log;
import games.stendhal.client.CStatusSender;
import games.stendhal.client.ClientSingletonRepository;
import games.stendhal.client.GameLoop;
import games.stendhal.client.PerceptionDispatcher;
import games.stendhal.client.StendhalClient;
import games.stendhal.client.UserContext;
import games.stendhal.client.actions.MoveContinuousAction;
import games.stendhal.client.gui.J2DClientGUI;
import games.stendhal.client.gui.UserInterface;
import games.stendhal.client.gui.chatlog.EventLine;
import games.stendhal.client.gui.j2DClient;
import games.stendhal.client.gui.login.Profile;
import games.stendhal.client.listener.PositionChangeListener;
import games.stendhal.client.sound.facade.SoundGroup;
import games.stendhal.client.sound.facade.SoundHandle;
import games.stendhal.client.sound.facade.SoundSystemFacade;
import games.stendhal.client.sound.facade.Time;
import games.stendhal.client.stendhal;
import games.stendhal.common.NotificationType;
import marauroa.client.net.IPerceptionListener;
import marauroa.client.net.PerceptionHandler;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;
import marauroa.common.net.message.MessageS2CPerception;
import marauroa.common.net.message.TransferContent;
import mindustry.Vars;
import mindustry.core.GameState;
import mindustry.game.EventType;

import static arc.Core.camera;
import static mindustry.Vars.state;

public class T_InputApplication implements ApplicationListener {

    private /*final*/ PerceptionDispatcher perceptionDispatch;
    private /*final*/ UserContext userContext;

    T_GameScreen2 render;
    public static boolean gamerun = false;

    @Override
    public void init() {
        initClient();

        Events.on(EventType.ClientConnectOverEvent.class, e -> {
//            CStatusSender.send();
            state.set(GameState.State.playing);
            render = new T_GameScreen2(StendhalClient.get());
            render.onResized();
            this.gamerun = true;
            j2DClient locclient = new j2DClient(StendhalClient.get(), userContext, null);
            locclient.startGameLoop();
            perceptionDispatch.register(locclient.getPerceptionListener());
            // 清楚玩家移动状态, 如果先前是移动状态
            if (true) {
                Core.app.post(()->new MoveContinuousAction().sendAction(true, false));
            }

//            Core.input.getInputProcessors().clear();
            Vars.inputStendhal = new KeyInputprocess().add();
//            System.out.println(Core.input.getInputProcessors().size);
//            for (InputProcessor processor : Core.input.getInputProcessors()) {
//                System.out.println(processor.getClass().getName());
//            }
        });
//        Core.input.addProcessor(new T_InputHandler());
    }

    @Override
    public void update() {
        float zoom = 1f;
        camera.resize(Core.graphics.getWidth() * zoom, Core.graphics.getHeight() * zoom);

        if ( !gamerun) return;

//        client.loop(0);
//        clientManager.loop(0);
        GameLoop.get().runNoThread();

//        float zoom = 2f;
//        camera.resize(Core.graphics.getWidth() * zoom, Core.graphics.getHeight() * zoom);

        camera.update();
        Core.graphics.clear(Color.white);
        Draw.proj(Core.camera.projection());
        Draw.reset();
        Draw.proj(camera.projection());
//        Draw.proj().setOrtho(0, 0, Core.graphics.getWidth(), Core.graphics.getHeight());


        if (render == null) {
//            render = new T_GameScreen(StendhalClient.get());
//            Vars.gameScreen = render;
        }
        render.paintComponent(null);

        Draw.flush();
    }

    @Override
    public void dispose() {
    }

    @Override
    public void resize(int width, int height) {
        if (render != null)
            render.onResized();
    }

    private void initClient() {
        userContext = new UserContext();
        perceptionDispatch = new PerceptionDispatcher();
        final StendhalClient client = new StendhalClient(userContext, perceptionDispatch);
    }

    boolean initOver = false;

}
