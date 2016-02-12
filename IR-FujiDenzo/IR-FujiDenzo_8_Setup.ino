void setup() {
	pinMode(IR_LED_OUT, OUTPUT); // IR LED red
	pinMode(COM_LED, OUTPUT); // Communication LED blue
	pinMode(ACT_LED, OUTPUT); // Communication LED red
	digitalWrite(IR_LED_OUT, LOW); // Turn off IR LED
	digitalWrite(COM_LED, HIGH); // Turn off blue LED
	digitalWrite(ACT_LED, HIGH); // Turn off red LED

	Serial.begin(115200);

	Serial.setDebugOutput(false);
	Serial.println("");
	Serial.println("Hello from ESP8266");

	// Setup WiFi event handler
	WiFi.onEvent(WiFiEvent);
	
	// Try to connect to WiFi
	connectWiFi();

	Serial.println("");
	Serial.print("Connected to ");
	Serial.println(ssid);
	Serial.print("IP address: ");
	Serial.println(WiFi.localIP());

	// Start the web server to serve incoming requests
	server.begin();

	My_Sender.begin();

	Serial.println(F("00 - On/Off"));
	Serial.println(F("11 - Cool"));
	Serial.println(F("12 - Dry"));
	Serial.println(F("13 - Fan"));
	Serial.println(F("20 - High Fan"));
	Serial.println(F("21 - Medium Fan"));
	Serial.println(F("22 - Low Fan"));
	Serial.println(F("30 - Plus"));
	Serial.println(F("31 - Minus"));
	Serial.println(F("40 - Timer"));
	Serial.println(F("98 - Auto function on"));
	Serial.println(F("99 - Auto function off"));


	// Initialize file system.
	boolean foundStatus = SPIFFS.begin();
	if (foundStatus) { // File system found
		// Try to get last status from status.txt
		if (!readStatus()) {
			foundStatus = false;
		}
	}
	if (!foundStatus) // Could not get last status
	{
		/* Asume aircon off, timer off, power control enabled, */
		/* aircon mode fan, fan speed low, temperature set to 25 deg Celsius */
		acMode = acMode | AUTO_ON | TIM_OFF | AC_OFF | MODE_FAN | FAN_LOW;
		acTemp = acTemp & TEMP_CLR; // set temperature bits to 0
		acTemp = acTemp + 25; // set temperature bits to 25
	}

	// Get first values from spMonitor
	getPowerVal(false);

	// Start update of consumption value every 60 seconds if enabled
	if ((acMode & AUTO_ON) == AUTO_ON) {
		getPowerTimer.attach(60, triggerGetPower);
	}
	// Start sending status update every 5 minutes (5x60=300 seconds)
	// TODO testing to send only if status changed or power value changed by more than 10 Watt
	//sendUpdateTimer.attach(300, triggerSendUpdate);

	// Send aircon restart message
	sendBroadCast();
	inSetup = false;
	
	ArduinoOTA.onStart([]() {
		Serial.println("OTA start");
		ledFlasher.attach(0.1, redLedFlash); // Flash very fast if we started update
		getPowerTimer.detach();
		sendUpdateTimer.detach();
		WiFiUDP::stopAll();
		WiFiClient::stopAll();
		server.close();
	});

	// Start OTA server.
	ArduinoOTA.setHostname(DEVICE_ID);
	ArduinoOTA.begin();
}

