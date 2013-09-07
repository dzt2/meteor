package lab.meteor.core;

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
}
