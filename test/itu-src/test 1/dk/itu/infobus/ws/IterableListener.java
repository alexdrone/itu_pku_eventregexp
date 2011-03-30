package dk.itu.infobus.ws;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An Iterable listener. Once registered, it will behave like a normal container of Map&lt;String,Object&gt;.
 * <br/>
 * Calling the <code>iterator()</code> method will return a custom <code>Iterator</code>, but beware that
 * calling the <code>Iterator.remove()</code> method will result in a <code>RuntimeException</code>, as it is not
 * possible to remove the event from the stream.
 * <br/>
 * Example:
 * <pre>
 * EventBus eb = ...
 * IteratorListener l = new IteratorListener(...);
 * eb.addListener(l);
 * for( Map&lt;String,Object&gt; evt : l ) {
 * 	//do something with the event
 * 	if( somecondition ) {
 * 		eb.remove(l);
 * 		//the next method after this call will return false,
 * 		//causing the exit from the for loop
 * 	}
 * }
 * 
 * </pre>
 * @author frza
 *
 */
public class IterableListener extends Listener implements
		Iterable<Map<String,Object>> {

	protected boolean live = true;
	protected List<Map<String,Object>> cache = new LinkedList<Map<String,Object>>();
	protected Object lock = new Object();
	protected AtomicBoolean isWaiting = new AtomicBoolean(false);
	
	public IterableListener(List<ListenerToken<?>> pattern) {
		super(pattern);
	}

	@Override
	public void cleanUp() throws Exception {
		//set live to false
		live = false;
		//and if we are blocked, notify the block object
		if(isWaiting.compareAndSet(true, false)) {
			synchronized(lock) {
				lock.notify();
			}
		}
	}

	@Override
	public void onMessage(Map<String, Object> msg) {
		//put the event in the cache
		synchronized(cache) {
			cache.add(msg);
		}
		//if we are blocked, notify the lock object
		if(isWaiting.compareAndSet(true, false)) {
			synchronized(lock) {
				lock.notify();
			}
		}
	}

	@Override
	public void onStarted() {
	}

	@Override
	public Iterator<Map<String, Object>> iterator() {
		return new IteratorListener();
	}
	
	class IteratorListener implements Iterator<Map<String,Object>> {
		
		/**
		 * Return true whenever the underlying listener is connected and has some events.
		 * Blocks if the event queue is empty, waiting for a new event or a disconnection.
		 */
		@Override
		public boolean hasNext() {
			//let's block on the hasNext method, so the "next" call will always have a consistent behavior.
			
			//if the listener is still live (not closed)
			if(live) {
				//if the cache has some unread events, return true of course
				synchronized(cache) {
					if(!cache.isEmpty()) {
						return true;
					}
				}
				//otherwise wait for the next event or disconnection
				isWaiting.set(true);
				synchronized(lock) {
					try {
						lock.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				//if we are here, either we received an event (and live is true, so it's ok),
				//or the listener has been disconnected (so live will be false, and no other events are available)
				return live;
			}
			//of course, if the listener is not live, we do not have any event.
			return false;
		}
		
		/**
		 * Return the next available event.
		 * If called after a disconnection, this method will thrown a RuntimeException
		 */
		@Override
		public Map<String, Object> next() {
			if(!live){throw new RuntimeException("Listener is not live, no next event!");}
			if(!hasNext()){ return null; }
			synchronized(cache) {
				return cache.remove(0);
			}
		}
		
		/**
		 * If this method is called, a RuntimeException will be thrown,
		 * as it is not possible to remove an event from the event-stream
		 */
		@Override
		public void remove() {
			throw new RuntimeException("Cannot remove an event from a stream!");
		}
	}
//	class IteratorListener implements Iterator<Map<String,Object>> {
//		@Override
//		public boolean hasNext() {
//			return live;
//		}
//
//		@Override
//		public Map<String, Object> next() {
//			synchronized(cache) {
//				if(!cache.isEmpty()) {
//					return cache.remove(0);
//				}
//			}
//			
//			isWaiting.set(true);
//			synchronized(lock) {
//				try {
//					lock.wait();
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//			
//			if(live) {
//				synchronized(cache) {
//					return cache.remove(0);
//				}
//			}
//			
//			return null;
//		}
//
//		@Override
//		public void remove() {
//			throw new RuntimeException("Cannot remove an event from a stream!");
//		}
//	}

}
