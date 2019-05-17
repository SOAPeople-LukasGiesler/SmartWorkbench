/*
 * Copyright © 2019 Dennis Schulmeister-Zimolong
 * 
 * E-Mail: dhbw@windows3.de
 * Webseite: https://www.wpvs.de/
 * 
 * Dieser Quellcode ist lizenziert unter einer
 * Creative Commons Namensnennung 4.0 International Lizenz.
 */
package de.dhbw.wwi.iot.raspiapp;

import com.google.gson.Gson;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Konstanten und Hilfsmethoden.
 */
public class Utils {
    
    public static final Logger LOGGER = Logger.getLogger(Utils.class.getName());
    public static final Gson GSON = new Gson();

    /**
     * Exception ausgeben
     * @param t Exception
     */
    public static void logException(Throwable t) {
        LOGGER.log(Level.SEVERE, null, t);
    }
    
    /**
     * Setzt vor ein MQTT-Topic den konfigurierten Prefix.
     * 
     * @param topic Name eines Topics ohne Prefix
     * @return Name des Topics mit Prefix
     */
    public static String topic(String topic) {
        return Main.CONFIG.getMqttTopicPrefix() + topic;
    }
    
    /**
     * MQTT-Nachricht für einen beliebigen String erzeugen.
     * @param payload Zu versendender String
     * @return MQTT-Nachricht
     */
    public static MqttMessage message(String payload) {
        MqttMessage message = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
        message.setQos(0);
        return message;
    }
    
    public static MqttMessage message(Object object) {
        return message(GSON.toJson(object));
    }
    
    public static <T> T objectFromMessage(MqttMessage message, Class<T> klass) {
        String json = new String(message.getPayload());
        return GSON.fromJson(json, klass);
    }
}

