package lab.meteor.io.table;

import java.util.Map;
import java.util.TreeMap;

public abstract class DataTagable {
	
	Map<String, Object> tags;
	
	private Map<String, Object> getTags() {
		if (tags == null)
			tags = new TreeMap<String, Object>();
		return tags;
	}
	
	public void setTag(String name, Object tag) {
		getTags().put(name, tag);
	}
	
	public void removeTag(String name) {
		getTags().remove(name);
	}
	
	public Object getTag(String name) {
		return getTags().get(name);
	}
	
	public void clearTags() {
		if (tags != null)
			tags.clear();
	}
	
	public boolean hasTag(String name) {
		return getTags().containsKey(name);
	}
}
