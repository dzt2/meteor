package lab.meteor.core;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public abstract class MElement {
	
	public static enum MElementType {
		Primitive,
		Class,
		Attribute,
		Reference,
		Enum,
		Symbol,
		Package,
		Object,
		Tag
	}
	
	private MElementType type;
	
	protected long id;
	
	public static final long NULL_ID = 0x0000000000000000L;
	
	public static final char ID_PREFIX = '@';
	
	private boolean loaded = false;
	
	private boolean changed = false;
	
	private boolean deleted = false;

	protected MElement(MElementType type) {
		this.type = type;
	}
	
	/**
	 * Create a "lazy" element by specific id.
	 * @param id
	 */
	protected MElement(long id, MElementType type) {
		this.id = id;
		this.type = type;
		MDatabase.getDB().addElement(this);
	}
	
	protected void initialize() throws MException {
		this.id = MDatabase.getDB().getNewID();
		this.loaded = true;
		this.changed = false;
		MDatabase.getDB().addElement(this);
	}
	
	boolean isLoaded() {
		return this.loaded;
	}
	
	public long getID() {
		return id;
	}
	
	public MElementType getElementType() {
		return this.type;
	}
	
	public void load() throws MException {
		if (deleted)
			return;
		if (this.id == NULL_ID)
			return;
		MDatabase.getDB().loadElement(this);
		this.changed = false;
		this.loaded = true;
	}
	
	public void save() throws MException {
		if (deleted)
			return;
		if (!loaded || !changed)
			return;
		if (this.id == NULL_ID)
			return;
		MDatabase.getDB().saveElement(this);
		this.changed = false;
	}
	
	public void delete() throws MException {
		if (deleted)
			return;
		if (this.id == NULL_ID)
			return;
		MDatabase.getDB().removeElement(this);
		MDatabase.getDB().deleteElement(this);
		this.deleted = true;
		this.changed = false;
	}
	
	public boolean isDeleted() {
		return this.deleted;
	}
	
	public boolean isChanged() {
		return this.changed;
	}
	
	void setChanged() {
		this.changed = true;
	}

	public static long getElementID(MElement element) {
		if (element == null)
			return MElement.NULL_ID;
		else
			return element.id;
	}
	
	private Map<String, Set<MElementPointer>> tags;
	
	private boolean loaded_tags = false;
	
	private boolean changed_tags = false;
	
	public void loadTags() {
		if (deleted)
			return;
		if (this.id == NULL_ID)
			return;
		MDatabase.getDB().loadElementTags(this);
		this.changed_tags = false;
		this.loaded_tags = true;
	}
	
	public void saveTags() {
		if (deleted)
			return;
		if (this.id == NULL_ID)
			return;
		if (!loaded_tags || !changed_tags)
			return;
		MDatabase.getDB().saveElementTags(this);
		this.changed_tags = false;
	}
	
	void addTag(String name, long id) {
		Set<MElementPointer> tags = this.getTags().get(name);
		if (tags == null) {
			tags = new TreeSet<MElementPointer>();
			this.getTags().put(name, tags);
		}
		tags.add(new MElementPointer(id, MElementType.Tag));
	}
	
	void removeTag(String name, long id) {
		Set<MElementPointer> tags = this.getTags().get(name);
		if (tags == null)
			return;
		tags.remove(new MElementPointer(id, MElementType.Tag));
	}
	
	public void addTag(MTag tag) {
		if (deleted)
			return;
		if (this.id == NULL_ID)
			return;
		if (!loaded_tags)
			this.loadTags();
		
		addTag(tag.getName(), tag.getID());
		tag.addTarget(this.id, this.type);
		this.changed_tags = true;
	}
	
	public void removeTag(MTag tag) {
		if (deleted)
			return;
		if (this.id == NULL_ID)
			return;
		if (!loaded_tags)
			this.loadTags();
		
		removeTag(tag.getName(), tag.getID());
		tag.removeTarget(this.id, this.type);
		this.changed_tags = true;
	}
	
	public void removeTags(String name) {
		if (deleted)
			return;
		if (this.id == NULL_ID)
			return;
		if (!loaded_tags)
			this.loadTags();
		
		Set<MElementPointer> tags = this.getTags().get(name);
		if (tags == null)
			return;
		for (MElementPointer tag_pt : tags) {
			MTag tag = (MTag) tag_pt.getElement();
			if (tag != null)
				tag.removeTarget(this.id, this.type);
		}
		this.getTags().remove(name);
		this.changed_tags = true;
	}
	
	public MTag getTag(String name) {
		if (deleted)
			return null;
		if (this.id == NULL_ID)
			return null;
		if (!loaded_tags)
			this.loadTags();
		
		Set<MElementPointer> tags = this.getTags().get(name);
		if (tags == null)
			return null;
		Iterator<MElementPointer> it = tags.iterator();
		if (it.hasNext()) {
			MElementPointer tag_pt = it.next();
			return (MTag) tag_pt.getElement();
		} else
			return null;
	}
	
	public Set<String> getTagNames() {
		if (deleted)
			return null;
		if (this.id == NULL_ID)
			return null;
		if (!loaded_tags)
			this.loadTags();
		
		return new TreeSet<String>(this.getTags().keySet());
	}
	
	public Set<MTag> getTags(String name) {
		if (deleted)
			return null;
		if (this.id == NULL_ID)
			return null;
		if (!loaded_tags)
			this.loadTags();
		
		if (!this.getTags().containsKey(name))
			return null;
		Set<MTag> tags = new TreeSet<MTag>();
		Set<MElementPointer> tag_pts = this.getTags().get(name);
		for (MElementPointer pt : tag_pts) {
			MTag tag = (MTag) pt.getElement();
			tags.add(tag);
		}
		return tags;
	}

	private Map<String, Set<MElementPointer>> getTags() {
		if (this.tags == null)
			this.tags = new TreeMap<String, Set<MElementPointer>>();
		return this.tags;
	}
	
	void loadTagsFromDBInfo(MDBAdapter.ElementTagDBInfo dbInfo) {
		for (Long tag_id : dbInfo.tags_id) {
			MTag tag = MDatabase.getDB().getTag(tag_id);
			if (tag == null)
				continue;
			addTag(tag.getName(), tag_id);
		}
	}
	
	void saveTagsFromDBInfo(MDBAdapter.ElementTagDBInfo dbInfo) {
		dbInfo.id = this.id;
		if (this.tags != null) {
			for (Set<MElementPointer> set : this.tags.values()) {
				for (MElementPointer pt : set) {
					dbInfo.tags_id.add(pt.getID());
				}
			}
		}
	}
	
	abstract void loadFromDBInfo(Object dbInfo);
	abstract void saveToDBInfo(Object dbInfo);
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MElement) {
			return ((MElement) obj).id == this.id;
		} else if (obj instanceof MElementPointer) {
			return ((MElementPointer) obj).getID() == this.id;
		}
		return false;
	}
}
