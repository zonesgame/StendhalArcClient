/***************************************************************************
 *                   (C) Copyright 2003-2020 - Marauroa                    *
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
import java.util.LinkedList;
import java.util.List;

import marauroa.common.net.Channel;
import marauroa.common.net.message.MessageS2CLoginNACK;
import marauroa.server.db.DBTransaction;
import marauroa.server.game.container.SecuredLoginInfo;
import marauroa.server.game.db.AccountDAO;
import marauroa.server.game.db.DAORegister;
import marauroa.server.game.db.LoginEventDAO;
import marauroa.server.game.messagehandler.DelayedEventHandler;


/**
 * verifies the login using the database
 */
public class LoginCommand extends DBCommandWithCallback {
	private SecuredLoginInfo info;
	private MessageS2CLoginNACK.Reasons failReason = null;
	private String failMessage = null;
	private List<String> previousLogins;


	/**
	 * creates a new LoginCommand
	 *
	 * @param info SecuredLoginInfo
	 */
	public LoginCommand(SecuredLoginInfo info) {
		this.info = info;
	}

	/**
	 * creates a new LoginCommand.
	 *
	 * @param info SecuredLoginInfo
	 * @param callback DelayedEventHandler
	 * @param clientid optional parameter available to the callback
	 * @param channel optional parameter available to the callback
	 * @param protocolVersion protocolVersion
	 */
	public LoginCommand(SecuredLoginInfo info, DelayedEventHandler callback, int clientid,
			Channel channel, int protocolVersion) {
		super(callback, clientid, channel, protocolVersion);
		this.info = info;
	}
	
	@Override
	public void execute(DBTransaction transaction) throws SQLException, IOException {
		if (!blockCheck(transaction)) {
			return;
		}
		
		if (!credentialsCheck(transaction)) {
			return;
		}
		if (!accountStatusCheck(transaction)) {
			return;
		}
		processSuccessfulLogin(transaction);
	}

	protected boolean blockCheck(DBTransaction transaction) throws SQLException {
		if (info.isBlocked(transaction)) {
			failReason = MessageS2CLoginNACK.Reasons.TOO_MANY_TRIES;
			info.addLoginEvent(transaction, info.address, 4, this.getEnqueueTime());
			return false;
		}
		return true;
	}

	/**
	 * verifies credentials against the database
	 *
	 * @param transaction DBTransaction
	 * @throws SQLException in case of a database error
	 * @throws IOException in case of an input/output error
	 * @return verification result
	 */
	protected boolean verify(DBTransaction transaction) throws SQLException, IOException {
		return DAORegister.get().get(AccountDAO.class).verify(transaction, info);
	}

	protected boolean credentialsCheck(DBTransaction transaction) throws SQLException, IOException {
		boolean verified = verify(transaction);
		if (!verified) {
			if (info.reason == null) {
				info.reason = MessageS2CLoginNACK.Reasons.USERNAME_WRONG;
			}
			failReason = info.reason;
			info.addLoginEvent(transaction, info.address, 0, this.getEnqueueTime());
			return false;
		}
		return true;
	}

	protected boolean accountStatusCheck(DBTransaction transaction) throws SQLException {
		String accountStatusMessage = DAORegister.get().get(AccountDAO.class).getAccountBanMessage(transaction, info.username);
		if (accountStatusMessage != null) {
			String status = DAORegister.get().get(AccountDAO.class).getAccountStatus(transaction, info.username);
			if (status == null) {
				// oops
			} else if (status.equals("banned")) {
				info.addLoginEvent(transaction, info.address, 2, this.getEnqueueTime());
			} else if (status.equals("inactive")) {
				info.addLoginEvent(transaction, info.address, 3, this.getEnqueueTime());
			} else if (status.equals("merged")) {
				info.addLoginEvent(transaction, info.address, 5, this.getEnqueueTime());
			}
			failMessage = accountStatusMessage;
			return false;
		}
		return true;
	}

	protected void processSuccessfulLogin(DBTransaction transaction) throws SQLException {
		/* Successful login */
		previousLogins = DAORegister.get().get(LoginEventDAO.class).getLoginEvents(transaction, info.username, 1);
		info.addLoginEvent(transaction, info.address, 1, this.getEnqueueTime());
	}

	/**
	 * gets the SecuredLoginInfo object
	 *
	 * @return SecuredLoginInfo
	 */
	public SecuredLoginInfo getInfo() {
		return info;
	}

	/**
	 * gets the Reason enum if the login failed
	 * @return MessageS2CLoginNACK.Reasons or <code>null</code>
	 * in case the login did not fail (was succesful).
	 */
	public MessageS2CLoginNACK.Reasons getFailReason() {
		return failReason;
	}

	/**
	 * gets the message if the login failed
	 *
	 * @return error message or <code>null</code>
	 * in case the login did not fail (was succesful).
	 */
	public String getFailMessage() {
		return failMessage;
	}

	/**
	 * gets a list of previous logins so that the player can
	 * notice possible account hacks.
	 *
	 * @return list of last logins
	 */
	public List<String> getPreviousLogins() {
		return new LinkedList<String>(previousLogins);
	}

	/**
	 * returns a string suitable for debug output of this DBCommand.
	 *
	 * @return debug string
	 */
	@Override
	public String toString() {
		return "LoginCommand [info=" + info + ", failReason=" + failReason
				+ ", failMessage=" + failMessage + ", previousLogins="
				+ previousLogins + "]";
	}
}
