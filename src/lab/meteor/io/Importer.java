package lab.meteor.io;

import java.util.ArrayList;
import java.util.List;

public abstract class Importer<T> {
	public abstract void importData(T data);
	
	protected void makeProgress(int step, int allSteps) {
		for (ImportListener l : listeners) {
			l.onProgress(step, allSteps);
		}
	}
	
	List<ImportListener> listeners = new ArrayList<ImportListener>();
	public void addListener(ImportListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(ImportListener listener) {
		listeners.remove(listener);
	}
}
