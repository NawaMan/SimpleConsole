/*----------------------------------------------------------------------------------------------------------------------
 * Copyright (C) 2008-2019 Nawapunth Manusitthipol. Implements with and for Sun Java 1.6 JDK.
 *----------------------------------------------------------------------------------------------------------------------
 * LICENSE:
 * 
 * This file is part of Nawa's SimpleConsole.
 * 
 * The project is a free software; you can redistribute it and/or modify it under the SIMILAR terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or any later version.
 * You are only required to inform me about your modification and redistribution as or as part of commercial software
 * package. You can inform me via nawaman<at>gmail<dot>com.
 * 
 * The project is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the 
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 * ---------------------------------------------------------------------------------------------------------------------
 */

package net.nawaman.console;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.PrintStream;
import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.text.JTextComponent;

import net.nawaman.swing.DisplayBar;
import net.nawaman.swing.FixedPanel;

/**
 *
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public class ConsolePanel extends FixedPanel {
	
    private static final long serialVersionUID = 1408809081792089557L;
    
    /** Flush Action ID */
	static public final int    FlushActionID   =     100;
	/** Flush Action Name */
	static public final String FlushActionName = "Flush";
	
	ConsoleOutputPanel OutputPanel    = null;
	ConsoleInputPanel  InputPanel     = null;
	DisplayBar         SeparatorPanel = null;
	
	JPanel      InnerPanel       = null;
	JScrollPane InnerScrollPanel = null;
	
	boolean IsJustFlush = false; 
	
	public ConsolePanel() { this(null, null, null); }
	
	public ConsolePanel(JComponent LeftSeparate, JComponent CenterSeparate, JComponent RightSeparate) {
		
		// Display result of the console
		this.OutputPanel = new ConsoleOutputPanel(this);
		this.OutputPanel.setName(ConsoleLayout.OutputName);
		// Retrieve the input text
		this.InputPanel = new ConsoleInputPanel(this);
		this.InputPanel.setName(ConsoleLayout.InputName);

		// Display interesting information
		this.SeparatorPanel = new DisplayBar(LeftSeparate, CenterSeparate, RightSeparate);
		this.SeparatorPanel.setName(ConsoleLayout.SeparatorName);
		
		this.InnerPanel = new JPanel();
		this.InnerPanel.setLayout(new ConsoleLayout());
		this.InnerPanel.add(this.OutputPanel);
		this.InnerPanel.add(this.SeparatorPanel);
		this.InnerPanel.add(this.InputPanel);

		this.InnerScrollPanel = new JScrollPane(this.InnerPanel);
		this.InnerScrollPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		this.InnerScrollPanel.setVerticalScrollBarPolicy(  JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		this.InnerScrollPanel.getHorizontalScrollBar().setUnitIncrement(10);
		this.InnerScrollPanel.getVerticalScrollBar()  .setUnitIncrement(10);

		this.setLayout(new BorderLayout());
		this.add(this.InnerScrollPanel, BorderLayout.CENTER);
		
		// Force layout (Firstly, force the Bottom box to resize properly)
		//this.InputPanel.setText(" "); this.InputPanel.setText("");
		this.InnerPanel.getLayout().layoutContainer(InnerPanel);
		
		// Set the focus
		this.InputPanel.getTextComponent().grabFocus();
		
		// Set to fixed
		this.setFixed(true);
	}
	
	/** Returns the top text component */
	protected JTextComponent getTop() {
		return this.OutputPanel.getTextComponent();
	}
	/** Returns the bottom text component */
	public JTextComponent getBottom() {
		return this.InputPanel.getTextComponent();
	}
		
	/** Returns the output print stream for the console */
	public PrintStream getOut() {
		return this.OutputPanel.getOut();
	}
	/** Returns the error print stream for the console */
	public PrintStream getErr() {
		return this.OutputPanel.getErr();
	}
	
	/** Returns the current text of the bottom text component */
	public String getText() {
		return this.InputPanel.getText();
	}
	
	Vector<ActionListener> ActionListeners = null;
	boolean IsBeingFlush = false;
	
	/** This is for Subclass to get Notified */
	protected void notifiedFlush(String pText) {}
	
	/** Flush the current text (from the bottom TextComponent), Returns the text and remove from the component */
	final public String flush() {
		// Ensure that other flush that occur during the current flush (perhaps by the listeners) will not interfere or
		//     or cause recursive
		if(this.IsBeingFlush) return this.InputPanel.getText();
		
		String Text = null;
		this.IsBeingFlush = true;
		this.IsJustFlush  = false;
		try {
			Text = this.InputPanel.getText();
			// Notify ourself
			this.notifiedFlush(Text);
			// Notify the listeners
			if(this.ActionListeners != null) {
				ActionEvent AE = new ActionEvent(this, FlushActionID, FlushActionName);
				for(ActionListener AL : this.ActionListeners) AL.actionPerformed(AE);
			}
			this.InputPanel.setText("");
		} finally {
			this.IsBeingFlush = false;
			this.IsJustFlush  = true;
		}
		
		// Update UI
		this.InnerPanel.getLayout().layoutContainer(this.InnerPanel);
		this.repaint();
		return Text;
	}
	

    /** Invoked when a key has been pressed. */
	protected void processKeyPressed(Object pSource, KeyEvent Evt) {
		JTextComponent TC;
		if(     pSource instanceof ConsoleInputPanel)  TC = ((ConsoleInputPanel)pSource) .getTextComponent();
		else if(pSource instanceof ConsoleOutputPanel) TC = ((ConsoleOutputPanel)pSource).getTextComponent();
		else return;
		
		// Scroll the whole area 
		if(!Evt.isAltDown() && Evt.isControlDown() && !Evt.isShiftDown() && (Evt.getKeyCode() != 0)) {

			// Flush the command
			if((pSource == this.InputPanel) && (Evt.getKeyCode() == KeyEvent.VK_ENTER)) {
				this.flush();
				return;
			}
			
			// Scrolling --------------------------------------------------------------------------
			JScrollBar VSB = this.InnerScrollPanel.getVerticalScrollBar();
			int        FH  = TC.getFontMetrics(TC.getFont()).getHeight();
			switch(Evt.getKeyCode()) {
				case KeyEvent.VK_UP:    { VSB.setValue(VSB.getValue() - 3*FH); return; }
				case KeyEvent.VK_DOWN:  { VSB.setValue(VSB.getValue() + 3*FH); return; }
			}
		}
	}
	
	@Override public void paint(Graphics g) {
		if(this.IsJustFlush) {
			this.IsJustFlush = false;
			JScrollBar VSBar = this.InnerScrollPanel.getVerticalScrollBar();   VSBar.setValue(VSBar.getValue());
			JScrollBar HSBar = this.InnerScrollPanel.getHorizontalScrollBar(); HSBar.setValue(0);
		}
		super.paint(g);
	} 
}