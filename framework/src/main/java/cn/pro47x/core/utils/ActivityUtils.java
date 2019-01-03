package cn.pro47x.core.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import cn.pro47x.core.R;
import cn.pro47x.core.config.AppConfig;
import cn.pro47x.core.utils.log.Logger;

import java.lang.ref.WeakReference;
import java.util.Enumeration;
import java.util.Stack;

/**
 * 版权：XXX公司 版权所有
 * 作者：Jacky Yu
 * 版本：1.0
 * 创建日期：2017/1/3
 * 描述：activity工具类
 * 修订历史：
 */
public final class ActivityUtils {
    private static final String TAG = "ActivityUtils";
    // 堆栈管理对象
    private static final ActivityStack STACK = new ActivityStack();

    /**
     * push this activity to stack
     */
    public static void push(Activity activity) {
        Logger.i(TAG, "push = " + activity);
        STACK.pushToStack(activity);
    }

    /**
     * pop top activity from stack
     */
    public static void pop() {
        Activity activity = STACK.popFromStack();
        Logger.i(TAG, "pop = " + activity);
        if (activity != null) {
            activity.finish();
        }
    }

    /**
     * remove this activity from stack, maybe is null
     */
    public static void remove(Activity activity) {
        Logger.i(TAG, "remove = " + activity);
        STACK.removeFromStack(activity);
    }

    /**
     * peek top activity from stack, maybe is null
     */
    public static Activity peek() {
        Activity activity = STACK.peekFromStack();
        Logger.i(TAG, "peek = " + activity);
        return activity;
    }

    /**
     * pop activities until this Activity
     */
    @SuppressWarnings("unchecked")
    public static <T extends Activity> T popUntil(final Class<T> clazz) {
        if (clazz != null) {
            while (!STACK.isEmpty()) {
                final Activity activity = STACK.popFromStack();
                if (activity != null) {
                    if (clazz.isAssignableFrom(activity.getClass())) {
                        return (T) activity;
                    }
                    activity.finish();
                }
            }
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setClass(AppConfig.getContext(), clazz);
            AppConfig.getContext().startActivity(intent);
        }
        return null;
    }


    /**
     * pop activities until this Activity
     */
    @SuppressWarnings("unchecked")
    public static <T extends Activity> T popUntil(final Class<T> clazz, Intent intent) {
        if (clazz != null) {
            while (!STACK.isEmpty()) {
                final Activity activity = STACK.popFromStack();
                if (activity != null) {
                    if (clazz.isAssignableFrom(activity.getClass())) {
                        activity.setIntent(intent);
                        break;
                    }
                    activity.finish();
                }
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setClass(AppConfig.getContext(), clazz);
            AppConfig.getContext().startActivity(intent);
        }
        return null;
    }

    /**
     * pop activities until this Activity without refresh it.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Activity> T popUntilWithoutRefresh(final Class<T> clazz) {
        if (clazz != null) {
            while (!STACK.isEmpty()) {
                final Activity activity = STACK.popFromStack();
                if (activity != null) {
                    if (clazz.isAssignableFrom(activity.getClass())) {
                        return (T) activity;
                    }
                    if (clazz.getSimpleName().equals(activity.getClass().getSimpleName()) || activity.getClass().getSimpleName().equals("MainActivity")) {
                        return null;
                    } else
                        activity.finish();
                }
            }
        }
        return null;
    }

    /**
     * 最后一次尝试退出的时间戳
     */
    private static long lastExitPressedMills = 0;
    /**
     * 距上次尝试退出允许的最大时间差
     */
    private static final long MAX_DOUBLE_EXIT_MILLS = 800;

    /**
     * 退出APP,如果补丁已经应用成功则杀死进程
     */
    public static void onExit(boolean crash) {
        if (crash) {
            finishAll();
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        } else {
            final long now = System.currentTimeMillis();
            if (now <= lastExitPressedMills + MAX_DOUBLE_EXIT_MILLS) {
                finishAll();
                System.exit(0);
//            AppConfig.getWatcher().stopWatch();
//            System.exit(0);
            } else {
                Context context = peek();
                if (context != null) {
                    Toast.makeText(context, context.getString(R.string.app_exit), Toast.LENGTH_SHORT).show();
                }
                lastExitPressedMills = now;
            }
        }
    }

    /**
     * 当APP退出的时候，结束所有Activity
     */
    private static void finishAll() {
        Logger.i(TAG, "********** Exit **********");
        while (!STACK.isEmpty()) {
            final Activity activity = STACK.popFromStack();
            if (activity != null) {
                Logger.i(TAG, "Exit = " + activity);
                activity.finish();
            }
        }
    }


    /**
     * 结束Activity
     */
    public static void finish(final Class<? extends Activity> clazz) {
        Logger.i(TAG, "********** finish activity **********");
        if (clazz != null) {
            Enumeration<WeakReference<Activity>> elements = STACK.activityStack.elements();
            while (elements.hasMoreElements()) {
                Activity activity = elements.nextElement().get();
                if (activity != null) {
                    if (clazz.equals(activity.getClass())) {
                        Logger.i(TAG, "********** finish activity **********" + clazz.getName());
                        STACK.removeFromStack(activity);
                        activity.finish();
                        return;
                    }
                }
            }
        }
    }


    /**
     * activity堆栈，用以管理APP中的所有activity
     */
    private static class ActivityStack {
        // activity堆对象
        private final Stack<WeakReference<Activity>> activityStack = new Stack<>();

        /**
         * @return 堆是否为空
         */
        public boolean isEmpty() {
            return activityStack.isEmpty();
        }

        /**
         * 向堆中push此activity
         */
        void pushToStack(Activity activity) {
            activityStack.push(new WeakReference<>(activity));
        }

        /**
         * @return 从堆栈中pop出一个activity对象
         */
        Activity popFromStack() {
            while (!activityStack.isEmpty()) {
                final WeakReference<Activity> weak = activityStack.pop();
                final Activity activity = weak.get();
                if (activity != null) {
                    return activity;
                }
            }
            return null;
        }

        /**
         * @return 从堆栈中查看一个对象，且不会pop
         */
        Activity peekFromStack() {
            while (!activityStack.isEmpty()) {
                final WeakReference<Activity> weak = activityStack.peek();
                final Activity activity = weak.get();
                if (activity != null) {
                    return activity;
                } else {
                    activityStack.pop();
                }
            }
            return null;
        }

        /**
         * @return 从堆栈中删除指定对象
         */
        boolean removeFromStack(Activity activity) {
            for (WeakReference<Activity> weak : activityStack) {
                final Activity act = weak.get();
                if (act == activity) {
                    return activityStack.remove(weak);
                }
            }
            return false;
        }
    }
    ///////////////////////////////////////////////////////////////////////////
    // 启动activity
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 启动新的Activity
     */
    public static void push(Activity a, Class<? extends Activity> clazz, Intent intent, int requestCode) {
        if (a == null) {
            if (intent == null) {
                intent = new Intent();
            }
            intent.setClass(AppConfig.getContext(), clazz);
            if (requestCode >= 0) {
                Activity curActivity = AppConfig.getCurrentActivity();
                if (curActivity != null) {
                    curActivity.startActivityForResult(intent, requestCode);
                } else {
                    Logger.w(TAG, "activity is null");
                }
            } else {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                AppConfig.getContext().startActivity(intent);
            }
        } else {
            Logger.w(TAG, a.getClass().getSimpleName() + " -> " + clazz.getSimpleName());
            intent = getIntent(a, clazz, intent);
//            if (!(a instanceof HTML5WebView) || !clazz.getSimpleName().equals(HTML5WebView.class.getSimpleName()))
//                //排除HTML5WebView打开HTML5WebView的情况
//                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            if (requestCode >= 0) {
                a.startActivityForResult(intent, requestCode);
            } else {
                a.startActivity(intent);
            }
        }
    }

    public static void push(Class<? extends Activity> clazz, Intent intent) {
        push(peek(), clazz, intent, -1);
    }

    public static void push(Activity a, Class<? extends Activity> clazz, Intent intent) {
        push(a, clazz, intent, -1);
    }

    public static void push(Class<? extends Activity> clazz, Intent intent, int requestCode) {
        push(peek(), clazz, intent, requestCode);
    }

    public static void push(Class<? extends Activity> clazz) {
        push(peek(), clazz, null, -1);
    }

    public static void push(Activity a, Class<? extends Activity> clazz) {
        push(a, clazz, null, -1);
    }

    public static void push(Activity a, Class<? extends Activity> clazz, int code) {
        push(a, clazz, null, code);
    }

    /**
     * 根据入参，获得intent
     */
    private static Intent getIntent(Activity a, Class<? extends Activity> clazz, Intent intent) {
        if (intent == null) {
            intent = new Intent();
        }
        intent.setClass(a, clazz);
        return intent;
    }

    public static void pop(final Activity a) {
        pop(a, null);
    }

    public static void pop(final Activity a, int code) {
        pop(a, code, null);
    }

    public static void pop(final Activity a, Intent intent) {
        pop(a, Activity.RESULT_OK, intent);
    }

    /**
     * 关闭Activity
     */
    public static void pop(final Activity a, int code, Intent intent) {
        if (intent != null) {
            a.setResult(code, intent);
        }
        a.finish();
    }

    /**
     * dialog 的宿主activity是否已被销毁
     */
    public static boolean activityIsAlive(Dialog dialog) {
        Context context = dialog.getContext();
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            return !activity.isDestroyed() && !activity.isFinishing();
        }
        return true;
    }

    public static boolean isAlive(final Class<? extends Activity> clazz) {
        Enumeration<WeakReference<Activity>> elements = STACK.activityStack.elements();
        while (elements.hasMoreElements()) {
            Activity activity = elements.nextElement().get();
            if (activity != null && activity.getClass().equals(clazz)) {
                return true;
            }
        }
        return false;
    }

}
