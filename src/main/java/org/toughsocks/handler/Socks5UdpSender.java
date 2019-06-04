
package org.toughsocks.handler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Socks5UdpSender {

	private final  static int MAX_PACKET_LENGTH = 8192;

	private static final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*2);

	public static void asyncSendData(byte[] data, String addr, int port, int timeout, SendResultHandler handler) throws IOException {
		executorService.execute(()->{
			try{
				DatagramPacket packetIn = new DatagramPacket(new byte[MAX_PACKET_LENGTH], MAX_PACKET_LENGTH);
				DatagramPacket packetOut = new DatagramPacket(data, data.length, InetAddress.getByName(addr),port);
				DatagramSocket socket = new DatagramSocket();
				socket.setSoTimeout(timeout);
				socket.send(packetOut);
				socket.receive(packetIn);
				byte[] rdata = Arrays.copyOfRange( packetIn.getData(),0, packetIn.getLength());
				handler.onResp(rdata);
			}catch (Exception e){
				handler.onError(e);
			}
		});
	}

	public interface SendResultHandler{
		 void onResp(byte [] data);
		 void onError(Exception ioex);
	}

}
