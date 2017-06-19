package cn.join.android.ui.photopicker;

import android.app.Activity;
import android.content.Intent;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/8/5 0005.
 */
public class PhotoPickUtils {

    public static void onActivityResult(int requestCode, int resultCode, Intent data, PickHandler pickHandler) {
        if (resultCode == Activity.RESULT_OK || resultCode == Activity.RESULT_CANCELED) { // RESULT_CANCELED 表示是在预览的时候,点击了返回
            if (requestCode == PhotoPicker.REQUEST_CODE) {//第一次，选择图片后返回
                if (data != null) {
                    ArrayList<String> photos = data.getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS);
                    pickHandler.onPickSuccess(photos);
                } else {
                    pickHandler.onPickFail("选择图片失败");
                }
            } else if (requestCode == PhotoPreview.REQUEST_CODE) {//如果是预览与删除后返回
                if (data != null) {
                    ArrayList<String> photos = data.getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS);
                    pickHandler.onPreviewBack(photos);
                } else {
                    pickHandler.onPickFail("选择图片失败");
                }
            }
        } else {
            if (requestCode == PhotoPicker.REQUEST_CODE) {
                pickHandler.onPickCancle();
            }
        }
    }

    public static void startPick(Activity context, ArrayList<String> photos, int maxCount) {
        PhotoPicker.builder()
                .setPhotoCount(maxCount)
                .setShowCamera(true)
                .setShowGif(true)
                .setSelected(photos)
                .setPreviewEnabled(true)
                .start(context, PhotoPicker.REQUEST_CODE);
    }


    public interface PickHandler {
        void onPickSuccess(ArrayList<String> photos);

        void onPreviewBack(ArrayList<String> photos);

        void onPickFail(String error);

        void onPickCancle();
    }
}
