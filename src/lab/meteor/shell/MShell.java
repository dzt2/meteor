package lab.meteor.shell;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import lab.meteor.core.MAttribute;
import lab.meteor.core.MClass;
import lab.meteor.core.MDatabase;
import lab.meteor.core.MElement;
import lab.meteor.core.MEnum;
import lab.meteor.core.MPackage;
import lab.meteor.core.MReference;
import lab.meteor.core.MSymbol;

public class MShell {
	
	final MDatabase db = MDatabase.getDB();
	MPackage currentPkg = MPackage.DEFAULT_PACKAGE;
	
	List<IShellListener> listeners = new LinkedList<IShellListener>();
	
	public MShell() {
	}
	
	final static String CLASS = "class";
	final static String ENUM = "enum";
	final static String PACKAGE = "package";
	
	boolean enableConsolePrint = true;
	
	Object context;

	public void setEnableConsolePrint(boolean enable) {
		enableConsolePrint = enable;
	}
	
	private void commandFinish(String command, String message) {
		for (IShellListener l : listeners) {
			l.onCommandFinished(command, message);
		}
		if (enableConsolePrint)
			System.out.println(message);
	}
	
	public void parseCommandTest(String command) {
		Tokenizer tokenizer = new Tokenizer(command);
		Token t;
		while ((t = tokenizer.next()) != null) {
			System.out.println(t.content);
		}
	}
	
	private MElement findElement(String identifier) {
		// TODO
		return null;
	}
	
	private static void printError(int loc, String message) {
		System.out.println("error at " + loc + ":" + message);
	}
	
	/**
	 * Parse command string of model operation. Here are some examples: <br><br>
	 * -- Create Operation -- <br>
	 * create class People <br>
	 * create class People as Animal <br>
	 * create enum Gender <br>
	 * create package general <br>
	 * create general::People.name : string <br>
	 * create People.mother : People <br>
	 * create People.friends : People* <br>
	 * create Gender.male <br>
	 * create Gender.female <br>
	 * <br>
	 * -- Update Operation -- <br>
	 * update People -> Person <br>
	 * update Gender -> Sex <br>
	 * update People.name -> label <br>
	 * update People.phone -> tel : integer <br>
	 * update People.sex -> gender : Gender <br>
	 * <br>
	 * -- Delete Operation -- <br>
	 * delete People <br>
	 * delete People.phone <br>
	 * delete People.friends <br>
	 * delete Gender.male <br>
	 * delete Gender <br>
	 * <br>
	 * -- Move Operation (Move class/enum/package to another package) -- <br>
	 * move Person to general::society <br>
	 * <br>
	 * -- Goto Operation (Enter a package, i.e. change the operation context) -- <br>
	 * goto general <br>
	 * <br>
	 * -- Show Operation (Visualize a class/enum/package in diagram view) -- <br>
	 * show People <br>
	 * <br>
	 * -- Print Operation (Print a class/enum/package infomation) -- <br>
	 * print People <br>
	 * @param command
	 */
	
	public void parseCommand(String command) {
		Tokenizer tokenizer = new Tokenizer(command);
		tokenizer.inOperation = true;
		Token op = tokenizer.next();
		tokenizer.inOperation = false;
		switch (op.type) {
		case Create:
			int optype = 0;
			Token t1 = tokenizer.next();
			if (t1 == null)
			{ printError(tokenizer.loc, "op is too short.");return; }
			Token t2 = tokenizer.peek();
			if (t2 == null)
			{ printError(tokenizer.loc, "op is too short."); return; }
			if (t2.type == TokenType.Identifier) {// 是class,enum,package
				if (t1.content.equals(CLASS)) optype = 1;// class
				else if (t1.content.equals(ENUM)) optype = 2;// enum
				else if (t1.content.equals(PACKAGE)) optype = 3;// package
				else { printError(tokenizer.loc, "unknown keyword."); return; }
			} else {
				optype = 4;// attribute/symbol/reference
			}
			switch (optype) {
			case 1:
				{
					Token className = tokenizer.next();
					if (className == null || className.type != TokenType.Identifier)
					{ printError(tokenizer.loc, "class name is required."); return; }
					tokenizer.inOperation = true;
					Token next = tokenizer.next();
					tokenizer.inOperation = false;
					if (next != null) {
						if (next.type == TokenType.As) {
							Token supName = tokenizer.next();
							// TODO
							System.out.println("class " + className.content + " as " + supName.content);
						} else { printError(tokenizer.loc, "keyword \'as\' is required."); return; }
					} else {
						// TODO
						System.out.println("class " + className.content);
					}
					break;
				}
			case 2:
				{
					Token enumName = tokenizer.next();
					if (enumName == null || enumName.type != TokenType.Identifier)
					{ printError(tokenizer.loc, "enum name is required."); return; }
					if (tokenizer.next() != null)
					{ printError(tokenizer.loc, "unknown part."); return; }
					// TODO
					System.out.println("enum " + enumName.content);
					break;
				}
			case 3:
				{
					Token pkgName = tokenizer.next();
					if (pkgName == null || pkgName.type != TokenType.Identifier)
					{ printError(tokenizer.loc, "package name is required."); return; }
					if (tokenizer.next() != null)
					{ printError(tokenizer.loc, "unknown part."); return; }
					// TODO
					System.out.println("package " + pkgName.content);
					break;
				}
			case 4:
				{
					Token parentName = t1;
					t2 = tokenizer.next();
					if (t2.type != TokenType.Dot)
					{ printError(tokenizer.loc, "'.' is required."); return; }
					Token childName = tokenizer.next();
					if (childName.type != TokenType.Identifier)
					{ printError(tokenizer.loc, "field name is required."); return; }
					t2 = tokenizer.next();
					if (t2 != null) {
						if (t2.type == TokenType.Colon) {
							Token typeName = tokenizer.next();
							if (typeName == null || typeName.type != TokenType.Identifier)
							{ printError(tokenizer.loc, "type is required."); return; }
							Token star = tokenizer.next();
							boolean multiple = false;
							if (star != null) {
								if (star.type == TokenType.Star)
									multiple = true;
								else
								{ printError(tokenizer.loc, "unknown part."); return; }
							}
							// TODO attribute / reference
							System.out.println(parentName.content + "." + childName.content + " : " +
									typeName.content + (multiple ? "*" : ""));
						} else { printError(tokenizer.loc, "unknown part."); return; }
					} else {
						// TODO symbol
						System.out.println(parentName.content + "." + childName.content);
					}
					break;
				}
			}
			break;
		case Delete:
			{
				Token name = tokenizer.next();
				if (name == null || name.type != TokenType.Identifier)
				{ printError(tokenizer.loc, "name is required."); return; }
				
				Token dot = tokenizer.next();
				if (dot != null) {
					if (dot.type != TokenType.Dot)
					{ printError(tokenizer.loc, "unknown part."); return; }
					Token child = tokenizer.next();
					if (child == null || child.type != TokenType.Identifier)
					{ printError(tokenizer.loc, "field name is required."); return; }
					// TODO
					System.out.println("del " + name.content + " : " + child.content);
				} else {
					// TODO
					System.out.println("del " + name.content);
				}
				break;
			}
		case Update:
			{
				Token name = tokenizer.next();
				if (name == null || name.type != TokenType.Identifier)
				{ printError(tokenizer.loc, "name is required."); return; }
				
				Token next = tokenizer.next();
				if (next.type == TokenType.Arrow) {
					Token modify = tokenizer.next();
					if (modify == null || modify.type != TokenType.Identifier)
					{ printError(tokenizer.loc, "new name is required."); return; }
					// TODO only update class or enum or package name
					System.out.println(name.content + " -> " + modify.content);
				} else if (next.type == TokenType.Dot) {
					Token child = tokenizer.next();
					if (child == null || child.type != TokenType.Identifier)
					{ printError(tokenizer.loc, "field name is required."); return; }
					next = tokenizer.next();
					if (next == null || next.type != TokenType.Arrow)
					{ printError(tokenizer.loc, "'->' is required."); return; }
					Token modify = tokenizer.next();
					if (modify == null || modify.type != TokenType.Identifier)
					{ printError(tokenizer.loc, "new name is required."); return; }
					next = tokenizer.next();
					if (next != null) {
						if (next.type != TokenType.Colon)
						{ printError(tokenizer.loc, "unknown part."); return; }
						Token typeName = tokenizer.next();
						if (typeName == null || typeName.type != TokenType.Identifier)
						{ printError(tokenizer.loc, "type is required."); return; }
						Token star = tokenizer.next();
						boolean multiple = false;
						if (star != null) {
							if (star.type == TokenType.Star)
								multiple = true;
							else
							{ printError(tokenizer.loc, "unknown part."); return; }
						}
						// TODO attribute / reference update name & type
						System.out.println(name.content + "." + child.content + " -> " +
								modify.content + " : " +
								typeName.content + (multiple ? "*" : ""));
					} else {
						// TODO only update attribute / reference name
						System.out.println(name.content + "." + child.content + " -> " +
								modify.content);
					}
				} else {
					printError(tokenizer.loc, "unknown part."); return;
				}
				break;
			}
		case Move:
			{
				Token name = tokenizer.next();
				if (name == null || name.type != TokenType.Identifier)
				{ printError(tokenizer.loc, "name is required."); return; }
				tokenizer.inOperation = true;
				Token to = tokenizer.next();
				tokenizer.inOperation = false;
				if (to == null || to.type != TokenType.To)
				{ printError(tokenizer.loc, "'to' is required."); return; }
				Token targetName = tokenizer.next();
				if (targetName == null || targetName.type != TokenType.Identifier)
				{ printError(tokenizer.loc, "target package is required."); return; }
				System.out.println("mv " + name.content + " to " + targetName.content);
			}
			break;
		case Opposite:
			break;
		case Show:
			{
				Token name = tokenizer.next();
				if (name == null || name.type != TokenType.Identifier)
				{ printError(tokenizer.loc, "name is required."); return; }
				// TODO
				System.out.println("goto " + name.content);
			}
			break;
		case Print:
			{
				Token name = tokenizer.next();
				if (name == null || name.type != TokenType.Identifier)
				{ printError(tokenizer.loc, "name is required."); return; }
				// TODO
				System.out.println("print " + name.content);
			}
			break;
		case Goto:
			{
				Token pkgName = tokenizer.next();
				if (pkgName == null || pkgName.type != TokenType.Identifier)
				{ printError(tokenizer.loc, "name is required."); return; }
				// TODO
				System.out.println("goto " + pkgName.content);
			}
			break;
		case Error:
			printError(tokenizer.loc, "unknown operation."); return;
		default:
			break;
		}
		
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
		
		final static String CREATE = "create";
		final static String DELETE = "delete";
		final static String UPDATE = "update";
		final static String MOVE = "move";
		final static String OPPOSITE = "opposite";
		final static String GOTO = "goto";
		final static String SHOW = "show";
		final static String PRINT = "print";
		final static String AS = "as";
		final static String TO = "to";
		
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
				} else if (findWord(OPPOSITE, loc)) {
					loc += OPPOSITE.length();
					return new Token(OPPOSITE, TokenType.Opposite);
				} else if (findWord(MOVE, loc)) {
					loc += MOVE.length();
					return new Token(MOVE, TokenType.Move);
				} else if (findWord(GOTO, loc)) {
					loc += GOTO.length();
					return new Token(GOTO, TokenType.Goto);
				} else if (findWord(SHOW, loc)) {
					loc += SHOW.length();
					return new Token(SHOW, TokenType.Show);
				} else if (findWord(PRINT, loc)) {
					loc += PRINT.length();
					return new Token(PRINT, TokenType.Print);
				} else if (findWord(AS, loc)) {
					loc += AS.length();
					return new Token(AS, TokenType.As);
				} else if (findWord(TO, loc)) {
						loc += TO.length();
						return new Token(TO, TokenType.To);
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
			if (start + i == command.length() || !isWord(command.charAt(start + i))) {
				return true;
			} else
				return false;
		}
		
	}
	
	enum TokenType {
		Create,
		Update,
		Delete,
		Move,
		Opposite,
		As,
		To,
		
		Goto,
		Show,
		Print,
		
		Identifier,
		Dot, // .
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
		
		@Override
		public String toString() {
			return content;
		}
	}
}
