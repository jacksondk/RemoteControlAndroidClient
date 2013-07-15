package dk.scicomp.remotecontrolclient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.os.AsyncTask;
import android.util.Log;

public class UdpPacketSender extends AsyncTask<String, Void, Boolean> {

	private InetAddress dest;
	
	public UdpPacketSender(String destinationIp){
		try {
			dest = InetAddress.getByName("192.168.0.25");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	protected Boolean doInBackground(String... messages ) {
		try
		{
			DatagramSocket socket = new DatagramSocket();			
			for( String value : messages){
				byte[] bytes = value.getBytes();
				DatagramPacket packet = new DatagramPacket(bytes, bytes.length,dest,50000);
				Log.d("UDP Send", value);
				socket.send(packet);
			}
		}
		catch (SocketException ex)
		{
			Log.e("Socket","Unable to send",ex);
		}
		catch(IOException ex){
			Log.e("Socket", "Unable to send", ex);
		}
		return true;
	}

}
