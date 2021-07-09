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

package arc.ai.fsm;

import arc.ai.msg.Telegram;
import arc.ai.msg.Telegraph;

/**  状态机管理其实体的状态转换. 此外, 实体可以委托状态机处理其消息.<p>
 * A state machine manages the state transitions of its entity. Additionally, the state machine may be delegated by the entity to
 * handle its messages.
 * 
 * @param <E> 表示拥有状态机实体.<p>the type of the entity owning this state machine
 * @param <S> 表示状态机状态类型.<p>the type of the states of this state machine
 * 
 * @author davebaol */
public interface StateMachine<E, S extends State<E>> extends Telegraph {

	/** 更新状态机.<p>
	 * Updates the state machine.
	 * <p>
	 * Implementation classes should invoke first the {@code update} method of the global state (if any) then the {@code update}
	 * method of the current state.
	 * </p> */
	public void update ();

	/** 执行指定状态的切换.<p>Performs a transition to the specified state.
	 * @param newState 切换到的新状态.<p>the state to transition to */
	public void changeState (S newState);

	/** 将状态更改为前一个状态.<p>Changes the state back to the previous state.
	 * @return {@code false} 表示没有先前状态.<p>{@code True} in case there was a previous state that we were able to revert to. In case there is no previous state,
	 *         no state change occurs and {@code false} will be returned. */
	public boolean revertToPreviousState ();

	/** 设置状态机初始状态.<p>Sets the initial state of this state machine.
	 * @param state the initial state. */
	public void setInitialState (S state);

	/** 设置状态机全局化状态.<p>Sets the global state of this state machine.
	 * @param state the global state. */
	public void setGlobalState (S state);

	/** 返回状态机当前状态.<p>Returns the current state of this state machine. */
	public S getCurrentState ();

	/** 返回状态机全局状态.<p>Returns the global state of this state machine.
	 * <p>
	 * Implementation classes should invoke the {@code update} method of the global state every time the FSM is updated. Also, they
	 * should never invoke its {@code enter} and {@code exit} method.
	 * </p> */
	public S getGlobalState ();

	/** 返回状态机最后一个状态.<p>Returns the last state of this state machine. */
	public S getPreviousState ();

	/** 只是状态机是否处于给定状态.<p>Indicates whether the state machine is in the given state.
	 * @param state 将与当前状态进行比较的状态.<p>the state to be compared with the current state
	 * @return true 当前状态类型等于参数状态类型.<p>if the current state's type is equal to the type of the class passed as a parameter. */
	public boolean isInState (S state);

	/** 处理接收消息.<p>
	 * 实现类应该首先将消息路由到当前状态. 如果当前状态不处理消息, 则应该将其路由到全局状态.<p>
	 * Handles received telegrams.
	 * <p>
	 * Implementation classes should first route the telegram to the current state. If the current state does not deal with the
	 * message, it should be routed to the global state.
	 * </p>
	 * @param telegram 接收消息.<p>the received telegram
	 * @return true 消息已经成功处理.<p>if telegram has been successfully handled; false otherwise. */
	public boolean handleMessage (Telegram telegram);
}
