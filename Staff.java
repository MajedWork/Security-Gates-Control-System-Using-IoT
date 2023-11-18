package com.example.mqtt_tag;

//class for table to struct the data to it
public class Staff {
    private String Name;
    private String ID;
    private String Date;

    public Staff(String Name, String ID, String Date) {
        this.Name = Name;
        this.ID = ID;
        this.Date = Date;
    }

    public String getName() {
        return Name;
    }

    public String getID() {
        return ID;
    }

    public String getDate() {
        return Date;
    }
}
