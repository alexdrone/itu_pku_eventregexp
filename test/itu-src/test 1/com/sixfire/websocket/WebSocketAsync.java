package com.sixfire.websocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

public class WebSocketAsync extends Thread {

	public interface WebsocketMessageHandler {
		void onMessage(String message);
		void onMessage(byte[] message);
		void onException(IOException e);
	}
	
	/** The url. */
	private URI mUrl;
	
	/** The socket. */
	private Socket mSocket;
	
	/** Whether the handshake is complete. */
	private boolean mHandshakeComplete;
	
	/** The socket input stream. */
	private InputStream mInput;
	
	/** The socket mOutput stream. */
	private OutputStream mOutput;
	
	/** The external headers. */
	private HashMap<String, String> mHeaders; 
	
	private WebsocketMessageHandler handler;
	
	private boolean closed = false;
	
	/**
	 * Creates a new WebSocket targeting the specified URL.
	 * @param url The URL for the socket.
	 */
	public WebSocketAsync(URI url,WebsocketMessageHandler handler) {
		mUrl = url;
		this.handler = handler;
		
		String protocol = mUrl.getScheme();
		if (!protocol.equals("ws") && !protocol.equals("wss")) {
			throw new IllegalArgumentException("Unsupported protocol: " + protocol);
		}
	}
	
	/**
	 * Sets extra headers to be sent.
	 * @param headers A hash of header name-values.
	 */
	public void setHeaders(HashMap<String, String> headers) {
		mHeaders = headers;
	}
	
	public void setWebSocketProcotol(String protocol) {
		addHeader("WebSocket-Protocol",protocol);
	}
	public void addHeader(String k,String v) {
		if(mHeaders == null) {
			mHeaders = new HashMap<String, String>();
		}
		mHeaders.put(k, v);
	}

	/**
	 * Returns the underlying socket;
	 */
	public Socket getSocket() {
		return mSocket;
	}
	
	/**
	 * Establishes the connection.
	 */
	public void connect() throws java.io.IOException {
		String host = mUrl.getHost();
		String path = mUrl.getPath();
		if (path.equals("")) {
			path = "/";
		}
		
		String query = mUrl.getQuery();
		if (query != null) {
			path = path + "?" + query;
		}
		
		String origin = "http://" + host;

		mSocket = createSocket();
		int port = mSocket.getPort();
		if (port != 80) {
			host = host + ":" + port;
		}
		
		mOutput = mSocket.getOutputStream();
		StringBuffer extraHeaders = new StringBuffer();
		if (mHeaders != null) {
			for (Entry<String, String> entry : mHeaders.entrySet()) {
				extraHeaders.append(entry.getKey() + ": " + entry.getValue() + "\r\n");				
			}
		}
		
		String request = "GET "+path+" HTTP/1.1\r\n" +
		         	     "Upgrade: WebSocket\r\n" +
		         	     "Connection: Upgrade\r\n" +
		         	     "Host: "+host+"\r\n" +
		         	     "Origin: "+origin+"\r\n" +
		         	     extraHeaders.toString() +
		         	     "\r\n";
		mOutput.write(request.getBytes());
		mOutput.flush();
		
		mInput = mSocket.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(mInput));
		String header = reader.readLine();
		if (!header.equals("HTTP/1.1 101 Web Socket Protocol Handshake")) {
			throw new IOException("Invalid handshake response 1 - "+header);
		}
		
		header = reader.readLine();
		if (!header.equals("Upgrade: WebSocket")) {
			throw new IOException("Invalid handshake response 2 - "+header);
		}
		
		header = reader.readLine();
		if (!header.equals("Connection: Upgrade")) {
			throw new IOException("Invalid handshake response 3 - "+header);
		}
		
		do {
			header = reader.readLine();
		} while (!header.equals(""));
		
		mHandshakeComplete = true;
		
		start();
	}
	
	private Socket createSocket() throws java.io.IOException {
		String scheme = mUrl.getScheme();
		String host = mUrl.getHost();
				
		int port = mUrl.getPort();
		if (port == -1) {
			if (scheme.equals("wss")) {
				port = 443;
			} else if (scheme.equals("ws")) {
				port = 80;
			} else {
				throw new IllegalArgumentException("Unsupported scheme");
			}
		}
		
		if (scheme.equals("wss")) {
			SocketFactory factory = SSLSocketFactory.getDefault();
			return factory.createSocket(host, port);
		 } else {
			 return new Socket(host, port);
		 }
	}
	
	/**
	 * Sends the specified string as a data frame.
	 * @param str The string to send.
	 * @throws java.io.IOException
	 */
	public void send(String str) throws java.io.IOException {
		if (!mHandshakeComplete) {
			throw new IllegalStateException("Handshake not complete");
		}
		
		mOutput.write(0x00);
		mOutput.write(str.getBytes("UTF-8"));
		mOutput.write(0xff);
		mOutput.flush();
	}
	
	public void send(byte[] message) throws java.io.IOException {
		send(message,0,message.length);
	}
	public void send(byte[] message, int offset, int len) throws java.io.IOException {
		if (!mHandshakeComplete) {
			throw new IllegalStateException("Handshake not complete");
		}
		int l_bytes = (len>16384)?3:(len>128)?2:1;
		byte[] blen_buf = new byte[l_bytes];
		int idx = 0;
		switch (l_bytes)
        {
            case 3:
                blen_buf[idx] = ((byte)(0x80|(len>>14))); idx++;
            case 2:
                blen_buf[idx] = ((byte)(0x80|(0x7f&(len>>7)))); idx++;
            case 1:
                blen_buf[idx] = ((byte)(0x7f&len));
        }
		
		dbg("sending " + len + " bytes");
		
		mOutput.write(0x80);
		mOutput.write(blen_buf);
		mOutput.write(0x80);
		mOutput.write(message, offset, len);
//		mOutput.write(0xff);
		mOutput.flush();
	}
	
	public static boolean DBG = false;
	static final Logger log = Logger.getLogger(WebSocketAsync.class.getName());
	private void dbg(String debug) {
		if(DBG){
			log.info(debug);
		}
	}

	/**
	 * Receives the next data frame.
	 * @return The received data.
	 * @throws java.io.IOException
	 */
	private String recv() throws java.io.IOException {
		if (!mHandshakeComplete) {
			throw new IllegalStateException("Handshake not complete");
		}
		dbg("recv");
		
		StringBuffer buf = new StringBuffer();

		int b = mInput.read();
		dbg("first byte read: "+b);
		if ((b & 0x80) == 0x80) {
			// Skip data frame
			int len = 0;
			do {
				b = mInput.read() & 0x7f;
				len = b * 128 + len;
			} while ((b & 0x80) != 0x80);
			
			for (int i = 0; i < len; i++) {
				mInput.read();
			}
		}
		
		while (true) {
			b = mInput.read();
			if (b == 0xff) {
				break;
			}
			dbg("add char "+b);

			buf.append((char)b);			
		}

		String out = new String(buf.toString().getBytes(), "UTF8");
		dbg("read: "+out);
		return out;
	}

	class ReadState {
		static final int MATCH_START_BINARY_LGT = 0;
		static final int READING_BINARY_LGT = 1;
		static final int READING_BINARY_DATA = 2;
		static final int MATCH_START_MSG = 3;
		static final int READING_MSG = 4;
		int state;
		byte[] buf;
		int read = 0;
		int curIndex = 0;
		int binaryLen = 0;
		int binaryRead = 0;
		
		byte[] msgData = null;
		byte[] binaryData = null;
		
		byte cur() {
			return buf[curIndex];
		}
		byte read() {
			return buf[curIndex++];
		}
		boolean hasMore() {
			return curIndex < read;
		}
		boolean hasAtLeastMore(int len) {
			return (curIndex+len-1) <= read;
		}
		void saveBinaryData() {
			if(binaryData == null) {
				binaryData = Arrays.copyOfRange(buf, curIndex, curIndex+(binaryLen-binaryRead));
				dbg("Copying buffer from: " + curIndex + ", length: " + (binaryLen-binaryRead) + ". Results in an array of length: "+binaryData.length);
			} else {
				System.arraycopy(buf, curIndex, binaryData, binaryRead, (binaryLen-binaryRead));
				dbg("Appending buffer from: " + curIndex + ", length: " + (binaryLen-binaryRead) + ". Results in an array of length: "+binaryData.length);
			}
		}
		void savePartialBinaryData() {
			if(binaryData == null) {
				binaryData = new byte[binaryLen];
			}
			dbg("Saving partial binary data - " + (read-curIndex) + " - all binary length: "+binaryLen + " - cur binary read: " + binaryRead);
			dbg(binaryLen - binaryData.length + " to go");
			System.arraycopy(buf, curIndex, binaryData, binaryRead, (read-curIndex));
		}
		void saveMessageData(int from, int len) {
			if(len == 0){ return; }
			if(msgData == null) {
				msgData = new byte[len];
				System.arraycopy(buf, from, msgData, 0, len);
			} else {
				byte[] tmp = Arrays.copyOf(msgData, msgData.length + len);
				System.arraycopy(buf, from, tmp, msgData.length, len);
				msgData = tmp;
			}
		}
		void saveMessageData(int from) {
			dbg("save partial message data");
			saveMessageData(from, read-from);
		}
		void messageCompleted() {
			state = MATCH_START_BINARY_LGT;
			binaryData = null;
			binaryLen = 0;
			binaryRead = 0;
//			bytes = null;
			msgData = null;
		}
		String state() {
			String o = "";
			switch(state) {
			case MATCH_START_BINARY_LGT: o = "Match start binary len"; break;
			case MATCH_START_MSG: o = "Match start message"; break;
			case READING_BINARY_DATA: o = "Read binary data"; break;
			case READING_BINARY_LGT: o = "Read binary len"; break;
			case READING_MSG: o = "Read message"; break;
			default: o = "Unknown! "+state;
			}
			return o;
		}
	}
	
	private void protocol(ReadState rs) {
		boolean cont = true;
		while(cont) {
			dbg("protocol state: "+rs.state());
			switch(rs.state) {
			case ReadState.MATCH_START_BINARY_LGT:
				cont = matchStartBinaryLength(rs); break;
			case ReadState.READING_BINARY_LGT:
				cont = readBinaryLength(rs); break;
			case ReadState.READING_BINARY_DATA:
				cont = readBinary(rs); break;
			case ReadState.MATCH_START_MSG:
				cont = matchStartMsg(rs); break;
			case ReadState.READING_MSG:
				cont = readMessage(rs); break;
			}
		}
//		if(cont) {
//			protocol(rs);
//		} else {
			dbg("need data!");
//		}
	}
	
	private boolean matchStartBinaryLength(ReadState rs){
		if(!rs.hasMore()){return false;}
		byte b = rs.read();
		if((b & 0x80) == 0x80) {
			rs.state = ReadState.READING_BINARY_LGT;
			return true;
		} else {
			rs.state = ReadState.MATCH_START_MSG;
			return true;
		}
	}
	private boolean readBinaryLength(ReadState rs){
		byte b; int t;
		do {
			b = rs.read();
			if((b & 0x80) == 0x80) {
				dbg("Receiving binary data: "+rs.binaryLen+" bytes");
				rs.state = ReadState.READING_BINARY_DATA;
				return true;
			} else {
				t = b & 0x7f;
				rs.binaryLen = t + (rs.binaryLen * 128);
			}
		} while(rs.hasMore());
		return false;
	}
	private boolean readBinary(ReadState rs){
		if(rs.hasAtLeastMore(rs.binaryLen - rs.binaryRead)) {
			dbg("Buffer has " + (rs.binaryLen  -rs.binaryRead) + " bytes, finish buffering binary-data frame");
			rs.saveBinaryData();
			try {
				handler.onMessage(rs.binaryData);
			} catch(Exception e){
				e.printStackTrace();
			}
			rs.state = ReadState.MATCH_START_BINARY_LGT;
			return true;
		} else {
			rs.savePartialBinaryData();
			return false;
		}
	}
	private boolean matchStartMsg(ReadState rs){
		rs.state = ReadState.READING_MSG;
		return true;
	}
	private boolean readMessage(ReadState rs){
		if(!rs.hasMore()){return false;}
		int curIdx = rs.curIndex;
		byte b;
		int len = 0;
		while(rs.hasMore()){
			b = rs.read();
			if ((b & 0xff) == 0xff) {
				dbg("message end frame");
				rs.saveMessageData(curIdx, len);
				try {
					dbg("creating string");
					String msg = new String(rs.msgData,"UTF8");
					dbg("sending message to handler! - " + msg);
					handler.onMessage(msg);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				dbg("message completed!");
//				System.out.print("/");
				rs.messageCompleted();
				return true;
			} else {
				len++;
			}
		}
		rs.saveMessageData(curIdx);
		return false;
	}
	
	@Override
	public void run() {
		ReadState rs = new ReadState();
		rs.buf = new byte[8192];
		rs.state = ReadState.MATCH_START_BINARY_LGT;
		while(true) {
			try {
//				int size = mInput.available();
//				while(size==0) {
//					try {
//						Thread.sleep(100);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} finally {
//						size = mInput.available();
//						if(size<0){break;}
//					}
//				}
//				rs.buf = new byte[size];
				rs.read = mInput.read(rs.buf);
				if(rs.read < 0) {
					dbg("End of stream!");
					throw new IOException("Stream closed");
				}
				rs.curIndex = 0;
//				System.out.println("\nread: " + rs.read);
				dbg("read "+rs.read+" bytes");
				protocol(rs);
			} catch(IOException e) {
				if(!closed) {
					e.printStackTrace();
					handler.onException(e);
					break;
				}
			}
		}
		System.out.println("closing websocket reading thread");
	}

	/**
	 * Closes the socket.
	 * @throws java.io.IOException
	 */
	public void close() throws java.io.IOException {
		try {
//			mInput.close();
//			mOutput.close();
			mSocket.close();
		} finally {
			closed = true;
			this.interrupt();
		}
	}
	
	public boolean isConnected() {
		return mHandshakeComplete;
	}
}
