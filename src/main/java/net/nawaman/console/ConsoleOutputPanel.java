/*----------------------------------------------------------------------------------------------------------------------
 * Copyright (C) 2008-2021 Nawapunth Manusitthipol. Implements with and for Java 11 JDK.
 *----------------------------------------------------------------------------------------------------------------------
 * LICENSE:
 * 
 * This file is part of Nawa's SimpleConsole.
 * 
 * The project is a free software; you can redistribute it and/or modify it under the SIMILAR terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or any later version.
 * You are only required to inform me about your modification and redistribution as or as part of commercial software
 * package. You can inform me via nawa<at>nawaman<dot>net.
 * 
 * The project is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the 
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 * ---------------------------------------------------------------------------------------------------------------------
 */

package net.nawaman.console;

import java.awt.geom.Point2D;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.text.Element;
import javax.swing.text.JTextComponent;

import net.nawaman.swing.text.HTMLOutputComponent;
import net.nawaman.swing.text.HTMLOutputPane;

public class ConsoleOutputPanel extends HTMLOutputPane {
	
    private static final long serialVersionUID = 1469281891969565839L;
    
    ConsolePanel               CPanel = null;
	HTMLOutputPane.PrintStream OUT    = null;
	HTMLOutputPane.PrintStream ERR    = null;

	public ConsoleOutputPanel(ConsolePanel pCPanel) {
		// Redirect the system.out print stream to the out print stream
		super();
		
		if(pCPanel == null) throw new NullPointerException();
		this.CPanel = pCPanel;

		// Set property of this component
		this.toBeCopiedAsPlainText();
		this.setMinimumSize(new Dimension(200, 100));
		this.toBeCopiedAsPlainText();
		
		// Add the out print stream and redirect it to System.out
		this.OUT = this.getSimpleFormatterPrintStream();
		
		// Add the error print stream and redirect it to System.err
		this.ERR = this.newSimpleFormatterPrintStream(SystemERR);
		this.ERR.changeColor(Color.RED);
		
		// Mouse Wheel ----------------------------------------------------------------------------
		
		this.getTextComponent().addMouseWheelListener(new MouseWheelListener() {
			/**{@inheritDoc}*/ @Override
		    public void mouseWheelMoved(MouseWheelEvent e) {
				int V = CPanel.InnerScrollPanel.getVerticalScrollBar().getValue();
				CPanel.InnerScrollPanel.getVerticalScrollBar().setValue(V + e.getWheelRotation()*30);
			}
		});
	}
	
	/**{@inheritDoc}*/ @Override
	protected PrintStream newDefaultSimpleFormatterPrintStream() {
		return this.newSimpleFormatterPrintStream(SystemOUT);
	}
	
	/** Returns the output print stream for the console */
	public PrintStream getOut() { return this.OUT; }
	
	/** Returns the error print stream for the console */
	public PrintStream getErr() { return this.ERR; }

	/** Returns the text component that this is holding. */ 
	HTMLOutputComponent getMyTextComponent() {
		return super.getTextComponent();
	}
	
	// KeyEvent --------------------------------------------------------------------------------------------------------

    /**{@inheritDoc}*/ @Override
    protected void processKeyPressed(KeyEvent Evt) {		
    	// If the key is up and the cursor is on the first one, the cursor is moved to top.
    	if((Evt.getKeyCode() == KeyEvent.VK_DOWN) && !Evt.isAltDown() && !Evt.isControlDown() && !Evt.isShiftDown()) {
    		try {
	    		JTextComponent TTC = this.getTextComponent();
	    		Element TBody = TTC.getDocument().getDefaultRootElement().getElement(1);
	    		if(TBody.getElementIndex(TTC.getCaretPosition()) == (TBody.getElementCount() - 1)) {
	    			JTextComponent BTC = ((ConsolePanel)this.getParent().getParent().getParent().getParent()).
	    			                         InputPanel.getTextComponent();
	    			// Get the current caret position then find the position in text of the bottom one at the first line. 
	    			Point P = TTC.getCaret().getMagicCaretPosition();
	    			int Pos = BTC.viewToModel2D(new Point2D.Double(P.x, 1));	// Any number will do
		    		BTC.setCaretPosition(Pos);
		    		BTC.grabFocus();
	    		}
    		} catch(Exception E) {}
    		return;
		}
    	
    	// Delegate to the console panel
    	this.CPanel.processKeyPressed(this, Evt);
	}
}
