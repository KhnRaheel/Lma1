package com.lma.model;

public class Device extends Super {
    String id;
    String name;
    String modelNumber;
    String IMEI;
    String phone;

    public Device() {
    }

    public Device(String id, String name, String modelNumber, String IMEI, String phone) {
        this.id = id;
        this.name = name;
        this.modelNumber = modelNumber;
        this.IMEI = IMEI;
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModelNumber() {
        return modelNumber;
    }

    public void setModelNumber(String modelNumber) {
        this.modelNumber = modelNumber;
    }

    public String getIMEI() {
        return IMEI;
    }

    public void setIMEI(String IMEI) {
        this.IMEI = IMEI;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
