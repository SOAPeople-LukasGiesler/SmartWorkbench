/*
 * Copyright © 2019 Dennis Schulmeister-Zimolong
 * 
 * E-Mail: dhbw@windows3.de
 * Webseite: https://www.wpvs.de/
 * 
 * Dieser Quellcode ist lizenziert unter einer
 * Creative Commons Namensnennung 4.0 International Lizenz.
 */
package de.dhbw.wwi.iot.raspiapp.gpio;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.RaspiPin;

/**
 * Hilfsklasse zur Initialisierung und Durchführung der GPIO-Kommunikation.
 * Dadurch sollen alle GPIO-Aufrufe an einer zentralen Stelle gesammelt werden.
 */
public class GpioHandler {

    private final GpioController gpio = GpioFactory.getInstance();
    private GpioPinDigitalInput button = null;
    private GpioPinDigitalOutput redLED = null;
    private GpioPinDigitalOutput greenLED = null;

    /**
     * GPIO-Pins initialisieren.
     */
    public void initialize() {
        System.out.println("Initialisiere GPIO-Pins");

        this.button = gpio.provisionDigitalInputPin(RaspiPin.GPIO_27, "Button");
        this.redLED = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_28, "Red LED");
        this.greenLED = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_29, "Green LED");

        this.button.setShutdownOptions(true);
        this.redLED.setShutdownOptions(true);
        this.greenLED.setShutdownOptions(true);

        this.turnOffLEDs();
    }
    
    public void turnOffLEDs() {
        this.greenLED.low();
        this.redLED.low();
        
    }
    public void blinkLEDs() {
        this.greenLED.blink(500);
        this.redLED.blink(500);
        
        this.greenLED.high();
        this.redLED.high();
    }
    
    public void switchToGreenLED() {
        this.greenLED.high();
        this.redLED.low();
    }
    
    public void switchToRedLED() {
        this.greenLED.low();
        this.redLED.high();
    }

    //<editor-fold defaultstate="collapsed" desc="Getter-Methoden">
    public GpioController getGpio() {
        return gpio;
    }
    
    public GpioPinDigitalInput getButton() {
        return button;
    }
    
    public GpioPinDigitalOutput getRedLED() {
        return redLED;
    }
    
    public GpioPinDigitalOutput getGreenLED() {
        return greenLED;
    }
    //</editor-fold>

}
