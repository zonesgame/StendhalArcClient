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
import mindustry.game.EventType;

import static arc.Core.camera;

public class T_InputApplication implements ApplicationListener {

    private /*final*/ PerceptionDispatcher perceptionDispatch;
    private /*final*/ UserContext userContext;

    T_GameScreen render;
    boolean gamerun = false;

    @Override
    public void init() {
        initClient();

        Events.on(EventType.ClientConnectOverEvent.class, e -> {
//            CStatusSender.send();
            this.gamerun = true;
            j2DClient locclient = new j2DClient(StendhalClient.get(), userContext, null);
            locclient.startGameLoop();
            perceptionDispatch.register(locclient.getPerceptionListener());
            // 清楚玩家移动状态, 如果先前是移动状态
            if (true) {
                Core.app.post(()->new MoveContinuousAction().sendAction(true, false));
            }
        });
    }

    @Override
    public void update() {
        if ( !gamerun) return;

//        client.loop(0);
//        clientManager.loop(0);
        GameLoop.get().runNoThread();

        float zoom = 1f;
        camera.resize(Core.graphics.getWidth() * zoom, Core.graphics.getHeight() * zoom);

        Core.graphics.clear(Color.white);
//        Draw.proj(Core.camera.projection());
        Draw.reset();
        Draw.proj(camera.projection());
//        Draw.proj().setOrtho(0, 0, Core.graphics.getWidth(), Core.graphics.getHeight());


        if (render == null) {
            render = new T_GameScreen(StendhalClient.get());
        }
        render.draw();

        Draw.flush();
    }

    @Override
    public void dispose() {
    }

    private void initClient() {
        userContext = new UserContext();
        perceptionDispatch = new PerceptionDispatcher();
        final StendhalClient client = new StendhalClient(userContext, perceptionDispatch);
    }

    boolean initOver = false;

}
