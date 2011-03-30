package dk.itu.infobus.ws;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.eclipse.jetty.util.ajax.JSON;

import com.sixfire.websocket.WebSocketAsync;

/**
 * EventBus client handler class.
 * It is the main access to the EventBus server.<br/>
 * Communication with the server is handled by a websocket connection.
 * @author frza
 *
 */
@SuppressWarnings({"unchecked","serial"})
public class EventBus implements WebSocketAsync.WebsocketMessageHandler {
	static final Logger log = Logger.getLogger(EventBus.class.getName());
	/**
	 * debug flag, set to true to have a really annoying log messages
	 */
	public static boolean DBG = false;
	
	private void dbg(String debug) {
		if(DBG) {
			log.info(debug);
		}
	}
	
	/**
	 * Interface to be implemented by classes that wants to be notified
	 * of incoming messages from the server
	 * @author frza
	 *
	 */
	public interface EventBusMessageListener {
		void onMessage(Map<String,Object> msg);
	}
	/**
	 * Interface to be implemented by classes that wants to be notified
	 * of changes in the state of the EventBus client
	 * @author frza
	 *
	 */
	public interface EventBusStatusListener {
		void onStatusChange(EventBusStatus status, EventBus eb);
		void onDispatchError(String wrongMessage, Exception e);
	}
	
	/**
	 * State of the EventBus client
	 * @author frza
	 *
	 */
	public enum EventBusStatus {
		/**
		 * client connected to the server
		 */
		CONNECTED,
		/**
		 * client has been requested to disconnect
		 */
		DISCONNECTED,
		/**
		 * client is in trying to reconnect to the server,<br/>
		 * e.g. after a connection drop
		 */
		RECONNECTING,
		/**
		 * the connection with the server has been lost
		 */
		CONNECTION_LOST,
		/**
		 * the client tried to reconnect, but gave up
		 */
		CANNOT_RECONNECT
	}

	
	
//	static Random r = new Random();
	static String nextId() {
		return UUID.randomUUID().toString();//new System.currentTimeMillis() + "." + r.nextInt(10000);
	}
	
	/**
	 * client identifier. Initially the server assigns this field.
	 * If the connection is lost, the client will try to reconnect
	 * using the original clientId. When the connection is re-established,
	 * but the server timed out his representation of this client,
	 * listeners and generators are automatically re-registered.
	 */
	String clientId = null;

	/**
	 * Hostname of the EventBus server
	 */
	String hostname;
	/**
	 * Port of the EventBus server
	 */
	int port;
	
	/**
	 * Uri of the EventBus websocket v2 endpoint
	 */
	String uri;
	
	/**
	 * The websocket through which the communication is managed
	 */
	WebSocketAsync wsocket;
	
	/**
	 * map of the current registered listeners. The key field is the listener identifier
	 */
	Map<String,Listener> listeners = new HashMap<String,Listener>();
	/**
	 * map of the current registered generators. The key field is the generator name
	 */
	Map<String,Generator> generators = new HashMap<String,Generator>();
	/**
	 * Temporary list of events that generators wants to send when
	 * the client is not yet connected or is in the process of
	 * re-connecting to the server
	 */
	List<Map<String,Object>> cached = new LinkedList<Map<String,Object>>();
	
	boolean connected = false;
	
	EventBusStatusListener statusListener = null;
	
	/**
	 * Set the status listener. By means of this, an application can be
	 * notified of changes in the EventBus status, such as when the connection
	 * is lost and when is re-established.
	 * @param sl
	 */
	public void setStatusListener(EventBusStatusListener sl) {
		this.statusListener = sl;
	}
	
	/**
	 * Get the raw websocket. This should be used only for test purposes,<br/>
	 * e.g. to abruptly close the websocket to simulate a connection loss.
	 * @return
	 */
	public WebSocketAsync getWebSocket() {
		return wsocket;
	}

	/**
	 * Construct a new EventBus client instance. The connection is not initialized here.
	 * @param hostname the EventBus server hostname or ip
	 * @param port the EventBus server port
	 */
	public EventBus(String hostname, int port) {
		String host = hostname;
		if(port > 0) {
			host += ":" + port;
		}
		uri = "ws://"+host+"/infobusws2";
		wsocket = new WebSocketAsync(URI.create(uri),this);
	}
	
	private void notifyStatus(EventBusStatus status) {
		if(null != this.statusListener) {
			this.statusListener.onStatusChange(status, this);
		}
	}
	
	protected boolean reconnect() {
		notifyStatus(EventBusStatus.RECONNECTING);
		String uriCl = uri+"?clientid="+clientId;
		wsocket = new WebSocketAsync(URI.create(uriCl),this);
		try {
			start();
			return true;
		} catch(IOException e){
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Open the websocket connection
	 * @throws IOException
	 */
	public void start() throws IOException {
		dbg("Starting");
		wsocket.connect();
		connected = true;
		notifyStatus(EventBusStatus.CONNECTED);
	}
	/**
	 * Stop the client. All the listeners and generators will be 
	 * removed from the server, then the connection is closed.
	 * @throws IOException
	 */
	public void stop() throws IOException {
		dbg("Stopping");
		try {
			for(String reg : new ArrayList<String>(listeners.keySet())) {
				listeners.get(reg).stop();
			}
			listeners.clear();
			for(String name : new ArrayList<String>(generators.keySet())) {
				generators.get(name).stop();
			}
			generators.clear();
			
			disconnect();
		} finally {
			try {
				wsocket.close();
			} finally {
				notifyStatus(EventBusStatus.DISCONNECTED);
			}
		}

	}
	
	@Override
	public void onMessage(String message) {
		if(message.length()==0){
			return;
		}
//		Object[] events = null;
//		dbg("recvd: "+message);
		Object event = null;
		try {
			event = (Object)JSON.parse(message);
			dispatchEvent((Map<String,Object>)event);
		} catch(Exception e) {
			if(this.statusListener != null) {
				this.statusListener.onDispatchError(message,e);
			} else {
				System.out.println("failed to dispatch message: "+message);
//				e.printStackTrace();
			}
		}
	}
	@Override
	public void onMessage(byte[] message) {
		//discard
		log.info("received a binary-frame, discard");
	}
	
	private void dispatchEvent(Map<String,Object> msg) throws Exception {
//		Map<String,Object> msg = (Map<String,Object>)JSON.parse(message);
		Map<String,Object> payload = (Map<String,Object>)msg.get("event");

		if(msg.containsKey("listener")) {
			if(listeners.containsKey((String)msg.get("listener"))) {
				listeners.get((String)msg.get("listener")).onMessage(payload);
			}
		} else if(msg.containsKey("generator")) {
			if(generators.containsKey((String)msg.get("generator"))) {
				generators.get((String)msg.get("generator")).onMessage(payload);
			}
		} else if(msg.containsKey("registration")) {
			String r = (String)msg.get("registration");
			Listener l = listeners.remove((String)msg.get("txid"));
			listeners.put(r,l);
			l.registration = r;
			l.onStarted();
		} else if(msg.containsKey("clientid")) {
			// here is the clientId
			String clientid = (String)msg.get("clientid");
			System.out.println("received clientid: "+clientid);
			if(this.clientId == null) {
				this.clientId = clientid;
				System.out.println("client id set "+this.clientId);
			} else if(!this.clientId.equals(clientid)) {
				this.clientId = clientid;
				//perform registrations and stuff
				System.out.println("ouch, our stuff has timed out!");
				List<Listener> tmplisteners = new LinkedList<Listener>(this.listeners.values());
				List<Generator> tmpgenerators = new LinkedList<Generator>(this.generators.values());
				
				this.listeners.clear();
				this.generators.clear();
				
				for(Listener l : tmplisteners) {
					try {
						this.addListener(l);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				for(Generator g : tmpgenerators) {
					try {
						this.addGenerator(g);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else {
				this.clientId = clientid;
			}
		} else if(msg.containsKey("keepalive")) {
			//keepalive message
		}
	}
	
	/**
	 * When a communication exception occurs, the client
	 * will try to reconnect to the server every 5 seconds,
	 * for 100 times. After this, it will give up and notify
	 * the CANNOT_RECONNECT state.
	 */
	@Override
	public void onException(IOException e) {
		dbg("client "+clientId+" disconnected!");
		notifyStatus(EventBusStatus.CONNECTION_LOST);
		connected = false;
		int attempts = 0;
		while(!connected) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			dbg("attempt to reconnected "+attempts);
			connected = reconnect();
			attempts++;
			if(attempts >= 100) {
				//well..give up!
				break;
			}
		}
		if(connected) {
			dbg("reconnected");
			List<Map<String,Object>> copy = new LinkedList<Map<String,Object>>(this.cached);
			this.cached.clear();
			for(Map<String,Object> msg : copy) {
				try {
					send(msg);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		} else {
			notifyStatus(EventBusStatus.CANNOT_RECONNECT);
			dbg("not reconnected!");
		}
	}
	
	private void send(Map<String,Object> msg) throws IOException {
		if(!connected) {
			dbg("caching");
			synchronized(cached) {
				this.cached.add(msg);
			}
		} else {
			dbg("Sending: "+JSON.toString(msg));
			synchronized(wsocket) {
				this.wsocket.send(JSON.toString(msg));
			}
		}
	}
	
	/**
	 * Register an event listener. Already registered listeners are ignored.
	 * @param listener
	 * @throws IOException
	 * @see dk.itu.infobus.ws.Listener
	 */
	public void addListener(final Listener listener) throws IOException {
		if(listener.registration != null && this.listeners.containsKey(listener.registration)) {
			//already registered
			return;
		}
		listener.setEventBus(this);
		final String myTxId = nextId();
		final List evt = new ArrayList();
		for(ListenerToken t : listener.pattern) {
			evt.add(t.toHashMap());
		}

		listeners.put(myTxId, listener);
		Map<String,Object> regMsg = new HashMap<String,Object>() {{
			put("method","subscribe");
			put("params",evt);
			put("txid",myTxId);
		}};
		send(regMsg);
	}
	/**
	 * Register an event generator. Already registered generators are ignored.
	 * @param generator
	 * @throws Exception
	 * @see dk.itu.infobus.ws.Generator
	 */
	public void addGenerator(final Generator generator) throws Exception {
		if(generators.containsKey(generator.name)) {
			return;
		}
		generator.setEventBus(this);
		generators.put(generator.name, generator);
		Map<String,Object> regMsg = new HashMap<String,Object>(){{
			put("method","generator");
			put("params", new Object[]{generator.name,generator.fields});
		}};
		send(regMsg);
		generator.onStarted();
	}
	
	/**
	 * Remove an event listener. This method is automatically called when the listener is stopped.
	 * @param registration
	 * @throws IOException
	 */
	protected void removeListener(final String registration) throws IOException {
		Listener l = listeners.remove(registration);
		try {
			l.cleanUp();
		} catch (Exception e) {
			e.printStackTrace();
		}
		dbg("removing listener " + registration);
		send(new HashMap<String,Object>(){{
			put("method","removelistener");
			put("params",registration);
		}});
	}
	/**
	 * Remove an event generator. This method is automatically called when the generator is stopped.
	 * @param name
	 * @throws IOException
	 */
	protected void removeGenerator(final String name) throws IOException {
		Generator g = generators.remove(name);
		try {
			g.cleanUp();
		} catch (Exception e) {
			e.printStackTrace();
		}
		send(new HashMap<String,Object>(){{
			put("method","removegenerator");
			put("params",name);
		}});
	}
	
	protected void disconnect() throws IOException {
		send(new HashMap<String,Object>(){{
			put("method","disconnect");
		}});
	}
	protected void publish(final Map<String,Object> evt) throws IOException {
		send(new HashMap<String,Object>(){{
			put("method","publish");
			put("params",new Object[]{evt});
		}});
	}
	
}
