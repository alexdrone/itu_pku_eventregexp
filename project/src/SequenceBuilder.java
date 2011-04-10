import java.util.*;
import dk.itu.infobus.ws.*;

public class SequenceBuilder {
	
	/* contains all the terms added to the sequence
	 * TODO: Is possible to have just 2 nested terms, the subsequences
	 * should (?) be inifinite. */
	private List<List<SequenceTerm>> terms = 
		new LinkedList<List<SequenceTerm>>();
	
	/* the name of the sequence */
	private String name;
	
	public SequenceBuilder(String name) {
		this.name = name;
	}
	
	/**
	 * Adds the given terms to the sequence
	 * @param objects the <code>SequenceTerms</code> to match 
	 * @return The current sequence
	 */
	public SequenceBuilder and(SequenceTerm... objects) { 
		
		List<SequenceTerm> list = new LinkedList<SequenceTerm>();
		for (SequenceTerm t : objects) list.add(t);
		
		this.terms.add(list); 
		return this;
	}
	
	/**
	 * Returns the terms of the list 
	 */
	public List<List<SequenceTerm>> getTerms() { return this.terms; }
	
	public String getName() { return name; }
	
}