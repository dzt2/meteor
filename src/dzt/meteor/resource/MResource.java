package dzt.meteor.resource;

import java.io.InputStream;
import java.io.OutputStream;

public interface MResource {
	public boolean is_write();
	public boolean is_read();
	public String getID();
	public void setID(String id);
	
	public InputStream getInputStream();
	public OutputStream getOutputStream();
}
