package lab.meteor.core.script;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

public class MScriptTask {
	
	private String code;
	
	private MScriptEngine engine;
	
	private boolean isRunning = false;
	
	public MScriptTask(MScriptEngine engine, String code) {
		this.engine = engine;
		this.code = code;
	}
	
	public final void setCode(String code) {
		this.code = code;
	}
	
	public final String getCode() {
		return code;
	}
	
	public final void setEngine(MScriptEngine engine) {
		this.engine = engine;
	}
	
	public final MScriptEngine getEngine() {
		return this.engine;
	}
	
	public final boolean isRunning() {
		return isRunning;
	}
	
	private Thread currentThread;
	
	public final void execute() {
		final ScriptEngine se = engine.getScriptEngine();
		currentThread = new Thread() {
			@Override
			public void run() {
				started();
				try {
					isRunning = true;
					se.eval(code);
					isRunning = false;
					completed();
				} catch (ScriptException e) {
					MScriptTask.this.interrupted(e.getLineNumber(), 
							e.getColumnNumber(), e.getCause().getMessage());
				} finally {
					currentThread = null;
					isRunning = false;
				}
			}
		};
		currentThread.start();
	}
	
	public final void cancel() {
		// TODO
		throw new UnsupportedOperationException();
	}
	
	protected void started() { }
	protected void completed() { }
	protected void interrupted(int line, int column, String message) {
		// Process the code line exception: line and column is the position error happened.
		// message is the error information.
	}
	protected void canceled() {  }

}
