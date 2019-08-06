/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.myapplication;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String BLE_DEVICE_READ = "00002a00-0000-1000-8000-00805f9b34fb";
    public static String BLE_DEVICE_WRITE = "00002a01-0000-1000-8000-00805f9b34fb";
    public static String BLE_DEVICE_BROADCAST = "00002a04-0000-1000-8000-00805f9b34fb";
    public static String BLE_DEVICE_OTHER = "00002aa6-0000-1000-8000-00805f9b34fb";

    static {
        // Sample Services.
        attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        // Sample Characteristics.
        attributes.put(BLE_DEVICE_READ, "讀資料");
        attributes.put(BLE_DEVICE_WRITE, "寫資料");
        attributes.put(BLE_DEVICE_BROADCAST, "廣播");
        attributes.put(BLE_DEVICE_OTHER, "其他");

    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
