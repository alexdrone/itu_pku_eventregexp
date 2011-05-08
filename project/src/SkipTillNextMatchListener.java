import dk.itu.infobus.ws.*;

import java.io.IOException;
import java.util.*;

import dk.itu.infobus.ws.ListenerToken;

public class SkipTillNextMatchListener /*extends dk.itu.infobus.ws.Listener*/ {
	
	/* the class stream */
	private Listener listener;
	
	/* the given sequence to match */
	private SequenceBuilder sequence;
	
	/* all the matched elements */
	private List<Map<String, Object>> matched =
		new LinkedList<Map<String, Object>>();
		
	/* the current match */
	private List<List<Map<String,Object>>> currentMatch;
	
	/* the counter for the occurrences */
	private List<Integer> counters;
		
	/* pointer to the current node in the sequence */
	private int pointer = 0;
	
	/**
	 * Creates a new <code>PatternMatchingListener</code> from a given stream
	 * @param pattern The pattern that describes the stream 
	 */
	public SkipTillNextMatchListener(
		EventBus eb,
		List<ListenerToken<?>> pattern, 
		SequenceBuilder sequence) {
		
		//super(pattern);
		
		this.listener = new Listener(pattern) {
			
			/* is used when the listener has to release some resource when 
			 * it is removed from the server */
			public void cleanUp() throws Exception { /* todo */ }
			
			/* is called when an event matching the pattern has been 
			 * received */
			public void onMessage(Map<String, Object> msg) {
				match(msg);
				//onMessage(msg);
			}
			
			/* is used to define some initialization. It is called when the 
			 * listener has been registered on the Genie Hub server */
			public void onStarted() { /* todo */ }
			
		};
		
		this.sequence = sequence;
		
		try { 
			eb.addListener(this.listener);
		} catch (IOException e) { 
			System.out.println("Unable to connect to the server");
		}
	}
	
	/**
	 * Called when the client receive the server-assigned registration 
	 * identifier. Useful for one-time initializations that needs to know 
	 * the identity of the listener, or that needs the listener to be 
	 * already connected to the server.
	 */
	public void cleanUp() throws Exception { /* todo */ }
	
	/** 
	 * Called whenever an event is received for this listener
	 */
	public void onMessage(Map<String, Object> msg) { 
		/* end-user will override this method */
	}
	
	/**
	 * Called when the listener has to be removed. Useful for freeing 
	 * resources.
	 */
	public void onStarted() { /* todo */ }

	
	/**
	 * Core method - automata implementing the static matching 
	 * of the pattern
	 */
	private void match(Map<String, Object> msg) { 
						
		/* the current pointed node in the sequence */
		List<SequenceTermBuilder> node = sequence.get(pointer);
		
		/* initialize the used data structures */
		if (counters == null) {
			
			counters = new LinkedList<Integer>();
									
			currentMatch = new LinkedList<List<Map<String,Object>>>();
			for (SequenceTermBuilder term : node) {
				
				/* creates the list for this node */
				List<Map<String,Object>> l = 
					new LinkedList<Map<String,Object>>();
					
				currentMatch.add(l);
				
				/* the counters */
				counters.add(0);
			}				
		}
			
		/* element in AND */
		if (!sequence.isNegated(pointer)) {
						
			/* iterate through each term of the node */
			int i = 0;
			for (SequenceTermBuilder term : node) {				
				
				int expectedOccurrences = term.getOccurrences();
				int occurrences = counters.get(i);
				
				/* if this term match */
				if (matchEvent(msg, term.getCriteria())) {
					System.out.println("is matched");
					
					currentMatch.get(i).add(msg);
					counters.add(i, ++occurrences);
				}
				/* if a matching of a single term is terminated we just
				 * move the pointer on */
				if (expectedOccurrences > 0 && 
					occurrences == expectedOccurrences) {
						
						/* we append all the current match for this term
						 * in the final matched events list */
						matched.addAll(currentMatch.get(i));
						
						/* we move to next node */
						pointer++;
						
						/* we initialize the temporary variables for the 
						 * next node-check */
						counters = null;
						currentMatch = null;
						break;
					}				
					
				/* is a possible infinite sequence */
				if (expectedOccurrences < 0) {
					
					/* call to the recursive function */
					List<Map<String,Object>> aheadCall = 
						lookAheadSequence(pointer+1, term, msg);
					
					if (aheadCall != null) {
						/* the matched subsequence will be added by the recursive
						 * function ?  */
						matched.addAll(aheadCall);

						/* we initialize the temporary variables for the 
						 * next node-check */
						counters = null;
						currentMatch = null;	
						
					/* easiest case - the first is element compatible with
					 * the infinite match */
					} else if (matchEvent(msg, term.getCriteria())) {
						System.out.println("(lookAheadTerm) kleene match");

						currentMatch.get(i).add(msg);
						counters.add(i, ++occurrences);
						pointer++;

						break;
					}
				}
				
				i++;
			}
		
		/* element in AND NOT */
		} else {
			
			/* todo */
		}
		
		/* check if the entire sequence is matched */
		if (pointer == sequence.size()) {
			/* we just notify the client with the entire matched sequence */
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("sequence", matched);
			onMessage(map);
			
			/* pointer is resetted */
			pointer = 0;
			
			/* and matched initialized again */
			 matched =
				new LinkedList<Map<String, Object>>();
		}

		/* fin */
	}
	
	/**
	 * It checks if a single event in the stream is compatible with a given
	 * criteria
	 * @param msg the event from the stream
	 * @param criteria the given criteria - a dictonary with the following 
	 * keys: <code>field</code>, <code>operator</code> and <code>value</code>
	 * @return <code>true</code> if the event match the criteria, 
	 * <code>false</code> otherwise.
	 */
	private boolean matchEvent(Map<String, Object> m, 
	List<Map<String, Object>> criteria) {

		
		for (Map<String, Object> c : criteria) {

			
			String field = (String) c.get("field");
			Object value = c.get("value");
		
			switch((PatternOperator) c.get("operator")) {
				
				case EQ:
					if (!m.get(field).equals(value)) return false;
					break;
					
				/* TODO: other cases */
				
				default: 
					break;
			}	
		}
		
		return true;
	}
	
	/** 
	 * this function is called everytime an infinite term is found.
	 * it looks ahead in the sequence for terms that could match the 
	 * given message
	 * @param lookAheadPointer local pointer to the sequence node
	 * @param termCaller the term that called this method
	 */
	private List<Map<String,Object>> lookAheadSequence(int lookAheadPointer, 
	SequenceTermBuilder termCaller, Map<String,Object> msg) {
				
		/* the previous term stack */ 
		InfiniteTermStack stack = termCaller._stack;
		
		/* get the node from the sequence */
		List<SequenceTermBuilder> node = sequence.get(lookAheadPointer);
		
		/* initialize the data structures */
		if (stack.aheadMatches.size() == 0) {
			for (SequenceTermBuilder term : node)
				stack.aheadMatches.add(new LinkedList<Map<String,Object>>());
		}
		
		/* element in AND */
		if (!sequence.isNegated(lookAheadPointer)) {
						
			/* iterate through each term of the node */
			int i = 0;			
			
			for (SequenceTermBuilder term : node) {
				
				int expectedOccurrences = term.getOccurrences();
				int occurrences = stack.aheadCounters.get(i);
				
				/* again is an infinite term */
				if (expectedOccurrences < 0) {
					
					/* evaluate the recursive call */
					List<Map<String,Object>> result =
					lookAheadSequence(lookAheadPointer + 1, term, msg);
					
					/* returns the recursive result */
					if (result != null) {
						stack.aheadMatches.get(i).addAll(result);
						return stack.aheadMatches.get(i);
					}
				
				/* it's a finite cardinality term */
				} else {
					
					/* try to match the event message */
					if (matchEvent(msg, term.getCriteria())) {
						System.out.println("(lookAheadSequence) match");

						stack.aheadMatches.get(i).add(msg);
						stack.aheadCounters.add(i, ++occurrences);
					}
					
					/* if a matching of a single term is terminated we just
					 * move the pointer on */
					if (expectedOccurrences > 0 && 
						occurrences == expectedOccurrences) {	
							this.pointer = lookAheadPointer;
							return stack.aheadMatches.get(i);
						}
				}
				i++;
			}
			
		/* element in AND NOT */
		} else {
			/* todo */
		}
		
		return null;
	}
	
}