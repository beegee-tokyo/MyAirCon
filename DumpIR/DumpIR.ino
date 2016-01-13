/*
 * IRremote: DumpIR - dump details of IR codes with IRrecv
 * Based on IRrecvDump exampler from IRremote library
 * http://www.righto.com/2009/08/multi-protocol-infrared-remote-library.html
 * // IRremote: IRrecvDump - dump details of IR codes with IRrecv
 * // An IR detector/demodulator must be connected to the input RECV_PIN.
 * // Version 0.1 July, 2009
 * // Copyright 2009 Ken Shirriff
 * // http://arcfn.com
 * // JVC and Panasonic protocol added by Kristian Lauszus (Thanks to zenwheel and other people at the original blog post)
 * // LG added by Darryl Smith (based on the JVC protocol)
 * Different to IRrecvDump, this example will print out the commands as binary arrays that can be integrated in your own
 * remote app very easy.
 * Used IR receiver: Vishay TSOP58438 https://ph.rs-online.com/web/p/ir-receivers/7733845/
 */

//------------------------------------------------------------------------------
// Include the IRremote library header
//
#include <IRremote.h>

//------------------------------------------------------------------------------
// Tell IRremote which Arduino pin is connected to the IR Receiver (TSOP4838)
//
int recvPin = 11;
IRrecv irrecv(recvPin);

//------------------------------------------------------------------------------
// This example code is for 2 different aircons
// 1) manufacturer FujiDenzo
// 2) manufacturer Carrier
// These aircons have different command sets
// Use #define FUJIDENZO for recording the FujiDenzo commands
// Use #define CARRIER for recording the Carrier commands
#define FUJIDENZO
// #define CARRIER

// Counter for repeated recording of command
int receiveLoop = 0;

// For testing of Fujidenzo (1 times same code) endReceiveLoop will be 1
// For testing of Carrier (3 times same code) we apply counter to separate commands
#ifdef FUJIDENZO
int endReceiveLoop = 1;
#endif
#ifdef CARRIER
int endReceiveLoop = 3;
#endif

// Counter for command;
int cmdIndex = 0;

#ifdef FUJIDENZO
// FujiDenzo remote has 10 buttons
int endCmdIndex = 10;
// Sequence of commands on the FujiDenzo remote
String cmdList[] = {"H-FAN", "M-FAN", "L-FAN", "TIMER", "TEMP+", "TEMP-", "COOL", "DRY", "FAN", "POWER"};
#endif
#ifdef CARRIER
// Carrier remote has 14 buttons
int endCmdIndex = 14;
// Sequence of commands on the Carrier remote
String cmdList[] = {"POWER", "SWEEP", "TEMP+", "TEMP-", "FANSPEED", "TURBO", "TIMER", "DRY", "SLEEP", "ION", "HEAT", "COOL", "FAN", "AUTO"};
#endif

// We test each command 3 times
int cmdTestCnt = 0;

//+=============================================================================
// Configure the Arduino
//
void  setup ( )
{
  Serial.begin(115200);   // Status message will be sent to PC at 115200 baud
  irrecv.enableIRIn();  // Start the receiver
  Serial.println("Start testing, now press " + cmdList[0]);
}

//+=============================================================================
// Display IR code
//
void  ircode (decode_results *results)
{
  // Panasonic has an Address
  if (results->decode_type == PANASONIC) {
    Serial.print(results->address, HEX);
    Serial.print(":");
  }

  // Print Code
  Serial.print("B");
  Serial.print(results->value, BIN);
}

//+=============================================================================
// Display encoding type
//
void  encoding (decode_results *results)
{
  switch (results->decode_type) {
    default:
    case UNKNOWN:      Serial.print("UNKNOWN");       break ;
    case NEC:          Serial.print("NEC");           break ;
    case SONY:         Serial.print("SONY");          break ;
    case RC5:          Serial.print("RC5");           break ;
    case RC6:          Serial.print("RC6");           break ;
    case DISH:         Serial.print("DISH");          break ;
    case SHARP:        Serial.print("SHARP");         break ;
    case JVC:          Serial.print("JVC");           break ;
    case SANYO:        Serial.print("SANYO");         break ;
    case MITSUBISHI:   Serial.print("MITSUBISHI");    break ;
    case SAMSUNG:      Serial.print("SAMSUNG");       break ;
    case LG:           Serial.print("LG");            break ;
    case WHYNTER:      Serial.print("WHYNTER");       break ;
    case AIWA_RC_T501: Serial.print("AIWA_RC_T501");  break ;
    case PANASONIC:    Serial.print("PANASONIC");     break ;
    case DENON:        Serial.print("Denon");         break ;
  }
}

//+=============================================================================
// Dump out the decode_results structure.
//
void  dumpInfo (decode_results *results)
{
  // Check if the buffer overflowed
  if (results->overflow) {
    Serial.println("IR code too long. Edit IRremoteInt.h and increase RAWLEN");
    return;
  }

  // Show Encoding standard
  Serial.print("Encoding  : ");
  encoding(results);
  Serial.println("");

  // Show Code & length
  Serial.print("Code      : ");
  ircode(results);
  Serial.print(" (");
  Serial.print(results->bits, DEC);
  Serial.println(" bits)");

  Serial.print("unsigned int  data = 0x");
  Serial.println(results->value, HEX);

}

void dumpBin(decode_results *results)
{
  int bitCnt = 0;
  boolean firstB = true;
  for (int i = 4; i < results->rawlen; i = i + 2) {
    unsigned long  x = results->rawbuf[i] * USECPERTICK;
    if (x > 8000) {
      Serial.print("H1-");
    }
    else if (x > 4000) {
      Serial.print("H2-");
    }
    else if (x > 1000) {
      if (bitCnt == 0) {
        if (firstB) {
          Serial.print("{B");
          firstB = false;
        } else {
          Serial.print(",B");
        }
      }
      Serial.print("1");
    }
    else if (x > 300) {
      if (bitCnt == 0) {
        if (firstB) {
          Serial.print("{B");
          firstB = false;
        } else {
          Serial.print(",B");
        }
      }
      Serial.print("0");
    }
    bitCnt++;
    if (bitCnt == 8) {
      bitCnt = 0;
    }
  }
  Serial.print("}");
}
//+=============================================================================
// Dump out the decode_results structure.
//
void  dumpRaw (decode_results * results)
{
  // Print Raw data
  Serial.print("Timing[");
  Serial.print(results->rawlen - 1, DEC);
  Serial.println("]: ");

  for (int i = 1;  i < results->rawlen;  i++) {
    unsigned long  x = results->rawbuf[i] * USECPERTICK;
    if (!(i & 1)) {  // even
      Serial.print("-");
      if (x < 1000)  Serial.print(" ") ;
      if (x < 100)   Serial.print(" ") ;
      Serial.print(x, DEC);
    } else {  // odd
      Serial.print("     ");
      Serial.print("+");
      if (x < 1000)  Serial.print(" ") ;
      if (x < 100)   Serial.print(" ") ;
      Serial.print(x, DEC);
      if (i < results->rawlen - 1) Serial.print(", "); //',' not needed for last one
    }
    if (!(i % 8))  Serial.println("");
  }
  Serial.println("");                    // Newline
}

//+=============================================================================
// Dump out the decode_results structure.
//
void  dumpCode (decode_results * results)
{
  // Start declaration
  Serial.print("unsigned int  ");          // variable type
  Serial.print("rawData[");                // array name
  Serial.print(results->rawlen - 1, DEC);  // array size
  Serial.print("] = {");                   // Start declaration

  // Dump data
  for (int i = 1;  i < results->rawlen;  i++) {
    Serial.print(results->rawbuf[i] * USECPERTICK, DEC);
    if ( i < results->rawlen - 1 ) Serial.print(","); // ',' not needed on last one
    if (!(i & 1))  Serial.print(" ");
  }

  // End declaration
  Serial.print("};");  //

  // Comment
  Serial.print("  // ");
  encoding(results);
  Serial.print(" ");
  ircode(results);

  // Newline
  Serial.println("");

  // Now dump "known" codes
  if (results->decode_type != UNKNOWN) {

    // Some protocols have an address
    if (results->decode_type == PANASONIC) {
      Serial.print("unsigned int  addr = 0x");
      Serial.print(results->address, HEX);
      Serial.println(";");
    }

    // All protocols have data
    Serial.print("unsigned int  data = 0x");
    Serial.print(results->value, HEX);
    Serial.println(";");
  }
}

//+=============================================================================
// The repeating section of the code
//
void  loop ( )
{
  decode_results  results;        // Somewhere to store the results

  if (irrecv.decode(&results)) {  // Grab an IR code
    if (receiveLoop == 0) {
      if (cmdTestCnt == 0) {
        Serial.println("====================");
        Serial.println(cmdList[cmdIndex]);
      }
    }
    dumpInfo(&results);           // Output the results
    //dumpRaw(&results);            // Output the results in RAW format
    dumpCode(&results);           // Output the results as source code
    Serial.println("");
    dumpBin(&results);            // Output the results in binary
    Serial.println("");
    receiveLoop++; // next receive of same command
    if (receiveLoop == endReceiveLoop) { // received the number of sendings of a single command
      receiveLoop = 0;
      cmdTestCnt++;
      if (cmdTestCnt == 3) { // single command finished
        Serial.println("====================");
        Serial.println("Command finished, now press " + cmdList[cmdIndex + 1]);
        cmdTestCnt = 0;
        cmdIndex++;
        if (cmdIndex == endCmdIndex) { // all commands done
          Serial.println("####################");
          Serial.println ("Done");
          cmdIndex = 0;
        }
      }
    }
    Serial.println("");           // Blank line between entries
    irrecv.resume();              // Prepare for the next value
  }
}
