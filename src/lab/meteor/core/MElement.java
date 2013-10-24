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
	 * The type of element.
	 */
	private MElementType type;
	
	/**
	 * The identifier.
	 */
	protected long id;
	
	/**
	 * The ID of null element.
	 */
	public static final long NULL_ID = 0x0000000000000000L;
	
	/**
	 * The prefix for presenting the element's ID.
	 */
	public static final char ID_PREFIX = '@';
	
	/**
	 * A state of element.
	 */
	private boolean loaded = false;
	
	/**
	 * A state of element.
	 */
	private boolean changed = false;
	
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
		this.changed = false;
		MDatabase.getDB().addElement(this);
	}
	
	/**
	 * If the element is loaded from database.
	 * @return
	 */
	boolean isLoaded() {
		return this.loaded;
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
	 * Load the content of element from database.
	 */
	public void load() {
		if (deleted)
			return;
		if (this.id == NULL_ID)
			return;
		MDatabase.getDB().loadElement(this);
		this.changed = false;
		this.loaded = true;
	}
	
	/**
	 * Save the content of element to database.
	 */
	public void save() {
		if (deleted)
			return;
		if (!loaded || !changed)
			return;
		if (this.id == NULL_ID)
			return;
		MDatabase.getDB().saveElement(this);
		this.changed = false;
	}
	
	/**
	 * Delete the element from database.
	 */
	public void delete() {
		if (deleted)
			return;
		if (this.id == NULL_ID)
			return;
		MDatabase.getDB().removeElement(this);
		MDatabase.getDB().deleteElement(this);
		this.deleted = true;
		this.changed = false;
	}
	
	/**
	 * If the element is deleted.
	 * @return <code>true</code> if deleted.
	 */
	public boolean isDeleted() {
		return this.deleted;
	}
	
	/**
	 * If the element is changed.
	 * @return <code>true</code> if changed.
	 */
	public boolean isChanged() {
		return this.changed;
	}
	
	/**
	 * Set element changed.
	 */
	void setChanged() {
		this.changed = true;
	}

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
	 * Load tags from database.
	 */
	public void loadTags() {
		if (deleted)
			return;
		if (this.id == NULL_ID)
			return;
		MDatabase.getDB().loadElementTags(this);
		this.changed_tags = false;
		this.loaded_tags = true;
	}
	
	/**
	 * Save tags to database.
	 */
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
	
	/**
	 * Add a tag of element.
	 * @param name Tag's name.
	 * @param id Tag's ID.
	 */
	void addTag(String name, long id) {
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
		Set<MElementPointer> tags = this.getTags().get(name);
		if (tags == null)
			return;
		tags.remove(new MElementPointer(id, MElementType.Tag));
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
		tag.addTarget(this.id, this.type);
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
		tag.removeTarget(this.id, this.type);
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
				tag.removeTarget(this.id, this.type);
		}
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
	public Set<String> getTagNames() {
		if (deleted)
			return null;
		if (this.id == NULL_ID)
			return null;
		if (!loaded_tags)
			this.loadTags();
		
		return new TreeSet<String>(this.getTags().keySet());
	}
	
	/**
	 * Get all tags with the specific name.
	 * @param name The name.
	 * @return Tags with specific name.
	 */
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
	 * Load tags from tag database information.
	 * @param dbInfo
	 */
	void loadTagsFromDBInfo(MDBAdapter.ElementTagDBInfo dbInfo) {
		for (Long tag_id : dbInfo.tags_id) {
			MTag tag = MDatabase.getDB().getTag(tag_id);
			if (tag == null)
				continue;
			addTag(tag.getName(), tag_id);
		}
	}
	
	/**
	 * Save tags to tag database information.
	 * @param dbInfo
	 */
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
