package com.lma.singletons;

import com.lma.model.Device;

public class DeviceSingleton {
    public static Device instance;

    private DeviceSingleton() {
    }

    public static Device getInstance() {
        return instance;
    }

    public static void setInstance(Device instance) {
        DeviceSingleton.instance = instance;
    }
}
