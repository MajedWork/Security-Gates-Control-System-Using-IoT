package com.example.mqtt_tag;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class LoginController {

    // Text Fields elements
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField usernameField;

    // Label element
    @FXML
    private Label vaildationLabel;

    // Radio buttons elements
    @FXML
    private RadioButton database_radioBtn;
    @FXML
    private RadioButton monitor_radioBtn;

    // Objects
    private Stage stage = new Stage(); /* to create the new stage */

    // Varibales
    final private String dbUrl = "jdbc:sqlite:src\\main\\resources\\com\\example\\mqtt_tag\\Database\\MQTTDatabase.db"; //url for database
    final private String query = "SELECT Username FROM " + "'Security Staff'" + "WHERE Password = "; //for Security Staff table
    final private String query2 = "SELECT Username FROM " + "'Database Managers'" + "WHERE Password = "; //* for database managers table
    protected static String currentUser = "";
    private int choice = 0;
    private int styleOneTime = 0;

    public void closeSystem() {
        System.exit(0);
    }

    //to control radio buttons
    public void login_to_Database_Manager() {
        database_radioBtn.setSelected(true);
        monitor_radioBtn.setSelected(false);
        choice = 1;
    }

    public void login_to_Monitor() {
        database_radioBtn.setSelected(false);
        monitor_radioBtn.setSelected(true);
        choice = 0;
    }

    public void login_check() {
        String pass = passwordField.getText();
        String username = usernameField.getText();
        if (choice == 0) { /* login to monitor system */
            try {
                Connection connection = DriverManager.getConnection(dbUrl);
                Statement statement = connection.createStatement();
                ResultSet resulttemp = statement.executeQuery(query + "'" + pass + "';"); // to get ID we sent the index
                String result = resulttemp.getString("Username");
                connection.close(); /* close the connection */
                if (result.matches(username)) {
                    // clear fields
                    passwordField.clear();
                    usernameField.clear();
                    FXMLLoader fxmlLoader = new FXMLLoader(GUIController.class.getResource("GUI_FXML.fxml")); // to open gui for monitoring system
                    Scene scene = new Scene(fxmlLoader.load(), 900, 669); // to change the window size
                    stage.close(); /* close the stage */
                    // set the stage
                    if (styleOneTime == 0) {
                        stage.initStyle(StageStyle.UNDECORATED);
                        styleOneTime = 1;
                    }
                    stage.setScene(scene);
                    stage.setResizable(false);
                    stage.setMaximized(false);
                    stage.show();
                    vaildationLabel.setText("");
                } else {
                    vaildationLabel.setText("Error check username or password");
                    vaildationLabel.setTextFill(Paint.valueOf("#CF2828"));
                }
            } catch (Exception e) {
                vaildationLabel.setText("Error check username or password");
                vaildationLabel.setTextFill(Paint.valueOf("#CF2828"));
            }
        } else { /* login to database manager */
            try {
                Connection connection = DriverManager.getConnection(dbUrl);
                Statement statement = connection.createStatement();
                ResultSet resulttemp = statement.executeQuery(query2 + "'" + pass + "';"); // to get ID we sent the
                // index
                String result = resulttemp.getString("Username");
                connection.close(); /* close the connection */
                if (result.matches(username)) {
                    // clear fields
                    passwordField.clear();
                    usernameField.clear();
                    currentUser = result; // to pass current user for the next stage
                    FXMLLoader fxmlLoader = new FXMLLoader(DatabaseController.class.getResource("Database_page.fxml")); // to open gui for database manager
                    Scene scene = new Scene(fxmlLoader.load(), 609, 622); /* to change the window size */
                    stage.close(); /* close the stage */
                    if (styleOneTime == 0) {
                        stage.initStyle(StageStyle.UNDECORATED);
                        styleOneTime = 1;
                    }
                    stage.setScene(scene);
                    stage.setResizable(false);
                    stage.setMaximized(false);
                    stage.show();
                    vaildationLabel.setText("");
                } else {
                    vaildationLabel.setText("Error check username or password");
                    vaildationLabel.setTextFill(Paint.valueOf("#CF2828"));
                }
            } catch (Exception e) {
                vaildationLabel.setText("Error check username or password");
                vaildationLabel.setTextFill(Paint.valueOf("#CF2828"));
            }

        }
    }
}
