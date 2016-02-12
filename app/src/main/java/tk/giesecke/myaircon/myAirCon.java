/**
 * myAirCon
 * <p/>
 * Main application UI thread
 *
 * @author Bernd Giesecke
 * @version 01 January 3, 2016.
 */
package tk.giesecke.myaircon;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class myAirCon extends AppCompatActivity implements View.OnClickListener {

	/** Flag for testing (show JSON responses) */
	public static boolean showDebugJSON = false;

	/** A HTTP client to access the spMonitor device */
	private static OkHttpClient client;
	/** The url to access the ESP8266 device */
	private String url = "";
	/** Flag for ongoing communication */
	private boolean isCommunicating = false;
	/** Access to shared preferences of application*/
	private static SharedPreferences mPrefs;
	/** Shared preferences value for device status */
	private final String prefsDeviceStatus = "deviceStatus";
	/** Shared preferences value for location name */
	private final String prefsLocationName = "locationName";
	/** Shared preferences value for location icon */
	private final String prefsDeviceIcon = "deviceIcon";
	/** Shared preferences value for last selected device */
	@SuppressWarnings("FieldCanBeLocal")
	private final String prefsSelDevice = "selDevice";

	/** ID of the selected device */
	public static int selDevice = 0;
	/** Max number of devices */
	// TODO limit to 2 devices as I don't plan to have more but can be extended
	public static final int MAX_DEVICE = 2;
	/** IP address of the selected device */
	private final String[] espIP = {"192.168.0.142",
			"192.168.0.143",
			"192.168.0.144",
			"192.168.0.145",
			"192.168.0.146",
			"192.168.0.147",
			"192.168.0.148",
			"192.168.0.149"};
	/** Name of the device */
	public static final String[] deviceName = {"", "", "", "", "", "", "", ""};
	/** Layout version for the device */
	public static final int[] deviceType = {99, 99, 99, 99, 99, 99, 99, 99};
	/** Valid device type ids */
	public static final int FUJIDENZO = 0;
	public static final int CARRIER = 1;
	/** Location of the device */
	private final String[] locationName = {"Office", "Living", "", "", "", "", "", ""};
	/** Icon for the device */
	private final int[] deviceIcon = {7, 6, 0, 0, 0, 0, 0, 0};
	/** Power status of device */
	public static final int[] powerStatus = {0, 0, 0, 0, 0, 0, 0, 0};
	/** Mode status of device */
	public static final int[] modeStatus = {0, 0, 0, 0, 0, 0, 0, 0};
	/** Fan speed of device */
	public static final int[] fanStatus = {0, 0, 0, 0, 0, 0, 0, 0};
	/** Cooling temperature of device */
	public static final int[] coolStatus = {0, 0, 0, 0, 0, 0, 0, 0};
	/** Consumption status of device (only from master device */
	public static double consStatus = 0;
	/** Auto power status of device (only from master device */
	public static int autoStatus = 0;
	/** Auto power enabled status of device */
	public static final int[] autoOnStatus = {0, 0, 0, 0, 0, 0, 0, 0};
	/** Sweep enabled status of device */
	public static final int[] sweepStatus = {0, 0, 0, 0, 0, 0, 0, 0};
	/** Turbo enabled status of device */
	public static final int[] turboStatus = {0, 0, 0, 0, 0, 0, 0, 0};
	/** Ion enabled status of device */
	public static final int[] ionStatus = {0, 0, 0, 0, 0, 0, 0, 0};
	/** Status of device 0=in search, 1=found, 2=not found */
	public static final int[] deviceStatus = {0, 0, 0, 0, 0, 0, 0, 0};
	/** SW build date of device */
	public static final String[] deviceBuild = {"", "", "", "", "", "", "", ""};

	private static final String CMD_ON_OFF = "00";

	private static final String CMD_MODE_AUTO = "10";
	private static final String CMD_MODE_COOL = "11";
	private static final String CMD_MODE_DRY = "12";
	private static final String CMD_MODE_FAN = "13";

	private static final String CMD_FAN_HIGH = "20";
	private static final String CMD_FAN_MED = "21";
	private static final String CMD_FAN_LOW = "22";
	private static final String CMD_FAN_SPEED = "23";

	private static final String CMD_TEMP_PLUS = "30";
	private static final String CMD_TEMP_MINUS = "31";

	/* Timer function not supported atm
	static final String CMD_OTHER_TIMER = "40";
	 */
	private static final String CMD_OTHER_SWEEP = "41";
	private static final String CMD_OTHER_TURBO = "42";
	private static final String CMD_OTHER_ION = "43";

	private static final String CMD_AUTO_ON = "98";
	private static final String CMD_AUTO_OFF = "99";

	/** Light of button to switch consumption control for FujiDenzo layout */
	private static View btAutoLightFD;
	/** Light of button to switch consumption control for Carrier layout */
	private static View btAutoLightCA;
	/** Light of button to switch on/off for FujiDenzo layout */
	private static View btOnOffLightFD;
	/** Light of button to switch on/off for Carrier layout */
	private static View btOnOffLightCA;
	/** Light of button to switch fan to high speed for FujiDenzo layout */
	private static View btFanHighLightFD;
	/** Light of button to switch fan to medium speed for FujiDenzo layout */
	private static View btFanMedLightFD;
	/** Light of button to switch fan to low speed for FujiDenzo layout */
	private static View btFanLowLightFD;
	/** Light of button to switch to cool mode for FujiDenzo layout */
	private static View btCoolLightFD;
	/** Light of button to switch to cool mode for Carrier layout */
	private static View btCoolLightCA;
	/** Light of button to switch to dry mode for FujiDenzo layout */
	private static View btDryLightFD;
	/** Light of button to switch to dry mode for Carrier layout */
	private static View btDryLightCA;
	/** Light of button to switch to fan mode for FujiDenzo layout */
	private static View btFanLightFD;
	/** Light of button to switch to fan mode for Carrier layout */
	private static View btFanLightCA;
	/** Light of button to switch on sweep for Carrier layout */
	private static View btSweepLightCA;
	/** Light of button to switch on turbo mode for Carrier layout */
	private static View btTurboLightCA;
	/** Light of button to switch on ion mode for Carrier layout */
	private static View btIonLightCA;
	/** Light of button to switch on auto temp function for Carrier layout */
	private static View btAutomLightCA;
	/** Light of button to switch to timer function for FujiDenzo layout */
	/* Timer function is not supported atm
	View static btTimerLightFD;
	*/
	/** Light of button to switch to timer function for Carrier layout */
	/* Timer function is not supported atm
	View static btTimerLightCA;
	*/
	/** Button to switch fan speed for Carrier layout */
	private static Button btFanCA;
	/** Consumption value display for FujiDenzo layout */
	private static TextView txtConsValFD;
	/** Temperature value display for FujiDenzo layout */
	private static TextView txtTempValFD;
	/** Status value display for FujiDenzo layout */
	private static TextView txtAutoStatusValFD;
	/** Consumption value display for Carrier layout */
	private static TextView txtConsValCA;
	/** Temperature value display for Carrier layout */
	private static TextView txtTempValCA;
	/** Status value display for Carrier layout */
	private static TextView txtAutoStatusValCA;
	/** Text field for debug messages */
	public static TextView debugTxt;
	/** Array with the ids of the menu items */
	private static final MenuItem[] menuContent = {null, null, null, null, null, null, null, null};
	/** Relative layout when no ESP devices were found */
	private static RelativeLayout noDeviceLayout;
	/** Relative layout for FujiDenzo aircon */
	private static RelativeLayout fdLayout;
	/** Relative layout for Carrier aircon */
	private static RelativeLayout caLayout;

	/** Color for activated button */
	private static int colorRed;
	/** Color for deactivated button */
	private static int colorGrey;

	/** Id of menu, needed to set user selected icons and device names */
	private Menu abMenu;
	/** View for dialog to select icon and device name */
	private View locationsView;
	/** View for selecting device to change icon and device name */
	private View locationSettingsView;
	/** Button ids from location selection dialog */
	private final int[] buttonIds = {R.id.dia_sel_device0,
			R.id.dia_sel_device1,
			R.id.dia_sel_device2,
			R.id.dia_sel_device3,
			R.id.dia_sel_device4,
			R.id.dia_sel_device5,
			R.id.dia_sel_device6,
			R.id.dia_sel_device7};
	/** R.id of selected icon for a device */
	private int dlgIconIndex;
	/** Resource ids of drawables for the icons */
	private final int[] iconIDs = {R.drawable.ic_bathroom,
			R.drawable.ic_bedroom,
			R.drawable.ic_dining,
			R.drawable.ic_entertainment,
			R.drawable.ic_kids,
			R.drawable.ic_kitchen,
			R.drawable.ic_livingroom,
			R.drawable.ic_office};
	/* Resource ids of the icon buttons */
	private final int[] iconButtons = {R.id.im_bath,
			R.id.im_bed,
			R.id.im_dining,
			R.id.im_entertain,
			R.id.im_kids,
			R.id.im_kitchen,
			R.id.im_living,
			R.id.im_office};
	/** Index of device handled in dialog box */
	private int dlgDeviceIndex;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_air_con);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		// Enable access to internet
		if (Build.VERSION.SDK_INT > 9) {
			/** ThreadPolicy to get permission to access internet */
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

		// Set all global variables
		setGlobalVar();

		client.setConnectTimeout(60, TimeUnit.SECONDS); // connect timeout
		client.setReadTimeout(60, TimeUnit.SECONDS);    // socket timeout

		// Check saved preferences if search for all devices is necessary
		mPrefs = getSharedPreferences("myAirCon", 0);
		for (int i = 0; i < MAX_DEVICE; i++) {
			deviceStatus[i] = mPrefs.getInt(prefsDeviceStatus + Integer.toString(i), 0);
			locationName[i] = mPrefs.getString(prefsLocationName + Integer.toString(i), "");
			deviceIcon[i] = mPrefs.getInt(prefsDeviceIcon + Integer.toString(i), 0);
		}
		// Get index of last selected device */
		selDevice = mPrefs.getInt(prefsSelDevice, 0);

		startRefreshAnim();
		if (deviceStatus[0] == 0) { // Complete search necessary
			for (int i = 0; i < MAX_DEVICE; i++) {
				url = "http://" + espIP[i] + "/?s";
				new callESP().execute("search", url, Integer.toString(i));
			}
		} else { // only refresh necessary
			// Pointer to text view for warning text */
			TextView warnText = (TextView) findViewById(R.id.txt_search_status);
			warnText.setText(R.string.txt_refreshing);

			// Limit search to devices we know already
			for (int i = 0; i < MAX_DEVICE; i++) {
				if (deviceStatus[i] == 1) {
					deviceStatus[i] = 0;
				}
			}
			// Refresh status of previously known devices
			for (int i = 0; i < MAX_DEVICE; i++) {
				if (deviceStatus[i] == 0) {
					url = "http://" + espIP[i] + "/?s";
					new callESP().execute("search", url, Integer.toString(i));
				}
			}
		}
		if (showDebugJSON) {
			debugTxt.setMovementMethod(new ScrollingMovementMethod());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_my_air_con, menu);
		// Save id of menu for later use */
		abMenu = menu;
		// Enable menu entries for the found devices
		for (int i = 0; i < MAX_DEVICE; i++) {
			menuContent[i] = menu.getItem(i);
			if (deviceStatus[i] == 1) {
				menuContent[i].setVisible(true);
				menuContent[i].setTitle(locationName[i]);
				//noinspection deprecation
				menuContent[i].setIcon(getResources().getDrawable(iconIDs[deviceIcon[i]]));
			}
		}

		/** Menu item to toggle debug option */
		MenuItem debugMenu = menu.getItem(10);
		if (showDebugJSON) {
			debugTxt.setMovementMethod(new ScrollingMovementMethod());
			debugMenu.setTitle(R.string.menu_debug_off);
		} else {
			debugMenu.setTitle(R.string.menu_debug_on);
		}

		return true;
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("InflateParams")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// Switch to another device if available
		/** Index of new selected device */
		int newDevice = 99;
		/** Pointer to text view for warning text */
		TextView warnText;
		switch (item.getItemId()) {
			case R.id.device_0:
				newDevice = 0;
				break;
			case R.id.device_1:
				newDevice = 1;
				break;
			case R.id.device_2:
				newDevice = 2;
				break;
			case R.id.device_3:
				newDevice = 3;
				break;
			case R.id.device_4:
				newDevice = 4;
				break;
			case R.id.device_5:
				newDevice = 5;
				break;
			case R.id.device_6:
				newDevice = 6;
				break;
			case R.id.device_7:
				newDevice = 7;
				break;
			case R.id.search:
				noDeviceLayout.setVisibility(View.VISIBLE);
				caLayout.setVisibility(View.GONE);
				fdLayout.setVisibility(View.GONE);
				// TODO add other device layouts here
				for (int i = 0; i < MAX_DEVICE; i++) {
					// Disable menu entries for the all devices
					menuContent[i].setVisible(false);
				}

				startRefreshAnim();

				warnText = (TextView) findViewById(R.id.txt_search_status);
				warnText.setText(R.string.txt_searching);

				for (int i = 0; i < MAX_DEVICE; i++) {
					deviceStatus[i] = 0;
				}
				for (int i = 0; i < MAX_DEVICE; i++) {
					url = "http://" + espIP[i] + "/?s";
					new callESP().execute("search", url, Integer.toString(i));
				}
				return true;
			case R.id.refresh:
				noDeviceLayout.setVisibility(View.VISIBLE);
				caLayout.setVisibility(View.GONE);
				fdLayout.setVisibility(View.GONE);
				startRefreshAnim();

				warnText = (TextView) findViewById(R.id.txt_search_status);
				warnText.setText(R.string.txt_refreshing);

				// Limit search to devices we know already
				for (int i = 0; i < MAX_DEVICE; i++) {
					if (deviceStatus[i] == 1) {
						deviceStatus[i] = 0;
					}
				}
				// Refresh status of previously known devices
				for (int i = 0; i < MAX_DEVICE; i++) {
					if (deviceStatus[i] == 0) {
						url = "http://" + espIP[i] + "/?s";
						new callESP().execute("search", url, Integer.toString(i));
					}
				}
				return true;
			case R.id.debug:
				if (showDebugJSON) {
					debugTxt.setMovementMethod(new ScrollingMovementMethod());
					item.setTitle(R.string.menu_debug_off);
					showDebugJSON = false;
					debugTxt.setText(R.string.txt_empty);
					invalidateOptionsMenu();
				} else {
					item.setTitle(R.string.menu_debug_on);
					showDebugJSON = true;
					invalidateOptionsMenu();
				}
				return true;
			case R.id.locations:
				// get location_selector.xml view
				/** Layout inflater for device selection dialog */
				LayoutInflater dialogInflater = LayoutInflater.from(this);
				locationSettingsView = dialogInflater.inflate(R.layout.location_selector, null);
				/** Alert dialog builder for device selection dialog */
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

				// set location_selector.xml to alert dialog builder
				alertDialogBuilder.setView(locationSettingsView);

				/** Pointer to button, used to set OnClickListener for buttons in the dialog */
				Button btToSetOnClickListener;
				/** Pointer to button text, used to give each button in the dialog a specific name */
				String buttonTxt;

				for (int i = 0; i < MAX_DEVICE; i++) {
					if (deviceStatus[i] == 1) {
						btToSetOnClickListener = (Button) locationSettingsView.findViewById(buttonIds[i]);
						btToSetOnClickListener.setVisibility(View.VISIBLE);
						if (locationName[i].equalsIgnoreCase("")) {
							btToSetOnClickListener.setText(deviceName[i]);
						} else {
							buttonTxt = locationName[i];
							btToSetOnClickListener.setText(buttonTxt);
						}
						btToSetOnClickListener.setOnClickListener(this);
					}
				}

				// set dialog message
				alertDialogBuilder
						.setTitle(getResources().getString(R.string.dialog_selector_title))
						.setCancelable(false)
						.setNegativeButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										for (int i = 0; i < MAX_DEVICE; i++) {
											mPrefs.edit().putInt(
													prefsDeviceStatus + Integer.toString(i),
													deviceStatus[i]).apply();
											mPrefs.edit().putString(
													prefsLocationName + Integer.toString(i),
													locationName[i]).apply();
											mPrefs.edit().putInt(
													prefsDeviceIcon + Integer.toString(i),
													deviceIcon[i]).apply();
										}
										dialog.cancel();
									}
								});

				// create alert dialog
				/** Alert dialog  for device selection */
				AlertDialog alertDialog = alertDialogBuilder.create();

				// show it
				alertDialog.show();

				return true;
		}

		if (deviceType[newDevice] != 99) { // Device is available
			selDevice = newDevice;
			/** Text view to show location name */
			TextView locationText;
			/** Image view to show location icon */
			ImageView locationIcon;
			switch (deviceType[newDevice]) {
				case FUJIDENZO:
					locationText = (TextView) findViewById(R.id.txt_device_fd);
					locationText.setText(locationName[selDevice]);
					locationIcon = (ImageView) findViewById(R.id.im_icon_fd);
					locationIcon.setImageDrawable(getResources().getDrawable(iconIDs[deviceIcon[selDevice]]));
					noDeviceLayout.setVisibility(View.GONE);
					caLayout.setVisibility(View.GONE);
					fdLayout.setVisibility(View.VISIBLE);
					break;
				case CARRIER:
					locationText = (TextView) findViewById(R.id.txt_device_ca);
					locationText.setText(locationName[selDevice]);
					locationIcon = (ImageView) findViewById(R.id.im_icon_fd);
					locationIcon.setImageDrawable(getResources().getDrawable(iconIDs[deviceIcon[selDevice]]));
					noDeviceLayout.setVisibility(View.GONE);
					fdLayout.setVisibility(View.GONE);
					caLayout.setVisibility(View.VISIBLE);
					break;
				// TODO here is the place to add more layout versions for air cons
			}
			mPrefs = getSharedPreferences("myAirCon", 0);
			mPrefs.edit().putInt(prefsSelDevice, selDevice).apply();

			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@SuppressLint("InflateParams")
	@Override
	public void onClick(View view) {
		if (!isCommunicating) {
			url = "";
			switch (view.getId()) {
				case R.id.bt_auto_fd:
				case R.id.bt_auto_ca:
					if (autoOnStatus[selDevice] == 1) {
						url = "http://" + espIP[selDevice] + "/?c=" + CMD_AUTO_OFF;
					} else {
						url = "http://" + espIP[selDevice] + "/?c=" + CMD_AUTO_ON;
					}
					break;
				case R.id.bt_on_off_fd:
				case R.id.bt_on_off_ca:
					url = "http://" + espIP[selDevice] + "/?c=" + CMD_ON_OFF;
					break;
				case R.id.bt_fan_high_fd:
					if (fanStatus[selDevice] != 2) {
						url = "http://" + espIP[selDevice] + "/?c=" + CMD_FAN_HIGH;
					}
					break;
				case R.id.bt_fan_med_fd:
					if (fanStatus[selDevice] != 1) {
						url = "http://" + espIP[selDevice] + "/?c=" + CMD_FAN_MED;
					}
					break;
				case R.id.bt_fan_low_fd:
					if (fanStatus[selDevice] != 0) {
						url = "http://" + espIP[selDevice] + "/?c=" + CMD_FAN_LOW;
					}
					break;
				case R.id.bt_autom_ca:
					if (modeStatus[selDevice] != 3) {
						url = "http://" + espIP[selDevice] + "/?c=" + CMD_MODE_AUTO;
					}
					break;
				case R.id.bt_cool_fd:
				case R.id.bt_cool_ca:
					if (modeStatus[selDevice] != 2) {
						url = "http://" + espIP[selDevice] + "/?c=" + CMD_MODE_COOL;
					}
					break;
				case R.id.bt_dry_fd:
				case R.id.bt_dry_ca:
					if (modeStatus[selDevice] != 1) {
						url = "http://" + espIP[selDevice] + "/?c=" + CMD_MODE_DRY;
					}
					break;
				case R.id.bt_fan_fd:
				case R.id.bt_fan_ca:
					if (modeStatus[selDevice] != 0) {
						url = "http://" + espIP[selDevice] + "/?c=" + CMD_MODE_FAN;
					}
					break;
				case R.id.bt_sweep_ca:
					url = "http://" + espIP[selDevice] + "/?c=" + CMD_OTHER_SWEEP;
					break;
				case R.id.bt_turbo_ca:
					url = "http://" + espIP[selDevice] + "/?c=" + CMD_OTHER_TURBO;
					break;
				case R.id.bt_ion_ca:
					url = "http://" + espIP[selDevice] + "/?c=" + CMD_OTHER_ION;
					break;
				case R.id.bt_plus_fd:
				case R.id.bt_plus_ca:
					url = "http://" + espIP[selDevice] + "/?c=" + CMD_TEMP_PLUS;
					break;
				case R.id.bt_minus_fd:
				case R.id.bt_minus_ca:
					url = "http://" + espIP[selDevice] + "/?c=" + CMD_TEMP_MINUS;
					break;
				case R.id.bt_fanspeed_ca:
					url = "http://" + espIP[selDevice] + "/?c=" + CMD_FAN_SPEED;
					break;
				// TODO here is the place to add more commands for other air cons
				case R.id.dia_sel_device0:
				case R.id.dia_sel_device1:
				case R.id.dia_sel_device2:
				case R.id.dia_sel_device3:
				case R.id.dia_sel_device4:
				case R.id.dia_sel_device5:
				case R.id.dia_sel_device6:
				case R.id.dia_sel_device7:
					switch (view.getId()) {
						case R.id.dia_sel_device0:
							dlgDeviceIndex = 0;
							break;
						case R.id.dia_sel_device1:
							dlgDeviceIndex = 1;
							break;
						case R.id.dia_sel_device2:
							dlgDeviceIndex = 2;
							break;
						case R.id.dia_sel_device3:
							dlgDeviceIndex = 3;
							break;
						case R.id.dia_sel_device4:
							dlgDeviceIndex = 4;
							break;
						case R.id.dia_sel_device5:
							dlgDeviceIndex = 5;
							break;
						case R.id.dia_sel_device6:
							dlgDeviceIndex = 6;
							break;
						default:
							dlgDeviceIndex = 7;
							break;
					}
					// get location_selector.xml view
					/** Layout inflater for dialog to change device name and icon */
					LayoutInflater dialogInflater = LayoutInflater.from(this);
					locationsView = dialogInflater.inflate(R.layout.locations, null);
					/** Alert dialog builder for dialog to change device name and icon */
					AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
					// set location_selector.xml to alert dialog builder
					alertDialogBuilder.setView(locationsView);

					/** Button to set onClickListener for icon buttons in the dialog */
					ImageButton btOnlyClickListener;

					for (int i = 0; i < 8; i++) {
						btOnlyClickListener = (ImageButton) locationsView.findViewById(iconButtons[i]);
						btOnlyClickListener.setOnClickListener(this);
					}

					dlgIconIndex = deviceIcon[dlgDeviceIndex];
					highlightDlgIcon(iconButtons[dlgIconIndex]);

					/** Edit text field for the user selected device name */
					final EditText userInput = (EditText) locationsView.findViewById(R.id.dia_et_location);
					userInput.setText(locationName[dlgDeviceIndex]);

					// set dialog message
					alertDialogBuilder
							.setTitle(getResources().getString(R.string.dialog_change_title))
							.setCancelable(false)
							.setPositiveButton("OK",
									new DialogInterface.OnClickListener() {
										@SuppressWarnings("deprecation")
										public void onClick(DialogInterface dialog, int id) {
											locationName[dlgDeviceIndex] = userInput.getText().toString();
											deviceIcon[dlgDeviceIndex] = dlgIconIndex;
											// Update menu
											menuContent[dlgDeviceIndex] = abMenu.getItem(dlgDeviceIndex);
											menuContent[dlgDeviceIndex].setVisible(true);
											menuContent[dlgDeviceIndex].setTitle(locationName[dlgDeviceIndex]);
											menuContent[dlgDeviceIndex].setIcon(getResources()
													.getDrawable(iconIDs[dlgIconIndex]));
											invalidateOptionsMenu();
											// Update underlying dialog box with new device name
											/** Button of selection dialog that we are processing */
											Button btToChangeName = (Button) locationSettingsView.findViewById(buttonIds[dlgDeviceIndex]);
											btToChangeName.setText(locationName[dlgDeviceIndex]);
											locationSettingsView.invalidate();
											// Update UI
											/** Text view to show location name */
											TextView locationText;
											/** Image view to show location icon */
											ImageView locationIcon;
											if (deviceType[dlgDeviceIndex] == FUJIDENZO) {
												locationText = (TextView) findViewById(R.id.txt_device_fd);
												locationText.setText(locationName[dlgDeviceIndex]);
												locationIcon = (ImageView) findViewById(R.id.im_icon_fd);
												locationIcon.setImageDrawable(getResources().getDrawable(iconIDs[deviceIcon[dlgDeviceIndex]]));
											} else if (deviceType[dlgDeviceIndex] == CARRIER) {
												locationText = (TextView) findViewById(R.id.txt_device_ca);
												locationText.setText(locationName[dlgDeviceIndex]);
												locationIcon = (ImageView) findViewById(R.id.im_icon_ca);
												locationIcon.setImageDrawable(getResources().getDrawable(iconIDs[deviceIcon[dlgDeviceIndex]]));
											}
											// TODO add other aircon control layouts here
										}
									})
							.setNegativeButton("Cancel",
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int id) {
											dialog.cancel();
										}
									});

					// create alert dialog
					AlertDialog alertDialog = alertDialogBuilder.create();

					// show it
					alertDialog.show();
					break;
				case R.id.im_bath:
					dlgIconIndex = 0;
					highlightDlgIcon(iconButtons[dlgIconIndex]);
					break;
				case R.id.im_bed:
					dlgIconIndex = 1;
					highlightDlgIcon(iconButtons[dlgIconIndex]);
					break;
				case R.id.im_dining:
					dlgIconIndex = 2;
					highlightDlgIcon(iconButtons[dlgIconIndex]);
					break;
				case R.id.im_entertain:
					dlgIconIndex = 3;
					highlightDlgIcon(iconButtons[dlgIconIndex]);
					break;
				case R.id.im_kids:
					dlgIconIndex = 4;
					highlightDlgIcon(iconButtons[dlgIconIndex]);
					break;
				case R.id.im_kitchen:
					dlgIconIndex = 5;
					highlightDlgIcon(iconButtons[dlgIconIndex]);
					break;
				case R.id.im_living:
					dlgIconIndex = 6;
					highlightDlgIcon(iconButtons[dlgIconIndex]);
					break;
				case R.id.im_office:
					dlgIconIndex = 7;
					highlightDlgIcon(iconButtons[dlgIconIndex]);
					break;
			}
			if (!url.isEmpty()) {
				// Send command
				new callESP().execute("", url, "");
				// Ask for status to update
				url = "http://" + espIP[selDevice] + "/?s";
				new callESP().execute("", url, Integer.toString(selDevice));
				if (deviceType[selDevice] == CARRIER) { // 5 seconds delay between commands needed
					isCommunicating = true;
					final Handler handler = new Handler();
					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							isCommunicating = false;
						}
					}, 5000);
				}
			}
		}
	}

	/**
	 * Wrapper to send several parameters to onPostExecute of AsyncTask
	 *
	 * isSearchDevice = flag that device search is active
	 * deviceIndex = index of device that is investigated
	 * reqCmd = command to be sent to the ESP device
	 * comResult = return string as JSON from the ESP device
	 */
	public class onPostExecuteWrapper {
		public boolean isSearchDevice; // Flag if we are in search devices mode
		public int deviceIndex; // Index of device we sent to command / status request
		public String comResult; // Result of communication
	}

	/**
	 * Async task class to contact ESP device
	 * params[0] = flag if search for devices is started
	 * params[1] = url to be contacted
	 * params[2] = index of device that is searched
	 */
	private class callESP extends AsyncTask<String, String, onPostExecuteWrapper> {

		@Override
		protected onPostExecuteWrapper doInBackground(String... params) {

			/** Return values for onPostExecute */
			onPostExecuteWrapper result = new onPostExecuteWrapper();

			if (params[0].equalsIgnoreCase("search")) {
				result.isSearchDevice = true;
				result.deviceIndex = Integer.parseInt(params[2]);
			} else {
				result.isSearchDevice = false;
				result.deviceIndex = selDevice;
			}
			/** URL to be called */
			String urlString = params[1]; // URL to call

			if (BuildConfig.DEBUG) Log.d("myAirCon", "callESP = " + urlString);

			/** Request to ESP device */
			Request request = new Request.Builder()
					.url(urlString)
					.build();

			if (request != null) {
				try {
					/** Response from ESP device */
					Response response = client.newCall(request).execute();
					if (response != null) {
						result.comResult = response.body().string();
					}
				} catch (IOException e) {
					result.comResult = e.getMessage();
					try {
						if (result.comResult.contains("EHOSTUNREACH")) {
							result.comResult = getApplicationContext().getString(R.string.err_esp);
						}
						if (result.comResult.equalsIgnoreCase("")) {
							result.comResult = getApplicationContext().getString(R.string.err_esp);
						}
						return result;
					} catch (NullPointerException en) {
						result.comResult = getResources().getString(R.string.err_no_esp);
						return result;
					}
				}
			}

			if (result.comResult.equalsIgnoreCase("")) {
				result.comResult = getApplicationContext().getString(R.string.err_esp);
			}
			return result;
		}

		protected void onPostExecute(onPostExecuteWrapper result) {
			if (BuildConfig.DEBUG) Log.d("myAirCon", "Result of callESP = " + result);
			activityUpdate(result);
		}
	}

	/**
	 * Update UI with values received from ESP device
	 *
	 * @param result
	 *        result sent by onPostExecute
	 */
	private void activityUpdate(final onPostExecuteWrapper result) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (showDebugJSON) {
					/** Character sequence with current content of debug text view */
					CharSequence debugTxtVal = debugTxt.getText();
					debugTxtVal = debugTxtVal + "\n" + result.comResult;
					debugTxt.setText(debugTxtVal);
				}
				if (result.isSearchDevice) { // Was searching for available ESP devices on the net
					if (jsonParser(result)) {
						deviceStatus[result.deviceIndex] = 1;
					} else {
						deviceStatus[result.deviceIndex] = 2;
					}
					boolean searchIsFinished = true;
					for (int i = 0; i < MAX_DEVICE; i++) {
						if (deviceStatus[i] == 0) {
							searchIsFinished = false;
						}
					}
					if (searchIsFinished) {
						// Check if we found any devices
						selDevice = 9;
						for (int i = 0; i < MAX_DEVICE; i++) {
							if (deviceStatus[i] != 2) {
								selDevice = i;
								break;
							}
						}
						if (selDevice == 9) { // No devices found
							/** Text view for warning text */
							TextView warnText = (TextView) findViewById(R.id.txt_search_status);
							warnText.setText(R.string.err_no_esp);
						} else { // Write result of search to saved preferences if sweep found any device
							for (int i = 0; i < MAX_DEVICE; i++) {
								mPrefs.edit().putInt(
										prefsDeviceStatus + Integer.toString(i),
										deviceStatus[i]).apply();
								mPrefs.edit().putString(
										prefsLocationName + Integer.toString(i),
										locationName[i]).apply();
								mPrefs.edit().putInt(
										prefsDeviceIcon + Integer.toString(i),
										deviceIcon[i]).apply();
							}
							// Get index of last selected device */
							selDevice = mPrefs.getInt(prefsSelDevice, 9);
							if ((selDevice == 9) || (deviceStatus[selDevice] != 1)) {
								for (int i = 0; i < MAX_DEVICE; i++) {
									if (deviceStatus[i] != 2) {
										selDevice = i;
										break;
									}
								}
							}
							switch (deviceType[selDevice]) {
								case FUJIDENZO:
									caLayout.setVisibility(View.GONE);
									fdLayout.setVisibility(View.VISIBLE);
									break;
								case CARRIER:
									fdLayout.setVisibility(View.GONE);
									caLayout.setVisibility(View.VISIBLE);
									break;
								// TODO here is the place to add more layout versions for air cons
							}
							noDeviceLayout.setVisibility(View.GONE);
						}

						// Enable menu entries for the found devices
						for (int i = 0; i < MAX_DEVICE; i++) {
							if (deviceStatus[i] != 2) {
								menuContent[i].setVisible(true);
								menuContent[i].setTitle(locationName[i]);
							}
						}
						stopRefreshAnim();
					}
				} else { // Status request or command
					jsonParser(result);
				}
			}
		});
	}

	/**
	 * Parse JSON and show received status in UI
	 *
	 * @param result
	 *            onPostExecuteWrapper
	 *               isSearchDevice = flag that device search is active
	 *               deviceIndex = index of device that is investigated
	 *               reqCmd = command to be sent to the ESP device
	 *               comResult = return string as JSON from the ESP device
	 * @return <code>boolean</code>
	 *            true - JSON parsed and status variables updated
	 *            false - result was "fail" or result was not JSON
	 */
	private boolean jsonParser(onPostExecuteWrapper result) {

		try {
			/** JSON object with the result from the ESP device */
			JSONObject deviceResult = new JSONObject(result.comResult);
			if (deviceResult.has("result")) {
				if (deviceResult.getString("result").equalsIgnoreCase("success")) {
					if (deviceResult.has("device")) {
						deviceName[result.deviceIndex] = deviceResult.getString("device");
						if (deviceName[result.deviceIndex].substring(0, 2).equalsIgnoreCase("fd")) {
							deviceType[result.deviceIndex] = FUJIDENZO;
						}
						if (deviceName[result.deviceIndex].substring(0, 2).equalsIgnoreCase("ca")) {
							deviceType[result.deviceIndex] = CARRIER;
						}
						// TODO here is the place to add more layout versions for air cons
					}
					if (deviceResult.has("power")) {
						powerStatus[result.deviceIndex] = deviceResult.getInt("power");
					}
					if (deviceResult.has("mode")) {
						modeStatus[result.deviceIndex] = deviceResult.getInt("mode");
					}
					if (deviceResult.has("speed")) {
						fanStatus[result.deviceIndex] = deviceResult.getInt("speed");
					}
					if (deviceResult.has("temp")) {
						coolStatus[result.deviceIndex] = deviceResult.getInt("temp");
					}
					if (deviceResult.has("cons")) {
						consStatus = deviceResult.getDouble("cons");
					}
					if (deviceResult.has("status")) {
						autoStatus = deviceResult.getInt("status");
					}
					if (deviceResult.has("auto")) {
						autoOnStatus[result.deviceIndex] = deviceResult.getInt("auto");
					}
					if (deviceResult.has("sweep")) {
						sweepStatus[result.deviceIndex] = deviceResult.getInt("sweep");
					}
					if (deviceResult.has("turbo")) {
						turboStatus[result.deviceIndex] = deviceResult.getInt("turbo");
					}
					if (deviceResult.has("ion")) {
						ionStatus[result.deviceIndex] = deviceResult.getInt("ion");
					}
					if (deviceResult.has("build")) {
						deviceBuild[result.deviceIndex] = deviceResult.getString("build");
					}
					// TODO here is the place to add more status for other air cons

					// Update UI
					updateUI(result.deviceIndex);
				} else {
					if (BuildConfig.DEBUG)
						Log.d("myAirCon", "Communication result = " + result.comResult);
					return false;
				}
			}
		} catch (JSONException e) {
			if (BuildConfig.DEBUG) Log.d("myAirCon", "Received invalid JSON = " + result.comResult);
			return false;
		}
		return true;
	}

	/**
	 * Update UI fields with the latest status of a device
	 *
	 * @param deviceIndex
	 *            Index of the device to be updated
	 */
	public static void updateUI(int deviceIndex) {
		String consText = String.format("%.0f", consStatus) + "W";
		String tempText = Integer.toString(coolStatus[deviceIndex]) + "C";
		String statusText = Integer.toString(autoStatus);

		switch (deviceType[deviceIndex]) {
			case FUJIDENZO:
				btOnOffLightFD.setBackgroundColor(
						(powerStatus[deviceIndex] == 1) ? colorRed : colorGrey);
				switch (modeStatus[deviceIndex]) {
					case 0: // Fan mode
						btCoolLightFD.setBackgroundColor(colorGrey);
						btDryLightFD.setBackgroundColor(colorGrey);
						btFanLightFD.setBackgroundColor(colorRed);
						break;
					case 1: // Dry mode
						btCoolLightFD.setBackgroundColor(colorGrey);
						btDryLightFD.setBackgroundColor(colorRed);
						btFanLightFD.setBackgroundColor(colorGrey);
						break;
					case 2: // Cool mode
						btCoolLightFD.setBackgroundColor(colorRed);
						btDryLightFD.setBackgroundColor(colorGrey);
						btFanLightFD.setBackgroundColor(colorGrey);
						break;
				}
				switch (fanStatus[deviceIndex]) {
					case 0: // Fan low mode
						btFanHighLightFD.setBackgroundColor(colorGrey);
						btFanMedLightFD.setBackgroundColor(colorGrey);
						btFanLowLightFD.setBackgroundColor(colorRed);
						break;
					case 1: // Fan medium mode
						btFanHighLightFD.setBackgroundColor(colorGrey);
						btFanMedLightFD.setBackgroundColor(colorRed);
						btFanLowLightFD.setBackgroundColor(colorGrey);
						break;
					case 2: // Fan high mode
						btFanHighLightFD.setBackgroundColor(colorRed);
						btFanMedLightFD.setBackgroundColor(colorGrey);
						btFanLowLightFD.setBackgroundColor(colorGrey);
						break;
				}
				txtConsValFD.setText(consText);
				txtTempValFD.setText(tempText);
				txtAutoStatusValFD.setText(statusText);
				btAutoLightFD.setBackgroundColor(
						(autoOnStatus[deviceIndex] == 1) ? colorRed : colorGrey);
				break;
			case CARRIER:
				btOnOffLightCA.setBackgroundColor(
						(powerStatus[deviceIndex] == 1) ? colorRed : colorGrey);
				switch (modeStatus[deviceIndex]) {
					case 0: // Fan mode
						btAutomLightCA.setBackgroundColor(colorGrey);
						btCoolLightCA.setBackgroundColor(colorGrey);
						btDryLightCA.setBackgroundColor(colorGrey);
						btFanLightCA.setBackgroundColor(colorRed);
						break;
					case 1: // Dry mode
						btAutomLightCA.setBackgroundColor(colorGrey);
						btCoolLightCA.setBackgroundColor(colorGrey);
						btDryLightCA.setBackgroundColor(colorRed);
						btFanLightCA.setBackgroundColor(colorGrey);
						break;
					case 2: // Cool mode
						btAutomLightCA.setBackgroundColor(colorGrey);
						btCoolLightCA.setBackgroundColor(colorRed);
						btDryLightCA.setBackgroundColor(colorGrey);
						btFanLightCA.setBackgroundColor(colorGrey);
						break;
					case 3: // Auto mode
						btAutomLightCA.setBackgroundColor(colorRed);
						btCoolLightCA.setBackgroundColor(colorGrey);
						btDryLightCA.setBackgroundColor(colorGrey);
						btFanLightCA.setBackgroundColor(colorGrey);
						break;
				}
				switch (fanStatus[deviceIndex]) {
					case 0: // Fan low mode
						btFanCA.setText(R.string.bt_txt_fan_low);
						break;
					case 1: // Fan medium mode
						btFanCA.setText(R.string.bt_txt_fan_med);
						break;
					case 2: // Fan high mode
						btFanCA.setText(R.string.bt_txt_fan_high);
						break;
				}
				btSweepLightCA.setBackgroundColor(
						(sweepStatus[deviceIndex] == 1) ? colorRed : colorGrey);
				btTurboLightCA.setBackgroundColor(
						(turboStatus[deviceIndex] == 1) ? colorRed : colorGrey);
				btIonLightCA.setBackgroundColor(
						(ionStatus[deviceIndex] == 1) ? colorRed : colorGrey);

				txtConsValCA.setText(consText);
				txtTempValCA.setText(tempText);
				txtAutoStatusValCA.setText(statusText);
				btAutoLightCA.setBackgroundColor(
						(autoOnStatus[deviceIndex] == 1) ? colorRed : colorGrey);

				break;
			// TODO here is the place to add more layouts for other air cons
		}
	}

	/**
	 * Start animation of refresh icon in action bar
	 */
	private void startRefreshAnim() {
		/** Progressbar that will be shown during refresh */
		ProgressBar refreshRot = (ProgressBar) findViewById(R.id.pb_refresh_rot);
		refreshRot.setVisibility(View.VISIBLE);
		isCommunicating = true;
	}

	/**
	 * Stop animation of refresh icon in action bar
	 */
	private void stopRefreshAnim() {
		/** Progressbar that will be shown during refresh */
		ProgressBar refreshRot = (ProgressBar) findViewById(R.id.pb_refresh_rot);
		refreshRot.setVisibility(View.INVISIBLE);
		isCommunicating = false;
	}

	/**
	 * Set all global variables used
	 */
	private void setGlobalVar() {
		btAutoLightFD = findViewById(R.id.bt_auto_hl_fd);
		btAutoLightCA = findViewById(R.id.bt_auto_hl_ca);
		btOnOffLightFD = findViewById(R.id.bt_on_off_hl_fd);
		btOnOffLightCA = findViewById(R.id.bt_on_off_hl_ca);
		btFanHighLightFD = findViewById(R.id.bt_fan_high_hl_fd);
		btFanMedLightFD = findViewById(R.id.bt_fan_med_hl_fd);
		btFanLowLightFD = findViewById(R.id.bt_fan_low_hl_fd);
		btCoolLightFD = findViewById(R.id.bt_cool_hl_fd);
		btCoolLightCA = findViewById(R.id.bt_cool_hl_ca);
		btDryLightFD = findViewById(R.id.bt_dry_hl_fd);
		btDryLightCA = findViewById(R.id.bt_dry_hl_ca);
		btFanLightFD = findViewById(R.id.bt_fan_hl_fd);
		btFanLightCA = findViewById(R.id.bt_fan_hl_ca);
		btSweepLightCA = findViewById(R.id.bt_sweep_hl_ca);
		btTurboLightCA = findViewById(R.id.bt_turbo_hl_ca);
		btIonLightCA = findViewById(R.id.bt_ion_hl_ca);
		btAutomLightCA = findViewById(R.id.bt_autom_hl_ca);

		btFanCA = (Button) findViewById(R.id.bt_fanspeed_ca);

		txtConsValFD = (TextView) findViewById(R.id.txt_cons_val_fd);
		txtTempValFD = (TextView) findViewById(R.id.txt_temp_val_fd);
		txtAutoStatusValFD = (TextView) findViewById(R.id.txt_auto_status_val_fd);
		txtConsValCA = (TextView) findViewById(R.id.txt_cons_val_ca);
		txtTempValCA = (TextView) findViewById(R.id.txt_temp_val_ca);
		txtAutoStatusValCA = (TextView) findViewById(R.id.txt_auto_status_val_ca);

		debugTxt = (TextView) findViewById(R.id.txt_debug);
		if (!showDebugJSON) {
			debugTxt.setText(R.string.txt_empty);
		}

		noDeviceLayout = (RelativeLayout) findViewById(R.id.no_device);
		fdLayout = (RelativeLayout) findViewById(R.id.fuji_denzo);
		caLayout = (RelativeLayout) findViewById(R.id.carrier);

		//noinspection deprecation
		colorRed = getResources().getColor(android.R.color.holo_red_light);
		//noinspection deprecation
		colorGrey = getResources().getColor(android.R.color.darker_gray);

		client = new OkHttpClient();
	}

	/**
	 * Highlight selected icon in dialog to set device name and icon
	 * all other icons will be shown normal
	 *
	 * @param selIconID
	 *            index of icon that will be highlighted
	 */
	@SuppressWarnings("deprecation")
	private void highlightDlgIcon(int selIconID) {
		// deselect all buttons
		/** Image button in device change dialog used to deselect and highlight */
		ImageButton changeButton = (ImageButton) locationsView.findViewById(R.id.im_bath);
		changeButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
		changeButton = (ImageButton) locationsView.findViewById(R.id.im_bed);
		changeButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
		changeButton = (ImageButton) locationsView.findViewById(R.id.im_dining);
		changeButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
		changeButton = (ImageButton) locationsView.findViewById(R.id.im_entertain);
		changeButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
		changeButton = (ImageButton) locationsView.findViewById(R.id.im_kids);
		changeButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
		changeButton = (ImageButton) locationsView.findViewById(R.id.im_kitchen);
		changeButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
		changeButton = (ImageButton) locationsView.findViewById(R.id.im_living);
		changeButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
		changeButton = (ImageButton) locationsView.findViewById(R.id.im_office);
		changeButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
		switch (selIconID) {
			case R.id.im_bath:
				changeButton = (ImageButton) locationsView.findViewById(R.id.im_bath);
				changeButton.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
				break;
			case R.id.im_bed:
				changeButton = (ImageButton) locationsView.findViewById(R.id.im_bed);
				changeButton.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
				break;
			case R.id.im_dining:
				changeButton = (ImageButton) locationsView.findViewById(R.id.im_dining);
				changeButton.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
				break;
			case R.id.im_entertain:
				changeButton = (ImageButton) locationsView.findViewById(R.id.im_entertain);
				changeButton.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
				break;
			case R.id.im_kids:
				changeButton = (ImageButton) locationsView.findViewById(R.id.im_kids);
				changeButton.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
				break;
			case R.id.im_kitchen:
				changeButton = (ImageButton) locationsView.findViewById(R.id.im_kitchen);
				changeButton.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
				break;
			case R.id.im_living:
				changeButton = (ImageButton) locationsView.findViewById(R.id.im_living);
				changeButton.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
				break;
			case R.id.im_office:
				changeButton = (ImageButton) locationsView.findViewById(R.id.im_office);
				changeButton.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
				break;
		}
	}
}
