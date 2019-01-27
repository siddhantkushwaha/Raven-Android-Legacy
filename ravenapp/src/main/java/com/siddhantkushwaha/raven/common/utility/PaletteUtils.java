package com.siddhantkushwaha.raven.common.utility;

import android.graphics.Bitmap;

import androidx.palette.graphics.Palette;

public class PaletteUtils {

    public static void createPalette(Bitmap bitmap, Palette.PaletteAsyncListener paletteAsyncListener) {

        Palette.from(bitmap).generate(paletteAsyncListener);
    }
}
