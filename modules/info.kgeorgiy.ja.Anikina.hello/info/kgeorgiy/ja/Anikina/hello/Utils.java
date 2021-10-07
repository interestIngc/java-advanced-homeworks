package info.kgeorgiy.ja.Anikina.hello;

import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;

public final class Utils {

    public static String makeRequest(String prefix, int thread, int number) {
        return prefix + thread + "_" + number;
    }

    public static String getStringFromPacket(DatagramPacket packet) {
        return new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);
    }


}
