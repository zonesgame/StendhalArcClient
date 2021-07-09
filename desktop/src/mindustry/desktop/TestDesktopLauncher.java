package mindustry.desktop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.NetworkInterface;
import java.nio.charset.Charset;
import java.util.Enumeration;

import arc.Files;
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
import mindustry.Vars;
import mindustry.core.GameState;
import mindustry.core.Platform;
import mindustry.core.Version;
import mindustry.net.ArcNetProvider;
import mindustry.net.CrashSender;
import mindustry.net.Net;
import mindustry.type.Publishable;
import z.TestLauncher;

import static mindustry.Vars.net;
import static mindustry.Vars.playerGroup;
import static mindustry.Vars.state;
import static mindustry.Vars.steam;
import static mindustry.Vars.testMobile;
import static mindustry.Vars.ui;
import static mindustry.Vars.world;

/**
 *
 */
public class TestDesktopLauncher extends TestLauncher  implements Platform {
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
            new SdlApplication(new TestDesktopLauncher(arg), new SdlConfig(){{
                title = "Mindustry";
//                maximized = true;
                depth = 0;
                stencil = 0;
                width = 1280;
                height = 720;
                setWindowIcon(Files.FileType.internal, "icons/icon_64.png");
            }});
        }catch(Throwable e){
            handleCrash(e);
        }
    }

    public TestDesktopLauncher(String[] args){
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
//        return super.getWorkshopContent(type);
        return null;
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
    public Net.NetProvider getNet(){
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
        boolean inGame = !state.is(GameState.State.menu);
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
    }

    @Override
    public String getUUID(){
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
//            return super.getUUID();
            return null;
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
