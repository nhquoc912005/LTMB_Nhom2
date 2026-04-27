package com.project_mobile.network;

/**
 * Network configuration used by ApiClient.
 *
 * Default uses 10.0.2.2 which points to host machine from Android emulator.
 * Change this value for device testing or production (or replace with
 * BuildConfig fields).
 */
public class NetworkConfig {
    /**
     * LƯU Ý QUAN TRỌNG:
     * 1. Nếu dùng EMULATOR: Thay IP bên dưới bằng 10.0.2.2
     * 2. Nếu dùng MÁY THẬT (USB/Wifi):
     * - Thay IP bằng 192.168.1.122 (địa chỉ IPv4 của bạn)
     * - Đảm bảo điện thoại và máy tính kết nối CÙNG một mạng Wifi.
     * - Kiểm tra Firewall trên Windows (nên tắt tạm hoặc thêm rule cho port 3000).
     * 3. Đảm bảo server Node.js đang chạy (npm start).
     */
    // Use 10.0.2.2 when running on the Android Emulator.
    // For a physical phone, replace this with your computer's LAN IPv4 address.
    public static final String BASE_URL = "http://10.0.2.2:3000/";

}
