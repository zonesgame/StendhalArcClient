package mindustry.mod;

/**
 *  模组信息列表.<p/>
 * Mod listing as a data class.
 * */
public class ModListing{
    /** 再用*/
    public String repo, /** 名称*/name, /** 作者*/author, /** 更新日期*/lastUpdated, /** 描述信息*/description;
    /** 评级*/
    public int stars;

    @Override
    public String toString(){
        return "ModListing{" +
        "repo='" + repo + '\'' +
        ", name='" + name + '\'' +
        ", author='" + author + '\'' +
        ", lastUpdated='" + lastUpdated + '\'' +
        ", description='" + description + '\'' +
        ", stars=" + stars +
        '}';
    }
}
