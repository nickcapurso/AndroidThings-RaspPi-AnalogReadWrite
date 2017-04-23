package com.capurso.androidthings_analogrw;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

import com.capurso.androidthings_analogrw.driver.adc.Adc;
import com.capurso.androidthings_analogrw.driver.adc.AdcChannel;
import com.capurso.androidthings_analogrw.driver.adc.Mcp3002;
import com.capurso.androidthings_analogrw.driver.dac.Dac;
import com.capurso.androidthings_analogrw.driver.dac.Mcp4725Impl;
import com.capurso.androidthings_analogrw.driver.pwmled.PwmLed;
import com.google.android.things.contrib.driver.button.Button;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

import timber.log.Timber;

/**
 * Reads from two potentiometers and outputs the corresponding voltage to two LEDs.
 * Reading in analog values from the potentiometers requires an external ADC for the Raspberry Pi.
 * Outputting to one LED is done via PWM while the other is done via an external DAC.
 * A push button is used to start and stop reading and outputting.
 */
public class MainActivity extends Activity {
    private static final String ENABLE_BUTTON_PIN_NAME = "BCM6";

    private static final String LED_PWM_PIN_NAME = "PWM0";

    private static final String ADC_SPI_DEVICE_NAME = "SPI0.0";

    private static final String DAC_I2C_DEVICE_NAME = "I2C1";

    private static final long POT_SAMPLE_DELAY = 250;

    private static final long BUTTON_DEBOUNCE_DELAY = 50;

    private Button enableBtn;

    private PwmLed pwmLed;

    private Adc<AdcChannel> adc;

    private Dac dac;

    private Handler handler;

    private boolean enabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        printDevices();
        handler = new Handler();

        try {
            enableBtn = new Button(ENABLE_BUTTON_PIN_NAME, Button.LogicState.PRESSED_WHEN_HIGH);
            enableBtn.setDebounceDelay(BUTTON_DEBOUNCE_DELAY);
            enableBtn.setOnButtonEventListener(new EnableListener());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts / stops reading from potentiometers (and outputting to LEDs) when the
     * button is pressed.
     */
    private class EnableListener implements Button.OnButtonEventListener {
        @Override
        public void onButtonEvent(Button button, boolean pressed) {
            if (pressed) {
                if (!enabled) {
                    Timber.d("Initializing I/O devices");

                    openDevices();
                    startReading();
                } else {
                    Timber.d("Closing I/O devices");
                    closeDevices();
                }
                enabled = !enabled;
            }
        }
    }

    /**
     * Read from the potentiometers over and over and output the values to the
     * corresponding LEDs.
     */
    private class ReadPotsAndOutput implements Runnable {
        @Override
        public void run() {
            try {
                if (enabled) {
                    readAndSetLedWithPwm();
                    readAndSetLedWithAnalog();
                    handler.postDelayed(new ReadPotsAndOutput(), POT_SAMPLE_DELAY);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void readAndSetLedWithPwm() throws Exception {
            int reading = adc.analogRead(AdcChannel.CHANNEL_1);

            // Convert reading to a percentage for the duty cycle
            double duty = ((double) reading / adc.getMaxValue()) * 100;
            duty = correctValueIfNeeded(duty, 100, 0, 1);

            Timber.d("Channel 1: read %s, pwm duty %s", reading, duty);
            pwmLed.setDuty(duty);
        }

        private void readAndSetLedWithAnalog() throws Exception {
            int reading = adc.analogRead(AdcChannel.CHANNEL_2);
            reading = (int) correctValueIfNeeded(reading, adc.getMaxValue(), adc.getMinValue(), 1);

            // Convert from ADC's range to the DAC's range
            reading = (int) (((double) reading / adc.getMaxValue()) * dac.getMaxValue());

            Timber.d("Channel 2: read %s, sending to DAC", reading);
            dac.analogWrite(reading);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeDevices();
        closeDevice(enableBtn);
    }

    private void startReading() {
        handler.post(new ReadPotsAndOutput());
    }

    private void printDevices() {
        PeripheralManagerService peripheralManagerService = new PeripheralManagerService();
        Timber.i(peripheralManagerService.getGpioList().toString());
        Timber.i(peripheralManagerService.getSpiBusList().toString());
        Timber.i(peripheralManagerService.getPwmList().toString());
        Timber.i(peripheralManagerService.getI2cBusList().toString());
    }

    private void openDevices() {
        try {
            pwmLed = new PwmLed(LED_PWM_PIN_NAME);
            pwmLed.setDuty(0);

            adc = new Mcp3002(ADC_SPI_DEVICE_NAME);
            dac = new Mcp4725Impl(DAC_I2C_DEVICE_NAME);

            // Uncomment (and comment the line above) to try the Arduino-proxy DAC
//            dac = new ArduinoProxyDac(DAC_I2C_DEVICE_NAME);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeDevices() {
        closeDevice(pwmLed);
        closeDevice(adc);
        closeDevice(dac);
    }

    private void closeDevice(AutoCloseable autoCloseable) {
        if (autoCloseable != null) {
            try {
                autoCloseable.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Simple threshold-based checks to make sure the value does not exceed a max or min.
     *
     * @return value if it is not within threshold from max or min. Else, returns max or min.
     */
    private double correctValueIfNeeded(double value, double max, double min, double threshold) {
        if (value > max - threshold) {
            return max;
        } else if (value < min + threshold) {
            return min;
        }
        return value;
    }
}

