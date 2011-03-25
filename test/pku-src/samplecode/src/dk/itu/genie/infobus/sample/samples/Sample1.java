package dk.itu.genie.infobus.sample.samples;

import dk.itu.genie.infobus.sample.AbstractSample;
import dk.itu.infobus.ws.EventBus;
import java.util.Map;
import dk.itu.infobus.ws.*;

public class Sample1 extends AbstractSample {

	Generator sampleGenerator = new GeneratorAdapter("sample4.generator",
			"foo", "bar", "baz") {
		protected void onStarted() throws Exception {
			Thread t = new Thread() {
				public void run() {
					EventBuilder eb = new EventBuilder();
					int[] bars = { 0, 5, 10, 15, 20, 25, 30, 35, 40 };
					int[] bazs = { 0, 1, 2, 3, 4, 5, 6, 7, 8 };
					int i = 0;
					while (true) {
						eb.reset(); // calling reset simply discard the current
						// map
						try {
							Thread.sleep(500); // sleep for half second
							publish(eb.put("foo", "I am the foo field!").put(
									"bar", bars[i]).put("baz", bazs[i])
									.getEvent());
						} catch (Exception e) {
						}
						// just update the i variable to iterate on the bars and
						// bazs arrays
						i = (i + 1) % bars.length;
					}
				};
			};
			t.start();
		}
	};

	Listener sampleListener = new Listener(new PatternBuilder().addMatchAll(
			"foo").add("bar", PatternOperator.NEQ, 20).add("baz",
			PatternOperator.GT, 5).getPattern()) {
		public void cleanUp() throws Exception {
		}

		public void onStarted() {
			System.out.println("Listener started!");
		}

		public void onMessage(Map<String, Object> msg) {
			Util.traceEvent(msg);
		}
	};

	@Override
	protected void doSample(EventBus eb) throws Exception {
		// TODO Auto-generated method stub
		eb.addListener(sampleListener);
	    eb.addGenerator(sampleGenerator);
	}

	@Override
	protected boolean waitForExit() {
		// TODO Auto-generated method stub
		return true;
	}

}
