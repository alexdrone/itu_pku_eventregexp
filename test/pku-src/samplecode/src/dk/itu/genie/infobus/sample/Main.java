package dk.itu.genie.infobus.sample;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import dk.itu.infobus.ws.EventBus;

public class Main {
	static String Exit = "exit";
	static int CLASS_NAME = 0;
	static int HOST = 2;
	static int PORT = 3;
	static int DBG = 1;
	
	/**
	 * Get the pos-th argument in the program arguments. If the args's length is smaller than pos, then def is returned.
	 * @param pos index of the argument
	 * @param args program arguments
	 * @param def default value
	 * @return the pos-th argument, or default value
	 */
	static String getStr(int pos, String[] args, String def) {
		if (args.length > pos) {
			return args[pos];
		}
		return def;
	}

	/**
	 * Parse the pos-th argument. If the args's length is smaller than pos, then def is returned.
	 * @param pos index of the argument
	 * @param args program arguments
	 * @param def default value
	 * @return Integer.parseInt of the pos-th argument, or default value
	 */
	static int getInt(int pos, String[] args, int def) {
		if (args.length > pos) {
			return Integer.parseInt(args[pos]);
		}
		return def;
	}
	

	/**
	 * @param args
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
		String cname,host;
		int port;
		boolean dbg,wait;
		
		// get the name of the sample class. Default Sample1
		cname = getStr(CLASS_NAME,args,"Sample1");
		// get the eventbus host. Default tiger.itu.dk
		host = getStr(HOST,args,"tiger.itu.dk");
		
		// get the eventbus port. Default 8004
		port = getInt(PORT,args,8004);
		
		// is debug? if 1 yes, no otherwise (default 1)
		dbg  = 1 == getInt(DBG,args,1);
	
		if(dbg) {
			EventBus.DBG = true;
		}
		
		// add the package to the classname, if necessary
		if(!cname.contains(".")) {
			cname = "dk.itu.genie.infobus.sample.samples."+cname;
		}
		
		// load and instance the class
		Class<? extends AbstractSample> sampleclass = (Class<? extends AbstractSample>) Class.forName(cname);
		AbstractSample s = sampleclass.newInstance();
		
		// if true, the program will terminate when the user enter "exit"
		wait = s.waitForExit();
		
		// start the sample
		s.start(host,port);
		
		// if wait, read lines from the stdin until "exit" is read
		if(wait) {
			s.dbg("Waiting for exit");
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			String exit = "";
			while(!exit.equalsIgnoreCase(Exit)) {
				exit = reader.readLine();
			}
			s.dbg("ok, exit read, quitting");
		}
		
		// stop the sample
		s.stop();
		System.exit(1);
	}
}
