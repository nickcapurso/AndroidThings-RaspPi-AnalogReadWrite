# AndroidThings-RaspPi-AnalogReadWrite
Example Android Things project which reads from two potentiometers and echos the same voltage out to two respective LEDs. 
Since the Raspberry Pi does not have an on-board ADC or an on-board DAC, we have to use external chips to get these functionalities.
I used the MCP 3002 ADC and the MCP 4025 DAC, which require SPI and I2C communication, respectively. The Pi reads from the
ADC and lights one LED using the built-in PWM functions and lights the other using the DAC.

![Fritzing Diagram](https://github.com/nickcapurso/AndroidThings-RaspPi-AnalogReadWrite/blob/master/CompletedCircuit_bb.png)
