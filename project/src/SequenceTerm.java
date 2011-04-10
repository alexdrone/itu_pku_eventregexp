import dk.itu.infobus.ws.*;

/**
 * This class represent a single term for a sequence to build.
 * TODO: Is just working with static right-operand evaluation.
 */
public class SequenceTerm {
	
	public final static int KLEENE_STAR = -1;
	public final static int KLEENE_PLUS = -2;
	public final static int SINGLE = 1;

	/* the name of the field to compare */
	private String field;
	
	/* the needed operator */
	private PatternOperator operator;
	
	/* right value of the comparison (static) */
	private Object rValue;
	
	/* the cardinality of the term to be matched */
	private int cardinality;
	
	/* asserted when you want to avoid the match */
	private boolean negated = false;
	
	/**
	 * Builds a static sequence term from the given parameters 
	 */
	public static SequenceTerm create(String field, PatternOperator operator, 
	Object rValue, int cardinality) {
							
		SequenceTerm term = new SequenceTerm(); 			
		term.field = field;
		term.operator = operator;
		term.rValue = rValue;
		term.cardinality = cardinality;
		
		return term;
	}   
	
	public static SequenceTerm createNot(String field, PatternOperator op, 
	Object rValue, int cardinality) {
							
		SequenceTerm term = 
			SequenceTerm.create(field, op, rValue, cardinality);
		
		term.negated = true;
		return term;
	}
	
	/**
	 * Returns the name of the field to compare
	 * @return the sequence term to compare 
	 */
	public String getField() { return field; }
	
	/**
	 * @return The operator 
	 */
	public PatternOperator getPatternOperator() { return operator; }
	
	/**
	 * The object for the right-term evaluation
	 * @return The value to be compared to the field 
	 */
	public Object getRValue() { return rValue; }
	
	/**
	 * The number of occurences expected of this term
	 * @return an integer representing the cardinality 
	 */
	public int getCardinality() { return cardinality; }
}