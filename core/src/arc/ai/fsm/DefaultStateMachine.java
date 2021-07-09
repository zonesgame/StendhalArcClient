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

/** 默认实现状态机接口类.<p>
 * Default implementation of the {@link StateMachine} interface.
 * 
 * @param <E> 拥有状态机实体.<p>the type of the entity owning this state machine
 * @param <S> 状态机状态类型.<p>the type of the states of this state machine
 * 
 * @author davebaol */
public class DefaultStateMachine<E, S extends State<E>> implements StateMachine<E, S> {

	/** 状态机拥有实体.<p>The entity that owns this state machine. */
	protected E owner;

	/** 状态机当前状态.<p>The current state the owner is in. */
	protected S currentState;

	/** 状态机最后状态.<p>The last state the owner was in. */
	protected S previousState;

	/** 全局状态, 每次更新时都会调用执行.<p>The global state of the owner. Its logic is called every time the FSM is updated. */
	protected S globalState;

	/** 创建未初始化, 状态机.<p>
	 * Creates a {@code DefaultStateMachine} with no owner, initial state and global state. */
	public DefaultStateMachine () {
		this(null, null, null);
	}

	/** 创建指定拥有者, 状态机.<p>
	 * Creates a {@code DefaultStateMachine} for the specified owner.
	 * @param owner the owner of the state machine */
	public DefaultStateMachine (E owner) {
		this(owner, null, null);
	}

	/** 创建指定拥有者, 和初始化状态, 状态机.<p>
	 * Creates a {@code DefaultStateMachine} for the specified owner and initial state.
	 * @param owner the owner of the state machine
	 * @param initialState the initial state */
	public DefaultStateMachine (E owner, S initialState) {
		this(owner, initialState, null);
	}

	/** 创建指定拥有者, 初始化状态, 和全局状态, 状态机.<p>
	 * Creates a {@code DefaultStateMachine} for the specified owner, initial state and global state.
	 * @param owner the owner of the state machine
	 * @param initialState the initial state
	 * @param globalState the global state */
	public DefaultStateMachine (E owner, S initialState, S globalState) {
		this.owner = owner;
		this.setInitialState(initialState);
		this.setGlobalState(globalState);
	}
	/** 返回状态机拥有者.<p>Returns the owner of this state machine. */
	public E getOwner () {
		return owner;
	}

	/** 设置状态机拥有者.<p>Sets the owner of this state machine.
	 * @param owner the owner. */
	public void setOwner (E owner) {
		this.owner = owner;
	}

	@Override
	public void setInitialState (S state) {
		this.previousState = null;
		this.currentState = state;
	}

	@Override
	public void setGlobalState (S state) {
		this.globalState = state;
	}

	@Override
	public S getCurrentState () {
		return currentState;
	}

	@Override
	public S getGlobalState () {
		return globalState;
	}

	@Override
	public S getPreviousState () {
		return previousState;
	}

	/**  通过先调用全局状态(如果有的话)的{@code update}方法来更新状态机, 然后调用当前状态的{@code update}方法.<p>
	 * Updates the state machine by invoking first the {@code execute} method of the global state (if any) then the {@code execute}
	 * method of the current state. */
	@Override
	public void update () {
		// Execute the global state (if any)
		if (globalState != null) globalState.update(owner);

		// Execute the current state (if any)
		if (currentState != null) currentState.update(owner);
	}

	@Override
	public void changeState (S newState) {
		{	// Mindusty check
			if(newState == currentState) return;       // default  ==
		}
		// Keep a record of the previous state
		previousState = currentState;

		// Call the exit method of the existing state
		if (currentState != null) currentState.exit(owner);

		// Change state to the new state
		currentState = newState;

		// Call the entry method of the new state
		if (currentState != null) currentState.enter(owner);
	}

	@Override
	public boolean revertToPreviousState () {
		if (previousState == null) {
			return false;
		}

		changeState(previousState);
		return true;
	}

	/** Indicates whether the state machine is in the given state.
	 * <p>
	 * This implementation assumes states are singletons (typically an enum) so they are compared with the {@code ==} operator
	 * instead of the {@code equals} method.
	 * 
	 * @param state the state to be compared with the current state
	 * @return true if the current state and the given state are the same object. */
	@Override
	public boolean isInState (S state) {
		return currentState == state;
	}

	/** Handles received telegrams. The telegram is first routed to the current state. If the current state does not deal with the
	 * message, it's routed to the global state's message handler.
	 * 
	 * @param telegram the received telegram
	 * @return true if telegram has been successfully handled; false otherwise. */
	@Override
	public boolean handleMessage (Telegram telegram) {

		// First see if the current state is valid and that it can handle the message
		if (currentState != null && currentState.onMessage(owner, telegram)) {
			return true;
		}

		// If not, and if a global state has been implemented, send
		// the message to the global state
		if (globalState != null && globalState.onMessage(owner, telegram)) {
			return true;
		}

		return false;
	}
}
