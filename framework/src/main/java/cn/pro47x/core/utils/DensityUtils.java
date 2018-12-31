package cn.pro47x.core.utils;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.TypedValue;

/**
 * 单位转换类
 *
 * @author lisiqi
 * @date 2018-12-28 14:15:43
 */
@SuppressWarnings("unused")
public class DensityUtils {

    public static DisplayMetrics getMetrics() {
        return Resources.getSystem().getDisplayMetrics();
    }

    /**
     * dp转px
     */
    public static int dp2px(float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal, getMetrics());
    }

    /**
     * sp转px
     */
    public static int sp2px(float spVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spVal, getMetrics());
    }

    /**
     * 特殊处理
     * 降scaledDensity
     */
    public static int sp2px(float spVal, boolean low) {
        if (!low) {
            return sp2px(spVal);
        } else {
            DisplayMetrics metrics = getMetrics();
            if (metrics.scaledDensity > 4) {
                return (int) spVal * 3;
            } else {
                return sp2px(spVal);
            }
        }
    }


    /**
     * px转dp
     */
    public static float px2dp(float pxVal) {
        final float scale = getMetrics().density;
        return (pxVal / scale);
    }

    /**
     * px转sp
     */
    public static float px2sp(float pxVal) {
        return (pxVal / getMetrics().scaledDensity);
    }

    /**
     * 把dip转换为px单位
     **/
    public static int getPxByDip(int dip) {
        DisplayMetrics dm = getMetrics();
        return (int) (dip * dm.density + 0.5f);
    }

    /**
     * 把dip转换为px单位
     */
    public static float getPxByDip(float dp) {
        DisplayMetrics dm = getMetrics();
        return dp * dm.density;
    }

    /**
     * 把dip转换为px单位
     **/
    public static int getPxBySp(int sp) {
        DisplayMetrics dm = getMetrics();
        return (int) (sp * dm.scaledDensity);
    }

    /**
     * 获取屏幕宽度
     */
    public static float getWidth() {
        return getMetrics().widthPixels;
    }

    /**
     * 获取屏幕高度
     */
    public static float getHeight() {
        return getMetrics().heightPixels;
    }

    /**
     * 获取手机屏幕相关参数
     */
    public static Rect getAppRect(Activity activity) {
        Rect appRect = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(appRect); // 应用区域
        return appRect;
    }

}
