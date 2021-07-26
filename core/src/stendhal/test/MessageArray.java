package stendhal.test;

import arc.struct.Array;
import arc.util.reflect.ArrayReflection;

/**
 *  聊天框消息数据容器
 * */
public class MessageArray<T> {
    /** 数据容器  UI显示数据*/
    private T[] showItems;

    /** 数据容器  Save文件数据*/
    private T[] saveItems;

    /** 最大接收尺寸*/
    private int size;
    /** 自动清除数据尺寸*/
    private int attenuate = 64;
    /** true 正序打印排列  false倒叙打印排列*/
    private boolean ordered;

    public MessageArray () {
        this(true, 256);
    }

    public MessageArray (boolean ordered) {
        this(ordered, 256);
    }

    public MessageArray (int capacity) {
        this(true, capacity);
    }

    public MessageArray (boolean ordered, int capacity) {
        this.ordered = ordered;
        showItems = (T[])new Object[capacity];
        saveItems = (T[])new Object[capacity];
    }

//    public void setOrdered (boolean order) {
//        this.ordered = order;
//    }

    public void add (T showValue, T saveValue) {
        if (size == this.showItems.length) {
            this.showItems = resize(this.showItems, attenuate);
            this.saveItems = resize(this.saveItems, attenuate);
            this.size = this.showItems.length - attenuate;
//            System.out.println("+++++++++++" + this.size + "     " + this.showItems.length + "     " + this.saveItems.length);
        }
        this.showItems[this.size] = showValue;
        this.saveItems[this.size++] = saveValue;
    }

    public T getShow (int index) {
        if (index >= size) throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + size);
        return showItems[index];
    }


    public boolean isEmpty () {
        return size == 0;
    }

    public void clear () {
        T[] items = this.showItems;
        for (int i = 0, n = size; i < n; i++)
            items[i] = null;
        items = this.saveItems;
        for (int i = 0, n = size; i < n; i++)
            items[i] = null;
        size = 0;
    }

    /** Creates a new backing array with the specified size containing the current items. */
    protected T[] resize (T[] items, int attenuate) {
        T[] newItems = (T[]) ArrayReflection.newInstance(items.getClass().getComponentType(), items.length);
        System.arraycopy(items, attenuate, newItems, 0, items.length - attenuate);
//        this.size = items.length - attenuate;
//        System.out.println(this.getClass().getSimpleName() + ":  " + (items.length - attenuate) + "      " + newItems.length);
        return newItems;
    }

    public T[] toArray (T[] items) {
        return (T[])toArray(items, items.getClass().getComponentType());
    }

    public <V> V[] toArray (T[] items, Class type) {
        V[] result = (V[])ArrayReflection.newInstance(type, size);
        System.arraycopy(items, 0, result, 0, size);
        return result;
    }

    @Override
    public int hashCode () {
        if (!ordered) return super.hashCode();
        Object[] items = this.showItems;
        int h = 1;
        for (int i = 0, n = size; i < n; i++) {
            h *= 31;
            Object item = items[i];
            if (item != null) h += item.hashCode();
        }
        return h;
    }

    @Override
    public boolean equals (Object object) {
        if (object == this) return true;
        if (!ordered) return false;
        if (!(object instanceof Array)) return false;
        Array array = (Array)object;
        if (!array.ordered) return false;
        int n = size;
        if (n != array.size) return false;
        Object[] items1 = this.showItems;
        Object[] items2 = array.items;
        for (int i = 0; i < n; i++) {
            Object o1 = items1[i];
            Object o2 = items2[i];
            if (!(o1 == null ? o2 == null : o1.equals(o2))) return false;
        }
        return true;
    }

    public String toString (boolean isSave) {
        return this.toString(isSave, ordered);
    }

    public String toString (boolean isSave, boolean order) {
        if (size == 0) return "";
        T[] items = null;
        if (isSave)
            items = this.saveItems;
        else
            items = this.showItems;

        StringBuilder buffer = new StringBuilder(64);

        if (order) {
            buffer.append(items[0]);
            for (int i = 1; i < size; i++) {
                buffer.append("\n" + items[i]);
            }
        }
        else {
            buffer.append(items[size - 1]);
            for (int i = size - 2; i >= 0; i--) {
                buffer.append("\n" + items[i]);
            }
        }
//        buffer.append(items[size - 1]);
//        for (int i = size - 2; i >= 0; i--) {
//            buffer.append("\n" + items[i]);
//        }
        return buffer.toString();
    }

}