import java.util.Map;
import dk.itu.infobus.ws.*;

public class SequencePatternBuilder {
	
	private List<SequencePatternVariable> variableList = 
		new ArrayList<SequencePatternVariable>();
		
	private List<SequencePatternConstraint> constraintList = 
		new ArrayList<SequencePatternConstraint>();
	
	public SequencePatternBuilder addVariable(String name, int min, int max) {
		
		if (max < min || min < 0 || max < 0)
			throw new IllegalArgumentException("Invalid bounds");
		
		this.variableList.add(new SequencePatternVariable(name, min, max));
		return this;
	}
	
	public SequencePatternBuilder addConstraint(
		String lval, int lvalIndex, String lvalField, PatternOperator op, 
		String rval, int rvalIndex, String rvalField) {
					
		this.constraintList.add(new SequencePatternConstraint(lval, lvalIndex, lvalField, op, rval, rvalIndex, rvalField));
		return this;
	}
	
	public SequencePatternBuilder addConstraint(
		String lval, int lvalIndex, String lvalField, PatternOperator op,
		String rval) {
		
		this.constraintList.add(new SequencePatternConstraint(lval, lvalIndex, lvalField, op, rval));
		return this;
	}

	
	public class SequencePatternVariable {
		public String name;
		public int min, max;
		
		public SequencePatternVariable(String name, int min, int max) {
			this.name = name;
			this.min = min;
			this.max = max;
		}
	}
	
	public class SequencePatternConstraint {
		public String lval, rval, lvalField, rvalField;
		public int lvalIndex, rvalIndex;
		public PatternOperator op;
		public boolean isConstant;
		
		public SequencePatternConstraint(
			String lval, int lvalIndex, String lvalField, PatternOperator op, 
			String rval, int rvalIndex, String rvalField) {
		
			this.lval = lval;
			this.lvalIndex = lvalIndex;
			this.lvalField = lvalField;
			this.op = op;
			this.rval = rval;
			this.rvalIndex = rvalIndex;
			this.rvalField = rvalField;
			this.isConstant = false;
		}
		
		public SequencePatternConstraint(
			String lval, int lvalIndex, String lvalField, PatternOperator op, 
			String rval) {
		
			this.lval = lval;
			this.lvalIndex = lvalIndex;
			this.lvalField = lvalField;
			this.op = op;
			this.rval = rval;
			this.isConstant = true;
		}
		
	}
	
}

