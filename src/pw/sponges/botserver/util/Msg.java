package pw.sponges.botserver.util;

public class Msg {

    private static final boolean DEBUG = true;

    public static void log(String msg) {
        System.out.printf("info> %s\n", msg);
    }

    public static void debug(String msg) {
        if (!DEBUG) return;
        System.out.printf("debug> %s\n", msg);
    }

    public static void warning(String msg) {
        System.out.printf("warning> %s\n", msg);
    }

}