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
	Serial.println();
	Serial.flush();
	digitalWrite(COM_LED, HIGH);
}

