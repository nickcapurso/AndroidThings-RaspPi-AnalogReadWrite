/**
 * Based on PwmSpeaker at:
 * https://github.com/androidthings/contrib-drivers
 **/
package com.capurso.androidthings_analogrw.driver.pwmled;


import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.Pwm;

import java.io.IOException;

/**
 * Used to interface with an LED which is lit via PWM.
 */
public class PwmLed implements AutoCloseable {
    // Adequate frequency for PWM for LEDs
    private static final int DEFAULT_FREQUENCY = 120;

    private Pwm led;

    public PwmLed(String pwmName) throws IOException {
        try {
            connect(pwmName);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void connect(String pwmName) throws IOException {
        PeripheralManagerService peripheralManagerService = new PeripheralManagerService();
        led = peripheralManagerService.openPwm(pwmName);
        led.setPwmFrequencyHz(DEFAULT_FREQUENCY);
    }

    @Override
    public void close() throws IOException {
        if (led != null) {
            try {
                led.close();
            } finally {
                led = null;
            }
        }
    }

    public void setDuty(double duty) throws IOException, IllegalStateException {
        if (led == null) {
            throw new IllegalStateException("PWM device not opened");
        }
        led.setPwmDutyCycle(duty);
        led.setEnabled(true);
    }
}
