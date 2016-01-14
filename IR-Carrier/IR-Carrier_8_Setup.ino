void setup() {
	pinMode(IR_LED_OUT, OUTPUT); // IR LED red
	pinMode(COM_LED, OUTPUT); // Communication LED blue
	pinMode(ACT_LED, OUTPUT); // Communication LED blue
	digitalWrite(IR_LED_OUT, LOW); // Turn off IR LED
	digitalWrite(COM_LED, HIGH); // Turn off LED
	digitalWrite(ACT_LED, HIGH); // Turn off LED

	Serial.begin(115200);
	Serial.flush();
	Serial.setDebugOutput(false);
	Serial.println("");
	Serial.println("Hello from ESP8266");

	WiFi.mode(WIFI_STA);
	WiFi.config(ipAddr, ipGateWay, ipSubNet);
	WiFi.begin(ssid, password);
	Serial.print("Waiting for WiFi connection ");
	while (WiFi.status() != WL_CONNECTED) {
		delay(500);
		Serial.print(".");
	}
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
	Serial.println(F("23 - Fan Speed"));
	Serial.println(F("30 - Plus"));
	Serial.println(F("31 - Minus"));
	Serial.println(F("40 - Timer"));
	Serial.println(F("41 - Sweep"));
	Serial.println(F("42 - Turbo"));
	Serial.println(F("43 - Ion"));
	Serial.println(F("98 - Auto function on"));
	Serial.println(F("99 - Auto function off"));

	/* Asume aircon off, timer off, power control enabled, */
	/* aircon mode fan, fan speed low, temperature set to 25 deg Celsius */
	// TODO save status in EEPROM and read it from there after reboot
	acMode = acMode | AUTO_ON | TIM_OFF | AC_OFF | MODE_FAN | FAN_LOW | TUR_OFF | SWP_OFF | ION_OFF;
	acTemp = acTemp & TEMP_CLR; // set temperature bits to 0
	acTemp = acTemp + 25; // set temperature bits to 25

	// Get first values from spMonitor
	//Only used in main control ESP on 192.168.0.142 address
	//getPowerVal(false);

	// Start update of consumption value every 60 seconds if enabled
	//if ((acMode & AUTO_ON) == AUTO_ON) {
	//	getPowerTimer.attach(60, triggerGetPower);
	//}

	ArduinoOTA.onStart([]() {
		Serial.println("OTA start");
		resetFanModeTimer.detach();
		WiFiUDP::stopAll();
		WiFiClient::stopAll();
		server.close();
	});

	// Start OTA server.
	ArduinoOTA.setHostname(AC_ID);
	ArduinoOTA.begin();
}

