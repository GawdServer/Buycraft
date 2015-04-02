package net.buycraft.util;

import tk.coolv1994.gawdserver.utils.ColorCodes;

public class Chat {
    private static final String header = ColorCodes.WHITE + "|----------------------"+ColorCodes.LIGHT_PURPLE+" BUYCRAFT "+ColorCodes.WHITE+"---------------------";
    private static final String footer = ColorCodes.WHITE + "|----------------------------------------------------";
    private static final String seperator = ColorCodes.WHITE + "| ";

    private Chat() {}

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
