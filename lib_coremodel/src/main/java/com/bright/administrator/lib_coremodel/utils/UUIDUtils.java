package com.bright.administrator.lib_coremodel.utils;

import java.util.UUID;

public class UUIDUtils {
    public static final UUID UUID_LOST_SERVICE = UUID.fromString("000FF12-0000-1000-8000-00805f9b34fb"); //Service UUID
    public static final UUID WRITE_CHARACTERISTIC_UUID = UUID.fromString("0000FF01-0000-1000-8000-00805f9b34fb");//写特征
    public static final UUID READ_CHARACTERISTIC_UUID = UUID.fromString("0000FF02-0000-1000-8000-00805f9b34fb");//Notify UUID
    public static final UUID CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
}
