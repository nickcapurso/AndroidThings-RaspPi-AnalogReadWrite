package com.capurso.androidthings_analogrw;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.capurso.androidthings_analogrw.driver.adc.Adc;
import com.capurso.androidthings_analogrw.driver.adc.AdcChannel;
import com.capurso.androidthings_analogrw.driver.adc.Mcp3002;
import com.capurso.androidthings_analogrw.driver.dac.Dac;
import com.capurso.androidthings_analogrw.driver.dac.Mcp4725;
import com.capurso.androidthings_analogrw.driver.pwmled.PwmLed;
import com.google.android.things.contrib.driver.button.Button;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getName();

    private static final String ENABLE_BUTTON_PIN_NAME = "BCM6";

    private static final String LED_PWM_PIN_NAME = "PWM0";

    private static final String ADC_SPI_DEVICE_NAME = "SPI0.0";

    private static final String DAC_I2C_DEVICE_NAME = "I2C1";

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
            enableBtn.setDebounceDelay(50);
            enableBtn.setOnButtonEventListener(new EnableListener());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class EnableListener implements Button.OnButtonEventListener {
        @Override
        public void onButtonEvent(Button button, boolean pressed) {
            if (pressed) {
                if (!enabled) {
                    Log.d(TAG, "Initializing I/O devices");
                    openDevices();
                    startReading();
                } else {
                    Log.d(TAG, "Closing I/O devices");
                    closeDevices();
                }
                enabled = !enabled;
            }
        }
    }

    private class ReadPotsAndOutput implements Runnable {
        @Override
        public void run() {
            try {
                if (enabled) {
                    readAndSetLedWithPwm();
                    readAndSetLedWithAnalog();
                    handler.postDelayed(new ReadPotsAndOutput(), 1000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void readAndSetLedWithPwm() throws Exception {
            int reading = adc.analogRead(AdcChannel.CHANNEL_1);
            double duty = ((double) reading / Mcp3002.MAX_VALUE) * 100;
            duty = duty < 1 ? 0 : duty;
            duty = duty > 99 ? 100 : duty;
            Log.i(TAG, "Channel 1 Read: " + reading + ", pwm duty: " + duty);
            pwmLed.setDuty(duty);
        }

        private void readAndSetLedWithAnalog() throws Exception {
            int reading = adc.analogRead(AdcChannel.CHANNEL_2);
            reading = reading < 1 ? 0 : reading;
            reading = reading > 4095 ? 4095 : reading;
            Log.i(TAG, "Channel 2 Read: " + reading + ", writing to DAC");
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
        Log.i(TAG, peripheralManagerService.getGpioList().toString());
        Log.i(TAG, peripheralManagerService.getSpiBusList().toString());
        Log.i(TAG, peripheralManagerService.getPwmList().toString());
        Log.i(TAG, peripheralManagerService.getI2cBusList().toString());
    }

    private void openDevices() {
        try {
            pwmLed = new PwmLed(LED_PWM_PIN_NAME);
            pwmLed.setDuty(0);

            adc = new Mcp3002(ADC_SPI_DEVICE_NAME);
            dac = new Mcp4725(DAC_I2C_DEVICE_NAME);
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
}

