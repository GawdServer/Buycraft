package net.buycraft.util;

import io.github.gawdserver.api.player.Sender;
import io.github.gawdserver.api.utils.ColorCodes;

public class ChatUtils {
    private static final String header = ColorCodes.WHITE + "|----------------------"+ColorCodes.LIGHT_PURPLE+" BUYCRAFT "+ColorCodes.WHITE+"---------------------";
    private static final String footer = ColorCodes.WHITE + "|----------------------------------------------------";
    private static final String seperator = ColorCodes.WHITE + "| ";

    public static String header() {
        return header;
    }

    public static String footer() {
        return footer;
    }

    public static String seperator() {
        return seperator;
    }
}
