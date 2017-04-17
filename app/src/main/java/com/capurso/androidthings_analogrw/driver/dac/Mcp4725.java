package com.capurso.androidthings_analogrw.driver.dac;


import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

/**
 * Common functionality between different implementations of interfacing with the MCP4725.
 * See {@link Mcp4725Impl} and {@link ArduinoProxyDac}
 */
abstract class Mcp4725 implements Dac {
    private static final int MAX_VALUE = 4095;

    private static final int MIN_VALUE = 0;

    I2cDevice dac;

    Mcp4725(String i2cName) throws IOException, IllegalStateException {
        connect(i2cName, getDefaultI2cAddress());
    }

    Mcp4725(String i2cName, int i2cAddr) throws IOException, IllegalStateException {
        connect(i2cName, i2cAddr);
    }

    public void connect(String i2cName, int i2cAddr) throws IOException, IllegalStateException {
        if (dac != null) {
            throw new IllegalStateException("Close DAC before reconnecting");
        }

        PeripheralManagerService peripheralManagerService = new PeripheralManagerService();
        dac = peripheralManagerService.openI2cDevice(i2cName, i2cAddr);
    }

    public abstract int getDefaultI2cAddress();

    @Override
    public int getMaxValue() {
        return MAX_VALUE;
    }

    @Override
    public int getMinValue() {
        return MIN_VALUE;
    }

    @Override
    public void close() throws Exception {
        if (dac != null) {
            try {
                dac.close();
            } finally {
                dac = null;
            }
        }
    }
}
