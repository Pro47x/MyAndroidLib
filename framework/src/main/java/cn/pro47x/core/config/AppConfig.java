package cn.pro47x.core.config;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import cn.pro47x.core.receiver.GestureLockWatcher;
import cn.pro47x.core.utils.DataUtils;
import cn.pro47x.core.utils.InfoUtils;
import cn.pro47x.core.utils.MiscUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.concurrent.*;

/**
 * 此类必须在程序刚刚启动的时候注册，这样在程序的
 * 运行中就可以通过此类来获取全局的Application了
 * 最好的实现是自定义一个Application的子类，然后在此子类
 * 的初始化中注册。
 *
 * @author lisiqi
 * @date 2018-12-28 11:12:06
 */
public class AppConfig {
    public static final String ACTION_API_OPEN = "com.framework.core.api.err.open";
    public static final String EXTRA_ERR_CODE = "__extra__api_err_code__";
    public static final String EXTRA_ERR_MSG = "__extra__api_err_msg__";
    private static final String ALA_CORE_SHARED_PREFERENCE_DATA = "_sp.fw.core.config_";
    public static final String ALA_MAIDIAN_INFO_FILE_NAME = "maidianInfo.txt";
    private static WeakReference<Activity> currentActivity;// 当前正在显示的Activity
    /**
     * 必须需要显式设置
     */
    private static Application application;
    /**
     * 系统全局的线程池，Application启动的时候创建，不需要销毁
     */
    private static ExecutorService es;
    /**
     * 主线程的handler，用于方便post一些事情做主线程去做
     */
    private static Handler handler;
    private static LocalBroadcastManager localBroadcastManager;
    /**
     * 阻塞队列
     */
    private static LinkedBlockingQueue<String> blockQueue;
    /**
     * 用于监听APP是否到后台
     */
    private static GestureLockWatcher watcher;

    private static boolean isShowTips = true;
    private static boolean isFirstEntrance = true;
    private static Thread maidianInfoThread;
    //    private static boolean isNewHome = false;//是否是新版首页

    public static void init(Application application) {
        localBroadcastManager = LocalBroadcastManager.getInstance(application);
        // 首先是生成线程池，最多10个线程,最少1个，闲置1分钟后线程退出
        es = Executors.newFixedThreadPool(10);
        AppConfig.application = application;
        // 调用此方法触发保存的动作
        getFirstLaunchTime();

        handler = new Handler(Looper.getMainLooper());
        blockQueue = new LinkedBlockingQueue<>();
        startTakeMaidianInfoThread();
    }

    /**
     * 开启全局的一个保存埋点信息的线程
     */
    public static void startTakeMaidianInfoThread() {
        if (maidianInfoThread != null) return;
        maidianInfoThread = new Thread(() -> {
            while (true) {
                try {
                    //take方法阻塞,有值就去保存
                    String maidianInfo = blockQueue.take();
                    File file = DataUtils.createIfNotExistsOnPhone(ALA_MAIDIAN_INFO_FILE_NAME);
                    DataUtils.saveToFileCanAppend(maidianInfo, file);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        maidianInfoThread.start();
    }

    public static void putMaidianInfo(String info) {
        try {
            blockQueue.put(info);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static Activity getCurrentActivity() {
        return currentActivity != null ? currentActivity.get() : null;
    }

    public static void setCurrentActivity(Activity activity) {
        currentActivity = new WeakReference<>(activity);
    }

    public static LocalBroadcastManager getLocalBroadcastManager() {
        return localBroadcastManager;
    }

    public static <T> Future<T> submit(Callable<T> call) {
        return es.submit(call);
    }

    public static void postOnUiThread(Runnable task) {
        handler.post(task);
    }

    public static void postDelayOnUiThread(Runnable task, long delay) {
        handler.postDelayed(task, delay);
    }

    public static void execute(Runnable task) {
        es.execute(task);
    }

    public static void removeCallbacks(Runnable task) {
        handler.removeCallbacks(task);
    }

    public static int addLaunchVersionCount() {
        SharedPreferences prefs = application.getSharedPreferences(ALA_CORE_SHARED_PREFERENCE_DATA, Application.MODE_PRIVATE);
        String key = "lc" + InfoUtils.getVersionCode();
        int count = prefs.getInt(key, 0) + 1;
        Editor editor = prefs.edit();
        editor.putInt(key, count);
        editor.commit();
        return count;
    }

    public static int getLaunchVersionCount() {
        SharedPreferences prefs = application.getSharedPreferences(ALA_CORE_SHARED_PREFERENCE_DATA, Application.MODE_PRIVATE);
        String key = "lc" + InfoUtils.getVersionCode();
        return prefs.getInt(key, 0);
    }

    public static int addLaunchCount() {
        SharedPreferences prefs = application.getSharedPreferences(ALA_CORE_SHARED_PREFERENCE_DATA, Application.MODE_PRIVATE);
        int count = prefs.getInt("lc", 0) + 1;
        Editor editor = prefs.edit();
        editor.putInt("lc", count);
        editor.apply();
        return count;
    }

    public static int getLaunchCount() {
        SharedPreferences prefs = application.getSharedPreferences(ALA_CORE_SHARED_PREFERENCE_DATA, Application.MODE_PRIVATE);
        return prefs.getInt("lc", 0);
    }

    public static long getLastAdTime() {
        SharedPreferences prefs = application.getSharedPreferences(ALA_CORE_SHARED_PREFERENCE_DATA, Application.MODE_PRIVATE);
        return prefs.getLong("lastATime", -1L);
    }

    public static void updateLastAdTime() {
        SharedPreferences prefs = application.getSharedPreferences(ALA_CORE_SHARED_PREFERENCE_DATA, Application.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putLong("lastATime", System.currentTimeMillis());
        editor.commit();
    }

    public static long getLastPauseTime() {
        SharedPreferences prefs = application.getSharedPreferences(ALA_CORE_SHARED_PREFERENCE_DATA, Application.MODE_PRIVATE);
        return prefs.getLong("lastPauseTime", -1L);
    }

    public static void updateLastPauseTime() {
        SharedPreferences prefs = application.getSharedPreferences(ALA_CORE_SHARED_PREFERENCE_DATA, Application.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putLong("lastPauseTime", System.currentTimeMillis());
        editor.commit();
    }

    /**
     * 获取第一次启动时间，此方法永远不会返回null
     * ，返回的格式是 yyyy-MM-dd HH:mm:ss
     */
    public static String getFirstLaunchTime() {
        SharedPreferences prefs = application.getSharedPreferences(ALA_CORE_SHARED_PREFERENCE_DATA, Application.MODE_PRIVATE);
        String firstTime = prefs.getString("ft", "");
        if (MiscUtils.isEmpty(firstTime)) {
            firstTime = MiscUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
            Editor editor = prefs.edit();
            editor.putString("ft", firstTime);
            editor.commit();
        }
        return firstTime;
    }

    public static boolean getTagAlias() {
        SharedPreferences prefs = application.getSharedPreferences(ALA_CORE_SHARED_PREFERENCE_DATA, Application.MODE_PRIVATE);
        boolean state = prefs.getBoolean("tagalias", false);
        return state;
    }

    /**
     * 设置推送的别名和Tag是否设置成功状态
     *
     * @param state
     */
    public static void setTagAlias(boolean state) {
        SharedPreferences prefs = application.getSharedPreferences(ALA_CORE_SHARED_PREFERENCE_DATA, Application.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putBoolean("tagalias", state);
        editor.commit();
    }

    public static String getPackageName() {
        Context context = getContext();
        if (context != null) {
            return context.getPackageName();
        }
        return null;
    }

    public static Application getContext() {
        return application;
    }

    public static Resources getResources() {
        return application.getResources();
    }

    public static GestureLockWatcher getWatcher() {
        return watcher;
    }

    public static void setIsFirstEntrance(boolean isFirstEntrance) {
        AppConfig.isFirstEntrance = isFirstEntrance;
    }

    public static boolean getIsFirstEntrance() {
        return isFirstEntrance;
    }

//    public static boolean isNewHome() {
//        return isNewHome;
//    }

//    public static void setIsNewHome(boolean isNewHome) {
//        AppConfig.isNewHome = isNewHome;
//    }

    //是否下载过借贷超人
    public static void setShowSuperLoan(int showDate) {
        SharedPreferences prefs = application.getSharedPreferences(ALA_CORE_SHARED_PREFERENCE_DATA, Application.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putInt("ShowSuperLoan", showDate);
        editor.commit();
    }

    public static int getShowSuperLoan() {
        SharedPreferences prefs = application.getSharedPreferences(ALA_CORE_SHARED_PREFERENCE_DATA, Application.MODE_PRIVATE);
        return prefs.getInt("ShowSuperLoan", 0);
    }


    //是否下载过借贷超人
    public static void setLong(String key, long value) {
        SharedPreferences prefs = application.getSharedPreferences(ALA_CORE_SHARED_PREFERENCE_DATA, Application.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    public static long getLong(String key) {
        SharedPreferences prefs = application.getSharedPreferences(ALA_CORE_SHARED_PREFERENCE_DATA, Application.MODE_PRIVATE);
        return prefs.getLong(key, 0L);
    }

    //是否下载过借贷超人
    public static void setBoolean(String key, boolean value) {
        SharedPreferences prefs = application.getSharedPreferences(ALA_CORE_SHARED_PREFERENCE_DATA, Application.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static boolean geBoolean(String key) {
        SharedPreferences prefs = application.getSharedPreferences(ALA_CORE_SHARED_PREFERENCE_DATA, Application.MODE_PRIVATE);
        return prefs.getBoolean(key, false);
    }
}
