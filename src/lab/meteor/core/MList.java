package lab.meteor.core;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

public class MList extends MCollection {

	MList(MNotifiable parent) {
		super(parent);
	}

	final LinkedList<Object> list = new LinkedList<Object>();
	
	public boolean add(Object e) {
		checkType(e);
		
		e = toInputObject(e);
		boolean b = this.list.add(e);
		if (b)
			this.notifyChanged();
		return b;
	}

	public void add(int index, Object element) {
		checkType(element);
		
		element = toInputObject(element);
		list.add(index, element);
		this.notifyChanged();
	}

	public boolean remove(Object o) {
		o = toInputObject(o);
		boolean b = list.remove(o);
		if (b)
			this.notifyChanged();
		return b;
	}

	public Object remove(int index) {
		Object o = list.remove(index);
		this.notifyChanged();
		return o;
	}

	public Object set(int index, Object element) {
		checkType(element);
		
		element = toInputObject(element);
		Object o = list.set(index, element);
		this.notifyChanged();
		return o;
	}

	public Object get(int index) {
		Object o = list.get(index);
		o = toOutputObject(o);
		return o;
	}

	public void clear() {
		list.clear();
		this.notifyChanged();
	}

	public boolean contains(Object o) {
		o = toInputObject(o);
		return list.contains(o);
	}

	public int indexOf(Object o) {
		o = toInputObject(o);
		return list.indexOf(o);
	}

	public int lastIndexOf(Object o) {
		o = toInputObject(0);
		return list.lastIndexOf(o);
	}

	public boolean isEmpty() {
		return list.isEmpty();
	}

	public int size() {
		return list.size();
	}

	public Iterator<Object> iterator() {
		return listIterator();
	}

	public ListIterator<Object> listIterator() {
		return listIterator(0);
	}

	public ListIterator<Object> listIterator(int index) {
		return new ListItr(index);
	}

	private class ListItr implements ListIterator<Object> {
		private final ListIterator<Object> it;
		
		public ListItr(int index) {
			this.it = MList.this.list.listIterator(index);
		}
		
		@Override
		public void add(Object e) {
			checkType(e);
			
			e = MList.this.toInputObject(e);
			it.add(e);
			MList.this.notifyChanged();
		}

		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		@Override
		public boolean hasPrevious() {
			return it.hasPrevious();
		}

		@Override
		public Object next() {
			Object o = it.next();
			o = MList.this.toOutputObject(o);
			return o;
		}

		@Override
		public int nextIndex() {
			return it.nextIndex();
		}

		@Override
		public Object previous() {
			Object o = it.previous();
			o = MList.this.toOutputObject(o);
			return o;
		}

		@Override
		public int previousIndex() {
			return it.previousIndex();
		}

		@Override
		public void remove() {
			it.remove();
			MList.this.notifyChanged();
		}

		@Override
		public void set(Object e) {
			checkType(e);
			
			e = MList.this.toInputObject(e);
			it.add(e);
			MList.this.notifyChanged();
		}
		
	}

	@Override
	public void forEach(ForEachCallback callback) {
		if (callback == null)
			return;
		for (Object o : list) {
			o = toOutputObject(o);
			callback.function(o);
		}
	}
	
}
