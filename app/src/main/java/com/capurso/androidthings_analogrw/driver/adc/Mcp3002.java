package com.capurso.androidthings_analogrw.driver.adc;


import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.SpiDevice;

import java.io.IOException;
import java.util.Arrays;

import timber.log.Timber;

/**
 * Two channel 10-bit analog to digital converter. Interfaced with SPI.
 * <p>
 * Datasheet:
 * http://ww1.microchip.com/downloads/en/DeviceDoc/21294E.pdf
 */
public class Mcp3002 implements Adc<AdcChannel> {
    public static final int MAX_VALUE = 1023;

    public static final int MIN_VALUE = 0;

    private SpiDevice adc;

    public Mcp3002(String spiName) throws IOException, IllegalStateException {
        connect(spiName);
    }

    public void connect(String spiName) throws IOException, IllegalStateException {
        if (adc != null) {
            throw new IllegalStateException("Close ADC before reconnecting");
        }

        PeripheralManagerService peripheralManagerService = new PeripheralManagerService();
        adc = peripheralManagerService.openSpiDevice(spiName);
        adc.setMode(SpiDevice.MODE0);
        adc.setFrequency(1200000); //  1.2 MHz
        adc.setBitsPerWord(8);
        adc.setBitJustification(false); // MSB first
    }

    @Override
    public void close() throws IOException {
        if (adc != null) {
            try {
                adc.close();
            } finally {
                adc = null;
            }
        }
    }

    /**
     * Data transfer for reading is explained on page 15 of the datasheet.
     * - First five control bits are (for two-channel mode):
     * - A 0 bit for padding
     * - Start bit (1)
     * - Two-channel mode indicator (1)
     * - Channel select (0 = channel 0, 1 = channel 1)
     * - MSBF vs. LSBF (0 = LSBF, 1 = MSBF)
     * - Then, 10-bits of Don't Cares are transmitted while the slave responds with the
     *   10-bit analog value at the same time.
     */
    @Override
    public int analogRead(AdcChannel channel) throws IOException, IllegalStateException {
        if (adc == null) {
            throw new IllegalStateException("ADC not opened");
        }

        byte[] txAdc = new byte[2];
        byte[] rxAdc = new byte[2];

        txAdc[0] = (byte) (0x68 | channel.ordinal() << 4);
        txAdc[1] = (byte) 0x00;

        adc.transfer(txAdc, rxAdc, rxAdc.length);
        Timber.d("R/W ADC MCP 3002: channel %s, tx %s, rx %s", channel.ordinal() + 1, Arrays.toString(txAdc), Arrays.toString(rxAdc));
        return (rxAdc[0] & 0x03) << 8 | (rxAdc[1] & 0xFF);
    }

    @Override
    public int getMaxValue() {
        return MAX_VALUE;
    }

    @Override
    public int getMinValue() {
        return MIN_VALUE;
    }
}
