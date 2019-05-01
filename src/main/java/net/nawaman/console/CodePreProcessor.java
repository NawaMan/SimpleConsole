package net.nawaman.console;

import net.nawaman.script.ScriptEngine;

/** This class allow the code that about to be run in SimpleConsole to be pre-processed before actually run */
public interface CodePreProcessor {

	/** Checks if the code from the given engine can be pre-processed by this pre-processor */
	public boolean isCompatibleWithCodeFrom(ScriptEngine pSEngine);
	
	/** Process the code */
	public String process(String Code);
	
}
