/**
	replyClient
	answer request on http server
	returns command to client
*/
void replyClient(WiFiClient httpClient) {
	digitalWrite(COM_LED, LOW);
	boolean isValidCmd = false;
	int waitTimeOut = 0;
	DynamicJsonBuffer jsonBuffer;

	// Prepare json object for the response
	JsonObject& root = jsonBuffer.createObject();
	root["result"] = "fail";
	root["device"] = AC_ID;

	// Wait until the client sends some data
	while (!httpClient.available()) {
		delay(1);
		waitTimeOut++;
		if (waitTimeOut > 3000) { // If no response for 3 seconds return
			root["result"] = "timeout";
			root.printTo(httpClient);
			httpClient.flush();
			httpClient.stop();
			digitalWrite(COM_LED, HIGH);
			return;
		}
	}

	// Read the first line of the request
	String req = httpClient.readStringUntil('\r');
	// Prepare the response
	String statResponse = "fail " + req;
	root["result"] = statResponse;
	if (req.substring(4, 8) == "/?c=") { // command received
		if (req.substring(9, 10) != " ") {
			statResponse = req.substring(8, 10);
			irCmd = statResponse.toInt();
			parseCmd(root);
		} else {
			irCmd = 9999;
		}
	} else if (req.substring(4, 7) == "/?s") { // status request received
		root["result"] = "success";
		// Display status of aircon
		if ((acMode & AC_ON) == AC_ON) {
			root["power"] = 1;
		} else {
			root["power"] = 0;
		}
		byte testMode = acMode & MODE_MASK;
		if (testMode == MODE_FAN) {
			root["mode"] = 0;
		} else if (testMode == MODE_DRY) {
			root["mode"] = 1;
		} else if (testMode == MODE_COOL) {
			root["mode"] = 2;
		} else if (testMode == MODE_AUTO) {
			root["mode"] = 3;
		}
		testMode = acMode & FAN_MASK;
		if (testMode == FAN_LOW) {
			root["speed"] = 0;
		} else if (testMode == FAN_MED) {
			root["speed"] = 1;
		} else if (testMode == FAN_HIGH) {
			root["speed"] = 2;
		}
		testMode = acTemp & TEMP_MASK;
		root["temp"] = testMode;

		// Display power consumption and production values
		/** Calculate average power consumption of the last 10 minutes */
		consPower = 0;
		for (int i = 0; i < 10; i++) {
			consPower += avgConsPower[i];
		}
		consPower = consPower / 10;

		//statResponse += String(consPower, 2) + "W\n";
		root["cons"] = consPower;

		// Display power cycle status
		root["status"] = powerStatus;

		// Display status of auto control by power consumption
		if ((acMode & AUTO_ON) == AUTO_ON) {
			root["auto"] = 1;
		} else {
			root["auto"] = 0;
		}
		root["device"] = AC_ID;
	} else if (req.substring(4, 7) == "/?r") { // initialization request received
		initAC();
		root["result"] = "success";
	}
	// Send the response to the client
	httpClient.print("HTTP/1.1 200 OK\r\nAccess-Control-Allow-Origin: *\r\nContent-Type: application/json\r\n\r\n");
	root.printTo(httpClient);
	httpClient.flush();
	httpClient.stop();
}

/**
	getPowerVal
	Get current power consumption from spMonitor device on address ipSPM
*/
void getPowerVal(boolean doPowerCheck) {
	digitalWrite(COM_LED, LOW);
	const int httpPort = 80;
	if (!tcpClient.connect(ipSPM, httpPort)) {
		Serial.println("connection to time server " + String(ipSPM[0]) + "." + String(ipSPM[1]) + "." + String(ipSPM[2]) + "." + String(ipSPM[3]) + " failed");
		tcpClient.stop();
		digitalWrite(COM_LED, HIGH);
		return;
	}

	tcpClient.print("GET /data/get HTTP/1.0\r\n\r\n");

	// Read all the lines of the reply from server and print them to Serial
	String line = "";
	int waitTimeOut = 0;
	while (tcpClient.connected()) {
		line = tcpClient.readStringUntil('\r');
		delay(1);
		waitTimeOut++;
		if (waitTimeOut > 2000) { // If no more response for 2 seconds return
			break;
		}
	}
	tcpClient.stop();
	/** Buffer for JSON string */
	DynamicJsonBuffer jsonBuffer;
	char json[line.length()];
	line.toCharArray(json, line.length() + 1);
	JsonObject& root = jsonBuffer.parseObject(json);
	if (!root.success()) {
		Serial.println("parseObject() failed");
		return;
	}

	// Switch between status depending on consumption
	consPower = root["value"]["C"];
	if (avgConsIndex < 10) {
		avgConsPower[avgConsIndex] = consPower;
		avgConsIndex++;
	} else {
		for (int i = 0; i < 9; i++) { // Shift values in array
			avgConsPower[i] = avgConsPower[i + 1];
		}
		avgConsPower[9] = consPower;
		if (doPowerCheck) {
			checkPower();
		}
	}
	// TODO next lines are for debug only
	/** Calculate average power consumption of the last 10 minutes */
	consPower = 0;
	//Serial.println("Last 10 power readings: ");
	for (int i = 0; i < 10; i++) {
		consPower += avgConsPower[i];
		//Serial.print(avgConsPower[i], 2);
		//Serial.print(":");
	}
	consPower = consPower / 10;
	//Serial.print("Current average power = ");
	//Serial.println(consPower, 2);

	digitalWrite(COM_LED, HIGH);
}

