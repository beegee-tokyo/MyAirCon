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
		root["speed"] = testMode;
		if (isInFanMode) {
			root["fan"] = true;
		} else {
			root["fan"] = false;
		}
		testMode = acMode & SWP_MASK;
		if (testMode == SWP_ON) {
			root["sweep"] = 1;
		} else {
			root["sweep"] = 0;
		}
		testMode = acTemp & TUR_MASK;
		if (testMode == TUR_ON) {
			root["turbo"] = 1;
		} else {
			root["turbo"] = 0;
		}
		testMode = acTemp & ION_MASK;
		if (testMode == ION_ON) {
			root["ion"] = 1;
		} else {
			root["ion"] = 0;
		}
		testMode = acTemp & TEMP_MASK;
		root["temp"] = testMode;

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
	digitalWrite(COM_LED, HIGH);
}

