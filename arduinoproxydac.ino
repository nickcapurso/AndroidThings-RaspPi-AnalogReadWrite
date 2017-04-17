#include <Wire.h>

#define I2C_ADDR 0x77
#define MCP4725_ADDR 0x60
#define MCP4725_WRITE_MODE 0x40
#define POWERDOWN_DEFAULT_MODE 0x00

// 12-bit value == top8 bits + bottom 4 bits (pre shifted) (+ 4 don't cares)
byte top8;
byte bottom4;

void setup()
{  
  top8 = 0;
  bottom4 = 0;
  
  Wire.begin(I2C_ADDR);
  Wire.onReceive(onI2CReceived);
}

void onI2CReceived(int numBytes) {
  if (Wire.available() >= 2) {
     top8 = Wire.read();
     bottom4 = Wire.read();
     while(Wire.available()) {
      Wire.read();
     }
  }
}

void loop(){
     delay(250);
     Wire.beginTransmission(MCP4725_ADDR);
     Wire.write(MCP4725_WRITE_MODE | POWERDOWN_DEFAULT_MODE);                     
     Wire.write(top8);
     Wire.write(bottom4); 
     Wire.endTransmission();
}
