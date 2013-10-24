package lab.meteor.core.type;

/**
 * A simple wrapper of binary data.
 * @author Qiang
 *
 */
public class MBinary {
	
	/**
	 * The data.
	 */
	final byte[] data;
	
	/**
	 * Create with data.
	 * @param data
	 */
	public MBinary(byte[] data) {
		this.data = data;
	}
	
	/**
	 * The data.
	 * @return
	 */
	public byte[] getData() {
		return this.data;
	}
	
	/**
	 * The length of data.
	 * @return
	 */
	public int length() {
		return this.data.length;
	}
}
