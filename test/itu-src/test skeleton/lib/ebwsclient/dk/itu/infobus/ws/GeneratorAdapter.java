package dk.itu.infobus.ws;

import java.util.List;
import java.util.Map;

/**
 * This is just a convenience adapter class for event generators.
 * Refer to the dk.itu.infobus.ws.Generator documentation.
 * @author frza
 * @see dk.itu.infobus.ws.Generator
 *
 */
public class GeneratorAdapter extends Generator {

	public GeneratorAdapter(String name, List<String> fields) {
		super(name, fields);
	}
	public GeneratorAdapter(String name, String...fields) {
		super(name, fields);
	}

	@Override
	protected void cleanUp() throws Exception {
	}

	@Override
	protected void onActivationRequest(Map<String, Object> evt)
			throws Exception {
	}

	@Override
	protected void onDeactivationRequest(Map<String, Object> evt)
			throws Exception {
	}

	@Override
	protected void onStarted() throws Exception {
	}

	@Override
	protected void onSystemEvent(Map<String, Object> evt) throws Exception {
	}

}
