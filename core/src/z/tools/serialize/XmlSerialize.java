package z.tools.serialize;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import arc.Core;
import arc.Events;
import arc.struct.ObjectMap;
import arc.util.Log;
import arc.util.reflect.ClassReflection;
import arc.util.reflect.ReflectionException;
import arc.util.serialization.JsonReader;
import arc.util.serialization.JsonValue;
import arc.util.serialization.XmlReader;
import arc.util.serialization.XmlReader.Element;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.Bullets;
import mindustry.content.Items;
import mindustry.entities.bullet.BulletType;
import mindustry.game.EventType;
import mindustry.type.Item;
import mindustry.world.Block;

/**
 *
 */
public class XmlSerialize {
    // caesar begon
    public static final String aniRegions = "ANIREGIONS";
    public static final String offset = "OFFSET";
    // caesar end
    // zones qqtx begon
    public static final String qqtxRegions = "QQTXREGIONS";
    public static final String qqtxRects = "QQTXRECTS";
    /** 块地板*/
    public static final String qqtxBG = "QQTXBG";
    public static final String qqtxBGR = "QQTXBGR";
    /**块动画*/
    public static final String qqtxANI = "QQTXANI";
    public static final String qqtxANIR = "QQTXANIR";
    // zones qqtx end




    protected JsonReader jsonReader;
    protected JSON json;

    private BlockLoader blockLoader;
    private UnitLoader unitLoader;

    /** 存储临时xml文件数据*/
    private ObjectMap<String, XmlReader.Element> roots;

    public XmlSerialize() {
        jsonReader = new JsonReader();
        json = new JSON();
        roots = new ObjectMap<>(32);

        blockLoader = new BlockLoader(this);
        unitLoader = new UnitLoader(this);
        {   // temp code
            items = new Items();
            bullets = new Bullets();
            initDefaults();
        }

        Events.on(EventType.ClearCacheEvent.class, event -> clear());
    }

    private void initDefaults() {
        // 添加默认类型, 和内部类
//        classMap.put("null", null);
        classMap.put(Boolean.class, boolean.class);
        classMap.put(Float.class, float.class);
        classMap.put(Integer.class, int.class);
        classMap.put(Long.class, long.class);
        classMap.put(Double.class, double.class);
        classMap.put(Short.class, short.class);
        classMap.put(Byte.class, byte.class);
        initClassReflection();
    }

    private void initClassReflection() {
        String reflectionFile = "debug/xml/ClassReflection.xml";
        Element root = Vars.xmlReader.parse(Core.files.internal(reflectionFile));
        for (int i = 0; i < root.getChildCount(); i++) {
            Element node = root.getChild(i);
            String key = node.getAttribute("key", null);
            if (key == null || key.equals(""))  continue;

            Class value = null;
            try {
                value = Class.forName(node.getAttribute("class"));
            } catch (ClassNotFoundException e) {
                continue;
            }
            json.classMap.put(key, value);
        }
    }


    /** Block, 加载块配置数据*/
    public void loadBlockConfig(String configFile, Object instance) {
        blockLoader.loadConfigFile(getElement(configFile), instance);
    }

    /** Block, 加载块动画数据*/
    public ObjectMap loadBlockAnimation(String configFile) {
        return blockLoader.loadAnimation(getElement(configFile));
    }

    /** Unit, 加载单位动画数据*/
    public ObjectMap loadUnitAnimation(String configFile) {
        return unitLoader.loadAnimation(getElement(configFile));
    }


    private void clear() {
        roots.clear();
        Log.info("XmlSerialize: clear xml cache over.");
    }


    private XmlReader.Element getElement(String filePath) {
        XmlReader.Element root = roots.get(filePath);
        if (root == null) {
            root = Vars.xmlReader.parse(Core.files.internal(filePath));
            roots.put(filePath, root);
        }

        return root;
    }


//    private ObjectMap<String, Item> itemPool = new ObjectMap<>();
    private final Items items;
    protected Item getItem(String keyName) {
//        Class.getField("keyName").get(new Items()); // or
        Item item = null;
        try {
            item = (Item) items.getClass().getField(keyName).get(items);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return item;
    }

    private final Bullets bullets;
    protected BulletType getBulletType(String keyName) {
        BulletType bulletType = null;
        try {
            bulletType = (BulletType) ClassReflection.getField(Bullets.class, keyName).get(bullets);
//            bulletType = (BulletType) bullets.getClass().getField(keyName).get(bullets);
        } catch (ReflectionException e) {
            e.printStackTrace();
        }
        return bulletType;
    }

    /** 获取自定义对象*/
    protected Object getCustomObject(String name, JsonValue value) {
        if (name.equals("Items")) {
            String paramName = value.child.asString();
            paramName = paramName.substring(paramName.indexOf('.') + 1);
            return getItem(paramName);
        }

        return null;
    }

    /** 获取内部数据类型*/
    private ObjectMap<Class, Class> classMap = new ObjectMap();
    private Class<?> getPrimitiveClass(Class c) {
        Class returnValue = classMap.get(c);
        return returnValue == null ? c : returnValue;
    }

    // extends static function
    protected void executeField(Object instance, String fieldName, Object setValue) {
        try {
            instance.getClass().getField(fieldName).set(instance, setValue);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    protected void executeMethod(Object instance, String methodName, Class[] classValues, Object[] parameterValues) {
        Method method = null;
        try {
            method = instance.getClass().getMethod(methodName, classValues);
        } catch (NoSuchMethodException e) {
        }

        try {
            if(method != null) {
                if(parameterValues == null){
                    method.invoke(instance);
                }
                else if(method.getParameterTypes().length == 1) {
                    Object parameters = parameterValues[0];
                    method.invoke(instance, parameters);
                }
                else {
                    method.invoke(instance, parameterValues);
                }
            }
            else {
                throw new RuntimeException("zones throw NoSuchMethodException.");
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    protected void executeMethod(Object instance, String methodName, Object... parameterValues) {
        try {
            Method method = null;
            try {
                if (parameterValues == null)
                    method = instance.getClass().getMethod(methodName);
                else
                    method = instance.getClass().getMethod(methodName, parameterValues.getClass());
            } catch (NoSuchMethodException e) {
            }

            if (method == null) {
                Class<?>[] arr = new Class[parameterValues.length];
                for (int i = arr.length; --i >= 0; ) {
                    arr[i] = getPrimitiveClass(parameterValues[i].getClass());
                }
                try {
                    method = instance.getClass().getMethod(methodName, arr);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }

            if(method != null) {
                if(parameterValues == null){
                    method.invoke(instance);
                }
                else if(method.getParameterTypes().length == 1) {
                    Object parameters = parameterValues;
                    method.invoke(instance, parameters);
                }
                else {
                    method.invoke(instance, parameterValues);
                }
            }
            else {
                throw new RuntimeException("zones throw NoSuchMethodException.");
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
    // end




    protected void loadMethods(Object instance, Element root) {
        for (int i = 0, len = root.getChildCount(); i < len; i++) {
            Element methodNode = root.getChild(i);

            String methodName = methodNode.getAttribute("method", methodNode.getName());
            if (methodName == null || methodName.equals("")) return;

            int paramsCount = methodNode.getInt("paramsCount", methodNode.getChildCount());
            Class[] classes = null;
            Object[] objects = null;

            try {
                if (paramsCount > 0) {
                    classes = new Class[paramsCount];
                    objects = new Object[paramsCount];

                    for (int pi = 0; pi < paramsCount; pi++) {
                        Element paramsNode = methodNode.getChildByName("param" + (pi+1));

                        Class c = null;
                        String classKey = paramsNode.getAttribute("class", null);
                        if (classKey != null) {
                            c = json.classMap.get(classKey);
                            if (c == null)
                                c = Class.forName(classKey);
                        }

                        Object o = null;
                        o = json.readValue(c, Vars.jsonReader.parse(paramsNode.getText()));

                        classes[pi] = c == null ? o.getClass() : c ;
                        objects[pi] = o;
                    }
                }
            } catch (ClassNotFoundException e) {
                Log.warn(methodNode.toString());
                continue;
            }

            executeMethod(instance, methodName,  classes, objects);    // 参数
        }
    }

    protected void loadFields(Object instance, Element root) {
        try { //  // 属性数据加载测试
            for(int i = 0; i < root.getChildCount(); i++) {
                XmlReader.Element fieldNode = root.getChild(i);
                JsonValue value = jsonReader.parse(fieldNode.getText());
                String fieldName = value.child.name;
                if(fieldName == null) fieldName = fieldNode.getName();

                json.readField(instance, fieldName, fieldName, value);
            }
        } catch (Exception e) {
            Log.warn("XmlSerialize: " + root.getParent().getAttribute("name", null), e);
        }
    }


    /**
     * @param  fieldName Blocks属性名称.
     * */
    @Deprecated
    public <T extends Block> T getBlock(String fieldName) {
        T obj = null;
        try {
            obj = (T) ClassReflection.getField(Blocks.class, fieldName).get(null);
//            bulletType = (BulletType) bullets.getClass().getField(keyName).get(bullets);
        } catch (ReflectionException e) {
            e.printStackTrace();
        }
        return obj;
    }
}
