package lab.meteor.core;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * The set, one of a primitive type in meteor system. When there is a modify operation, such
 * as <code>add()</code>, <code>remove()</code>, etc., or a modify operation activated by
 * its iterator, the set will notify its parent there is a change needed to be updated.
 * <p>
 * The set can only be created by the factory method <code>MCollection.createCollection()</code>.
 * <p>
 * It's a wrapper of <code>HashSet&ltObject&gt</code>.
 * @author Qiang
 *
 */
public class MSet extends MCollection implements Iterable<Object> {

	final Set<Object> set = new HashSet<Object>();
	
	MSet(MNotifiable parent) {
		super(parent);
	}

	public boolean add(Object e) {
		e = toInputObject(e);
		boolean b = set.add(e);
		if (b)
			this.notifyChanged();
		return b;
	}

	public boolean remove(Object o) {
		checkType(o);
		
		o = toInputObject(o);
		boolean b = set.remove(o);
		if (b)
			this.notifyChanged();
		return b;
	}

	public void clear() {
		this.clear();
		this.notifyChanged();
	}

	public boolean contains(Object o) {
		o = toInputObject(o);
		return set.contains(o);
	}

	public boolean isEmpty() {
		return set.isEmpty();
	}

	public int size() {
		return set.size();
	}

	@Override
	public Iterator<Object> iterator() {
		return new SetItr();
	}
	
	private class SetItr implements Iterator<Object> {

		private final Iterator<Object> it;
		
		public SetItr() {
			it = MSet.this.set.iterator();
		}
		
		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		@Override
		public Object next() {
			Object o = it.next();
			o = toOutputObject(o);
			return o;
		}

		@Override
		public void remove() {
			it.remove();
			MSet.this.notifyChanged();
		}
		
	}

	@Override
	public void forEach(ForEachCallback callback) {
		for (Object o : set) {
			o = toOutputObject(o);
			callback.action(null, o);
		}
	}

}
