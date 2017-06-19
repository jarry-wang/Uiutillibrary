package cn.join.android.ui.photopicker;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import cn.join.android.R;
import cn.join.android.ui.photopicker.fragment.ImagePagerFragment;
import cn.join.android.ui.photopicker.interfac.IfdoInterface;
import cn.join.android.ui.photopicker.widget.MultiPickResultView;
import cn.join.android.ui.photopicker.widget.Titlebar;

import static cn.join.android.ui.photopicker.PhotoPicker.DEFAULT_MAX_COUNT;
import static cn.join.android.ui.photopicker.PhotoPicker.EXTRA_MAX_COUNT;
import static cn.join.android.ui.photopicker.PhotoPicker.KEY_SELECTED_PHOTOS;
import static cn.join.android.ui.photopicker.PhotoPreview.EXTRA_ACTION;
import static cn.join.android.ui.photopicker.PhotoPreview.EXTRA_CURRENT_ITEM;
import static cn.join.android.ui.photopicker.PhotoPreview.EXTRA_PHOTOS;
import static cn.join.android.ui.photopicker.PhotoPreview.EXTRA_SELECTED_PHOTOS;
import static cn.join.android.ui.photopicker.PhotoPreview.EXTRA_SHOW_DELETE;

/**
 * 预览照片
 * Created by donglua on 15/6/24.
 */
public class PhotoPagerActivity extends AppCompatActivity {

    private ImagePagerFragment pagerFragment;

    //private ActionBar actionBar;
    private boolean showDelete;
    private Titlebar titlebar;
    public Dialog ifDoDialog;
    public TextView ifdoConfirm, ifdoCancel;

    private int maxCount = DEFAULT_MAX_COUNT;
    private ImageView vSelected;
    private ArrayList<String> paths;
    private ArrayList<String> selectedPaths;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.__picker_activity_photo_pager);

        int currentItem = getIntent().getIntExtra(EXTRA_CURRENT_ITEM, 0);
        //获得所有的照片
        paths = getIntent().getStringArrayListExtra(EXTRA_PHOTOS);

        //获得选中的照片
        selectedPaths = getIntent().getStringArrayListExtra(EXTRA_SELECTED_PHOTOS);

        //showDelete已经废弃
        showDelete = getIntent().getBooleanExtra(EXTRA_SHOW_DELETE, true);

        int action = getIntent().getIntExtra(EXTRA_ACTION, MultiPickResultView.ACTION_ONLY_SHOW);
        maxCount = getIntent().getIntExtra(EXTRA_MAX_COUNT, DEFAULT_MAX_COUNT);

        if (pagerFragment == null) {
            pagerFragment =
                    (ImagePagerFragment) getSupportFragmentManager().findFragmentById(R.id.photoPagerFragment);
        }
        pagerFragment.setPhotos(paths, currentItem);
        titlebar = (Titlebar) findViewById(R.id.titlebar);
        titlebar.init(this);

        //是否已经选择的组件
        vSelected = (ImageView) findViewById(R.id.v_selected);

        if (action == MultiPickResultView.ACTION_SELECT) {
            updateRight();
            vSelected.setVisibility(View.VISIBLE);
        } else if (action == MultiPickResultView.ACTION_PREVIEW) {
            titlebar.setRitht(getApplicationContext().getResources().getDrawable(R.drawable.__picker_delete), "", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    doDeletePhoto();
                }
            });
            vSelected.setVisibility(View.GONE);
        } else {
            vSelected.setVisibility(View.GONE);
        }

        //点击返回
        titlebar.setLeftOnclickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra(KEY_SELECTED_PHOTOS, selectedPaths);
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });
        titlebar.setTitle(getString(R.string.__picker_preview));

        pagerFragment.getViewPager().addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                updateSelectedState(position);
                titlebar.setTitle(getString(R.string.__picker_image_index, pagerFragment.getViewPager().getCurrentItem() + 1,
                        pagerFragment.getPaths().size()));
            }
        });

        //是否选择的按钮
        vSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //获取当前点击的图片的位置
                int pos = pagerFragment.getViewPager().getCurrentItem();
                String path = paths.get(pos);
                if (isSelected(paths.get(pos))) { //判断是否已经选中, 如果已经选中,则移除
                    selectedPaths.remove(path);
                } else { //未选中则添加
                    if (selectedPaths.size() >= maxCount) {
                        Toast.makeText(PhotoPagerActivity.this, "已选了" + maxCount + "张图片", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    selectedPaths.add(path);
                }
                vSelected.setSelected(isSelected(path));
                updateRight();
            }
        });
    }

    private void updateRight() {
        titlebar.setRitht(null, getString(R.string.__picker_done_with_count, selectedPaths.size(), maxCount), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void updateSelectedState(int position) {
        String path = paths.get(position);
        boolean selected = isSelected(path);
        vSelected.setSelected(selected);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (showDelete) {
        }
        return true;
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(KEY_SELECTED_PHOTOS, selectedPaths);
        setResult(RESULT_OK, intent);
        finish();
        super.onBackPressed();
    }

    /**
     * 删除图片功能(只在从现实组件中点击以选择的图片过来才有)
     */
    private void doDeletePhoto() {
        final int index = pagerFragment.getViewPager().getCurrentItem();
        //在图片选择页面中的index
        final String deletedPath = pagerFragment.getPaths().get(index);

        //在底部提示
        Snackbar snackbar = Snackbar.make(pagerFragment.getView(), R.string.__picker_deleted_a_photo,
                Snackbar.LENGTH_LONG);

        if (pagerFragment.getPaths().size() <= 1) {
            showIfdoDialogCb("提示", getString(R.string.__picker_confirm_to_delete), new IfdoInterface() {
                @Override
                public void doConfirm() {
                    ifDoDialog.dismiss();
                    pagerFragment.getPaths().remove(index);
                    pagerFragment.getViewPager().getAdapter().notifyDataSetChanged();
                    selectedPaths.remove(deletedPath);
                    onBackPressed();
                }

                @Override
                public void doCancel() {
                    ifDoDialog.dismiss();
                }
            });

        } else {
            snackbar.show();
            selectedPaths.remove(deletedPath);
            pagerFragment.getPaths().remove(index);
            pagerFragment.getViewPager().getAdapter().notifyDataSetChanged();
        }

        snackbar.setAction(R.string.__picker_undo, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pagerFragment.getPaths().size() > 0) {
                    pagerFragment.getPaths().add(index, deletedPath);
                    selectedPaths.add(index, deletedPath);
                } else {
                    pagerFragment.getPaths().add(deletedPath);
                    selectedPaths.add(deletedPath);
                }
                pagerFragment.getViewPager().getAdapter().notifyDataSetChanged();
                pagerFragment.getViewPager().setCurrentItem(index, true);
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showIfdoDialogCb(String title, String content, final IfdoInterface ifdoInterface) {
        // TODO Auto-generated method stub
        ifDoDialog = new Dialog(this, R.style.MyDialog);
        ifDoDialog.setContentView(R.layout.if_do_dialog);
        TextView titleTv = (TextView) ifDoDialog.findViewById(R.id.textView_do_title);
        TextView doInfoView = (TextView) ifDoDialog.findViewById(R.id.textView_do_info);

        if (title != null && !"".equals(title)) {
            titleTv.setText(title);
        }

        doInfoView.setText(content);
        ifdoConfirm = (TextView) ifDoDialog.findViewById(R.id.input_ifdo_confirm);
        ifdoCancel = (TextView) ifDoDialog.findViewById(R.id.input_ifdo_cancel);
        ifdoConfirm.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                ifdoInterface.doConfirm();
            }
        });
        ifdoCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                ifdoInterface.doCancel();
            }
        });
        ifDoDialog.show();
    }

    public boolean isSelected(String path) {
        return selectedPaths.contains(path);
    }
}
