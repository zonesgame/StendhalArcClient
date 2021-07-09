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

package arc.ai.btree.branch;

import arc.ai.btree.SingleRunningChildBranch;
import arc.ai.btree.Task;
import arc.struct.Array;

/**  一个序列执行的分支任务, 它管理每个子任务, 直到其中一个失败. 如果子任务执行成功, 选择器将开始并运行下一个子任务.<p/>
 * A {@code Sequence} is a branch task that runs every children until one of them fails. If a child task succeeds, the selector
 * will start and run the next child task.
 * 
 * @param <E> type of the blackboard object that tasks use to read or modify game state
 * 
 * @author implicit-invocation */
public class Sequence<E> extends SingleRunningChildBranch<E> {

	/** Creates a {@code Sequence} branch with no children. */
	public Sequence () {
		super();
	}

	/** Creates a {@code Sequence} branch with the given children.
	 * 
	 * @param tasks the children of this task */
	public Sequence (Array<Task<E>> tasks) {
		super(tasks);
	}

	/** Creates a {@code Sequence} branch with the given children.
	 * 
	 * @param tasks the children of this task */
	public Sequence (Task<E>... tasks) {
		super(new Array<Task<E>>(tasks));
	}

	@Override
	public void childSuccess (Task<E> runningTask) {
		super.childSuccess(runningTask);
		if (++currentChildIndex < children.size) {
			run(); // Run next child
		} else {
			success(); // All children processed, return success status
		}
	}

	@Override
	public void childFail (Task<E> runningTask) {
		super.childFail(runningTask);
		fail(); // Return failure status when a child says it failed
	}

}
