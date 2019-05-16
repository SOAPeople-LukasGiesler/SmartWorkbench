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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 *
 * @author dennis
 */
public class MqttHandler implements IMqttMessageListener {

    private MqttClient mqttClient;
    private String connectRequestTopic = "";
    private String connectResponseTopic = "";
    private String sensorDataTopic = "";
    private String controlMessageTopic = "";

    private final List<String> waitingDevices = new ArrayList<>();
    private final List<String> approvedDevices = new ArrayList<>();

    public void start(MqttClient mqttClient) throws MqttException, IOException {
        // Benötigte Topics
        System.out.println("Starte MQTT-Verarbeitung");

        this.mqttClient = mqttClient;
        this.connectRequestTopic = Utils.topic("/device_manager/request");
        this.connectResponseTopic = Utils.topic("/device_manager/response/"); // + ClientId
        this.sensorDataTopic = Utils.topic("/sensors/#");
        this.controlMessageTopic = Utils.topic("/control/"); // + ClientId

        mqttClient.subscribe(connectRequestTopic, this);
        mqttClient.subscribe(sensorDataTopic, this);

        // Hauptmenü für den Benutzer zeigen
        this.showMainMenu();
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        if (topic.startsWith("/sensors/")) {
            System.out.println("Empfange Sensordaten: " + new String(message.getPayload()));
            SensorDataMessage sensorDataMessage = Utils.objectFromMessage(message, SensorDataMessage.class);
        } else if (topic.startsWith(connectRequestTopic)) {
            System.out.println("Empfange Verbindungsanfrage: " + new String(message.getPayload()));
            ConnectRequest connectRequest = Utils.objectFromMessage(message, ConnectRequest.class);
            
            if (!approvedDevices.contains(connectRequest.clientId)) {
                waitingDevices.add(connectRequest.clientId);
            }
        }
    }

    private void showMainMenu() throws IOException, MqttException {
        BufferedReader fromKeyboard = new BufferedReader(new InputStreamReader(System.in));
        boolean exit = false;

        while (!exit) {
            System.out.println("=========");
            System.out.println("Hauptmenü");
            System.out.println("=========");
            
            System.out.println();
            System.out.println("Auf Genehmigung wartende Devices:");
            
            for (String device : waitingDevices) {
                System.out.println(" >> " + device);
            }
            
            System.out.println();
            System.out.println("Bereits genehmigte Devices:");
            
            for (String device : approvedDevices) {
                System.out.println(" >> " + device);
            }
            
            System.out.println();
            System.out.println("[1] Genehmige ein wartendes Device");
            System.out.println("[2] Sende SHUTDOWN an ein Device");
            System.out.println("[3] Sende DISPLAY_TEXT an ein Device");
            System.out.println("[E] Ende");
            System.out.println();
            
            System.out.print("Ihre Wahl: ");
            String command = fromKeyboard.readLine();
            
            String clientId;
            String text;
            ConnectResponse connectResponse;
            ControlMessage controlMessage;
            MqttMessage message;
            
            switch (command) {
                case "1":
                    // Genehmige ein wartendes Device
                    System.out.print("Client ID: ");
                    clientId = fromKeyboard.readLine();
                    
                    waitingDevices.remove(clientId);
                    approvedDevices.add(clientId);
                    
                    connectResponse = new ConnectResponse();
                    connectResponse.clientId = clientId;
                    connectResponse.result = "OKAY";
                    
                    message = Utils.message(connectResponse);
                    mqttClient.publish(this.connectResponseTopic, message);
                    break;
                case "2":
                    // Sende SHUTDOWN an ein Device
                    System.out.print("Client ID: ");
                    clientId = fromKeyboard.readLine();
                    
                    controlMessage = new ControlMessage();
                    controlMessage.clientId = clientId;
                    controlMessage.method = "SHUTDOWN";
                    
                    message = Utils.message(controlMessage);
                    mqttClient.publish(this.controlMessageTopic + clientId, message);
                    break;
                case "3":
                    // Sende DISPLAY_TEXT an ein Device
                    System.out.print("Client ID: ");
                    clientId = fromKeyboard.readLine();
                    
                    System.out.print("Text: ");
                    text = fromKeyboard.readLine();
                    
                    controlMessage = new ControlMessage();
                    controlMessage.clientId = clientId;
                    controlMessage.method = "DISPLAY_TEXT";
                    controlMessage.value = text;
                    
                    message = Utils.message(controlMessage);
                    mqttClient.publish(this.controlMessageTopic + clientId, message);
                    break;
                case "E":
                    exit = true;
                    break;
                default:
                    System.out.println("Ungültige Eingabe!");
            }
        }
    }

    public static class ConnectRequest {

        public String clientId = "";
        public String method = "";
    }

    public static class ConnectResponse {

        public String clientId = "";
        public String result = "";
    }

    public static class SensorDataMessage {

        public String clientId = "";
        public long timestamp = System.currentTimeMillis();
        public String sensor = "";
        public double value = 0.0;
    }

    public static class ControlMessage {

        public String clientId = "";
        public String method = "";
        public String value = "";
    }
}
