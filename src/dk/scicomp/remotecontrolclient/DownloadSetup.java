package dk.scicomp.remotecontrolclient;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import android.os.AsyncTask;
import android.util.Log;

public class DownloadSetup extends AsyncTask<URL, Void, Boolean>{

	private Document doc;
	private IDownloadComplete _callback;
	
	public interface IDownloadComplete {
		void DownloadIsComplete(Document doc);
	}
	
	
	public DownloadSetup(IDownloadComplete callback){
		_callback = callback;
	}
	
	@Override
	protected Boolean doInBackground(URL... arg0) {
		Log.d("DownloadSetup", "Starting download");
		try {
			String uri =
				    "http://192.168.0.25:50004/";

				URL url = new URL(uri);
				HttpURLConnection connection =
				    (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("GET");
				connection.setRequestProperty("Accept", "application/xml");

				InputStream xml = connection.getInputStream();

				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				doc = db.parse(xml);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("Socket","Unable to send",e);
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("Socket","Unable to send",e);
			return false;
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			Log.e("Socket","Unable to parse",e);
			e.printStackTrace();
			return false;
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			Log.e("Socket","Unable to parse",e);
			e.printStackTrace();
			return false;
		}		
		Log.d("Socket", "Download success");
		return true;
	}

	protected void onPostExecute(Boolean result)
	{
		if (_callback != null)
		{
			_callback.DownloadIsComplete(doc);
		}
	}
	
}
