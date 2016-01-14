// Test this external editor thing
/*
	SerialEvent occurs whenever a new data comes in the
	hardware serial RX.	This routine is run between each
	time loop() runs, so using delay inside loop can delay
	response.	Multiple bytes of data may be available.
*/
void serialEvent() {
	digitalWrite(COM_LED, LOW);
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
			serDataAvail = true;
			if (irCmd > 99) {
				serDataAvail = false;
				Serial.println("Invalid command");
				irCmd = 9999;
			}
			// clear the string for new input:
			inString = "";
		}
	}
}

/**
	replySerial
	answer command sent by serial port
	returns command to client
*/
void replySerial() {
	boolean isValidCmd = false;
	int waitTimeOut = 0;
	DynamicJsonBuffer jsonBuffer;

	// Prepare json object for the response
	JsonObject& root = jsonBuffer.createObject();
	root["result"] = "fail";
	root["device"] = AC_ID;
	parseCmd(root);
	// Send the response to the client
	Serial.println();
	root.printTo(Serial);
	Serial.flush();
	serDataAvail = false;
	digitalWrite(COM_LED, HIGH);
}

