/*  Permission Group	                        Permissions
 *  android.permission-group.CALENDAR           android.permission.READ_CALENDAR
 *                                              android.permission.WRITE_CALENDAR
 *
 *  android.permission-group.CAMERA             android.permission.CAMERA
 *
 *  android.permission-group.CONTACTS           android.permission.READ_CONTACTS
 *                                              android.permission.WRITE_CONTACTS
 *                                              android.permission.GET_ACCOUNTS
 *
 *  android.permission-group.LOCATION           android.permission.ACCESS_FINE_LOCATION
 *                                              android.permission.ACCESS_COARSE_LOCATION
 *
 *  android.permission-group.MICROPHONE         android.permission.RECORD_AUDIO
 *
 *  android.permission-group.PHONE              android.permission.READ_PHONE_STATE
 *                                              android.permission.CALL_PHONE
 *                                              android.permission.READ_CALL_LOG
 *                                              android.permission.WRITE_CALL_LOG
 *                                              com.android.voicemail.permission.ADD_VOICEMAIL
 *                                              android.permission.USE_SIP
 *                                              android.permission.PROCESS_OUTGOING_CALLS
 *
 *  android.permission-group.SENSORS            android.permission.BODY_SENSORS
 *  android.permission-group.SMS                android.permission.SEND_SMS
 *                                              android.permission.RECEIVE_SMS
 *                                              android.permission.READ_SMS
 *                                              android.permission.RECEIVE_WAP_PUSH
 *                                              android.permission.RECEIVE_MMS
 *                                              android.permission.READ_CELL_BROADCASTS
 *
 *  android.permission-group.STORAGE            android.permission.READ_EXTERNAL_STORAGE
 *                                              android.permission.WRITE_EXTERNAL_STORAGE
 */
package cn.pro47x.core.utils;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import cn.pro47x.core.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 版权：XXX公司 版权所有
 * 作者：Jacky Yu
 * 版本：1.0
 * 创建日期：2017/1/3
 * 描述：运行时权限校验工具类
 * 修订历史：
 */
public class PermissionCheck {
    // 存储相关权限
    public static final int REQUEST_CODE_STORAGE = 0xAAA1;
    // 电话相关权限
    public static final int REQUEST_CODE_PHONE = 0xAAA2;
    // 申请所有授权
    public static final int REQUEST_CODE_ALL = 0xAAAA;

    public static final int REQUEST_CODE_CAMERA = 0x110;

    private PermissionCheck() {
    }

    public static PermissionCheck getInstance() {
        return PermissionCheckInstance.instance;
    }


    private static class PermissionCheckInstance {
        static PermissionCheck instance = new PermissionCheck();
    }

    /**
     * 申请权限
     *
     * @param activity    activity
     * @param permissions 权限列表
     *                    <p/>
     *                    Manifest.permission.WRITE_EXTERNAL_STORAGE
     *                    Manifest.permission.CALL_PHONE
     *                    Manifest.permission.READ_SMS
     *                    Manifest.permission.RECEIVE_SMS
     */
    @TargetApi(Build.VERSION_CODES.M)
    public void askForPermissions(Activity activity, List<String> permissions, int requestCode) {
        // 如果不是android6.0以上的系统，则不需要检查是否已经获取授权
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        List<String> delPermissions = new ArrayList<String>();
        for (String permission : permissions) {
            // PackageManager.PERMISSION_GRANTED    授予权限
            // PackageManager.PERMISSION_DENIED     没有权限
            // 如果已经授予该权限，则从添加到删除list中
            if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED) {
                delPermissions.add(permission);
            }
        }
        if (MiscUtils.isNotEmpty(delPermissions)) {
            permissions.removeAll(delPermissions);
        }
        if (MiscUtils.isNotEmpty(permissions)) {
            activity.requestPermissions(permissions.toArray(new String[permissions.size()]), requestCode);
        }
    }

    public void askForPermissions(Activity activity, String[] permissions, int requestCode) {
        if (null == permissions || permissions.length <= 0) {
            return;
        }
        List<String> list = new ArrayList<>();
        Collections.addAll(list, permissions);
        askForPermissions(activity, list, requestCode);
    }

    /**
     * 校验权限
     *
     * @param context    context
     * @param permission 需要校验的权限
     * @return 是否授予该权限
     * true - 授予
     * false - 还未授予
     */
    public boolean checkPermission(Context context, String permission) {
        // 如果不是android6.0以上的系统，则不需要检查是否已经获取授权
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        int verify = PermissionChecker.checkSelfPermission(context, permission);
//        int verify = ContextCompat.checkSelfPermission(context, permission);
        // PackageManager.PERMISSION_GRANTED    授予权限
        // PackageManager.PERMISSION_DENIED     没有权限
        return verify == PermissionChecker.PERMISSION_GRANTED;
//        return verify == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 校验权限
     *
     * @param context     context
     * @param permissions 需要校验的权限
     * @return 是否授予该权限
     * true - 授予
     * false - 还未授予
     */
    public boolean checkPermission(Context context, String[] permissions) {
        // 如果不是android6.0以上的系统，则不需要检查是否已经获取授权
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        List<String> list = new ArrayList<>();
        Collections.addAll(list, permissions);
        for (String permission : list) {
            int verify = ContextCompat.checkSelfPermission(context, permission);
            // PackageManager.PERMISSION_GRANTED    授予权限
            // PackageManager.PERMISSION_DENIED     没有权限
            if (verify != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * 有权限后，初始化操作
     */
    private void initApp(int requestCode) {
        if (requestCode == REQUEST_CODE_STORAGE || requestCode == REQUEST_CODE_ALL) {
            File filePath = new File(DataUtils.getSDCardAppPath());
            if (!filePath.exists()) {
                filePath.mkdirs();
            }
        }
    }

    /**
     * 是否需要对没授权，且被拒绝过一次AND不再提醒的重要权限进行二次申请
     *
     * @param activity    activity
     * @param permissions 权限列表
     * @return 是否需要
     * true - 需要再次申请
     * false - 不需要再次申请
     */
    private boolean needAskAgainForPermissions(Activity activity, String[] permissions) {
        for (String permission : permissions) {
            switch (permission) {
                // 与APP稳定性、体验等相关的重要的运行时权限，进行提示
                case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                case Manifest.permission_group.LOCATION:
                    // 是否可以弹出一个解释申请该权限的提示给用户，如果为true，则可以弹
                    if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED
                            && !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                        // false，则自己弹出提示，也可以Intent到系统的APP setting界面
                        showAskDialog(activity, R.string.permission_name_location);
                        return true;
                    }

                default:
                    break;
            }
        }
        return false;
    }

    public void showContactAskDialog(final Activity activity, int resId) {
//        new PermissionRequestDialog.Builder(activity).setTxtId(resId).setListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                try {
//                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                    intent.setData(Uri.parse("package:" + AppConfig.getContext().getPackageName()));
//                    activity.startActivity(intent);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }).creater().show();
    }

    /**
     * 显示提示dialog
     */
    private void showAskDialog(final Activity activity, int resId) {
//        new CustomDialog.Builder(activity).setMessage(resId)
//                .setNegativeButton(R.string.cancel, null)
//                .setPositiveButton(R.string.permission_settings, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        // 去设置中设置权限
//                        try {
//                            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                            intent.setData(Uri.parse("package:" + AppConfig.getContext().getPackageName()));
//                            activity.startActivity(intent);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }).create().show();
    }
}
