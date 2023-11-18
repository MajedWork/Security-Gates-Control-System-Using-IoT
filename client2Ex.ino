//Wi-Fi
#include <WiFi.h>
const char* ssid = "WiFi Username";
const char* password = "WiFi Password";
WiFiClient espClient2;

//MQTT
#include <PubSubClient.h>
const char* mqtt_server = "mqtt IP";
PubSubClient client2(espClient2);

//Ultrasonic
#define Trigger 23
#define Echo 22
long duration;
int distance;

//Motor
#include <Stepper.h>
#define IN1 13
#define IN2 12
#define IN3 14
#define IN4 27
const int stepsPerRevolution = 510;
Stepper myStepper = Stepper(stepsPerRevolution, IN1, IN3, IN2, IN4);

//Logic
bool inBetween = false;

void setup() {
  Serial.begin(115200);

  //WiFi setup
  wifi_Connection();

  //MQTT setup
  mqtt_Connection();

  //Ultrasonic setup
  ultrasonic_setup();

  //Steppermotor setup
  myStepper.setSpeed(10);
}

// Wifi connection
void wifi_Connection() {

  WiFi.begin(ssid, password);  // connect to the wifi
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
  }

  Serial.println("Connected !");
}

// MQTT connection
void mqtt_Connection() {
  client2.setServer(mqtt_server, 1883);
  client2.setCallback(callback);
}

// to show message and take action with it
void callback(char* topic, byte* message, unsigned int length) {
  String messageSTR;
  // Collect the message as a whole string
  for (int i = 0; i < length; i++) {
    messageSTR += (char)message[i];
  }

  if (String(topic) == "/system/gate1/process/status") {
    if (messageSTR == "ok")  // in case client 1 did it's process and car passed gate 1
    {
      ultrasonic_check_sensor();
    }
  }
}

void reconnect() {
  while (!client2.connected()) {
    String client_id = "Client2";
    if (client2.connect(client_id.c_str(), "mqtt Username", "mqtt Password")) {
      Serial.println("client 2 connected");
    } else {
      // Retry to connect after 5 min
      delay(5000);
    }
  }
  client2.subscribe("/system/gate1/process/status");
}

void ultrasonic_setup() {
  pinMode(Trigger, OUTPUT);
  pinMode(Echo, INPUT);
}

void ultrasonic_check_sensor() {
  // Send triggering waves
  digitalWrite(Trigger, LOW);
  delay(2);
  digitalWrite(Trigger, HIGH);
  delay(10);
  digitalWrite(Trigger, LOW);
  // Receive the echo wave
  duration = pulseIn(Echo, HIGH);
  // Calculate the distance
  distance = duration * 0.034 / 2;
  // Make sure to open the gate according to these distances
  while (distance > 0 && distance <= 24) {
    inBetween = true;
    myStepper.step(-stepsPerRevolution);
    client2.loop();
    delay(7000);
    client2.loop();
    myStepper.step(stepsPerRevolution);
    client2.loop();
    delay(2);
    digitalWrite(Trigger, HIGH);
    delay(10);
    digitalWrite(Trigger, LOW);
    duration = pulseIn(Echo, HIGH);
    distance = duration * 0.034 / 2;
    delay(3000);
  }
  client2.publish("/system/gate2/process/sensor/status", "no");
  if (inBetween) {
    client2.publish("/system/gate2/process/sensor/status", "yes");
  }
  inBetween = false;
}

// Main loop
void loop() {
  if (!client2.connected()) {
    reconnect();
  }
  delay(2000);
  client2.loop();
}