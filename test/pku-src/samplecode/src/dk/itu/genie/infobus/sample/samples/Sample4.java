package dk.itu.genie.infobus.sample.samples;

import java.util.Map;

import dk.itu.genie.infobus.sample.AbstractSample;
import dk.itu.infobus.ws.*;

public class Sample4 extends AbstractSample {

	/**
	*@param EventBus eb, the EventBus instance
	*@param String resultField, a field that the answer event must contains
    *@param Map<String,Object> queryEvent, the event to be sent to the system, to which the queried service has to answer
	*@param long timeout, the max milliseconds to wait for an answer. If it takes longer, null is returned
	**/
	@Override
	protected void doSample(EventBus eb) throws Exception {
		// TODO Auto-generated method stub
		Map<String, Object> rep = Quirk.DoQuery(eb, "terminals",
				new EventBuilder().put("current.terminals.in",
						"itu.zone4.zone4c").getEvent(), 10000);
		// check is rep is null, which may happen if timeout has passed
		if (rep != null) {
			int num = ((Long) rep.get("terminals")).intValue();
			for (int i = 0; i < num; i++) {
				String btmac = (String) rep.get("terminal" + i);
				System.out.println("The " + i + "th terminal is: " + btmac);
			}
		} else {
			System.out.println("timed out!");
		}
		Quirk.SendMsg(eb, new EventBuilder().put("current.terminals.in",
				"itu.zone4.zone4c").getEvent());
	}

	@Override
	protected boolean waitForExit() {
		// TODO Auto-generated method stub
		return false;
	}
}
