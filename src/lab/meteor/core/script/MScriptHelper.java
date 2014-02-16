package lab.meteor.core.script;

import java.util.Iterator;

import lab.meteor.core.MClass;
import lab.meteor.core.MDictionary;
import lab.meteor.core.MElement;
import lab.meteor.core.MElementPointer;
import lab.meteor.core.MEnum;
import lab.meteor.core.MList;
import lab.meteor.core.MObject;
import lab.meteor.core.MPackage;
import lab.meteor.core.MElement.MElementType;
import lab.meteor.core.MProperty;
import lab.meteor.core.MReference;
import lab.meteor.core.MReference.Multiplicity;
import lab.meteor.core.MTag;

public class MScriptHelper {
	
	public Iterator<MObject> objectItr(MClass cls) {
		return cls.objectsIterator();
	}
	
	public Iterator<MObject> objectItr(String className) throws MScriptException {
		// Here className is a all-path name: p1::p2::p3::c
		MElement e = getElement(className);
		if (e == null || e.getElementType() != MElementType.Class)
			throw new MScriptException("class is not found.");
		return objectItr((MClass) e);
	}
	
	public MObject object(long id) {
		return (MObject)(new MElementPointer(id, MElementType.Object)).getElement();
	}

	public MObject newObject(MClass cls) {
		if (cls == null)
			return null;
		return new MObject(cls);
	}
	
	public MObject newObject(String clsName) throws MScriptException {
		MClass cls = findClass(clsName);
		return newObject(cls);
	}
	
	public MTag newTag(MElement e, String name, Object value) {
		return new MTag(e, name, value);
	}
	
	public MPackage defaultPackage() {
		return MPackage.DEFAULT_PACKAGE;
	}
	
	public MPackage findPackage(String name) throws MScriptException {
		MElement e = getElement(name);
		if (e == null || e.getElementType() != MElementType.Package)
			throw new MScriptException("package is not found.");
		return (MPackage) e;
	}
	
	public MClass findClass(String name) throws MScriptException {
		MElement e = getElement(name);
		if (e == null || e.getElementType() != MElementType.Class)
			throw new MScriptException("class is not found.");
		return (MClass) e;
	}
	
	public MEnum findEnum(String name) throws MScriptException {
		MElement e = getElement(name);
		if (e == null || e.getElementType() != MElementType.Enum)
			throw new MScriptException("enum is not found.");
		return (MEnum) e;
	}
	
	public void print(MElement e) {
		System.out.println(e.details());
	}

	public Object get(MElement obj, String exp) throws MScriptException {
		Tokenizer tn = new Tokenizer(exp);
		Object it = obj;
		Token t = tn.next();
		if (t == null || !t.name.equals("self"))
			throw new MScriptException("expression must start with 'self'.");
		while (tn.hasNext()) {
			t = tn.next();
			if (it == null)
				throw new MScriptException("interupt with null value.");
			// For d.d1.d2, this program will return the d2 as a Mobject object.
			it = find(it, t);
		}
		return it;
	}
	
	// MObject is the object to set values, exp express the name of attribute or reference or index of the object to be set.
	public void set(MObject obj, String exp, Object value) throws MScriptException {
		Tokenizer tn = new Tokenizer(exp);
		Object it = obj;
		Token t = tn.next();
		if (t == null || !t.name.equals("self"))
			throw new MScriptException("expression must start with 'self'.");
		while (tn.hasNext()) {
			t = tn.next();
			if (it == null)
				throw new MScriptException("interupt with null value.");
			if (!tn.hasNext())
				break;
			it = find(it, t);
		}
		if (t.type == TokenType.Dot) {
			if (!(it instanceof MObject))
				throw new MScriptException("wrong operation at \'" + t.name + "\'");
			MObject o = (MObject) it;
			o.load();
			MProperty p = o.getClazz().getProperty(t.name);
			if (p.getElementType() == MElementType.Attribute) {
				o.setAttribute(t.name, value);
			} else {
				if (((MReference) p).getMultiplicity() == Multiplicity.Multiple)
					throw new MScriptException("wrong operation for the invalid multiplicity.");
				if (!(value instanceof MObject))
					throw new MScriptException("invalid value.");
				o.setReference(t.name, (MObject) value);
			}
		} else if (t.type == TokenType.Bracket) {
			if (Character.isDigit(t.name.charAt(0))) {
				int index = Integer.parseInt(t.name);
				if (!(it instanceof MList))
					throw new MScriptException("wrong operation at \'" + t.name + "\'");
				MList list = (MList) it;
				if (index > list.size())
					throw new MScriptException("out of range at \'" + t.name + "\'");
				list.set(index, value);
			} else {
				if (!(it instanceof MDictionary))
					throw new MScriptException("wrong operation at \'" + t.name + "\'");
				MDictionary dict = (MDictionary) it;
				dict.put(t.name, value);
			}
		} else if (t.type == TokenType.Angular) {
			if (!(it instanceof MObject))
				throw new MScriptException("wrong operation at \'" + t.name + "\'");
			MElement o = (MElement) it;
			o.loadTags();
			MTag tag = o.tag(t.name);
			if (tag != null)
				tag.set(value);
			else {
				new MTag(o, t.name, value);
			}
		}
	}
	
	// add only used for set object's list attribute. So the exp must be an index of List or Dictionary.
	public void add(MObject obj, String exp, MObject value) throws MScriptException {
		if (value == null)
			return;
		Tokenizer tn = new Tokenizer(exp);
		Object it = obj;
		Token t = tn.next();
		if (t == null || !t.name.equals("self"))
			throw new MScriptException("expression must start with 'self'.");
		while (tn.hasNext()) {
			t = tn.next();
			if (it == null)
				throw new MScriptException("interupt with null value.");
			if (!tn.hasNext())
				break;
			it = find(it, t);
		}
		if (t.type == TokenType.Dot) {
			if (!(it instanceof MObject))
				throw new MScriptException("wrong operation at \'" + t.name + "\'");
			MObject o = (MObject) it;
			o.load();
			MProperty p = o.getClazz().getProperty(t.name);
			if (p.getElementType() == MElementType.Attribute) {
				throw new MScriptException("wrong operation for the invalid property.");
			} else {
				if (((MReference) p).getMultiplicity() == Multiplicity.One)
					throw new MScriptException("wrong operation for the invalid multiplicity.");
				o.addReference(t.name, value);
			}
		} else {
			throw new MScriptException("add operation should be operated on a reference property.");
		}
	}
	
	// To remove a value from a List or Dictionary reference which is addressed by exp.
	public void remove(MObject obj, String exp, MObject value) throws MScriptException {
		if (value == null)
			return;
		Tokenizer tn = new Tokenizer(exp);
		Object it = obj;
		Token t = tn.next();
		if (t == null || !t.name.equals("self"))
			throw new MScriptException("expression must start with 'self'.");
		while (tn.hasNext()) {
			t = tn.next();
			if (it == null)
				throw new MScriptException("interupt with null value.");
			if (!tn.hasNext())
				break;
			it = find(it, t);
		}
		if (t.type == TokenType.Dot) {
			if (!(it instanceof MObject))
				throw new MScriptException("wrong operation at \'" + t.name + "\'");
			MObject o = (MObject) it;
			o.load();
			MProperty p = o.getClazz().getProperty(t.name);
			if (p.getElementType() == MElementType.Attribute) {
				throw new MScriptException("wrong operation for the invalid property.");
			} else {
				if (((MReference) p).getMultiplicity() == Multiplicity.One)
					throw new MScriptException("wrong operation for the invalid multiplicity.");
				o.removeReference(t.name, value);
			}
		} else {
			throw new MScriptException("add operation should be operated on a reference property.");
		}
	}
	
	// process the token and generate its object.
	private Object find(Object it, Token t) throws MScriptException {
		if (t.type == TokenType.Dot) {
			if (!(it instanceof MObject))
				throw new MScriptException("wrong operation at \'" + t.name + "\'");
			MObject o = (MObject) it;
			o.load();
			it = o.get(t.name);
		} else if (t.type == TokenType.Bracket){
			if (Character.isDigit(t.name.charAt(0))) {
				int index = Integer.parseInt(t.name);
				if (!(it instanceof MList))
					throw new MScriptException("wrong operation at \'" + t.name + "\'");
				MList list = (MList) it;
				if (index >= list.size())
					throw new MScriptException("out of range at \'" + t.name + "\'");
				it = list.get(index);
			} else {
				if (!(it instanceof MDictionary))
					throw new MScriptException("wrong operation at \'" + t.name + "\'");
				MDictionary dict = (MDictionary) it;
				if (!dict.containsKey(t.name))
					throw new MScriptException("out of range at \'" + t.name + "\'");
				it = dict.get(t.name);
			}
		} else { //Angular
			if (!(it instanceof MObject))
				throw new MScriptException("wrong operation at \'" + t.name + "\'");
			MElement o = (MElement) it;
			o.loadTags();
			MTag tag = o.tag(t.name);
			if (tag == null)
				throw new MScriptException("interupt with null tag.");
			tag.load();
			it = tag.get();
		}
		return it;
	}

	public class MScriptException extends Exception {
		private static final long serialVersionUID = 8051203885298589303L;
		public MScriptException(String message) {
			super(message);
		}
	}
	
	private enum TokenType {
		Dot,
		Bracket,
		Angular
	}
	
	private class Token {
		String name;
		TokenType type;
		public Token(String n, TokenType t) { name = n; type = t; }
	}
	
	private class Tokenizer {
		String exp;
		int loc = 0;
		
		Tokenizer(String exp) {
			this.exp = exp;
		}
		
		boolean hasNext() {
			return loc < exp.length();
		}
		
		Token next() throws MScriptException {
			if (loc >= exp.length())
				return null;
			TokenType type;
			StringBuilder sb = new StringBuilder();
			char ch = cur();
			if (isWordChar(ch)) {
				// process d.d1... and get d as an MObject.
				sb.append(ch);
				loc++;
				while (isWordChar(ch = cur())) {
					sb.append(ch);
					loc++;
				}
				if (ch == '.')
					loc++;
				type = TokenType.Dot;	// What is the end.
			} 
			else if (ch == '[') {
				// process [string]. and get string as a List or Dictionary Index.
				loc++;
				while (isWordChar(ch = cur())) {
					sb.append(ch);
					loc++;
				}
				if (ch == ']')
					loc++;
				else
					throw new MScriptException("wrong expression.");
				// after [string] must follow a dot
				if (cur() == '.')
					loc++;
				else if (cur() != '[' && cur() != '<')
					throw new MScriptException("wrong expression.");
				type = TokenType.Bracket;
			} 
			else if (ch == '<')  {
				// process <string>. and get string as a Tag Object
				loc++;
				while (isWordChar(ch = cur())) {
					sb.append(ch);
					loc++;
				}
				if (ch == '>')
					loc++;
				else
					throw new MScriptException("wrong expression.");
				if (cur() == '.')
					loc++;
				else if (cur() != '[' && cur() != '<')
					throw new MScriptException("wrong expression.");
				type = TokenType.Angular;
			}
			else {
				throw new MScriptException("wrong expression.");
			}
			return new Token(sb.toString(), type);
		}
		
		char cur() {
			if (loc >= exp.length())
				return '\0';
			return exp.charAt(loc);
		}
		
		boolean isWordChar(char ch) {
			return Character.isLetter(ch) || Character.isDigit(ch) || ch == '_';
		}
	}
	
	/**
	 * A example of identifier:
	 * <blockquote>pkg_name::pkg_name::pkg_name::class_name</blockquote>
	 * @param identifier
	 * @return
	 */
	public static MElement getElement(String identifier) {
		if (identifier.equals(""))
			return MPackage.DEFAULT_PACKAGE;
		return getElement(identifier, MPackage.DEFAULT_PACKAGE);
	}
	
	/**
	 * 
	 * @param identifier
	 * @param pkg
	 * @return
	 */
	public static MElement getElement(String identifier, MPackage pkg) {
		String[] names = identifier.split("::");
		MElement pt = pkg;
		for (int i = 0; i < names.length; i++) {
			String name = names[i];
			if (pt.getElementType() == MElementType.Package) {
				pt = ((MPackage) pt).getChild(name);
				if (pt == null)
					return null;
			} else if (i < names.length - 1) {
				return null;
			}
		}
		return pt;
	}
	
	public void test() throws Exception {
		throw new Exception("test");
	}
	
}
