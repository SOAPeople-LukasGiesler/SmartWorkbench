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

import de.dhbw.wwi.iot.raspiapp.Main;
import de.dhbw.wwi.iot.raspiapp.Utils;
import de.dhbw.wwi.iot.raspiapp.mqtt.ReceiveControlMessageMqttHandler.ControlMessage;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * MQTT-Klasse zum Empfang von Steuernachrichten aus dem Backend.
 */
public class ReceiveControlMessageMqttHandler extends MqttHandler {

    private MqttClient mqttClient;
    private String topic = "";
    
    @Override
    public void start(MqttClient mqttClient) throws MqttException {
        System.out.println("Starte Empfang von Kontrollnachrichten aus dem Backend");
        
        // Topic, von welchem die Daten empfangen werden
        this.mqttClient = mqttClient;
        this.topic = Utils.topic("/control/" + mqttClient.getClientId());
        
        mqttClient.subscribe(topic, this);
    }

    @Override
    public void stop(MqttClient mqttClient) throws MqttException {
        this.mqttClient.unsubscribe(this.topic);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        ControlMessage controlMessage = Utils.objectFromMessage(message, ControlMessage.class);
        
        if (controlMessage.method.equalsIgnoreCase(("DISPLAY_TEXT"))) {
            System.out.println("Backend hat eine DISPLAY_TEXT-Nachricht gesendet.");
            System.out.println("---> " + controlMessage.value);
        } else if (controlMessage.method.equalsIgnoreCase(("SHUTDOWN"))) {
            System.out.println("Backend hat eine SHUTDOWN-Nachricht gesendet.");
            Main.exit();
        }
    }
    
    public static class ControlMessage {
        public String clientId = "";
        public String method = "";
        public String value = "";
    }
}
