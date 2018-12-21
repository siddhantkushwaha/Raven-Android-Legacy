package com.siddhantkushwaha.raven.commonUtility;

import android.graphics.Bitmap;
import android.support.v7.graphics.Palette;

public class PaletteUtils {

    public static void createPalette(Bitmap bitmap, Palette.PaletteAsyncListener paletteAsyncListener) {

        Palette.from(bitmap).generate(paletteAsyncListener);
    }
}
