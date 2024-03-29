/***************************************************************************
 *                   (C) Copyright 2003-2018 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.server.game.dbcommand;

import java.io.IOException;
import java.sql.SQLException;

import marauroa.common.Logger;
import marauroa.common.game.RPObject;
import marauroa.server.db.DBTransaction;
import marauroa.server.db.command.AbstractDBCommand;
import marauroa.server.db.command.DBCommandPriority;
import marauroa.server.db.command.DBCommandQueue;
import marauroa.server.game.db.CharacterDAO;
import marauroa.server.game.db.DAORegister;

/**
 * Asynchronously stores a character's progress.
 *
 * @author hendrik
 */
public class StoreCharacterCommand extends AbstractDBCommand {
	private Logger logger = Logger.getLogger(StoreCharacterCommand.class);

	private final String username;
	private final String character;
	private final RPObject frozenObject;

	/**
	 * Asynchronously stores a character's progress
	 *
	 * @param username  username
	 * @param character charactername
	 * @param object    character object
	 */
	public StoreCharacterCommand(String username, String character, RPObject object) {
		this.username = username;
		this.character = character;
		this.frozenObject = (RPObject) object.clone();
	}

	@Override
	public void execute(DBTransaction transaction) throws SQLException, IOException {
		try {
			DAORegister.get().get(CharacterDAO.class).storeCharacter(transaction, username, character, frozenObject, getEnqueueTime());
		} catch (SQLException e) {
			if (!transaction.isConnectionError(e)) {
				handleStorageFailure();
			}
			throw e;
		} catch (IOException e) {
			handleStorageFailure();
			throw e;
		} catch (RuntimeException e) {
			handleStorageFailure();
			throw e;
		}
	}

	/**
	 * handles a storage failure by disabling the character
	 *
	 * @throws IOException in case of an input/output error
	 * @throws SQLException in case of an database error
	 */
	private void handleStorageFailure() throws SQLException, IOException {
		logger.error("Disabling character " + character + " because of storage error.");

		// use a dedicated transaction because our original transaction is flagged 
		// as broken and will be rolled back as we are handling a DB error. It still may
		// own locks at this time, so we cannot open a new connection right away.
		DBCommandQueue.get().enqueue(new SetCharacterStatusCommand(username, character, "inactive"), DBCommandPriority.CRITICAL);
	}

	/**
	 * returns a string suitable for debug output of this DBCommand.
	 *
	 * @return debug string
	 */
	@Override
	public String toString() {
		return "StoreCharacterCommand [username=" + username + ", character=" + character + "]";
	}

}
