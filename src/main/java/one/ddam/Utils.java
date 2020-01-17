package one.ddam;

import java.util.regex.Pattern;

public class Utils {
    public static boolean isAddress(String address) {
        return Pattern.matches("DD[a-fA-F0-9]{64}", address);
    }
}
