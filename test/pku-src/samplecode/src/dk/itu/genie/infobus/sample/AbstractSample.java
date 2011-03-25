package dk.itu.genie.infobus.sample;

import java.io.IOException;

import dk.itu.infobus.ws.EventBus;
import dk.itu.infobus.ws.EventBus.EventBusStatus;
import dk.itu.infobus.ws.EventBus.EventBusStatusListener;

/**
 * Abstract class for the EventBus WebSocket client sample framework.
 * @author frza
 *
 */
public abstract class AbstractSample {

	/**
	 * Entry point for interacting with the EventBus
	 */
	EventBus eb;
	
	/**
	 * Empty constructor required for the class to be constructed using reflection
	 */
	public AbstractSample(){}
	
	/**
	 * Debug message
	 * @param msg
	 */
	protected void dbg(String msg){dbg(msg,null);}
	
	/**
	 * Debug message and an exception
	 * @param msg
	 * @param e
	 */
	protected void dbg(String msg, Exception e) {
		if(EventBus.DBG) {
			System.out.println(msg);
			if(e != null) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Create the eventbus, start it and call doSample.
	 * @param host
	 * @param port
	 */
	public void start(String host, int port) {
		this.eb = new EventBus(host,port);
		if(EventBus.DBG) {
			this.eb.setStatusListener(new EventBusStatusListener() {
				@Override
				public void onStatusChange(EventBusStatus status, EventBus eb) {
					dbg("Status changed : "+status.name());
				}
			});
		}

		try {
			eb.start();
		} catch (IOException e) {
			dbg("cannot start EventBus!",e);
			return;
		}
		
		try {
			doSample(eb);
		} catch (Exception e) {
			dbg("cannot complete doSample method!",e);
		}
		dbg("Sample " + this.getClass().getName() + " completed.");
	}
	
	/**
	 * Close the eventbus connection
	 */
	public void stop() {
		dbg("Closing EventBus connection");
		try {
			eb.stop();
		} catch (IOException ignored) {}
	}
	
	/**
	 * if set to true, the user will have to type "exit" to end the application
	 * @return
	 */
	protected abstract boolean waitForExit();

	/**
	 * inside this method, interact with the EventBus.
	 * It is not necessary to call "close" on the bus, as it is 
	 * managed by the mini-sample framework.
	 * @param eb
	 * @throws Exception
	 */
	protected abstract void doSample(EventBus eb) throws Exception;

}
