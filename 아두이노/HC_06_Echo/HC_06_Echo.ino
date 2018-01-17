#include <SoftwareSerial.h>
#include <stdio.h>
#define rxPin 3 
#define txPin 2 
#define BUFF_SIZE 1024

SoftwareSerial BTSerial(txPin, rxPin);

#include <DHT11.h> 
int pin=4;    // DHT11 temperature sensor
DHT11 dht11(pin); 


String temp2;
String humi2;
String HumanDetectVal;
String DoorOCVal;

int HumanDetectPin = 5; // 센서 시그널핀
int pirState = LOW; // PIR 초기상태
int Humanval = 0; // Signal 입력값

int MagDoorPin = 6;
int Doorval = 0;

int PanPin = 7;

// 데이터 버퍼
byte buffer[BUFF_SIZE];
byte index;
byte data;

char SendTemp[50];
char SendHumi[50];

char TempPlusHumi[50];

int PanHumi = 20;
int PanPower = 125;

char ReciveTemp[10];
char RecivePanPower[10];

void setup() {
  pinMode(HumanDetectPin, INPUT); // 센서 Input 설정
  pinMode(MagDoorPin,INPUT);
  pinMode (PanPin, OUTPUT);
  BTSerial.begin(9600);
  Serial.begin(9600);
  index = 0;
  
}

void loop() {

  
  
  if(BTSerial.available())  //사용자가 명령어를 보낸다.
  {
    while(BTSerial.available()) {
      data = BTSerial.read();
      buffer[index++] = data;
      if(index == BUFF_SIZE || data == '\0') break;
      delay(1);
    }
  
  String myString = String((char *)buffer);
  myString.substring(0,2).toCharArray(ReciveTemp,3);
  myString.substring(3).toCharArray(RecivePanPower,3);

  PanHumi = atoi(ReciveTemp);
  PanPower = atoi(RecivePanPower);
  Serial.print(PanHumi);
  Serial.println();
  Serial.print(PanPower);
  Serial.println();
      
  index = 0;
  }
  
  Humanval = digitalRead(HumanDetectPin); //인체 감지 파트
  
  if (Humanval == HIGH){
     HumanDetectVal = "1";
    }
    else
    {
      HumanDetectVal = "0";
      }

  Doorval = digitalRead(MagDoorPin);  //문 제어 파트
  if(Doorval == HIGH)
  {
    DoorOCVal = "1";
    }
    else
    {
      DoorOCVal = "0";
      }
      
  int err;                          //실시간 온습도 전송 파트
  float temp, humi;
  if((err=dht11.read(humi, temp))==0)
  {
    dtostrf(temp, 4, 2,SendTemp);
    dtostrf(humi, 4, 2,SendHumi);

    temp2 = SendTemp;
    humi2 = SendHumi;

    String result = temp2+","+humi2+","+HumanDetectVal+","+DoorOCVal+","+PanPower;
    Serial.print(result);
    Serial.println();

    for(int i = 0; i < result.length(); i ++)
    {
      TempPlusHumi[i] = result.charAt(i);
      }
    
    TempPlusHumi[result.length()] = '\0';
    
    for(uint8_t j = 0; j <= result.length(); ++j) {   //온,습도 센서 값 + 인체감지 센서값 + 문 감지 센서 값을 TempPlusHumi에 넣고 전송함.
      BTSerial.print(TempPlusHumi[j]);
    }
    
    delay(500);
  }
  else
  {
  }
  
  if(temp > PanHumi)
  {
    digitalWrite (PanPin, HIGH);
    }
    else
    {
      digitalWrite (PanPin, LOW);
      }
}
