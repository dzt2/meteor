package lab.meteor.shell;

import java.util.LinkedList;
import java.util.List;

import lab.meteor.core.MAttribute;
import lab.meteor.core.MClass;
import lab.meteor.core.MDatabase;
import lab.meteor.core.MElement;
import lab.meteor.core.MElement.MElementType;
import lab.meteor.core.MEnum;
import lab.meteor.core.MProperty;
import lab.meteor.core.MPackage;
import lab.meteor.core.MPrimitiveType;
import lab.meteor.core.MReference;
import lab.meteor.core.MReference.Multiplicity;
import lab.meteor.core.script.MScriptHelper;
import lab.meteor.core.MSymbol;
import lab.meteor.core.MDataType;

public class MShell {
	
	final MDatabase db = MDatabase.getDB();
	MPackage currentPkg = MPackage.DEFAULT_PACKAGE;
	
	List<IShellListener> listeners = new LinkedList<IShellListener>();
	
	IShowListener showListener;
	
	public MShell() {
	}
	
	public void addShellListener(IShellListener listener) {
		listeners.add(listener);
	}
	
	public void removeShellListener(IShellListener listener) {
		listeners.remove(listener);
	}
	
	public void setShowListener(IShowListener listener) {
		showListener = listener;
	}
	
	final static String CLASS = "class";
	final static String ENUM = "enum";
	final static String PACKAGE = "package";
	
	boolean enableConsolePrint = true;
	
	Object context;

	public void setEnableConsolePrint(boolean enable) {
		enableConsolePrint = enable;
	}
	
	public MPackage getCurrentPackage() {
		return this.currentPkg;
	}
	
	private MElement findElement(String identifier) {
		if (identifier == null)
			return null;
		if (identifier.contains("::"))
			return MScriptHelper.getElement(identifier);
		else
			return MScriptHelper.getElement(identifier, currentPkg);
	}

	private void commandFinish(String command, String message) {
		for (IShellListener l : listeners) {
			l.onCommandFinished(command, message);
		}
		if (enableConsolePrint)
			System.out.println(message);
	}

	private void printError(int loc, String message) {
		String em = "error at " + loc + ":" + message;
		for (IShellListener l : listeners) {
			l.printError(em);
		}
		if (enableConsolePrint)
			System.out.println(em);
	}
	
	private void print(String content) {
		for (IShellListener l : listeners) {
			l.print(content);
		}
		if (enableConsolePrint)
			System.out.println(content);
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
			if (t2.type == TokenType.Identifier) {// class,enum,package
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
							// create class People as Animal
							try {
								createClass(className.content, supName.content);
								commandFinish(command, "");
							} catch (MShellException e) {
								commandFinish(command, e.getMessage());
							}
						} else { printError(tokenizer.loc, "keyword \'as\' is required."); return; }
					} else {
						// create class People
						try {
							createClass(className.content, null);
							commandFinish(command, "");
						} catch (MShellException e) {
							commandFinish(command, e.getMessage());
						}
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
					// create enum Gender
					try {
						createEnum(enumName.content);
						commandFinish(command, "");
					} catch (MShellException e) {
						commandFinish(command, e.getMessage());
					}
					break;
				}
			case 3:
				{
					Token pkgName = tokenizer.next();
					if (pkgName == null || pkgName.type != TokenType.Identifier)
					{ printError(tokenizer.loc, "package name is required."); return; }
					if (tokenizer.next() != null)
					{ printError(tokenizer.loc, "unknown part."); return; }
					// create package processes
					try {
						createPackage(pkgName.content);
						commandFinish(command, "");
					} catch (MShellException e) {
						commandFinish(command, e.getMessage());
					}
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
							// attribute / reference
							// create People.name : string
							try {
								createProperty(childName.content, parentName.content, typeName.content, multiple);
								commandFinish(command, "");
							} catch (MShellException e) {
								commandFinish(command, e.getMessage());
							}
						} else { printError(tokenizer.loc, "unknown part."); return; }
					} else {
						// symbol
						// create Gender.Male
						try {
							createSymbol(childName.content, parentName.content);
							commandFinish(command, "");
						} catch (MShellException e) {
							commandFinish(command, e.getMessage());
						}
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
					// delete child of class or enum
					try {
						deleteChildElement(child.content, name.content);
						commandFinish(command, "");
					} catch (MShellException e) {
						commandFinish(command, e.getMessage());
					}
				} else {
					// delete class, enum or package
					try {
						deleteElement(name.content);
						commandFinish(command, "");
					} catch (MShellException e) {
						commandFinish(command, e.getMessage());
					}
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
					// only update class or enum or package name
					try {
						updateElementName(name.content, modify.content).save();
						commandFinish(command, "");
					} catch (MShellException e) {
						commandFinish(command, e.getMessage());
					}
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
						// update attribute / reference name and type (class / enum / primitive)
						try {
							updateProperty(child.content, name.content, modify.content, 
									typeName.content, multiple).save();
							commandFinish(command, "");
						} catch (MShellException e) {
							commandFinish(command, e.getMessage());
						}
					} else {
						// only update attribute / reference name / symbol name
						try {
							updateChildName(child.content, name.content, modify.content).save();
							commandFinish(command, "");
						} catch (MShellException e) {
							commandFinish(command, e.getMessage());
						}
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
				// move
				try {
					moveElement(name.content, targetName.content).save();
					commandFinish(command, "");
				} catch (MShellException e) {
					commandFinish(command, e.getMessage());
				}
			}
			break;
		case Opposite:
			{
				System.out.println("not support yet.");
			}
			break;
		case Show:
			{
				Token name = tokenizer.next();
				if (name == null || name.type != TokenType.Identifier)
				{ printError(tokenizer.loc, "name is required."); return; }
				try {
					showElement(name.content);
					commandFinish(command, "");
				} catch (MShellException e) {
					commandFinish(command, e.getMessage());
				}
			}
			break;
		case Print:
			{
				Token name = tokenizer.next();
				if (name == null) {
					print(currentPkg.details());
					commandFinish(command, "");
				} else if (name.type != TokenType.Identifier)
				{ printError(tokenizer.loc, "name is required."); return; }
				else {
					try {
						printElement(name.content);
						commandFinish(command, "");
					} catch (MShellException e) {
						commandFinish(command, e.getMessage());
					}
				}
			}
			break;
		case Goto:
			{
				Token pkgName = tokenizer.next();
				if (pkgName == null) {
					try {
						gotoPackege("");
						commandFinish(command, "");
					} catch (MShellException e) {
						commandFinish(command, e.getMessage());
					}
				}
				else {
					// goto
					try {
						gotoPackege(pkgName.content);
						commandFinish(command, "");
					} catch (MShellException e) {
						commandFinish(command, e.getMessage());
					}
				}
			}
			break;
		case Error:
			printError(tokenizer.loc, "unknown operation."); return;
		default:
			break;
		}
	}
	
	public void setCurrentPackage(MPackage pkg) {
		this.currentPkg = pkg;
	}
	
	private MClass createClass(String name, String superclass) throws MShellException {
		MClass supcls = null;
		if (superclass != null) {
			MElement e = findElement(superclass);
			if (e == null || e.getElementType() != MElementType.Class)
				throw new MShellException("superclass is not found.");
			supcls = (MClass) e;
		}
		if (name == null)
			throw new MShellException("invalid name.");
		if (name.contains("::"))
			throw new MShellException("full name has not been supported when creating class.");
		if (currentPkg.hasChild(name))
			throw new MShellException("the name has been used in current package.");
		return new MClass(name, supcls, currentPkg);
	}
	
	private MEnum createEnum(String name) throws MShellException {
		if (name == null)
			throw new MShellException("invalid name.");
		if (name.contains("::"))
			throw new MShellException("full name has not been supported when creating enum.");
		if (currentPkg.hasChild(name))
			throw new MShellException("the name has been used in current package.");
		return new MEnum(name, currentPkg);
	}
	
	private MPackage createPackage(String name) throws MShellException {
		if (name == null)
			throw new MShellException("invalid name.");
		if (name.contains("::"))
			throw new MShellException("full name has not been supported when creating package.");
		if (currentPkg.hasChild(name))
			throw new MShellException("the name has been used in current package.");
		return new MPackage(name, currentPkg);
	}
	
	private MProperty createProperty(String name, String parentName, String typeName, boolean multiple) throws MShellException {
		if (name == null || parentName == null || typeName == null)
			throw new MShellException("invalid name.");
		MClass parent = null;
		MElement e = findElement(parentName);
		if (e == null || e.getElementType() != MElementType.Class)
			throw new MShellException("class is not found.");
		parent = (MClass) e;
		if (parent.hasProperty(name))
			throw new MShellException("the name has been used in the class.");
		if (MPrimitiveType.isPrimitiveTypeIdentifier(typeName)) {
			MDataType type = MPrimitiveType.getPrimitiveType(typeName);
			return new MAttribute(parent, name, type);
		} else {
			e = findElement(typeName);
			System.out.println(typeName);
			if (e == null || (e.getElementType() != MElementType.Class &&
				e.getElementType() != MElementType.Enum))
				throw new MShellException("type of property is not found.");
			if (e.getElementType() == MElementType.Enum) {
				MDataType type = (MEnum) e;
				return new MAttribute(parent, name, type);
			} else {
				MClass refcls = (MClass) e;
				Multiplicity mul = Multiplicity.One;
				if (multiple)
					mul = Multiplicity.Multiple;
				return new MReference(parent, name, refcls, mul);
			}
		}
	}
	
	private MSymbol createSymbol(String name, String parentName) throws MShellException {
		if (name == null || parentName == null)
			throw new MShellException("invalid name.");
		MEnum parent = null;
		MElement e = findElement(parentName);
		if (e == null || e.getElementType() != MElementType.Enum)
			throw new MShellException("enum is not found.");
		parent = (MEnum) e;
		if (parent.hasSymbol(name))
			throw new MShellException("the name has been used in the enum.");
		return new MSymbol(parent, name);
	}
	
	private void deleteElement(String name) throws MShellException {
		MElement e = findElement(name);
		if (e == null)
			throw new MShellException("element is not found.");
		e.delete();
	}
	
	private void deleteChildElement(String name, String parentName) throws MShellException {
		if (name == null || parentName == null)
			throw new MShellException("invalid name.");
		MElement e = findElement(parentName);
		if (e == null)
			throw new MShellException("element is not found.");
		if (e.getElementType() != MElementType.Class && e.getElementType() != MElementType.Enum) {
			throw new MShellException("class or enum is not found.");
		}
		if (e.getElementType() == MElementType.Class) {
			MProperty p = ((MClass) e).getProperty(name);
			if (p == null)
				throw new MShellException("no such property.");
			p.delete();
		} else { // Enum
			MSymbol s = ((MEnum) e).getSymbol(name);
			if (s == null)
				throw new MShellException("no such symbol.");
			s.delete();
		}
	}
	
	private MElement updateElementName(String name, String newName) throws MShellException {
		if (name == null || newName == null)
			throw new MShellException("invalid name.");
		MElement e = findElement(name);
		if (e == null)
			throw new MShellException("element is not found.");
		if (e.getElementType() == MElementType.Package)
			((MPackage) e).setName(newName);
		else if (e.getElementType() == MElementType.Class)
			((MClass) e).setName(newName);
		else // Enum
			((MEnum) e).setName(newName);
		return e;
	}
	
	private MElement updateChildName(String name, String parentName, String newName) throws MShellException {
		if (name == null || parentName == null)
			throw new MShellException("invalid name.");
		MElement e = findElement(parentName);
		if (e == null)
			throw new MShellException("element is not found.");
		if (e.getElementType() != MElementType.Class && e.getElementType() != MElementType.Enum) {
			throw new MShellException("class or enum is not found.");
		}
		if (e.getElementType() == MElementType.Class) {
			MProperty p = ((MClass) e).getProperty(name);
			p.setName(newName);
			return p;
		} else { // Enum
			MSymbol s = ((MEnum) e).getSymbol(name);
			s.setName(newName);
			return s;
		}
	}
	
	private MElement updateProperty(String name, String parentName, 
			String newName, String typeName, boolean multiple) throws MShellException {
		if (name == null || parentName == null)
			throw new MShellException("invalid name.");
		MElement e = findElement(parentName);
		if (e == null)
			throw new MShellException("element is not found.");
		if (e.getElementType() != MElementType.Class) {
			throw new MShellException("class is not found.");
		}
		MClass cls = (MClass) e;
		MProperty p = cls.getProperty(name);
		if (MPrimitiveType.isPrimitiveTypeIdentifier(typeName)) {
			if (p.getElementType() == MElementType.Attribute) {
				((MAttribute) p).setDataType(MPrimitiveType.getPrimitiveType(typeName));
				return p;
			} else {
				p.delete();
				return new MAttribute(cls, newName, MPrimitiveType.getPrimitiveType(typeName));
			}
		} else {
			e = findElement(typeName);
			if (e == null || (e.getElementType() != MElementType.Class && 
				e.getElementType() != MElementType.Enum))
				throw new MShellException("type of property is not found.");
			if (e.getElementType() == MElementType.Class) {
				if (p.getElementType() == MElementType.Reference) {
					MReference r = (MReference) p;
					r.setName(newName);
					r.setReference((MClass) e);
					r.setMultiplicity(multiple ? Multiplicity.Multiple: Multiplicity.One);
					return r;
				} else {
					p.delete();
					return new MReference(cls, newName, (MClass) e, 
						multiple ? Multiplicity.Multiple: Multiplicity.One);
				}
			} else { // enum type
				if (p.getElementType() == MElementType.Reference) {
					p.delete();
					return new MAttribute(cls, newName, (MEnum) e);
				} else {
					MAttribute a = (MAttribute) p;
					a.setName(newName);
					a.setDataType((MEnum) e);
					return a;
				}
			}
		}
	}
	
	private MElement moveElement(String name, String parentName) throws MShellException {
		if (name == null || parentName == null)
			throw new MShellException("invalid name.");
		MElement e = findElement(name);
		if (e == null)
			throw new MShellException("element is not found.");
		MElement parent = findElement(parentName);
		if (parent == null || parent.getElementType() != MElementType.Package)
			throw new MShellException("target package is not found.");
		MPackage pkg = (MPackage) parent;
		if (e.getElementType() == MElementType.Package)
			((MPackage) e).setPackage(pkg);
		else if (e.getElementType() == MElementType.Class)
			((MClass) e).setPackage(pkg);
		else if (e.getElementType() == MElementType.Enum)
			((MEnum) e).setPackage(pkg);
		return e;
	}
	
	private void gotoPackege(String name) throws MShellException {
		if (name == null)
			throw new MShellException("invalid name.");
		if (name.equals("")) {
			if (this.currentPkg == MPackage.DEFAULT_PACKAGE)
				return;
			this.currentPkg = this.currentPkg.getPackage();
		} else {
			MElement e = findElement(name);
			if (e == null || e.getElementType() != MElementType.Package)
				throw new MShellException("package is not found.");
			this.currentPkg = (MPackage) e;
		}
	}
	
	private void printElement(String name) throws MShellException {
		if (name == null)
			throw new MShellException("invalid name.");
		MElement e = findElement(name);
		if (e == null)
			throw new MShellException("element is not found.");
		print(e.details());
	}
	
	private void showElement(String name) throws MShellException {
		if (name == null)
			throw new MShellException("invalid name.");
		MElement e = findElement(name);
		if (e == null)
			throw new MShellException("element is not found.");
		if (showListener != null)
			showListener.show(e);
	}
	
	static class MShellException extends Exception {
		public MShellException(String message) {
			super(message);
		}
		private static final long serialVersionUID = -770231784709755511L;
		
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
