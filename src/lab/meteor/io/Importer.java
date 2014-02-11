package lab.meteor.io;

import java.util.ArrayList;
import java.util.List;

public abstract class Importer<T> {
	
	public void doImport(final T data) {
		Thread t = new Thread() {
			@Override
			public void run() {
				importData(data);
			}
		};
		t.start();
	}
	
	protected abstract void importData(T data);
	
	protected void makeProgress(int step, int allSteps) {
		/*
		 *	step: ?
		 *	all steps: ? 
		 * 
		 */
		for (ImportListener l : listeners) {
			l.onProgress(this, step, allSteps);
		}
	}
	
	protected void finished() {
		for (ImportListener l : listeners) {
			l.onFinished(this);
		}
	}
	
	List<ImportListener> listeners = new ArrayList<ImportListener>();
	public void addListener(ImportListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(ImportListener listener) {
		listeners.remove(listener);
	}
	
	Result r = new Result();
	
	public Result getResult() {
		return r;
	}
	
	public class Result {
		private Result() {  }
		String message;
		ResultState state;
		
		public String getMessage() {
			return message;
		}
		
		public ResultState getResultState() {
			return state;
		}
	}
	
	public enum ResultState {
		Error,
		Warning,
		Success
	}
}
