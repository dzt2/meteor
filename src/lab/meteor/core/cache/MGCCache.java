package lab.meteor.core.cache;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

class MGCCache<T> {
	
	private static class MCacheReference<U> extends SoftReference<U> {

		long id;
		
		public MCacheReference(U referent, long id) {
			super(referent);
			this.id = id;
		}
		
		public MCacheReference(U referent, long id, ReferenceQueue<U> q) {
			super(referent, q);
			this.id = id;
		}
		
	}
	
	private Map<Long, MCacheReference<T>> cache;
	
	private ReferenceQueue<T> gcQueue;
	
	private static class Lock { }
	
	private Lock lock = new Lock();
	
	MGCCache() {
		cache = new HashMap<Long, MCacheReference<T>>();
		gcQueue = new ReferenceQueue<T>();
		ClearHandler ch = new ClearHandler();
		ch.setDaemon(true);
		ch.start();
	}
	
	public void add(long id, T e) {
		MCacheReference<T> r = new MCacheReference<T>(e, id, gcQueue);
		synchronized (lock) {
			cache.put(id, r);
		}
	}
	
	public void remove(long id) {
		synchronized (lock) {
			MCacheReference<T> r = cache.remove(id);
			if (r != null) {
				r.clear();
			}
		}
	}
	
	public T get(long id) {
		synchronized (lock) {
			MCacheReference<T> ref = cache.get(id);
			if (ref == null)
				return null;
			if (ref.get() == null) {
				cache.remove(id);
				return null;
			} else {
				T e = ref.get();
				return e;
			}
		}
	}
	
	public boolean contains(long id) {
		synchronized (lock) {
			return cache.containsKey(id);
		}
	}
	
	public void clear() {
		synchronized (lock) {
			Iterator<Entry<Long, MCacheReference<T>>> it = cache.entrySet().iterator();
			while (it.hasNext()) {
				MCacheReference<T> r = it.next().getValue();
				r.clear();
				it.remove();
			}
		}
	}
	
	public int size() {
		synchronized (lock) {
			return cache.size();
		}
	}
	
	private class ClearHandler extends Thread {
		
		public void run() {
			for(;;) {
				try {
					@SuppressWarnings("unchecked")
					MCacheReference<T> g = (MCacheReference<T>) gcQueue.remove();
					synchronized (lock) {
						cache.remove(g.id);
					}
				} catch (InterruptedException e) {
					/**
					 * do nothing
					 */
				}
			}
		};
	}
	
}
