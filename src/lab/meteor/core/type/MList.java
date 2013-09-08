package lab.meteor.core.type;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class MList implements List<Object> {

private LinkedList<Object> list = new LinkedList<Object>();
	
	public MList() {
	}
	
	@Override
	public boolean add(Object e) {
//		if (!validValue(e))
//			throw new MException(MException.Reason.INVALID_VALUE_TYPE);
		// TODO
		return list.add(e);
	}

	@Override
	public void add(int index, Object element) {
//		if (!validValue(element))
//			throw new MException(MException.Reason.INVALID_VALUE_TYPE);
		// TODO
		list.add(index, element);
	}

	@Override
	public boolean addAll(Collection<? extends Object> c) {
//		for (Object o : c) {
//			if (validValue(o))
//				list.add(o);
//		}
		// TODO
		return true;
	}

	@Override
	public boolean addAll(int index, Collection<? extends Object> c) {
//		List<Object> objs = new LinkedList<Object>();
//		for (Object o : c) {
//			if (validValue(o))
//				objs.add(o);
//		}
		// TODO
//		return list.addAll(index, objs);
		return false;
	}

	@Override
	public void clear() {
		list.clear();
	}

	@Override
	public boolean contains(Object o) {
		return list.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return list.containsAll(c);
	}

	@Override
	public Object get(int index) {
		return list.get(index);
	}

	@Override
	public int indexOf(Object o) {
		return list.indexOf(o);
	}

	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	public Iterator<Object> iterator() {
		return list.iterator();
	}

	@Override
	public int lastIndexOf(Object o) {
		return list.lastIndexOf(o);
	}

	@Override
	public ListIterator<Object> listIterator() {
		return list.listIterator();
	}

	@Override
	public ListIterator<Object> listIterator(int index) {
		return list.listIterator(index);
	}

	@Override
	public boolean remove(Object o) {
		return list.remove(o);
	}

	@Override
	public Object remove(int index) {
		return list.remove(index);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return list.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return list.retainAll(c);
	}

	@Override
	public Object set(int index, Object element) {
//		if (!validValue(element))
//			throw new MException(MException.Reason.INVALID_VALUE_TYPE);
		// TODO
		return list.set(index, element);
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public List<Object> subList(int fromIndex, int toIndex) {
		return list.subList(fromIndex, toIndex);
	}

	@Override
	public Object[] toArray() {
		return list.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return list.toArray(a);
	}
	
}
