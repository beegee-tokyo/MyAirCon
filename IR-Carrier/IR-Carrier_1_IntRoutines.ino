/**
	resetFanMode
	called by Ticker resetFanModeTimer
	resets flag for fan speed change mode
*/
void resetFanMode () {
	isInFanMode = false;
	resetFanModeTimer.detach();
}

