import java.util.Map;
import dk.itu.infobus.ws.*;
import dk.itu.infobus.ws.EventBus;
import dk.itu.infobus.ws.EventBus.EventBusStatus;
import dk.itu.infobus.ws.EventBus.EventBusStatusListener;
import dk.itu.infobus.ws.PatternBuilder;

public class Sample {
	
	public static void main(String[] args) throws Exception {
		
		Listener listener = createSampleListener();
		
		EventBus eb = new EventBus("tiger.itu.dk",8004);

		eb.setStatusListener(new EventBusStatusListener() {
			
			public void onStatusChange(EventBusStatus status, EventBus eb) {
				System.out.println("→ onStatusChange : " + status.name());
			}
			
			public void onDispatchError(String str,Exception exc) {
				System.out.println("→ onDispatchError : " + str);
			}
		});
		
		eb.start();
		eb.addListener(listener);
		eb.addGenerator(new SampleGenerator());
	}

	public static Listener createSampleListener() {
		Listener listener;
		
		/* new pattern */
		PatternBuilder pb = new PatternBuilder()
			.addMatchAll("foo")
			.add("bar", PatternOperator.NEQ, 10);
			
		listener = new Listener(pb.getPattern()) {
			

			/* is used when the listener has to release some resource when 
			 * it is removed from the server */
		    public void cleanUp() throws Exception {
		    }
		
			/* is used to define some initialization. It is called when the 
			 * listener has been registered on the Genie Hub server */
		    public void onStarted() {
		        System.out.println("Listener started!");
		    }
				
			/* is called when an event matching the pattern has been 
			 * received */
		    public void onMessage(Map<String, Object> msg) {
		        Util.traceEvent(msg);
		    }
		};
		
		return listener;
	}
}