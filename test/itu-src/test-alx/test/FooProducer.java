import java.util.Map;
import dk.itu.infobus.ws.*;

public class FooProducer extends GeneratorAdapter {

	private Thread fooThread;
	private boolean running = true;
	
	public FooProducer() {
		
		/* the name of the generator and event-fields */
		super("producer-alx.producer", "foo");
	}
	
	/* when closed the thread will be shuted down */
	@Override
    protected void cleanUp() throws Exception {
        running = false;
    }

	/* when started, a thread will be started too that emits an event 
	 * every second */
	@Override
    public void onStarted() {
        fooThread = new Thread() {
	
			@Override
			public void run() {
				
				while(running) {
					
					/* publish the evnt */
					publish(new EventBuilder()
						.put("foo", "foo:" + System.currentTimeMillis())
						.getEvent());
						
					try { Thread.sleep(1000); 
					} catch(Exception e) { /* handle */ }
				}
			}
		};
		
		fooThread.start();
	}
}