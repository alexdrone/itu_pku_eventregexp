import dk.itu.infobus.ws.*;

import java.io.IOException;
import java.util.*;

import dk.itu.infobus.ws.ListenerToken;

public class PatternMatchingListener extends dk.itu.infobus.ws.Listener {
	
	/* the class stream */
	private Listener listener;
	
	/* contains all the terms added to the sequence
	 * TODO: Is possible to have just 2 nested terms, the subsequences
	 * should (?) be inifinite. */
	private List<List<SequenceTerm>> terms = 
		new LinkedList<List<SequenceTerm>>();
		
	/* pointer to the current term-set */
	private int seqPointer = 0;
	
	/* contains all the matched entries */
	private List<Map<String, Object>> matchedEntries;
	
	/* the name of the sequence */
	private String seqName;
	
	/**
	 * Creates a new <code>PatternMatchingListener</code> from a given stream
	 * @param pattern The pattern that describes the stream 
	 */
	public PatternMatchingListener(List<ListenerToken<?>> pattern) {
		
		super(pattern);
		
		this.listener = new Listener(pattern) {
			
			/* is used when the listener has to release some resource when 
			 * it is removed from the server */
			public void cleanUp() throws Exception { /* todo */ }
			
			/* is called when an event matching the pattern has been 
			 * received */
			public void onMessage(Map<String, Object> msg) {
				staticMatching(msg);
				onMessage(msg);
			}
			
			/* is used to define some initialization. It is called when the 
			 * listener has been registered on the Genie Hub server */
			public void onStarted() { /* todo */ }
			
		};
		
		/* todo: other initialization */
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
	@Override
	public void onMessage(Map<String, Object> msg) { 
		/* end-user will override this method */
	}
	
	/**
	 * Called when the listener has to be removed. Useful for freeing 
	 * resources.
	 */
	public void onStarted() { /* todo */ }
	
	/**
	 * Add the given sequence to the match-criteria 
	 * @param b A <code>SequenceBuilder</code> representing the sequence 
	 */
	public void setSequence(SequenceBuilder b) {
		this.terms = b.getTerms();
		this.seqName = b.getName();
	}
	
	/**
	 * Core method - automata implementing the static matching 
	 * of the pattern
	 */
	private void staticMatching(Map<String, Object> msg) { 

		/* last item of the sequence */
		if (seqPointer == terms.size()) {
			Map result = new HashMap();
			result.put(seqName, matchedEntries);
			
			/* call the onMessage method in order to notify the client
			 * of the matched data */
			onMessage(result);
			return;
		}				
		
		/* TODO */
		
	}
	
	/* test main */
	public static void main(String[] args) throws Exception {
		
		EventBus eb = new EventBus("tiger.itu.dk",8004);
		
		eb.start();
		eb.addGenerator(new SampleGenerator());
		
		/* new pattern */
		PatternBuilder pb = new PatternBuilder()
			.addMatchAll("foo")
			.add("bar", PatternOperator.NEQ, 10);
			
		Listener listener = new PatternMatchingListener(pb.getPattern()) {
			
			/* user will mainly override this method */
			public void onMessage(Map<String, Object> msg) {
			    Util.traceEvent(msg);	
			}

		};
		
		eb.addListener(listener);
	}
}