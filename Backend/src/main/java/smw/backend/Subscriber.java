/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smw.backend;

import java.net.URI;
import java.net.URISyntaxException;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 *
 * @author giesler
 */
public class Subscriber {
    private static final String USERNAME = "smart-workbench";
    private static final String PASSWORD = "Smart/WB";
    private static final String TOPIC = "/smw/";
    
    public Subscriber() throws MqttException{
        
        MqttClient client = new MqttClient("ssl://mqtt.iot-embedded.de:8883", MqttClient.generateClientId());
        
        MqttConnectOptions connOpts = setUpConnectionOptions(USERNAME, PASSWORD);
        client.connect(connOpts);
        client.subscribe(TOPIC);
        
            client.setCallback(new MqttCallback(){
            
            @Override
            public void connectionLost(Throwable throwable){}
            
            @Override
            public void messageArrived(String t, MqttMessage m) throws Exception {
                System.out.println(new String(m.getPayload()));
            }
            
            @Override
            public void deliveryComplete(IMqttDeliveryToken t){}
        });

    }
    
    private static MqttConnectOptions setUpConnectionOptions(String username, String password){
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        connOpts.setUserName(username);
        connOpts.setPassword(password.toCharArray());
        return connOpts;
        
    }
}
