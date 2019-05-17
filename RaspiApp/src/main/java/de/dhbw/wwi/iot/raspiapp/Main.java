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

import de.dhbw.wwi.iot.raspiapp.gpio.GpioHandler;
import de.dhbw.wwi.iot.raspiapp.mqtt.ConnectDeviceMqttHandler;
import de.dhbw.wwi.iot.raspiapp.mqtt.MqttHandler;
import de.dhbw.wwi.iot.raspiapp.mqtt.ReceiveControlMessageMqttHandler;
import de.dhbw.wwi.iot.raspiapp.mqtt.SendSensorDataMqttHandler;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

/**
 * Hauptklasse der Raspberry-Pi-Anwendung. Hier wird eine Verbindung zum
 * MQTT-Server im Backend aufgebaut und anschließend das Protokoll zur Anmeldung
 * eines neuen Devices durchlaufen. Sobald die Anmeldung erfolgreich verlaufen
 * ist, wird die eigentliche Hauptverarbeitung gestartet.
 */
public class Main {

    public static final Config CONFIG = new Config();
    public static final GpioHandler GPIO = new GpioHandler();
    public static MqttClient mqttClient = null;
    
    private static MqttHandler sensorDataMqttHandler = null;
    private static MqttHandler controlMessageMqttHandler = null;
    
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    /**
     * Hauptmethode zum Starten des Programms
     *
     * @param args
     * @throws java.io.FileNotFoundException
     * @throws java.net.SocketException
     * @throws org.eclipse.paho.client.mqttv3.MqttException
     */
    public static void main(String[] args) throws FileNotFoundException, SocketException, MqttException {
        // Konsolenausgaben umleiten
        redirectConsole();

        // Konfigurationswerte anzeigen
        CONFIG.printValues();
        System.out.println();

        // GPIO-Pins initialisieren
        GPIO.initialize();
        
        // Verbindung zum MQTT-Server herstellen
        System.out.println("Stelle Verbindung zum MQTT-Server her");
        
        String clientId = determineMqttClientId();
        mqttClient = connectToMqttServer(clientId);
        installMqttShutdownHandler();
        
        System.out.println("Verbunden als Client " + clientId);
        
        // Device am Backend anmelden und die Kommunikation starten
        MqttHandler connectHandler = new ConnectDeviceMqttHandler();
        startMqttHandler(connectHandler);
    }
    
    /**
     * Neues Handler-Objekt zur MQTT-Kommunikation starten.
     * @param handler MQTT-Handler
     * @throws MqttException Fehler
     */
    public static void startMqttHandler(MqttHandler handler) throws MqttException {
        if (handler != null) {
            handler.start(mqttClient);
        }
    }
    
    /**
     * Handler-Objekt zur MQTT-Kommunikation stoppen.
     * @param handler MQTT-Handler
     * @throws MqttException Fehler
     */
    public static void stopMqttHandler(MqttHandler handler) throws MqttException {
        if (handler != null) {
            handler.stop(mqttClient);
        }
    }
    
    /**
     * Eigentliche Hauptverarbeitung nach erfolgreicher Anmeldung am Backend
     * starten.
     * @throws MqttException Fehler
     */
    public static void onBackendConnected() throws MqttException {
        sensorDataMqttHandler = new SendSensorDataMqttHandler();
        startMqttHandler(sensorDataMqttHandler);
        
        controlMessageMqttHandler = new ReceiveControlMessageMqttHandler();
        startMqttHandler(controlMessageMqttHandler);
    }
    
    public static void exit() throws MqttException {
        GPIO.blinkLEDs();

        stopMqttHandler(sensorDataMqttHandler);
        stopMqttHandler(controlMessageMqttHandler);
        
        System.exit(0);
    }

    /**
     * Konsolenein- und Ausgaben auf den physikalischen Bildschirm umleiten,
     * wenn die Anwendung im Remote-Debugging gestartet wird. Dies wird leider
     * benötigt, da Maven die Konsolenausgaben leider puffert und dadurch erst
     * nach Beenden des Programms anzeigt. So können sie wenigstens noch auf
     * einem angeschlossenen Bildschirm betrachtet werden.
     */
    private static void redirectConsole() throws FileNotFoundException {
        if (!CONFIG.getRedirectTty().isEmpty()) {
            String ttyFile = CONFIG.getRedirectTty();

            System.setOut(new PrintStream(ttyFile));
            System.setErr(new PrintStream(ttyFile));
            System.setIn(new FileInputStream(ttyFile));
        }
    }

    /**
     * Client-ID für die Anmeldung am MQTT-Server ermitteln. Diese muss zwischen
     * allen Clients immer eindeutig sein. Eine einfache Strategie könnte sein,
     * einfach einen feste Prefix plus die aktuelle Zeit und Milisekunden zu
     * verwenden. Hier gehen wir aber anders vor und nehmen die MAC-Adresse des
     * ersten Netzerkinterfaces, um einen Mehrfachstart der Anwendungen auf
     * demselben Gerät zu verhindern.
     *
     * @return Die ermittelte Client-ID
     */
    private static String determineMqttClientId() throws SocketException {
        // MAC-Adresse ermitteln
        String clientId;

        Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
        byte[] mac = null;

        while (nics.hasMoreElements()) {
            NetworkInterface nic = nics.nextElement();

            if (!nic.isVirtual() && nic.isUp()) {
                mac = nic.getHardwareAddress();
                break;
            }
        }

        if (mac == null || mac.length == 0) {
            clientId = "NO-CLIENT-ID";
        } else {
            // Byte-Array in einen Hex-String umwandeln
            // Vgl. https://stackoverflow.com/a/9855338
            char[] macHex = new char[mac.length * 2];

            for (int i = 0; i < mac.length; i++) {
                int v = mac[i] & 0xFF;
                macHex[i * 2] = HEX_ARRAY[v >>> 4];
                macHex[i * 2 + 1] = HEX_ARRAY[v & 0x0F];
            }

            clientId = new String(macHex);
        }

        // Konfigurierten Prefix ergänzen
        clientId = CONFIG.getMqttClientIdPrefix() + clientId;
        return clientId;
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
