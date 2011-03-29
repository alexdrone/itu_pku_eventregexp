package dk.itu.infobus.ws;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import dk.itu.infobus.ws.ListenerToken;

/**
 * Abstract class representing an event-listener.
 * After constructing a Listener, one can register it using
 * a dk.itu.infobus.ws.EventBus instance.
 * @author frza
 * @see dk.itu.infobus.ws.EventBus
 *
 */
public abstract class Listener {

	/**
	 * Server-assigned identifier
	 */
	protected String registration;
	/**
	 * Event pattern
	 */
	protected List<ListenerToken<?>> pattern;
	
	protected EventBus eb;
	
	/**
	 * Construct a new event listener.
	 * For your convenience, you can use the utility
	 * class dk.itu.infobus.ws.PatternBuilder class to
	 * create the pattern parameter
	 * @param pattern
	 * @see dk.itu.infobus.ws.PatternBuilder
	 */
	public Listener(List<ListenerToken<?>> pattern) {
		this.pattern = pattern;
	}
	
	protected void setEventBus(EventBus eb) {
		this.eb = eb;
	}
	
	/**
	 * Remove the listener from the server.
	 * After a call to this method, no events will
	 * be passed to this listener
	 * @throws IOException
	 */
	public void stop() throws IOException {
		this.eb.removeListener(registration);
	}
	
	/**
	 * Called when the client receive the server-assigned registration identifier.
	 * Useful for one-time initializations that needs to know the identity of the listener,
	 * or that needs the listener to be already connected to the server.
	 */
	public abstract void onStarted();
	/**
	 * Called whenever an event is received for this listener
	 * @param msg
	 */
	public abstract void onMessage(Map<String,Object> msg);
	/**
	 * Called when the listener has to be removed. Useful for freeing resources.
	 * @throws Exception
	 */
	public abstract void cleanUp() throws Exception;
	
}
