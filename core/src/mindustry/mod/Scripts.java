package mindustry.mod;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.commonjs.module.RequireBuilder;
import org.mozilla.javascript.commonjs.module.provider.ModuleSource;
import org.mozilla.javascript.commonjs.module.provider.SoftCachingModuleScriptProvider;
import org.mozilla.javascript.commonjs.module.provider.UrlModuleSourceProvider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import arc.Core;
import arc.files.Fi;
import arc.struct.Array;
import arc.util.Disposable;
import arc.util.Log;
import arc.util.Log.LogLevel;
import arc.util.Time;
import mindustry.Vars;
import mindustry.mod.Mods.LoadedMod;

/**
 *  脚本管理器
 * */
public class Scripts implements Disposable{
    /** 黑色字体列表*/
    private final Array<String> blacklist = Array.with("net", "files", "reflect", "javax", "rhino", "file", "channels", "jdk",
        "runtime", "util.os", "rmi", "security", "org.", "sun.", "beans", "sql", "http", "exec", "compiler", "process", "system",
        ".awt", "socket", "classloader", "oracle", "invoke");
    /** 白色字体列表*/
    private final Array<String> whitelist = Array.with("mindustry.net");
    /** 脚本内容*/
    private final Context context;
    /** 脚本列表*/
    private Scriptable scope;
    /** 错误状态*/
    private boolean errored;
    /** 加载模组*/
    private LoadedMod currentMod = null;

    /** 脚本管理器构建器*/
    public Scripts(){
        Time.mark();

        context = Vars.platform.getScriptContext();
        context.setClassShutter(type -> !blacklist.contains(type.toLowerCase()::contains) || whitelist.contains(type.toLowerCase()::contains));
        context.getWrapFactory().setJavaPrimitiveWrap(false);

        scope = new ImporterTopLevel(context);

        new RequireBuilder()
            .setModuleScriptProvider(new SoftCachingModuleScriptProvider(new ScriptModuleProvider()))
            .setSandboxed(true).createRequire(context, scope).install(scope);

        if(!run(Core.files.internal("scripts/global.js").readString(), "global.js")){       // 加载全局脚本
            errored = true;
        }
        Log.debug("Time to load script engine: {0}", Time.elapsed());
    }

    /** 是否包含执行出错内容*/
    public boolean hasErrored(){
        return errored;
    }

    /** 执行控制台内容*/
    public String runConsole(String text){
        try{
            Object o = context.evaluateString(scope, text, "console.js", 1, null);
            if(o instanceof NativeJavaObject){
                o = ((NativeJavaObject)o).unwrap();
            }
            if(o instanceof Undefined){
                o = "undefined";
            }
            return String.valueOf(o);
        }catch(Throwable t){
            return getError(t);
        }
    }

    /** 处理错误异常*/
    private String getError(Throwable t){
        t.printStackTrace();
        return t.getClass().getSimpleName() + (t.getMessage() == null ? "" : ": " + t.getMessage());
    }

    /** 打印日志*/
    public void log(String source, String message){
        log(LogLevel.info, source, message);
    }

    /** 打印日志*/
    public void log(LogLevel level, String source, String message){
        Log.log(level, "[{0}]: {1}", source, message);
    }

    /** 记载模组*/
    public void run(LoadedMod mod, Fi file){
        currentMod = mod;
        run(file.readString(), file.name());
        currentMod = null;
    }

    /** 加载脚本文件*/
    private boolean run(String script, String file){
        try{
            if(currentMod != null){
                //inject script info into file (TODO maybe rhino handles this?)
                context.evaluateString(scope, "modName = \"" + currentMod.name + "\"\nscriptName = \"" + file + "\"", "initscript.js", 1, null);
            }
            context.evaluateString(scope, script, file, 1, null);
            return true;
        }catch(Throwable t){
            if(currentMod != null){
                file = currentMod.name + "/" + file;
            }
            log(LogLevel.err, file, "" + getError(t));
            return false;
        }
    }

    @Override
    public void dispose(){
        Context.exit();
    }


    /** 脚本模组提供者*/
    private class ScriptModuleProvider extends UrlModuleSourceProvider{
        private Pattern directory = Pattern.compile("^(.+?)/(.+)");

        public ScriptModuleProvider(){
            super(null, null);
        }

        @Override
        public ModuleSource loadSource(String moduleId, Scriptable paths, Object validator) throws IOException, URISyntaxException{
            if(currentMod == null) return null;
            return loadSource(moduleId, currentMod.root.child("scripts"), validator);
        }

        private ModuleSource loadSource(String moduleId, Fi root, Object validator) throws URISyntaxException{
            Matcher matched = directory.matcher(moduleId);
            if(matched.find()){
                LoadedMod required = Vars.mods.locateMod(matched.group(1));
                String script = matched.group(2);
                if(required == null){ // Mod not found, treat it as a folder
                    Fi dir = root.child(matched.group(1));
                    if(!dir.exists()) return null; // Mod and folder not found
                    return loadSource(script, dir, validator);
                }

                currentMod = required;
                return loadSource(script, required.root.child("scripts"), validator);
            }

            Fi module = root.child(moduleId + ".js");
            if(!module.exists() || module.isDirectory()) return null;
            return new ModuleSource(
                new InputStreamReader(new ByteArrayInputStream((module.readString()).getBytes())),
                null, new URI(moduleId), root.file().toURI(), validator);
        }
    }
}
