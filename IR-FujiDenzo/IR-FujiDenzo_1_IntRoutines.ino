/**
	triggerGetPower
	called by Ticker getPowerTimer
	sets flag powerUpdateTriggered to true for handling in loop()
	will initiate a call to getPowerVal() from loop()
*/
void triggerGetPower() {
	powerUpdateTriggered = true;
}


