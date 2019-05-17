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

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Basisklasse für verschiedene Teile der Anwendung, die MQTT-Nachrichten
 * senden und empfangen. Ziel dieses Interfaces ist es, den Quellcode zu
 * strukturieren, indem unterschiedliche Aufgabenbereiche in unterschiedliche,
 * von dieser Klasse erbende Klassen ausgelagert werden.
 */
public abstract class MqttHandler implements IMqttMessageListener {
    
    private MqttClient mqttClient;
    private String topic = "";
    
    /**
     * MQTT-Kommunikation vorbereiten, indem zum Beispiel Topics abonniert
     * oder Anmeldenachrichten verschickt werden.
     *
     * @param mqttClient MQTT-Client-Instanz
     * @throws MqttException Fehler
     */
    public abstract void start(MqttClient mqttClient) throws MqttException;
    
    /**
     * MQTT-Kommunikation stoppen, in dem insbesondere die abonnieren Topics
     * nicht mehr abonniert werden.
     * 
     * @param mqttClient MQTT-Client-Instanz
     * @throws MqttException  Fehler
     */
    public void stop(MqttClient mqttClient) throws MqttException {
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
    }
    
}
