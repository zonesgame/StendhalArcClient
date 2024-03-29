package mindustry.desktop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.NetworkInterface;
import java.nio.charset.Charset;
import java.util.Enumeration;

import arc.Files.FileType;
import arc.backend.sdl.SdlApplication;
import arc.backend.sdl.SdlConfig;
import arc.backend.sdl.jni.SDL;
import arc.files.Fi;
import arc.func.Cons;
import arc.struct.Array;
import arc.util.Log;
import arc.util.OS;
import arc.util.Strings;
import arc.util.serialization.Base64Coder;
import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import mindustry.ClientLauncher;
import mindustry.Vars;
import mindustry.core.GameState.State;
import mindustry.core.Version;
import mindustry.net.ArcNetProvider;
import mindustry.net.CrashSender;
import mindustry.net.Net.NetProvider;
import mindustry.type.Publishable;

import static mindustry.Vars.net;
import static mindustry.Vars.playerGroup;
import static mindustry.Vars.state;
import static mindustry.Vars.steam;
import static mindustry.Vars.testMobile;
import static mindustry.Vars.ui;
import static mindustry.Vars.world;

public class DesktopLauncher extends ClientLauncher{
    public final static String discordID = "610508934456934412";

    boolean useDiscord = OS.is64Bit, loadError = false;
    Throwable steamError;

    static{
        if(!Charset.forName("US-ASCII").newEncoder().canEncode(System.getProperty("user.name", ""))){
            System.setProperty("com.codedisaster.steamworks.SharedLibraryExtractPath", new File("").getAbsolutePath());
        }
    }

    public static void main(String[] arg){
        try{
            Vars.loadLogger();
            new SdlApplication(new DesktopLauncher(arg), new SdlConfig(){{
                title = "Mindustry";
//                maximized = true;
                depth = 0;
                stencil = 0;
                width = 1280;
                height = 720;
                setWindowIcon(FileType.internal, "icons/icon_64.png");
            }});
        }catch(Throwable e){
            handleCrash(e);
        }
    }

    public DesktopLauncher(String[] args){
//        Version.init();
        boolean useSteam = Version.modifier.contains("steam");
        testMobile = Array.with(args).contains("-testMobile");
        {
            useSteam= false;
            useDiscord= false;
        }

        if(useDiscord){
            try{
                DiscordEventHandlers handlers = new DiscordEventHandlers();
                DiscordRPC.INSTANCE.Discord_Initialize(discordID, handlers, true, "1127400");
                Log.info("Initialized Discord rich presence.");

                Runtime.getRuntime().addShutdownHook(new Thread(DiscordRPC.INSTANCE::Discord_Shutdown));
            }catch(Throwable t){
                useDiscord = false;
                Log.err("Failed to initialize discord.", t);
            }
        }

        if(useSteam){
//            //delete leftover dlls
//            Fi file = new Fi(".");
//            for(Fi other : file.parent().list()){
//                if(other.name().contains("steam") && (other.extension().equals("dll") || other.extension().equals("so") || other.extension().equals("dylib"))){
//                    other.delete();
//                }
//            }
//
//            Events.on(ClientLoadEvent.class, event -> {
//                if(steamError != null){
//                    Core.app.post(() -> Core.app.post(() -> Core.app.post(() -> {
//                        ui.showErrorMessage(Core.bundle.format("steam.error", (steamError.getMessage() == null) ? steamError.getClass().getSimpleName() : steamError.getClass().getSimpleName() + ": " + steamError.getMessage()));
//                    })));
//                }
//            });
//
//            try{
//                SteamAPI.loadLibraries();
//
//                if(!SteamAPI.init()){
//                    loadError = true;
//                    Log.err("Steam client not running.");
//                }else{
//                    initSteam(args);
//                    Vars.steam = true;
//                }
//
//                if(SteamAPI.restartAppIfNecessary(SVars.steamID)){
//                    System.exit(0);
//                }
//            }catch(NullPointerException ignored){
//                steam = false;
//                Log.info("Running in offline mode.");
//            }catch(Throwable e){
//                steam = false;
//                Log.err("Failed to load Steam native libraries.");
//                logSteamError(e);
//            }
        }
    }

    void logSteamError(Throwable e){
        steamError = e;
        loadError = true;
        Log.err(e);
        try(OutputStream s = new FileOutputStream(new File("steam-error-log-" + System.nanoTime() + ".txt"))){
            String log = Strings.parseException(e, true);
            s.write(log.getBytes());
        }catch(Exception e2){
            Log.err(e2);
        }
    }

    void initSteam(String[] args){
//        SVars.net = new SNet(new ArcNetProvider());
//        SVars.stats = new SStats();
//        SVars.workshop = new SWorkshop();
//        SVars.user = new SUser();
//        boolean[] isShutdown = {false};
//
//        Events.on(ClientLoadEvent.class, event -> {
//            player.name = SVars.net.friends.getPersonaName();
//            Core.settings.defaults("name", SVars.net.friends.getPersonaName());
//            Core.settings.put("name", player.name);
//            Core.settings.save();
//            //update callbacks
//            Core.app.addListener(new ApplicationListener(){
//                @Override
//                public void update(){
//                    if(SteamAPI.isSteamRunning()){
//                        SteamAPI.runCallbacks();
//                    }
//                }
//            });
//
//            Core.app.post(() -> {
//                if(args.length >= 2 && args[0].equals("+connect_lobby")){
//                    try{
//                        long id = Long.parseLong(args[1]);
//                        ui.join.connect("steam:" + id, port);
//                    }catch(Exception e){
//                        Log.err("Failed to parse steam lobby ID: {0}", e.getMessage());
//                        e.printStackTrace();
//                    }
//                }
//            });
//        });
//
//        Events.on(DisposeEvent.class, event -> {
//            SteamAPI.shutdown();
//            isShutdown[0] = true;
//        });
//
//        //steam shutdown hook
//        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//            if(!isShutdown[0]){
//                SteamAPI.shutdown();
//            }
//        }));
    }

    static void handleCrash(Throwable e){
        Cons<Runnable> dialog = Runnable::run;
        boolean badGPU = false;
        String finalMessage = Strings.getFinalMesage(e);
        String total = Strings.getCauses(e).toString();

        if(total.contains("Couldn't create window") || total.contains("OpenGL 2.0 or higher") || total.toLowerCase().contains("pixel format") || total.contains("GLEW")){

            dialog.get(() -> message(
                total.contains("Couldn't create window") ? "A graphics initialization error has occured! Try to update your graphics drivers:\n" + finalMessage :
                            "Your graphics card does not support OpenGL 2.0 with the framebuffer_object extension!\n" +
                                    "Try to update your graphics drivers. If this doesn't work, your computer may not support Mindustry.\n\n" +
                                    "Full message: " + finalMessage));
            badGPU = true;
        }

        boolean fbgp = badGPU;

        CrashSender.send(e, file -> {
            Throwable fc = Strings.getFinalCause(e);
            if(!fbgp){
                dialog.get(() -> message("A crash has occured. It has been saved in:\n" + file.getAbsolutePath() + "\n" + fc.getClass().getSimpleName().replace("Exception", "") + (fc.getMessage() == null ? "" : ":\n" + fc.getMessage())));
            }
        });
    }

    @Override
    public Array<Fi> getWorkshopContent(Class<? extends Publishable> type){
//        return !steam ? super.getWorkshopContent(type) : SVars.workshop.getWorkshopFiles(type);
        return super.getWorkshopContent(type);
    }

    @Override
    public void viewListing(Publishable pub){
//        SVars.workshop.viewListing(pub);
    }

    @Override
    public void viewListingID(String id){
//        SVars.net.friends.activateGameOverlayToWebPage("steam://url/CommunityFilePage/" + id);
    }

    @Override
    public NetProvider getNet(){
//        return steam ? SVars.net : new ArcNetProvider();
        return new ArcNetProvider();
    }

    @Override
    public void openWorkshop(){
//        SVars.net.friends.activateGameOverlayToWebPage("https://steamcommunity.com/app/1127400/workshop/");
    }

    @Override
    public void publish(Publishable pub){
//        SVars.workshop.publish(pub);
    }

    @Override
    public void inviteFriends(){
//        SVars.net.showFriendInvites();
    }

    @Override
    public void updateLobby(){
//        SVars.net.updateLobby();
    }

    @Override
    public void updateRPC(){
        //if we're using neither discord nor steam, do no work
        if(!useDiscord && !steam) return;

        //common elements they each share
        boolean inGame = !state.is(State.menu);
        String gameMapWithWave = "Unknown Map";
        String gameMode = "";
        String gamePlayersSuffix = "";
        String uiState = "";

        if(inGame){
            if(world.getMap() != null){
                gameMapWithWave = world.isZone() ? world.getZone().localizedName : Strings.capitalize(world.getMap().name());
            }
            if(state.rules.waves){
                gameMapWithWave += " | Wave " + state.wave;
            }
            gameMode = state.rules.pvp ? "PvP" : state.rules.attackMode ? "Attack" : "Survival";
            if(net.active() && playerGroup.size() > 1){
                gamePlayersSuffix = " | " + playerGroup.size() + " Players";
            }
        }else{
            if(ui.editor != null && ui.editor.isShown()){
                uiState = "In Editor";
            }else if(ui.deploy != null && ui.deploy.isShown()){
                uiState = "In Launch Selection";
            }else{
                uiState = "In Menu";
            }
        }

        if(useDiscord){
            DiscordRichPresence presence = new DiscordRichPresence();

            if(inGame){
                presence.state = gameMode + gamePlayersSuffix;
                presence.details = gameMapWithWave;
                if(state.rules.waves){
                    presence.largeImageText = "Wave " + state.wave;
                }
            }else{
                presence.state = uiState;
            }

            presence.largeImageKey = "logo";

            DiscordRPC.INSTANCE.Discord_UpdatePresence(presence);
        }

        if(steam){
//            //Steam mostly just expects us to give it a nice string, but it apparently expects "steam_display" to always be a loc token, so I've uploaded this one which just passes through 'steam_status' raw.
//            SVars.net.friends.setRichPresence("steam_display", "#steam_status_raw");
//
//            if(inGame){
//                SVars.net.friends.setRichPresence("steam_status", gameMapWithWave);
//            }else{
//                SVars.net.friends.setRichPresence("steam_status", uiState);
//            }
        }
    }

    @Override
    public String getUUID(){
        if(steam){
//            try{
//                byte[] result = new byte[8];
//                new Rand(SVars.user.user.getSteamID().getAccountID()).nextBytes(result);
//                return new String(Base64Coder.encode(result));
//            }catch(Exception e){
//                e.printStackTrace();
//            }
        }

        try{
            Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
            NetworkInterface out;
            for(out = e.nextElement(); (out.getHardwareAddress() == null || out.isVirtual() || !validAddress(out.getHardwareAddress())) && e.hasMoreElements(); out = e.nextElement());

            byte[] bytes = out.getHardwareAddress();
            byte[] result = new byte[8];
            System.arraycopy(bytes, 0, result, 0, bytes.length);

            String str = new String(Base64Coder.encode(result));

            if(str.equals("AAAAAAAAAOA=") || str.equals("AAAAAAAAAAA=")) throw new RuntimeException("Bad UUID.");

            return str;
        }catch(Exception e){
            return super.getUUID();
        }
    }

    private static void message(String message){
        SDL.SDL_ShowSimpleMessageBox(SDL.SDL_MESSAGEBOX_ERROR, "oh no", message);
    }

    private boolean validAddress(byte[] bytes){
        if(bytes == null) return false;
        byte[] result = new byte[8];
        System.arraycopy(bytes, 0, result, 0, bytes.length);
        return !new String(Base64Coder.encode(result)).equals("AAAAAAAAAOA=") && !new String(Base64Coder.encode(result)).equals("AAAAAAAAAAA=");
    }
}
