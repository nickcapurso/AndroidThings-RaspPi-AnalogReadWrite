package com.capurso.androidthings_analogrw.driver.dac;


import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

/**
 * 12-bit digital-to-analog converter. Interfaced with I2C.
 *
 * Datasheet:
 * http://ww1.microchip.com/downloads/en/DeviceDoc/22039d.pdf
 */
public class Mcp4725 implements Dac {
    private static final String TAG = Mcp4725.class.getName();

    public static final int DEFAULT_I2C_ADDRESS = 0x60;

    final byte WRITE_MODE = 0x40;

    final byte POWER_DOWN_DEFAULT_MODE = 0x00;

    private I2cDevice dac;

    public Mcp4725(String i2cName) throws IOException {
        this(i2cName, DEFAULT_I2C_ADDRESS);
    }

    public Mcp4725(String i2cName, int i2cAddr) throws IOException {
        connect(i2cName, i2cAddr);
    }

    public void connect(String i2cName, int i2cAddr) throws IOException {
        PeripheralManagerService peripheralManagerService = new PeripheralManagerService();
        dac = peripheralManagerService.openI2cDevice(i2cName, i2cAddr);
    }

    @Override
    public void close() throws IOException {
        dac.close();
    }

    /**
     * Write operations are defined on page 23 of the datasheet. Here, the value is only
     * written to the DAC register for speed (i.e. not to EEPROM).
     *
     * Three bytes are needed to be transmitted, total of 24 bits:
     *      - A command byte (indicates the command the power-down mode)
     *          - Using write-to-DAC-only command (1 in bit 6) & default power-down mode (0's in bits 1-2)
     *      - 10 bits of data
     *      - 4 don't cares
     */
    @Override
    public void analogWrite(int val) throws Exception {
        if (val > 4096 || val < 0) {
            throw new Exception("val must be between 0 and 4095, inclusive");
        }

        /*
         * Although this works on the Arduino, Android Things is not allowing the
         * MCP4725 to pull the clock low for an ACK and is not sending them between bytes.
         *      Verified by checking on oscilloscope.
         */
//        byte[] writeCmd = new byte[3];
//        writeCmd[0] = WRITE_MODE | POWER_DOWN_DEFAULT_MODE;
//        writeCmd[1] = (byte) (val >> 4);
//        writeCmd[2] = (byte) (val << 4);
//        Log.d(TAG, "Writing: " + Arrays.toString(writeCmd));
//        dac.write(writeCmd, writeCmd.length);

        // Works to an extent, for now.
        dac.writeRegWord(WRITE_MODE | POWER_DOWN_DEFAULT_MODE, (short) val);
    }
}
