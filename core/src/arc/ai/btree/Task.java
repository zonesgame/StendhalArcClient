/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package arc.ai.btree;

import arc.ai.btree.annotation.TaskConstraint;
import arc.util.pooling.Pool;
import arc.util.reflect.ClassReflection;
import arc.util.reflect.ReflectionException;

/**  这是所有行为树任务的抽象基类. 行为树的{@code Task}有一个状态器,一个控制器和一个子Task列表.<p/>
 * This is the abstract base class of all behavior tree tasks. The {@code Task} of a behavior tree has a status, one control and a
 * list of children.
 * 
 * @param <E> 任务操作的对象.<t/>type of the blackboard object that tasks use to read or modify game state
 * 
 * @author implicit-invocation
 * @author davebaol */
@TaskConstraint
public abstract class Task<E> implements Pool.Poolable {

	/** 一项任务执行状态值的枚举. <p/>
	 * The enumeration of the values that a task's status can have.
	 * 
	 * @author davebaol */
	public enum Status {
		/** 意味着任务从未运行或已被重置.<p/>Means that the task has never run or has been reset. */
		FRESH,
		/** 意味着任务需要再次运行.<p/>Means that the task needs to run again. */
		RUNNING,
		/** 表示任务返回失败结果.<p/>Means that the task returned a failure result. */
		FAILED,
		/** 这意味着任务返回了一个成功的结果.<p/>Means that the task returned a success result. */
		SUCCEEDED,
		/** 这意味着任务已经被取消.<p/>Means that the task has been terminated by an ancestor. */
		CANCELLED;
	}

	/** The clone strategy (if any) that {@link #cloneTask()} will use. Defaults to {@code null}, meaning that {@link #copyTo(Task)}
	 * is used instead. In this case, properly overriding this method in each task is developer's responsibility but this gives you
	 * the opportunity to target GWT.
	 * <p>
	 * For instance, if you don't care about GWT, you can let Kryo make a deep copy for you like that
	 * 
	 * <pre>
	 * <code>
	 *    Task.TASK_CLONER = new TaskCloner() {
	 *       Kryo kryo;
	 *       
	 *       {@literal @}Override
	 *       public {@code <T> Task<T> cloneTask (Task<T>} task) {
	 *          if (kryo == null) {
	 *             kryo = new Kryo();
	 *             kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
	 *          }
	 *          return kryo.copy(task);
	 *       }
	 *    };
	 * </code>
	 * </pre> */
	public static TaskCloner TASK_CLONER = null;

	/** 任务执行结果.<p/>The status of this task. */
	protected Status status = Status.FRESH;

	/** 父任务控制器.<p/>The parent of this task */
	protected Task<E> control;

	/** 任务所属行为树.<p/>The behavior tree this task belongs to. */
	protected BehaviorTree<E> tree;

	/** 这个任务的保护.<p/>The guard of this task */
	protected Task<E> guard;

	/** 添加一个子任务到列表.<p/>
	 * This method will add a child to the list of this task's children
	 * 
	 * @param child the child task which will be added
	 * @return the index where the child has been added.
	 * @throws IllegalStateException if the child cannot be added for whatever reason. */
	public final int addChild (Task<E> child) {
		int index = addChildToTask(child);
		if (tree != null && tree.listeners != null) tree.notifyChildAdded(this, index);
		return index;
	}

	/** 添加一个子任务到列表.<p/>
	 * This method will add a child to the list of this task's children
	 * 
	 * @param child the child task which will be added
	 * @return the index where the child has been added.
	 * @throws IllegalStateException if the child cannot be added for whatever reason. */
	protected abstract int addChildToTask (Task<E> child);

	/** 子任务数量.<p/>
	 * Returns the number of children of this task.
	 * 
	 * @return an int giving the number of children of this task */
	public abstract int getChildCount ();

	/** 返回指定索引子任务.<p/>Returns the child at the given index. */
	public abstract Task<E> getChild (int i);

	/** 返回任务绑定对象.<p/>
	 * Returns the blackboard object of the behavior tree this task belongs to.
	 * @throws IllegalStateException if this task has never run */
	public E getObject () {
		if (tree == null) throw new IllegalStateException("This task has never run");
		return tree.getObject();
	}

	/** 返回这个任务的保护.<p/>Returns the guard of this task. */
	public Task<E> getGuard () {
		return guard;
	}

	/** 设置这个任务的保护.<p/>Sets the guard of this task.
	 * @param guard the guard */
	public void setGuard (Task<E> guard) {
		this.guard = guard;
	}

	/** 返回该任务的执行状态.<p/>Returns the status of this task. */
	public final Status getStatus () {
		return status;
	}

	/** 设置父任务.<p/>
	 * This method will set a task as this task's control (parent)
	 * 
	 * @param control the parent task */
	public final void setControl (Task<E> control) {
		this.control = control;
		this.tree = control.tree;
	}

	/** 检查这个任务的保护.<p/>Checks the guard of this task.
	 * @param control the parent task
	 * @return {@code true} if guard evaluation succeeds or there's no guard; {@code false} otherwise.
	 * @throws IllegalStateException if guard evaluation returns any status other than {@link Status#SUCCEEDED} and
	 *            {@link Status#FAILED}. */
	public boolean checkGuard (Task<E> control) {
		// No guard to check
		if (guard == null) return true;
		
		// Check the guard of the guard recursively
		if (!guard.checkGuard(control)) return false;

		// Use the tree's guard evaluator task to check the guard of this task
		guard.setControl(control.tree.guardEvaluator);
		guard.start();
		guard.run();
		switch (guard.getStatus()) {
		case SUCCEEDED:
			return true;
		case FAILED:
			return false;
		default:
			throw new IllegalStateException("Illegal guard status '" + guard.getStatus() + "'. Guards must either succeed or fail in one step.");
		}
	}

	/** 该方法将在该任务的第一次运行之前调用一次.<p/>This method will be called once before this task's first run. */
	public void start () {
	}

	/** 这个方法将被{@link #success()},{@link #fail()}或{@link #cancel()}调用.<p/>
	 * This method will be called by {@link #success()}, {@link #fail()} or {@link #cancel()}, meaning that this task's status has
	 * just been set to {@link Status#SUCCEEDED}, {@link Status#FAILED} or {@link Status#CANCELLED} respectively. */
	public void end () {
	}

	/** 该方法包含该任务的更新逻辑. 实际的实现必须调用{@link #run()},{@link #success()}或{@link #fail()}.<p/>
	 * This method contains the update logic of this task. The actual implementation MUST call {@link #running()},
	 * {@link #success()} or {@link #fail()} exactly once. */
	public abstract void run ();

	/** 该方法将被调用{@link #run()}以通知控制该任务需要再次运行.<p/>This method will be called in {@link #run()} to inform control that this task needs to run again */
	public final void running () {
		Status previousStatus = status;
		status = Status.RUNNING;
		if (tree.listeners != null && tree.listeners.size > 0) tree.notifyStatusUpdated(this, previousStatus);
		if (control != null) control.childRunning(this, this);
	}

	/** 该方法将被调用为{@link #run()},以通知控制该任务已完成运行并取得成功.<p/>This method will be called in {@link #run()} to inform control that this task has finished running with a success result */
	public final void success () {
		Status previousStatus = status;
		status = Status.SUCCEEDED;
		if (tree.listeners != null && tree.listeners.size > 0) tree.notifyStatusUpdated(this, previousStatus);
		end();
		if (control != null) control.childSuccess(this);
	}

	/** 该方法将被调用{@link #run()},以通知控制该任务已完成运行失败结果.<p/>This method will be called in {@link #run()} to inform control that this task has finished running with a failure result */
	public final void fail () {
		Status previousStatus = status;
		status = Status.FAILED;
		if (tree.listeners != null && tree.listeners.size > 0) tree.notifyStatusUpdated(this, previousStatus);
		end();
		if (control != null) control.childFail(this);
	}

	/** 当这个任务的一个孩子成功时, 这个方法就会被调用.<p/>
	 * This method will be called when one of the children of this task succeeds
	 * 
	 * @param task the task that succeeded */
	public abstract void childSuccess (Task<E> task);

	/** 当这个任务的一个孩子失败时, 这个方法就会被调用.<p/>
	 * This method will be called when one of the children of this task fails
	 * 
	 * @param task the task that failed */
	public abstract void childFail (Task<E> task);

	/** 当这个任务的一个parent需要再次运行时,这个方法就会被调用.<p/>
	 * This method will be called when one of the ancestors of this task needs to run again
	 * 
	 * @param runningTask the task that needs to run again
	 * @param reporter the task that reports, usually one of this task's children */
	public abstract void childRunning (Task<E> runningTask, Task<E> reporter);

	/** 终止这个任务和所有运行的子任务. 只有当任务running时, 才能调用此方法.<p/>Terminates this task and all its running children. This method MUST be called only if this task is running. */
	public final void cancel () {
		cancelRunningChildren(0);
		Status previousStatus = status;
		status = Status.CANCELLED;
		if (tree.listeners != null && tree.listeners.size > 0) tree.notifyStatusUpdated(this, previousStatus);
		end();
	}

	/** 将任务的running子任务从指定的索引结束.<p/>Terminates the running children of this task starting from the specified index up to the end.
	 * @param startIndex the start index */
	protected void cancelRunningChildren (int startIndex) {
		for (int i = startIndex, n = getChildCount(); i < n; i++) {
			Task<E> child = getChild(i);
			if (child.status == Status.RUNNING) child.cancel();
		}
	}

	/** 重置任务.<p/>Resets this task to make it restart from scratch on next run. */
	public void resetTask () {
		if (status == Status.RUNNING) cancel();
		for (int i = 0, n = getChildCount(); i < n; i++) {
			getChild(i).resetTask();
		}
		status = Status.FRESH;
		tree = null;
		control = null;
	}

	/** 将这个任务克隆到新的任务. 如果你没有通过{@link #TASK_CLONER)指定一个克隆策略, 那么新的任务将通过镜像实例化, 并调用{@link #copyTo(Task)}.<p/>
	 * Clones this task to a new one. If you don't specify a clone strategy through {@link #TASK_CLONER} the new task is
	 * instantiated via reflection and {@link #copyTo(Task)} is invoked.
	 * @return the cloned task
	 * @throws TaskCloneException if the task cannot be successfully cloned. */
	@SuppressWarnings("unchecked")
	public Task<E> cloneTask () {
		if (TASK_CLONER != null) {
			try {
				return TASK_CLONER.cloneTask(this);
			} catch (Throwable t) {
				throw new TaskCloneException(t);
			}
		}
		try {
			Task<E> clone = copyTo(ClassReflection.newInstance(this.getClass()));
			clone.guard = guard == null ? null : guard.cloneTask();
			return clone;
		} catch (ReflectionException e) {
			throw new TaskCloneException(e);
		}
	}

	/** 将这个任务复制到给定的任务. 这个方法是由{@link #cloneTask()}调用的{@link #TASK_CLONER }是{@code null}它是它的默认值.<p/>
	 * Copies this task to the given task. This method is invoked by {@link #cloneTask()} only if {@link #TASK_CLONER} is
	 * {@code null} which is its default value.
	 * @param task the task to be filled
	 * @return the given task for chaining
	 * @throws TaskCloneException if the task cannot be successfully copied. */
	protected abstract Task<E> copyTo (Task<E> task);
	
	@Override
	public void reset() {
		control = null;
		guard = null;
		status = Status.FRESH;
		tree = null;
	}

}
