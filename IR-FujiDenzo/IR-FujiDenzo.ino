/**
	Web controlled IR remote for FujiDenzo aircon

	Hardware
	Adafruit HUZZAH ESP8266 (ESP-12) module - https://www.adafruit.com/products/2471
	S9014 NPN transistor
	330 Ohm resistor
	2x IR led

	Receives commands for the aircon through WiFi.
	Checks production of solar panels
	Switch on/off the aircon depending on command or production of solar panels.

	@author Bernd Giesecke
	@version 0.1 beta December 2, 2015.
*/

/* Includes from libraties */
#include <Ticker.h>
#include <ESP8266WiFi.h>
#include <IRremoteESP8266.h>
#include <IRremoteInt.h>
#include <ArduinoJson.h>
#include <WiFiClient.h>
#include <ArduinoOTA.h>

#define AC_ID "fd1" // ID for aircon FujiDenzo in office room

/* wifiAPinfo.h contains wifi SSID and password */
/* file content looks like: */
/* Begin of file:
	const char* ssid = "YOUR_WIFI_SSID";
	const char* password = "YOUR_WIFI_PASSWORD";
	End of file */
#include "wifiAPinfo.h"

/** WiFiClient class to create TCP communication */
WiFiClient tcpClient;

/** WiFiServer class to create simple web server */
WiFiServer server(80);

/** IP address of this module */
IPAddress ipAddr(192, 168, 0, 142);
/** IP address of first slave */
IPAddress ipSlave1(192, 168, 0, 143);
/** Gateway address of WiFi access point */
IPAddress ipGateWay(192, 168, 0, 1);
/** Network mask of the local lan */
IPAddress ipSubNet(255, 255, 255, 0);
/** IP address of this module */
IPAddress ipSPM(192, 168, 0, 140);

/** Server address for OTA updates */
const char* host = "ota";

/** Received command (from lan or serial connection) */
int irCmd = 9999;
/** String to hold incoming command from serial port */
String inString = "";


/** Definition of available commands */
#define CMD_ON_OFF			00

#define CMD_MODE_AUTO		10
#define CMD_MODE_COOL		11
#define CMD_MODE_DRY		12
#define CMD_MODE_FAN		13

#define CMD_FAN_HIGH		20
#define CMD_FAN_MED			21
#define CMD_FAN_LOW			22
#define CMD_FAN_SPEED		23

#define CMD_TEMP_PLUS		30
#define CMD_TEMP_MINUS		31

#define CMD_OTHER_TIMER 	40
#define CMD_OTHER_SWEEP 	41
#define CMD_OTHER_TURBO 	42
#define CMD_OTHER_ION		43

#define CMD_RESET			70

#define CMD_REMOTE_0		80
#define CMD_REMOTE_1		81
#define CMD_REMOTE_2		82

#define CMD_AUTO_ON			98
#define CMD_AUTO_OFF		99

/** Mode status of aircon (only guess, as human could have used remote)
		acMode acTemp
		MSB ... LSB (2x8bit)
		satp mmff Titt tttt
		s = sweep enabled:	0 sweep off
							1 sweep on
		a = auto mode:		0 power control disabled
							1 power control enabled
		t = timer:			0 normal mode
							1 timer button pressed
		p = status:			0 off
							1 on
		mm = mode:			00 fan
							01 dry
							10 cool
							11 auto
		ff = fan speed:		00 low
							01 medium
							10 high
		T = turbo enabled:	0 turbo off
							1 turbo on
		i = ion enabled: 	0 ion off
							1 ion on
		tttttt = temperature:	010000 16 deg C
								010001 17 deg C
								010010 18 deg C
								010011 19 deg C
								010100 20 deg C
								010101 21 deg C
								010110 22 deg C
								010111 23 deg C
								011000 24 deg C
								011001 25 deg C
								011010 26 deg C
								011011 27 deg C
								011100 28 deg C
								011101 29 deg C
								011110 30 deg C
								011111 31 deg C
								100000 32 deg C
*/
byte acMode =		B00000000;
byte acTemp =		B00000000;
#define SWP_OFF		B00000000
#define SWP_ON		B10000000
#define SWP_CLR		B01111111
#define SWP_MASK	B10000000
#define AUTO_OFF	B00000000
#define AUTO_ON		B01000000
#define AUTO_CLR	B10111111
#define AUTO_MASK	B01000000
#define AUTO_ON		B01000000
#define TIM_OFF		B00000000
#define TIM_ON		B00100000
#define TIM_CLR		B11011111
#define TIM_MASK	B00100000
#define AC_OFF		B00000000
#define AC_ON		B00010000
#define AC_CLR		B11101111
#define AC_MASK		B00010000
#define MODE_FAN	B00000000
#define MODE_DRY	B00000100
#define MODE_COOL	B00001000
#define MODE_AUTO	B00001100
#define MODE_CLR	B11110011
#define MODE_MASK	B00001100
#define FAN_LOW		B00000000
#define FAN_MED		B00000001
#define FAN_HIGH	B00000010
#define FAN_CLR		B11111100
#define FAN_MASK	B00000011

#define TEMP_CLR	B11000000
#define TEMP_MASK	B00111111
#define TUR_OFF		B00000000
#define TUR_ON		B10000000
#define TUR_CLR		B01111111
#define TUR_MASK	B10000000
#define ION_OFF		B00000000
#define ION_ON		B01000000
#define ION_CLR		B10111111
#define ION_MASK	B01000000

/** Max selectable temperature */
#define MAX_TEMP 32
/** Min selectable temperature */
#define MIN_TEMP 16

/** IR LED on GPIO13 for communciation with aircon */
#define IR_LED_OUT 13
/** Blue LED on GPIO2 for communication activities */
#define COM_LED 2
/** Red LED on GPIO2 for aircon control activities */
#define ACT_LED 0

/** Timer to contact spMonitor server to get current consumption */
Ticker getPowerTimer;
/** Flag for request to contact spMonitor server to get current consumption */
boolean powerUpdateTriggered = false;

/** Instance of the IR sender */
IRsend My_Sender(IR_LED_OUT);

/* Status of aircon related to power consumption value of the house
 * If consumption is negative => production higher than consumption => switch on aircon
 * If consumption is positive => production lower than consumption => switch off aircon
 * Tresholds: TODO adapt values
 *						positive => Switch aircon off = status 0
 *						-100W => Switch on aircon in fan mode low = status 1
 *						additional -50W => Switch aircon to fan mode medium = status 2
 *						additional -50W => Switch aircon to fan mode high = status 3
 *						additional -200W => Switch aircon to cool mode 25 deg C = status 4
 *						additional -200W => Switch aircon to cool mode 16 deg C = status 5
 * Check every minute to switch aircon on, if on, check every 5 minutes before changing to another level
 */
byte powerStatus = 0;
/** Power consumption of the house from spMonitor */
double consPower = 0;
/** Collected power consumption of last 5 minutes of the house from spMonitor */
double avgConsPower[10] = {0,0,0,0,0,0,0,0,0,0};
/** Pointer to element in avgConsPower[] */
byte avgConsIndex = 0;

/** Counter for "I am alive" red LED blinking in loop() */
long liveCnt = 0;

