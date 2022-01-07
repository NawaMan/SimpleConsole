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

import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.nawaman.script.CompiledCode;
import net.nawaman.script.ProblemContainer;
import net.nawaman.script.Scope;
import net.nawaman.script.ScriptEngine;
import net.nawaman.script.ScriptManager;
import net.nawaman.script.SimpleScriptExecutionExceptionWrapper;
import net.nawaman.script.java.JavaEngine;
//import net.nawaman.script.jsr223.JSEngine;
import net.nawaman.swing.LineNumberedTextComponentPanel.Config;

/**
 * This class is a console that run scripts from SimpleScript
 **/
public class SimpleConsole extends ConsolePanel {
	
    private static final long serialVersionUID = 3927487626642797359L;
    
    /** The default first line of the code (in case it does not have one) */
	static public final String DefaultEngineIdentifyingLine = "// @Java:";
	/** The name of variable for the engine identifying line  */
	static public final String EngineIdentifyingLineVarName = "$EngineIdentifyingLine";
	/** The default screen width */
	static public final int    DefaultScreenWitdth          = 100;
	
	/** The real scope - keep all variables in case the variable type are loss (scope of some engines does not support typing)*/
	Scope        $RealScope    = null;
	/** The current scope - This scope is used for better performance by keeping the scope for the engine of the previous code */
	Scope        $CurrentScope = null;
	/** The width of the screen that the console will try not the use over this (the width is in characters) */
	int          ScreenWitdth  = DefaultScreenWitdth;
		
	/** Flag to indicate that text (either In or Out) has been updated */
	private boolean IsTextUpdate  = false;
	
	/** Constructor of the simple console */
	public SimpleConsole() {
		super(new JLabel("CODE #1"), null, null);
		final SimpleConsole This = this;
		
		this.OutputPanel.clearText();
		this.OutputPanel.clearPattern().changeRelativeFontSize(3).addBold().println("Welcome to Simple Console.");
		this.OutputPanel.clearPattern()                                    .println("Type @@help to get help.");

		this.OutputPanel.getTextComponent().getDocument().addDocumentListener(new DocumentListener() {
	        public  void insertUpdate(DocumentEvent e)  { this.update(); }
	        public  void removeUpdate(DocumentEvent ej) { this.update(); }
	        public  void changedUpdate(DocumentEvent e) { this.update(); }
	        private void update() {
	        	This.IsTextUpdate = true;
	        }
		});
		
		Config Config = new Config();
		Config.setRightLimit(DefaultScreenWitdth);
		this.InputPanel.setConfig(Config);
		this.InputPanel.getTextComponent().getDocument().addDocumentListener(new DocumentListener() {
	        public void insertUpdate(DocumentEvent e)  { this.update(); }
	        public void removeUpdate(DocumentEvent ej) { this.update(); }
	        public void changedUpdate(DocumentEvent e) { this.update(); }
	        
	        private void update() {
	    		if(This.isModifiying) return;
	        	
	        	if(This.CodeNumber == (This.Codes.size() - 1)) return;
				// If there is a change to the code, Record it with the latest one
				This.Codes.set(This.Codes.size() - 1, This.InputPanel.getText());
				This.setCodeNumber(This.Codes.size() - 1);
	        }
		});
		
		// Add the first code in to the code container
		this.Codes.add("");
		this.setCodeNumber(0);

		(new Thread(new Runnable() {
			public void run() {
				try {
					boolean IsUpdated = false;
					while(true) {
						Thread.sleep(50);
						if(This.IsTextUpdate && !IsUpdated) {
							IsUpdated = true;
							
						} if(This.IsTextUpdate && IsUpdated) {
							This.InnerPanel.getLayout().layoutContainer(This.InnerPanel);
							This.InnerScrollPanel.revalidate();
							This.InnerScrollPanel.repaint();
							This.IsTextUpdate = false;
							IsUpdated         = true;
							
						} else if(IsUpdated) {
							This.InnerScrollPanel.getVerticalScrollBar().setValue(This.InnerScrollPanel.getVerticalScrollBar().getMaximum());
							IsUpdated = false;
						}
					}
				} catch(Throwable E) {}
			}
		})).start();
	}
	
	// Previous code ------------------------------------------------------------------------------

	/** The number of code (for referencing) */
	private int            CodeNumber   = 0;
	/** The flag indicating if the code is being modified and no need to do any more notifying */
	private boolean        isModifiying = false;
	/** Container of the previous code */
	private Vector<String> Codes        = new Vector<String>();

	/** Change the current code number */
	protected void setCodeNumber(int pCNumber) {
		if(this.isModifiying) return;
		if(this.CodeNumber == pCNumber) return;
		try {
			this.isModifiying = true;
		
			if(pCNumber <                  0) pCNumber = 0;
			if(pCNumber >= this.Codes.size()) pCNumber = this.Codes.size() - 1; 
			this.CodeNumber = pCNumber;
			((JLabel)this.SeparatorPanel.getLeft()).setText("CODE #" + (this.CodeNumber + 1));
			
			// This will somehow cause an exception saying that the document was locked somewhere and we are trying to
			//     change it; However, the document seems to be changing as we asked for. It is very possible that our
			//     changes are successful but there is some but in swing that in this situation create another attempt to
			//     change the text (change over what we ask) and that attempt fail. Since there is no time to investigate
			//     now, this will just simple be ignored.
			try { this.InputPanel.getTextComponent().setText(this.Codes.get(this.CodeNumber)); }
			catch(java.lang.IllegalStateException E) {
				if(!E.getMessage().toString().equals("Attempt to mutate in notification")) throw E;
			}
		} finally {
			this.isModifiying = false;
		}
	}
	
	/** Returns the current code number */
	public int getCodeNumber() {
		return this.CodeNumber;
	}
	
	// Execution ----------------------------------------------------------------------------------

	// Forward the execution to execute
	/** {@inheritDoc} */ @Override protected void notifiedFlush(String pText) {
		this.execute();
	}

	/** Execute the code in the input panel */
	public void execute() {
		// Save the code
		String Code = this.InputPanel.getText();
		this.Codes.set(this.CodeNumber, Code);
		
		// Execute
		this.execute("CODE #" + (this.CodeNumber + 1), Code);

		// Prepare the next code
		if(!"".equals(this.Codes.get(this.Codes.size() - 1).trim())) this.Codes.add("");
		this.setCodeNumber(this.Codes.size() - 1);
		this.InputPanel.clearHistory();
	}
	
	/** Execute the given code */
	public void execute(String CodeName, String pCode) {
		// No code, return null
		if((pCode == null) || "".equals(pCode.trim())) return;
		
		String PreCompileMessage = null;
		
		// Get the engine
		ScriptEngine TheEngine = null;
		try { TheEngine = ScriptManager.GetEngineFromCode(pCode); } catch(RuntimeException RTE) {}
		if(TheEngine == null) {	// Cannot get the engine name from the code, let's try the default code
			// Add the engine identifying line
			String TheEngineIdentifyingLine = null;
			if(this.$CurrentScope != null) {
				if(this.$CurrentScope.isExist(EngineIdentifyingLineVarName) &&
				  (this.$CurrentScope.getTypeOf(EngineIdentifyingLineVarName) == String.class)) {
					// Found in the current scope, try to use it
					String EIL = this.$CurrentScope.getValue(EngineIdentifyingLineVarName).toString();

					try { TheEngine = ScriptManager.GetEngineFromCode(EIL + "\n"); } catch(RuntimeException RTE) {}
					if(TheEngine == null) {
						if(this.CodeNumber != 0) {
							PreCompileMessage =
								"WARNING: The default engine is not specified, invalid or unknown. "+
								"The default will be used. (\""+DefaultEngineIdentifyingLine+"\")";
						}
					} else {
						// The value is valid
						TheEngineIdentifyingLine = EIL;
					}
				}
			}
			
			// Cannot find, just use the default
			if(TheEngineIdentifyingLine == null) {
				TheEngineIdentifyingLine = DefaultEngineIdentifyingLine;
				if(this.$CurrentScope != null) this.$CurrentScope.newVariable(EngineIdentifyingLineVarName, String.class, DefaultEngineIdentifyingLine);
			}

			pCode = TheEngineIdentifyingLine + "\n" + pCode;
			
			if(TheEngine == null) TheEngine = ScriptManager.GetEngineFromCode(pCode);
		}
		
		// Prepare the run
		if(this.$CurrentScope == null) { // Create the scope if it does not exist
			System.setOut(this.getOut());
			System.setErr(this.getErr());
			this.$CurrentScope = TheEngine.newScope();
			this.$RealScope    = new Scope.Simple();

			this.$CurrentScope.newVariable("$Scope",                     Scope.class,  this.$CurrentScope);
			this.$CurrentScope.newVariable(EngineIdentifyingLineVarName, String.class, DefaultEngineIdentifyingLine);
			
		} else	{ // OR Make sure it compatible for this execution.
			Scope NewScope = TheEngine.getCompatibleScope(this.$CurrentScope);
			// There is a changing of scope, the scope 
			if(NewScope != this.$CurrentScope) {
				// Create a new scope that combine variable from both scope.
				// Base on all variables on current scope.
				// If the variable exist in both. See if the value (from CurrentScope) can be assigned to type from MainScope.
				// If it cannot, ignore the type.
				NewScope = TheEngine.newScope();
				
				for(String VName : this.$CurrentScope.getVariableNames()) {
					if(VName == null)          continue;
					if("$Scope".equals(VName)) continue;
					// Exist in real scope, ensure the type
					Class<?> OrgCls = null;
					Object   Value  = this.$CurrentScope.getValue(VName);
					// If not found in real scope, no type or cannot be assigned by the value, create one
					if(!this.$RealScope.isExist(VName) || ((OrgCls = this.$RealScope.getTypeOf(VName)) == null) || !OrgCls.isInstance(Value)) {
						// Create a new variable with the same type with the current scope
						Class<?> NewCls =  this.$CurrentScope.getTypeOf(VName); if(NewCls == null) NewCls = Object.class;
						if(this.$CurrentScope.isWritable(VName) || !NewScope.isConstantSupport())
						     NewScope.newVariable(VName, NewCls, Value);
						else NewScope.newConstant(VName, NewCls, Value);

						// Also adjust the real scope
						if(this.$RealScope.isExist(VName)) this.$RealScope.removeVariable(VName);
						if(this.$CurrentScope.isWritable(VName))
							 this.$RealScope.newVariable(VName, NewCls, Value);
						else this.$RealScope.newConstant(VName, NewCls, Value);

					} else {
						// Create a new variable with the same type with the real scope
						if(this.$CurrentScope.isWritable(VName) || !NewScope.isConstantSupport())
							 NewScope.newVariable(VName, OrgCls, Value);
						else NewScope.newConstant(VName, OrgCls, Value);
						
						if(this.$RealScope.isWritable(VName) && this.$CurrentScope.isWritable(VName))
							this.$RealScope.setValue(VName, Value);
						else {
							// Also adjust the real scope
							if(this.$RealScope.isExist(VName)) this.$RealScope.removeVariable(VName);
							if(this.$CurrentScope.isWritable(VName))
								 this.$RealScope.newVariable(VName, OrgCls, Value);
							else this.$RealScope.newConstant(VName, OrgCls, Value);
						}
					}
				}
				
				// Remove variable in real scope if it is no longer in the current scope
				String[] RSVNames = this.$RealScope.getVariableNames().toArray(new String[this.$RealScope.getVariableNames().size()]);
				for(String VName : RSVNames) { if(!this.$CurrentScope.isExist(VName)) this.$RealScope.removeVariable(VName); }
				
				// Set the scope
				this.$CurrentScope = NewScope;
				
				// Add itself, so that the code can examine the scope
				this.$CurrentScope.newVariable("$Scope", Scope.class, this.$CurrentScope);
			}
		}
		
		// Compile
		ProblemContainer Problems = TheEngine.newCompileProblemContainer();
		CompiledCode     TheCCode = null;
		String           Error    = null;
		Throwable        Cause    = null;
		boolean          IsToStop = false;

		try {
			try {
				{	// Print the Start Message
					this.getOut().println();
					if(PreCompileMessage != null) this.getErr().println(PreCompileMessage);
					this.getOut().println();
					StringBuffer Msg = new StringBuffer("START: " + CodeName + " [" + TheEngine.getShortName() + "] ");
					while(Msg.length() < this.ScreenWitdth) Msg.append("+");
					this.getOut().println("<html><b>"+Msg+"</b>");
				}
				
				// Try to compile (so that the error message is separated)
				if(TheEngine.isCompilable()) {
					try {
						TheCCode = TheEngine.compile(pCode, null, null, null, Problems);
			
						// There are problems with the compilation
						if(Problems.hasProblem()) {
							// Record the error message
							Error = Problems.toString();
							// If it is an error stop the process
							if(Problems.hasError()) {
								IsToStop = true;
								return;
							}
						}
					} catch(Throwable E) {
						if((E instanceof SimpleScriptExecutionExceptionWrapper) && (E.getCause() != null)) E = E.getCause();
						Error = "There is a problem compile the code: " + E.toString();
						Cause = E;
						IsToStop = true;
						return;
					}
				}
				
				if(!IsToStop) {	// Continue
					// Run the script
					if(TheEngine.isCompilable()) TheEngine.eval(TheCCode, this.$CurrentScope, Problems);
					else                         TheEngine.eval(pCode,    this.$CurrentScope, Problems);
		
					// There are problems with the compilation
					if(Problems.hasProblem()) {
						// Record the error message
						Error = Problems.toString();
						// If it is an error stop the process
						if(Problems.hasError()) {
							IsToStop = true;
							return;
						}
					}
				}
			} catch(Throwable E) {
				if((E instanceof SimpleScriptExecutionExceptionWrapper) && (E.getCause() != null)) E = E.getCause();
				// There is an exception
				Error = "There is a problem executing the code: " + E.toString();
				Cause = E;
				return;
			}

		} finally {
			if(Error != null) {	// Display error
				this.getErr().println("<html>" + Error);
				if(Cause != null) Cause.printStackTrace(this.getErr());
			}
	
			{	// Print the Last Message
				StringBuffer Msg = new StringBuffer("<br>END: " + CodeName + " ");
				while(Msg.length() < this.ScreenWitdth) Msg.append("=");
				this.getOut().println("<html><b>"+Msg+"</b>");
			}
		}
	}
	
    /**{@inheritDoc}*/ @Override
    protected void processKeyPressed(Object pSource, KeyEvent Evt) {
		super.processKeyPressed(pSource, Evt);
		
		if(pSource == this.InputPanel) {
			if(((Evt.getKeyCode() == KeyEvent.VK_UP) || (Evt.getKeyCode() == KeyEvent.VK_DOWN)) &&
					Evt.isAltDown() && !Evt.isControlDown() && !Evt.isShiftDown()) {
				
				// If there is a change to the code, Record it with the latest one
				String Text = this.InputPanel.getText();
				if(!Text.equals(this.Codes.get(this.CodeNumber))) {
					this.Codes.set(this.Codes.size() - 1, Text);
					this.CodeNumber = this.Codes.size() - 1;
				}

				if(Evt.getKeyCode() == KeyEvent.VK_UP) this.setCodeNumber(this.CodeNumber - 1);
				else                                   this.setCodeNumber(this.CodeNumber + 1);
			}
		}
	}
	
	// Accessory member ------------------------------------------------------------------------------------------------ 
	
	// This ------------------------------------------------------------------------------------------------------------
	
	static public void main(String[] args) {
		// Load available engine
		ScriptManager.Instance.loadEngine(JavaEngine.class);
//		ScriptManager.Instance.loadEngine(JSEngine.class);
		
		JFrame f = new JFrame("Simple Console");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		final SimpleConsole JP = new SimpleConsole();
		JP.setVisible(true);
		
		f.getContentPane().add(JP);
		f.setSize(1000, 700);
		f.setLocationRelativeTo(null);
		f.setVisible(true);
		
		f.addWindowListener( new WindowAdapter() {
			@Override public void windowOpened( WindowEvent e ){
				JP.getBottom().grabFocus();
			}
		});
		
		System.out.println("END");
	}
}
