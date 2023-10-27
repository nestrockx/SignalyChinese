package com.wegielek.signalychinese.utils;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.widget.PopupMenu;

import com.wegielek.signalychinese.R;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class Utils {

    public static boolean containsChinese(String input) {
        return input.matches(".*[\\u4e00-\\u9fff]+.*");
    }

    public static Paint getPaint(int color, float brushSize, boolean dashed) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setXfermode(null);
        paint.setAlpha(0xff);

        paint.setColor(color);
        paint.setStrokeWidth(brushSize);

        if (dashed) {
            paint.setPathEffect(new DashPathEffect(new float[]{10, 10}, 5f));
        }

        return paint;
    }

    public static int dpToPixels(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int getScreenWidth(Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    public static int getScreenHeight(Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    public static boolean isScreenRotated(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    public static boolean copyToClipboard(Context context, String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", text);
        clipboard.setPrimaryClip(clip);
        return true;
    }

    public static void showPopup(View v, String text, String languageFrom, String languageTo, TextToSpeech tts) {
        PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.copy) {
                String tmpText = "".concat(text);
                if (text.split("/").length > 1) {
                    tmpText = text.split("/")[0];
                }
                if (copyToClipboard(v.getContext(), tmpText)) {
                    Toast.makeText(v.getContext(), "Successfully copied", Toast.LENGTH_SHORT).show();
                }
                return true;
            } else if (item.getItemId() == R.id.search) {
                String tmpText = "".concat(text);
                if (text.split("/").length > 1) {
                    tmpText = text.split("/")[0];
                }
                Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                intent.putExtra(SearchManager.QUERY, tmpText);
                v.getContext().startActivity(intent);
                return true;
            } else if (item.getItemId() == R.id.translate) {
                String tmpText = "".concat(text);
                if (text.split("/").length > 1) {
                    tmpText = text.split("/")[1];
                }
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://translate.google.com/?" + "sl=" + languageFrom + "&" + "op=translate&" + "tl=" + languageTo + "&" + "text=" + tmpText));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                v.getContext().startActivity(intent);
                return true;
            } else if (item.getItemId() == R.id.speak) {
                String tmpText = "".concat(text);
                if (text.split("/").length > 1) {
                    tmpText = text.split("/")[0];
                }
                tts.speak(tmpText, TextToSpeech.QUEUE_FLUSH, null, null);
            }
            return false;
        });
        popupMenu.inflate(R.menu.popup_menu);

        try {
            Field popup = PopupMenu.class.getDeclaredField("mPopup");
            popup.setAccessible(true);
            var menu = popup.get(popupMenu);
            if (menu != null) {
                menu.getClass().getDeclaredMethod("setForceShowIcon", boolean.class)
                        .invoke(menu, true);
            }
        } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
            e.printStackTrace();
            Log.e("LOG_TAG", "Error:" + e.getMessage());
        } finally {
            popupMenu.show();
        }
    }

}
