package dk.itu.infobus.ws;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to build event patterns.
 * Uses chaining to quickly build patterns, e.g:
 * <code>
 * <pre>
 * List&lt;ListenerToken&lt;?&gt;&gt; pattern = new PatternBuilder()
 * 	.addMatchAll("foo")                 // match all values of field foo
 * 	.add("bar", PatternOperator.EQ, 10) // match bar == 10
 * 	.addUndefined("baz")                // field baz must not be present in the event
 * 	.getPattern();
 * </pre>
 * </code>
 * If you want to reuse instances of this class, remember to call <code>reset()</code>. This class is not thread-safe.
 * @author frza
 *
 */
@SuppressWarnings("unchecked")
public class PatternBuilder {

	List<ListenerToken<?>> pattern = new ArrayList<ListenerToken<?>>();
	
	public PatternBuilder add(ListenerToken<?> token) {
		pattern.add(token);
		return this;
	}
	public <T extends Comparable<T>> PatternBuilder add(String fieldName, PatternOperator operator, T... value) {
		pattern.add(new ListenerToken<T>(fieldName,operator,value));
		return this;
	}
	public PatternBuilder addMatchAll(String fieldName) {
		pattern.add(new ListenerToken(fieldName,PatternOperator.ANY));
		return this;
	}
	public PatternBuilder addUndefined(String fieldName) {
		pattern.add(new ListenerToken(fieldName,PatternOperator.UNDEF));
		return this;
	}
	
	public List<ListenerToken<?>> getPattern() {
		return pattern;
	}
	public void reset() {
		pattern.clear();
	}
}
