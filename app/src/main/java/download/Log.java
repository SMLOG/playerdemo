package download;

public class Log {

    private static volatile int level = Constant.INFO;

    public static void i(CharSequence message) {
        System.out.println( message);

    }

    public static void d(CharSequence message) {
            System.out.println( message);

    }

    public static void e(CharSequence message) {
        System.out.println( message);

    }

    public static void setLevel(int level) {
        if (level != Constant.NONE && level != Constant.INFO && level != Constant.DEBUG && level != Constant.ERROR)
            throw new IllegalArgumentException("日志参数信息设置错误！");
        Log.level = level;
    }

    public static int getLevel() {
        return level;
    }
}
