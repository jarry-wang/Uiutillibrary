package cn.join.android.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


/**
 * 解决图片OOM
 * Created by YHB on 2016/11/16.
 */
public class ImgOomUtils {

    public static final int MAX_IMG_KB = 300;
    public static final int MAX_IMG_COUNT = 9;

    public static Bitmap getBitmap(String path) {
        Bitmap bitmap = null;
        try {
            if (FileSizeUtil.getFileOrFilesSize(path, 2) > 1000) { //如果图片大于1000K,则进行缩放
                bitmap = scaleImg(path);
            } else { //小于1000K,则不进行缩放
                bitmap = localImg2Bitmap(path);
            }
        } catch (Throwable e) {

        }
        return bitmap;
    }

    /**
     * 不进行缩放
     *
     * @param path
     * @return
     */
    public static Bitmap localImg2Bitmap(String path) {
        Bitmap bitmap;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inJustDecodeBounds = false;
        options.inSampleSize = 1;
        try {
            bitmap = BitmapFactory.decodeFile(path, options);
        } catch (Throwable e) {
            bitmap = scaleImg(path);
        }
        return bitmap;
    }

    /**
     * 同步缩放图片
     *
     * @param path
     */
    public static Bitmap scaleImg(String path) {
        Bitmap bitmap;
        int w = 1280;
        int h = 720;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        int width = options.outWidth;
        int height = options.outHeight;
        int scaleWidth = 0, scaleHeight = 0;
        if (width > w || height > h) {
            // 缩放
            scaleWidth = width / w;
            scaleHeight = height / h;
        }
        options.inJustDecodeBounds = false;
        int scale = (int) Math.max(scaleWidth, scaleHeight);
        if (scale <= 0) {
            scale = 1;
        }
        options.inJustDecodeBounds = false;
        options.inSampleSize = scale;
        bitmap = BitmapFactory.decodeFile(path, options);
        return bitmap;
    }

    /**
     * 将图片转为Base64
     *
     * @param bitmap
     * @return
     */
    public static String bitmapToBase64(Bitmap bitmap) {
        String imgBase64Str = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                int options = 80;
                bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
                while (baos.toByteArray().length / 1024 > MAX_IMG_KB) {  //循环判断如果压缩后图片是否大于100kb,大于继续压缩
                    baos.reset();//重置baos即清空baos
                    options -= 10;
                    if (options <= 0) {
                        options = 10;
                        break;
                    }
                    bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
                }
                byte[] bitmapBytes = baos.toByteArray();
                imgBase64Str = android.util.Base64.encodeToString(bitmapBytes, android.util.Base64.DEFAULT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return imgBase64Str;
    }
}
