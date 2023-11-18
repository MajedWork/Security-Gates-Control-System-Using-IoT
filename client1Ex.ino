#include <string.h>

//Wi-Fi
#include <WiFi.h>
const char* ssid = "WiFi Username";
const char* password = "WiFi password";
WiFiClient espClient;

//MQTT
#include <PubSubClient.h>
const char* mqtt_server = "mqtt IP";
PubSubClient client(espClient);

//RFID
#include <SPI.h> //protocol
#include <MFRC522.h>
#define SS_PIN 5
#define RST_PIN 0
MFRC522::MIFARE_Key key;
MFRC522 rfid = MFRC522(SS_PIN, RST_PIN);
String tag = "";

//Fingerprint
#include <Adafruit_Fingerprint.h>
#define mySerial Serial2
Adafruit_Fingerprint finger = Adafruit_Fingerprint(&mySerial);
int idd = -1;
int Finger_Trials = 0;

//Motor
#include <Stepper.h>
#define IN1 13
#define IN2 12
#define IN3 14
#define IN4 27
const int stepsPerRevolution = 510;
Stepper myStepper = Stepper(stepsPerRevolution, IN1, IN3, IN2, IN4);


//LCD
#include <LiquidCrystal.h>
#define rs 3
#define en 21
#define d4 32
#define d5 33
#define d6 25
#define d7 26
LiquidCrystal lcd(rs, en, d4, d5, d6, d7);

//Logic
bool stop = false;
bool alert = false;
bool restart = false;
bool gate2 = false;

void setup() {
  Serial.begin(115200);

  //Wi-Fi setup
  setup_wifi();

  //MQTT setup
  client.setServer(mqtt_server, 1883);
  client.setCallback(callback);

  //RFID setup
  SPI.begin();
  rfid.PCD_Init();

  //Fingerprint setup
  finger.begin(57600);
  delay(5);
  finger.verifyPassword();

  //Steppermotor setup
  myStepper.setSpeed(10);

  //LCD setup
  lcd.begin(16, 2);
  lcd.print("Starting ...");
  delay(2000);
  lcd.clear();
}

// WiFi Connection
void setup_wifi() {
  delay(10);
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("");
  Serial.println("WiFi connected");
}

// Show message and take action with it
void callback(char* topic, byte* message, unsigned int length) {
  String messageTemp;
  // Collect the message as a whole string
  for (int i = 0; i < length; i++) {
    messageTemp += (char)message[i];
  }

  if (String(topic) == "/system/app/process/security/response") {
    //Restart the system
    restart = true;
  }

  if (String(topic) == "/system/app/process/authenticate/response") {
    if (messageTemp == "ok") {  //Authenticated
      lcd.clear();
      lcd.setCursor(0, 0);
      lcd.print("Proceed");
      myStepper.step(stepsPerRevolution);
      client.loop();
      delay(7000);
      client.loop();
      myStepper.step(-stepsPerRevolution);
      client.loop();
      client.publish("/system/gate1/process/status", "ok");
      while (!gate2) {
        delay(2000);
        client.loop();
      }
      gate2 = false;
    } else {  //Not Authenticated
      restart = true;
    }
    stop = true;
  }

  if (String(topic) == "/system/gate2/process/sensor/status") {
    if (messageTemp == "no") {  //No car between the two gates
      Serial.println("No Detection");
      gate2 = true;
    }
  }
}

// Read RFID Tag
String readRFID(void) {
  String tt = "";

  //Read RFID card
  for (byte i = 0; i < 6; i++) {
    key.keyByte[i] = 0xFF;
  }

  // Verify if the NUID has been readed
  if (!rfid.PICC_ReadCardSerial())
    return "";

  // Collect the tag as a whole string
  for (byte i = 0; i < rfid.uid.size; i++) {
    tt = tt + rfid.uid.uidByte[i];
  }

  Serial.println();
  // Halt PICC
  rfid.PICC_HaltA();
  // Stop encryption on PCD
  rfid.PCD_StopCrypto1();

  return tt;
}

// Read Fingerprint
int getFingerprintIDez() {

  uint8_t p = finger.image2Tz();
  if (p != FINGERPRINT_OK) return -1;

  p = finger.fingerFastSearch();
  if (p != FINGERPRINT_OK) return -1;

  // found a match!
  return finger.fingerID;
}

// MQTT connection
void reconnect() {
  // Loop until we're reconnected
  while (!client.connected()) {
    Serial.print("Attempting MQTT connection...");
    String client_id = "Client1-";
    if (client.connect(client_id.c_str(), "mqtt Username", "mqtt Password")) {
      Serial.println("connected");
      client.subscribe("/system/app/process/security/response");
      client.subscribe("/system/app/process/authenticate/response");
      client.subscribe("/system/gate2/process/sensor/status");
    } else {
      // Wait 5 seconds before retrying
      delay(5000);
    }
  }
}

// Main loop
void loop() {
  if (!client.connected()) {
    reconnect();
  }
  client.loop();

  while (!stop) {
    // Reading the card
    Serial.println("Waiting for Card...");
    lcd.clear();
    lcd.print("Card ...");
    while (!rfid.PICC_IsNewCardPresent()) {  //Wait until card is present
      delay(2000);
      client.loop();
    }
    tag = readRFID();

    // Reading the fingerprint
    while (idd == -1) {
      lcd.clear();
      lcd.print("Fingerprint ...");
      Serial.println("Waiting for Fingerprint...");
      while (finger.getImage() == FINGERPRINT_NOFINGER) {  //wait until fingerprint is present
        delay(2000);
        client.loop();
      }
      idd = getFingerprintIDez();
      Finger_Trials++;
      if (idd == -1) {
        lcd.setCursor(0, 1);
        lcd.print("Try Again!");
        delay(500);
      }
      if (Finger_Trials == 3 && idd == -1) {
        client.loop();
        client.publish("/system/gate1/process/security", "Alert");
        Finger_Trials = 0;
        idd = -1;
        alert = true;      
        lcd.clear();
        lcd.setCursor(0, 0);
        lcd.print("Alert!");
        while (!restart) {  // Wait for security to unlock the system
          delay(2000);
          client.loop();
        }
        restart = false;
        break;
      }
    }
    if (alert) {  //restart the system in case of an alert
      alert = false;
      continue;
    } else {  // Pass the scanning process
      stop = true;
    }
  }

  lcd.clear();
  // Format the ID and Tag
  String both = String(idd) + " , " + tag;
  char worchar[both.length() + 1];
  both.toCharArray(worchar, both.length() + 1);

  client.loop();
  // Publish the info so client 3 can process it
  client.publish("/system/gate1/process/authenticate", worchar);
  // Reset the variables
  idd = -1;
  Finger_Trials = 0;
  stop = false;

  while (!stop) {  // Wait for the authentication result
    delay(2000);
    client.loop();
  }
  stop = false;

  if (restart) {  // in case of false authentiaction
    restart = false;
    client.publish("/system/gate1/process/security", "Alert");
    lcd.clear();
    lcd.setCursor(0, 0);
    lcd.print("Alert!");
    while (!restart) {  // Wait for security to unlock the system
      delay(2000);
      client.loop();
    }
    restart = false;
  }
}