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

/**  一个{@code LeafTask}是一个行为树的终端任务, 包含动作或条件逻辑, 不能有任何子任务.<p/>
 * A {@code LeafTask} is a terminal task of a behavior tree, contains action or condition logic, can not have any child.
 * 
 * @param <E> type of the blackboard object that tasks use to read or modify game state
 * 
 * @author implicit-invocation
 * @author davebaol */
@TaskConstraint(minChildren = 0, maxChildren = 0)
public abstract class LeafTask<E> extends Task<E> {

	/** 创建一个叶任务.<p/>Creates a leaf task. */
	public LeafTask () {
	}

	/** 该方法包含该叶片任务的更新逻辑. 实际的实现必须返回一个{@link Status#RUNNING}, {@link Status#SUCCEEDED} 或{@link Status#FAILED}.
	 *  其他返回值将导致{@code IllegalStateException}.<p/>
	 * This method contains the update logic of this leaf task. The actual implementation MUST return one of {@link Status#RUNNING}
	 * , {@link Status#SUCCEEDED} or {@link Status#FAILED}. Other return values will cause an {@code IllegalStateException}.
	 * @return the status of this leaf task */
	public abstract Status execute ();

	/**  该方法包含该任务的更新逻辑. 执行代表{@link #execute()}方法.<p/>
	 * This method contains the update logic of this task. The implementation delegates the {@link #execute()} method. */
	@Override
	public final void run () {
		Status result = execute();
		if (result == null) throw new IllegalStateException("Invalid status 'null' returned by the execute method");
		switch (result) {
		case SUCCEEDED:
			success();
			return;
		case FAILED:
			fail();
			return;
		case RUNNING:
			running();
			return;
		default:
			throw new IllegalStateException("Invalid status '" + result.name() + "' returned by the execute method");
		}
	}

	/** 总是抛出{@code IllegalStateException }因为一个叶子任务不能有任何子任务.<p/>Always throws {@code IllegalStateException} because a leaf task cannot have any children. */
	@Override
	protected int addChildToTask (Task<E> child) {
		throw new IllegalStateException("A leaf task cannot have any children");
	}

	@Override
	public int getChildCount () {
		return 0;
	}

	@Override
	public Task<E> getChild (int i) {
		throw new IndexOutOfBoundsException("A leaf task can not have any child");
	}

	@Override
	public final void childRunning (Task<E> runningTask, Task<E> reporter) {
	}

	@Override
	public final void childFail (Task<E> runningTask) {
	}

	@Override
	public final void childSuccess (Task<E> runningTask) {
	}

}
