package com.capurso.androidthings_analogrw.driver.dac;


import java.io.IOException;
import java.util.Arrays;

import timber.log.Timber;

/**
 * 12-bit digital-to-analog converter. Interfaced with I2C.
 * <p>
 * Datasheet:
 * http://ww1.microchip.com/downloads/en/DeviceDoc/22039d.pdf
 */
public class Mcp4725Impl extends Mcp4725 {

    public static final int DEFAULT_I2C_ADDRESS = 0x60;

    private static final byte WRITE_MODE = 0x40;

    private static final byte POWER_DOWN_DEFAULT_MODE = 0x00;

    public Mcp4725Impl(String i2cName) throws IOException {
        super(i2cName);
    }

    public Mcp4725Impl(String i2cName, int i2cAddr) throws IOException {
        super(i2cName, i2cAddr);
    }

    /**
     * Write operations are defined on page 23 of the datasheet. Here, the value is only
     * written to the DAC register for speed (i.e. not to EEPROM).
     * <p>
     * Three bytes are needed to be transmitted, total of 24 bits:
     * - A command byte (indicates the command the power-down mode)
     * - Using write-to-DAC-only command (1 in bit 6) & default power-down mode (0's in bits 1-2)
     * - 10 bits of data
     * - 4 don't cares (using 0's here)
     */
    @Override
    public void analogWrite(int val) throws Exception {
        if (val > getMaxValue() || val < getMinValue()) {
            throw new Exception("val must be between 0 and 4095, inclusive");
        } else if (dac == null) {
            throw new IllegalStateException("DAC not opened");
        }

        byte[] writeCmd = new byte[3];
        writeCmd[0] = WRITE_MODE | POWER_DOWN_DEFAULT_MODE;
        writeCmd[1] = (byte) (val >> 4);
        writeCmd[2] = (byte) (val << 4);
        Timber.d("Writing to DAC MCP 4725: %s", Arrays.toString(writeCmd));
        dac.write(writeCmd, writeCmd.length);
    }

    @Override
    public int getDefaultI2cAddress() {
        return DEFAULT_I2C_ADDRESS;
    }
}
