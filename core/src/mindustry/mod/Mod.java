package mindustry.mod;

import arc.files.*;
import arc.util.*;
import mindustry.*;

/**
 *  模组
 * */
public class Mod{
    /** @return 配置文件.<p/>the config file for this plugin, as the file 'mods/[plugin-name]/config.json'.*/
    public Fi getConfig(){
        return Vars.mods.getConfig(this);
    }

    /** 初始化.<p/>Called after all plugins have been created and commands have been registered.*/
    public void init(){

    }

    /** 登记服务器控制台命令.<p/>Register any commands to be used on the server side, e.g. from the console. */
    public void registerServerCommands(CommandHandler handler){

    }

    /** 登记客户端控制台命令.<p/>Register any commands to be used on the client side, e.g. sent from an in-game player.. */
    public void registerClientCommands(CommandHandler handler){

    }
}
