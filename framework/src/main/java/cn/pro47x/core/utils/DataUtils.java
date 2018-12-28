package cn.pro47x.core.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.io.*;
import java.lang.ref.WeakReference;
import java.security.MessageDigest;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static android.os.Environment.MEDIA_MOUNTED;

/**
 * 此类专门负责读取程序的数据和资源 并且处理外部数据和程序内置数据的优先级关系
 *
 * @author lisiqi
 */
public class DataUtils {

    private static final int HEADER_SIZE = 16;
    private static final String TAG = "dataUtils";

    // 放在Assets里面的图片缓存
    private static Map<String, WeakReference<Drawable>> imageCaches = new HashMap<String, WeakReference<Drawable>>();

    private DataUtils() {
    }

    public static void decryptFile(InputStream is, OutputStream os, int bufferSize) throws IOException {
        byte[] header = new byte[HEADER_SIZE];
        int offset = 0;
        int length = header.length;
        //先把头读出来
        while (true) {
            int readLength = is.read(header, offset, length);
            if (readLength == -1) {
                break;
            } else {
                offset += readLength;
                length -= readLength;
            }
            if (length <= 0) {
                break;
            }
        }
        byte[] buffer = new byte[bufferSize];
        int index = 0;
        while ((length = is.read(buffer)) != -1) {
            for (int i = 0; i < length; i++) {
                buffer[i] ^= header[index % header.length];
                index++;
            }
            os.write(buffer, 0, length);
        }
        os.flush();
    }

    public static void decryptFile(InputStream is, OutputStream os) throws IOException {
        decryptFile(is, os, 8192);
    }


    /**
     * 从指定路径中获取图片,并缓存
     */
    public static Drawable getDrawableOfAssets(String fullPath) {
        return loadAndCacheIfNeed(fullPath, imageCaches);
    }

    private static Drawable loadAndCacheIfNeed(String filePath, Map<String, WeakReference<Drawable>> map) {
        Drawable dr = getCachedDrawable(map, filePath);
        if (dr != null) {
            return dr;
        }
        InputStream is = null;
        try {
            DisplayMetrics dm = getCurrentDisplayMetrics();
            is = AlaConfig.getContext().getAssets().open(filePath);
            Options opts = new Options();
            opts.inDensity = 160;
            opts.inTargetDensity = dm.densityDpi;
            opts.inScreenDensity = dm.densityDpi;
            Bitmap bitmap = BitmapFactory.decodeStream(is, null, opts);

            int usedWidth = (dm.widthPixels - 40);
            if (bitmap.getWidth() > usedWidth) {
                Matrix matrix = new Matrix();
                float scale = 1.f * usedWidth / bitmap.getWidth();
                matrix.postScale(scale, scale);
                Bitmap newB = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                bitmap = newB;
            }
            dr = new BitmapDrawable(bitmap);

            map.put(filePath, new WeakReference<Drawable>(dr));
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            DataUtils.close(is);
        }
        return dr;
    }

    private static Drawable getCachedDrawable(Map<String, WeakReference<Drawable>> map, String key) {
        WeakReference<Drawable> wr = map.get(key);
        if (wr != null) {
            Drawable entry = wr.get();
            if (entry != null) {
                return entry;
            } else {
                map.remove(key);
            }
        }
        return null;
    }

    public static DisplayMetrics getCurrentDisplayMetrics() {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) AlaConfig.getContext().getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);
        return dm;
    }

    public static Drawable readFromFile(String filePath) {
        InputStream is = null;
        try {
            is = new FileInputStream(filePath);
            Options options = new Options();
            options.inDensity = 160;
            options.inTargetDensity = 160;
            options.inScreenDensity = 160;
            Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
            return new BitmapDrawable(bitmap);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            close(is);
        }
        return null;
    }

    public static boolean deleteFile(File file) {
        if (file == null) {
            return false;
        }
        try {
            boolean succ = file.delete();
            if (succ == false) {
                file.deleteOnExit();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static String bytesToHex(byte[] data) {
        StringBuilder dist = new StringBuilder();
        for (byte b : data) {
            dist.append(String.format("%02x", b));
        }
        return dist.toString();
    }

    public static File createIfNotExistsOnPhone(String fileName) {
        File file = AlaConfig.getContext().getFileStreamPath(fileName);
        if (file != null) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    public static File createDirIfNotExists(String dirName) {
        if (isExternalStorageMounted()) {
            return createDirIfNotExistsOnSDCard(dirName);
        } else {
            return createDirIfNotExistsOnPhone(dirName);
        }
    }

    public static File createDirIfNotExistsOnPhone(String dirName) {
        File file = AlaConfig.getContext().getDir(dirName, Context.MODE_PRIVATE);
        if (file != null) {
            try {
                file.mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    public static File createIfNotExistsOnSDCard(String fileName) {
        if (MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            File file = new File(Environment.getExternalStorageDirectory(),
                    "/Android/data/" + AlaConfig.getContext().getPackageName() + "/files/" + fileName);
            if (file != null) {
                try {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return file;
        }
        return null;
    }

    public static File createDirIfNotExistsOnSDCard(String dirName) {
        if (MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            File file = new File(Environment.getExternalStorageDirectory(),
                    "/Android/data/" + AlaConfig.getContext().getPackageName() + "/files/" + dirName);
            if (file != null) {
                try {
                    file.mkdirs();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return file;
        }
        return null;
    }

    public static File getTempDir() {
        return createDirIfNotExists("temp");
    }

    public static void deleteSDCardFolder(File dir) {
        File to = new File(dir.getAbsolutePath() + System.currentTimeMillis());
        dir.renameTo(to);
        if (to.isDirectory()) {
            String[] children = to.list();
            for (int i = 0; i < children.length; i++) {
                File temp = new File(to, children[i]);
                if (temp.isDirectory()) {
                    deleteSDCardFolder(temp);
                } else {
                    boolean b = temp.delete();
                    if (b == false) {
                        Logger.e("ala", "DELETE FAIL");
                    }
                }
            }
            to.delete();
        }
    }

    public static File createIfNotExists(String externalFileName) {
        // 如果可用SD卡，则用SD卡，否则，用手机的内存
        File file = null;
        if (MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            file = new File(Environment.getExternalStorageDirectory(),
                    "/Android/data/" + AlaConfig.getContext().getPackageName() + "/files/" + externalFileName);
        } else {
            file = AlaConfig.getContext().getFileStreamPath("/" + externalFileName);
        }
        if (file != null) {
//            /**
//             * java.io.IOException: open failed: EBUSY (Device or resource busy)
//             */
//            deleteSDCardFolder(file);

            boolean success = true;
            if (!file.getParentFile().exists()) {
                success = file.getParentFile().mkdir();
            }
            if (success) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Logger.e("ala", "create file is err");
            }
        }

        return file;
    }

    public static void saveToFile(String s, File file) {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
            bw.write(s);
            bw.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            close(bw);
        }
    }

    public static void saveToFileCanAppend(String s, File file) {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8"));
            bw.write(s);
            bw.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            close(bw);
        }
    }

    public static void saveToFile(byte[] data, File file) throws IOException {
        FileOutputStream bw = new FileOutputStream(file);
        try {
            bw.write(data);
        } finally {
            bw.close();
        }
    }

    public static String readFile(File file) {
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(file);
            return readFromStream(fin, "UTF-8");
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            close(fin);
        }
        return null;
    }

    /**
     * 读取可配置的内容，如果外部的文件存在，则读取外部的文件 否则，读取程序内置的文件内容
     *
     * @param externFileName 外部文件的访问路径
     * @param assetFileName  内置文件的访问路径
     */
    public static String readFile(String externFileName, String assetFileName) {
        InputStream is = null;
        try {
            is = readFileStream(externFileName, assetFileName);
            return readFromStream(is, "UTF-8");
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            close(is);
        }
        return null;
    }

    public static Properties readPropertiesFromFile(File file) {
        Properties pro = new Properties();
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(file);
            pro.load(fin);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            close(fin);
        }
        return pro;
    }

    public static void savePropertiesToFile(Properties pro, File file) {
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(file);
            pro.store(fout, "ala");
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            close(fout);
        }
    }

    public static void close(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Exception ex) {
            Logger.w("ala", null, ex);
        }
    }

    public static void close(Cursor cursor) {
        try {
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception ex) {
            Logger.w("ala", null, ex);
        }
    }

    public static void close(SQLiteDatabase db) {
        try {
            //在这里进行判断，如果系统的版本低于3.0，则不关闭
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB || db == null) {
                return;
            }
            db.releaseReference();
        } catch (Exception ex) {
            Logger.w("ala", null, ex);
        }
    }

    public static byte[] readData(InputStream is) throws IOException {
        ByteArrayOutputStream bout = null;
        try {
            bout = new ByteArrayOutputStream();
            copy(is, bout);
            return bout.toByteArray();
        } finally {
            DataUtils.close(bout);
        }
    }

    /**
     * 读取可配置的内容，如果外部的文件存在，则读取外部的文件 否则，读取程序内置的文件内容
     *
     * @param externFileName 外部文件的访问路径
     * @param assetFileName  内置文件的访问路径
     */
    public static InputStream readFileStream(String externFileName, String assetFileName) {
        File externalFile = checkExternalDataFileExists(externFileName);
        InputStream is = null;
        try {
            if (externalFile != null && externalFile.length() > 0) {
                is = new FileInputStream(externalFile);
            } else {
                is = AlaConfig.getContext().getAssets().open(assetFileName);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return is;
    }

    public static final String readClassResource(String resName) {
        InputStream is = null;
        try {
            is = DataUtils.class.getResourceAsStream(resName);
            return readFromStream(is, "UTF-8");
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            close(is);
        }
        return null;
    }

    public static final List<String> readContentByLine(String externFileName, String assetFileName) {
        String content = readFile(externFileName, assetFileName);
        if (MiscUtils.isEmpty(content) == false) {
            List<String> ss = readContentByLine(content);
            return ss;
        }
        return Collections.emptyList();
    }

    public static final List<String> readContentByLine(String content) {
        List<String> list = new ArrayList<String>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new StringReader(content));
            String line = null;
            while ((line = br.readLine()) != null) {
                list.add(line);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            close(br);
        }

        return list;
    }

    public static final String readAssetFileContent(String filePath) {
        InputStream is = null;
        try {
            is = AlaConfig.getContext().getAssets().open(filePath);
            String content = readFromStream(is, "UTF-8");
            return content;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            close(is);
        }
        return null;
    }

    public static String readFromStream(InputStream is, String encoding) {
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is, encoding));
            char[] buffer = new char[1024];
            int length = -1;
            while ((length = br.read(buffer)) != -1) {
                sb.append(buffer, 0, length);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return sb.toString();
    }

    /**
     * 检查某个外部文件是否存在，首先会检查SD卡里面的文件 如果SD里面的不存在，或者SD卡不可用，则检查系统的，
     * 如果存在，则返回此文件对象，如果不存在，则返回null
     *
     * @param fileName 文件名
     */
    public static File checkExternalDataFileExists(String fileName) {
        if (MiscUtils.isEmpty(fileName)) {
            return null;
        }
        File file = checkSDFile(fileName);
        if (file == null) {
            file = checkPhoneFile(fileName);
        }
        return file;
    }

    public static File checkSDFile(String fileName) {
        if (MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            File file = new File(Environment.getExternalStorageDirectory(),
                    "/Android/data/" + AlaConfig.getContext().getPackageName() + "/files/" + fileName);
            if (file != null && file.exists()) {
                return file;
            }
        }
        return null;
    }

    public static File checkPhoneFile(String fileName) {
        File file = AlaConfig.getContext().getFileStreamPath(fileName);
        if (file != null && file.exists()) {
            return file;
        }
        return null;
    }

    public static String getPhoneAppPath() {
        return AlaConfig.getContext().getFilesDir().getPath();
    }

    public static String getSDCardAppPath() {
        File file = new File(Environment.getExternalStorageDirectory(),
                "/Android/data/" + AlaConfig.getContext().getPackageName());
        return file.getPath();
    }

    public static String getSDCardRootPath() {
        return Environment.getExternalStorageDirectory().getPath();
    }

    public static boolean isExternalStorageMounted() {
        return MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    public static void copy(File source, File dest) throws IOException {
        FileInputStream fin = null;
        FileOutputStream fout = null;
        try {
            fin = new FileInputStream(source);
            fout = new FileOutputStream(dest);
            copy(fin, fout);
        } finally {
            close(fin);
            close(fout);
        }
    }

    public static void copy(InputStream is, OutputStream os) throws IOException {
        byte[] buffer = new byte[8192];
        int length = -1;
        while ((length = is.read(buffer)) != -1) {
            os.write(buffer, 0, length);
        }
    }

    public static void copy(InputStream is, OutputStream os, MessageDigest md) throws IOException {
        byte[] buffer = new byte[8192];
        int length = -1;
        while ((length = is.read(buffer)) != -1) {
            os.write(buffer, 0, length);
            md.update(buffer, 0, length);
        }
    }

    private static File getSaveFile(String key) {
        return new File(AlaConfig.getContext().getCacheDir(), key);
    }


    /**
     * 解开ZIP里面的文件，并返回说明文件的文件名
     *
     * @return 说明文件的文件名
     */
    public static void extractZipFile(File zip, File toDir) throws IOException {
        ZipFile zipFile = new ZipFile(zip);
        try {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                File file = new File(toDir, entry.getName());
                file.getParentFile().mkdirs();
                if (entry.isDirectory()) {
                    file.mkdirs();
                } else {
                    FileOutputStream fout = null;
                    InputStream fin = null;
                    try {
                        fout = new FileOutputStream(file);
                        fin = zipFile.getInputStream(entry);
                        DataUtils.copy(fin, fout);
                    } finally {
                        DataUtils.close(fout);
                        DataUtils.close(fin);
                    }
                }
            }
        } finally {
            zipFile.close();
        }
    }

    private static final String EXTERNAL_STORAGE_PERMISSION = "android.permission.WRITE_EXTERNAL_STORAGE";
    private static final String INDIVIDUAL_DIR_NAME = "uil-images";

    /**
     * Returns application cache directory. Cache directory will be created on SD card
     * <i>("/Android/data/[app_package_name]/cache")</i> if card is mounted and app has appropriate permission. Else -
     * Android defines cache directory on device's file system.
     *
     * @param context Application context
     * @return Cache {@link File directory}.<br />
     * <b>NOTE:</b> Can be null in some unpredictable cases (if SD card is unmounted and
     * {@link Context#getCacheDir() Context.getCacheDir()} returns null).
     */
    public static File getCacheDirectory(Context context) {
        return getCacheDirectory(context, true);
    }

    /**
     * Returns application cache directory. Cache directory will be created on SD card
     * <i>("/Android/data/[app_package_name]/cache")</i> (if card is mounted and app has appropriate permission) or
     * on device's file system depending incoming parameters.
     *
     * @param context        Application context
     * @param preferExternal Whether prefer external com.framework.core.location for cache
     * @return Cache {@link File directory}.<br />
     * <b>NOTE:</b> Can be null in some unpredictable cases (if SD card is unmounted and
     * {@link Context#getCacheDir() Context.getCacheDir()} returns null).
     */
    public static File getCacheDirectory(Context context, boolean preferExternal) {
        File appCacheDir = null;
        String externalStorageState;
        try {
            externalStorageState = Environment.getExternalStorageState();
        } catch (NullPointerException e) { // (sh)it happens (Issue #660)
            externalStorageState = "";
        }
        if (preferExternal && MEDIA_MOUNTED.equals(externalStorageState) && hasExternalStoragePermission(context)) {
            appCacheDir = getExternalCacheDir(context);
        }
        if (appCacheDir == null) {
            appCacheDir = context.getCacheDir();
        }
        if (appCacheDir == null) {
            String cacheDirPath = "/data/data/" + context.getPackageName() + "/cache/";
            Logger.w("cache", String.format("Can't define system cache directory! trying to use '%s'", cacheDirPath));
            appCacheDir = new File(cacheDirPath);
        }
        if (appCacheDir != null && !appCacheDir.exists()) {
            appCacheDir.mkdirs();
        }
        if (appCacheDir == null || !appCacheDir.exists()) {
            return null;
        }
        return appCacheDir;
    }

    /**
     * Returns individual application cache directory (for only image caching from ImageLoader). Cache directory will be
     * created on SD card <i>("/Android/data/[app_package_name]/cache/uil-images")</i> if card is mounted and app has
     * appropriate permission. Else - Android defines cache directory on device's file system.
     *
     * @param context Application context
     * @return Cache {@link File directory}
     */
    public static File getIndividualCacheDirectory(Context context) {
        File cacheDir = getCacheDirectory(context);
        File individualCacheDir = new File(cacheDir, INDIVIDUAL_DIR_NAME);
        if (!individualCacheDir.exists()) {
            if (!individualCacheDir.mkdir()) {
                individualCacheDir = cacheDir;
            }
        }
        return individualCacheDir;
    }

    /**
     * Returns specified application cache directory. Cache directory will be created on SD card by defined path if card
     * is mounted and app has appropriate permission. Else - Android defines cache directory on device's file system.
     *
     * @param context  Application context
     * @param cacheDir Cache directory path (e.g.: "AppCacheDir", "AppDir/cache/images")
     * @return Cache {@link File directory}
     */
    public static File getOwnCacheDirectory(Context context, String cacheDir) {
        File appCacheDir = null;
        if (MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) && hasExternalStoragePermission(context)) {
            appCacheDir = new File(Environment.getExternalStorageDirectory(), cacheDir);
        }
        if (appCacheDir == null || (!appCacheDir.exists() && !appCacheDir.mkdirs())) {
            appCacheDir = context.getCacheDir();
        }

        return appCacheDir;
    }

    private static File getExternalCacheDir(Context context) {
        File dataDir = new File(new File(Environment.getExternalStorageDirectory(), "Android"), "data");
        File appCacheDir = new File(new File(dataDir, context.getPackageName()), "cache");
        if (!appCacheDir.exists()) {
            if (!appCacheDir.mkdirs()) {
                Logger.w("cache", "Unable to create external cache directory");
                return null;
            }
            try {
                new File(appCacheDir, ".nomedia").createNewFile();
            } catch (IOException e) {
                Logger.i("cache", "Can't create \".nomedia\" file in application external cache directory");
            }
        }
        return appCacheDir;
    }

    public static boolean hasExternalStoragePermission(Context context) {
        int perm = context.checkCallingOrSelfPermission(EXTERNAL_STORAGE_PERMISSION);
        return perm == PackageManager.PERMISSION_GRANTED;
    }


    /**
     * 获取升级存储卡的file
     */
    private static File getCoreExternalCacheDir(Context context) {
        File dataDir = new File(new File(Environment.getExternalStorageDirectory(), "Android"), "data");
        File appCacheDir = new File(new File(dataDir, "core"), "cache");
        if (!appCacheDir.exists()) {
            if (!appCacheDir.mkdirs()) {
                Logger.w(TAG, "Unable to create external cache directory");
                return null;
            }
            try {
                new File(appCacheDir, ".nomedia").createNewFile();
            } catch (IOException e) {
                Logger.i(TAG, "Can't create \".nomedia\" file in application external cache directory");
            }
        }
        return appCacheDir;
    }

    /**
     * 配合 filerovider基础模块获取file
     */
    public static File getCoreCacheDirectory(Context context) {
        File appCacheDir = null;
        if (DataUtils.isExternalStorageMounted() && DataUtils.hasExternalStoragePermission(context)) {
            appCacheDir = getCoreExternalCacheDir(context);
        }
        if (appCacheDir == null) {
            appCacheDir = context.getCacheDir();
        }
        if (appCacheDir == null) {
            Logger.w(TAG, "Can't define system cache directory! The app should be re-installed.");
        }
        return appCacheDir;
    }


    public static class Pair<LEFT, RIGHT> {

        public LEFT left;
        public RIGHT right;

        public Pair() {
        }

        public Pair(LEFT left, RIGHT right) {
            this.left = left;
            this.right = right;
        }
    }
}
