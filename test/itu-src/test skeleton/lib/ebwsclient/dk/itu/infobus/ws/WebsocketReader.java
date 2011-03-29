package dk.itu.infobus.ws;

import java.io.IOException;

import com.sixfire.websocket.WebSocket;

/**
 * Asynchronous websocket reader. Each time a message is read, it is dispatched to the WebsocketMessageHandler.
 * If an exception occours, the WebsocketMessageHandler will be notified and the reader closed.
 * @author frza
 *
 */
public class WebsocketReader extends Thread {

	public interface WebsocketMessageHandler {
		void onMessage(String message);
		void onException(IOException e);
	}
	
	WebsocketMessageHandler handler;
	WebSocket wsocket;
	
	boolean running = true;
	
	public WebsocketReader(WebSocket wsocket, WebsocketMessageHandler handler) {
		this.wsocket = wsocket;
		this.handler = handler;
	}
	
	public void stopReading() {
		running = false;
	}
	
	@Override
	public void run() {
		System.out.println("Starting websocket reader");
		while(!wsocket.isConnected()) {
			System.out.println("..websocket is not connected..");
			try {
				Thread.sleep(100);
			} catch(Exception e){}
			if(!running) {return;}
		}
		System.out.println("read messages...");
		while(running) {
			try {
				String msg = new String(wsocket.recv(),"UTF-8");
				handler.onMessage(msg);
			} catch (IOException e) {
				if(running) {
					e.printStackTrace();
					handler.onException(e);
					running = false;
				} else {
					System.out.println("websocket reader closed");
				}
			}
		}
	}
}
