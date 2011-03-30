package dk.itu.infobus.ws;

import java.util.Map;

public class Util {
	public static void traceEvent(Map<String,Object> evt) {
		StringBuilder sb = new StringBuilder();
		sb.append("received event:\n");
		for(String k : evt.keySet()) {
			sb.append("\t").append(k).append(" = ").append(evt.get(k)).append("\n");
		}
		System.out.println(sb.toString());
	}
	
	public static String join(Object[] objs, String separator) {
		StringBuilder sb = new StringBuilder();
		boolean f = true;
		for(Object o : objs) {
			if(f){f=!f;}else{sb.append(separator);}
			sb.append(o.toString());
		}
		return sb.toString();
	}
}
