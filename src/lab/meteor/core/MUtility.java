package lab.meteor.core;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import lab.meteor.core.type.MBinary;
import lab.meteor.core.type.MCode;
import lab.meteor.core.type.MDictionary;
import lab.meteor.core.type.MList;
import lab.meteor.core.type.MRef;
import lab.meteor.core.type.MSet;

public class MUtility {
	
	private static char[] alphabet = {
		'0','1','2','3','4','5','6','7','8','9',
		'a','b','c','d','e','f','g','h','i','j','k','l','m',
		'n','o','p','q','r','s','t','u','v','w','x','y','z',
		'A','B','C','D','E','F','G','H','I','J','K','L','M',
		'N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
		'_','='
	};
	
	public static String idEncode(long id) {
		StringBuilder builder = new StringBuilder();
		int ch = 0;
		long _id = id;
		while (_id != 0) {
			ch = (int) (_id & 0x000000000000003FL);
			_id >>>= 6;
			builder.append(alphabet[ch]);
		}
		return builder.reverse().toString();
	}
	
	public static long idDecode(String cid) {
		char ch = '\0';
		long _id = 0;
		for (int i = 0; i < cid.length(); i++) {
			int v = 0;
			_id <<= 6;
			ch = cid.charAt(i);
			if (ch == '_')
				v = 62;
			else if (ch == '=')
				v = 63;
			else if ('0' <= ch && ch <= '9')
				v = ch - '0';
			else if ('a' <= ch && ch <= 'z')
				v = ch - 'a' + 10;
			else if ('A' <= ch && ch <= 'Z')
				v = ch - 'A' + 36;
			_id |= v;
		}
		return _id;
	}
	
	private static Map<Class<?>, MPrimitiveType> classes;
	
	{
		classes = new HashMap<Class<?>, MPrimitiveType>();
		// Number
		classes.put(java.lang.Integer.class, MPrimitiveType.Int32);
		classes.put(java.lang.Long.class, MPrimitiveType.Int64);
		classes.put(java.lang.Float.class, MPrimitiveType.Number);
		classes.put(java.lang.Double.class, MPrimitiveType.Number);
		classes.put(java.lang.Short.class, MPrimitiveType.Number);
		classes.put(java.lang.Byte.class, MPrimitiveType.Number);
		// String
		classes.put(java.lang.String.class, MPrimitiveType.String);
		// Boolean
		classes.put(java.lang.Boolean.class, MPrimitiveType.Boolean);
		// DateTime
		classes.put(java.util.Date.class, MPrimitiveType.DateTime);
		classes.put(java.sql.Timestamp.class, MPrimitiveType.DateTime);
		// List
		classes.put(MList.class, MPrimitiveType.List);
		// Set
		classes.put(MSet.class, MPrimitiveType.Set);
		// Dict
		classes.put(MDictionary.class, MPrimitiveType.Dictionary);
		// Bin
		classes.put(MBinary.class, MPrimitiveType.Binary);
		// Regex
		classes.put(java.util.regex.Pattern.class, MPrimitiveType.Regex);
		// Code
		classes.put(MCode.class, MPrimitiveType.Code);
		// Ref
		classes.put(MRef.class, MPrimitiveType.Ref);
	}
	
	public static boolean checkType(MType type, Object value) {
		if (value == null)
			return true;
		MNativeDataType nType = type.getNativeDataType();
		switch (nType) {
		case Any:
			MPrimitiveType t = classes.get(value.getClass());
			if (t != null)
				return true;
			if (value instanceof MSymbol)
				return true;
			break;
		case Number:
			if (value instanceof java.lang.Number)
				return true;
			break;
		case Boolean:
			if (value instanceof java.lang.Boolean)
				return true;
			break;
		case String:
			if (value instanceof java.lang.String)
				return true;
			break;
		case DateTime:
			if (value instanceof java.util.Date)
				return true;
			break;
		case Enum:
			if (value instanceof MSymbol) {
				MSymbol sym = (MSymbol) value;
				if (sym.getEnum() == type)
					return true;
			}
			break;
		case List:
			if (value instanceof MList)
				return true;
			break;
		case Set:
			if (value instanceof MSet)
				return true;
			break;
		case Dictionary:
			if (value instanceof MDictionary)
				return true;
			break;
		case Binary:
			if (value instanceof MBinary)
				return true;
			break;
		case Regex:
			if (value instanceof Pattern)
				return true;
			break;
		case Int32:
			if (value instanceof java.lang.Integer)
				return true;
			break;
		case Int64:
			if (value instanceof java.lang.Long)
				return true;
			break;
		case Code:
			if (value instanceof MCode)
				return true;
			break;
		case Ref:
			if (value instanceof MRef)
				return true;
			break;
		default:
			return false;
		}
		return false;
	}
}
