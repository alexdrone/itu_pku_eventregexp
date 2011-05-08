import dk.itu.infobus.ws.*;

import java.io.IOException;
import java.util.*;

import dk.itu.infobus.ws.ListenerToken;

public class Test {
	
	/* test main */
	public static void main(String[] args) throws Exception {
	
		/* Creating the eventbus and the SkipTillNextMatchListener with 
		 * a stream */
		EventBus eb = new EventBus("tiger.itu.dk",8004);
		
		eb.start();
		eb.addGenerator(new SampleGenerator());
		
		doTestB(eb);
	}
	
	/* first test */
	public static void doTestA(EventBus eb) {
		PatternBuilder pb = new PatternBuilder()
			.addMatchAll("foo");
	
		/* Sequence test */
		SequenceTermBuilder foo_a = 
			new SequenceTermBuilder()
				.add("foo", PatternOperator.EQ, new Long(5));
		
		SequenceTermBuilder bar_a = 
			new SequenceTermBuilder()
				.add("bar", PatternOperator.EQ, 30);
						
		SequenceTermBuilder foo_b = 
			new SequenceTermBuilder()
				.add("foo", PatternOperator.EQ, new Long(6));

		SequenceTermBuilder bar_b = 
			new SequenceTermBuilder()
				.add("bar", PatternOperator.EQ, new Long(40))
				.setOccurrences(2);
		
		SequenceBuilder sequence = 
			new SequenceBuilder().and(foo_a).and(foo_b, bar_b);
		
		/* creates the 	SkipTillNextMatchListener */
		SkipTillNextMatchListener listener = 
			new SkipTillNextMatchListener(eb, pb.getPattern(), sequence) {
			
			/* user will mainly override this method */
			public void onMessage(Map<String, Object> msg) {
				//System.out.println("called inside the listener");
			    Util.traceEvent(msg);	
			}

		};
	}
	
	/* second test */
	public static void doTestB(EventBus eb) {
		PatternBuilder pb = new PatternBuilder()
			.addMatchAll("foo");
	
		/* Sequence test */
		SequenceTermBuilder foo_a = 
			new SequenceTermBuilder()
				.add("foo", PatternOperator.EQ, new Long(5));
		
		SequenceTermBuilder bar_a = 
			new SequenceTermBuilder()
				.add("bar", PatternOperator.EQ, 30)
				.setOccurrences(SequenceTermBuilder.KLEENE_STAR);
						
		SequenceTermBuilder foo_b = 
			new SequenceTermBuilder()
				.add("foo", PatternOperator.EQ, new Long(6));

		SequenceTermBuilder bar_b = 
			new SequenceTermBuilder()
				.add("bar", PatternOperator.EQ, new Long(40))
				.setOccurrences(2);
		
		SequenceBuilder sequence = 
			new SequenceBuilder().and(foo_a).and(bar_a_).and(foo_b, bar_b);
		
		/* creates the 	SkipTillNextMatchListener */
		SkipTillNextMatchListener listener = 
			new SkipTillNextMatchListener(eb, pb.getPattern(), sequence) {
			
			/* user will mainly override this method */
			public void onMessage(Map<String, Object> msg) {
				//System.out.println("called inside the listener");
			    Util.traceEvent(msg);	
			}

		};
	}
	
	
	
}