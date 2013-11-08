package lab.meteor.core.cache;

import java.util.HashMap;
import java.util.Map;

import lab.meteor.core.MElement;
import lab.meteor.core.MObject;
import lab.meteor.core.MTag;
import lab.meteor.core.MElement.MElementType;

public class MCaches {

	/**
	 * The meta of system, i.e. the model, include class, attribute, reference, 
	 * enum and symbol(enumeration literal).
	 */
	private Map<Long, MElement> metaElements;
	
	/**
	 * The objects' cache.
	 */
	private MGCCache<MObject> objectsCache;
	
	/**
	 * The tags' cache.
	 */
	private MGCCache<MTag> tagsCache;
	
	public MCaches() {
		metaElements = new HashMap<Long, MElement>();
		objectsCache = new MGCCache<MObject>();
		tagsCache = new MGCCache<MTag>();
	}
	
	/**
	 * Get a meta element with id.
	 * @param id
	 * @return the model, which could be class, attribute, reference, enum
	 * and symbol(enumeration literal).
	 */
	public MElement getMetaElement(long id) {
		return metaElements.get(id);
	}
	
	/**
	 * Get an object with id.
	 * @param id
	 * @return the object.
	 */
	public MObject getObjectElement(long id) {
		return objectsCache.get(id);
	}
	
	/**
	 * Get a tag with id.
	 * @param id
	 * @return the tag.
	 */
	public MTag getTagElement(long id) {
		return tagsCache.get(id);
	}
	
	/**
	 * Check that an element has been loaded into cache.
	 * @param id
	 * @return true if element with specific id is in cache.
	 */
	public boolean isElementInCache(long id) {
		if (metaElements.containsKey(id))
			return true;
		if (objectsCache.contains(id))
			return true;
		if (tagsCache.contains(id))
			return true;
		return false;
	}
	
	/**
	 * Get an element in cache.
	 * @param id
	 * @return
	 */
	public MElement getElement(long id) {
		MElement ele = metaElements.get(id);
		if (ele == null)
			ele = objectsCache.get(id);
		if (ele == null)
			ele = tagsCache.get(id);
		return ele;
	}
	
	/**
	 * Add an element into system cache.
	 * @param meta
	 */
	public void addElement(MElement ele) {
		if (ele.getElementType() == MElementType.Object)
			objectsCache.add(ele.getID(), (MObject) ele);
		else if (ele.getElementType() == MElementType.Tag)
			tagsCache.add(ele.getID(), (MTag) ele);
		else
			metaElements.put(ele.getID(), ele);
	}
	
	/**
	 * Remove an element from system cache.
	 * @param meta
	 */
	public void removeElement(MElement ele) {
		if (ele instanceof MObject)
			objectsCache.remove(ele.getID());
		else if (ele instanceof MTag)
			tagsCache.remove(ele.getID());
		else
			metaElements.remove(ele.getID());
	}
	
	public void clear() {
		metaElements.clear();
		objectsCache.clear();
		tagsCache.clear();
	}
	
}
