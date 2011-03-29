package dk.itu.infobus.ws;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * Convenience class for query-based services and to send an event without bothering about the reply (useful for implement command-based services).
 * @author frza
 *
 */
public class Quirk {

	/**
	 * Sends a message to the event bus. Does not care about a possible response.<br/>
	 * Each message is created using a random generated generator, which is then removed
	 * from the bus automatically.
	 * @param eb EventBus instance
	 * @param msg to be sent
	 * @throws Exception
	 */
	public static void SendMsg(EventBus eb, Map<String,Object> msg) throws Exception {
		String[] genFields = new String[msg.size()];
		int i = 0;
		for(String k : msg.keySet()) {
			genFields[i] = k;
			i++;
		}
		Generator g = new GeneratorAdapter(UUID.randomUUID().toString(),genFields);
		eb.addGenerator(g);
		g.publish(msg);
	}
	
	/**
	 * Query a service: the query message is sent using a random generated generator.<br/>
	 * The query message is augmented with a "txid" field, which is the field that identify 
	 * the response of the service.<br>The response message must contains the "resultField" parameter
	 * as a field.
	 * For example the call (in pseudocode):<br/>
	 * <code>
	 *   resp = Quirk.DoQuery(eb, "baz", { foo:1, bar:'bar' }, 10000)
	 * </code>
	 * <br/>
	 * will send the message <code>
	 * { foo:1, bar:'bar, txid:'1234567890' }
	 * </code> (where <code>txid</code> is a random generated UUID).<br/>
	 * If a response is received before the <code>timeout</code> parameter (in milliseconds), <br/>
	 * the response in returned (which will be like <code> { baz:'bazzz123', txid:'123456890' } </code>), <br/>
	 * otherwise <code>null</code> is returned.
	 * @param eb an EventBus instance
	 * @param resultField the field where (possibly a part of) the response is held
	 * @param qMsg the message that contains the query
	 * @param timeout in milliseconds
	 * @return
	 * @throws IOException
	 */
	public static Map<String,Object> DoQuery(EventBus eb, String resultField, Map<String,Object> qMsg, long timeout) throws IOException {
		String[] genFields = new String[qMsg.size()];
		int i = 0;
		for(String k : qMsg.keySet()) {
			genFields[i] = k;
			i++;
		}
		Query q = _q.new Query(new PatternBuilder().addMatchAll(resultField),UUID.randomUUID(),genFields);
		eb.addListener(q);
		return q.query(qMsg,timeout);
	}
	
	private static Quirk _q = new Quirk();
	private Quirk(){}
	
	private class Query extends Transformer {

		Map<String,Object> response;
		Object lock = new Object();
//		boolean wait = true;
		
		String txid;
		private Query(PatternBuilder builder, UUID txid, String...generatorFields) {
			super(builder.add("txid", PatternOperator.EQ, txid.toString()).getPattern());
			Transformer.LinkedGenerator g = new Transformer.LinkedGenerator(UUID.randomUUID().toString(), generatorFields);
			setGenerator(g);
			this.txid = txid.toString();
		}
		
		boolean configured = false;
		boolean stopped = false;
		@Override
		public void onStarted() {
			super.onStarted();
			configured = true;
		}
		
		protected Map<String,Object> query(Map<String,Object> qMsg, long timeout) {
			if(!qMsg.containsKey("txid")) {
				qMsg.put("txid", txid);
			}
			while(!configured) {
				System.out.println("unconfigured, sleep!");
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			this.generator.publish(qMsg);
//			if(wait) {
				stopped = true;
				synchronized(lock) {
					try {
						lock.wait(timeout);
						stopped = false;
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
//			}
			
			try {
				this.stop();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return response;
		}
		
		@Override
		protected Map<String, Object> transform(Map<String, Object> msg)
				throws Exception {
			response = msg;
			if(stopped) {
				synchronized(lock) {
					lock.notify();
				}
			}
			return null;
		}
		
	}
}
