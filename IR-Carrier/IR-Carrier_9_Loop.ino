void loop() {
	// Handle OTA updates
	ArduinoOTA.handle();

	// Handle new client request on HTTP server if available
	WiFiClient client = server.available();
	if (client) {
		replyClient(client);
	}

	if (serDataAvail) {
		replySerial();
	}

	if (irCmd == CMD_REMOTE_0) {
		if ((acMode & AC_ON) == AC_ON) { // AC is on
			irCmd = CMD_MODE_FAN;
			sendCmd();
			delay(1000);
			irCmd = CMD_ON_OFF;
			sendCmd();
			powerStatus = 0;
		}
		irCmd = 9999;
	}

	if (irCmd == CMD_REMOTE_1) {
		if ((acMode & AC_ON) != AC_ON) { // AC is off
			irCmd = CMD_ON_OFF;
			sendCmd();
			delay(1000);
		}
		irCmd = CMD_MODE_FAN;
		sendCmd();
		irCmd = 9999;
		powerStatus = 1;
	}

	if (irCmd == CMD_REMOTE_2) {
		if ((acMode & AC_ON) != AC_ON) { // AC is off
			irCmd = CMD_ON_OFF;
			sendCmd();
			delay(1000);
		}
		irCmd = CMD_MODE_AUTO;
		sendCmd();
		irCmd = 9999;
		powerStatus = 2;
	}

	if (irCmd != 9999) {
		Serial.println("Send command triggered");
		sendCmd();
	}

	// Give a "I am alive" signal
	liveCnt++;
	if (liveCnt == 100000) {
		digitalWrite(ACT_LED, !digitalRead(ACT_LED));
	}
	if (liveCnt > 100000) {
		liveCnt = 0;
	}
}

