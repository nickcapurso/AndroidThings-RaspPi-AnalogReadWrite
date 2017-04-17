package com.capurso.androidthings_analogrw.driver.dac;


public interface Dac extends AutoCloseable {
    void analogWrite(int val) throws Exception;
    int getMaxValue();
    int getMinValue();
}
