void loop() {
	// Handle OTA updates
	ArduinoOTA.handle();

	// Handle new client request on HTTP server if available
	WiFiClient client = server.available();
	if (client) {
		replyClient(client);
	}

	while (Serial.available()) {
		int inChar = Serial.read();
		if (isDigit(inChar)) {
			// convert the incoming byte to a char
			// and add it to the string:
			inString += (char)inChar;
		}
		// if you get a newline, print the string,
		// then the string's value:
		if (inChar == '\n') {
			irCmd = inString.toInt();
			// if (irCmd > 99) {
				// Serial.println("Invalid command");
				// irCmd = 9999;
			// }
			// clear the string for new input:
			Serial.println("inString after receiving \n " + inString);
			inString = "";
			Serial.println("irCmd after receiving \n " + String(irCmd));
			replySerial();
		}
	}

	if (irCmd == CMD_REMOTE_0) {
		if ((acMode & AC_ON) == AC_ON) { // AC is on
			irCmd = CMD_MODE_FAN;
			sendCmd();
			delay(1000);
			irCmd = CMD_ON_OFF;
			sendCmd();
		}
		irCmd = 9999;
	}

	if (irCmd == CMD_REMOTE_1) {
		if ((acMode & AC_ON) != AC_ON) {
			irCmd = CMD_ON_OFF;
			sendCmd();
			delay(1000);
		}
		irCmd = CMD_MODE_FAN;
		sendCmd();
		irCmd = 9999;
	}
	
	if (irCmd == CMD_REMOTE_2) {
		if ((acMode & AC_ON) != AC_ON) {
			irCmd = CMD_ON_OFF;
			sendCmd();
			delay(1000);
		}
		irCmd = CMD_MODE_AUTO;
		sendCmd();
		irCmd = 9999;
	}

	if (irCmd == CMD_RESET) {
		ESP.reset();
	}

	if (powerUpdateTriggered) {
		//Serial.println("Power Update triggered");
		powerUpdateTriggered = false;
		getPowerVal(true);
	}

	if (irCmd != 9999) {
		//Serial.println("Send command triggered");
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

