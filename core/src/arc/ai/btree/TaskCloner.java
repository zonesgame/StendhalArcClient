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

/**  一个{@code TaskCloner}允许您使用像Kryo这样的第三方库克隆行为树. 参见{@link Task#TASK_CLONER}.<p/>
 * A {@code TaskCloner} allows you to use third-party libraries like Kryo to clone behavior trees. See {@link Task#TASK_CLONER}
 * 
 * @author davebaol */
public interface TaskCloner {

	/** 对给定的任务进行深入的复制.<p/>
	 * Makes a deep copy of the given task.
	 * @param task the task to clone
	 * @return the cloned task */
	public <T> Task<T> cloneTask (Task<T> task);

	/**  释放之前创建的任务{@link TaskCloner}.<p/>
	 * Free task previously created by this {@link TaskCloner}
	 * @param task task to free
	 */
	public <T> void freeTask(Task<T> task);

}
