package lab.meteor.shell;

import java.util.LinkedList;
import java.util.List;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import lab.meteor.core.type.MCode;

public class MScriptEngine {
	
	private ScriptEngine se;
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
		ScriptEngineManager sem = new ScriptEngineManager();
		se = sem.getEngineByName("javascript");
		helper = new MScriptHelper();
		se.getBindings(ScriptContext.ENGINE_SCOPE).put("me", helper);
	}
	
	public void run(String code) {
		try {
			se.eval(code);
		} catch (ScriptException e) {
			printError(e.getMessage());
		}
	}
	
	public void run(MCode code) {
		run(code.getCode());
	}
}
