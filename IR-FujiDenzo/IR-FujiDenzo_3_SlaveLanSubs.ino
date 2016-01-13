/**
	switchSlaveAC
	Puts slave AC into requested mode
*/
boolean switchSlaveAC(IPAddress ipSlave, byte reqMode) {
	digitalWrite(COM_LED, LOW);
	const int httpPort = 80;
	if (!tcpClient.connect(ipSlave, httpPort)) {
		Serial.println("connection to slave AC " + String(ipSPM[0]) + "." + String(ipSPM[1]) + "." + String(ipSPM[2]) + "." + String(ipSPM[3]) + " failed");
		tcpClient.stop();
		digitalWrite(COM_LED, HIGH);
		return false;
	}

	switch (reqMode) {
		case CMD_REMOTE_0:
			tcpClient.print("GET /?c=" + String(CMD_REMOTE_0) + " HTTP/1.0\r\n\r\n");
			break;
		case CMD_REMOTE_1:
			tcpClient.print("GET /?c=" + String(CMD_REMOTE_1) + " HTTP/1.0\r\n\r\n");
			break;
		case CMD_REMOTE_2:
			tcpClient.print("GET /?c=" + String(CMD_REMOTE_2) + " HTTP/1.0\r\n\r\n");
			break;
	}

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
	digitalWrite(COM_LED, HIGH);
	if (!root.success()) {
		Serial.println("parseObject() failed");
		return false;
	}

	if (root["result"] == "success") {
		return true;
	}
	return false;
}

