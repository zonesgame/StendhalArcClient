package mindustry.ctype;

import arc.*;
import arc.util.ArcAnnotate.*;
import mindustry.annotations.Annotations.*;
import arc.graphics.g2d.*;
import arc.scene.ui.layout.*;
import mindustry.*;
import mindustry.graphics.*;
import mindustry.ui.Cicon;

/**
 *  需要解锁的内容类型.<p/>
 * Base interface for an unlockable content type.
 * */
public abstract class UnlockableContent extends MappableContent{
    /** 本地化的,正式的名称. 永远不要为空. 设置为块名,如果没有在bundle中找到.<p/>Localized, formal name. Never null. Set to block name if not found in bundle. */
    public String localizedName;
    /** 本地化描述, 可能为null.<p/>Localized description. May be null. */
    public @Nullable String description;
    /** 标志ID图标纹理容器.<p/>Icons by Cicon ID.*/
    protected TextureRegion[] cicons = new TextureRegion[mindustry.ui.Cicon.all.length];

    public UnlockableContent(String name){
        super(name);

        this.localizedName = Core.bundle.get(getContentType() + "." + this.name + ".name", this.name);
        this.description = Core.bundle.getOrNull(getContentType() + "." + this.name + ".description");
    }

    /** 显示描述内容*/
    public String displayDescription(){
        return minfo.mod == null ? description : description + "\n" + Core.bundle.format("mod.display", minfo.mod.meta.displayName());
    }

    /** 为该内容生成图标, 异步调用.<p/>Generate any special icons for this content. Called asynchronously.*/
    @CallSuper
    public void createIcons(MultiPacker packer){

    }

    /** 返回特定的内容图标, 或未找到的纹理.<p/>Returns a specific content icon, or the region {contentType}-{name} if not found.*/
    public TextureRegion icon(Cicon icon){
        if(cicons[icon.ordinal()] == null){
            cicons[icon.ordinal()] = Core.atlas.find(getContentType().name() + "-" + name + "-" + icon.name(),
                Core.atlas.find(getContentType().name() + "-" + name + "-full",
                Core.atlas.find(getContentType().name() + "-" + name,
                Core.atlas.find(name,
                Core.atlas.find(name + "1")))));
        }
        return cicons[icon.ordinal()];
    }

    /** 这应该显示指定表中的这些内容的所有必要信息.<p/>This should show all necessary info about this content in the specified table. */
    public abstract void displayInfo(Table table);

    /** 当此内容解锁时调用. 使用它来解锁其他相关内容.<p/>Called when this content is unlocked. Use this to unlock other related content. */
    public void onUnlock(){
    }

    /** 该内容是否总是隐藏在内容信息界面中.<p/>Whether this content is always hidden in the content info dialog. */
    public boolean isHidden(){
        return false;
    }

    /** 重写以使内容永远解锁.<p/>Override to make content always unlocked. */
    public boolean alwaysUnlocked(){
        return false;
    }

    /** 内容解锁状态*/
    public final boolean unlocked(){
        return Vars.data.isUnlocked(this);
    }

    /** @return 不管这个内容是否解锁, 或者玩家在一个自定义游戏中.<p/>whether this content is unlocked, or the player is in a custom game. */
    public final boolean unlockedCur(){
        return Vars.data.isUnlocked(this) || !Vars.world.isZone();
    }

    /** 内容是否上锁*/
    public final boolean locked(){
        return !unlocked();
    }
}
