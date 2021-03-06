/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.paypal.developer.jeffprestes.lightbulbswitch;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 *
 * @author jeffprestes
 */
public class IoTMqttListener implements MqttCallback {

    private GpioPinDigitalOutput pin;
    private MqttAsyncClient client;
    private String queue;
    
    public IoTMqttListener(GpioPinDigitalOutput parPin, MqttAsyncClient client, String queue)  {
        this.pin = parPin;
        this.client = client;
        this.queue = queue;
    }
    
    @Override
    public void connectionLost(Throwable th) {
        System.out.println("Connection was closed");
        th.printStackTrace();
        System.exit(0);
    }

    @Override
    public void messageArrived(String topic, MqttMessage mm)  {
        System.out.println("=Message arrived: " + mm.toString() + " at " + topic);
        
        if (mm.toString().equals("alive"))      {
            this.publish("yes");
            
        } else if (mm.toString().equals("lighton"))  {
            // turn on gpio pin #02
            pin.high();
            System.out.println("--> GPIO state should be: ON");
            
            this.publish("light on");
        
        } else if (mm.toString().equalsIgnoreCase("lightoff"))  {
            // turn off gpio pin #02
            pin.low();
            System.out.println("--> GPIO state should be: OFF");
            this.publish("light off");
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken imdt) {
        
        if (imdt == null)     {
            System.out.println("DeliveryToke is null");
            return;
        }
        
        try {
            System.out.println("A message was deliveried into " + imdt.getTopics()[0]);
            
        } catch (NullPointerException npe)  {
            System.out.println("msg "+ npe.getMessage());
            System.out.println("loc "+ npe.getLocalizedMessage());
            System.out.println("cause "+ npe.getCause());
            System.out.println("excep "+ npe);
            npe.printStackTrace();
        }
    }
    
    
    public void publish(String msg)  {
        
        System.out.println("+Publishing message: "+msg);
        MqttMessage message = new MqttMessage(msg.getBytes());
        message.setQos(2);
        
        try {
            client.publish(queue, message);
            System.out.println("+Message published");  
            
        } catch(MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
        }
    }
}
