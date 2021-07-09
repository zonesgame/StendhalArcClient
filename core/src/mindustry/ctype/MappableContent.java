package mindustry.ctype;

import mindustry.*;

/**
 *  映射内容
 * */
public abstract class MappableContent extends Content{
    /** 内容名称*/
    public final String name;

    public MappableContent(String name){
        this.name = Vars.content.transformName(name);
        Vars.content.handleMappableContent(this);
    }

    @Override
    public String toString(){
        return name;
    }
}
