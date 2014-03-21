package lab.meteor.core.script;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import lab.meteor.core.MDatabase;
import lab.meteor.dba.MongoDBAdapter;
import lab.meteor.visualize.MongoConnector;

public class ScriptImporter {
	public static MongoConnector connector;
	public static MScriptTask task=new MScriptTask(null,null);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		init();
		job(System.in);
		//process_code(System.in);
		exit();
	}
	
	public static void process_code(InputStream din){
		Scanner in = new Scanner(din);
		String line=null;
		//System.out.print(">>");
		while((line=in.nextLine())!=null){
			if(task.isRunning()){
				System.err.println("The Script Engine is running, please wait...");
				continue;
			}
			task.setCode(line.trim());
			task.execute();
			while(task.isRunning());
			System.out.println("Finished!Next Commands...");
			//System.out.print(">>");
		}
		in.close();
	}
	
	public static void job(InputStream din){
		Scanner in = new Scanner(din);
		String line=null;
		while((line=in.nextLine())!=null){
			File file=new File(line.trim());
			process(file);
		}
		in.close();
	}
	
	public static void init(){
		connector = new MongoConnector("meteor");
		connector.open();
		MDatabase.getDB().setDBAdapter(new MongoDBAdapter(connector.getDB()));
		MDatabase.getDB().initialize();
		MDatabase.getDB().setAutoSave(true);
		
		task.setEngine(new MScriptEngine());
		task.setCode("");
		
	}
	
	public static void exit(){
		connector.close();
	}
	
	@SuppressWarnings("deprecation")
	public static void process(File file){
		if(task.isRunning()){
			System.out.println("The Script Engine is running, please wait...");
			return;
		}
		try {
			DataInputStream in = new DataInputStream(new FileInputStream(file));
			
			StringBuilder code = new StringBuilder();
			String line;
			while((line=in.readLine())!=null){
				code.append(line);
			}
			task.setCode(code.toString());
			task.execute();
			in.close();
		} catch (FileNotFoundException e) {
			System.err.println("There exist no file: "+file.getName());
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("File close failed at: "+file.getName());
		}
	}
	
}
