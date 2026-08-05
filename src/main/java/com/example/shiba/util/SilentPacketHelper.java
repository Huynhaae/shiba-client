package com.example.shiba.util;

public class SilentPacketHelper {
    private static boolean isSilentPacket = false;

    public static void setSilentPacket(boolean value) {
        isSilentPacket = value;
    }

    public static boolean isSilentPacket() {
        return isSilentPacket;
    }
}
