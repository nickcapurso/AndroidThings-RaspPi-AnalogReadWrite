package com.capurso.androidthings_analogrw.driver.adc;


public interface Adc<T> extends AutoCloseable {
    int analogRead(T channel) throws Exception;
    int getMaxValue();
    int getMinValue();
}
