package dk.itu.infobus.ws;

public enum PatternOperator {

	EQ("="),
	NEQ("!="),
	GT(">"),
	GTEQ(">="),
	LT("<"),
	LTEQ("<="),
	ANY("ANY"),
	UNDEF("UNDEF")
	;
	String repr;
	private PatternOperator(String repr) {
		this.repr = repr;
	}
	public String getRepresentation() {
		return repr;
	}
	public static PatternOperator fromRepresentation(String repr) {
		for(PatternOperator po : values()) {
			if(po.repr.equals(repr))
				return po;
		}
		return null;
	}
}
