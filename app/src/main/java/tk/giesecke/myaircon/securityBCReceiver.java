package tk.giesecke.myaircon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class securityBCReceiver extends BroadcastReceiver {
	public securityBCReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String message = intent.getStringExtra("message");

		try {
			/** JSON object with the result from the ESP device */
			JSONObject deviceResult = new JSONObject(message);
			if (deviceResult.has("result")) {
				if (deviceResult.getString("result").equalsIgnoreCase("success")) {
					if (deviceResult.has("device")) {
						String sendingDevice = deviceResult.getString("device");
						int sendingID;
						for (sendingID = 0; sendingID < myAirCon.MAX_DEVICE; sendingID++) {
							if (myAirCon.deviceName[sendingID].equalsIgnoreCase(sendingDevice)) {
								if (sendingDevice.substring(0, 2).equalsIgnoreCase("fd")) {
									myAirCon.deviceType[sendingID] = myAirCon.FUJIDENZO;
								}
								if (sendingDevice.substring(0, 2).equalsIgnoreCase("ca")) {
									myAirCon.deviceType[sendingID] = myAirCon.CARRIER;
								}
								// TODO here is the place to add more layout versions for air cons
								if (deviceResult.has("power")) {
									myAirCon.powerStatus[sendingID] = deviceResult.getInt("power");
								}
								if (deviceResult.has("mode")) {
									myAirCon.modeStatus[sendingID] = deviceResult.getInt("mode");
								}
								if (deviceResult.has("speed")) {
									myAirCon.fanStatus[sendingID] = deviceResult.getInt("speed");
								}
								if (deviceResult.has("temp")) {
									myAirCon.coolStatus[sendingID] = deviceResult.getInt("temp");
								}
								if (deviceResult.has("cons")) {
									myAirCon.consStatus = deviceResult.getDouble("cons");
								}
								if (deviceResult.has("status")) {
									myAirCon.autoStatus = deviceResult.getInt("status");
								}
								if (deviceResult.has("auto")) {
									myAirCon.autoOnStatus[sendingID] = deviceResult.getInt("auto");
								}
								if (deviceResult.has("sweep")) {
									myAirCon.sweepStatus[sendingID] = deviceResult.getInt("sweep");
								}
								if (deviceResult.has("turbo")) {
									myAirCon.turboStatus[sendingID] = deviceResult.getInt("turbo");
								}
								if (deviceResult.has("ion")) {
									myAirCon.ionStatus[sendingID] = deviceResult.getInt("ion");
								}
								// TODO here is the place to add more status for other air cons

								if (myAirCon.selDevice == sendingID) {
									// Update UI
									myAirCon.updateUI(sendingID);
								}
								if (myAirCon.showDebugJSON) {
									/** Character sequence with current content of debug text view */
									CharSequence debugTxtVal = myAirCon.debugTxt.getText();
									debugTxtVal = debugTxtVal + "\n" + message;
									myAirCon.debugTxt.setText(debugTxtVal);
								}
							}
						}
					}
				}
			}
		} catch (JSONException e) {
			if (BuildConfig.DEBUG) Log.d("myAirCon", "Received invalid JSON = " + message);
		}
	}
}
