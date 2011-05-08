import dk.itu.infobus.ws.*;

import java.io.IOException;
import java.util.*;

import dk.itu.infobus.ws.ListenerToken;

public class InfiniteTermStack {
	
	public List<List<Map<String, Object>>> aheadMatches;		
	public List<Integer> aheadCounters;
	
	public void cleanUp() {
		this.aheadMatches = null;
		this.aheadCounters = null;
	}
	
}