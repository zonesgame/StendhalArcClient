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

import arc.util.pooling.Pool;

/**
 *  消息<p>
 * A Telegram is the container of a message. The {@link MessageDispatcher} manages telegram life-cycle.
 * @author davebaol */
public class Telegram implements Comparable<Telegram>, Pool.Poolable {

	/** 指示发送方不需要任何回执.<p>Indicates that the sender doesn't need any return receipt */
	public static final int RETURN_RECEIPT_UNNEEDED = 0;

	/** 指示发送方需要回执.<p>Indicates that the sender needs the return receipt */
	public static final int RETURN_RECEIPT_NEEDED = 1;

	/** 表示回执已发回电报的原始发件人.<p>Indicates that the return receipt has been sent back to the original sender of the telegram */
	public static final int RETURN_RECEIPT_SENT = 2;

	/** 消息发送者.<p>The agent that sent this telegram */
	public Telegraph sender;

	/** 消息接收者.<p>The agent that is to receive this telegram */
	public Telegraph receiver;

	/** 消息类型.<p>The message type. */
	public int message;

	/** 返回接收状态.<p>The return receipt status of this telegram. Its value should be {@link #RETURN_RECEIPT_UNNEEDED}, {@link #RETURN_RECEIPT_NEEDED} or
	 * {@link #RETURN_RECEIPT_SENT}. */
	public int returnReceiptStatus;

	/** 消息发送延时.<p>Messages can be dispatched immediately or delayed for a specified amount of time. If a delay is necessary, this field is
	 * stamped with the time the message should be dispatched. */
	private float timestamp;

	/** 消息扩展信息.<p>Any additional information that may accompany the message */
	public Object extraInfo;

	/** 创建一个空消息.<p>Creates an empty {@code Telegram}. */
	public Telegram () {
	}

	/** 消息发送延时.<p>Returns the time stamp of this telegram. */
	public float getTimestamp () {
		return timestamp;
	}

	/** 设置消息发送延时.<p>Sets the time stamp of this telegram. */
	public void setTimestamp (float timestamp) {
		this.timestamp = timestamp;
	}

	/** 数据重置*/
	@Override
	public void reset () {
		this.sender = null;
		this.receiver = null;
		this.message = 0;
		this.returnReceiptStatus = RETURN_RECEIPT_UNNEEDED;
		this.extraInfo = null;
		this.timestamp = 0;
	}

	/** 排序比对*/
	@Override
	public int compareTo (Telegram other) {
		if (this.equals(other)) return 0;
		return (this.timestamp - other.timestamp < 0) ? -1 : 1;
	}

	@Override
	public int hashCode () {
		final int prime = 31;
		int result = 1;
		result = prime * result + message;
		result = prime * result + ((receiver == null) ? 0 : receiver.hashCode());
		result = prime * result + ((sender == null) ? 0 : sender.hashCode());
		result = prime * result + Float.floatToIntBits(timestamp);
		return result;
	}

	@Override
	public boolean equals (Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Telegram other = (Telegram)obj;
		if (message != other.message) return false;
		if (Float.floatToIntBits(timestamp) != Float.floatToIntBits(other.timestamp)) return false;
		if (sender == null) {
			if (other.sender != null) return false;
		} else if (!sender.equals(other.sender)) return false;
		if (receiver == null) {
			if (other.receiver != null) return false;
		} else if (!receiver.equals(other.receiver)) return false;
		return true;
	}

}
