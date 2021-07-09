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
import arc.struct.Array;

/**  分支任务, 定义行为树分支, <p/>
 *  A branch task defines a behavior tree branch, contains logic of starting or running sub-branches and leaves
 * 
 * @param <E> type of the blackboard object that tasks use to read or modify game state
 * 
 * @author implicit-invocation
 * @author davebaol */
@TaskConstraint(minChildren = 1)
public abstract class BranchTask<E> extends Task<E> {

	/** 分支子任务容器.<p/>The children of this branch task. */
	protected Array<Task<E>> children;

	/** 在没有子任务的情况下创建一个分支任务.<p/>
	 * Create a branch task with no children */
	public BranchTask () {
		this(new Array<Task<E>>());
	}

	/** 指定子任务列表创建一个分支任务.<p/>
	 * Create a branch task with a list of children
	 * 
	 * @param tasks list of this task's children, can be empty */
	public BranchTask (Array<Task<E>> tasks) {
		this.children = tasks;
	}

	@Override
	protected int addChildToTask (Task<E> child) {
		children.add(child);
		return children.size - 1;
	}

	@Override
	public int getChildCount () {
		return children.size;
	}

	@Override
	public Task<E> getChild (int i) {
		return children.get(i);
	}

	@Override
	protected Task<E> copyTo (Task<E> task) {
		BranchTask<E> branch = (BranchTask<E>)task;
		if (children != null) {
			for (int i = 0; i < children.size; i++) {
				branch.children.add(children.get(i).cloneTask());
			}
		}

		return task;
	}
	
	@Override
	public void reset() {
		children.clear();
		super.reset();
	}

}
