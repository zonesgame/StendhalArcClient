/***************************************************************************
 *                   (C) Copyright 2003-2013 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.BoxView;
import javax.swing.text.Caret;
import javax.swing.text.ComponentView;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

import arc.Core;
import arc.files.Fi;
import arc.func.Boolp;
import arc.func.Cons;
import arc.graphics.Color;
import arc.scene.Group;
import arc.scene.ui.Label;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Table;
import arc.util.Align;
import arc.util.io.Streams;
import games.stendhal.client.stendhal;
import games.stendhal.client.gui.chatlog.ChatTextSink;
import games.stendhal.client.gui.chatlog.EventLine;
import games.stendhal.client.gui.chatlog.HeaderLessEventLine;
import games.stendhal.client.gui.textformat.StringFormatter;
import games.stendhal.client.gui.textformat.StyleSet;
import games.stendhal.common.MathHelper;
import games.stendhal.common.NotificationType;
import marauroa.common.Logger;
import mindustry.Vars;
import mindustry.graphics.Pal;
import mindustry.ui.dialogs.FloatingDialog;
import stendhal.test.MessageArray;

/**
 * Appendable text component to be used as the chat log.
 */
public class KTextEdit {
    /**
     * Color of the time stamp written before the lines.
     */
    protected static final Color HEADER_COLOR = Color.gray;

    private static final Logger logger = Logger.getLogger(KTextEdit.class);

    /**
     * Name of the log.
     */
    private String name = "";
    /**
     * Background color when not highlighting unread messages.
     */
    private Color defaultBackground = Color.white;
    /**
     * Formatting class for text containing stendhal markup.
     */
    private final StringFormatter<Style, StyleSet> formatter = new StringFormatter<Style, StyleSet>();
    private final Format dateFormatter = new SimpleDateFormat("[HH:mm] ");
    private final Format saveDateFormatter = new SimpleDateFormat("[yyy-MM-dd HH:mm:ss] ");

    /**
     * The actual text component for showing the chat log.
     */
//	JTextPane textPane;
    public float starty;
    private boolean saveing = false;
    private MessageArray<String> messageArray = new MessageArray(true);

    private Table root;
    private ScrollPane pane;
    private Label chatHistory;

    public void build(Table parent) {
        root = new Table() {
            @Override
            public void act(float delta) {
                super.act(delta);
//                System.out.println(root.isVisible());
            }
        };
//        root.fill().left().top().margin(100);
        root.clearChildren();
        parent.add(root);

        chatHistory = new Label("test");
        chatHistory.setWrap(true);
//        chatHistory.setAlignment(Align.topLeft);

        pane = new ScrollPane(chatHistory);
        pane.setFadeScrollBars(false);
        pane.setScrollingDisabled(true, false);

//        pane.setSize(200, 200);
//        pane.setPosition(200, 100);
//        root.addImage(Vars.atlasS.find("StendhalSplash")).size(999, 999);
//        root.setFillParent(true);
//        root.fill();

        root.add(pane).width(300).height(200).top().left();
    }

    public void setVisible(Boolp visible) {
        root.visible(visible);
    }

//	public void show(Group parent){
//		FloatingDialog dialog = new FloatingDialog("$credits");
//		dialog.addCloseButton();
//		dialog.cont.add("$credits.text").fillX().wrap().get().setAlignment(Align.center);
//		dialog.cont.row();
//		if(!contributors.isEmpty()){
//			dialog.cont.addImage().color(Pal.accent).fillX().height(3f).pad(3f);
//			dialog.cont.row();
//			dialog.cont.add("$contributors");
//			dialog.cont.row();
//			dialog.cont.pane(new Table(){{
//				int i = 0;
//				left();
//				for(String c : contributors){
//					add("[lightgray]" + c).left().pad(3).padLeft(6).padRight(6);
//					if(++i % 3 == 0){
//						row();
//					}
//				}
//			}});
//		}
//		dialog.show();
//	}

//	/** Listener for opening the popup menu when it's requested. */
//	private final class TextPaneMouseListener extends MousePopupAdapter {
//		@Override
//		protected void showPopup(final MouseEvent e) {
//			final JPopupMenu popup = new JPopupMenu("save");
//
//			JMenuItem menuItem = new JMenuItem("Save");
//			menuItem.addActionListener(new ActionListener() {
//				@Override
//				public void actionPerformed(final ActionEvent e) {
//					save();
//				}
//			});
//			popup.add(menuItem);
//
//			menuItem = new JMenuItem("Clear");
//			menuItem.addActionListener(new ActionListener() {
//				@Override
//				public void actionPerformed(final ActionEvent e) {
//					clear();
//				}
//			});
//			popup.add(menuItem);
//
//			popup.show(e.getComponent(), e.getX(), e.getY());
//		}
//
//
//	}

    /**
     * Basic Constructor.
     */
    public KTextEdit(String name, Table parent) {
//        buildGUI();
        this.name = name;
        build(parent);
    }


    /**
     * Add a new line with a specified header and content. The style will be
     * chosen according to the type of the message.
     *
     * @param header a string with the header
     * @param line   a string representing the line to be printed
     * @param type   The logical format type.
     */
    private void addLine(final String header, final String line,
                         final NotificationType type) {
        // do the whole thing in the event dispatch thread to ensure the generated
        // events get handled in the correct order
        try {
            if (SwingUtilities.isEventDispatchThread()) {
                handleAddLine(header, line, type);
            } else {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        handleAddLine(header, line, type);
                    }
                });
            }
        } catch (final RuntimeException e) {
            logger.error(e, e);
        }
    }

    /**
     * Add a new line with a specified header and content. The style will be
     * chosen according to the type of the message. Keep the view at the last
     * line unless the user has scrolled higher.
     *
     * @param header a string with the header
     * @param line   a string representing the line to be printed
     * @param type   The logical format type.
     */
    private void handleAddLine(final String header, final String line, final NotificationType type) {
        String dateString = dateFormatter.format(new Date());

        StringBuilder sb = new StringBuilder();
        sb.append(dateString);
        if (header.length() > 0) {
            sb.append("[YELLOW]<");
            sb.append(header);
            sb.append(">[]");
        }
        sb.append(type.getColor());        // 添加信息颜色
        sb.append(line);
        sb.append("[]");        // 关闭信息颜色

        StringBuilder sbSave = new StringBuilder();
        sbSave.append(dateString);
        if (header.length() > 0) {
            sbSave.append("<");
            sbSave.append(header);
            sbSave.append(">");
        }
        sbSave.append(line);

        messageArray.add(sb.toString(), sbSave.toString());

        chatHistory.setText(messageArray.toString(false));
//        ScrollPane scrollText = group.findActor(script.PANE);
//        scrollToAction.setActor(scrollText);
//        if ( !(scrollText.isDragging() || scrollText.isPanning() || scrollText.isFlinging()) ) {
//            if (scrollText.hasActions())
//                return;
//            scrollText.addAction(Actions.sequence(Actions.delay(0.1f), scrollToAction));
////            scrollText.setScrollY(0);   // 向上归零
////            scrollText.scrollTo(0, 0, 0, 0);        // 向下归零
//
////            scrollText.scrollTo(0, 0, 0, 0);
////            System.out.println(scrollText.getMaxY() + "    " + scrollText.getHeight() + "    "+ scrollText.getMinHeight() + "     " + ""+ "    " + scrollText.getVisualScrollY());
//        }
    }


    /**
     * Append an event line.
     *
     * @param line event line
     */
    void addLine(final EventLine line) {
        this.addLine(line.getHeader(), line.getText(), line.getType());
    }

    /**
     * Clear the context.
     */
    void clear() {
        messageArray.clear();
        chatHistory.setText("");
    }

    /**
     * Set the background color to be used normally, when not highlighting
     * unread messages.
     *
     * @param color background color
     */
    public void setDefaultBackground(Color color) {
        defaultBackground = color;
    }

    /**
     * Set the name of the logged channel.
     *
     * @param name channel name
     */
    void setChannelName(String name) {
        this.name = name;
    }

    /**
     * Get name of the file where logs should be saved on request.
     * @return file name
     */
    private Fi getSaveFileName() {
        String dateString = saveDateFormatter.format(new Date());
        dateString = dateString.replaceAll(":","-");
        dateString = dateString.replace('[', ']');
        dateString = dateString.replaceAll("]","");
        if ("".equals(name)) {
            return Vars.saveDirectory.child("chat\\gamechat" + "_" + dateString + ".log");
        } else {
            return Vars.saveDirectory.child("chat\\gamechat-" + name + "_" + dateString + ".log");
        }
    }

    /**
     * Save the contents into the log file and inform the user about it.
     */
    private void save() {
        saveing = true;
        Core.app.post(() -> {
            Fi saveFile = getSaveFileName();
            BufferedWriter out = null;
            String message = null;
            try {
                out = new BufferedWriter(new OutputStreamWriter(saveFile.write(false), "UTF-8"));
                try {
                    out.write(messageArray.toString(true, true));
                } finally {
                    Streams.close(out);
                }

                addLine("", "Chat log has been saved to " + saveFile.path(), NotificationType.CLIENT);
            } catch (final IOException ex) {
                logger.error(ex, ex);
            }
            saveing = false;
        });
    }

}
