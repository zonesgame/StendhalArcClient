package mindustry.ctype;

import arc.files.*;
import arc.util.ArcAnnotate.*;
import mindustry.*;
import mindustry.mod.Mods.*;


/**
 *  ContentLoader中加载的基类.<p/>
 * Base class for a content type that is loaded in {@link mindustry.core.ContentLoader}.
 * */
public abstract class Content implements Comparable<Content>{
    public final short id;
    /** 从mod中加入内容信息.<p/>Info on which mod this content was loaded from. */
    public @NonNull ModContentInfo minfo = new ModContentInfo();


    public Content(){
        this.id = (short) Vars.content.getBy(getContentType()).size;
        Vars.content.handleContent(this);
    }

    /**
     *  返回此内容的类型名称. 这应该对该内容类型的所有实例返回相同值.<p/>
     * Returns the type name of this piece of content.
     * This should return the same value for all instances of this content type.
     */
    public abstract ContentType getContentType();

    /** 在所有内容和模块创建后调用. 不要用于加载区域或纹理数据!<p/>Called after all content and modules are created. Do not use to load regions or texture data! */
    public void init(){
    }

    /**
     *  在所有内容创建后调用, 只在非headless版本上创建. 用于加载纹理或其它图像数据.<p/>
     * Called after all content is created, only on non-headless versions.
     * Use for loading regions or other image data.
     */
    public void load(){
    }

    /** @return 在mod加载中是否有错误.<p/>whether an error ocurred during mod loading. */
    public boolean hasErrored(){
        return minfo.error != null;
    }

    @Override
    public int compareTo(Content c){
        return Integer.compare(id, c.id);
    }

    @Override
    public String toString(){
        return getContentType().name() + "#" + id;
    }


    /**
     *  Mod内容信息
     * */
    public static class ModContentInfo{
        /** 载入这部分内容的mod.<p/>The mod that loaded this piece of content. */
        public @Nullable LoadedMod mod;
        /** 加载内容的文件.<p/>File that this content was loaded from. */
        public @Nullable Fi sourceFile;
        /** 在加载过程中发生的错误,如适用. null没有错误发生.<p/>The error that occurred during loading, if applicable. Null if no error occurred. */
        public @Nullable String error;
        /** 基础的导致错误异常.<p/>Base throwable that caused the error. */
        public @Nullable Throwable baseError;
    }
}
