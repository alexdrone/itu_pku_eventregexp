package dk.itu.infobus.ws;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Abstract class representing an event generator.
 * After constructing a generator, one can register it using
 * a dk.itu.infobus.ws.EventBus instance.
 * @author frza
 * @see dk.itu.infobus.ws.EventBus
 *
 */
public abstract class Generator {

	/**
	 * The user-assigned name of the generator
	 */
	protected String name;
	/**
	 * The names of the fields in the generated events
	 */
	protected List<String> fields;
	
	protected EventBus eb;
	
	/**
	 * Construct a new generator
	 * @param name the given name
	 * @param fields the list of fields that this generators' events will contains
	 */
	public Generator(String name, List<String> fields) {
		this.name = name;
		this.fields = fields;
	}
	/**
	 * Construct a new generator
	 * @param name the given name
	 * @param fields the list of fields that this generators' events will contains
	 */
	public Generator(String name, String...fields) {
		this.name = name;
		this.fields = Arrays.asList(fields);
	}
	
	protected void setEventBus(EventBus eb) {
		this.eb = eb;
	}
	
	/**
	 * Called when an event is received from the server
	 * @param evt
	 */
	public void onMessage(Map<String,Object> evt) {
		try {
			if(evt.containsKey("system")) {
				String sys = (String)evt.get("system");
				if("activate".equals(sys)){
					onActivationRequest(evt);
				} else if("deactivate".equals(sys)) {
					onDeactivationRequest(evt);
				} else {
					onSystemEvent(evt);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * When this generator wants to publish a new event,
	 * call this method.
	 * @param evt
	 */
	public void publish(Map<String,Object> evt) {
		try {
			this.eb.publish(evt);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.eb.onException(e);
		}
	}
	
	/**
	 * Remove the generator from the server
	 * @throws IOException
	 */
	public void stop() throws IOException {
		this.eb.removeGenerator(name);
	}
	
	/**
	 * Called when the generator is stopped.
	 * @throws Exception
	 */
	protected abstract void cleanUp() throws Exception;
	
	/**
	 * Called when the server figures out that this generator has no
	 * associated listeners
	 * @param evt
	 * @throws Exception
	 */
	protected abstract void onDeactivationRequest(Map<String,Object> evt) throws Exception;
	/**
	 * Called when the server figures out that this generator has
	 * some associated listeners
	 * @param evt
	 * @throws Exception
	 */
	protected abstract void onActivationRequest(Map<String,Object> evt) throws Exception;
	/**
	 * Called when the generator has been registered to the server
	 * @throws Exception
	 */
	protected abstract void onStarted() throws Exception;
	/**
	 * Called when the an unidentified event has been received for this generator.
	 * @param evt
	 * @throws Exception
	 */
	protected abstract void onSystemEvent(Map<String,Object> evt) throws Exception;
}
