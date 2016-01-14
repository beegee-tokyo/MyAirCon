/**
	parseCmd
	Parse the received command
*/
void parseCmd(JsonObject& root) {
	String statResponse;
	// The following commands are followed independant of AC on or off
	switch (irCmd) {
		case CMD_REMOTE_0: // Auto mode controlled off
			if ((acMode & AUTO_ON) != AUTO_ON) {
				irCmd = 9999;
				root["result"] = "auto_off";
			} else {
				root["result"] = "success";
				root["cmd"] = CMD_REMOTE_0;
				powerStatus = 0;
			}
			break;
		case CMD_REMOTE_1: // Auto mode controlled fan mode
			if ((acMode & AUTO_ON) != AUTO_ON) {
				irCmd = 9999;
				root["result"] = "auto_off";
			} else {
				root["result"] = "success";
				root["cmd"] = CMD_REMOTE_1;
				powerStatus = 1;
			}
			break;
		case CMD_REMOTE_2: // Auto mode controlled auto cool mode
			if ((acMode & AUTO_ON) != AUTO_ON) {
				irCmd = 9999;
				root["result"] = "auto_off";
			} else {
				root["result"] = "success";
				root["cmd"] = CMD_REMOTE_2;
				powerStatus = 2;
			}
			break;
		case CMD_AUTO_ON: // Command to (re)start auto control
			acMode = acMode & AUTO_CLR;
			acMode = acMode | AUTO_ON;
			root["result"] = "success";
			root["cmd"] = CMD_AUTO_ON;
			break;
		case CMD_AUTO_OFF: // Command to stop auto control
			acMode = acMode & AUTO_CLR;
			acMode = acMode | AUTO_OFF;
			root["result"] = "success";
			root["cmd"] = CMD_AUTO_OFF;
			return;
			break;
		default: // Handle other commands
			if ((acMode & AC_ON) == AC_ON) { // AC is on
				root["result"] = "success";
				switch (irCmd) {
					case CMD_ON_OFF: // Switch aircon On/Off
						root["cmd"] = CMD_ON_OFF;
						break;
					case CMD_MODE_AUTO: // Switch to Auto
						root["cmd"] = CMD_MODE_AUTO;
						break;
					case CMD_MODE_COOL: // Switch to Cool
						root["cmd"] = CMD_MODE_COOL;
						break;
					case CMD_MODE_DRY: // Switch to Dry
						root["cmd"] = CMD_MODE_DRY;
						break;
					case CMD_MODE_FAN: // Switch to Fan
						root["cmd"] = CMD_MODE_FAN;
						break;
					case CMD_FAN_HIGH: // Switch to High Fan
						root["cmd"] = CMD_FAN_HIGH;
						break;
					case CMD_FAN_MED: // Switch to Medium Fan
						root["cmd"] = CMD_FAN_MED;
						break;
					case CMD_FAN_LOW: // Switch to Low Fan
						root["cmd"] = CMD_FAN_LOW;
						break;
					case CMD_FAN_SPEED: // Switch to next fan speed
						root["cmd"] = CMD_FAN_SPEED;
						break;
					case CMD_TEMP_PLUS: // Temp +
						root["cmd"] = CMD_TEMP_PLUS;
						break;
					case CMD_TEMP_MINUS: // Temp -
						root["cmd"] = CMD_TEMP_MINUS;
						break;
					case CMD_OTHER_TIMER: // Switch to Timer
						root["cmd"] = CMD_OTHER_TIMER;
						break;
					case CMD_OTHER_SWEEP: // Switch on/off sweep
						root["cmd"] = CMD_OTHER_SWEEP;
						break;
					case CMD_OTHER_TURBO: // Switch on/off turbo
						if (((acMode & MODE_COOL) == MODE_COOL) || ((acMode & MODE_AUTO) == MODE_AUTO)) {
							root["cmd"] = CMD_OTHER_TURBO;
						} else {
							root["result"] = "fail - AC not in cool mode";
							irCmd = 9999;
						}
						break;
					case CMD_OTHER_ION: // Switch on/off ion
						root["cmd"] = CMD_OTHER_ION;
						break;
					default:
						root["result"] = "fail - unknown command";
						irCmd = 9999;
						break;
				}
			} else { // AC is off
				root["result"] = "success";
				switch (irCmd) {
					case CMD_ON_OFF: // Switch aircon On/Off
						root["cmd"] = CMD_ON_OFF;
						break;
					default:
						root["result"] = "fail - AC is off";
						irCmd = 9999;
						break;
				}
			}
			break;
	}
}

