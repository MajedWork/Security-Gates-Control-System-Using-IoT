package com.example.mqtt_tag;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Paint;

import java.sql.*;

import static com.example.mqtt_tag.LoginController.currentUser; /*to pass the login user from login page*/

public class DatabaseController {

    // TextField elements

    @FXML
    private PasswordField add2_password;

    @FXML
    private TextField add2_username;

    @FXML
    private TextField add_id;

    @FXML
    private TextField add_index;

    @FXML
    private TextField add_name;

    @FXML
    private TextField update1_id;

    @FXML
    private TextField update1_index;

    @FXML
    private TextField update1_name;

    @FXML
    private PasswordField update2_password;

    @FXML
    private TextField update2_username;

    //Label elements
    @FXML
    private Label vaildationLabel1;

    @FXML
    private Label vaildationLabel2;

    @FXML
    private Label vaildationLabel3;

    @FXML
    private Label vaildationLabel4;

    //Radio button elements
    @FXML
    private RadioButton update2_securityRaido;
    @FXML
    private RadioButton update2_dbRaido;
    @FXML
    private RadioButton add_securityRaido;
    @FXML
    private RadioButton add_dbRaido;

    //Variables
    final private String dbUrl = "jdbc:sqlite:src\\main\\resources\\com\\example\\mqtt_tag\\Database\\MQTTDatabase.db";
    private int choice1 = 0;
    private int choice2 = 0;



    public void closeSystem() {
        System.exit(0);
    }
    //these functions to control the selection of raido buttons
    public  void setChoice1_1(){
        add_securityRaido.setSelected(true);
        add_dbRaido.setSelected(false);
        choice1 = 0;
    }

    public  void setChoice1_2(){
        add_securityRaido.setSelected(false);
        add_dbRaido.setSelected(true);
        choice1 = 1;
    }

    public  void setChoice2_1(){
        update2_securityRaido.setSelected(true);
        update2_dbRaido.setSelected(false);
        choice2 = 0;
    }

    public  void setChoice2_2(){
        update2_securityRaido.setSelected(false);
        update2_dbRaido.setSelected(true);
        choice2 = 1;
    }


    //to add new Staff
    public void addStaff() throws SQLException {
        String name = add_name.getText();
        String id = add_id.getText();
        String index = add_index.getText();

        //Query statement
        String query = "INSERT INTO Staff (Indices,Name,ID) VALUES (" + "'" + index + "'" + "," + "'" + name + "' ," + "'" + id + "');";
        if (index.isBlank() || id.isBlank() || name.isBlank()) {
            vaildationLabel1.setText("Error try again");
            vaildationLabel1.setTextFill(Paint.valueOf("#CF2828"));
        } else {
            Connection connection = DriverManager.getConnection(dbUrl);

            //adding operation
            try {
                Statement statement = connection.createStatement();
                statement.executeUpdate(query);
                connection.close(); /*close the connection*/
                vaildationLabel1.setText("New staff added successfully!");
                vaildationLabel1.setTextFill(Paint.valueOf("#0CA380"));
                add_name.clear();
                add_id.clear();
                add_index.clear();
            } catch (Exception e) {
                vaildationLabel1.setText("Error try again");
                vaildationLabel1.setTextFill(Paint.valueOf("#CF2828"));
                connection.close(); /*close the connection*/
            }
        }
    }

    //to update staff
    public void updateStaff() throws SQLException {
        String id = update1_id.getText();
        String name = update1_name.getText();
        String index = update1_index.getText();

        //Query statement
        String query = "UPDATE Staff SET Name = '"+name+"', Indices = '"+index+"' WHERE ID ='"+id+"';";
        if (index.isBlank() || name.isBlank()||id.isBlank()) {
            vaildationLabel3.setText("Error try again");
            vaildationLabel3.setTextFill(Paint.valueOf("#CF2828"));
        } else {
            Connection connection = DriverManager.getConnection(dbUrl);
            //adding operation
            try {

                Statement statement = connection.createStatement();
                int result = statement.executeUpdate(query);
                connection.close(); /*close the connection*/
                if(result !=0){
                    vaildationLabel3.setText("Staff updated successfully!");
                    vaildationLabel3.setTextFill(Paint.valueOf("#0CA380"));
                    update1_id.clear();
                    update1_name.clear();
                    update1_index.clear();
                }else{
                    vaildationLabel3.setText("Error try again");
                    vaildationLabel3.setTextFill(Paint.valueOf("#CF2828"));
                }

            } catch (Exception e) {
                vaildationLabel3.setText("Error try again");
                vaildationLabel3.setTextFill(Paint.valueOf("#CF2828"));
                connection.close(); /*close the connection*/
            }
        }
    }

    //to remove staff
    public void removeStaff() throws SQLException {
        String id = update1_id.getText();

        //Query statement
        String query = "DELETE FROM Staff  WHERE ID ='"+id+"';";
        if (id.isBlank()) {
            vaildationLabel3.setText("Error try again");
            vaildationLabel3.setTextFill(Paint.valueOf("#CF2828"));
        } else {
            Connection connection = DriverManager.getConnection(dbUrl);
            //adding operation
            try {

                Statement statement = connection.createStatement();
                int result = statement.executeUpdate(query);
                connection.close(); /*close the connection*/
                if(result !=0){
                    vaildationLabel3.setText("Staff removed successfully!");
                    vaildationLabel3.setTextFill(Paint.valueOf("#0CA380"));
                    update1_id.clear();
                }else{
                    vaildationLabel3.setText("Error try again");
                    vaildationLabel3.setTextFill(Paint.valueOf("#CF2828"));
                }

            } catch (Exception e) {
                vaildationLabel3.setText("Error try again");
                vaildationLabel3.setTextFill(Paint.valueOf("#CF2828"));
                connection.close(); /*close the connection*/
            }
        }
    }

    //to add security staff or database manager
    public void addSecurity_database() throws SQLException {
        String username = add2_username.getText();
        String password = add2_password.getText();
        if (choice1 == 0) {/* add security staff*/
            //Query statement
            String query = "INSERT INTO" + "'Security Staff'" + "(Username,Password) Values(" + "'" + username + "'" + "," + "'" + password + "');";
            if (password.isBlank() || username.isBlank()) {
                vaildationLabel2.setText("Error try again");
                vaildationLabel2.setTextFill(Paint.valueOf("#CF2828"));
            } else {
                Connection connection = DriverManager.getConnection(dbUrl);

                //adding operation
                try {
                    Statement statement = connection.createStatement();
                    statement.executeUpdate(query);
                    connection.close(); /*close the connection*/
                    vaildationLabel2.setText("New security staff added successfully!");
                    vaildationLabel2.setTextFill(Paint.valueOf("#0CA380"));
                    add2_password.clear();
                    add2_username.clear();
                } catch (Exception e) {
                    vaildationLabel2.setText("Error try again");
                    vaildationLabel2.setTextFill(Paint.valueOf("#CF2828"));
                    connection.close(); /*close the connection*/
                }
            }
        }else{ /* add database manager*/
            String query = "INSERT INTO" + "'Database Managers'" + "(Username,Password) Values(" + "'" + username + "'" + "," + "'" + password + "');";
            if (password.isBlank() || username.isBlank()) {
                vaildationLabel2.setText("Error try again");
                vaildationLabel2.setTextFill(Paint.valueOf("#CF2828"));
            } else {
                Connection connection = DriverManager.getConnection(dbUrl);
                //adding operation
                try {
                    Statement statement = connection.createStatement();
                    statement.executeUpdate(query);
                    connection.close(); /*close the connection*/
                    vaildationLabel2.setText("New database manager added successfully!");
                    vaildationLabel2.setTextFill(Paint.valueOf("#0CA380"));
                    add2_password.clear();
                    add2_username.clear();
                } catch (Exception e) {
                    vaildationLabel2.setText("Error try again");
                    vaildationLabel2.setTextFill(Paint.valueOf("#CF2828"));
                    connection.close(); /*close the connection*/
                }
            }

        }
    }

    //to update security staff or database manager
    public void updateSecurity_database() throws SQLException {
        String password = update2_password.getText();
        String username = update2_username.getText();
        if (choice2 == 0) { /*update security staff*/
            //Query statement
            String query = "UPDATE " + "'Security Staff' SET Password = '" + password + "' WHERE Username ='" + username + "';";
            if (password.isBlank() || username.isBlank()) {
                vaildationLabel4.setText("Error try again");
                vaildationLabel4.setTextFill(Paint.valueOf("#CF2828"));
            } else {
                Connection connection = DriverManager.getConnection(dbUrl);

                //adding operation
                try {
                    Statement statement = connection.createStatement();
                    int result = statement.executeUpdate(query);
                    connection.close(); /*close the connection*/
                    if (result != 0) {
                        vaildationLabel4.setText("Security staff updated successfully!");
                        vaildationLabel4.setTextFill(Paint.valueOf("#0CA380"));
                        update2_password.clear();
                        update2_username.clear();
                    } else {
                        vaildationLabel4.setText("Error try again");
                        vaildationLabel4.setTextFill(Paint.valueOf("#CF2828"));
                    }

                } catch (Exception e) {
                    vaildationLabel4.setText("Error try again");
                    vaildationLabel4.setTextFill(Paint.valueOf("#CF2828"));
                    connection.close(); /*close the connection*/

                }
            }
        }else{/*update database manager*/
            //Query statement
            String query = "UPDATE " + "'Database Managers' SET Password = '" + password + "' WHERE Username ='" + username + "';";
            if (password.isBlank() || username.isBlank()) {
                vaildationLabel4.setText("Error try again");
                vaildationLabel4.setTextFill(Paint.valueOf("#CF2828"));
            } else {
                Connection connection = DriverManager.getConnection(dbUrl);

                //adding operation
                try {
                    Statement statement = connection.createStatement();
                    int result = statement.executeUpdate(query);
                    connection.close(); /*close the connection*/
                    if (result != 0) {
                        vaildationLabel4.setText("Database manager updated successfully!");
                        vaildationLabel4.setTextFill(Paint.valueOf("#0CA380"));
                        update2_password.clear();
                        update2_username.clear();
                    } else {
                        vaildationLabel4.setText("Error try again");
                        vaildationLabel4.setTextFill(Paint.valueOf("#CF2828"));
                    }

                } catch (Exception e) {
                    vaildationLabel4.setText("Error try again");
                    vaildationLabel4.setTextFill(Paint.valueOf("#CF2828"));
                    connection.close(); /*close the connection*/
                }
            }
        }
    }

    //to remove security staff or database manager
    public void removeSecurity_database() throws SQLException {
        String username = update2_username.getText();
        if (choice2 == 0) {
            //Query statement
            String query = "DELETE FROM " + "'Security Staff'  WHERE Username ='" + username + "';";
            if (username.isBlank()) {
                vaildationLabel4.setText("Error try again");
                vaildationLabel4.setTextFill(Paint.valueOf("#CF2828"));
            } else {
                Connection connection = DriverManager.getConnection(dbUrl);
                //adding operation
                try {
                    Statement statement = connection.createStatement();
                    int result = statement.executeUpdate(query);
                    connection.close(); /*close the connection*/
                    if (result != 0) {
                        vaildationLabel4.setText("Security Staff removed successfully!");
                        vaildationLabel4.setTextFill(Paint.valueOf("#0CA380"));
                        update2_username.clear();
                    } else {
                        vaildationLabel4.setText("Error try again");
                        vaildationLabel4.setTextFill(Paint.valueOf("#CF2828"));
                    }

                } catch (Exception e) {
                    vaildationLabel4.setText("Error try again");
                    vaildationLabel4.setTextFill(Paint.valueOf("#CF2828"));
                    connection.close(); /*close the connection*/
                }
            }
        }else{
            //Query statement
            String query = "DELETE FROM " + "'Database Managers'  WHERE Username ='" + username + "';";
            if (username.isBlank()||username.matches(currentUser)) {
                vaildationLabel4.setText("Error try again");
                vaildationLabel4.setTextFill(Paint.valueOf("#CF2828"));
            } else {
                Connection connection = DriverManager.getConnection(dbUrl);

                //adding operation
                try {
                    Statement statement = connection.createStatement();
                    int result = statement.executeUpdate(query);
                    connection.close(); /*close the connection*/
                    if (result != 0) {
                        vaildationLabel4.setText("Database manager removed successfully!");
                        vaildationLabel4.setTextFill(Paint.valueOf("#0CA380"));
                        update2_username.clear();
                    } else {
                        vaildationLabel4.setText("Error try again");
                        vaildationLabel4.setTextFill(Paint.valueOf("#CF2828"));
                    }

                } catch (Exception e) {
                    vaildationLabel4.setText("Error try again");
                    vaildationLabel4.setTextFill(Paint.valueOf("#CF2828"));
                    connection.close(); /*close the connection*/
                }
            }
        }
    }
}