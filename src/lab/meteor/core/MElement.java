package lab.meteor.core;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import lab.meteor.core.MDBAdapter.DBInfo;

/**
 * The atomic record in system for create, update, delete and read. Conceptually, there are
 * two type of element, meta element and instance element. Meta elements present the data
 * model of system, and the instance element present the instance objects. <br>
 * <p>
 * The reason of putting the model and objects in a same abstract level is to support that
 * everything can be tagged. Every element can be tagged by tags. The tag is a special type
 * of element, which can attach the additional information to an element. So that tags can
 * also create a detailed, independent, structure-free relationship between the elements, 
 * which is difference from the relationships depending on classes' references. <br>
 * <p>
 * Meta element includes: <p>
 * <blockquote>
 *   <code>MClass</code>, 
 *   <code>MAttribute</code>, 
 *   <code>MReference</code>, 
 *   <code>MEnum</code>, 
 *   <code>MSymbol</code>, 
 *   <code>MPackage</code>
 * </blockquote>
 * <p>
 * Instance element includes: <p>
 * <blockquote>
 * <code>MObject</code>, <code>MTag</code>
 * </blockquote>
 * @author Qiang
 * @see MTag
 */
public abstract class MElement {
	
	/**
	 * The element type of MElement.
	 * @author Qiang
	 *
	 */
	public static enum MElementType {
		/**
		 * Meta type.
		 * @see MClass
		 */
		Class,
		/**
		 * Meta type.
		 * @see MAttribute
		 */
		Attribute,
		/**
		 * Meta type.
		 * @see MReference
		 */
		Reference,
		/**
		 * Meta type.
		 * @see MEnum
		 */
		Enum,
		/**
		 * Meta type.
		 * @see MSymbol
		 */
		Symbol,
		/**
		 * Meta type.
		 * @see MPackage
		 */
		Package,
		/**
		 * Meta type.
		 * @see MObject
		 */
		Object,
		/**
		 * Meta type.
		 * @see MTag
		 */
		Tag
	}
	
	/**
	 * The ID of null element.
	 */
	public static final long NULL_ID = 0x0000000000000000L;

	/**
	 * The prefix for presenting the element's ID.
	 */
	public static final char ID_PREFIX = '@';

	/**
	 * Get the ID of an element.
	 * @param element An element or <code>null</code>.
	 * @return <code>NULL_ID</code> if <code>null</code>, otherwise <code>MElement.getID()</code>
	 */
	public static long getElementID(MElement element) {
		if (element == null)
			return MElement.NULL_ID;
		else
			return element.id;
	}

	/**
	 * The identifier.
	 */
	protected long id;
	
	/**
	 * The type of element.
	 */
	private MElementType type;

	/**
	 * A state of element.
	 */
	private boolean loaded = false;
	
	/**
	 * A state of element.
	 */
	private int changed_flag = 0;
	
	/**
	 * A state of element.
	 */
	private boolean deleted = false;

	/**
	 * Create a element by element type.
	 * @param type The type of element.
	 */
	protected MElement(MElementType type) {
		this.type = type;
	}
	
	/**
	 * Create a "lazy" element by specific id.
	 * @param id The ID of element.
	 * @param type The type of element.
	 */
	protected MElement(long id, MElementType type) {
		this.id = id;
		this.type = type;
		MDatabase.getDB().addElement(this);
	}
	
	/**
	 * Initialize the element content, include allocating a new ID, initializing the
	 * states and store it into cache and database.
	 */
	protected void initialize() {
		this.id = MDatabase.getDB().getNewID();
		this.loaded = true;
		this.changed_flag = 0;
		MDatabase.getDB().addElement(this);
	}
	
	/**
	 * If the element is loaded from database.
	 * @return
	 */
	public boolean isLoaded() {
		return this.loaded;
	}
	
	/**
	 * If the element is changed.
	 * @return <code>true</code> if changed.
	 */
	public boolean isChanged() {
		return changed_flag == 0;
	}

	/**
	 * If the element is deleted.
	 * @return <code>true</code> if deleted.
	 */
	public boolean isDeleted() {
		return deleted;
	}

	/**
	 * The identifier of an element. It's a unique value in type of long.
	 * @return The ID.
	 */
	public long getID() {
		return id;
	}
	
	/**
	 * The type of element.
	 * @return The element type.
	 */
	public MElementType getElementType() {
		return this.type;
	}
	
	/**
	 * Load all attributes of element from database. If the element has been loaded once,
	 * there is no effect for calling this method.
	 */
	public void load() {
		if (!loaded)
			forceLoad();
	}
	
	/**
	 * 
	 * @param flag The flag that which attributes is going to be loaded.
	 */
	public void load(int flag) {
		if (!loaded)
			forceLoad(flag);
	}
	
	/**
	 * Forcibly load the content of element from database.
	 */
	public void forceLoad() {
		forceLoad(FULL_ATTRIB_FLAG);
		changed_flag = 0;
		loaded = true;
	}
	
	/**
	 * Forcibly load the specific attributes of element from database according to flag.
	 * @param flag The flag that which attributes is going to be loaded.
	 */
	public void forceLoad(int flag) {
		if (deleted)
			return;
		if (id == NULL_ID)
			return;
		MDatabase.getDB().loadElement(this, flag);
	}
	
	/**
	 * Save the changed attributes of element to database. If there is no change, there 
	 * is no effect for calling this method.
	 */
	public void save() {
		if (isChanged())
			forceSave(changed_flag);
	}
	
	/**
	 * Save the specific attributes of element to database according flag. If the flagged
	 * attribute has been changed, the attribute will be saved.
	 * @param flag The flag that which attributes is going to be saved.
	 */
	public void save(int flag) {
		if ((changed_flag & flag) != 0)
			forceSave(changed_flag & flag);
	}
	
	/**
	 * Forcibly save all attributes of element to database.
	 */
	public void forceSave() {
		forceSave(FULL_ATTRIB_FLAG);
		changed_flag = 0;
	}
	
	/**
	 * Forcibly save specific attributes of element to database according to the flag.
	 * @param flag The flag that which attributes is going to be saved.
	 */
	public void forceSave(int flag) {
		if (deleted)
			return;
		if (id == NULL_ID)
			return;
		if (!loaded)
			throw new MException(MException.Reason.FORBIDEN_SAVE_BEFORE_LOAD);
		MDatabase.getDB().saveElement(this, flag);
	}
	
	/**
	 * Delete the element from database.
	 */
	public void delete() {
		if (deleted)
			return;
		if (id == NULL_ID)
			return;
		
		loadTags();
		Iterator<MTag> it = tagIterator();
		while (it.hasNext()) {
			MTag tag = it.next();
			removeTag(tag);
			it.remove();
		}
		
		MDatabase.getDB().removeElement(this);
		MDatabase.getDB().deleteElement(this);
		deleted = true;
		changed_flag = 0;
	}
	
	/**
	 * Set element changed. It's necessary to call this method manually after making a
	 * modification of element content.
	 */
	protected void setChanged(int flag) {
//		changed_flag |= flag;
		forceSave(flag);
	}

	/**
	 * An iterator for iterate the tags.
	 * @return
	 */
	public Iterator<MTag> tagIterator() {
		getTags();
		return new TagItr();
	}
	
	/**
	 * An iterator for iterate the tags.
	 * @author Qiang
	 *
	 */
	private class TagItr implements Iterator<MTag> {
		private final Iterator<Map.Entry<String, Set<MElementPointer>>> mapit;
		private Iterator<MElementPointer> setit;
		private MElementPointer current;
		
		TagItr() {
			mapit = MElement.this.tags.entrySet().iterator();
			setit = null;
		}
		
		@Override
		public boolean hasNext() {
			if (setit == null || !setit.hasNext()) {
				if (mapit.hasNext())
					setit = mapit.next().getValue().iterator();
				else
					setit = null;
			}
			if (setit != null)
				return setit.hasNext();
			return false;
		}

		@Override
		public MTag next() {
			if (setit == null || !setit.hasNext()) {
				if (mapit.hasNext())
					setit = mapit.next().getValue().iterator();
				else
					setit = null;
			}
			if (setit != null) {
				current = setit.next();
				return (MTag) current.getElement();
			}
			current = null;
			return null;
		}

		@Override
		public void remove() {
			setit.remove();
			current.getElement().delete();
		}
		
	}
	
	/**
	 * The tags.
	 */
	private Map<String, Set<MElementPointer>> tags;
	
	/**
	 * A state of tags.
	 */
	private boolean loaded_tags = false;
	
	/**
	 * A state of tags.
	 */
	private boolean changed_tags = false;
	
	/**
	 * Forcibly load tags from database.
	 */
	public void forceLoadTags() {
		if (deleted)
			return;
		if (this.id == NULL_ID)
			return;
		MDatabase.getDB().loadElementTags(this);
		changed_tags = false;
		loaded_tags = true;
	}
	
	/**
	 * Load tags from database. If it has been loaded once, there is no effect
	 * for calling this method.
	 */
	public void loadTags() {
		if (!loaded_tags)
			forceLoadTags();
	}
	
	/**
	 * Forcibly save tags to database.
	 */
	public void forceSaveTags() {
		if (deleted)
			return;
		if (this.id == NULL_ID)
			return;
		if (!loaded_tags)
			throw new MException(MException.Reason.FORBIDEN_SAVE_BEFORE_LOAD);
		MDatabase.getDB().saveElementTags(this);
		changed_tags = false;
	}
	
	/**
	 * Save tags to database. If tags of element have not been loaded or there
	 * is no change, there is no effect for calling this method.
	 */
	public void saveTags() {
		if (changed_tags)
			forceSaveTags();
	}
	
	/**
	 * Add a tag of element.
	 * @param tag The tag.
	 */
	public void addTag(MTag tag) {
		if (deleted)
			return;
		if (this.id == NULL_ID)
			return;
		if (!loaded_tags)
			this.loadTags();
		
		addTag(tag.getName(), tag.getID());
		tag.addElement(this);
		this.changed_tags = true;
	}
	
	/**
	 * Remove a tag of element.
	 * @param tag The tag.
	 */
	public void removeTag(MTag tag) {
		if (deleted)
			return;
		if (this.id == NULL_ID)
			return;
		if (!loaded_tags)
			this.loadTags();
		
		removeTag(tag.getName(), tag.getID());
		tag.removeElement(this);
		this.changed_tags = true;
	}
	
	/**
	 * Remove all tags with a common name.
	 * @param name The common name.
	 */
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
				tag.removeElement(this);
		}
		tags.clear();
		this.getTags().remove(name);
		this.changed_tags = true;
	}
	
	/**
	 * Get a tag with the specific name.
	 * @param name The name.
	 * @return Tag with specific name.
	 */
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
	
	/**
	 * Get all tags' names.
	 * @return Tags' names.
	 */
	public String[] getTagNames() {
		if (deleted)
			return null;
		if (this.id == NULL_ID)
			return null;
		if (!loaded_tags)
			this.loadTags();
		
		return this.getTags().keySet().toArray(new String[0]);
	}
	
	/**
	 * Get all tags with the specific name.
	 * @param name The name.
	 * @return Tags with specific name.
	 */
	public MTag[] getTags(String name) {
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
		return tags.toArray(new MTag[0]);
	}

	/**
	 * Get all tags, which labeled by names.
	 * @return The map from name to tags.
	 */
	private Map<String, Set<MElementPointer>> getTags() {
		if (this.tags == null)
			this.tags = new TreeMap<String, Set<MElementPointer>>();
		return this.tags;
	}

	/**
	 * Add a tag of element.
	 * @param name Tag's name.
	 * @param id Tag's ID.
	 */
	void addTag(String name, long id) {
		if (name == null)
			return;
		Set<MElementPointer> tags = this.getTags().get(name);
		if (tags == null) {
			tags = new TreeSet<MElementPointer>();
			this.getTags().put(name, tags);
		}
		tags.add(new MElementPointer(id, MElementType.Tag));
	}

	/**
	 * Remove a tag of element.
	 * @param name Tag's name.
	 * @param id Tag's ID.
	 */
	void removeTag(String name, long id) {
		if (name == null)
			return;
		Set<MElementPointer> tags = this.getTags().get(name);
		if (tags == null)
			return;
		tags.remove(new MElementPointer(id, MElementType.Tag));
	}

	/**
	 * Load tags from tag database information.
	 * @param dbInfo
	 */
	void loadTagsFromDBInfo(MDBAdapter.IDList idList) {
		for (Long tag_id : idList) {
			MTag tag = MDatabase.getDB().getTag(tag_id);
			if (tag == null)
				continue;
			tag.preloadName();
			addTag(tag.getName(), tag_id);
		}
	}
	
	/**
	 * Save tags to tag database information.
	 * @param dbInfo
	 */
	void saveTagsToDBInfo(MDBAdapter.IDList idList) {
		if (this.tags != null) {
			for (Set<MElementPointer> set : this.tags.values()) {
				for (MElementPointer pt : set) {
					idList.add(pt.getID());
				}
			}
		}
	}
	
	/**
	 * Load the content from a particular type of object.
	 * @param dbInfo The object containing the content of element.
	 */
	abstract void loadFromDBInfo(DBInfo dbInfo);
	
	/**
	 * Save the content to a particular type of object.
	 * @param dbInfo The object to contain the content of element.
	 */
	abstract void saveToDBInfo(DBInfo dbInfo);
	
	/**
	 * An element can equal to its pointer.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MElement) {
			return ((MElement) obj).id == this.id;
		} else if (obj instanceof MElementPointer) {
			return ((MElementPointer) obj).getID() == this.id;
		}
		return false;
	}
	
	public static final int FULL_ATTRIB_FLAG = 0xFFFFFFFF;
}
