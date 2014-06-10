package org.solarex.test3g4g;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.os.SystemProperties;

import java.util.Calendar;

public class Utils {
    private static final String TAG = "Test3G4G";
    private Utils(){
    }
    
    public static String getActiveSubscriberId(Context context)
    {
        final TelephonyManager tele = TelephonyManager.from(context);
        final String actualSubscriberId = tele.getSubscriberId();
        String subscreberId = SystemProperties.get("test.subscriberid", actualSubscriberId);
        log("getActiveSubscriberId subscreberId:" + subscreberId);
        return subscreberId;
    }

    public static void log(String msg){
        Log.d(TAG, msg != null ? msg : "null");
    }
    
    public static long computeMonthBeginTime(){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, cal.getMinimum(Calendar.DAY_OF_MONTH));
        return cal.getTimeInMillis();
    }
    
    public static String formatMB(long bytes){
        log("bytes = " + bytes);
        float fBytes = (float)bytes;
        String suffix = "B";
        int i = 0;
        while (!"G".equals(suffix) && fBytes >= 1024) {
            i++;
            fBytes/=1024;
            switch (i) {
                case 1:
                    suffix = "K";
                    break;
                case 2:
                    suffix = "M";
                    break;
                case 3:
                    suffix = "G";
                    break;
                default:
                    log("I wont handle this situation");
                    break;
            }
        }
        String value = String.format("%.2f", fBytes);
        return value + " " + suffix;
    }
}
