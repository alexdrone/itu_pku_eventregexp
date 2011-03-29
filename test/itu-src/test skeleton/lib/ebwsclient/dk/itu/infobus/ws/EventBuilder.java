package dk.itu.infobus.ws;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to build events (AKA <code>Map&lt;String,Object&gt;</code>).
 * Uses chaining to quickly building maps, e.g:
 * <code>
 * <pre>
 * Map&lt;String,Object&gt; evt = new EventBuilder()
 * 	.put("foo",10)
 * 	.put("bar","barrrr")
 * 	.getEvent():
 * </pre>
 * </code>
 * this class is not thread-safe.
 * @author frza
 *
 */
public class EventBuilder {

	Map<String,Object> evt;
	
	public EventBuilder() {
		evt = new HashMap<String,Object>();
	}
	
	public EventBuilder put(String name, Object value) {
		evt.put(name,value);
		return this;
	}
	
	public Map<String,Object> getEvent(){
		return evt;
	}
	
	public void reset() {
		evt = new HashMap<String,Object>();
	}
}
