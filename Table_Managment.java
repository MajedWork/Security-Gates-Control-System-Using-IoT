package com.example.mqtt_tag;

import javafx.concurrent.Task;

public class Table_Managment extends Task<Staff> {
    // Variables
    protected static int addNewStaff = 0; /* to unlock and lock the adding to tableview process */
    protected static String name = "";
    protected static String id = "";
    protected static String date = "";

    @Override
    protected Staff call() throws Exception {
        while (true) {
            if (addNewStaff == 1) {
                updateValue(new Staff(name, id, date));
                addNewStaff = 0; /* unlock the process */
            }
            Thread.sleep(10); /* make thread sleep */
        }
    }
}
