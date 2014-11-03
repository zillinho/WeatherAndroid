package com.example.dennis.d2xx;

/**
 * Created by Dennis on 01.11.2014.
 */
public class Measurement {
    private int id;
    private double sht21Temperature;
    private double sht21Humidity;
    private double lm73Temperature;
    private double bmp180Temperature;
    private double bmp180Pressure;
    private  double medianTemperature;

    public Measurement(int id, double sht21Temperature, double sht21Humidity, double lm73Temperature,
                       double bmp180Temperature, double bmp180Pressure) {
        this.id = id;
        this.sht21Temperature = sht21Temperature;
        this.sht21Humidity = sht21Humidity;
        this.lm73Temperature = lm73Temperature;
        this.bmp180Temperature = bmp180Temperature;
        this.bmp180Pressure = bmp180Pressure;
    }

    public Measurement(String[] parsedValues) throws NumberFormatException{
        this.id = Integer.parseInt(parsedValues[0].trim());
        this.sht21Temperature = Double.parseDouble(parsedValues[1].trim());
        this.sht21Humidity = Double.parseDouble(parsedValues[2].trim());
        this.lm73Temperature = Double.parseDouble(parsedValues[3].trim() + "." + parsedValues[4].trim());
        this.bmp180Temperature = Double.parseDouble(parsedValues[5].trim());
        this.bmp180Pressure = Double.parseDouble(parsedValues[6].trim());

        this.medianTemperature = (this.lm73Temperature + this.bmp180Temperature + this.sht21Temperature) / 3;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getSht21Temperature() {
        return sht21Temperature;
    }

    public void setSht21Temperature(double sht21Temperature) {
        this.sht21Temperature = sht21Temperature;
    }

    public double getSht21Humidity() {
        return sht21Humidity;
    }

    public void setSht21Humidity(double sht21Humidity) {
        this.sht21Humidity = sht21Humidity;
    }

    public double getLm73Temperature() {
        return lm73Temperature;
    }

    public void setLm73Temperature(double lm73Temperature) {
        this.lm73Temperature = lm73Temperature;
    }

    public double getBmp180Temperature() {
        return bmp180Temperature;
    }

    public void setBmp180Temperature(double bmp180Temperature) {
        this.bmp180Temperature = bmp180Temperature;
    }

    public double getBmp180Pressure() {
        return bmp180Pressure;
    }

    public void setBmp180Pressure(double bmp180Pressure) {
        this.bmp180Pressure = bmp180Pressure;
    }

    public double getMedianTemperature() {
        return medianTemperature;
    }

    public void setMedianTemperature(double medianTemperature) {
        this.medianTemperature = medianTemperature;
    }
}
