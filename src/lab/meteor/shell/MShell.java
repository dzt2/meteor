package lab.meteor.shell;

import java.util.LinkedList;
import java.util.Set;
import java.util.regex.Pattern;

import lab.meteor.core.MAttribute;
import lab.meteor.core.MClass;
import lab.meteor.core.MDatabase;
import lab.meteor.core.MEnum;
import lab.meteor.core.MPackage;
import lab.meteor.core.MReference;
import lab.meteor.core.MSymbol;

public class MShell {
	
	final MDatabase db = MDatabase.getDB();
	MPackage currentPkg = MPackage.DEFAULT_PACKAGE;
	
	public MShell() {
	}
	
	final static String CREATE = "create";
	final static String DELETE = "delete";
	final static String UPDATE = "update";
	
	final static String CLASS = "class";
	final static String ENUM = "enum";
	final static String PACKAGE = "package";
	
	final static String AS = "as";
	
	Object context;
	
	public void parseCommandTest(String command) {
		Tokenizer tokenizer = new Tokenizer(command);
		Token t;
		while ((t = tokenizer.next()) != null) {
			System.out.println(t.content);
		}
	}
	
	public void parseCommand(String command) {
		Tokenizer tokenizer = new Tokenizer(command);
		tokenizer.inOperation = true;
		Token op = tokenizer.next();
		tokenizer.inOperation = false;
		switch (op.type) {
		case Create:
			int optype = 0;
			Token t1 = tokenizer.next();
			Token t2 = tokenizer.peek();
			if (t2.type == TokenType.Identifier) {// 是class,enum,package
				if (t1.content.equals(CLASS)) optype = 1;// class
				else if (t1.content.equals(ENUM)) optype = 2;// enum
				else if (t1.content.equals(PACKAGE)) optype = 3;// package
				else { System.out.println("error"); return; }
			} else {
				optype = 4;// attribute/symbol/reference
			}
			switch (optype) {
			case 1:
				Token className = tokenizer.next();
				if (className.type != TokenType.Identifier)
					{ System.out.println("error"); return; }
				tokenizer.inOperation = true;
				Token next = tokenizer.next();
				if (next != null) {
					if (next.type == TokenType.As) {
						tokenizer.inOperation = false;
						Token supName = tokenizer.next();
						
					} else { System.out.println("error"); return; }
				} 
				break;
			}
			break;
		case Delete:
			break;
		case Update:
			break;
		case Error:
			break;
		}
//		Token t;
//		while ((t = tokenizer.getNext()) != null) {
//			System.out.println(t.content);
//		}
		// create class People [as Animal]
		// create enum Gender
		// create package general
		// create general::People.name : string
		// create People.mother : People
		// create People.friends : People*
		// create Gender.male
		// create Gender.female
		
		// update People -> Person
		// update Gender -> Sex
		// update People.name -> label
		// update People.phone -> tel : integer
		// update People.sex -> gender : Gender
		
		// goto general
		
		// delete People
		// delete People.phone
		// delete People.friends
		// delete Gender.male
		// delete Gender
		
		
	}
	
	public void gotoPackege(String name) {
		if (name == null)
			return;
		if (name.equals(""))
			this.currentPkg = this.currentPkg.getParent();
		else if (currentPkg.hasPackage(name))
			this.currentPkg = this.currentPkg.getPackage(name);
	}
	
	public void setCurrentPackage(MPackage pkg) {
		this.currentPkg = pkg;
	}
	
	public String getPrintString(String name) {
		if (currentPkg.hasClass(name))
			return this.getPrintString(currentPkg.getClazz(name));
		else if (currentPkg.hasEnum(name))
			return this.getPrintString(currentPkg.getEnum(name));
		else if (currentPkg.hasPackage(name))
			return this.getPrintString(currentPkg.getPackage(name));
		else
			return "Not Found.";
	}
	
	public String getPrintString(MClass cls) {
		if (cls == null) {
			return null;
		}
		StringBuilder builder = new StringBuilder();
		builder.append(cls.toString()).append('\n');
		Set<String> atbnames = cls.getAllAttributeNames();
		for (String name : atbnames) {
			MAttribute atb = cls.getAttribute(name);
			builder.append(atb.toString()).append(" - Attribute\n");
		}
		Set<String> refnames = cls.getAllReferenceNames();
		for (String name : refnames) {
			MReference ref = cls.getReference(name);
			builder.append(ref.toString()).append(" - Reference\n");
		}
		return builder.toString();
	}
	
	public String getPrintString(MEnum enm) {
		if (enm == null) {
			return null;
		}
		StringBuilder builder = new StringBuilder();
		Set<String> symnames = enm.getSymbolNames();
		for (String name : symnames) {
			MSymbol sym = enm.getSymbol(name);
			builder.append(sym.toString()).append('\n');
		}
		return builder.toString();
	}
	
	public String getPrintString(MPackage pkg) {
		if (pkg == null)
			return null;
		StringBuilder builder = new StringBuilder();
		Set<String> names = pkg.getPackageNames();
		for (String name : names) {
			builder.append(name).append(" - Package\n");
		}
		names = pkg.getClassNames();
		for (String name : names) {
			builder.append(name).append(" - Class\n");
		}
		names = pkg.getEnumNames();
		for (String name : names) {
			builder.append(name).append(" - Enum\n");
		}
		return builder.toString();
	}
	
	static class Tokenizer {
		
		final String command;
		int loc;
		LinkedList<Token> buffer;
		
		boolean inOperation;
		
		Tokenizer(String command) {
			this.command = command;
			loc = 0;
			inOperation = false;
			buffer = new LinkedList<Token>();
		}
		
		private Token nextToken() {
			if (loc == command.length())
				return null;
			
			while (Character.isWhitespace(command.charAt(loc)))
				loc++;
			
			if (inOperation) {
				if (findWord(CREATE, loc)) {
					loc += CREATE.length();
					return new Token(CREATE, TokenType.Create);
				} else if (findWord(UPDATE, loc)) {
					loc += UPDATE.length();
					return new Token(UPDATE, TokenType.Update);
				} else if (findWord(DELETE, loc)) {
					loc += DELETE.length();
					return new Token(DELETE, TokenType.Delete);
//				} else if (findWord(CLASS, loc)) {
//					loc += CLASS.length();
//					return new Token(CLASS, TokenType.Class);
//				} else if (findWord(ENUM, loc)) {
//					loc += ENUM.length();
//					return new Token(ENUM, TokenType.Enum);
//				} else if (findWord(PACKAGE, loc)) {
//					loc += PACKAGE.length();
//					return new Token(PACKAGE, TokenType.Pacakge);
				} else if (findWord(AS, loc)) {
					loc += AS.length();
					return new Token(AS, TokenType.As);
				} else return Token.error;
			}
			
			
			char ch = nextChar();
			if (Character.isLetter(ch)) {
				StringBuilder sb = new StringBuilder();
				sb.append(ch);
				while (isWord(ch = peekChar()) || ch == ':') {
					if (ch == ':') {
						if (peekChar(1) == ':') {
							nextChar();nextChar();
							sb.append("::");
						} else {
							break;
						}
					} else {
						nextChar();
						sb.append(ch);
					}
				}
				return new Token(sb.toString(), TokenType.Identifier);
			} else if (ch == '.') {
				return new Token(".", TokenType.Dot);
			} else if (ch == ':') {
				return new Token(":", TokenType.Colon);
			} else if (ch == '*') {
				return new Token("*", TokenType.Star);
			} else if (ch == '-') {
				if (nextChar() == '>')
					return new Token("->", TokenType.Arrow);
				else
					return Token.error;
			} else {
				return Token.error;
			}
		}
				
		Token next() {
			if (buffer.size() != 0)
				return buffer.poll();
			else
				return nextToken();
		}
		
		Token peek() {
			if (buffer.size() == 0) {
				buffer.offer(nextToken());
			}
			return buffer.peek();
		}
		
		char nextChar() {
			if (loc == command.length())
				return 0;
			return command.charAt(loc++);
		}
		
		char peekChar() {
			if (loc == command.length())
				return 0;
			return command.charAt(loc);
		}
		
		char peekChar(int skip) {
			if (loc+skip == command.length())
				return 0;
			return command.charAt(loc+skip);
		}
		
		static boolean isWord(char ch) {
			return ch == '_' || Character.isLetterOrDigit(ch);
		}
		
		boolean findWord(String word, int start) {
			int i;
			for (i = 0; i < word.length(); i++) {
				if (word.charAt(i) != command.charAt(start + i))
					return false;
			}
			if (!isWord(command.charAt(start + i))) {
				return true;
			} else
				return false;
		}
		
	}
	
	enum TokenType {
		Create,
		Update,
		Delete,
		As,
		
//		Pacakge,
//		Class,
//		Enum,
		
		Identifier,
		Dot, // .
//		DoubleColon, //::
		Colon, // :
		Star, // *
		Arrow, // ->
		Error
	}
	
	static class Token {
		final static Token error = new Token("", TokenType.Error);
		TokenType type;
		String content;
		Token(String content, TokenType type) {
			this.content = content;
			this.type = type;
		}
	}
}
