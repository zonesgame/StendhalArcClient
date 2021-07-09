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

/**  状态机的状态. 定义了进入, 退出和结束该状态的实体的逻辑. 此外, 一个实体可以委托一个状态来处理它的消息.<p>
 * The state of a state machine defines the logic of the entities that enter, exit and last this state. Additionally, a state may
 * be delegated by an entity to handle its messages.
 * 
 * @param <E> is the type of the entity handled by this state machine
 * 
 * @author davebaol */
public interface State<E> {

	/** 进入状态执行.<p>
	 * This method will execute when the state is entered.
	 * 
	 * @param entity the entity entering the state */
	default void enter (E entity){};			// default public

	/** 状态正常更新方法.<p>This is the state's normal update function
	 * 
	 * @param entity the entity lasting the state */
	default void update (E entity){};			// default public

	/** 状态退出执行.<p>This method will execute when the state is exited.
	 * 
	 * @param entity the entity exiting the state */
	default void exit (E entity){};			// default public

	/** 实体在该状态下, 则接收消息由该状态处理.<p>This method executes if the {@code entity} receives a {@code telegram} from the message dispatcher while it is in this
	 * state.
	 * 
	 * @param entity 接收消息实体.<p>the entity that received the message
	 * @param telegram 发送消息实体.<p>the message sent to the entity
	 * @return true 表示消息已经成功处理.<p>if the message has been successfully handled; false otherwise. */
	default boolean onMessage (E entity, Telegram telegram){return false;};		// default public
}
