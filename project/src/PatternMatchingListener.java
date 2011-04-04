import java.util.list.*;
import dk.itu.infobus.ws.*;

public class PatternMatchingListener extends Listener {
	
	/* the class stream */
	private Listener listener;
	
	/**
	 * Creates a new <code>PatternMatchingListener</code> from a given stream
	 * @param pattern The pattern that describes the stream 
	 */
	public PatternMatchingListener(List<ListenerToken<E>> pattern) {
		
		listener = new Listener(pattern) {
			
			/* is used when the listener has to release some resource when 
			 * it is removed from the server */
			public void cleanUp() throws Exception { /* todo */ }
			
			/* is called when an event matching the pattern has been 
			 * received */
			public void onMessage(Map<String, Object> msg) {
				someLogic();
				onMessage(msg);
			}
			
			/* is used to define some initialization. It is called when the 
			 * listener has been registered on the Genie Hub server */
			public void onStarted() { /* todo */ }
			
		};
		
		/* todo: other initialization */
	}
	
	/**
	 * Called when the client receive the server-assigned registration 
	 * identifier. Useful for one-time initializations that needs to know 
	 * the identity of the listener, or that needs the listener to be 
	 * already connected to the server.
	 */
	public void cleanUp() throws Exception() { /* todo */ }
	
	/** 
	 * Called whenever an event is received for this listener
	 */
	@Override
	public void onMessage(Map<String, Object> msg) { 
		/* end-user will override this method */
	}
	
	/**
	 * Called when the listener has to be removed. Useful for freeing 
	 * resources.
	 */
	public void onStarted() { /* todo */ }
	
	
	public void someLogic() { 
	
		System.out.println("filtering the data.");
		/* pattern matching logic */ 
	}
	
	/* test main */
	public static void main(String[] args) {
		
		EventBus eb = new EventBus("tiger.itu.dk",8004);
		
		eb.start();
		eb.addGenerator(new SampleGenerator());
		
		/* new pattern */
		PatternBuilder pb = new PatternBuilder()
			.addMatchAll("foo")
			.add("bar", PatternOperator.NEQ, 10);
			
		Listener listener = new PatternMatchingListener(pb.getPattern()) {
			
			/* user will mainly override this method */
			public void onMessage(Map<String, Object> msg) {
			    Util.traceEvent(msg);	
			}

		};
	}
}