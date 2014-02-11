package lab.meteor.io;

public interface ImportListener {
	/*
	 *	??? 
	 */
	void onProgress(Importer<?> i, int currentSteps, int allSteps);
	
	void onFinished(Importer<?> i);
}
