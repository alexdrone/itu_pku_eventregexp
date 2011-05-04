import java.util.*;
import dk.itu.infobus.ws.*;

/**
 * This class represent a single term for a sequence to build.
 * TODO: Is just working with static right-operand evaluation.
 */
public class SequenceTermBuilder {
	
	public final static int KLEENE_STAR = -1;
	public final static int KLEENE_PLUS = -2;
	public final static int SINGLE = 1;

	/* It contains all the criteria for a single term */
	private List<Map<String,Object>> terms = 
		new LinkedList<Map<String,Object>>();
	
	/* the cardinality of the term in the sequence */
	private int occurrences = SINGLE;
	
	/* tofix: put it protected - used to keep track
	 * of the sequence stack for this term */
	public InfiniteTermStack _stack;
	
	/**
	 * Adds a criteria to the current term-filter 
	 * @param field The fieldname 
	 * @param operator The <code>PatternOperator</code> to use
	 * @param value The right value in the expression
	 * @return <code>this</code> term builder.
	 */
	public SequenceTermBuilder add(String field, PatternOperator operator, 
	Object value) {
		
		if (value == null) 
			throw new IllegalArgumentException("Value cannot be null.");
			
		Map<String, Object> term = new HashMap<String, Object>();
		
		term.put("field", field);
		term.put("operator", operator);
		term.put("value", value);
		
		terms.add(term);
		
		return this;
	}
	
	/**
	 * Set the cardinality for this term
	 */
	public SequenceTermBuilder setOccurrences(int occurrences) {
		this.occurrences = occurrences;
		
		/* stack structure */
		if (this.occurrences == KLEENE_STAR || 
			this.occurrences == KLEENE_PLUS) _stack = new InfiniteTermStack();
		
		return this;
	}
	
	public List<Map<String,Object>> getCriteria() { return terms; }
	
	public int getOccurrences() { return occurrences; }
	
	
	public String toString() { return terms.toString(); }

}