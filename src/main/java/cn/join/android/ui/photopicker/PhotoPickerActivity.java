package cn.join.android.ui.photopicker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.join.android.R;
import cn.join.android.ui.photopicker.entity.Photo;
import cn.join.android.ui.photopicker.event.OnItemCheckListener;
import cn.join.android.ui.photopicker.fragment.ImagePagerFragment;
import cn.join.android.ui.photopicker.fragment.PhotoPickerFragment;
import cn.join.android.ui.photopicker.widget.Titlebar;

import static cn.join.android.ui.photopicker.PhotoPicker.DEFAULT_COLUMN_NUMBER;
import static cn.join.android.ui.photopicker.PhotoPicker.DEFAULT_MAX_COUNT;
import static cn.join.android.ui.photopicker.PhotoPicker.EXTRA_GRID_COLUMN;
import static cn.join.android.ui.photopicker.PhotoPicker.EXTRA_MAX_COUNT;
import static cn.join.android.ui.photopicker.PhotoPicker.EXTRA_ORIGINAL_PHOTOS;
import static cn.join.android.ui.photopicker.PhotoPicker.EXTRA_PREVIEW_ENABLED;
import static cn.join.android.ui.photopicker.PhotoPicker.EXTRA_SHOW_CAMERA;
import static cn.join.android.ui.photopicker.PhotoPicker.EXTRA_SHOW_GIF;
import static cn.join.android.ui.photopicker.PhotoPicker.KEY_SELECTED_PHOTOS;

public class PhotoPickerActivity extends AppCompatActivity {

    private PhotoPickerFragment pickerFragment;
    private ImagePagerFragment imagePagerFragment;
    //private MenuItem menuDoneItem;

    private int maxCount = DEFAULT_MAX_COUNT;

    /**
     * to prevent multiple calls to inflate menu
     */
    // private boolean menuIsInflated = false;

    private boolean showGif = false;
    private int columnNumber = DEFAULT_COLUMN_NUMBER;
    private ArrayList<String> originalPhotos = null;

    private Titlebar titlebar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean showCamera = getIntent().getBooleanExtra(EXTRA_SHOW_CAMERA, true);
        boolean showGif = getIntent().getBooleanExtra(EXTRA_SHOW_GIF, false);
        boolean previewEnabled = getIntent().getBooleanExtra(EXTRA_PREVIEW_ENABLED, true);

        setShowGif(showGif);

        setContentView(R.layout.__picker_activity_photo_picker);

        titlebar = (Titlebar) findViewById(R.id.titlebar);
        titlebar.init(this);

   /* Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(mToolbar);
    setTitle("");//去掉原生的标题

    //将原生的返回图标换掉
    mToolbar.setNavigationIcon(R.drawable.__picker_delete);
    mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        PhotoPickerActivity.this.finish();
      }
    });



    ActionBar actionBar = getSupportActionBar();

    assert actionBar != null;
    actionBar.setDisplayHomeAsUpEnabled(true);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      actionBar.setElevation(25);
    }*/

        maxCount = getIntent().getIntExtra(EXTRA_MAX_COUNT, DEFAULT_MAX_COUNT);
        columnNumber = getIntent().getIntExtra(EXTRA_GRID_COLUMN, DEFAULT_COLUMN_NUMBER);
        originalPhotos = getIntent().getStringArrayListExtra(EXTRA_ORIGINAL_PHOTOS);

        pickerFragment = (PhotoPickerFragment) getSupportFragmentManager().findFragmentByTag("tag");
        if (pickerFragment == null) {
            pickerFragment = PhotoPickerFragment
                    .newInstance(showCamera, showGif, previewEnabled, columnNumber, maxCount, originalPhotos);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, pickerFragment, "tag")
                    .commit();
            getSupportFragmentManager().executePendingTransactions();
        }

        //右边的点击事件
        titlebar.getTvRight().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> photos = pickerFragment.getPhotoGridAdapter().getSelectedPhotoPaths();
                if (photos != null && photos.size() > 0) {
                    Intent intent = new Intent();
                    intent.putStringArrayListExtra(KEY_SELECTED_PHOTOS, photos);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "还没有选择图片", Toast.LENGTH_SHORT).show();
                }
            }
        });

        pickerFragment.getPhotoGridAdapter().setOnItemCheckListener(new OnItemCheckListener() {
            @Override
            public boolean OnItemCheck(int position, Photo photo, final boolean isCheck, int selectedItemCount) {

                int total = selectedItemCount + (isCheck ? -1 : 1);

                if (maxCount <= 1) {
                    List<Photo> photos = pickerFragment.getPhotoGridAdapter().getSelectedPhotos();
                    if (!photos.contains(photo)) {
                        photos.clear();
                        pickerFragment.getPhotoGridAdapter().notifyDataSetChanged();
                    }
                    return true;
                }

                if (total > maxCount) {
                    Toast.makeText(getActivity(), getString(R.string.__picker_over_max_count_tips, maxCount), Toast.LENGTH_LONG).show();
                    return false;
                }
                updateRightText(total);
                return true;
            }
        });

    }

    /**
     * 更新标题右边的文案
     *
     * @param total
     */
    private void updateRightText(int total) {
        titlebar.getTvRight().setText(getString(R.string.__picker_done_with_count, total, maxCount));
    }


    /**
     * Overriding this method allows us to run our exit animation first, then exiting
     * the activity when it complete.
     */
    @Override
    public void onBackPressed() {
        if (imagePagerFragment != null && imagePagerFragment.isVisible()) {
            imagePagerFragment.runExitAnimation(new Runnable() {
                public void run() {
                    if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                        getSupportFragmentManager().popBackStack();
                    }
                }
            });
        } else {
            super.onBackPressed();
        }
    }


    public void addImagePagerFragment(ImagePagerFragment imagePagerFragment) {
        this.imagePagerFragment = imagePagerFragment;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, this.imagePagerFragment)
                .addToBackStack(null)
                .commit();
    }

    public PhotoPickerActivity getActivity() {
        return this;
    }

    public boolean isShowGif() {
        return showGif;
    }

    public void setShowGif(boolean showGif) {
        this.showGif = showGif;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PhotoPreview.REQUEST_CODE && resultCode == RESULT_OK) { //预览点击了完成
            //ArrayList<String> photos = pickerFragment.getPhotoGridAdapter().getSelectedPhotoPaths();
            ArrayList<String> photos = data.getStringArrayListExtra(KEY_SELECTED_PHOTOS);
            if (photos != null && photos.size() > 0) {
                Intent intent = new Intent();
                intent.putStringArrayListExtra(KEY_SELECTED_PHOTOS, photos);
                getActivity().setResult(RESULT_OK, intent);
                getActivity().finish();
            } else {
                Toast.makeText(this, "还没有选择图片", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PhotoPreview.REQUEST_CODE && resultCode == RESULT_CANCELED) { //点击了返回
            ArrayList<String> paths = data.getStringArrayListExtra(KEY_SELECTED_PHOTOS);
            ArrayList<Photo> selectPhotos = new ArrayList<>();
            for (String path : paths) {
                selectPhotos.add(new Photo(path));
            }
            pickerFragment.getPhotoGridAdapter().setSelectedPhotos(selectPhotos);
            pickerFragment.getPhotoGridAdapter().notifyDataSetChanged();
            updateRightText(selectPhotos.size());
        }
    }
}
