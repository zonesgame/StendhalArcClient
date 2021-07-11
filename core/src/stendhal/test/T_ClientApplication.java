package stendhal.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arc.ApplicationListener;
import games.stendhal.client.stendhal;
import marauroa.client.net.IPerceptionListener;
import marauroa.client.net.PerceptionHandler;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;
import marauroa.common.net.message.MessageS2CPerception;
import marauroa.common.net.message.TransferContent;

public class T_ClientApplication implements ApplicationListener {

    @Override
    public void init() {
        callInit();

        try {
            clientManager.connect(host, Integer.parseInt(port));
            clientManager.login(username, password);
        } catch (final Exception e) {
            e.printStackTrace();
            return;
        }
    }

    @Override
    public void update() {
        run();
    }

    @Override
    public void dispose() {
    }




    private String host;
    private String username;
    private String password;
    private String character;
    private String port;
    private boolean showWorld;
    private Map<RPObject.ID, RPObject> world_objects;
    private marauroa.client.ClientFramework clientManager;
    private PerceptionHandler handler;

    public void callInit() {
        username = "zonesa";
        password = "a123456";
        character = "zonesa";
        host = "stendhalgame.org";
        port = "32160";
//        tcp = true;

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

    }

    public void run() {
        clientManager.loop(0);
    }

}
