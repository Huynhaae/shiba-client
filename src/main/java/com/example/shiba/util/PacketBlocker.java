package com.example.shiba.util;

public class PacketBlocker {
    private static boolean ignoreLookPackets = false;

    public static void setIgnoreLookPackets(boolean value) {
        ignoreLookPackets = value;
    }

    public static boolean shouldIgnoreLookPackets() {
        return ignoreLookPackets;
    }
}
