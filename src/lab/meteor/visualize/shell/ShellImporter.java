package lab.meteor.visualize.shell;

import java.io.InputStream;
import java.util.Scanner;

import lab.meteor.core.MDatabase;
import lab.meteor.core.MElement;
import lab.meteor.dba.MongoDBAdapter;
import lab.meteor.shell.IShowListener;
import lab.meteor.shell.MShell;
import lab.meteor.visualize.MongoConnector;

public class ShellImporter {
	public static MongoConnector connector;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		init();
		shell_console(System.in);
		exit();
	}
	
	public static void init(){
		connector = new MongoConnector("meteor");
		connector.open();
		MDatabase.getDB().setDBAdapter(new MongoDBAdapter(connector.getDB()));
		MDatabase.getDB().initialize();
		MDatabase.getDB().setAutoSave(true);
	}
	
	public static void exit(){
		connector.close();
	}
	
	public static void shell_console(InputStream fin){
		Scanner in = new Scanner(fin);
		String line=null;
		MShell shell=new MShell();
		shell.setShowListener(new IShowListener(){
			@Override
			public void show(MElement e) {
				// TODO Auto-generated method stub
				if(e!=null)
					System.out.println(e.details());
				else System.out.println("Finding "+null);
			}
			
		});
		while((line=in.nextLine())!=null){
			shell.parseCommand(line.trim());
		}
		in.close();
	}

}
