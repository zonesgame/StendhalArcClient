/***************************************************************************
 *                (C) Copyright 2003-2018 - Faiumoni e.V.                  *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package stendhal.test;

import java.awt.Component;
import java.awt.Frame;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.JFrame;

import games.stendhal.client.StendhalClient;
import games.stendhal.client.entity.User;
import games.stendhal.client.gui.J2DClientGUI;
import games.stendhal.client.gui.OutfitColor;
import games.stendhal.client.listener.PositionChangeListener;
import games.stendhal.common.NotificationType;
import marauroa.common.game.RPObject;
import temp.Debug;

public class ArcClientGUI {

    private boolean offline;
    private User user;

    public ArcClientGUI() {
    }


    public void requestQuit(StendhalClient client) {
        if (client.getConnectionState() || !offline) {
            if (Debug.NOTE2)
                ;
//            quitDialog.requestQuit(user);
        } else {
            System.exit(0);
        }
    }

    public boolean isOffline() {
        return offline;
    }

    public void setOffline(final boolean offline) {
        this.offline = offline;
    }

    public void updateUser(User user) {
        this.user = user;
    }

    public void chooseOutfit() {
        if (Debug.NOTE2)
            ;
    }

    public void resetClientDimensions() {
        if (Debug.NOTE2)
            ;
    }

    public void setChatLine(String text) {
        if (Debug.NOTE2)
            ;
    }

    public Collection<PositionChangeListener> getPositionChangeListeners() {
        return Arrays.asList();
    }


    // temp

    public void afterPainting() { }
    public void beforePainting() { }
    /**
     * Requests repaint at the window areas that are painted according to the
     * game loop frame rate.
     */
    public void triggerPainting() { }
    public JFrame getFrame() { return null; }
    public void switchToSpellState(RPObject spell) { }

}
