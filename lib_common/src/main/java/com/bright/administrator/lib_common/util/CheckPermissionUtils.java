package com.bright.administrator.lib_common.util;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.location.LocationManager;

public class CheckPermissionUtils {
    public static boolean checkGPSIsOpen(Context context) { //检查GPS是否打开
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            return false;
        }
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
    }
    public static boolean checkBlueToothIsOpen() { //检查蓝牙是否打开
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            return false;
        }
        return bluetoothAdapter.isEnabled();
    }
}
