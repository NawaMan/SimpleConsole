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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

/**
 * Layout for console panel
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
class ConsoleLayout implements LayoutManager {

	static public final String OutputName    = "Output";
	static public final String SeparatorName = "Separator";
	static public final String InputName     = "Input";
	
	static private final Dimension ZeroDimension = new Dimension(0, 0);
	
	/** Create a new text-flow layout */
    public ConsoleLayout() { super(); }

    /** Adds a layout component (do nothing) */
    public void addLayoutComponent(String name, Component comp) {}
    /** Removes a layout component */
    public void removeLayoutComponent(Component comp)           {}
    
    /**
     * Determine the minimum layout size of this container. The minimum size if the size in which
     *    no line wrapping is performed and all the size of the all component is its minimum size.
     **/
	public Dimension minimumLayoutSize(Container target) {
		synchronized (target.getTreeLock()) {
			Dimension Size = new Dimension(0, 0);
					
			Component Output    = null;
			Component Separator = null;
			Component Input     = null;
			// Get the component number
			int MCount = target.getComponentCount();
			// Loop all the component looking for line setting
			for (int i = 0; i < MCount; i++) {
				Component C =  target.getComponent(i);
				if((Output    == null) && OutputName   .equals(C.getName())) Output    = C;
				if((Separator == null) && SeparatorName.equals(C.getName())) Separator = C;
				if((Input     == null) && InputName    .equals(C.getName())) Input     = C;
			}
			
			if(Output    == null) throw new NullPointerException();
			if(Input == null) throw new NullPointerException();
			
			Dimension OutputSize    = Output.getPreferredSize();
			Dimension InputSize     = Input.getPreferredSize();
			Dimension SeparatorSize = ((Separator == null) || !Separator.isVisible())?ZeroDimension:Separator.getMinimumSize();
			
			Size.width  = Math.max(Math.max(OutputSize.width,   SeparatorSize.width),  InputSize.width);
			Size.height =                   OutputSize.height + SeparatorSize.height + InputSize.height;
			
			// Finish all component so add the effect of the decoration
			Insets insets = target.getInsets();
			Size.width  += insets.left + insets.right;	// Add the insets (border and other decoration)
			Size.height += insets.top  + insets.bottom;	// Add the insets (border and other decoration)

			// Return the size
			return Size;
		}
	}

    /**
     * Determine the preferred layout size of this container. The preferred size if the size in which
     *    no line wrapping is performed and all the size of the all component is its preferred size.
     **/
	public Dimension preferredLayoutSize(Container target) {
		synchronized (target.getTreeLock()) {
			Dimension Size = new Dimension(0, 0);
					
			Component Output    = null;
			Component Separator = null;
			Component Input     = null;
			// Get the component number
			int MCount = target.getComponentCount();
			// Loop all the component looking for line setting
			for (int i = 0; i < MCount; i++) {
				Component C =  target.getComponent(i);
				if((Output    == null) && OutputName   .equals(C.getName())) Output    = C;
				if((Separator == null) && SeparatorName.equals(C.getName())) Separator = C;
				if((Input     == null) && InputName    .equals(C.getName())) Input     = C;
			}
			
			if(Output == null) throw new NullPointerException();
			if(Input  == null) throw new NullPointerException();
			
			Dimension OutputSize    = Output   .getPreferredSize();
			Dimension InputSize     = Input.getPreferredSize();
			Dimension SeparatorSize = ((Separator == null) || !Separator.isVisible())?ZeroDimension:Separator.getPreferredSize();
			
			Size.width  = Math.max(Math.max(OutputSize.width,   SeparatorSize.width),  InputSize.width);
			Size.height =                   OutputSize.height + SeparatorSize.height + InputSize.height;
			
			// Finish all component so add the effect of the decoration
			Insets insets = target.getInsets();
			Size.width  += insets.left + insets.right;	// Add the insets (border and other decoration)
			Size.height += insets.top  + insets.bottom;	// Add the insets (border and other decoration)

			// Return the size
			return Size;
		}
	}

	// This method will be called after getPreferred size so its size will be already determined.
	/** Layout the container. **/
	public void layoutContainer(Container target) {
		synchronized (target.getTreeLock()) {
			// Get the declaration
			Insets    Insets   = target.getInsets();
			Dimension ThisSize = target.getSize();
					
			Component Output    = null;
			Component Separator = null;
			Component Input     = null;
			// Get the component number
			int MCount = target.getComponentCount();
			// Loop all the component looking for line setting
			for (int i = 0; i < MCount; i++) {
				Component C =  target.getComponent(i);
				if((Output    == null) && OutputName   .equals(C.getName())) Output    = C;
				if((Separator == null) && SeparatorName.equals(C.getName())) Separator = C;
				if((Input     == null) && InputName    .equals(C.getName())) Input     = C;
			}
			
			if(Output == null) throw new NullPointerException();
			if(Input  == null) throw new NullPointerException();
			
			Dimension OutputSize    = Output.getPreferredSize();
			Dimension InputSize     = Input.getPreferredSize();
			Dimension SeparatorSize = ((Separator == null) || !Separator.isVisible())?ZeroDimension:Separator.getPreferredSize();
			int Width  = Math.max(Math.max(OutputSize.width,   SeparatorSize.width),  InputSize.width);
			int Height =                   OutputSize.height + SeparatorSize.height + InputSize.height;
			
			int CLeft  = Insets.left + 2;
			int CWidth = ThisSize.width - Insets.left - Insets.right - 4;
			
			if(ThisSize.height > Height) {
				int OutputHeight = ThisSize.height - Insets.bottom - InputSize.height - SeparatorSize.height;

                                      Output   .setLocation(CLeft, Insets.top);
				if(Separator != null) Separator.setLocation(CLeft, OutputHeight);
                                      Input    .setLocation(CLeft, OutputHeight + SeparatorSize.height);

				
				                      Output   .setSize(CWidth, OutputHeight - Insets.top);
				if(Separator != null) Separator.setSize(CWidth, SeparatorSize.height);
                                      Input    .setSize(CWidth, InputSize.height);
				
			} else {
				                      Output   .setLocation(CLeft, Insets.top);
				if(Separator != null) Separator.setLocation(CLeft, Insets.top + OutputSize.height);
				                      Input    .setLocation(CLeft, Insets.top + OutputSize.height + SeparatorSize.height);
				
				                      Output   .setSize(CWidth, OutputSize   .height);
				if(Separator != null) Separator.setSize(CWidth, SeparatorSize.height);
				                      Input    .setSize(CWidth, InputSize    .height);
			}
			
			// Finish all component so add the effect of the decoration
			Width  += Insets.left + Insets.right;	// Add the insets (border and other decoration)
			Height += Insets.top  + Insets.bottom;	// Add the insets (border and other decoration)
		
			Dimension Size = new Dimension(Width, Height);

			target.setPreferredSize(Size);
		}
	}
}
