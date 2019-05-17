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
import java.util.Timer;
import java.util.TimerTask;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * MQTT-Klasse zur Anmeldung am Backend. Wenn die Anwendung startet wird erst
 * mit dieser Klasse immer wieder eine Anfrage an das Backend gesendet, bis
 * das Backend die Anfrage annimmt und bestätigt. Anschließend wird das
 * eigentliche Handler-Objekt zum Versenden der Sensordaten erzeugt.
 */
public class ConnectDeviceMqttHandler extends MqttHandler {

    private MqttClient mqttClient;
    private String requestTopic = "";
    private String responseTopic = "";
    private final Timer retryTimer = new Timer("Connect Retry Timer", false);
    private long retryPeriod = 54000;

    @Override
    public void start(MqttClient mqttClient) throws MqttException {
        this.mqttClient = mqttClient;
        this.requestTopic = Utils.topic("/device_manager/request");
        this.responseTopic = Utils.topic("/device_manager/response/" + mqttClient.getClientId());
        
        // LEDs für die Dauer der Anmeldung blinken
        Main.GPIO.blinkLEDs();
        
        // Topic für die Antworten des Backends überwachen
        mqttClient.subscribe(responseTopic, this);

        // Timer starten, der alle 15 Minuten einen Anmeldeversuch unternimmt,
        // wenn die Anmeldung nicht sofort funktioniert        
        if (Main.CONFIG.getConnectRetryTimeout() != null) {
            this.retryPeriod = Long.parseLong(Main.CONFIG.getConnectRetryTimeout());
        }
        
        retryTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // Anmelde-Anfrage an das Backend senden
                try {
                    System.out.println("Sende CONNECT-Nachricht, um sich mit dem Backend zu verbinden");
                    
                    Request request = new Request();
                    request.clientId = mqttClient.getClientId();
                    request.method = "CONNECT";
                    
                    MqttMessage message = Utils.message(request);
                    mqttClient.publish(requestTopic, message);
                } catch (MqttException ex) {
                    Utils.logException(ex);
                }
            }
        }, 0, this.retryPeriod);
    }

    @Override
    public void stop(MqttClient mqttClient) throws MqttException {
        mqttClient.unsubscribe(this.responseTopic);
        this.retryTimer.cancel();
    }
    
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        Response response = Utils.objectFromMessage(message, Response.class);
        
        if (response.result.equalsIgnoreCase("OKAY")) {
            // Backend hat die Anfrage bestätigt. Umschalten auf eigentliche
            // Anwendungsfunktionen. Andernfalls probieren wir es einfach
            // später wieder.
            System.out.println("Backend hat die Vebrindung mit OKAY bestätigt. Juhu!");
            Main.stopMqttHandler(this);
            Main.onBackendConnected();
        } else if (response.result.equalsIgnoreCase("PLEASE_WAIT")) {
            System.out.println("Backend sagt PLEASE_WAIT und will, dass wir auf die Bestätigung warten.");
            System.out.println("Spätestens in " + this.retryPeriod + " Sekunden probieren wir es wieder.");
        }
    }
    
    public static class Request {
        public String clientId = "";
        public String method = "";
    }
    
    public static class Response {
        public String clientId = "";
        public String result = "";
    }

}
