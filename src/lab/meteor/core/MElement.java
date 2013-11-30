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
public abstract class MElement implements Comparable<MElement> {
	
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
	protected boolean loaded = false;
	
	/**
	 * A state of element.
	 */
	protected int changed_flag = 0;
	
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
		MDatabase.getDB().addElementInCache(this);
	}
	
	/**
	 * Initialize the element content, include allocating a new ID, initializing the
	 * states and store it into cache and database.
	 */
	protected void initialize() {
		this.id = MDatabase.getDB().getNewID();
		this.loaded = true;
		this.changed_flag = 0;
		MDatabase.getDB().addElementInCache(this);
	}
	
	@Override
	protected void finalize() throws Throwable {
		save();
		saveTags();
		super.finalize();
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
		return changed_flag != 0;
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
		if (deleted || id == NULL_ID)
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
		if ((changed_flag & flag) != 0) {
			forceSave(changed_flag & flag);
			changed_flag &= ~flag;
		}
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
		if (deleted || id == NULL_ID)
			return;
		if (!loaded)
			throw new MException(MException.Reason.FORBIDEN_SAVE_BEFORE_LOAD);
		MDatabase.getDB().saveElement(this, flag);
	}
	
	/**
	 * Delete the element from database.
	 */
	public void delete() {
		if (deleted || id == NULL_ID)
			return;
		
		loadTags();
		Iterator<String> it = getTags().keySet().iterator();
		while (it.hasNext()) {
			String name = it.next();
			cleanTags(name);
			it.remove();
		}
		
		MDatabase.getDB().removeElementInCache(this);
		MDatabase.getDB().deleteElement(this);
		deleted = true;
		changed_flag = 0;
	}
	
	/**
	 * Set element changed. It's necessary to call this method manually after making a
	 * modification of element content.
	 */
	protected void setChanged(int flag) {
		changed_flag |= flag;
		if (MDatabase.getDB().isAutoSave()) {
			MDatabase.getDB().autoSave(this);
		}
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
		private final Iterator<Map.Entry<String, MTagSet>> mapit;
		private Iterator<MTag> setit;
		
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
				return setit.next();
			}
			return null;
		}

		@Override
		public void remove() {
			setit.remove();
		}
		
	}
	
	public class MTagSet implements Iterable<MTag> {
	
		Set<MElementPointer> pointers = new TreeSet<MElementPointer>();
		final String name;
		
		private MTagSet(String name) {
			this.name = name;
		}
		
		public void add(MTag tag) {
			if (deleted || id == NULL_ID || tag == null || tag.isDeleted())
				return;
			if (!tag.name.equals(name))
				return;
			pointers.add(new MElementPointer(tag));
			MElement.this.tagsChanged();
		}
	
		public void remove(MTag tag) {
			if (deleted || id == NULL_ID || tag == null || tag.isDeleted())
				return;
			if (!tag.name.equals(name))
				return;
			pointers.remove(new MElementPointer(tag));
			MElement.this.tagsChanged();
		}
		
		public void clear() {
			if (deleted || id == NULL_ID)
				return;
			for (MElementPointer pt : pointers) {
				MTag tag = (MTag) pt.getElement();
				if (tag != null && !tag.isDeleted())
					tag.removeElement(MElement.this);
			}
			pointers.clear();
			MElement.this.tagsChanged();
		}
		
		public boolean contains(MTag tag) {
			if (deleted || id == NULL_ID)
				return false;
			return pointers.contains(new MElementPointer(tag));
		}
		
		public boolean isEmpty() {
			if (deleted || id == NULL_ID)
				return true;
			return pointers.isEmpty();
		}
		
		public int size() {
			if (deleted || id == NULL_ID)
				return 0;
			return pointers.size();
		}
		
		@Override
		public Iterator<MTag> iterator() {
			return new Itr();
		}
		
		private class Itr implements Iterator<MTag> {
			
			final Iterator<MElementPointer> it = pointers.iterator();
			MElementPointer last;
			
			@Override
			public boolean hasNext() {
				if (deleted || id == NULL_ID)
					return false;
				return it.hasNext();
			}
	
			@Override
			public MTag next() {
				if (deleted || id == NULL_ID)
					return null;
				last = it.next();
				MTag tag = (MTag) last.getElement();
				boolean isChanged = false;
				while (tag == null || tag.isDeleted()) {
					it.remove();
					last = it.next();
					tag = (MTag) last.getElement();
					isChanged = true;
				}
				if (isChanged)
					MElement.this.tagsChanged();
				return tag;
			}
	
			@Override
			public void remove() {
				if (deleted || id == NULL_ID)
					return;
				MTag tag = (MTag) last.getElement();
				tag.removeElement(MElement.this);
				it.remove();
			}
			
		}
		
	}

	/**
	 * The tags.
	 */
	private Map<String, MTagSet> tags;
	
	/**
	 * A state of tags.
	 */
	private boolean loaded_tags = false;
	
	/**
	 * A state of tags.
	 */
	private boolean changed_tags = false;
	
	private void tagsChanged() {
		changed_tags = true;
		if (MDatabase.getDB().isAutoSave()) {
			MDatabase.getDB().autoSaveTags(this);
		}
	}
	
	/**
	 * Forcibly load tags from database.
	 */
	public void forceLoadTags() {
		if (deleted || id == NULL_ID)
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
		if (deleted || id == NULL_ID)
			return;
		if (!loaded_tags)
			throw new MException(MException.Reason.FORBIDEN_SAVE_BEFORE_LOAD);
		
		// TODO save the "elements" attributes of its tags.
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
		if (deleted || id == NULL_ID || tag == null || tag.isDeleted())
			return;
		
		loadTags();
		addTag(tag.getName(), tag.getID());
		tag.addElement(this);
	}
	
	/**
	 * Remove a tag of element.
	 * @param tag The tag.
	 */
	public void removeTag(MTag tag) {
		if (deleted || id == NULL_ID || tag == null || tag.isDeleted())
			return;
		
		loadTags();
		removeTag(tag.getName(), tag.getID());
		tag.removeElement(this);
	}
	
	/**
	 * Remove all tags with a common name.
	 * @param name The common name.
	 */
	public void cleanTags(String name) {
		if (deleted || id == NULL_ID || name == null)
			return;
		loadTags();
		
		MTagSet tags = this.getTags().get(name);
		if (tags == null)
			return;
		for (MElementPointer pt : tags.pointers) {
			MTag tag = (MTag) pt.getElement();
			if (tag != null && !tag.isDeleted())
				tag.removeElement(this);
		}
		tags.pointers.clear();
		tagsChanged();
	}
	
	/**
	 * Get a tag with the specific name. If there are more than 2 tags, return null.
	 * @param name The name.
	 * @return Tag with specific name.
	 */
	public MTag tag(String name) {
		if (deleted || id == NULL_ID)
			return null;
		loadTags();
		
		MTagSet tags = getTags().get(name);
		// TODO
		if (tags == null || tags.size() != 1)
			return null;
		Iterator<MTag> it = tags.iterator();
		MTag tag = it.next();
		if (tag == null) {
			it.remove();
			getTags().remove(name);
		}
		tagsChanged();
		return tag;
	}
	
	/**
	 * Get all tags' names.
	 * @return Tags' names.
	 */
	public String[] tagNames() {
		if (deleted || id == NULL_ID)
			return null;
		loadTags();
		
		return this.getTags().keySet().toArray(new String[0]);
	}
	
	/**
	 * Get all tags with the specific name.
	 * @param name The name.
	 * @return Tags with specific name.
	 */
	public MTagSet tags(String name) {
		if (deleted || id == NULL_ID)
			return null;
		loadTags();
		
		MTagSet tags = this.getTags().get(name);
		if (tags == null) {
			tags = new MTagSet(name);
			this.getTags().put(name, tags);
		}
		return tags;
	}

	/**
	 * Get all tags, which labeled by names.
	 * @return The map from name to tags.
	 */
	private Map<String, MTagSet> getTags() {
		if (this.tags == null)
			this.tags = new TreeMap<String, MTagSet>();
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
		MTagSet tags = this.getTags().get(name);
		if (tags == null) {
			tags = new MTagSet(name);
			this.getTags().put(name, tags);
		}
		tags.pointers.add(new MElementPointer(id, MElementType.Tag));
		tagsChanged();
	}

	/**
	 * Remove a tag of element.
	 * @param name Tag's name.
	 * @param id Tag's ID.
	 */
	void removeTag(String name, long id) {
		if (name == null)
			return;
		MTagSet tags = this.getTags().get(name);
		if (tags == null)
			return;
		tags.pointers.remove(new MElementPointer(id, MElementType.Tag));
		tagsChanged();
	}

	/**
	 * Load tags from tag database information.
	 * @param dbInfo
	 */
	void loadTagsFromDBInfo(MDBAdapter.IDList idList) {
		for (Long id : idList) {
			MTag tag = MDatabase.getDB().getLazyTag(id);
			if (tag == null)
				continue;
			tag.preloadName();
			addTag(tag.getName(), id);
		}
	}
	
	/**
	 * Save tags to tag database information.
	 * @param dbInfo
	 */
	void saveTagsToDBInfo(MDBAdapter.IDList idList) {
		if (this.tags != null) {
			for (MTagSet set : this.tags.values()) {
				for (MElementPointer pt : set.pointers) {
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
	
	public abstract String details();
	
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
	
	@Override
	public int compareTo(MElement o) {
		return Long.compare(id, o.id);
	}
	
	public static final int FULL_ATTRIB_FLAG = 0xFFFFFFFF;
}
