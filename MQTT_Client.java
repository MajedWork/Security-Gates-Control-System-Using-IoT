package com.example.mqtt_tag;
//Java built-in libraries

import javafx.concurrent.Task;

import java.util.Date;

//sql library
import java.sql.*;

//eclipse paho for creating mqtt client library
import org.eclipse.paho.client.mqttv3.*;

public class MQTT_Client extends Task<String> implements MqttCallback {

    // Objects
    protected static MqttAsyncClient client3; /* asynchronous mqtt client */
    // Variables
    protected static String status = "";
    protected static String tempStatus = "Good!"; /* to store the status if connection lost happen */
    final private String dbUrl = "jdbc:sqlite:src\\main\\resources\\com\\example\\mqtt_tag\\Database\\MQTTDatabase.db";
    final private String query1 = "SELECT ID FROM Staff WHERE Indices = "; /* to get ID from specific Index database */
    final private String query2 = "SELECT Name FROM Staff WHERE Indices = "; /*
     * to get Name of specific Index from
     * database
     */
    private Table_Managment tableTemp = new Table_Managment(); /* create table management object */
    private String tempName = null; /* to store successful entered user name */
    private String tempID = null; /* to store successful entered user ID */

    public static void connectClient(String broker, String username, char[] password) throws MqttException {
        try {
            client3 = new MqttAsyncClient("tcp://" + broker, "Client 3");
            MqttConnectOptions connectionOptions = new MqttConnectOptions();
            connectionOptions.setUserName(username); /* set username of the broker */
            connectionOptions.setPassword(password); /* set password of the broker */
            client3.setCallback(new MQTT_Client()); /* set the callback */
            client3.connect(connectionOptions).waitForCompletion(); /* establish the connection */

            // set the subscription for topics
            client3.subscribe("/system/gate1/process/authenticate", 0); /* to get data from gate 1 */
            client3.subscribe("/system/gate1/process/security", 0); /* to get an alert state from gate 1 */
            client3.subscribe("/system/gate2/process/sensor/status", 0); /*
             * to know if there is car between gate 1 and
             * gate 2
             */
            status = tempStatus; /* to change the system status */
        } catch (Exception e) {
            status = "Try again - not connected";
        }

    }

    @Override
    public void connectionLost(Throwable throwable) {
        status = "Connection lost";
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {

        if (topic.matches("/system/gate1/process/authenticate")) { /* to help client 1 to validate the user */
            String index = mqttMessage.toString().substring(0, mqttMessage.toString().lastIndexOf(",")).trim(); /*
             * to
             * get
             * the
             * index
             * from
             * the
             * message
             */
            String ID = mqttMessage.toString().substring(mqttMessage.toString().lastIndexOf(",")).substring(1)
                    .trim(); /* to get the id from the message */
            Connection   connection = DriverManager.getConnection(dbUrl);
            try {

                Statement statement = connection.createStatement(); /* create statement to execute later on database */
                ResultSet resulttemp = statement.executeQuery(query1 + index); /* to get ID we sent the index */
                String result = resulttemp.getString("ID"); /* to get ID data */
                connection.close();
                if (result.matches(ID)) {
                    client3.publish("/system/app/process/authenticate/response", "ok".getBytes(), 0, false); /*
                     * valid
                     * user
                     */
                    tempID = ID;
                    connection = DriverManager.getConnection(dbUrl);
                    statement = connection.createStatement();
                    resulttemp = statement.executeQuery(query2 + index); /* to get Name we sent the index */
                    result = resulttemp.getString("Name"); /* to get Name data */
                    connection.close();
                    tempName = result;

                } else { /* invalid user */
                    client3.publish("/system/app/process/authenticate/response", "no".getBytes(), 0, false);
                    connection.close();

                }
            } catch (Exception e) { /* invalid user */
                client3.publish("/system/app/process/authenticate/response", "no".getBytes(), 0, false);
                connection.close();

            }
        }

        if (topic.matches("/system/gate2/process/sensor/status") && mqttMessage.toString().matches("yes")) {/*
         * to add
         * new user
         * to the
         * table if
         * passes
         * the gates
         */

            tableTemp.name = tempName; /* pass the name of valid user to table class for later add */
            tableTemp.id = tempID; /* pass the id of valid user to table class for later add */
            Date date = new Date(); /* pass the date when the user passed gates */
            tableTemp.date = date.toString();
            tableTemp.addNewStaff = 1; /* to add user to the table unlock the adding process */

            // clear the Name and ID variables
            tempName = null;
            tempID = null;
        }

        if (topic.matches("/system/gate1/process/security") && mqttMessage.toString().matches("Alert")) { /*
         * to alert
         * the
         * security
         * staff about
         * the error
         * in the
         * system
         */
            status = "Alert";
            tempStatus = status;
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }

    @Override
    protected String call() throws Exception {
        while (true) {
            updateValue(status); /* to update system label in GUI */
        }
    }
}
