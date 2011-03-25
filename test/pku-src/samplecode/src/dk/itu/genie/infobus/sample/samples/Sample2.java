package dk.itu.genie.infobus.sample.samples;

import dk.itu.genie.infobus.sample.AbstractSample;
import dk.itu.infobus.ws.*;
import java.util.Map;

import org.eclipse.jetty.util.log.Log;

public class Sample2 extends AbstractSample {

	@Override
	protected void doSample(EventBus eb) throws Exception {
		// TODO Auto-generated method stub
		eb.addGenerator(new FooProducer());
	    eb.addListener(new FooBarTransformer());
	    eb.addListener(new BarListener());
	}

	@Override
	protected boolean waitForExit() {
		// TODO Auto-generated method stub
		return true;
	}

}

class FooProducer extends GeneratorAdapter {
	Thread fooThread;
	boolean running = true;

	FooProducer() {
		// the name of the generator is sample2.foo.producer, and the events
		// will have a single field sample2.foo
		super("sample2.foo.producer", "sample2.foo");
	}

	// when closed, the thread will be shut down
	@Override
	protected void cleanUp() throws Exception {
		running = false;
	}

	// when started, a thread will be started too
	// that emits an event every second
	@Override
	public void onStarted() {
		fooThread = new Thread() {
			@Override
			public void run() {
				while (running) {
					publish(new EventBuilder().put("sample2.foo",
							"foo-" + System.currentTimeMillis()).getEvent());

					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				Log.debug("Closing sample2.foo.producer thread");
				// 这里有一行错误
				// dbg("Closing sample2.foo.producer thread");
			}
		};
		fooThread.start();
	}
}

class FooBarTransformer extends Transformer {
	// a transformer is a Listener too, so it accepts a List<ListenerToken> as
	// parameter
	FooBarTransformer() {
		super(new PatternBuilder().addMatchAll("sample2.foo").addUndefined(
				"sample2.bar").getPattern());
		// we have to set a linked generator too.
		// the name of the generator is sample2.foobar.generator, and the fields
		// emitted will be sample2.foo and sample2.bar
		setGenerator(new LinkedGenerator("sample2.foobar.generator",
				"sample2.foo", "sample2.bar"));
	}

	// when an event is received, we add the sample2.bar field and return
	// the same event.
	@Override
	protected Map<String, Object> transform(Map<String, Object> msg)
			throws Exception {
		String foo = (String) msg.get("sample2.foo");
		msg.put("sample2.bar", "You said: " + foo);
		return msg;
	}
}

class BarListener extends Listener {
    BarListener() {
        super(new PatternBuilder().addMatchAll("sample2.bar").getPattern());
    }

    @Override
    public void cleanUp() throws Exception {
    }

    @Override
    public void onMessage(Map<String, Object> msg) {
        Util.traceEvent(msg);
    }
    @Override
    public void onStarted() {
    }
}