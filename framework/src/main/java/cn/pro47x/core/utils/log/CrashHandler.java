package cn.pro47x.core.utils.log;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import cn.pro47x.core.utils.DataUtils;

import java.io.*;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;


/**
 * 替换掉系统默认的线程未捕获异常处理器
 * 在友盟初始化前设置,就不会被友盟覆盖,反之则友盟可能被覆盖
 *
 * @author Jacky Yu
 */
public class CrashHandler implements UncaughtExceptionHandler {
    private static final String TAG = "CrashHandler";
    private static final boolean DEBUG = true;
    private static final String PATH = DataUtils.getSDCardAppPath() + "/crashLog";
    private static final String FILE_NAME = "crash-";
    // log文件的后缀名
    private static final String FILE_NAME_SUFFIX = ".trace";
    // 系统默认的异常处理（默认情况下，系统会终止当前的异常程序）
    private UncaughtExceptionHandler mDefaultCrashHandler;
    private Context mContext;

    // 构造方法私有，防止外部构造多个实例，即采用单例模式
    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        return CrashHandlerInstance.instance;
    }

    private static class CrashHandlerInstance {
        static CrashHandler instance = new CrashHandler();
    }

    // 这里主要完成初始化工作
    public void init(Context context) {
        // 获取系统默认的异常处理器
        mDefaultCrashHandler = Thread.getDefaultUncaughtExceptionHandler();
        // 将当前实例设为系统默认的异常处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
        // 获取Context，方便内部使用
        mContext = context.getApplicationContext();
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        //出现崩溃的时候清理首页缓存,防止下次读取的是奔溃的数据
        Logger.d("uncaughtExceptionTAG", e.getMessage());
        SPUtil.remove("com.alfl.www.main.model.HomeLoveSModel_home_data_cache");
//        mDefaultCrashHandler.uncaughtException(t, e);
        uncaughtEx(t, e);
    }

    /**
     * 这个是最关键的函数，当程序中有未被捕获的异常，系统将会自动调用#uncaughtException方法
     * thread为出现未捕获异常的线程，ex为未捕获的异常，有了这个ex，我们就可以得到异常信息。
     */


    private void uncaughtEx(Thread thread, final Throwable ex) {
        SPUtil.setValue("crash_exception", ex.getClass().getName());
        try {
            dumpExceptionToSDCard(ex);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (AlaConfig.isDebug()) {
            // 打印出当前调用栈信息
//            ex.printStackTrace();
            new Thread(() -> {
                final StringWriter stringWriter = new StringWriter();
                Looper.prepare();
                ClipboardManager clipboardManager = (ClipboardManager) ActivityUtils.peek()
                        .getSystemService(Context.CLIPBOARD_SERVICE);
                clipboardManager.setText(stringWriter.toString());
                ex.printStackTrace(new PrintWriter(stringWriter));
                AlertDialog dialog = new AlertDialog.Builder(ActivityUtils.peek()).setTitle("提示")
                        .setCancelable(false).setMessage("很抱歉，当前程序出现异常：\n" + stringWriter.toString())
                        .setNeutralButton("关闭", (dialog1, which) -> {
                            ActivityUtils.onExit(true);
                        }).setPositiveButton("复制异常信息", (dialog12, which) -> {
                            ClipboardManager clipboardManager1 = (ClipboardManager) ActivityUtils.peek()
                                    .getSystemService(Context.CLIPBOARD_SERVICE);
                            clipboardManager1.setText(stringWriter.toString());
                            ActivityUtils.onExit(true);
                        }).create();
                dialog.show();
                Looper.loop();
            }).start();
        } else {
            mDefaultCrashHandler.uncaughtException(thread, ex);
            ActivityUtils.onExit(true);
        }
    }

    private void dumpExceptionToSDCard(Throwable ex) throws IOException {
        Context context = AlaConfig.getContext();
        // 如果没有读写SD卡的权限，则不写入文件
        if (null != context && !PermissionCheck.getInstance()
                .checkPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            return;
        }

        // 如果SD卡不存在或无法使用，则无法把异常信息写入SD卡
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            if (DEBUG) {
                Logger.w(TAG, "sdcard unmounted,skip dump exception");
                return;
            }
        }

        File dir = new File(PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        long current = System.currentTimeMillis();
        String time = new SimpleDateFormat(DateFormatter.SS.getValue()).format(new Date(current));
        // 以当前时间创建log文件
        //文件名格式 a_13位时间戳_10位随机数.txt
        String fileName = "a_" + System.currentTimeMillis() + "_" + Integer.toString(new Random().nextInt(1000000000)) + Integer.toString(new Random().nextInt(1));
        File file = new File(PATH + File.separator + fileName);
        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            // 导出发生异常的时间
            pw.println(time);

            // 导出手机信息
            dumpPhoneInfo(pw);
            collectDeviceInfo(pw);

            pw.println();
            // 导出异常的调用栈信息
            ex.printStackTrace(pw);

            pw.close();
        } catch (Exception e) {
            Logger.e(TAG, "dump crash info failed");
        }
        // 这里可以通过网络上传异常信息到服务器，便于开发人员分析日志从而解决bug
//        uploadExceptionToServer(file);
    }

    private void dumpPhoneInfo(PrintWriter pw) throws NameNotFoundException {
        // 应用的版本名称和版本号
        PackageManager pm = mContext.getPackageManager();
        PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
        // 应用版本
        pw.print("App Version Name: ");
        pw.println(pi.versionName);
        // 应用版本号
        pw.print("App Version Code: ");
        pw.println(pi.versionCode);

        // android版本
        pw.print("Android OS Version Name: ");
        pw.println(Build.VERSION.RELEASE);
        // android版本号
        pw.print("Android OS Version Code: ");
        pw.println(Build.VERSION.SDK_INT);

        // 手机制造商
        pw.print("Vendor: ");
        pw.println(Build.MANUFACTURER);

        // 手机型号
        pw.print("Model: ");
        pw.println(Build.MODEL);
    }

    /**
     * 收集设备参数信息
     */
    private void collectDeviceInfo(PrintWriter pw) {
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                pw.print(field.getName() + ": ");
                pw.println(field.get(null).toString());
                Logger.d(TAG, field.getName() + " : " + field.get(null));
            } catch (Exception e) {
                Logger.e(TAG, "an error occurred when collect crash info", e);
            }
        }
    }

}
