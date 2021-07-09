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

import arc.math.Mathf;
import arc.struct.Array;

/**  单个循环分支任务. 一个{@code SingleRunningChildBranch} 任务是一个分支任务, 它只支持运行一个子任务.<p/>
 * A {@code SingleRunningChildBranch} task is a branch task that supports only one running child at a time.
 * 
 * @param <E> type of the blackboard object that tasks use to read or modify game state
 * 
 * @author implicit-invocation
 * @author davebaol */
public abstract class SingleRunningChildBranch<E> extends BranchTask<E> {

	/** 如果没有子任务运行, 则在返回运行状态为{@code null}. <p/>The child in the running status or {@code null} if no child is running. */
	protected Task<E> runningChild;

	/** 当前处理子任务索引.<p/>The index of the child currently processed. */
	protected int currentChildIndex;

	/** 随机子任务群. 如果它是{@code null}, 这个任务是确定的. <p/>Array of random children. If it's {@code null} this task is deterministic. */
	protected Task<E>[] randomChildren;

	/** 创建一个无子任务的{@code SingleRunningChildBranch} .<p/>Creates a {@code SingleRunningChildBranch} task with no children */
	public SingleRunningChildBranch () {
		super();
	}

	/** 指定子任务列表创建一个{@code SingleRunningChildBranch}.<p/>
	 * Creates a {@code SingleRunningChildBranch} task with a list of children
	 * 
	 * @param tasks list of this task's children, can be empty */
	public SingleRunningChildBranch (Array<Task<E>> tasks) {
		super(tasks);
	}

	@Override
	public void childRunning (Task<E> task, Task<E> reporter) {
		runningChild = task;
		running(); // Return a running status when a child says it's running
	}

	@Override
	public void childSuccess (Task<E> task) {
		this.runningChild = null;
	}

	@Override
	public void childFail (Task<E> task) {
		this.runningChild = null;
	}

	@Override
	public void run () {
		if (runningChild != null) {
			runningChild.run();
		} else {
			if (currentChildIndex < children.size) {
				if (randomChildren != null) {
					int last = children.size - 1;
					if (currentChildIndex < last) {
						// Random swap
						int otherChildIndex = Mathf.random(currentChildIndex, last);
						Task<E> tmp = randomChildren[currentChildIndex];
						randomChildren[currentChildIndex] = randomChildren[otherChildIndex];
						randomChildren[otherChildIndex] = tmp;
					}
					runningChild = randomChildren[currentChildIndex];
				} else {
					runningChild = children.get(currentChildIndex);
				}
				runningChild.setControl(this);
				runningChild.start();
				if (!runningChild.checkGuard(this))
					runningChild.fail();
				else
					run();
			} else {
				// Should never happen; this case must be handled by subclasses in childXXX methods
			}
		}
	}

	@Override
	public void start () {
		this.currentChildIndex = 0;
		runningChild = null;
	}

	@Override
	protected void cancelRunningChildren (int startIndex) {
		super.cancelRunningChildren(startIndex);
		runningChild = null;
	}

	@Override
	public void resetTask () {
		super.resetTask();
		this.currentChildIndex = 0;
		this.runningChild = null;
		this.randomChildren = null;
	}

	@Override
	protected Task<E> copyTo (Task<E> task) {
		SingleRunningChildBranch<E> branch = (SingleRunningChildBranch<E>)task;
		branch.randomChildren = null;

		return super.copyTo(task);
	}

	@SuppressWarnings("unchecked")
	protected Task<E>[] createRandomChildren () {
		Task<E>[] rndChildren = new Task[children.size];
		System.arraycopy(children.items, 0, rndChildren, 0, children.size);
		return rndChildren;
	}
	
	@Override
	public void reset() {
		this.currentChildIndex = 0;
		this.runningChild = null;
		this.randomChildren = null;
		super.reset();
	}

}
