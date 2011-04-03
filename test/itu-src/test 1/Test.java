import java.io.IOException;
import dk.itu.infobus.ws.EventBus;
import dk.itu.infobus.ws.EventBus.EventBusStatus;
import dk.itu.infobus.ws.EventBus.EventBusStatusListener;


import dk.itu.infobus.ws.*;

class Test {
	public static void main(String[] args) {
		
		Generator g;
		
		System.out.println("→ hello world =)");
		System.out.println("→ start");
		
		
		EventBus eb = new EventBus("tiger.itu.dk",8004);
		
		eb.setStatusListener(new EventBusStatusListener() {
			
			public void onStatusChange(EventBusStatus status, EventBus eb) {
				System.out.println("→ onStatusChange : "+status.name());
			}
			
			public void onDispatchError(String str,Exception exc) {
				System.out.println("→ onDispatchError : "+str);
			}
		});
		
		try { 
			eb.start();
			eb.stop();
			eb.start();
			
		} catch (Exception e) {
			System.out.println("exc");
		}
		
		

				
		System.out.println("→ completed");
		

	}
	
	
	
}