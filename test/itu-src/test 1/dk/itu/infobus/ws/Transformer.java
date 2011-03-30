package dk.itu.infobus.ws;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import dk.itu.infobus.ws.ListenerToken;

/**
 * A transformer is a listener that contains also a generator. When an event is received, it is transformed in some way and dispatched again to the server.
 * @author frza
 *
 */
public abstract class Transformer extends Listener {
	
	/**
	 * Custom generator to be used with Transformer
	 * @author frza
	 *
	 */
	public class LinkedGenerator extends Generator {

		Transformer transformer;
		public LinkedGenerator(String name,
				String... fields) {
			super(name, fields);
		}
		
		@Override
		protected void onActivationRequest(Map<String, Object> evt)
				throws Exception {
			this.transformer.restart();
		}
		@Override
		protected void onDeactivationRequest(Map<String, Object> evt)
				throws Exception {
			this.transformer.stop();
		}
		@Override
		protected void cleanUp() throws Exception {
		}
		@Override
		protected void onStarted() throws Exception {
		}
		@Override
		protected void onSystemEvent(Map<String, Object> evt) throws Exception {
		}
	}
	
	LinkedGenerator generator;
	
	/**
	 * Create a Transformer. If you use this constructor, you will have to call the serGenerator method in order to use the transformer.
	 * @param pattern
	 */
	public Transformer(List<ListenerToken<?>> pattern) {
		this(pattern,null);
	}
	/**
	 * Create a Transformer.
	 * @param pattern
	 * @param generator
	 */
	public Transformer(List<ListenerToken<?>> pattern, LinkedGenerator generator) {
		super(pattern);
		if(generator != null) {
			setGenerator(generator);
		}
	}
	
	/**
	 * Set the LinkedGenerator for this transformer. This method is meant to be called only once,
	 * ideally at initialization time.
	 * @param generator
	 */
	protected void setGenerator(LinkedGenerator generator) {
		this.generator = generator;
		this.generator.transformer = this;
	}
	
	protected void restart() {
		try {
			this.eb.addListener(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Transform a message.<br/>
	 * The returned map can be an enriched version of the <code>msg</code> parameter, an entirely new map or <code>null</code>.
	 * Return <code>null</code> if you do not want any event to be sent back to the server.
	 * In any case, <b>never</b> return the same <code>msg</code> parameter without any change: in that case a loop will be 
	 * constructed, and the event will bounce indefinitely between the server and this transformer.
	 * @param msg
	 * @return
	 * @throws Exception
	 */
	protected abstract Map<String, Object> transform(Map<String,Object> msg) throws Exception;

	@Override
	public void onMessage(Map<String, Object> msg) {
		Map<String,Object> reply = null;
		int hash = msg.hashCode();
		try {
			reply = transform(msg);
		} catch(Exception e){
			e.printStackTrace();
		}
		if(reply != null && (hash != reply.hashCode())) {
			generator.publish(reply);
		}
	}
	
	@Override
	public void cleanUp() throws Exception {
		this.generator.stop();
	}

	@Override
	public void onStarted() {
		try {
			eb.addGenerator(generator);
		} catch(Exception e){
			e.printStackTrace();
		}
	}
}
