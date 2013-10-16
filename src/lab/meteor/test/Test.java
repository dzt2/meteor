package lab.meteor.test;

import java.io.Serializable;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import lab.meteor.core.MAttribute;
import lab.meteor.core.MClass;
import lab.meteor.core.MDBAdapter;
import lab.meteor.core.MDatabase;
import lab.meteor.core.MObject;
import lab.meteor.core.MPackage;
import lab.meteor.core.MPrimitiveType;
import lab.meteor.core.MReference;
import lab.meteor.core.MReference.Multiplicity;
import lab.meteor.dba.MongoDBAdapter;
import lab.meteor.shell.MShell;

public class Test {
	
	public static void main2(String[] args) {
		Connector c = new Connector("mydb2");
		c.open();
//		MShell shell = new MShell();
		
//		MDBAdapter dba = new MongoDBAdapter(c.getDB());
//		MDatabase.getDB().setDBAdapter(dba);
//		MDatabase.getDB().initialize();
		
//		MPackage pkg = new MPackage("Test");
//		MClass cls = new MClass("Function", pkg);
//		new MAttribute(cls, "name", MPrimitiveType.String);
//		new MAttribute(cls, "loc", MPrimitiveType.Number);
//		cls = new MClass("TestCase", pkg);
//		new MAttribute(cls, "name", MPrimitiveType.String);
//		cls = new MClass("UnitTestCase", cls, pkg);
//		new MAttribute(cls, "caseCount", MPrimitiveType.Number);
//		new MReference(cls, "function", pkg.getClazz("Function"), Multiplicity.One);
		
//		shell.gotoPackege("Test");
//		System.out.println(shell.getPrintString("Function"));
//		System.out.println(shell.getPrintString("TestCase"));
//		System.out.println(shell.getPrintString("UnitTestCase"));
//		MPackage pkg = MPackage.DEFAULT_PACKAGE.getPackage("Test");
//		MObject obj = new MObject(pkg.getClazz("Function"));
//		obj.setAttribute("name", "printf");
//		obj.setAttribute("loc", 20);
//		obj.save();
		c.close();
	}
	
	public static void main(String[] args) {
		ScriptEngineManager sem = new ScriptEngineManager();
		ScriptEngine se = sem.getEngineByName("javascript");
		se.put("jv8", new Jav8Printer());
//		se.getContext().getBindings(ScriptContext.GLOBAL_SCOPE).put("jv8", new Jav8Printer());
		try {
			se.eval("var a = 'cat'; function test() {a = 'dog'; jv8.print(a);} test(); jv8.print(a);");
		} catch (ScriptException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static class Jav8Printer {
		public void print(Object obj) {
			System.out.print(obj);
		}
	}
}
