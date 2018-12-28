package cn.pro47x.core.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.*;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.DownloadListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import cn.pro47x.core.config.AppConfig;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 工具类
 *
 * @author lisiqi
 * @date 2018-12-28 11:10:59
 */
public class MiscUtils {

    public static final int MAX_GRPS_RETRY_COUNT = 5;// 用GPRS连接的时候，错误的最大重试次数

    public static String sayHello(String name) {
        return "hello " + name;
    }

    public static int getResourcesIdentifier(Context context, String fullname) {
        return AppConfig.getResources().getIdentifier(context.getPackageName() + ":" + fullname, null, null);
    }

    public static String optString(JSONObject jsonObject, String key) {
        return optString(jsonObject, key, "");
    }

    private static String optString(JSONObject jsonObject, String key, String fallback) {
        if (jsonObject.isNull(key)) {
            return fallback;
        }
        return jsonObject.optString(key, fallback);
    }

    public static int optInt(JSONObject jsonObject, String key) {
        return optInt(jsonObject, key, 0);
    }

    private static int optInt(JSONObject jsonObject, String key, int fallback) {
        if (jsonObject.isNull(key)) {
            return fallback;
        }
        return jsonObject.optInt(key, fallback);
    }

    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }


    public static boolean isLocationEnabled() {
        return isLocationProviderEnabled(LocationManager.GPS_PROVIDER) || isLocationProviderEnabled(
                LocationManager.NETWORK_PROVIDER);
    }

    private static boolean isLocationProviderEnabled(String provider) {
        LocationManager lm = (LocationManager) AppConfig.getContext().getSystemService(Context.LOCATION_SERVICE);
        return lm.isProviderEnabled(provider);
    }

    public static boolean isValidCarno(String carno) {
        if (isEmpty(carno)) {
            return false;
        }
        try {
            return carno.matches(
                    "(WJ|[\u0391-\uFFE5]{1})[a-zA-Z0-9]{6}|(WJ|[\u0391-\uFFE5]{1})[a-zA-Z0-9]{5}[\u0391-\uFFE5]{1}");
        } catch (Exception ex) {
        }
        return false;
    }

    /**
     * @param ID 身份证号
     */
    public static boolean isValidID(String ID) {
        Pattern p = Pattern.compile("^[0-9]{17}[0-9Xx]$");
        Matcher m = p.matcher(ID);
        return m.matches();
    }

    //极光推送 校验Tag Alias 只能是数字,英文字母和中文
    public static boolean isValidTagAndAlias(String s) {
        Pattern p = Pattern.compile("^[\u4E00-\u9FA50-9a-zA-Z_-]{0,}$");
        Matcher m = p.matcher(s);
        return m.matches();
    }

    public static String getUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static void lock(Object lock) {
        try {
            synchronized (lock) {
                lock.wait();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static String getSharepreferenceValue(String shareName, String key, String defaultValue) {
        SharedPreferences share = AppConfig.getContext().getSharedPreferences(shareName, Context.MODE_PRIVATE);
        return share.getString(key, defaultValue);
    }

    public static boolean getSharepreferenceValue(String shareName, String key, boolean defaultValue) {
        SharedPreferences share = AppConfig.getContext().getSharedPreferences(shareName, Context.MODE_PRIVATE);
        return share.getBoolean(key, defaultValue);
    }

    public static int getSharepreferenceValue(String shareName, String key, int defaultValue) {
        SharedPreferences share = AppConfig.getContext().getSharedPreferences(shareName, Context.MODE_PRIVATE);
        return share.getInt(key, defaultValue);
    }

    public static float getSharepreferenceValue(String shareName, String key, float defaultValue) {
        SharedPreferences share = AppConfig.getContext().getSharedPreferences(shareName, Context.MODE_PRIVATE);
        return share.getFloat(key, defaultValue);
    }

    public static long getSharepreferenceValue(String shareName, String key, long defaultValue) {
        SharedPreferences share = AppConfig.getContext().getSharedPreferences(shareName, Context.MODE_PRIVATE);
        return share.getLong(key, defaultValue);
    }

    public static long getSharepreferenceLongValue(String shareName, String key, long defaultValue) {
        SharedPreferences share = AppConfig.getContext().getSharedPreferences(shareName, Context.MODE_PRIVATE);
        return share.getLong(key, defaultValue);
    }

    public static void setSharedPreferenceValue(String shareName, String key, String value) {
        SharedPreferences share = AppConfig.getContext().getSharedPreferences(shareName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = share.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static void setSharedPreferenceValue(String shareName, String key, boolean value) {
        SharedPreferences share = AppConfig.getContext().getSharedPreferences(shareName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = share.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static void setSharedPreferenceValue(String shareName, String key, int value) {
        SharedPreferences share = AppConfig.getContext().getSharedPreferences(shareName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = share.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static void setSharedPreferenceValue(String shareName, String key, float value) {
        SharedPreferences share = AppConfig.getContext().getSharedPreferences(shareName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = share.edit();
        editor.putFloat(key, value);
        editor.commit();
    }

    public static void setSharedPreferenceValue(String shareName, String key, long value) {
        SharedPreferences share = AppConfig.getContext().getSharedPreferences(shareName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = share.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    public static boolean isCellphone(String str) {
        Pattern pattern = Pattern.compile("1[34589][0-9]{9}");
        Matcher matcher = pattern.matcher(str);
        Pattern patternTow = Pattern.compile("1[0-9]{10}");
        Matcher matcherTow = patternTow.matcher(str);
        return matcherTow.matches() || matcher.matches();
    }

    public static void onTelEvent(String webViewUrl) {
        StatisticsUtils.onEvent(AppConfig.getContext(), "tel", "all-" + getHost(webViewUrl));
    }

    public static void onConfirmTelEvent(String webViewUrl) {
        StatisticsUtils.onEvent(AppConfig.getContext(), "tel", "confirm-" + getHost(webViewUrl));
    }

    public static void onCancelTelEvent(String webViewUrl) {
        StatisticsUtils.onEvent(AppConfig.getContext(), "tel", "cancel-" + getHost(webViewUrl));
    }

    public static void onInterceptTelEvent(String webViewUrl) {
        StatisticsUtils.onEvent(AppConfig.getContext(), "tel", "intercept-" + getHost(webViewUrl));
    }


    public static int getCurrentYear() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.YEAR);
    }

    public static int getCurrentMonth() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.MONTH) + 1;
    }

    public static void showMessageDialog(final Activity context, final String message) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {

            public void run() {
                if (context.isFinishing()) {
                    return;
                }
                Builder builder = new Builder(context);
                builder.setTitle("消息");
                builder.setMessage(message);
                builder.setPositiveButton("确定", null);
                builder.create().show();
            }
        });
    }

    public static boolean showConfirmDialog(final Activity context, final String title, final String message) {
        Looper looper = Looper.getMainLooper();
        if (Thread.currentThread() == looper.getThread()) {
            throw new IllegalStateException("此方法只能在非主线程上调用！");
        }
        Handler handler = new Handler(looper);
        final Boolean[] fuck = new Boolean[1];
        final Object lock = new Object();

        handler.post(new Runnable() {

            public void run() {
                Builder builder = new Builder(context);
                builder.setTitle(title);
                builder.setMessage(message);
                builder.setCancelable(false);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        fuck[0] = Boolean.TRUE;
                        synchronized (lock) {
                            lock.notifyAll();
                        }
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        fuck[0] = Boolean.FALSE;
                        synchronized (lock) {
                            lock.notifyAll();
                        }
                    }
                });
                builder.create().show();
            }
        });
        while (fuck[0] == null) {
            synchronized (lock) {
                try {
                    lock.wait();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        return fuck[0];
    }

    private static String getHost(String url) {
        try {
            URL _url = new URL(url);
            return _url.getHost();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }


    public static boolean isIntentAvailable(Context context, Intent intent) {
        final PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return MiscUtils.isNotEmpty(list);
    }


    public static void lock(Object lock, long time) {
        try {
            synchronized (lock) {
                lock.wait(time);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);
        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }
        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;
        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 : (int) Math
                .min(Math.floor(w / minSideLength), Math.floor(h / minSideLength));
        if (upperBound < lowerBound) {
            return lowerBound;
        }
        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

    public static String getDaysDesc(int days) {
        if (days < 0) {
            return "已过期";
        } else if (days == 0) {
            return "今天";
        } else if (days == 1) {
            return "明天";
        } else if (days == 2) {
            return "后天";
        } else {
            return days + "天";
        }
    }

    public static ProgressDialog showProgressDialog(Activity context, String title, String message, ProgressDialog pd) {
        if (context.isFinishing()) {
            return null;
        }
        if (pd == null) {
            pd = new ProgressDialog(context);
        }
        if (MiscUtils.isNotEmpty(title)) {
            pd.setTitle(title);
        }
        if (MiscUtils.isNotEmpty(message)) {
            pd.setMessage(message);
        }
        pd.show();
        return pd;

    }

    public static int getFontSizeOfWidth(String content, int width) {
        Paint paint = new Paint();
        int fontSize = DensityUtils.getPxByDip(24);
        paint.setTextSize(fontSize);
        int realWidth = (int) paint.measureText(content);
        if (realWidth > width) {
            while (fontSize > 1) {
                fontSize--;
                paint.setTextSize(fontSize);
                realWidth = (int) paint.measureText(content);
                if (realWidth <= width) {
                    break;
                }
            }
        } else if (realWidth < width) {
            DisplayMetrics dm = new DisplayMetrics();
            WindowManager wm = (WindowManager) AppConfig.getContext().getSystemService(Context.WINDOW_SERVICE);
            if (wm != null)
                wm.getDefaultDisplay().getMetrics(dm);

            while (fontSize < dm.widthPixels) {
                fontSize++;
                paint.setTextSize(fontSize);
                realWidth = (int) paint.measureText(content);
                if (realWidth >= width) {
                    break;
                }
            }
        }
        return fontSize;
    }

    public static int getFontSizeOfMaxWidth(String content, int maxWidth, int initFontSize) {
        Paint paint = new Paint();
        int fontSize = initFontSize;
        paint.setTextSize(fontSize);
        int realWidth = (int) paint.measureText(content);
        if (realWidth > maxWidth) {
            while (fontSize > 1) {
                fontSize--;
                paint.setTextSize(fontSize);
                realWidth = (int) paint.measureText(content);
                if (realWidth <= maxWidth) {
                    break;
                }
            }
        }
        return fontSize;
    }

    public static Date getDateHeadOfDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date getDateTailOfDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }

    public static Date getWeekHeadOfDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date getWeekTailOfDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }

    public static Date getMonthHeadOfDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DATE, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date getMonthTailOfDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DATE, 1);
        cal.add(Calendar.MONTH, 1);
        cal.add(Calendar.DATE, -1);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }

    public static int calculateDays(Date from, Date to) {
        final long ONE_DAY = 86400000L;
        TimeZone timeZone = TimeZone.getDefault();
        long rawOffset = timeZone.getRawOffset();
        int fromDays = (int) ((from.getTime() + rawOffset) / ONE_DAY);
        int toDays = (int) ((to.getTime() + rawOffset) / ONE_DAY);
        return toDays - fromDays;
    }

    public static String getExtensionOfFile(File file) {
        String name = file.getName();
        int index = name.lastIndexOf(".");
        if (index != -1 && index != name.length() - 1) {
            return name.substring(index);
        } else {
            return "";
        }
    }

    public static String addTrail(String input, int maxLength) {
        if (MiscUtils.isEmpty(input)) {
            return input;
        }
        if (input.length() > maxLength) {
            return input.substring(0, maxLength - 1) + "...";
        } else {
            return input;
        }
    }

    public static void goToSite(Context context, String url) {
        Intent it = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(it);
    }

    public static void guard(boolean predicate, String errorMessage) throws Exception {
        if (predicate) {
            return;
        }
        throw new RuntimeException("错误：" + errorMessage);
    }

    public static String getWeekName(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int week = cal.get(Calendar.DAY_OF_WEEK);
        switch (week) {
            case Calendar.SUNDAY:
                return "星期日";
            case Calendar.MONDAY:
                return "星期一";
            case Calendar.TUESDAY:
                return "星期二";
            case Calendar.WEDNESDAY:
                return "星期三";
            case Calendar.THURSDAY:
                return "星期四";
            case Calendar.FRIDAY:
                return "星期五";
            case Calendar.SATURDAY:
                return "星期六";
        }
        return "星期八";
    }


    public static String getErrorMessage(Throwable th, String defaultMessage) {
        String msg = th.getMessage();
        if (MiscUtils.isEmpty(msg)) {
            return defaultMessage;
        } else {
            return msg;
        }
    }


    public static String format(Date date, String pattern) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }

    public static String formatDate(Date date) {
        return format(date, "yyyy-MM-dd");
    }

    public static String formatPhone(String phone) {
//        return  phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
        if (MiscUtils.isEmpty(phone)) {
            return "";
        }
        if (phone.length() < 11) {
            return phone;
        }
        String startPhone = phone.substring(0, 3);
        String endPhone = phone.substring(7);
        return startPhone + "****" + endPhone;
    }

    public static String formatMail(String mail) {
        return mail.replaceAll("(\\w?)(\\w+)(\\w)(@\\w+\\.[a-z]+(\\.[a-z]+)?)", "$1****$3$4"); //  *号显示数量与实际不一定相符,是固定四个
    }

    /**
     * 以最省内存的方式读取本地资源的图片
     *
     * @param context 上下文
     * @param resId   资源ID
     * @return bitmap
     */
    public static Bitmap readBitmap(Context context, int resId) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        // 获取资源图片
        InputStream is = AppConfig.getResources().openRawResource(resId);
        return BitmapFactory.decodeStream(is, null, opt);
    }


    private static Bitmap getBitmapFromDrawable(Drawable drawable, int width, int height) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else {
            if (drawable.getIntrinsicHeight() > 0 && drawable.getIntrinsicWidth() > 0) {
                width = drawable.getIntrinsicWidth();
                height = drawable.getIntrinsicHeight();
            }
            Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, width, height);
            drawable.draw(canvas);
            return bitmap;
        }
    }

    public static Drawable getCoveredDrawable(Drawable input, Drawable cover) {
        int width = input.getIntrinsicWidth();
        int height = input.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas cancas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        // paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        cancas.drawARGB(0, 0, 0, 0);
        input.setBounds(0, 0, width, height);
        input.draw(cancas);
        Bitmap converBitmap = getBitmapFromDrawable(cover, width, height);
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        cancas.drawBitmap(converBitmap, rect, rect, paint);
        return new BitmapDrawable(bitmap);
    }

    public static String safeURLEncode(String s, String encoding) {
        if (s == null) {
            return "";
        }
        try {
            return URLEncoder.encode(s, encoding);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return s;
    }

    public static String safeURLDecode(String s, String encoding) {
        if (s == null) {
            return null;
        }
        try {
            return URLDecoder.decode(s, encoding);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return s;
    }

    public static <T> T safeGetList(List<T> list, int index) {
        if (index >= 0 && index < list.size()) {
            return list.get(index);
        } else {
            return null;
        }
    }

    static boolean isTheSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        TimeZone timeZone = TimeZone.getDefault();
        long rawOffset = timeZone.getRawOffset();
        return (date1.getTime() + rawOffset) / 86400000L == (date2.getTime() + rawOffset) / 86400000L;
    }

    public static int parseInt(String s, int defaultInt) {
        if (isEmpty(s)) {
            return defaultInt;
        }
        try {
            return Integer.parseInt(s);
        } catch (Exception ex) {
            ex.printStackTrace();
            return defaultInt;
        }
    }

    public static boolean parseBoolean(String s, boolean defaultBool) {
        if (isEmpty(s)) {
            return defaultBool;
        }
        try {
            return Boolean.parseBoolean(s);
        } catch (Exception ex) {
            ex.printStackTrace();
            return defaultBool;
        }
    }

    public static double parseDouble(String s, double defaultDouble) {
        try {
            return Double.parseDouble(s);
        } catch (Exception ex) {
            ex.printStackTrace();
            return defaultDouble;
        }
    }

    /*
     *正则校验
     */
    public static Boolean checkKeyWordsByRegular(String keyWord, String regular) {
        Pattern pattern = Pattern.compile(regular);
        Matcher matcher = pattern.matcher(keyWord);
        return matcher.matches();
    }

    public static String format(long number, String pattern) {
        DecimalFormat df = new DecimalFormat(pattern);
        return df.format(number);
    }

    public static String format(double number, String pattern) {
        if (Double.isNaN(number) || Double.isInfinite(number)) {
            number = 0.0;
        }
        DecimalFormat df = new DecimalFormat(pattern);
        return df.format(number);
    }

    public static String format(Number number, String pattern) {
        DecimalFormat df = new DecimalFormat(pattern);
        return df.format(number);
    }

    public static Object invokeMethod(Object obj, String methodName, Object[] methodArgs, Class<?>... args) {
        Class<?> cls = obj.getClass();
        try {
            Method method = cls.getDeclaredMethod(methodName, args);
            method.setAccessible(true);
            return method.invoke(obj, methodArgs);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getValueOfField(Object obj, String fieldName) {
        Class<?> cls = obj.getClass();
        try {
            Field field = cls.getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(obj);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static boolean setValueOfField(Object obj, String fieldName, Object value) {
        Class<?> cls = obj.getClass();
        try {
            Field field = cls.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = DensityUtils.getPxByDip(12);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    public static <T> List<T> copy(List<T> list) {
        List<T> copy = new ArrayList<T>();
        if (MiscUtils.isEmpty(list)) {
            return copy;
        }
        for (T t : list) {
            copy.add(t);
        }
        return copy;
    }

    public static int parseInt(String s) {
        if (isEmpty(s)) {
            return 0;
        }
        try {
            return Integer.parseInt(s);
        } catch (Exception ex) {
            return 0;
        }
    }

    public static Date parseDate(String input, String pattern) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            sdf.setLenient(false);
            return sdf.parse(input);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new Date();
    }

    public static int parseColor(String color) {
        if (TextUtils.isEmpty(color)) {
            return 0xFF000000;
        }
        try {
            if (color.startsWith("#")) {
                color = color.substring(1);
            }
            return Integer.parseInt(color, 16) | 0xFF000000;
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        }
        return 0x000000;

    }


    /**
     * 得到允许的GPS提供者列表，以逗号分隔 移动网络定位：network(@see LocationManager.NETWORK_PROVIDER)
     * GPS卫星定位： gps(@see LocationManager.GPS_PROVIDER)
     *
     * @return 允许的提供者列表
     */
    public static String getAllowedLocationPrividers(Context context) {
        return Settings.System.getString(context.getContentResolver(), Secure.LOCATION_PROVIDERS_ALLOWED);
    }

    public static void assertTrue(boolean b, String error) {
        if (!b) {
            throw new RuntimeException(error);
        }
    }


    public static boolean isEquals(Object obj1, Object obj2) {
        if (obj1 != null) {
            return obj1.equals(obj2);
        } else
            return obj2 == null;
    }

    public static boolean isEqualsIgnoreCase(String str1, String str2) {
        if (str1 != null) {
            return str1.equalsIgnoreCase(str2);
        } else
            return str2 == null;
    }

    public static boolean isNotEquals(Object obj1, Object obj2) {
        return !isEquals(obj1, obj2);
    }

    public static boolean isNotEmpty(String s) {
        return !isEmpty(s);
    }


    public static boolean isEmptyOrLiterallyNull(String s) {
        return MiscUtils.isEmpty(s) || "null".equals(s);
    }

    public static boolean isNotEmpty(Collection<?> c) {
        return !isEmpty(c);
    }

    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    public static boolean isEmpty(String s) {
        return s == null || s.trim().length() == 0 || "null".equals(s);
    }

    /**
     * @desc 只含空格不为空
     */
    public static boolean isSimpleEmpty(String s) {
        return s == null || s.length() == 0 || "null".equals(s);
    }

    public static boolean isEmpty(Collection<?> c) {
        return c == null || c.isEmpty();
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static List<String> splitSql(String content, String split) {
        List<String> result = new ArrayList<>();
        if (MiscUtils.isEmpty(split)) {
            return result;
        }
        String[] ss = content.split(split);
        for (String sql : ss) {
            if (!MiscUtils.isEmpty(sql)) {
                result.add(sql);
            }
        }
        return result;
    }


    /**
     * 返回两个时间点的具体描述
     *
     * @param min 发生的时间
     * @param max 当前的时间
     */
    public static String getBetweenTime(long min, long max) {
        int seconds = (int) ((max - min) / 1000L);
        if (seconds < 30) {
            return "刚刚";
        } else if (seconds < 60) {
            return "30秒前";
        } else if (seconds < 3600) {
            return (seconds / 60) + "分钟前";
        } else {
            // 如果是同一天，则显示时分，否则就显示某一天
            Calendar minCal = Calendar.getInstance();
            Calendar maxCal = Calendar.getInstance();
            minCal.setTimeInMillis(min);
            maxCal.setTimeInMillis(max);
            Date minDate = minCal.getTime();
            // 如果同年月日，则显示时间和分钟就可以了
            if (minCal.get(Calendar.YEAR) == maxCal.get(Calendar.YEAR) && minCal.get(Calendar.MONTH) == maxCal
                    .get(Calendar.MONTH) && minCal.get(Calendar.DATE) == maxCal.get(Calendar.DATE)) {
                return format(minDate, "HH:mm");
            }// 如果同年，则显示日期和时间
            else if (minCal.get(Calendar.YEAR) == maxCal.get(Calendar.YEAR)) {
                return format(minDate, "MM-dd HH:mm");
            } else {
                return format(minDate, "yyyy-MM-dd HH:mm");
            }
        }

    }

    /**
     * 获取导航栏高度
     */
    public static int getNavigationBarHeight() {
        Resources resources = Resources.getSystem();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        int height = resources.getDimensionPixelSize(resourceId);
        Log.v("dbw", "Navi height:" + height);
        return height;
    }

    /**
     * 获取状态栏高度
     */
    public static int getStateBarHeight(Window window) {
        Rect frame = new Rect();
        window.getDecorView().getWindowVisibleDisplayFrame(frame);
        return frame.top;
    }

    /**
     * 获取应用区域宽高
     */
    public static Rect getAppRect(Window window) {
        Rect appRect = new Rect();
        window.getDecorView().getWindowVisibleDisplayFrame(appRect); // 应用区域
        return appRect;
    }

    /**
     * 获取状态栏高度
     */
    public static int getStateBarHeight() {
        return Resources.getSystem()
                .getDimensionPixelSize(Resources.getSystem().getIdentifier("status_bar_height", "dimen", "android"));
    }

    public static String getFragmentParams(String url, String key) {
        String value = "";
        try {
            Uri uri = Uri.parse(url);
            String fragment = uri.getFragment();
            if (MiscUtils.isNotEmpty(fragment) && fragment.contains(key + "=")) {
                String[] splits = fragment.split("&");
                for (String split : splits) {
                    String[] nameValuePairs = split.split("=");
                    if (nameValuePairs.length == 2) {
                        String name = nameValuePairs[0];
                        if (name.equals(key)) {
                            value = nameValuePairs[1];
                            break;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return value;
    }

    //隐藏软件盘
    public static void hideKeyBoard(View view) {
        InputMethodManager imm = (InputMethodManager) AppConfig.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null)
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    //显示软件盘
    public static void showKeyBoard(View view) {
        InputMethodManager imm = (InputMethodManager) AppConfig.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null)
            imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
    }

    public static int dpToPx(float dp, Resources resources) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
        return (int) px;
    }


    /**
     * 去除字符串中的空格、回车、换行符、制表符
     */
    public static String replaceBlank(String str) {
        String dest = "";
        if (str != null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }


    /**
     * 去除字符串中的回车、换行符
     */
    public static String replaceNewLine(String str) {
        return str.replaceAll("[\\t\\n\\r]", "");
    }


    //map转换成list
    public static List mapTransitionList(Map map) {
        List list = new ArrayList();
        for (Object o : map.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            list.add(entry.getValue());
        }

        return list;
    }

    /**
     * 使用 Map按key进行排序
     */
    public static Map<String, String> sortMapByKey(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }

        Map<String, String> sortMap = new TreeMap<>(new MapKeyComparator());

        sortMap.putAll(map);

        return sortMap;
    }


    public static class MapKeyComparator implements Comparator<String> {

        @Override
        public int compare(String str1, String str2) {

            return str1.compareTo(str2);
        }
    }


    /**
     * 颜色加深处理
     *
     * @param RGBValues RGB的值，由alpha（透明度）、red（红）、green（绿）、blue（蓝）构成，
     *                  Android中我们一般使用它的16进制，
     *                  例如："#FFAABBCC",最左边到最右每两个字母就是代表alpha（透明度）、
     *                  red（红）、green（绿）、blue（蓝）。每种颜色值占一个字节(8位)，值域0~255
     *                  所以下面使用移位的方法可以得到每种颜色的值，然后每种颜色值减小一下，在合成RGB颜色，颜色就会看起来深一些了
     */
    public static int colorBurn(int RGBValues) {
        int alpha = RGBValues >> 24;
        int red = RGBValues >> 16 & 0xFF;
        int green = RGBValues >> 8 & 0xFF;
        int blue = RGBValues & 0xFF;
        red = (int) Math.floor(red * (1 - 0.1));
        green = (int) Math.floor(green * (1 - 0.1));
        blue = (int) Math.floor(blue * (1 - 0.1));
        // return Color.rgb(red, green, blue);
        return Color.argb(alpha, red, green, blue);
    }

    public static boolean isNumeric(String str) {
        for (int i = 0; i < str.length(); i++) {
            System.out.println(str.charAt(i));
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 是否是Collection的实现类
     */
    public static boolean isInstanceOfCollection(Class clazz) {
        Type genType = clazz.getGenericSuperclass();
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        String result = params[0].toString();
        return result.contains(Collection.class.getName());
    }

    /**
     * 程序初始化所有WebView
     */
    public static void initWebView(Context context) {
        WebView webView = new WebView(context);
        webView.setVerticalScrollbarOverlay(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);//启用js
        webView.getSettings().setBlockNetworkImage(false);//解决图片不显示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        webView.getSettings()
                .setDatabasePath(AppConfig.getContext().getDir("database", Context.MODE_PRIVATE).getPath());
        webView.getSettings().setAppCachePath(AppConfig.getContext().getDir("cache", Context.MODE_PRIVATE).getPath());
    }

    @SuppressLint("SetJavaScriptEnabled")
    public static void enableHTML5(WebView webView, boolean netFirst) {
        webView.setVerticalScrollbarOverlay(true);
        int prid = MiscUtils.getResourcesIdentifier(AppConfig.getContext(), "string/product");
        String pr = AppConfig.getContext().getResources().getString(prid);
        String userAgent = webView.getSettings().getUserAgentString();
        userAgent += "ala" + pr;
        //适配部分机型整体布局向又靠
        webView.getSettings().setTextZoom(100);
        webView.getSettings().setGeolocationEnabled(true);
        webView.getSettings().setUserAgentString(userAgent);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings()
                .setDatabasePath(AppConfig.getContext().getDir("database", Context.MODE_PRIVATE).getPath());
        webView.getSettings().setAppCachePath(AppConfig.getContext().getDir("cache", Context.MODE_PRIVATE).getPath());
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setBlockNetworkImage(false);//解决图片不显示
        //可以让h5调用播放器自动播放
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        webView.getSettings().setDisplayZoomControls(false);
        try {
            webView.getSettings().setDomStorageEnabled(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        webView.getSettings().setAppCacheMaxSize(1024 * 1024 * 50);
        webView.getSettings().setAllowFileAccess(true);
        if (netFirst) {
            webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        } else {
            webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }
        webView.setDownloadListener(new DownloadListener() {

            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype,
                                        long contentLength) {
                MiscUtils.goToSite(AppConfig.getCurrentActivity(), url);
            }
        });

        // webview同时要实现如下方法
        // @Override
        // public void onExceededDatabaseQuota(String url, String databaseIdentifier, long quota, long
        // estimatedDatabaseSize, long totalQuota, QuotaUpdater quotaUpdater) {
        // quotaUpdater.updateQuota(estimatedDatabaseSize * 2);
        // }
    }

    public static boolean isNetAvailable(Context context) {
        boolean bisConnFlag = false;
        ConnectivityManager conManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (conManager != null) {
            NetworkInfo network = conManager.getActiveNetworkInfo();
            if (network != null) {
                bisConnFlag = conManager.getActiveNetworkInfo().isAvailable();
            }
        }
        return bisConnFlag;
    }

}

