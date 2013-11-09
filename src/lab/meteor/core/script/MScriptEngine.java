package lab.meteor.core.script;

import java.util.LinkedList;
import java.util.List;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import lab.meteor.core.type.MCode;

public class MScriptEngine {
	
	private ScriptEngineManager sem;
	MScriptHelper helper;
	
	List<IScriptListener> listeners = new LinkedList<IScriptListener>();
	
	boolean enableConsolePrint = true;
	
	public void setEnableConsolePrint(boolean enable) {
		enableConsolePrint = enable;
	}
	
	private void printError(String message) {
		if (enableConsolePrint)
			System.out.println(message);
		for (IScriptListener l : listeners) {
			l.printError(message);
		}
	}
	
	public MScriptEngine() {
		sem = new ScriptEngineManager();
	}
	
	public void run(String code) {
		try {
			ScriptEngine se = sem.getEngineByName("javascript");
			helper = new MScriptHelper();
			se.getBindings(ScriptContext.ENGINE_SCOPE).put("me", helper);
			se.eval(code);
		} catch (ScriptException e) {
			printError(e.getMessage());
		}
	}
	
	public void run(MCode code) {
		run(code.getCode());
	}
}
