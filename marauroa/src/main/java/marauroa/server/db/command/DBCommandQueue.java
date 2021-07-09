/***************************************************************************
 *                   (C) Copyright 2009-2020 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.server.db.command;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

import marauroa.common.i18n.I18N;

/**
 * An asynchronous command queue.
 *
 * @author hendrik, madmetzger
 */
public final class DBCommandQueue {
	private static DBCommandQueue instance;

	private PriorityBlockingQueue<DBCommandMetaData> pendingCommands = new PriorityBlockingQueue<DBCommandMetaData>();
	private List<DBCommandMetaData> processedCommands = Collections.synchronizedList(new LinkedList<DBCommandMetaData>());

	private boolean finished;

	/**
	 * gets the singleton instance
	 *
	 * @return DBCommandQueue
	 */
	public static DBCommandQueue get() {
		if (instance == null) {
			instance = new DBCommandQueue();
		}
		return instance;
	}

	/**
	 * createsa a new DBCommandQueue
	 */
	private DBCommandQueue() {
		Thread thread = new Thread(new DBCommandQueueBackgroundThread(), "DBCommandQueue");
		thread.start();
	}

	/**
	 * enqueues a "fire and forget" command.
	 *
	 * @param command DBCommand to add to the queue
	 */
	public void enqueue(DBCommand command) {
		enqueue(command, DBCommandPriority.CRITICAL);
	}

	/**
	 * enqueues a "fire and forget" command.
	 *
	 * @param command DBCommand to add to the queue
	 * @param priority DBCommandPriority
	 */
	public void enqueue(DBCommand command, DBCommandPriority priority) {
		pendingCommands.add(new DBCommandMetaData(command, null, Thread.currentThread(), false, I18N.getLocale(), priority));
	}

	/**
	 * enqueues a command and remembers the result.
	 *
	 * @param command DBCommand to add to the queue
	 * @param handle ResultHandle
	 */
	public void enqueueAndAwaitResult(DBCommand command, ResultHandle handle) {
		enqueueAndAwaitResult(command, DBCommandPriority.CRITICAL, handle);
	}

	/**
	 * enqueues a command and remembers the result.
	 *
	 * @param command DBCommand to add to the queue
	 * @param priority DBCommandPriority
	 * @param handle ResultHandle
	 */
	public void enqueueAndAwaitResult(DBCommand command, DBCommandPriority priority, ResultHandle handle) {
		pendingCommands.add(new DBCommandMetaData(command, handle, Thread.currentThread(), true, I18N.getLocale(), priority));
	}

	/**
	 * gets the next command in the queue.
	 *
	 * @return next command or <code>null</code>
	 * @throws InterruptedException in case the waiting was interrupted
	 */
	protected DBCommandMetaData getNextCommand() throws InterruptedException {
		return pendingCommands.poll(1, TimeUnit.SECONDS);
	}

	/**
	 * adds a result to be fetched later
	 *
	 * @param metaData a processed DBCommandMetaData
	 */
	protected void addResult(DBCommandMetaData metaData) {
		processedCommands.add(metaData);
	}

	/**
	 * gets the processed results of the specified DBCommand class that have
	 * been requested in the current thread.
	 *
	 * @param <T> the type of the DBCommand
	 * @param clazz the type of the DBCommand
	 * @param handle a handle to the expected results
	 * @return a list of processed DBCommands; it may be empty
	 */
	@SuppressWarnings("unchecked")
	public <T extends DBCommand> List<T> getResults(Class<T> clazz, ResultHandle handle) {
		LinkedList<T> res = new LinkedList<T>();

		synchronized(processedCommands) {
			Iterator<DBCommandMetaData> itr = processedCommands.iterator();
			while (itr.hasNext()) {
				DBCommandMetaData metaData = itr.next();
				DBCommand command = metaData.getCommand();
				if (clazz.isAssignableFrom(command.getClass())) {
					if (metaData.getResultHandle() == handle) {
						res.add((T) command);
						itr.remove();
					}
				}
			}
		}
		return res;
	}


	/**
	 * gets one processed result of the specified DBCommand class that have
	 * been requested in the current thread.
	 *
	 * @param <T> the type of the DBCommand
	 * @param clazz the type of the DBCommand
	 * @param handle a handle to the expected results
	 * @return a list of processed DBCommands; it may be empty
	 */
	@SuppressWarnings("unchecked")
	public <T extends DBCommand> T getOneResult(Class<T> clazz, ResultHandle handle) {
		synchronized(processedCommands) {
			Iterator<DBCommandMetaData> itr = processedCommands.iterator();
			while (itr.hasNext()) {
				DBCommandMetaData metaData = itr.next();
				DBCommand command = metaData.getCommand();
				if (clazz.isAssignableFrom(command.getClass())) {
					if (metaData.getResultHandle() == handle) {
						itr.remove();
						return (T) command;
					}
				}
			}
		}
		return null;
	}


	/**
	 * shuts the background thread down.
	 */
	public void finish() {
		finished = true;
	}

	/**
	 * should the background set be terminated?
	 *
	 * @return true, if the background thread should be terminated, false if it should continue.
	 */
	protected boolean isFinished() {
		return finished;
	}

	/**
	 * returns the size of the queue
	 *
	 * @return size of queue
	 */
	public int size() {
		return pendingCommands.size();
	}

	/**
	 * gets the timestamp of the oldest pending command
	 *
	 * @return Timestamp or <code>null</code>
	 */
	protected Timestamp getOldestEnqueueTimestamp() {
		DBCommandMetaData oldest = pendingCommands.peek();
		if (oldest == null) {
			return null;
		}
		return oldest.getCommand().getEnqueueTime();
	}

	/**
	 * returns a frozen array of the complete queue content
	 *
	 * @return array of the queue content
	 */
	protected DBCommandMetaData[] dumpQueue() {
		// Make the array a little larger than required, to make it less likely that toArray 
		// needs to create a completely new array because the size of the queue was increased
		// in the mean time
		DBCommandMetaData[] res = new DBCommandMetaData[pendingCommands.size() + 2];
		return pendingCommands.toArray(res);
	}
}
