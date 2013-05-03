/**
 * SakuraGroup - Package: net.syamn.sakuragroup.utils.plugin Created: 2012/10/16
 * 3:17:13
 */
package net.syamn.sakuragroup.utils.plugin;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Util (Util.java)
 * 
 * @author syam(syamn)
 */
public class PUtil {
    /**
     * Unix秒を yy/MM/dd HH:mm:ss フォーマットにして返す
     * 
     * @param unixSec
     *            Unix秒
     * @return yy/MM/dd HH:mm:ss
     */
    public static String getDispTimeByUnixTime(long unixSec) {
        SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
        return sdf.format(new Date(unixSec * 1000));
    }

    /**
     * 秒の差を読みやすい時間表記にして返す
     * 
     * @param before
     *            beforeSec
     * @param after
     *            afterSec
     * @return string
     */
    public static String getDiffString(Long before, Long after) {
        boolean minus = false;
        long diffSec = after - before;
        if (diffSec == 0) {
            return "0秒";
        } else if (diffSec < 0) {
            minus = true;
            diffSec = -diffSec;
        }
        String ret = "";

        final int SEC = 1;
        final int MIN = SEC * 60;
        final int HOUR = MIN * 60;
        final int DAY = HOUR * 24;

        if ((diffSec / DAY) >= 1) {
            ret += diffSec / DAY + "日";
            diffSec = diffSec - ((diffSec / DAY) * DAY);
        }
        if ((diffSec / HOUR) >= 1) {
            ret += diffSec / HOUR + "時間";
            diffSec = diffSec - ((diffSec / HOUR) * HOUR);
        }
        if ((diffSec / MIN) >= 1) {
            ret += diffSec / MIN + "分";
            diffSec = diffSec - ((diffSec / MIN) * MIN);
        }
        if ((diffSec / SEC) >= 1) {
            ret += diffSec / SEC + "秒";
            diffSec = diffSec - ((diffSec / SEC) * SEC);
        }

        if (minus) {
            ret = "-" + ret;
        }
        return ret;
    }

    /**
     * 文字列からCalendarクラスの単位数値を返す
     * 
     * @param str
     *            文字列
     * @return 対応する数値 または変換出来ない場合 -1
     */
    public static int getMeasure(String str) {
        int measure = 0;

        if (str.equalsIgnoreCase("SECOND")) {
            measure = Calendar.SECOND;
        } else if (str.equalsIgnoreCase("MINUTE")) {
            measure = Calendar.MINUTE;
        } else if (str.equalsIgnoreCase("HOUR")) {
            measure = Calendar.HOUR;
        } else if (str.equalsIgnoreCase("DAY")) {
            measure = Calendar.DAY_OF_MONTH;
        } else if (str.equalsIgnoreCase("WEEK")) {
            measure = Calendar.WEEK_OF_MONTH;
        } else if (str.equalsIgnoreCase("MONTH")) {
            measure = Calendar.MONTH;
        } else if (str.equalsIgnoreCase("YEAR")) {
            measure = Calendar.YEAR;
        } else {
            measure = -1;
        }

        return measure;
    }
}