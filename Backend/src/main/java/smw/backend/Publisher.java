/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smw.backend;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 *
 * @author giesler
 */
public class Publisher {
    
    String USERNAME = "smart-workbench";
    String PASSWORD = "Smart/WB";
    String TOPIC = "/smw/";
    
    public Publisher() throws MqttException{
        /*
        MqttClient client = new MqttClient("ssl://mqtt.iot-embedded.de:8883", MqttClient.generateClientId());
        
        MqttConnectOptions connOpts = setUpConnectionOptions(USERNAME, PASSWORD);
        client.connect(connOpts);
        
       
        MqttMessage message = new MqttMessage("Hello world from MQTT!".getBytes());
        message.setQos(0);
        client.publish(TOPIC, message);
        
        client.disconnect();
        */
    }
    
    public void publishMessage(String p_message) throws MqttException{
        
        MqttClient client = new MqttClient("ssl://mqtt.iot-embedded.de:8883", MqttClient.generateClientId());
        
        MqttConnectOptions connOpts = setUpConnectionOptions(USERNAME, PASSWORD);
        client.connect(connOpts);
        MqttMessage message = new MqttMessage("Hello world from MQTT!".getBytes());
        message.setQos(0);
        client.publish(TOPIC, message);
        
        client.disconnect();
    }
    
    private static MqttConnectOptions setUpConnectionOptions(String username, String password){
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        connOpts.setUserName(username);
        connOpts.setPassword(password.toCharArray());
        return connOpts;
    }
}
