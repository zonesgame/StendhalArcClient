package z.utils;

import arc.util.pooling.Pool;
import mindustry.Vars;
import mindustry.content.UnitTypes;
import mindustry.entities.type.BaseUnit;

/**
 * A {@link Pool} which keeps track of the obtained items (see {@link #obtain()}), which can be free'd all at once using the
 * {@link #flush()} method.
 * @author Xoppa
 */
public class WorkerPool<T extends BaseUnit> extends Pool<T>{
//    protected Array<T> obtained = new Array<>();

    public WorkerPool(){
        super();
    }

    public WorkerPool(int initialCapacity){
        super(initialCapacity);
    }

    public WorkerPool(int initialCapacity, int max){
        super(initialCapacity, max);
    }

    @Override
    protected T newObject() {
        T unit = (T) UnitTypes.worker.create(Vars.player.getTeam());
//        unit.add();
        return unit;
    }

    @Override
    public void free(T object){
//        obtained.remove(object, true);
//        object.remove();
        super.free(object);
    }

    @Override
    public T obtain(){
        T result = super.obtain();
//        result.add();
//        obtained.add(result);
        return result;
    }

    /** Frees all obtained instances. */
//    public void flush(){
//        super.freeAll(obtained);
//        obtained.clear();
//    }

//    @Override
//    public void freeAll(Array<T> objects){
//        obtained.removeAll(objects, true);
//        super.freeAll(objects);
//    }
}
