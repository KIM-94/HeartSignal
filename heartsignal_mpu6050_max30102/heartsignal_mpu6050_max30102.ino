#include <Bridge.h>
#include <BridgeClient.h>
#include <BridgeServer.h>
#include <BridgeSSLClient.h>
#include <BridgeUdp.h>
#include <Console.h>
#include <FileIO.h>
#include <HttpClient.h>
#include <Mailbox.h>
#include <Process.h>
#include <YunClient.h>
#include <YunServer.h>

/* Get all possible data from MPU6050
 * Accelerometer values are given as multiple of the gravity [1g = 9.81 m/s²]
 * Gyro values are given in deg/s
 * Angles are given in degrees
 * Note that X and Y are tilt angles and not pitch/roll.
 *
 * License: MIT
 */

// mpu6050
#include "Wire.h"
#include <MPU6050_light.h>

// max30102
#include <Wire.h>
#include "MAX30105.h"
#include "heartRate.h"

// bluetooth
#include <SoftwareSerial.h>
SoftwareSerial mySerial(2, 3); //블루투스의 Tx, Rx핀을 2번 3번핀으로

// mpu6050
MPU6050 mpu(Wire);
long timer = 0;

// max30102
MAX30105 particleSensor;
const byte RATE_SIZE = 4; //Increase this for more averaging. 4 is good.
byte rates[RATE_SIZE]; //Array of heart rates
byte rateSpot = 0;
long lastBeat = 0; //Time at which the last beat occurred
float beatsPerMinute;
int beatAvg;
long timer2 = 0;

// bluetooth 통신
float Xa, Ya, Za, SUMa;
float checksum=0;
float checkcount=0;
int checkstep;

float intensity_Xa, intensity_Ya, intensity_Za, intensity_SUMa;
float intensity_checksum=0;
float intensity_checkcount=0;
int intensity_checkstep;

// test
String incomingByte = "";
String incomingByte_temp = "";
String setByte = "";

String myString=""; //받는 문자열

// Steps
int STEPS = 0;
int INTENSITY = 0;
String FSTATE = "";
int SHOCK = 0;



void setup() {
  Serial.begin(115200);
  mySerial.begin(115200); // bluetooth
  Wire.begin(); // mpu6050

  // max30102
  Serial.println("Initializing...");

  // Initialize sensor
  if (!particleSensor.begin(Wire, I2C_SPEED_FAST)) //Use default I2C port, 400kHz speed
  {
    Serial.println("MAX30105 was not found. Please check wiring/power. ");
    while (1);
  }
  Serial.println("Place your index finger on the sensor with steady pressure.");
  particleSensor.setup(); //Configure sensor with default settings
  particleSensor.setPulseAmplitudeRed(0x0A); //Turn Red LED to low to indicate sensor is running
  particleSensor.setPulseAmplitudeGreen(0); //Turn off Green LED

  // mpu6050
  byte status = mpu.begin();
  Serial.print(F("MPU6050 status: "));
  Serial.println(status);
  while(status!=0){ } // stop everything if could not connect to MPU6050

  Serial.println(F("Calculating offsets, do not move MPU6050"));
  delay(1000);
  mpu.calcOffsets(true,true); // gyro and accelero
  Serial.println("Done!\n");

}

void loop() {

  while(mySerial.available()){
    incomingByte_temp = (char)mySerial.read();
    incomingByte += incomingByte_temp;
    Serial.print(incomingByte);
    delay(5); //수신 문자열 끊김 방지
  }

  if(!incomingByte.equals("")){
    if(incomingByte.equals("mpu")){
      myString = "mpu";
    }
    else if(incomingByte.equals("max")){
      myString = "max";
    }
    else if(incomingByte.equals("all")){
      myString = "all";
    }
    else if(incomingByte.equals("getsteps")){
      myString = "get_steps";
    }
    incomingByte = "";
  }

  // 자이로센서(mpu6050) 모니터링
  if (myString == "mpu"){
      fun_mpu6050();
  }

  // 심박센서(max30102) 모니터링
  else if (myString == "max"){
      fun_max30102();
  }

  // 걸음, 움직임, 밴드착용유무 체크
  else if (myString == "get_steps"){
    Serial.println(STEPS);
    mySerial.print(String("steps")+String(' ')+String(STEPS)+String(' ')+String(FSTATE)+String(' ')+String(INTENSITY)+String(' ')+String(SHOCK)+String('\n'));
    myString = "";
    STEPS = 0;
    INTENSITY = 0;
    SHOCK = 0;
  }

  else {
    fun_mpu6050_steps();
    fun_mpu6050_intensity();
    incomingByte="";
  }
}


void fun_mpu6050_steps(){
  long irValue = particleSensor.getIR();

  mpu.update();

  if(millis() - timer > 100){ // print data every second

    Xa = (mpu.getGyroX());
    Ya = (mpu.getGyroY());
    Za = (mpu.getGyroZ());
    SUMa = Xa + Ya + Za;

    if(Xa > 80 || Ya > 80 || Za>80){
        SHOCK++;
    }

    // step check
    if (SUMa > 30.0){
      checkcount = checkcount + 1;
      checksum = checksum + SUMa;
      checkstep = 0;
    }

    else{
      if(checksum/checkcount > 40 && checkcount>2){
        checkstep = 1;
        STEPS = STEPS + checkstep;
        checkcount = 0;
        checksum = 0;
      }
      else{
        checkstep = 0;
        checkcount = 0;
        checksum = 0;
      }
    }

    // 밴드 착용 체크
    if (irValue < 50000){
      FSTATE = "nofinger";
    } else{
      FSTATE = "finger";
    }

    Serial.println("stepchek");
    timer = millis();
  }
}

// 움직임 체크
void fun_mpu6050_intensity(){
  long irValue = particleSensor.getIR();

  mpu.update();

  // print data every second
  if(millis() - timer > 100){
    intensity_Xa = (mpu.getGyroX());
    intensity_Ya = (mpu.getGyroY());
    intensity_Za = (mpu.getGyroZ());
    intensity_SUMa = Xa + Ya + Za;

    // intensity
    if (intensity_Xa > 10.0 || intensity_Ya > 10.0 || intensity_Za > 10.0){
      INTENSITY = INTENSITY + 1;
    }

    else{
    }

    Serial.println("intensity chek");
    timer = millis();
  }
}

void fun_mpu6050(){
  mpu.update();

  // print data every second
  if(millis() - timer > 200){

    Xa = (mpu.getGyroX());
    Ya = (mpu.getGyroY());
    Za = (mpu.getGyroZ());
    SUMa = Xa + Ya + Za;

    // step
    if (SUMa > 30.0){
      checkcount = checkcount + 1;
      checksum = checksum + SUMa;
      checkstep = 0;
    }

    else{
      if(checksum/checkcount > 40 && checkcount>2){
        checkstep = 1;
        STEPS = STEPS + checkstep;
        checkcount = 0;
        checksum = 0;
      }
      else{
        checkstep = 0;
        checkcount = 0;
        checksum = 0;
      }
    }

    Serial.println(String(Xa)+String(',')+String(Ya)+String(',')+String(Za)+String(',')+String(SUMa)+String(',')+String(checkstep));

    mySerial.print(String("gyro")+String(' ')+String(Xa)+String(' ')+String(Ya)+String(' ')+String(Za)+String('\n'));

    timer = millis();
  }
}

void fun_max30102(){
  long irValue = particleSensor.getIR();

  if (checkForBeat(irValue) == true)
  {
    //We sensed a beat!
    long delta = millis() - lastBeat;
    lastBeat = millis();

    beatsPerMinute = 60 / (delta / 1000.0);

    if (beatsPerMinute < 255 && beatsPerMinute > 20)
    {
      rates[rateSpot++] = (byte)beatsPerMinute; //Store this reading in the array
      rateSpot %= RATE_SIZE; //Wrap variable

      //Take average of readings
      beatAvg = 0;
      for (byte x = 0 ; x < RATE_SIZE ; x++)
        beatAvg += rates[x];
      beatAvg /= RATE_SIZE;
    }
  }

  if(millis() - timer2 > 100){ // print data every second
    String state;
    state = "finger";

    if (irValue < 50000)
      state = "nofinger";

    mySerial.print(String("heartrate")+String(' ')+String(irValue)+String(' ')+String(beatsPerMinute)+String(' ')+String(state)+String('\n'));

    timer2 = millis();
  }
}
