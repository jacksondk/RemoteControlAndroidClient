package dk.scicomp.remotecontrolclient;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import dk.scicomp.remotecontrolclient.DownloadSetup.IDownloadComplete;

import android.R.attr;
import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends FragmentActivity implements
		ActionBar.OnNavigationListener, IDownloadComplete, View.OnClickListener {

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * current dropdown position.
	 */
	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			showSettings();
			break;
		case R.id.reload_setup:
			startLoadSetup();
			break;
		}
		return true;
	}

	private void showSettings() {
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}

	private void startLoadSetup() {
		try {
			setProgressBarIndeterminateVisibility(true);
			new DownloadSetup(this).execute(new URL[] { new URL(
					"http://192.168.0.25:50004/") });
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			setProgressBarIndeterminateVisibility(false);
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("main", "Setup");
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_main);

		
		
		startLoadSetup();
	}

	public void sendMessage(View view) {
		Log.e("Msg", Integer.toBinaryString(view.getId()));
		Object tag = view.getTag();
		if (tag != null) {

		}

		switch (view.getId()) {
		case R.id.nextButton:
			publish("next");
			break;
		case R.id.playPauseButton:
			publish("playPause");
			break;
		case R.id.previousButton:
			publish("previous");
			break;
		case R.id.selectPcSpeaker:
			publish("selectPcSpeaker");
			break;
		case R.id.selectDigitalOut:
			publish("selectDigitalOut");
			break;
		case R.id.selectHdmi:
			publish("selectHdmi");
			break;
		}

	}

	private void publish(String value) {
		new UdpPacketSender("192.168.0.25").execute(new String[] { value });
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		// Restore the previously serialized current dropdown position.
		// if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
		// getActionBar().setSelectedNavigationItem(
		// savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
		// }
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// Serialize the current dropdown position.
		// outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar()
		// .getSelectedNavigationIndex());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onNavigationItemSelected(int position, long id) {

		if (commandModules == null)
			return true;

		CommandModule selectedModule = commandModules.get(position);

		FrameLayout layout = (FrameLayout) findViewById(R.id.container);
		GridLayout list = new GridLayout(this);
		layout.removeAllViewsInLayout();

		
		List<Command> commands = selectedModule.getCommands();
		list.setColumnCount(selectedModule.getColumns());
		list.setRowCount(selectedModule.getRows());
		for (int index = 0; index < commands.size(); index++) {
			Command command = commands.get(index);
			Button commandButton = new Button(this);
			commandButton.setText(command.getText());
			commandButton.setTag(command);
			commandButton.setOnClickListener(this);
			
			GridLayout.LayoutParams params = new GridLayout.LayoutParams();
			params.columnSpec = GridLayout.spec(command.getColumn());
			params.rowSpec = GridLayout.spec(command.getRow());
			commandButton.setLayoutParams(params);
			list.addView(commandButton);
		}
		layout.addView(list);
		
		return true;
	}

	public static class SoundSectionFragment extends Fragment {
		public SoundSectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.sound_control, container,
					false);

			return rootView;
		}

	}

	private List<CommandModule> commandModules;

	private class CommandModule {
		private String name;
		private ArrayList<Command> commands;
		private int rows;
		private int columns;

		public String getName() {
			return name;
		}
		public int getRows() {
			return rows;			
		}
		
		public int getColumns() {
			return columns;
		}

		public List<Command> getCommands() {
			return commands;
		}

		public CommandModule(String name, int rows, int columns) {
			this.name = name;
			this.rows = rows;
			this.columns = columns;
			this.commands = new ArrayList<Command>();
		}
	}

	private class Command {
		private String cmd;
		private String text;
		private int row;
		private int column;

		public String getCommand() {
			return cmd;
		}

		public String getText() {
			return text;
		}
		
		public int getRow() {
			return row;
		}
		
		public int getColumn() {
			return column;
		}

		public Command(String cmd, String text, int row, int column) {
			this.cmd = cmd;
			this.text = text;
			this.row = row;
			this.column = column;
		}
	}

	@Override
	public void DownloadIsComplete(Document doc) {
		if (doc == null)
			return;

		Log.d("mainview", "Download " + doc.toString());
		NodeList moduleNodes = doc.getElementsByTagName("module");
		commandModules = new ArrayList<CommandModule>();
		for (int moduleIndex = 0; moduleIndex < moduleNodes.getLength(); moduleIndex++) {
			NamedNodeMap attributes = moduleNodes.item(moduleIndex).getAttributes();
			String name = attributes.getNamedItem("name").getNodeValue();
			int rows = Integer.parseInt( attributes.getNamedItem("rows").getNodeValue());
			int columns = Integer.parseInt(attributes.getNamedItem("columns").getNodeValue());
			
			CommandModule module = new CommandModule(name,rows,columns);

			NodeList commands = moduleNodes.item(moduleIndex).getChildNodes();
			Log.d("mainview", "Create tab for module : " + name + " with "
					+ commands.getLength() + " commands");
			for (int commandIndex = 0; commandIndex < commands.getLength(); commandIndex++) {
				Node commandNode = commands.item(commandIndex);
				if (commandNode instanceof Element) {
					Element commandElement = (Element) commandNode;
					NamedNodeMap commandAttributes = commandElement.getAttributes();
					String command = commandAttributes.getNamedItem("cmd").getNodeValue();
					int row = Integer.parseInt(commandAttributes.getNamedItem("row").getNodeValue());
					int column = Integer.parseInt(commandAttributes.getNamedItem("column").getNodeValue());
					
					String text = commandElement.getChildNodes().item(0)
							.getNodeValue();
					Log.d("mainview", "  Create command : " + command
							+ ", text " + text);
					module.getCommands().add(new Command(command, text, row, column));
				}
			}
			commandModules.add(module);
		}

		SetupActionBar();
		setProgressBarIndeterminateVisibility(false);

	}

	private void SetupActionBar() {
		String[] moduleNames = new String[commandModules.size()];
		for (int index = 0; index < commandModules.size(); index++)
			moduleNames[index] = commandModules.get(index).getName();
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		actionBar.setListNavigationCallbacks(
				new ArrayAdapter<String>(actionBar.getThemedContext(),
						android.R.layout.simple_list_item_1,
						android.R.id.text1, moduleNames), this);
	}

	@Override
	public void onClick(View v) {
		Object tag = v.getTag();
		if (tag instanceof Command) {
			Command tagNode = (Command) tag;
			String cmd = tagNode.getCommand();
			publish(cmd);
		}
	}

}
