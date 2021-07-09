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

package arc.ai.msg;

import arc.ai.GdxAI;
import arc.ai.Timepiece;
import arc.struct.Array;
import arc.struct.IntMap;
import arc.util.pooling.Pool;
import arc.util.reflect.ClassReflection;

/** 负责电报的创建、发送和管理.<p>
 * A {@code MessageDispatcher} is in charge of the creation, dispatch, and management of telegrams.
 * 
 * @author davebaol */
public class MessageDispatcher implements Telegraph {

	private static final String LOG_TAG = MessageDispatcher.class.getSimpleName();

	/** 消息缓存池*/
	private static final Pool<Telegram> POOL = new Pool<Telegram>(16) {
		@Override
		protected Telegram newObject () {
			return new Telegram();
		}
	};

	/** 消息队列*/
	private PriorityQueue<Telegram> queue;

	/** 消息监听者*/
	private IntMap<Array<Telegraph>> msgListeners;

	/** 消息提供者*/
	private IntMap<Array<TelegramProvider>> msgProviders;

	/** 调试开启状态*/
	private boolean debugEnabled;

	/** 创建一个消息派发器.<p>Creates a {@code MessageDispatcher} */
	public MessageDispatcher () {
		this.queue = new PriorityQueue<Telegram>();
		this.msgListeners = new IntMap<Array<Telegraph>>();
		this.msgProviders = new IntMap<Array<TelegramProvider>>();
	}

	/** returns true 调试状态.<p>Returns true if debug mode is on; false otherwise. */
	public boolean isDebugEnabled () {
		return debugEnabled;
	}

	/** 设置调试状态.<p>Sets debug mode on/off. */
	public void setDebugEnabled (boolean debugEnabled) {
		this.debugEnabled = debugEnabled;
	}

	/**  为指定的消息注册侦听器. 没有显式接收器的消息被发送到所有注册的监听器.<p>
	 * Registers a listener for the specified message code. Messages without an explicit receiver are broadcasted to all its
	 * registered listeners.
	 * @param listener the listener to add
	 * @param msg the message code */
	public void addListener (Telegraph listener, int msg) {
		Array<Telegraph> listeners = msgListeners.get(msg);
		if (listeners == null) {
			// Associate an empty unordered array with the message code
			listeners = new Array<Telegraph>(false, 16);
			msgListeners.put(msg, listeners);
		}
		listeners.add(listener);

		// Dispatch messages from registered providers
		Array<TelegramProvider> providers = msgProviders.get(msg);
		if (providers != null) {
			for (int i = 0, n = providers.size; i < n; i++) {
				TelegramProvider provider = providers.get(i);
				Object info = provider.provideMessageInfo(msg, listener);
				if (info != null) {
					Telegraph sender = ClassReflection.isInstance(Telegraph.class, provider) ? (Telegraph)provider : null;
					dispatchMessage(0, sender, listener, msg, info, false);
				}
			}
		}
	}

	/** 为指定的消息注册侦听器. 没有显式接收器的消息被发送到所有注册的监听器.<p>
	 * Registers a listener for a selection of message types. Messages without an explicit receiver are broadcasted to all its
	 * registered listeners.
	 * 
	 * @param listener the listener to add
	 * @param msgs the message codes */
	public void addListeners (Telegraph listener, int... msgs) {
		for (int msg : msgs)
			addListener(listener, msg);
	}

	/** 为指定消息注册提供者.<p>
	 * Registers a provider for the specified message code.
	 * @param msg the message code
	 * @param provider the provider to add */
	public void addProvider (TelegramProvider provider, int msg) {
		Array<TelegramProvider> providers = msgProviders.get(msg);
		if (providers == null) {
			// Associate an empty unordered array with the message code
			providers = new Array<TelegramProvider>(false, 16);
			msgProviders.put(msg, providers);
		}
		providers.add(provider);
	}

	/** 为指定消息注册提供者.<p>
	 * Registers a provider for a selection of message types.
	 * @param provider the provider to add
	 * @param msgs the message codes */
	public void addProviders (TelegramProvider provider, int... msgs) {
		for (int msg : msgs)
			addProvider(provider, msg);
	}

	/** 注销指定消息接收者.<p>
	 * Unregister the specified listener for the specified message code.
	 * @param listener the listener to remove
	 * @param msg the message code */
	public void removeListener (Telegraph listener, int msg) {
		Array<Telegraph> listeners = msgListeners.get(msg);
		if (listeners != null) {
			listeners.remove(listener, true);
		}
	}

	/** 注销指定消息接收者.<p>
	 * Unregister the specified listener for the selection of message codes.
	 * 
	 * @param listener the listener to remove
	 * @param msgs the message codes */
	public void removeListener (Telegraph listener, int... msgs) {
		for (int msg : msgs)
			removeListener(listener, msg);
	}

	/** 注销指定消息所有监听器.<p>
	 * Unregisters all the listeners for the specified message code.
	 * @param msg the message code */
	public void clearListeners (int msg) {
		msgListeners.remove(msg);
	}

	/** 注销指定消息所有监听器.<p>
	 * Unregisters all the listeners for the given message codes.
	 * 
	 * @param msgs the message codes */
	public void clearListeners (int... msgs) {
		for (int msg : msgs)
			clearListeners(msg);
	}

	/** 清空消息监听器.<p>Removes all the registered listeners for all the message codes. */
	public void clearListeners () {
		msgListeners.clear();
	}

	/** 注销指定消息提供者.<p>
	 * Unregisters all the providers for the specified message code.
	 * @param msg the message code */
	public void clearProviders (int msg) {
		msgProviders.remove(msg);
	}

	/** 清空消息提供者.<p>
	 * Unregisters all the providers for the given message codes.
	 * 
	 * @param msgs the message codes */
	public void clearProviders (int... msgs) {
		for (int msg : msgs)
			clearProviders(msg);
	}

	/** 清空消息提供者.<p>Removes all the registered providers for all the message codes. */
	public void clearProviders () {
		msgProviders.clear();
	}

	/** 清空消息队列, 并清空内存池.<p>Removes all the telegrams from the queue and releases them to the internal pool. */
	public void clearQueue () {
		for (int i = 0; i < queue.size(); i++) {
			POOL.free(queue.get(i));
		}
		queue.clear();
	}

	/** 清空消息池, 监听器, 和消息提供者.<p>Removes all the telegrams from the queue and the registered listeners for all the messages. */
	public void clear () {
		clearQueue();
		clearListeners();
		clearProviders();
	}

	/** 发送一个即时消息给所有注册的监听器, 没有额外的信息.<p>
	 * Sends an immediate message to all registered listeners, with no extra info.
	 * <p>
	 * This is a shortcut method for {@link #dispatchMessage(float, Telegraph, Telegraph, int, Object, boolean) dispatchMessage(0,
	 * null, null, msg, null, false)}
	 * 
	 * @param msg the message code */
	public void dispatchMessage (int msg) {
		dispatchMessage(0f, null, null, msg, null, false);
	}

	/** 发送一个即时消息给所有注册的监听器, 没有额外的信息.<p>
	 * Sends an immediate message to all registered listeners, with no extra info.
	 * <p>
	 * This is a shortcut method for {@link #dispatchMessage(float, Telegraph, Telegraph, int, Object, boolean) dispatchMessage(0,
	 * sender, null, msg, null, false)}
	 * 
	 * @param sender the sender of the telegram
	 * @param msg the message code */
	public void dispatchMessage (Telegraph sender, int msg) {
		dispatchMessage(0f, sender, null, msg, null, false);
	}

	/** 发送一个即时消息给所有注册的监听器, 没有额外的信息.<p>
	 * Sends an immediate message to all registered listeners, with no extra info.
	 * <p>
	 * This is a shortcut method for {@link #dispatchMessage(float, Telegraph, Telegraph, int, Object, boolean) dispatchMessage(0,
	 * sender, null, msg, null, needsReturnReceipt)}
	 * 
	 * @param sender the sender of the telegram
	 * @param msg the message code
	 * @param needsReturnReceipt 是否需要处理返回代码.<p>whether the return receipt is needed or not
	 * @throws IllegalArgumentException if the sender is {@code null} and the return receipt is needed */
	public void dispatchMessage (Telegraph sender, int msg, boolean needsReturnReceipt) {
		dispatchMessage(0f, sender, null, msg, null, needsReturnReceipt);
	}

	/** 发送一个即时消息给所有注册的监听器, 附加信息.<p>
	 * Sends an immediate message to all registered listeners, with extra info.
	 * <p>
	 * This is a shortcut method for {@link #dispatchMessage(float, Telegraph, Telegraph, int, Object, boolean) dispatchMessage(0,
	 * null, null, msg, extraInfo, false)}
	 * 
	 * @param msg the message code
	 * @param extraInfo an optional object */
	public void dispatchMessage (int msg, Object extraInfo) {
		dispatchMessage(0f, null, null, msg, extraInfo, false);
	}

	/** 发送一个即时消息给所有注册的监听器, 附加信息.<p>
	 * Sends an immediate message to all registered listeners, with extra info.
	 * <p>
	 * This is a shortcut method for {@link #dispatchMessage(float, Telegraph, Telegraph, int, Object, boolean) dispatchMessage(0,
	 * sender, null, msg, extraInfo, false)}
	 * 
	 * @param sender the sender of the telegram
	 * @param msg the message code
	 * @param extraInfo an optional object */
	public void dispatchMessage (Telegraph sender, int msg, Object extraInfo) {
		dispatchMessage(0f, sender, null, msg, extraInfo, false);
	}

	/**  发送一个即时消息给所有注册的监听器, 附加信息.<p>
	 * Sends an immediate message to all registered listeners, with extra info.
	 * <p>
	 * This is a shortcut method for {@link #dispatchMessage(float, Telegraph, Telegraph, int, Object, boolean) dispatchMessage(0,
	 * sender, null, msg, extraInfo, needsReturnReceipt)}
	 * 
	 * @param sender the sender of the telegram
	 * @param msg the message code
	 * @param extraInfo an optional object
	 * @param needsReturnReceipt whether the return receipt is needed or not
	 * @throws IllegalArgumentException if the sender is {@code null} and the return receipt is needed */
	public void dispatchMessage (Telegraph sender, int msg, Object extraInfo, boolean needsReturnReceipt) {
		dispatchMessage(0f, sender, null, msg, extraInfo, needsReturnReceipt);
	}

	/** 发送一个即时消息给所有注册的监听器, 附加信息. 发送一个即时消息到指定的接收者, 没有额外的信息. 接收方不需要是指定消息代码的注册侦听器.<p>
	 * Sends an immediate message to the specified receiver with no extra info. The receiver doesn't need to be a register listener
	 * for the specified message code.
	 * <p>
	 * This is a shortcut method for {@link #dispatchMessage(float, Telegraph, Telegraph, int, Object, boolean) dispatchMessage(0,
	 * sender, receiver, msg, null, false)}
	 * 
	 * @param sender the sender of the telegram
	 * @param receiver the receiver of the telegram; if it's {@code null} the telegram is broadcasted to all the receivers
	 *           registered for the specified message code
	 * @param msg the message code */
	public void dispatchMessage (Telegraph sender, Telegraph receiver, int msg) {
		dispatchMessage(0f, sender, receiver, msg, null, false);
	}

	/** 发送一个即时消息到指定的接收者, 没有额外的信息. 接收方不需要是指定消息代码的注册侦听器.<p>
	 * Sends an immediate message to the specified receiver with no extra info. The receiver doesn't need to be a register listener
	 * for the specified message code.
	 * <p>
	 * This is a shortcut method for {@link #dispatchMessage(float, Telegraph, Telegraph, int, Object, boolean) dispatchMessage(0,
	 * sender, receiver, msg, null, needsReturnReceipt)}
	 * 
	 * @param sender the sender of the telegram
	 * @param receiver the receiver of the telegram; if it's {@code null} the telegram is broadcasted to all the receivers
	 *           registered for the specified message code
	 * @param msg the message code
	 * @param needsReturnReceipt whether the return receipt is needed or not
	 * @throws IllegalArgumentException if the sender is {@code null} and the return receipt is needed */
	public void dispatchMessage (Telegraph sender, Telegraph receiver, int msg, boolean needsReturnReceipt) {
		dispatchMessage(0f, sender, receiver, msg, null, needsReturnReceipt);
	}

	/** 发送一个带有额外信息的即时消息到指定的接收者. 接收方不需要是指定消息代码的注册侦听器.<p>
	 * Sends an immediate message to the specified receiver with extra info. The receiver doesn't need to be a register listener
	 * for the specified message code.
	 * <p>
	 * This is a shortcut method for {@link #dispatchMessage(float, Telegraph, Telegraph, int, Object, boolean) dispatchMessage(0,
	 * sender, receiver, msg, extraInfo, false)}
	 * 
	 * @param sender the sender of the telegram
	 * @param receiver the receiver of the telegram; if it's {@code null} the telegram is broadcasted to all the receivers
	 *           registered for the specified message code
	 * @param msg the message code
	 * @param extraInfo an optional object */
	public void dispatchMessage (Telegraph sender, Telegraph receiver, int msg, Object extraInfo) {
		dispatchMessage(0f, sender, receiver, msg, extraInfo, false);
	}

	/** 发送一个带有额外信息的即时消息到指定的接收者. 接收方不需要是指定消息代码的注册侦听器.<p>
	 * Sends an immediate message to the specified receiver with extra info. The receiver doesn't need to be a register listener
	 * for the specified message code.
	 * <p>
	 * This is a shortcut method for {@link #dispatchMessage(float, Telegraph, Telegraph, int, Object, boolean) dispatchMessage(0,
	 * sender, receiver, msg, extraInfo, needsReturnReceipt)}
	 * 
	 * @param sender the sender of the telegram
	 * @param receiver the receiver of the telegram; if it's {@code null} the telegram is broadcasted to all the receivers
	 *           registered for the specified message code
	 * @param msg the message code
	 * @param extraInfo an optional object
	 * @param needsReturnReceipt whether the return receipt is needed or not
	 * @throws IllegalArgumentException if the sender is {@code null} and the return receipt is needed */
	public void dispatchMessage (Telegraph sender, Telegraph receiver, int msg, Object extraInfo, boolean needsReturnReceipt) {
		dispatchMessage(0f, sender, receiver, msg, extraInfo, needsReturnReceipt);
	}

	/** 向所有注册的侦听器发送一个消息, 带有指定的延迟, 但没有额外的信息.<p>
	 * Sends a message to all registered listeners, with the specified delay but no extra info.
	 * <p>
	 * This is a shortcut method for {@link #dispatchMessage(float, Telegraph, Telegraph, int, Object, boolean)
	 * dispatchMessage(delay, null, null, msg, null, null)}
	 * 
	 * @param delay the delay in seconds
	 * @param msg the message code */
	public void dispatchMessage (float delay, int msg) {
		dispatchMessage(delay, null, null, msg, null, false);
	}

	/** 向所有注册的侦听器发送一个消息, 带有指定的延迟, 但没有额外的信息.<p>
	 * Sends a message to all registered listeners, with the specified delay but no extra info.
	 * <p>
	 * This is a shortcut method for {@link #dispatchMessage(float, Telegraph, Telegraph, int, Object, boolean)
	 * dispatchMessage(delay, sender, null, msg, null, false)}
	 * 
	 * @param delay the delay in seconds
	 * @param sender the sender of the telegram
	 * @param msg the message code */
	public void dispatchMessage (float delay, Telegraph sender, int msg) {
		dispatchMessage(delay, sender, null, msg, null, false);
	}

	/** 向所有注册的侦听器发送一个消息, 带有指定的延迟, 但没有额外的信息.<p>
	 * Sends a message to all registered listeners, with the specified delay but no extra info.
	 * <p>
	 * This is a shortcut method for {@link #dispatchMessage(float, Telegraph, Telegraph, int, Object, boolean)
	 * dispatchMessage(delay, sender, null, msg, null, needsReturnReceipt)}
	 * 
	 * @param delay the delay in seconds
	 * @param sender the sender of the telegram
	 * @param msg the message code
	 * @param needsReturnReceipt whether the return receipt is needed or not
	 * @throws IllegalArgumentException if the sender is {@code null} and the return receipt is needed */
	public void dispatchMessage (float delay, Telegraph sender, int msg, boolean needsReturnReceipt) {
		dispatchMessage(delay, sender, null, msg, null, needsReturnReceipt);
	}

	/** 发送一个消息给所有注册的侦听器, 带有指定的延迟和额外的信息.<p>
	 * Sends a message to all registered listeners, with the specified delay and extra info.
	 * <p>
	 * This is a shortcut method for {@link #dispatchMessage(float, Telegraph, Telegraph, int, Object, boolean)
	 * dispatchMessage(delay, null, null, msg, extraInfo, false)}
	 * 
	 * @param delay the delay in seconds
	 * @param msg the message code
	 * @param extraInfo an optional object */
	public void dispatchMessage (float delay, int msg, Object extraInfo) {
		dispatchMessage(delay, null, null, msg, extraInfo, false);
	}

	/** 发送一个消息给所有注册的侦听器, 带有指定的延迟和额外的信息.<p>
	 * Sends a message to all registered listeners, with the specified delay and extra info.
	 * <p>
	 * This is a shortcut method for {@link #dispatchMessage(float, Telegraph, Telegraph, int, Object, boolean)
	 * dispatchMessage(delay, sender, null, msg, extraInfo, false)}
	 * 
	 * @param delay the delay in seconds
	 * @param sender the sender of the telegram
	 * @param msg the message code
	 * @param extraInfo an optional object */
	public void dispatchMessage (float delay, Telegraph sender, int msg, Object extraInfo) {
		dispatchMessage(delay, sender, null, msg, extraInfo, false);
	}

	/** 发送一个消息给所有注册的侦听器, 带有指定的延迟和额外的信息.<p>
	 * Sends a message to all registered listeners, with the specified delay and extra info.
	 * <p>
	 * This is a shortcut method for {@link #dispatchMessage(float, Telegraph, Telegraph, int, Object, boolean)
	 * dispatchMessage(delay, sender, null, msg, extraInfo, needsReturnReceipt)}
	 * 
	 * @param delay the delay in seconds
	 * @param sender the sender of the telegram
	 * @param msg the message code
	 * @param extraInfo an optional object
	 * @param needsReturnReceipt whether the return receipt is needed or not
	 * @throws IllegalArgumentException if the sender is {@code null} and the return receipt is needed */
	public void dispatchMessage (float delay, Telegraph sender, int msg, Object extraInfo, boolean needsReturnReceipt) {
		dispatchMessage(delay, sender, null, msg, extraInfo, needsReturnReceipt);
	}

	/** 发送一个消息到指定的接收者, 使用指定的延迟, 但没有额外的信息. 接收方不需要是指定消息代码的注册侦听器.<p>
	 * Sends a message to the specified receiver, with the specified delay but no extra info. The receiver doesn't need to be a
	 * register listener for the specified message code.
	 * <p>
	 * This is a shortcut method for {@link #dispatchMessage(float, Telegraph, Telegraph, int, Object, boolean)
	 * dispatchMessage(delay, sender, receiver, msg, null, false)}
	 * 
	 * @param delay the delay in seconds
	 * @param sender the sender of the telegram
	 * @param receiver the receiver of the telegram; if it's {@code null} the telegram is broadcasted to all the receivers
	 *           registered for the specified message code
	 * @param msg the message code */
	public void dispatchMessage (float delay, Telegraph sender, Telegraph receiver, int msg) {
		dispatchMessage(delay, sender, receiver, msg, null, false);
	}

	/** 发送一个消息到指定的接收者, 使用指定的延迟, 但没有额外的信息. 接收方不需要是指定消息代码的注册侦听器.<p>
	 * Sends a message to the specified receiver, with the specified delay but no extra info. The receiver doesn't need to be a
	 * register listener for the specified message code.
	 * <p>
	 * This is a shortcut method for {@link #dispatchMessage(float, Telegraph, Telegraph, int, Object, boolean)
	 * dispatchMessage(delay, sender, receiver, msg, null, needsReturnReceipt)}
	 * 
	 * @param delay the delay in seconds
	 * @param sender the sender of the telegram
	 * @param receiver the receiver of the telegram; if it's {@code null} the telegram is broadcasted to all the receivers
	 *           registered for the specified message code
	 * @param msg the message code
	 * @param needsReturnReceipt whether the return receipt is needed or not
	 * @throws IllegalArgumentException if the sender is {@code null} and the return receipt is needed */
	public void dispatchMessage (float delay, Telegraph sender, Telegraph receiver, int msg, boolean needsReturnReceipt) {
		dispatchMessage(delay, sender, receiver, msg, null, needsReturnReceipt);
	}

	/** 发送一个消息到指定的接收者, 使用指定的延迟, 但没有额外的信息. 接收方不需要是指定消息代码的注册侦听器.<p>
	 * Sends a message to the specified receiver, with the specified delay but no extra info. The receiver doesn't need to be a
	 * register listener for the specified message code.
	 * <p>
	 * This is a shortcut method for {@link #dispatchMessage(float, Telegraph, Telegraph, int, Object, boolean)
	 * dispatchMessage(delay, sender, receiver, msg, extraInfo, false)}
	 * 
	 * @param delay the delay in seconds
	 * @param sender the sender of the telegram
	 * @param receiver the receiver of the telegram; if it's {@code null} the telegram is broadcasted to all the receivers
	 *           registered for the specified message code
	 * @param msg the message code
	 * @param extraInfo an optional object */
	public void dispatchMessage (float delay, Telegraph sender, Telegraph receiver, int msg, Object extraInfo) {
		dispatchMessage(delay, sender, receiver, msg, extraInfo, false);
	}

	/** 给定消息, 接收方, 发送方和任何时间延迟, 此方法将消息路由到正确的代理(如果没有延迟)或存储在消息队列中, 以便在正确的时间发送.<p>
	 * Given a message, a receiver, a sender and any time delay, this method routes the message to the correct agents (if no delay)
	 * or stores in the message queue to be dispatched at the correct time.
	 * @param delay the delay in seconds
	 * @param sender the sender of the telegram
	 * @param receiver the receiver of the telegram; if it's {@code null} the telegram is broadcasted to all the receivers
	 *           registered for the specified message code
	 * @param msg the message code
	 * @param extraInfo an optional object
	 * @param needsReturnReceipt whether the return receipt is needed or not
	 * @throws IllegalArgumentException if the sender is {@code null} and the return receipt is needed */
	public void dispatchMessage (float delay, Telegraph sender, Telegraph receiver, int msg, Object extraInfo,
		boolean needsReturnReceipt) {
		if (sender == null && needsReturnReceipt)
			throw new IllegalArgumentException("Sender cannot be null when a return receipt is needed");

		// Get a telegram from the pool
		Telegram telegram = POOL.obtain();
		telegram.sender = sender;
		telegram.receiver = receiver;
		telegram.message = msg;
		telegram.extraInfo = extraInfo;
		telegram.returnReceiptStatus = needsReturnReceipt ? Telegram.RETURN_RECEIPT_NEEDED : Telegram.RETURN_RECEIPT_UNNEEDED;

		// If there is no delay, route telegram immediately
		if (delay <= 0.0f) {

			// TODO: should we set the timestamp here?
			// telegram.setTimestamp(GdxAI.getTimepiece().getTime());

			if (debugEnabled) {
				float currentTime = GdxAI.getTimepiece().getTime();
				GdxAI.getLogger().info(
					LOG_TAG,
					"Instant telegram dispatched at time: " + currentTime + " by " + sender + " for " + receiver
						+ ". Message code is " + msg);
			}

			// Send the telegram to the recipient
			discharge(telegram);
		} else {
			float currentTime = GdxAI.getTimepiece().getTime();

			// Set the timestamp for the delayed telegram
			telegram.setTimestamp(currentTime + delay);

			// Put the telegram in the queue
			boolean added = queue.add(telegram);

			// Return it to the pool if has been rejected
			if (!added) POOL.free(telegram);

			if (debugEnabled) {
				if (added)
					GdxAI.getLogger().info(
						LOG_TAG,
						"Delayed telegram from " + sender + " for " + receiver + " recorded at time " + currentTime
							+ ". Message code is " + msg);
				else
					GdxAI.getLogger().info(LOG_TAG,
						"Delayed telegram from " + sender + " for " + receiver + " rejected by the queue. Message code is " + msg);
			}
		}
	}

	/** 使用已过期的时间戳发送任何延迟的消息. 已发送的消息从队列中删除.<p>
	 * Dispatches any delayed telegrams with a timestamp that has expired. Dispatched telegrams are removed from the queue.
	 * <p>
	 * This method must be called regularly from inside the main game loop to facilitate the correct and timely dispatch of any
	 * delayed messages. Notice that the message dispatcher internally calls {@link Timepiece#getTime()
	 * GdxAI.getTimepiece().getTime()} to get the current AI time and properly dispatch delayed messages. This means that
	 * <ul>
	 * <li>if you forget to {@link Timepiece#update(float) update the timepiece} the delayed messages won't be dispatched.</li>
	 * <li>ideally the timepiece should be updated before the message dispatcher.</li>
	 * </ul> */
	public void update () {
		float currentTime = GdxAI.getTimepiece().getTime();

		// Peek at the queue to see if any telegrams need dispatching.
		// Remove all telegrams from the front of the queue that have gone
		// past their time stamp.
		Telegram telegram;
		while ((telegram = queue.peek()) != null) {

			// Exit loop if the telegram is in the future
			if (telegram.getTimestamp() > currentTime) break;

			if (debugEnabled) {
				GdxAI.getLogger().info(LOG_TAG,
					"Queued telegram ready for dispatch: Sent to " + telegram.receiver + ". Message code is " + telegram.message);
			}

			// Send the telegram to the recipient
			discharge(telegram);

			// Remove it from the queue
			queue.poll();
		}

	}

	/** 扫描队列并以任何特定的顺序将挂起的消息传递给给定的回调.<p>
	 * Scans the queue and passes pending messages to the given callback in any particular order.
	 * <p>
	 * Typically this method is used to save (serialize) pending messages and restore (deserialize and schedule) them back on game
	 * loading.
	 * @param callback The callback used to report pending messages individually. **/
	public void scanQueue (PendingMessageCallback callback) {
		float currentTime = GdxAI.getTimepiece().getTime();
		int queueSize = queue.size();
		for (int i = 0; i < queueSize; i++) {
			Telegram telegram = queue.get(i);
			callback.report(telegram.getTimestamp() - currentTime, telegram.sender, telegram.receiver, telegram.message,
				telegram.extraInfo, telegram.returnReceiptStatus);
		}
	}

	/** This method is used by {@link #dispatchMessage(float, Telegraph, Telegraph, int, Object) dispatchMessage} for immediate
	 * telegrams and {@link #update(float) update} for delayed telegrams. It first calls the message handling method of the
	 * receiving agents with the specified telegram then returns the telegram to the pool.
	 * @param telegram the telegram to discharge */
	private void discharge (Telegram telegram) {
		if (telegram.receiver != null) {
			// Dispatch the telegram to the receiver specified by the telegram itself
			if (!telegram.receiver.handleMessage(telegram)) {
				// Telegram could not be handled
				if (debugEnabled) GdxAI.getLogger().info(LOG_TAG, "Message " + telegram.message + " not handled");
			}
		} else {
			// Dispatch the telegram to all the registered receivers
			int handledCount = 0;
			Array<Telegraph> listeners = msgListeners.get(telegram.message);
			if (listeners != null) {
				for (int i = 0; i < listeners.size; i++) {
					if (listeners.get(i).handleMessage(telegram)) {
						handledCount++;
					}
				}
			}
			// Telegram could not be handled
			if (debugEnabled && handledCount == 0) GdxAI.getLogger().info(LOG_TAG, "Message " + telegram.message + " not handled");
		}

		if (telegram.returnReceiptStatus == Telegram.RETURN_RECEIPT_NEEDED) {
			// Use this telegram to send the return receipt
			telegram.receiver = telegram.sender;
			telegram.sender = this;
			telegram.returnReceiptStatus = Telegram.RETURN_RECEIPT_SENT;
			discharge(telegram);
		} else {
			// Release the telegram to the pool
			POOL.free(telegram);
		}
	}

	/** Handles the telegram just received. This method always returns {@code false} since usually the message dispatcher never
	 * receives telegrams. Actually, the message dispatcher implements {@link Telegraph} just because it can send return receipts.
	 * @param msg The telegram
	 * @return always {@code false}. */
	@Override
	public boolean handleMessage (Telegram msg) {
		return false;
	}

	/** A {@code PendingMessageCallback} is used by the {@link MessageDispatcher#scanQueue(PendingMessageCallback) scanQueue} method
	 * of the {@link MessageDispatcher} to report its pending messages individually.
	 * 
	 * @author davebaol */
	public interface PendingMessageCallback {

		/** Reports a pending message.
		 * @param delay The remaining delay in seconds
		 * @param sender The message sender
		 * @param receiver The message receiver
		 * @param message The message code
		 * @param extraInfo Any additional information that may accompany the message
		 * @param returnReceiptStatus The return receipt status of the message */
		public void report (float delay, Telegraph sender, Telegraph receiver, int message, Object extraInfo,
			int returnReceiptStatus);
	}

}
