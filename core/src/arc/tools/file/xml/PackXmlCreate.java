package arc.tools.file.xml;

import java.io.IOException;

import arc.files.Fi;
import arc.util.serialization.XmlWriter;

/**
 *
 */
public class PackXmlCreate {

    private static PackXmlCreate instance;

    public static PackXmlCreate get() {
        if (instance == null)
            instance = new PackXmlCreate();
        return instance;
    }


    private XmlWriter xmlWriter;

    public PackXmlCreate() {
    }


    private boolean isFirstNode =  false;

    public PackXmlCreate create(Fi handle) {
        if (handle.exists())
            handle.delete();
        xmlWriter = new XmlWriter(handle.writer(false));
        isFirstNode = true;

        return this;
    }

    public PackXmlCreate addElement(String elementName) {
        try {
//            if (isFirstNode) {
//                isFirstNode = false;
//            } else {
//                xmlWriter.pop();
//            }

            xmlWriter.element(elementName);
        } catch (IOException e) {
            e.printStackTrace();
        }




        return this;
    }

    public PackXmlCreate addAttribute(String key, Object value) {
        try {
            xmlWriter.attribute(key, value);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return this;
    }

    public PackXmlCreate addText(Object value) {
        try {
            xmlWriter.text(value);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return this;
    }

    public PackXmlCreate pop() {
        try {
            xmlWriter.pop();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return this;
    }

    public PackXmlCreate close() {
        try {
//            xmlWriter.pop();
            xmlWriter.flush();
            xmlWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return this;
    }

    /** 扩展内容*/
    public PackXmlCreate append(String text) {
        try {
//            xmlWriter.pop();
            xmlWriter.append(text);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return this;
    }

}
