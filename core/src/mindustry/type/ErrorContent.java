package mindustry.type;

import mindustry.ctype.*;
import mindustry.ctype.ContentType;

/** 表示一个错误的空白类型. 替换任何失败的解析. <p/>
 * Represents a blank type of content that has an error. Replaces anything that failed to parse. */
public class ErrorContent extends Content{
    @Override
    public ContentType getContentType(){
        return ContentType.error;
    }
}
