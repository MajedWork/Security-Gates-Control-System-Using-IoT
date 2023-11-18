package com.example.mqtt_tag;

//Java built-in libraries

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Paint;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

//sql library
import java.sql.*;

//eclipse paho for creating mqtt client library
import org.eclipse.paho.client.mqttv3.MqttException;

//Apache POI to generate and write Excel files
import org.apache.poi.xssf.usermodel.*;

public class GUIController implements Initializable {

    // TextField elements
    @FXML
    private TextField MQTTBroker_Field_Address; /* to get MQTT broker Adderss */
    @FXML
    private PasswordField MQTTBroker_Field_Password; /* to get MQTT broker Password */
    @FXML
    private TextField MQTTBroker_Field_Username; /* to get MQTT broker Username */
    @FXML
    private TextField usernametTextField; /* to get username */
    @FXML
    private PasswordField passField; /* to get password */
    @FXML
    private TextField FilenameTextField;
    // Editable Button elements
    @FXML
    private Button connectBtn; /* all buttons in GUI */
    @FXML
    private Button unlockBtn;
    // Label elements
    @FXML
    private Label sys_status; /* to show the stats of the system */
    @FXML
    private Label connectionStatus; /* to show the status of the connection */
    @FXML
    private Label vaildationLabel; /* to show the validation status of username and password */
    @FXML
    private Label wrongFileNameStauts; /* to show if the name of the file contain illegal characters */
    // Table elements:
    @FXML
    private TableColumn<Staff, String> idcol; /* Id column */
    @FXML
    private TableColumn<Staff, String> namecol; /* Name column */
    @FXML
    private TableColumn<Staff, String> datecol; /* Date/Time column */
    @FXML
    private TableView<Staff> table; /* Table view */
    // Objects
    private MQTT_Client mqtt = new MQTT_Client(); /* Mqtt client object */
    // Variables
    private int oneTimeThread = 0;
    final private String dbUrl = "jdbc:sqlite:src\\main\\resources\\com\\example\\mqtt_tag\\Database\\MQTTDatabase.db"; /*
                                                                                                                         url
                                                                                                                         for
                                                                                                                         database
                                                                                                                         */
    final private String query = "SELECT Username FROM " + "'Security Staff'" + "WHERE Password = ";

    public void closeSystem() throws IOException {
        createExcelFile(); /* create excel file first */
        System.exit(0); /* close the program */
    }

    public void createExcelFile() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook(); /* create excel workbook */
        XSSFSheet sheet = workbook.createSheet("Recordings"); /* create excel sheet */
        XSSFRow row = sheet.createRow(0); /* create row 0 in the sheet */
        XSSFCell cell = row.createCell(0); /* create cell 0 in row 0 */
        cell.setCellValue("No."); /* set cell 0 value */
        cell = row.createCell(1); /* create cell 1 in row 0 */
        cell.setCellValue("Name"); /* set cell 1 value */
        cell = row.createCell(2); /* create cell 2 in row 0 */
        cell.setCellValue("ID"); /* set cell 2 value */
        cell = row.createCell(3); /* create cell 3 in row 0 */
        cell.setCellValue("Date/Time"); /* set cell 3 value */
        int i = 0;

        // to read from table and add items to Excel sheet
        while (i < table.getItems().size()) {
            row = sheet.createRow(i + 1);
            cell = row.createCell(0);
            cell.setCellValue(i + 1);

            // add name and ID and date
            cell = row.createCell(1);
            cell.setCellValue(table.getItems().get(i).getName());
            cell = row.createCell(2);
            cell.setCellValue(table.getItems().get(i).getID());
            cell = row.createCell(3);
            cell.setCellValue(table.getItems().get(i).getDate());
            i++;
        }
        // to make all columns have their own size
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
        sheet.autoSizeColumn(2);
        sheet.autoSizeColumn(3);
        // save file with specific name
        String filename = FilenameTextField.getText();
        if (filename.contains("/") || filename.contains("\\") || filename.contains(":") || filename.contains("*")
                || filename.contains("?") || filename.contains("\"")
                || filename.contains("<") || filename.contains(">") || filename.contains("|") || filename.isBlank()) {
            wrongFileNameStauts.setText("change the name or add name for the file");
            wrongFileNameStauts.setTextFill(Paint.valueOf("#CF2828"));
        } else {
            wrongFileNameStauts.setText("");
            FileOutputStream fileOutputStream = new FileOutputStream(
                    System.getenv("userprofile") + "\\Documents\\" + filename + ".xlsx"); /* to save file on desktop */
            workbook.write(fileOutputStream);
            fileOutputStream.close();
        }
    }

    public void unlockSystem() throws MqttException, SQLException {
        String pass = passField.getText();
        String username = usernametTextField.getText();
        Connection connection = DriverManager.getConnection(dbUrl);
        try {

            Statement statement = connection.createStatement();
            ResultSet resulttemp = statement.executeQuery(query + "'" + pass + "'"); // to get ID we sent the index
            String result = resulttemp.getString("Username");
            if (result.matches(username)) {
                mqtt.status = "Good!";
                mqtt.tempStatus = "Good!";
                mqtt.client3.publish("/system/app/process/security/response", "unlocked".getBytes(), 0, false);
                passField.clear();
                usernametTextField.clear();
                passField.setDisable(true);
                usernametTextField.setDisable(true);
                unlockBtn.setDisable(true);
                vaildationLabel.setText("");
                connection.close();
            } else {
                vaildationLabel.setText("Error check username or password");
                vaildationLabel.setTextFill(Paint.valueOf("#CF2828"));
                connection.close();
            }
        } catch (Exception e) {
            vaildationLabel.setText("Error check username or password");
            vaildationLabel.setTextFill(Paint.valueOf("#CF2828"));
            connection.close();
        }
    }

    public void connectToMqtt() throws MqttException {
        // setup the mqtt client
        String broker = MQTTBroker_Field_Address.getText(); /* to get address of mqtt broker from the text Field */
        String username = MQTTBroker_Field_Username.getText(); /*
         * to get the username of mqtt broker from the text Field
         */
        char[] password = MQTTBroker_Field_Password.getText().toCharArray();
        if (broker.isBlank() || username.isBlank() || password.length == 0) {
        } else {
            mqtt.connectClient(broker, username, password);
            if (oneTimeThread == 0) { /* only start the thread one time */
                Thread mqtthread = new Thread(mqtt); /* Thread for mqtt client */
                mqtthread.start();
            }

            // listener for all the labels in GUI
            mqtt.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observableValue, String message, String labeltext) {

                    if (labeltext.matches("Good!")) {
                        sys_status.setText(labeltext);
                        sys_status.setTextFill(Paint.valueOf("#0CA380")); /* green color */
                        connectionStatus.setText("Connected");
                        connectionStatus.setTextFill(Paint.valueOf("#0CA380")); /* green color */
                        connectBtn.setDisable(true);
                        MQTTBroker_Field_Address.setDisable(true);
                        MQTTBroker_Field_Address.clear();
                        MQTTBroker_Field_Username.setDisable(true);
                        MQTTBroker_Field_Username.clear();
                        MQTTBroker_Field_Password.setDisable(true);
                        MQTTBroker_Field_Password.clear();
                    }

                    if (labeltext.matches("Alert")) {
                        sys_status.setText("Alert");
                        sys_status.setTextFill(Paint.valueOf("#CF2828")); /* red color */

                        // enable unlock system elements
                        usernametTextField.setDisable(false);
                        passField.setDisable(false);
                        unlockBtn.setDisable(false);
                        connectBtn.setDisable(true);

                        // disable connection elements
                        MQTTBroker_Field_Address.setDisable(true);
                        MQTTBroker_Field_Address.clear();
                        MQTTBroker_Field_Username.setDisable(true);
                        MQTTBroker_Field_Username.clear();
                        MQTTBroker_Field_Password.setDisable(true);
                        MQTTBroker_Field_Password.clear();
                    }

                    if (labeltext.matches("Try again - not connected")) {
                        connectionStatus.setText(labeltext);
                        connectionStatus.setTextFill(Paint.valueOf("#CF2828")); /* red color */
                    }

                    if (labeltext.matches("Connection lost")) {
                        sys_status.setText("Connection Error");
                        sys_status.setTextFill(Paint.valueOf("#CF2828")); /* red color */
                        connectionStatus.setText(labeltext);
                        connectionStatus.setTextFill(Paint.valueOf("#CF2828")); /* red color */
                        connectBtn.setDisable(false);
                        MQTTBroker_Field_Address.setDisable(false);
                        MQTTBroker_Field_Username.setDisable(false);
                        MQTTBroker_Field_Password.setDisable(false);
                        unlockBtn.setDisable(true);
                        usernametTextField.setDisable(true);
                        passField.setDisable(true);
                    }

                }
            });

            // setup the table and its column
            idcol.setCellValueFactory(new PropertyValueFactory<Staff, String>("ID")); /* make ID column read the ID */
            namecol.setCellValueFactory(new PropertyValueFactory<Staff, String>("Name")); /*
             * make Name column read the Name
             */
            datecol.setCellValueFactory(new PropertyValueFactory<Staff, String>("Date")); /*
             * make Date/Time column read the
             * Date
             */

            Table_Managment tableM = new Table_Managment(); /* Table Management task object */
            if (oneTimeThread == 0) { /* only start the thread one time */
                Thread tablethread = new Thread(tableM); /* Thread for table management */
                tablethread.start();
                oneTimeThread++;
            }

            // listener for the tableview in GUI
            tableM.valueProperty().addListener(new ChangeListener<Staff>() {
                @Override
                public void changed(ObservableValue<? extends Staff> observableValue, Staff staff, Staff object) {
                    table.getItems().add(object);
                }
            });

        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // initial setup for table
        idcol.setReorderable(false); /* to make no order-ability */
        namecol.setReorderable(false); /* to make no order-ability */
        datecol.setReorderable(false); /* to make no order-ability */

        // disable unlock system elements
        passField.setDisable(true);
        usernametTextField.setDisable(true);
        unlockBtn.setDisable(true);
    }
}