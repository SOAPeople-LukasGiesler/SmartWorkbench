/*
 * Copyright © 2019 Dennis Schulmeister-Zimolong
 * 
 * E-Mail: dhbw@windows3.de
 * Webseite: https://www.wpvs.de/
 * 
 * Dieser Quellcode ist lizenziert unter einer
 * Creative Commons Namensnennung 4.0 International Lizenz.
 */
package de.dhbw.wwi.iot.backend_simple;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Hilfsklasse zum Einlesen und Verwalten der Anwendungskonfiguration. Die Werte
 * werden aus der Datei config.properties gelesen und anschließend um die über
 * Umgebungsvariablen definierten Werte ergänzt. Nicht konfigurierte Werte
 * werden als Leerstring gespeichert.
 */
public class Config {

    private String mqttServer = null;
    private String mqttUsername = null;
    private String mqttPassword = null;
    private String mqttTopicPrefix = null;
    private String mqttClientIdPrefix = null;

    /**
     * Konstruktor.
     */
    public Config() {
        // config.properties lesen
        ResourceBundle bundle = ResourceBundle.getBundle("config");

        this.mqttServer = readValue(bundle, "MQTT_SERVER");
        this.mqttUsername = readValue(bundle, "MQTT_USERNAME");
        this.mqttPassword = readValue(bundle, "MQTT_PASSWORD");
        this.mqttTopicPrefix = readValue(bundle, "MQTT_TOPIC_PREFIX");
        this.mqttClientIdPrefix = readValue(bundle, "MQTT_CLIENTID_PREFIX");
    }

    /**
     * Hilfsmethode zum Einlesen eines Konfigurationswerts.
     *
     * @param bundle config.properties
     * @param key Gesuchter Schlüssel
     * @return Gefundener Wert
     */
    private static String readValue(ResourceBundle bundle, String key) {
        String value;

        try {
            value = bundle.getString(key);
        } catch (MissingResourceException ex) {
            value = System.getenv(key);
        }

        if (value != null) {
            value = value.strip();
        } else {
            value = "";
        }

        return value;
    }

    /**
     * Ausgaben aller Konfigurationswerte auf der Konsole.
     */
    public void printValues() {
        System.out.println("MQTT_SERVER          = " + this.mqttServer);
        System.out.println("MQTT_USERNAME        = " + this.mqttUsername);
        System.out.println("MQTT_PASSWORD        = " + this.mqttPassword);
        System.out.println("MQTT_TOPIC_PREFIX    = " + this.mqttTopicPrefix);
        System.out.println("MQTT_CLIENTID_PREFIX = " + this.mqttClientIdPrefix);
    }

    //<editor-fold defaultstate="collapsed" desc="Getter-Methoden">
    public String getMqttServer() {
        return mqttServer;
    }

    public String getMqttUsername() {
        return mqttUsername;
    }

    public String getMqttPassword() {
        return mqttPassword;
    }

    public String getMqttTopicPrefix() {
        return mqttTopicPrefix;
    }

    public String getMqttClientIdPrefix() {
        return mqttClientIdPrefix;
    }
    //</editor-fold>

}
