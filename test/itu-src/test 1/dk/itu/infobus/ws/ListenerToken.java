package dk.itu.infobus.ws;

import java.util.HashMap;
import java.util.Map;


public class ListenerToken<T extends Comparable<T>> {
	static final Object MatchAll = "*";
	static final Object Undefined = "UNDEF";

	String fieldName;
	PatternOperator operator;
	T[] value;
	
	public ListenerToken( String fieldName, PatternOperator operator ) {
		this.fieldName =fieldName;
		this.operator = operator;
		this.value = null;
	}
	public ListenerToken( String fieldName, T... value ) {
		this(fieldName, PatternOperator.EQ, value);
	}
	public ListenerToken( String fieldName, PatternOperator operator, T... value ) {
		this.fieldName = fieldName;
		this.operator = operator;
		this.value = value;
	}
	
	private Object value() {
		if(PatternOperator.EQ.equals(operator) && value.length > 1) {
			return value;
		} else if(PatternOperator.EQ.equals(operator) && this.value == null) {
			return MatchAll;
		}
		return value[0];
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof ListenerToken)) {
			return false;
		}
		ListenerToken b = (ListenerToken)obj;
		if(!b.fieldName.equals(fieldName))
			return false;
		if(!b.operator.equals(operator))
			return false;
		if(b.value != null && value != null) {
			if(b.value.length != value.length)
				return false;
			for(int i=0;i<value.length;i++) {
				if( ! value[i].equals(b.value[i]) )
					return false;
			}
		} else if( b.value == null && value == null ) {
			return true;
		} else {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		return "ListenerToken {"+fieldName+" "+operator.name()+" "+Util.join(value, ",") + "}";
		//MessageFormat.format("ListenerToken {{0} {1} {2}}", fieldName, operator.name(),
				//Util.join(value, ",")) ;
	}
	
	@SuppressWarnings("serial")
	public Map<String,Object> toHashMap() {
		return new HashMap<String,Object>(){{
			put("field",fieldName);
			if(operator.equals(PatternOperator.ANY)) {
				put("operator",PatternOperator.EQ.repr);
				put("value",MatchAll);
			} else if(operator.equals(PatternOperator.UNDEF)) {
				put("operator",PatternOperator.EQ.repr);
				put("value",Undefined);
			} else {
				put("operator",operator.repr);
				put("value",value());
			}
		}};
	}
	
}
