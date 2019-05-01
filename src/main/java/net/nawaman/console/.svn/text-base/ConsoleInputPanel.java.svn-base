/*----------------------------------------------------------------------------------------------------------------------
 * Copyright (C) 2008 Nawapunth Manusitthipol. Implements with and for Sun Java 1.6 JDK.
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

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.CaretEvent;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;

import net.nawaman.swing.LineNumberedTextComponentPanel;

public class ConsoleInputPanel extends LineNumberedTextComponentPanel {
	
	ConsolePanel CPanel = null;
	
	public ConsoleInputPanel(ConsolePanel pCPanel) {
		super();
		if(pCPanel == null) throw new NullPointerException();
		this.CPanel = pCPanel;
		this.setMinimumSize(new Dimension(300, 200));
		
		final ConsoleInputPanel This = this;

		// Create Text panel first
		JTextArea TC = (JTextArea)this.getTextComponent();
		// Add key
		TC.addKeyListener(new KeyListener() {
			/** {@inheritDoc} */ public void keyTyped(KeyEvent e)    { }
			/** {@inheritDoc} */ public void keyReleased(KeyEvent e) { }
			/** {@inheritDoc} */ public void keyPressed(KeyEvent e)  { This.processKeyPressed(e);  }	
		});
	}
	
    /** Invoked when a key has been pressed. */
	protected void processKeyPressed(KeyEvent Evt) {
    	// If the key is up and the cursor is on the first one, the cursor is moved to top.
    	if((Evt.getKeyCode() == KeyEvent.VK_UP) && !Evt.isAltDown() && !Evt.isControlDown() && !Evt.isShiftDown()) {
    		try {
    			/*
	    		JTextComponent BTC = this.getTextComponent();
	    		Element BBody = BTC.getDocument().getDefaultRootElement();
	    		if((BBody.getElementCount() == 0) || (BBody.getElementIndex(BTC.getCaretPosition()) == 0)) {
	    			JTextComponent TTC = ((ConsolePanel)this.getParent().getParent().getParent().getParent()).
	    			                         OutputPanel.getMyTextComponent();
	    			// Get the position of the last one of top
	    			Element TRoot = TTC.getDocument().getDefaultRootElement();
	    			// Get the current caret position then find the position in text of the bottom one at the first line.
	    			Rectangle OutR = TTC.modelToView(TRoot.getEndOffset() - 1);
	    			Rectangle InR  = BTC.getUI().modelToView(BTC, BTC.getCaret().getDot(), Position.Bias.Forward);
	    			//Point P = BTC.getCaret().getMagicCaretPosition();
	    			int Pos = TTC.viewToModel(new Point(InR.x, OutR.y - 20));
		    		TTC.setCaretPosition(Pos);
		    		TTC.grabFocus();
	    		}*/
    			JTextComponent TTC = this.CPanel.OutputPanel.getMyTextComponent();
	    		TTC.setCaretPosition(this.CPanel.OutputPanel.getPlainText().length());
	    		TTC.grabFocus();
	    		return;
    		} catch(Exception E) {}
		}

    	// If the key is up and the cursor is on the first one, the cursor is moved to top.
    	if((Evt.getKeyCode() == KeyEvent.VK_ENTER) && !Evt.isAltDown() && !Evt.isControlDown() && !Evt.isShiftDown()) {
			JTextComponent JTC = this.getTextComponent();
			
			String Text = JTC.getText();
			int    Pos  = JTC.getCaretPosition() - 2;
			int    BeginLine = Text.lastIndexOf("\n", Pos) + 1;
			if(BeginLine < 0) BeginLine = 0;
	
			Caret pos = JTC.getCaret();
			String Tabs = "";
			for(int i = BeginLine; true; i++) {
				if(     Text.charAt(i) == '\t') Tabs += '\t';
				else if(Text.charAt(i) ==  ' ') Tabs += ' ';
				else break;
			}
			try{ JTC.getDocument().insertString(pos.getDot(), Tabs, null); }
			catch(Exception E) {}
    	}
    	
    	// Delegate to the console panel
    	this.CPanel.processKeyPressed(this, Evt);
	}

	/**{@inheritDoc}*/ @Override
	protected void notifiedCaretUpdate(CaretEvent e) {
		if(e.getDot() == 0) {
    		// Scroll all to the left when the caret move to the first column
    		((JScrollPane)CPanel.InnerScrollPanel).getHorizontalScrollBar().setValue(0);
		}
		super.notifiedCaretUpdate(e);
	}

	// History ---------------------------------------------------------------------------------------------------------

    /**{@inheritDoc}*/ @Override
    protected void clearHistory() {
    	super.clearHistory();
    }
    /**{@inheritDoc}*/ @Override
    protected void setHistoryEnabled(boolean pIsHistoryEnabled) {
    	super.setHistoryEnabled(pIsHistoryEnabled);
    }
    
}