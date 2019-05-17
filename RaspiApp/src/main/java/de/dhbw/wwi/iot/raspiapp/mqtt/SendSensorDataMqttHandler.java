/*
 * Copyright © 2019 Dennis Schulmeister-Zimolong
 * 
 * E-Mail: dhbw@windows3.de
 * Webseite: https://www.wpvs.de/
 * 
 * Dieser Quellcode ist lizenziert unter einer
 * Creative Commons Namensnennung 4.0 International Lizenz.
 */
package de.dhbw.wwi.iot.raspiapp.mqtt;

import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListener;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import de.dhbw.wwi.iot.raspiapp.Main;
import de.dhbw.wwi.iot.raspiapp.Utils;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * MQTT-Klasse zum Auslesen und Versenden der Sensordaten.
 */
public class SendSensorDataMqttHandler extends MqttHandler {

    private MqttClient mqttClient;
    private String topic = "";
    private GpioPinListener buttonListener = null;
    
    @Override
    public void start(MqttClient mqttClient) throws MqttException {
        System.out.println("Starte Überwachung und Versand der Sensordaten");
        
        // Mit grüner LED anfangen
        Main.GPIO.switchToGreenLED();
        
        // Topic, an welches die Daten gesendet werden
        this.mqttClient = mqttClient;
        this.topic = Utils.topic("/sensors/" + mqttClient.getClientId());
        
        // Button abfragen und Statuswechsel an das Backend senden
        this.buttonListener = (GpioPinListenerDigital) (GpioPinDigitalStateChangeEvent event) -> {
            try {
                SensorDataMessage sensorMessage = new SensorDataMessage();
                sensorMessage.clientId = mqttClient.getClientId();
                sensorMessage.sensor = "Button";
                
                if (event.getState().isHigh()) {
                    System.out.println("Button wurde gedrückt, sende Sensordaten");
                    Main.GPIO.switchToRedLED();
                    sensorMessage.value = 1.0;
                } else {
                    System.out.println("Button wurde losgelassen, sende Sensordaten");
                    Main.GPIO.switchToGreenLED();
                    sensorMessage.value = 0.0;
                }
                
                MqttMessage mqttMessage = Utils.message(sensorMessage);
                mqttClient.publish(topic, mqttMessage);
            } catch (MqttException ex) {
                Utils.logException(ex);
            }
        };
        
        Main.GPIO.getButton().addListener(buttonListener);
    }

    @Override
    public void stop(MqttClient mqttClient) throws MqttException {
        System.out.println("Stope Überwachung und Versand der Sensordaten");
        Main.GPIO.getButton().removeListener(this.buttonListener);
        Main.GPIO.turnOffLEDs();
    }
    
    public static class SensorDataMessage {
        public String clientId = "";
        public long timestamp = System.currentTimeMillis();
        public String sensor = "";
        public double value = 0.0;
    }

}
