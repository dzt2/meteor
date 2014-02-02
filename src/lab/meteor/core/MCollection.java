package lab.meteor.core;

import java.util.Iterator;
import java.util.Map;

/**
 * Base class of List, Set and Dictionary. The collection need notify the parent when it's
 * changed. The parent may be another collection or an object. The changed collection tells the
 * parent that it needs updating, and this message go along the hierarchy and finally the message
 * notify the object to update.
 * @author Qiang
 *
 */
public abstract class MCollection {
	
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
	 * The pointer of MObject that this collection belongs to.
	 */
	private MNotifiable root;
	
	/**
	 * The pointer of MAttribute.
	 */
	private MElementPointer attribute;
	
	/**
	 * Constructor.
	 * @param parent
	 */
	MCollection(MNotifiable root, MAttribute atb) {
		if (root == null)
			throw new MException(MException.Reason.NULL_NOTIFICABLE);
		this.root = root;
		this.attribute = new MElementPointer(atb);
	}
	
	MCollection(MCollection parent) {
		if (parent == null)
			throw new MException(MException.Reason.NULL_NOTIFICABLE);
		this.root = parent.root;
		this.attribute = parent.attribute;
	}
	
	public void notifyChanged() {
		root.setChanged(attribute);
	}
	
	/**
	 * A callback interface for doing something when traverse a collection.
	 * @author Qiang
	 *
	 */
	public static interface ForEachCallback {
		void action(Object key, Object o);
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
			return MCollection.createCollection((MCollection.Factory) o, root, 
					(MAttribute) attribute.getElement());
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
	static MCollection createCollection(Factory factory, 
			MNotifiable root, MAttribute atb) {
		switch (factory) {
		case List:
			return new MList(root, atb);
		case Set:
			return new MSet(root, atb);
		case Dictionary:
			return new MDictionary(root, atb);
		}
		return null;
	}
	
	static void fromDBObject(MCollection parent, Object value, Object key) {
		if (value instanceof MDBAdapter.DataList) {
			MList list = new MList(parent);
			MDBAdapter.DataList dl = (MDBAdapter.DataList) value;
			for (Object o : dl) {
				fromDBObject(list, o, null);
			}
			value = list;
		} else if (value instanceof MDBAdapter.DataSet) {
			MSet set = new MSet(parent);
			MDBAdapter.DataSet ds = (MDBAdapter.DataSet) value;
			for (Object o : ds) {
				fromDBObject(set, o, null);
			}
			value = set;
		} else if (value instanceof MDBAdapter.DataDict) {
			MDictionary dict = new MDictionary(parent);
			MDBAdapter.DataDict dd = (MDBAdapter.DataDict) value;
			Iterator<Map.Entry<String, Object>> it = dd.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, Object> entry = it.next();
				String k = entry.getKey();
				Object o = entry.getValue();
				fromDBObject(dict, o, k);
			}
			value = dict;
		}
		
		if (parent instanceof MList) {
			((MList) parent).list.add(value);
		} else if (parent instanceof MSet) {
			((MSet) parent).set.add(value);
		} else if (parent instanceof MDictionary) {
			((MDictionary) parent).dict.put((String)key, value);
		}
	}
	
	static Object toDBObject(Object value) {
		if (value instanceof MList) {
			MDBAdapter.DataList dl = new MDBAdapter.DataList();
			Iterator<Object> it = ((MList) value).list.iterator();
			while (it.hasNext()) {
				dl.add(toDBObject(it.next()));
			}
			return dl;
		} else if (value instanceof MSet) {
			MDBAdapter.DataSet ds = new MDBAdapter.DataSet();
			Iterator<Object> it = ((MSet) value).set.iterator();
			while (it.hasNext()) {
				ds.add(toDBObject(it.next()));
			}
			return ds;
		} else if (value instanceof MDictionary) {
			MDBAdapter.DataDict dd = new MDBAdapter.DataDict();
			Iterator<Map.Entry<String, Object>> it = ((MDictionary) value).dict.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, Object> entry = it.next();
				dd.put(entry.getKey(), toDBObject(entry.getValue()));
			}
			return dd;
		} else if (value instanceof MElement) {
			return new MElementPointer((MElement) value);
		} else {
			return value;
		}
	}
	
	@Override
	public String toString() {
		return this.toString(0);
	}
	
	public String toString(final int intent) {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < intent; i++) {
			sb.append("    ");
		}
		sb.append("{\n");
		forEach(new ForEachCallback() {

			@Override
			public void action(Object key, Object o) {
				for (int i = 0; i < intent + 1; i++) {
					sb.append("    ");
				}
				if (key != null)
					sb.append(key.toString()).append(" : ");
				if (o instanceof MCollection) {
					sb.append(((MCollection)o).toString(intent+1));
				} else
					sb.append(o.toString());
				sb.append("\n");
			}
			
		});
		for (int i = 0; i < intent; i++) {
			sb.append("    ");
		}
		sb.append("}");
		return sb.toString();
	}
}
