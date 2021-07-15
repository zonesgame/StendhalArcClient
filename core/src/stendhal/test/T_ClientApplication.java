package stendhal.test;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import games.stendhal.client.ClientSingletonRepository;
import games.stendhal.client.PerceptionDispatcher;
import games.stendhal.client.StendhalClient;
import games.stendhal.client.UserContext;
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

public class T_ClientApplication implements ApplicationListener {

    T_GameScreen render;

    @Override
    public void init() {
//        callInit();


        Events.on(EventType.ClientLoadEvent.class, e -> {
            callInit();
        });
    }

    @Override
    public void update() {
        run();
    }

    @Override
    public void dispose() {
    }


    private Map<RPObject.ID, RPObject> world_objects;
    private marauroa.client.ClientFramework clientManager;
    private PerceptionHandler handler;

    private String host;
    private String username;
    private String password;
    private String character;
    private String port;
    private boolean showWorld;
//    private Map<RPObject.ID, RPObject> world_objects;
    private StendhalClient client;
    private Profile profile;
//    private PerceptionHandler handler;

    public void callInit() {
        {   //
            ClientSingletonRepository.setUserInterface(new UserInterface() {
                @Override
                public void addEventLine(EventLine line) {
                }

                @Override
                public void addGameScreenText(double x, double y, String text, NotificationType type, boolean isTalking) {
                }

                @Override
                public void addAchievementBox(String title, String description, String category) {
                }

                @Override
                public SoundSystemFacade getSoundSystemFacade() {
                    return new SoundSystemFacade() {
                        @Override
                        public void exit() {
                        }

                        @Override
                        public SoundGroup getGroup(String groupName) {
                            return null;
                        }

                        @Override
                        public void update() {
                        }

                        @Override
                        public void stop(SoundHandle sound, Time fadingDuration) {
                        }

                        @Override
                        public void mute(boolean turnOffSound, boolean useFading, Time delay) {
                        }

                        @Override
                        public float getVolume() {
                            return 0;
                        }

                        @Override
                        public Collection<String> getGroupNames() {
                            return null;
                        }

                        @Override
                        public void changeVolume(float volume) {
                        }

                        @Override
                        public List<String> getDeviceNames() {
                            return null;
                        }

                        @Override
                        public void positionChanged(double x, double y) {
                        }
                    };
                }
            });
        }

        username = "zonesa";
        password = "a123456";
        character = "zonesa";
        host = "stendhalgame.org";
        port = "32160";
//        tcp = true;
        profile = new Profile(host, Integer.parseInt(port), username, password);
        profile.setCharacter(character);

        UserContext userContext = new UserContext();
        PerceptionDispatcher perceptionDispatch = new PerceptionDispatcher();
        client = new StendhalClient(userContext, perceptionDispatch);
        ClientSingletonRepository.setClientFramework(client);

        world_objects = new HashMap<RPObject.ID, RPObject>();

        handler = new PerceptionHandler(new IPerceptionListener() {

            @Override
            public boolean onAdded(final RPObject object) {
                return false;
            }

            @Override
            public boolean onClear() {
                return false;
            }

            @Override
            public boolean onDeleted(final RPObject object) {
                return false;
            }

            @Override
            public void onException(final Exception exception,
                                    final MessageS2CPerception perception) {
                exception.printStackTrace();
            }

            @Override
            public boolean onModifiedAdded(final RPObject object, final RPObject changes) {
                return false;
            }

            @Override
            public boolean onModifiedDeleted(final RPObject object, final RPObject changes) {
                return false;
            }

            @Override
            public boolean onMyRPObject(final RPObject added, final RPObject deleted) {
                return false;
            }

            @Override
            public void onPerceptionBegin(final byte type, final int timestamp) {
            }

            @Override
            public void onPerceptionEnd(final byte type, final int timestamp) {
            }

            @Override
            public void onSynced() {
            }

            @Override
            public void onUnsynced() {
            }
        });

        clientManager = new marauroa.client.ClientFramework(
                "games/stendhal/log4j.properties") {

            @Override
            protected String getGameName() {
                return "stendhal";
            }

            @Override
            protected String getVersionNumber() {
                return stendhal.VERSION;
            }

            @Override
            protected void onPerception(final MessageS2CPerception message) {
                try {
                    System.out.println("Received perception "
                            + message.getPerceptionTimestamp());

                    handler.apply(message, world_objects);
                    final int i = message.getPerceptionTimestamp();

                    final RPAction action = new RPAction();
                    if (i % 50 == 0) {
                        action.put("type", "move");
                        action.put("dy", "-1");
                        clientManager.send(action);
                    } else if (i % 50 == 20) {
                        action.put("type", "move");
                        action.put("dy", "1");
                        clientManager.send(action);
                    }
                    if (showWorld) {
                        System.out.println("<World contents ------------------------------------->");
                        int j = 0;
                        for (final RPObject object : world_objects.values()) {
                            j++;
                            System.out.println(j + ". " + object);
                        }
                        System.out.println("</World contents ------------------------------------->");
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected List<TransferContent> onTransferREQ(
                    final List<TransferContent> items) {
                for (final TransferContent item : items) {
                    item.ack = true;
                }

                return items;
            }

            @Override
            protected void onTransfer(final List<TransferContent> items) {
                System.out.println("Transfering ----");
                for (final TransferContent item : items) {
                    System.out.println(item);
                }
            }

            @Override
            protected void onAvailableCharacters(final String[] characters) {
                System.out.println("Characters available");
                for (final String characterAvail : characters) {
                    System.out.println(characterAvail);
                }

                try {
                    chooseCharacter(character);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected void onServerInfo(final String[] info) {
                System.out.println("Server info");
                for (final String info_string : info) {
                    System.out.println(info_string);
                }
            }

            @Override
            protected void onPreviousLogins(final List<String> previousLogins) {
                System.out.println("Previous logins");
                for (final String info_string : previousLogins) {
                    System.out.println(info_string);
                }
            }
        };

        try {
//            clientManager.connect(host, Integer.parseInt(port));
//            clientManager.login(username, password);

            client.connect(profile.getHost(), profile.getPort());

            client.setAccountUsername(profile.getUser());
            client.setCharacter(profile.getCharacter());
            client.login(profile.getUser(), profile.getPassword(), profile.getSeed());
        } catch (final Exception e) {
            e.printStackTrace();
            return;
        }

        j2DClient locclient = new j2DClient(client, userContext, null);
        perceptionDispatch.register(locclient.getPerceptionListener());

        j2DClient.get().positionChangeListener.add(new PositionChangeListener() {
            @Override
            public void positionChanged(double x, double y) {
                camera.position.lerpDelta(new Vec2((float)x * 32f, (float)((StendhalClient.get().getStaticGameLayers().getHeight() - y - 1) * 32f)), 0.8f);
            }
        });

        Core.input.addProcessor(new T_InputHandler());

        initOver = true;
    }

    boolean initOver = false;
    public void run() {
        if ( !initOver) return;

        client.loop(0);
//        clientManager.loop(0);

        float zoom = 1f;
        camera.resize(Core.graphics.getWidth() * zoom, Core.graphics.getHeight() * zoom);

        Core.graphics.clear(Color.white);
//        Draw.proj(Core.camera.projection());
        Draw.reset();
        Draw.proj(camera.projection());
//        Draw.proj().setOrtho(0, 0, Core.graphics.getWidth(), Core.graphics.getHeight());


        if (render == null) {
            render = new T_GameScreen(client);
        }
        render.draw();

        Draw.flush();
    }

}
