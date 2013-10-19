package lab.meteor.core;

/**
 * Base class of List, Set and Dictionary. The collection need notify the parent when it's
 * changed. The parent may be another collection or an object. The changed collection tells the
 * parent that it needs updating, and this message go along the hierarchy and finally the message
 * notify the object to update.
 * @author Qiang
 *
 */
public abstract class MCollection implements MNotifiable {
	
	/**
	 * A "factory" of collection instances. Pass it to <code>MCollection.createCollection()</code>
	 * to instantiate a collection class. <br>
	 * List : Instantiate <code>MList</code>, which actually acts like a linked list. <br>
	 * Set : Instantiate <code>MSet</code>, which actually acts like a hash set. <br>
	 * Dictionary : Instantiate <code>MDictionary</code>, which actually acts like 
	 * a hash set with string key. <br>
	 * @author Qiang
	 *
	 */
	public enum Factory {
		List,
		Set,
		Dictionary
	}
	
	/**
	 * Parent for pass the update message.
	 */
	private MNotifiable parent;
	
	/**
	 * Constructor.
	 * @param parent
	 */
	protected MCollection(MNotifiable parent) {
		if (parent == null)
			throw new MException(MException.Reason.NULL_NOTIFICABLE);
	}
	
	@Override
	public void notifyChanged() {
		this.parent.notifyChanged();
	}
	
	/**
	 * A callback interface for doing something when traverse a collection.
	 * @author Qiang
	 *
	 */
	public static interface ForEachCallback {
		void action(Object o);
	}
	
	/**
	 * Traverse the collection. Each type of collection must implement this method.
	 * @param callback the action when traverse collection
	 */
	public abstract void forEach(ForEachCallback callback);
	
	/**
	 * Convert a general type object to a inner type object.
	 * @param o the general object
	 * @return <code>MElementPointer</code> when object is <code>MElement</code>, 
	 * <code>MCollection</code> when object is <code>MCollection.Factory</code>.
	 */
	protected Object toInputObject(Object o) {
		if (o instanceof MElement) {
			return new MElementPointer((MElement) o);
		} else if (o instanceof MCollection.Factory) {
			return MCollection.createCollection((MCollection.Factory) o, this);
		}
		return o;
	}
	
	/**
	 * Convert a inner type object to general type object.
	 * @param o the inner type object
	 * @return <code>MElement</code> when object is <code>MElementPointer</code>
	 */
	protected Object toOutputObject(Object o) {
		if (o instanceof MElementPointer)
			return ((MElementPointer) o).getElement();
		return o;
	}
	
	/**
	 * Check whether a general object is an instance of a valid type.
	 * @param o
	 */
	protected static void checkType(Object o) {
		if (o instanceof MCollection.Factory)
			return;
		if (!MUtility.isValidValue(o))
			throw new MException(MException.Reason.INVALID_VALUE_TYPE);
	}
	
	/**
	 * Create collection with <code>MCollection.Factory</code>.
	 * @param factory
	 * @param parent collection's parent.
	 * @return <code>MList</code> when factory is <code>Factory.List</code>;
	 * <code>MSet</code> when factory is <code>Factory.Set</code>;
	 * <code>MDictionary</code> when factory is <code>Factory.Dictionary</code>.
	 */
	protected static MCollection createCollection(Factory factory, MNotifiable parent) {
		switch (factory) {
		case List:
			return new MList(parent);
		case Set:
			return new MSet(parent);
		case Dictionary:
			return new MDictionary(parent);
		}
		return null;
	}
}
