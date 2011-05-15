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
	
	/* used for lookAhed(1) in not */
	private List<List<Map<String,Object>>> aheadMatch;
	
	
	/* the counter for the occurrences */
	private List<Integer> counters;
	
	/* aheadcounter */
	private List<Integer> aheadCounters;
	

	/* pointer to the current node in the sequence */
	private int pointer = 0;
	
	
	private boolean smallestSequence = false;
	
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
	 * For every node if is an AND node <code>andNode()</code> is called, 
	 * <code>notNode()</code> otherwise.
	 * On a single event message, the pointer can be moved of several 
	 * position (ex. current sequence mode is an infinite term or a NOT 
	 * term).
	 * The two methods can therefore perform several lookaheads in 
	 * the sequence.
	 * @param msg the current event message, evenutally matched by 
	 * one of the terms of the current node
	 */
	private void match(Map<String, Object> msg) { 
						
		/* the current pointed node in the sequence, it contains  
		 * all the terms (criterias) specified from the user */
		List<SequenceTermBuilder> node = sequence.get(pointer);
		
		/* the temporary variables are setted to null, when a complete
		 * match of a term occurs. In this case all the variables should 
		 * be reinitialized */
		if (counters == null) init(pointer);
		
		/* the current node is an AND */
		if (!sequence.isNegated(pointer)) andNode(pointer, msg, false);			
	
		/* node is an AND NOT */
		else notNode(pointer, msg);
		
		/* check if the entire sequence is matched */
		if (pointer == sequence.size()) {
			
			/* we just notify the client with the entire matched sequence */
			Map<String, Object> map = new HashMap<String, Object>();
			
			/* TODO: The name of the sequence should be specified from 
			 * the user in the constructor. */
			map.put("sequence", matched);
			onMessage(map);
			
			/* pointer and result list are re-initialized */
			pointer = 0;
			matched = new LinkedList<Map<String, Object>>();
		}

		/* end */
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
	 * This function is called everytime an infinite term is found.
	 * it looks ahead in the sequence for terms that could match the 
	 * given message.
	 * The field <code>_stack</code> of all the terms is used in order
	 * to keep the information about the number of occurrences found since 
	 * now (it's a sort of recoursive function call emulation, since 
	 * pure recursion is not available for the event-based nature of 
	 * this automata).
	 * @param lookAheadPointer local pointer to the sequence node
	 * @param termCaller the term that called this method
	 */
	private List<Map<String,Object>> lookAheadSequence(int lookAheadPointer, 
	SequenceTermBuilder termCaller, Map<String,Object> msg) {
						
		/* This should never happen */
		if (lookAheadPointer >= sequence.size())
			throw new RuntimeException("(lookAheadSequence) Recursion went" +
			"too far");
						
		/* the previous term stack
		 * here are stored all the information about the found occurrences and  
		 * the matched sequences. */ 
		InfiniteTermStack stack = termCaller._stack;
		
		/* initialize the data structures */
		/* the current pointed node in the sequence */
		List<SequenceTermBuilder> node = sequence.get(lookAheadPointer);
		
		/* initializing the stack for the current node
		 * This section is extremely similiar to the init() method, 
		 * but it refers to the _stack structures */
		if (stack.aheadCounters == null) {
			
			stack.aheadCounters = new LinkedList<Integer>();						
			stack.aheadMatches  = new LinkedList<List<Map<String,Object>>>();
			
			for (SequenceTermBuilder term : node) {
				
				/* creates the list for this node */
				List<Map<String,Object>> list = 
				new LinkedList<Map<String,Object>>();
					
				stack.aheadMatches.add(list);
				
				/* the counters */
				stack.aheadCounters.add(0);
			}				
		}
		
		/* element in AND
		 * This section is similiar to the andNode() method but 
		 * is performing the lookAhead on the sequence */
		if (!sequence.isNegated(lookAheadPointer)) {
						
			/* iterate through each term of the node */
			int i = 0;			
			
			for (SequenceTermBuilder term : node) {
				
				int expectedOccurrences = term.getOccurrences();
				int occurrences = stack.aheadCounters.get(i);
				
				/* again is an infinite term, the lookahead should 
				 * continue */
				if (expectedOccurrences < 0) {
			
					if (lookAheadPointer + 1 < sequence.size())	{				
					
						/* evaluate the recursive call */
						List<Map<String,Object>> result =
						lookAheadSequence(lookAheadPointer + 1, term, msg);
					
						/* returns the recursive result */
						if (result != null) {
							stack.aheadMatches.get(i).addAll(result);
							return stack.aheadMatches.get(i);
						}
					} else {
						/* TODO: just add the matching elements with 
						 * the infinite term */						
					}
				
				/* it's a finite cardinality term */
				} else {
										
					/* try to match the event message */
					if (matchEvent(msg, term.getCriteria())) {
						stack.aheadMatches.get(i).add(msg);
						stack.aheadCounters.add(i, ++occurrences);
					}
					
					/* if a matching of a single term is terminated we just
					 * move the pointer on */
					if (expectedOccurrences > 0 && 
						occurrences == expectedOccurrences) {	
							this.pointer = lookAheadPointer + 1;
							return stack.aheadMatches.get(i);
					}
				}
				i++;
			}
			
		/* element in AND NOT
		 * TODO: Test this part of code */
		} else {
						
			/* iterate through each term of the node */
			int i = 0;			
			for (SequenceTermBuilder term : node) {

				int expectedOccurrences = term.getOccurrences();
				int occurrences = stack.aheadCounters.get(i);

				/* is not possible to handle infinite term sequence
				 * for an AND NOT node */
				if (expectedOccurrences < 0)
					throw new RuntimeException("NOT term are no with" +
					"infinite cardinality aren't allowed.");
				
				/* try to match the event message */
				if (matchEvent(msg, term.getCriteria())) {
					stack.aheadMatches.get(i).add(msg);
					stack.aheadCounters.add(i, ++occurrences);
				}
				
				/* The NOT term has been matched. The sequence should 
				 * terminate here */
				if (expectedOccurrences > 0 && 
					occurrences == expectedOccurrences) {

					System.out.println("Sequence halted.(NOT term found)");

					/* the matched sequence is resetted */
					this.matched = new LinkedList<Map<String, Object>>();
					this.pointer = 0;

					/* we initialize the temporary variables for the 
					 * next node-check */
					resetVariables();			
					return null;
				}
				
				/* call the lookAhead function in order to match 
				 * the nect token */
				if (lookAheadPointer + 1 < sequence.size())	{				
				
					/* evaluate the recursive call */
					List<Map<String,Object>> result =
					lookAheadSequence(lookAheadPointer + 1, term, msg);
				
					/* returns the recursive result */
					if (result != null) {
						stack.aheadMatches.get(i).addAll(result);
						return stack.aheadMatches.get(i);
					}
				}
				
				/* end */
			}
		}
		
		return null;
	}
	
	/**
	 * Tries to match an AND node and performs all the connected 
	 * actions. This methods performs several lookaheads on the sequence
	 * in order to handle infinite term matching (<code>KLEENE_STAR</code>
	 * cardinality).
	 * @param localPtr the pointer to the current node in the sequence
	 * @param msg the event message 
	 * @param ahead this parameter should be set to <code>true</code> if 
	 * the method is called from the <code>notNode()</code> method, in 
	 * order to skip the NOT node not matched.
	 */
	private void andNode(int localPtr, Map<String, Object> msg, 
	boolean ahead) {
			
		/* get the node pointed by localPtr */
		List<SequenceTermBuilder> node = sequence.get(localPtr);
		
		/* The structures where the matched subsequences will be 
		 * saved. They're different depending from wheter this is 
		 * a lookahead node, or not */
		List<List<Map<String,Object>>> M;	
		List<Integer> C;
		
		M = ahead ? aheadMatch : currentMatch;
		C = ahead ? aheadCounters : counters;
		
		/* iterate through each term of the node */
		int i = 0;
		for (SequenceTermBuilder term : node) {
							
			int expectedOccurrences = term.getOccurrences();
			int occurrences = C.get(i);
			
			/* if this term match */
			if (matchEvent(msg, term.getCriteria())) {
				System.out.println("is matched");
				
				M.get(i).add(msg);
				C.add(i, ++occurrences);
			}
			
			/* if a matching of a single term is terminated we just
			 * move the pointer on */
			if (expectedOccurrences > 0 && 
				occurrences == expectedOccurrences) {
					
				/* we append all the current match for this term
				 * in the final matched events list */
				matched.addAll(M.get(i));
					
				/* move to next node */
				pointer = localPtr + 1;
					
				/* re-initialize the temporary variables for the 
				 * next node-check */
				resetVariables();
				break;
			}				
				
			/* is a possible infinite sequence */
			if (expectedOccurrences < 0 && 
				localPtr + 1 < sequence.size()) {
				
				/* call to the recursive function */
				List<Map<String,Object>> aheadCall = 
					lookAheadSequence(localPtr + 1, term, msg);
				
				if (aheadCall != null) {
					/* the matched subsequence will be added by the
					 * recursive function ?  */
					matched.addAll(aheadCall);

					/* we initialize the temporary variables for the 
					 * next node-check */
					resetVariables();
					break;
					
				/* easiest case - the first is element compatible with
				 * the infinite match */
				} else if (matchEvent(msg, term.getCriteria())) {
					M.get(i).add(msg);
					C.add(i, ++occurrences);
				}
			
				/* TODO : } else (expectedOccurences < 0) .. last term inf */	
			}
			i++;
		}
	}
	
	/**
	 * Tries to match an AND NOT node and performs all the connected 
	 * actions. This methods could perform several lookaheds, because 
	 * it calls <code>andNode()</code> to perform a lookAhead(1) and 
	 * from that, everything can happen. 
	 * @param localPtr the pointer to the current node in the sequence
	 * @param msg the event message 
	 * @param ahead this parameter should be set to <code>true</code> if 
	 * the method is called from the <code>notNode()</code> method, in 
	 * order to skip the NOT node not matched.
	 */
	private void notNode(int localPtr, Map<String, Object> msg) {
				
		List<SequenceTermBuilder> node = sequence.get(localPtr);
							
		/* iterate through each term of the node */
		int i = 0;
		for (SequenceTermBuilder term : node) {	

			int expectedOccurrences = term.getOccurrences();
			int occurrences = counters.get(i);

			/* if this term match */
			if (matchEvent(msg, term.getCriteria())) {
				
				System.out.println("matched");
							
				currentMatch.get(i).add(msg);
				counters.add(i, ++occurrences);
			}
			
			/* if a matching of a single term is terminated we just
			 * move the pointer on */
			if (expectedOccurrences > 0 && 
				occurrences == expectedOccurrences) {
					
				System.out.println("Sequence halted. (NOT term found)");

				/* the matched sequence is resetted */
				this.matched = new LinkedList<Map<String, Object>>();
				this.pointer = 0;

				/* we initialize the temporary variables for the 
				 * next node-check */
				resetVariables();			
				return;
			}	
				
			/* is not possible to handle infinite term sequence */
			if (expectedOccurrences < 0)
				throw new RuntimeException("NOT term are no with" +
				"infinite cardinality aren't allowed.");	
				
			i++;
		}
		
		/* lookahead (1) */
		System.out.println("notNode: lookAhead(+1)");
		andNode(localPtr + 1, msg, true);
	}
	
	/**
	 * Initialization of the global datastructures 
	 * These temporary variables are used to store the current 
	 * matched subsequences and the number of occurrences found since now
	 * @param localPtr the structures will be initialized gathering the 
	 * information from the node in the sequence pointed by localPtr.
	 */
	private void init(int localPtr) {
		
		/* get the node */
		List<SequenceTermBuilder> node = sequence.get(localPtr);
		
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
		
		/* init of the ahead node */
		if (pointer + 1 < sequence.size()) {
			List<SequenceTermBuilder> aheadNode = sequence.get(pointer+1);
			aheadCounters = new LinkedList<Integer>();

			aheadMatch = new LinkedList<List<Map<String,Object>>>();
			for (SequenceTermBuilder term : aheadNode) {

				/* creates the list for this ahead node */
				List<Map<String,Object>> l = 
					new LinkedList<Map<String,Object>>();
				aheadMatch.add(l);

				/* the counters */
				aheadCounters.add(0);
			}
		}
	}
	
	/**
	 * Reset the global datastructures, usualy it's called 
	 * when a match is completed, and the pointer is moved to the next 
	 * node in the sequence */
	private void resetVariables() {
		counters = null;
		currentMatch = null;
		aheadMatch = null;
		aheadCounters = null;
		
		/* clean the stack data for all the terms in the sequence */
		for (List<SequenceTermBuilder> l : sequence.sequence)
			for (SequenceTermBuilder t : l) t.cleanUp();
	}
}