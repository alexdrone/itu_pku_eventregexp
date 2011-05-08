import java.util.*;
import dk.itu.infobus.ws.*;

public class SequenceBuilder {

	/* the sequence of terms */
	public List<List<SequenceTermBuilder>> sequence = 
		new LinkedList<List<SequenceTermBuilder>>();
	
	/* conjuctions[i] is true if the i-term is asserted, false otherwise */
	public List<Boolean> conjunctions = new LinkedList<Boolean>(); 
	
	/**
	 * It add a new node to the current sequence.
	 * All the terms given as a parameter will be evaluated with an 'OR' 
	 * relationship.
	 * @param terms The given terms.
	 */
	public SequenceBuilder and(SequenceTermBuilder... terms) {
		
		/* the term is not negated */
		conjunctions.add(true);
		addTerms(terms);
		
		return this;
	}
	
	/**
	 * It add a new node to the current sequence.
	 * All the terms given as a parameter (negated) will be evaluated with an 
	 * 'OR' relationship.
	 * @param terms The given terms.
	 */
	public SequenceBuilder andNot(SequenceTermBuilder... terms) {
		
		/* the term is not negated */
		conjunctions.add(false);
		addTerms(terms);
		
		return this;
	}

	/* adds the terms to the sequence */
	private void addTerms(SequenceTermBuilder... terms) {
		
		/* creates a new node for the sequence */
		LinkedList<SequenceTermBuilder> node = 
			new LinkedList<SequenceTermBuilder>();
		
		for (SequenceTermBuilder t : terms) node.add(t);
		
		/* add the node to the current sequence */
		sequence.add(node);
	}
	
	/**
	 * Returns a <code>SequenceTerm</code> at the given index 
	 */ 
	public List<SequenceTermBuilder> get(int index) { 
		return sequence.get(index);
	}
	
	public boolean isNegated(int index) {
		return !conjunctions.get(index);
	}

	public int size() {
		return sequence.size();
	}
}