package com.project_mobile.network;

/**
 * Network configuration used by ApiClient.
 *
 * Default uses 10.0.2.2 which points to host machine from Android emulator.
 * Change this value for device testing or production (or replace with BuildConfig fields).
 */
public class NetworkConfig {
    // Default for Android emulator: 10.0.2.2 -> host localhost
    public static final String BASE_URL = "http://10.0.2.2:3000/";
}

