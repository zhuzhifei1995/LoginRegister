package com.test.chat.view.view;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.M)
public class ViewCompat {

    public static void postOnAnimation(View view, Runnable runnable) {
        SDK16.postOnAnimation(view, runnable);
    }

    public static void setBackground(View view, Drawable background) {
        SDK16.setBackground(view, background);
    }

    public static void setLayerType(View view, int layerType) {
        SDK11.setLayerType(view, layerType);
    }

    static class SDK11 {

        public static void setLayerType(View view, int layerType) {
            view.setLayerType(layerType, null);
        }
    }

    static class SDK16 {

        public static void postOnAnimation(View view, Runnable runnable) {
            view.postOnAnimation(runnable);
        }

        public static void setBackground(View view, Drawable background) {
            view.setBackground(background);
        }

    }

}
