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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketException;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

/**
 * Hauptklasse dieses einfach gestrickten IoT-Backends. Zeigt, wie die vom
 * Raspberry Pi gesendeten Daten hier empfangen werden können, macht aber nichts
 * wirklich damit.
 */
public class Main {

    public static final Config CONFIG = new Config();
    public static MqttClient mqttClient = null;
    
    /**
     * Hauptmethode zum Starten des Programms
     *
     * @param args
     * @throws java.io.FileNotFoundException
     * @throws java.net.SocketException
     * @throws org.eclipse.paho.client.mqttv3.MqttException
     */
    public static void main(String[] args) throws FileNotFoundException, SocketException, MqttException, IOException {
        // Konfigurationswerte anzeigen
        CONFIG.printValues();
        System.out.println();

        // Verbindung zum MQTT-Server herstellen
        System.out.println("Stelle Verbindung zum MQTT-Server her");

        String clientId = "backend-" + System.currentTimeMillis();
        mqttClient = connectToMqttServer(clientId);
        installMqttShutdownHandler();

        System.out.println("Verbunden als Client " + clientId);

        // Endlosschleife starten        
        MqttHandler mqttHandler = new MqttHandler();
        mqttHandler.start(mqttClient);
    }

    /**
     * Verbindung zum MQTT-Server herstellen.
     *
     * @param clientId Client-ID für die Anmeldung
     */
    private static MqttClient connectToMqttServer(String clientId) throws MqttException {
        // Benutzername und Passwort setzen
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);

        if (!CONFIG.getMqttUsername().isEmpty()) {
            options.setUserName(CONFIG.getMqttUsername());
        }

        if (!CONFIG.getMqttPassword().isEmpty()) {
            options.setPassword(CONFIG.getMqttPassword().toCharArray());
        }

        // Verbindung herstellen
        MqttClient client = new MqttClient(CONFIG.getMqttServer(), clientId);
        client.connect(options);

        return client;
    }

    /**
     * Sicherstellen, dass die MQTT-Verbindung beim Beenden der Anwendung sauber
     * getrennt wird.
     */
    private static void installMqttShutdownHandler() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    System.out.println("Trenne Verbindung zum MQTT-Server");

                    mqttClient.close();

                    System.out.println("Verbindung getrennt");
                } catch (MqttException ex) {
                    Utils.logException(ex);
                }
            }
        });
    }

}
