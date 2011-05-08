import dk.itu.infobus.ws.*;

import java.io.IOException;
import java.util.*;

import dk.itu.infobus.ws.ListenerToken;

public class InfiniteTermStack {
	
	public List<List<Map<String, Object>>> aheadMatches = 
		new LinkedList<List<Map<String, Object>>>();
		
	public List<Integer> aheadCounters = new LinkedList<Integer>();
	
}