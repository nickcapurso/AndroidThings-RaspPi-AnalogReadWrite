package com.capurso.androidthings_analogrw.driver.dac;


import java.io.IOException;
import java.util.Arrays;

import timber.log.Timber;

public class ArduinoProxyDac extends Mcp4725 {
    public static final int DEFAULT_I2C_ADDRESS = 0x77;

    public ArduinoProxyDac(String i2cName) throws IOException {
        super(i2cName);
    }

    /**
     * Sends the shifted value (according to the format required by the MCP4725) to an
     * Arduino, which will then proxy them forward to the actual DAC.
     * <p>
     * See {@link Mcp4725Impl} for more details.
     */
    @Override
    public void analogWrite(int val) throws Exception {
        byte[] writeCmd = new byte[2];
        writeCmd[0] = (byte) (val >> 4);
        writeCmd[1] = (byte) (val << 4);
        Timber.d("Writing to DAC thru Arduino: %s", Arrays.toString(writeCmd));
        dac.write(writeCmd, writeCmd.length);
    }

    @Override
    public int getDefaultI2cAddress() {
        return DEFAULT_I2C_ADDRESS;
    }
}
