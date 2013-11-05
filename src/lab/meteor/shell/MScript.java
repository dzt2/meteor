package lab.meteor.shell;

import java.util.Iterator;

import lab.meteor.core.MDictionary;
import lab.meteor.core.MElement;
import lab.meteor.core.MList;
import lab.meteor.core.MObject;
import lab.meteor.core.MPackage;
import lab.meteor.core.MElement.MElementType;

public class MScript {
	
	public Iterator<MObject> objects(String className) {
		// TODO
		return null;
	}
	
	public Object get(MObject obj, String exp) throws MScriptException {
		Tokenizer tn = new Tokenizer(exp);
		Object it = obj;
		Token t = tn.next();
		if (t == null || !t.name.equals("self"))
			throw new MScriptException("expression must start with 'self'.");
		while (tn.hasNext()) {
			t = tn.next();
			if (t.type == TokenType.Dot) {
				if (!(it instanceof MObject))
					throw new MScriptException("wrong operation at \'" + t.name + "\'");
				MObject o = (MObject) it;
				it = o.getProperty(t.name);
			} else {
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
			}
		}
		return it;
	}
	
	public static void main(String[] args) {
		MScript s = new MScript();
		try {
			s.get(null, "self[friends][0][name]");
		} catch (MScriptException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void set(MObject obj, String exp, Object value) {
		// TODO
	}
	
	public void add(MObject obj, String exp, MObject value) {
		
	}
	
	public void remove(MObject obj, String exp, MObject value) {
		
	}
	
	public class MScriptException extends Exception {
		private static final long serialVersionUID = 8051203885298589303L;
		public MScriptException(String message) {
			super(message);
		}
	}
	
	private enum TokenType {
		Dot,
		Bracket
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
				sb.append(ch);
				loc++;
				while (isWordChar(ch = cur())) {
					sb.append(ch);
					loc++;
				}
				if (ch == '.')
					loc++;
				type = TokenType.Dot;
			} else if (ch == '[') {
				loc++;
				while (isWordChar(ch = cur())) {
					sb.append(ch);
					loc++;
				}
				if (ch == ']')
					loc++;
				else
					throw new MScriptException("wrong expression.");
				if (cur() == '.')
					loc++;
				else if (cur() != '[')
					throw new MScriptException("wrong expression.");
				type = TokenType.Bracket;
			} else {
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
	
}
