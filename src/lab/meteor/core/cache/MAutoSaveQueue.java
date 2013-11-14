package lab.meteor.core.cache;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public abstract class MAutoSaveQueue<T> {
	
	long saveCycleMillis;
	
	Queue<T> queue = new LinkedList<T>();
	
	Set<T> elements = new HashSet<T>();
	
	private static class Lock { }
	
	private Lock lock = new Lock();
	
	private AutoSaveHandler handler;
	
	private boolean enable = false;
	
	public MAutoSaveQueue() {
		this(0);
	}
	
	public MAutoSaveQueue(long saveCycleMillis) {
		this.saveCycleMillis = saveCycleMillis;
	}
	
	public long getSaveCycle() {
		return this.saveCycleMillis;
	}
	
	public void setSaveCycle(long saveCycleMillis) {
		this.saveCycleMillis = saveCycleMillis;
	}
	
	public boolean isEnable() {
		return enable;
	}
	
	public void setEnable(boolean enable) {
		if (this.enable != enable) {
			if (enable) {
				handler = new AutoSaveHandler();
				handler.start();
			} else {
				handler.disable();
				handler = null;
			}
		}
		this.enable = enable;
	}
	
	public void offer(T e) {
		synchronized (lock) {
			if (elements.contains(e))
				return;
			elements.add(e);
			queue.offer(e);
			lock.notify();
		}
	}
	
	private void remove() {
		synchronized (lock) {
			if (queue.size() == 0) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					/*
					 * do nothing
					 */
				}
			}
			// save all elements in queue
			while (queue.size() != 0) {
				T e = queue.remove();
				elements.remove(e);
				save(e);
			}
		}
	}
	
	protected abstract void save(T e);
	
	private class AutoSaveHandler extends Thread {
		
		boolean enable = true;
		
		AutoSaveHandler() {
			super("Meteor-AutoSave-Handler");
			setDaemon(true);
		}
		
		public void disable() {
			enable = false;
		}
		
		@Override
		public void run() {
			for (;enable;) {
				if (saveCycleMillis != 0) {
					try {
						// waiting for interval
						Thread.sleep(saveCycleMillis);
					} catch (InterruptedException e1) {
						/*
						 * do nothing
						 */
					}
				}
				remove();
			}
		}
	}
}
