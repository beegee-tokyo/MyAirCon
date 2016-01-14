# MyAirCon
Remote control for air con using Adafruit HUZZAH ESP8266 modules<br />

## MyAirCon

Uses Adafruit HUZZAH ESP8266 modules to control air con<br />
Main task for the SP8266 module is to automatically switch on the air con if enough energy is produced by the solar panels.<br />
Optional the air con can be controlled over WiFi by an Android phone or tablet<br />

## More details about the project:
-- Link: http://www.instructables.com/id/ESP8266-WiFi-Controlled-Aircon-Remote/<br />
### ESP8266/Arduino part:
#### Hardware:
Adafruit HUZZAH ESP8266 (or similar ESP12 breakout) https://www.adafruit.com/products/2471<br />
NPN transistor (I took a S9014, because it was in my storage)<br />
330 Ohm resistor<br />
2x IR LED e.g. Vishay TSUS4300 https://ph.rs-online.com/web/p/ir-leds/7082835/<br />
spMonitor device to receive consumption status https://github.com/beegee-tokyo/spMonitor<br />
Arduino UNO (for recording the IR commands and used as a USBtoSerial converter to program the ESP8266) https://www.arduino.cc/en/Main/ArduinoBoardUno<br />
IR receiver (for recording the IR commands) e.g. Vishay TSOP58438 https://ph.rs-online.com/web/p/ir-receivers/7733845/<br />
#### Required libraries:
##### IRremote ESP8266 Library
-- Link: https://github.com/markszabo/IRremoteESP8266<br />
-- License: GNU LESSER GENERAL PUBLIC LICENSE Version 2.1, February 1999<br />

##### Arduino JSON library
-- Link: https://github.com/bblanchon/ArduinoJson<br />
-- License: MIT license<br />

##### Multi-Protocol Infrared Remote Library for the Arduino
-- Link: https://github.com/z3t0/Arduino-IRremote<br />
-- License: GNU LESSER GENERAL PUBLIC LICENSE Version 2.1, February 1999<br />
#### Software:
--> in subfolder DumpIR - IR signal recording program<br />
--> in subfolder IR-FujiDenzo - IR remote program for FujiDenzo air con<br />
### Android part:
#### Software:
--> This repository<br />
#### Required libraries:
###### OkHttp - an HTTP & SPDY client for Android and Java applications
-- Link: http://square.github.io/okhttp/<br />
-- License: Apache License, Version 2.0<br />
###### Okio - a modern I/O API for Java http://square.github.io/okio
-- Link: https://github.com/square/okio/<br />
-- License: Apache License, Version 2.0<br />
###### Java documentation
-- Link: TODO add link to documentation<br />

