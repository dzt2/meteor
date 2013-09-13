package lab.meteor.core;

import java.util.Iterator;
import java.util.TreeSet;

public class MSet extends MCollection {

	final TreeSet<Object> set = new TreeSet<Object>();
	
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
			callback.function(o);
		}
	}

}
