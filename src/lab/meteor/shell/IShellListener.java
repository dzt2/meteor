package lab.meteor.shell;

public interface IShellListener {
	
	void onCommandFinished(String command, String message);
	
	void printError(String message);
	
	void print(String content);
	
}
