package dk.itu.genie.infobus.sample.samples;

import java.util.Map;
import dk.itu.genie.infobus.sample.AbstractSample;
import dk.itu.infobus.ws.*;

public class Sample3 extends AbstractSample {

	@Override
	protected void doSample(EventBus eb) throws Exception {
		// TODO Auto-generated method stub
		eb.addListener(new Listener(
                new PatternBuilder()
                .addMatchAll("terminal.btmac")
                .add("zone.current", PatternOperator.EQ, "itu.zone4.zone4c")
                .add("type", PatternOperator.EQ, "device.moved", "device.detected")
                .getPattern()
            ){

            public void cleanUp() throws Exception {
                dbg("Sample3 listener, cleanUp");
            }

            public void onMessage(Map<String, Object> msg) {
                Util.traceEvent(msg);
            }

            public void onStarted() {
                dbg("Sample3 listener, started");
            }
        });
    }


	@Override
	protected boolean waitForExit() {
		// TODO Auto-generated method stub
		return true;
	}

}
