package lab.meteor.core.script;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class MScriptEngine {
	
	private ScriptEngineManager sem;
	
	// The Script Engine where get from will contain a MScriptHelper in its context.
	public final ScriptEngine getScriptEngine() {
		ScriptEngine se = sem.getEngineByName("javascript");
		MScriptHelper helper = new MScriptHelper();
		// context
		se.getBindings(ScriptContext.ENGINE_SCOPE).put("me", helper);
		setBindings();
		return se;
	}
	
	protected void setBindings() { }
	
	public MScriptEngine() {
		sem = new ScriptEngineManager();
	}
	
}
