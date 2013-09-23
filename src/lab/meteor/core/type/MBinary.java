package lab.meteor.core.type;

public class MBinary {
	
	final byte[] data;
	
	public MBinary(byte[] data) {
		this.data = data;
	}
	
	public byte[] getData() {
		return this.data;
	}
	
	public int length() {
		return this.data.length;
	}
}
