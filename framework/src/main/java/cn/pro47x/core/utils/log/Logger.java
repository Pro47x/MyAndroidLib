package cn.pro47x.core.utils.log;

import android.Manifest;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;
import cn.pro47x.core.BuildConfig;
import cn.pro47x.core.config.AppConfig;
import cn.pro47x.core.utils.DataUtils;
import cn.pro47x.core.utils.PermissionCheck;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 版权：XXX公司 版权所有
 * 作者：Jacky Yu
 * 版本：1.0
 * 创建日期：2017/1/3
 * 描述：日志打印类
 * 修订历史：
 */
public final class Logger {
    private static final String TAG = "Logger";
    private static final String SAVE_PATH;
    protected static final String LOG_PREFIX;
    protected static final String LOG_DIR;
    // Log switch open - development; close - released
    public static boolean debug = BuildConfig.DEBUG;
    // Write file level
    public static int     level = Log.ERROR;

    static {
        // SAVE_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
        SAVE_PATH = DataUtils.getSDCardAppPath();
        LOG_PREFIX = setLogPrefix("");
        LOG_DIR = setLogPath("");
    }

    public static void v(String tag, String msg) {
        trace(Log.VERBOSE, tag, msg);
    }

    public static void v(String tag, String msg, Throwable tr) {
        trace(Log.VERBOSE, tag, msg, tr);
    }

    public static void d(String tag, String msg) {
        trace(Log.DEBUG, tag, msg);
    }

    public static void d(String tag, String msg, Throwable tr) {
        trace(Log.DEBUG, tag, msg, tr);
    }

    public static void i(String tag, String msg) {
        trace(Log.INFO, tag, msg);
    }

    public static void i(String tag, String msg, Throwable tr) {
        trace(Log.INFO, tag, msg, tr);
    }

    public static void w(String tag, String msg) {
        trace(Log.WARN, tag, msg);
    }

    public static void w(String tag, String msg, Throwable tr) {
        trace(Log.WARN, tag, msg, tr);
    }

    public static void e(String tag, String msg) {
        trace(Log.ERROR, tag, msg);
    }

    public static void e(String tag, String msg, Throwable tr) {
        trace(Log.ERROR, tag, msg, tr);
    }

    private static void trace(final int type, String tag, final String msg) {
        trace(type, tag, msg, null);
    }

    /**
     * Custom Log output style
     *
     * @param type
     *         Log type
     * @param tag
     *         TAG
     * @param msg
     *         Log message
     */
    private static void trace(final int type, final String tag, final String msg, final Throwable tr) {
        // LogCat
        if (debug) {
            switch (type) {
                case Log.VERBOSE:
                    Log.v(tag, msg);
                    break;
                case Log.DEBUG:
                    Log.d(tag, msg);
                    break;
                case Log.INFO:
                    Log.i(tag, msg);
                    break;
                case Log.WARN:
                    Log.w(tag, msg);
                    break;
                case Log.ERROR:
                    Log.e(tag, msg);
                    break;
            }
        }
        // Write to file
        if (type >= level) {
            writeLog(type, tr == null ? msg : msg + '\n' + Log.getStackTraceString(tr));
        }
    }

    /**
     * Write log file to the SDCard
     */
    private static void writeLog(int type, String msg) {
        Context context = AppConfig.getContext();
        // 如果没有读写SD卡的权限，则不写入文件
        if (null != context && !PermissionCheck.getInstance().checkPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            return;
        }

        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return;
        }

        try {
            SparseArray<String> logMap = new SparseArray<>();
            logMap.put(Log.VERBOSE, " VERBOSE ");
            logMap.put(Log.DEBUG, " DEBUG ");
            logMap.put(Log.INFO, " INFO ");
            logMap.put(Log.WARN, " WARN ");
            logMap.put(Log.ERROR, " ERROR ");

            final StackTraceElement tag = new Throwable().fillInStackTrace().getStackTrace()[2];

            msg = new StringBuilder()
                    .append("\r\n")
                    .append(getDateFormat(DateFormatter.SS.getValue()))
                    .append(logMap.get(type)).append(tag.getClassName())
                    .append(" - ").append(tag.getMethodName()).append("(): ")
                    .append(msg).toString();

            final String fileName = new StringBuffer()
                    .append(LOG_PREFIX)
                    .append(getDateFormat(DateFormatter.DD.getValue()))
                    .append(".log").toString();

            recordLog(LOG_DIR, fileName, msg, true);
        } catch (Exception e) {
            Logger.e("Logger: ", e.getMessage());
        }
    }

    /**
     * Write log
     *
     * @param logDir
     *         Log path to save
     * @param fileName
     *         Log file name
     * @param msg
     *         Log content
     * @param append
     *         Save as type, false override save, true before file add save
     */
    private static void recordLog(String logDir, String fileName, String msg, boolean append) {
        try {
            createDir(logDir);

            final File saveFile = new File(new StringBuffer()
                    .append(logDir)
                    .append("/")
                    .append(fileName).toString());

            if (!append && saveFile.exists()) {
                saveFile.delete();
                saveFile.createNewFile();
                write(saveFile, msg, append);
            } else if (append && saveFile.exists()) {
                write(saveFile, msg, append);
            } else if (append && !saveFile.exists()) {
                saveFile.createNewFile();
                write(saveFile, msg, append);
            } else if (!append && !saveFile.exists()) {
                saveFile.createNewFile();
                write(saveFile, msg, append);
            }
        } catch (IOException e) {
            recordLog(logDir, fileName, msg, append);
        }
    }

    private static String getDateFormat(String pattern) {
        final DateFormat format = new SimpleDateFormat(pattern);
        return format.format(new Date());
    }

    private static File createDir(String dir) {
        final File file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    /**
     * Write msg to file
     */
    private static void write(final File file, final String msg, final boolean append) {
        new SafeAsyncTask<Void>() {
            @Override
            public Void call() throws Exception {
                final FileOutputStream fos;
                try {
                    fos = new FileOutputStream(file, append);
                    try {
                        fos.write(msg.getBytes());
                    } catch (IOException e) {
                        Logger.e(TAG, "write fail!!!", e);
                    } finally {
                        if (fos != null) {
                            try {
                                fos.close();
                            } catch (IOException e) {
                                Logger.d(TAG, "Exception closing stream: ", e);
                            }
                        }
                    }
                } catch (FileNotFoundException e) {
                    Logger.e(TAG, "write fail!!!", e);
                }
                return null;
            }
        }.execute();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Logger config methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 设置日志文件名前缀
     *
     * @param prefix
     *         (prefix-20121212.log)
     */
    public static String setLogPrefix(final String prefix) {
        return prefix.length() == 0 ? "logger-" : prefix + "-";
    }

    /**
     * 设置日志文件存放路径
     *
     * @param subPath
     *         子路径("/Downloads/subPath")
     */
    public static String setLogPath(final String subPath) {
        return subPath.length() == 0 ? SAVE_PATH + "/logs" : subPath;
    }
}
