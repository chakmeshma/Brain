package net.chakmeshma.brutengine.utilities;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * Created by chakmeshma on 17.05.2017.
 */

public final class GeneralUtilities {

    public static int pxToDp(Context context, int px) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}
