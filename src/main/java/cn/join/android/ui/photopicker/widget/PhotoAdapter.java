package cn.join.android.ui.photopicker.widget;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;

import cn.join.android.R;
import cn.join.android.ui.photopicker.PhotoPickUtils;
import cn.join.android.ui.photopicker.PhotoPreview;


/**
 * 显示组件里面的adapter
 * Created by donglua on 15/5/31.
 */
public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    private ArrayList<String> photoPaths;
    private LayoutInflater inflater;

    private Context mContext;
    private int maxCount;


    public void setAction(@MultiPickResultView.MultiPicAction int action) {
        this.action = action;
    }

    private int action;


    public PhotoAdapter(Context mContext, ArrayList<String> photoPaths, int maxCount) {
        this.photoPaths = photoPaths;
        this.mContext = mContext;
        this.maxCount = maxCount;
        inflater = LayoutInflater.from(mContext);
        padding = dip2Px(10);
    }

    public void add(ArrayList<String> photoPaths) {
        if (photoPaths != null && photoPaths.size() > 0) {
            this.photoPaths.addAll(photoPaths);
            notifyDataSetChanged();
        }

    }

    public void refresh(ArrayList<String> photoPaths) {
        this.photoPaths.clear();
        if (photoPaths != null && photoPaths.size() > 0) {
            this.photoPaths.addAll(photoPaths);
        }
        notifyDataSetChanged();
    }


    @Override
    public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.__picker_item_photo, parent, false);
        return new PhotoViewHolder(itemView);
    }

    public int dip2Px(int dip) {
        // px/dip = density;
        float density = mContext.getResources().getDisplayMetrics().density;
        int px = (int) (dip * density + .5f);
        return px;
    }

    int padding;

    @Override
    public void onBindViewHolder(final PhotoViewHolder holder, final int position) {

        if (action == MultiPickResultView.ACTION_SELECT || action == MultiPickResultView.ACTION_PREVIEW) {


            if (position == getItemCount() - 1) {//最后一个始终是+号，点击能够跳去添加图片
                Glide.with(mContext)
                        .load("")
                        .centerCrop()
                        .thumbnail(0.1f)
                        .placeholder(R.drawable.icon_pic_default)
                        .error(R.drawable.icon_pic_default)
                        .into(holder.ivPhoto);

                holder.ivPhoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (photoPaths != null && photoPaths.size() == maxCount) {
                            Toast.makeText(mContext, "已选了" + maxCount + "张图片", Toast.LENGTH_SHORT).show();
                        } else {
                            PhotoPickUtils.startPick((Activity) mContext, photoPaths, maxCount);
                        }
                    }
                });
                holder.deleteBtn.setVisibility(View.GONE);
            } else {
                String str = photoPaths.get(position);
                Uri uri = Uri.fromFile(new File(photoPaths.get(position)));
                Glide.with(mContext)
                        .load(uri)
                        .centerCrop()
                        .thumbnail(0.1f)
                        .placeholder(R.drawable.__picker_default_weixin)
                        .error(R.drawable.__picker_ic_broken_image_black_48dp)
                        .into(holder.ivPhoto);
                //展示不做此功能
                holder.deleteBtn.setVisibility(View.GONE);
                holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        photoPaths.remove(position);
                        notifyDataSetChanged();
                    }
                });

                holder.ivPhoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PhotoPreview.builder()
                                .setPhotos(photoPaths)
                                .setSelectedPhotos(photoPaths)
                                .setAction(action)
                                .setCurrentItem(position)
                                .start((Activity) mContext);
                    }
                });
            }

            holder.ivPhoto.setPadding(0, padding, padding, padding);

        } else if (action == MultiPickResultView.ACTION_ONLY_SHOW) {
            Glide.with(mContext)
                    .load(photoPaths.get(position))
                    .centerCrop()
                    .thumbnail(0.1f)
                    .placeholder(R.drawable.__picker_default_weixin)
                    .error(R.drawable.__picker_ic_broken_image_black_48dp)
                    .into(holder.ivPhoto);

            holder.ivPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PhotoPreview.builder()
                            .setPhotos(photoPaths)
                            .setSelectedPhotos(photoPaths)
                            .setAction(action)
                            .setCurrentItem(position)
                            .start((Activity) mContext);
                }
            });
        }


    }


    @Override
    public int getItemCount() {
        return (action == MultiPickResultView.ACTION_SELECT || action == MultiPickResultView.ACTION_PREVIEW) ? photoPaths.size() + 1 : photoPaths.size();
    }


    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivPhoto;
        private View vSelected;
        public View cover;
        public View deleteBtn;

        public PhotoViewHolder(View itemView) {
            super(itemView);
            ivPhoto = (ImageView) itemView.findViewById(R.id.iv_photo);
            vSelected = itemView.findViewById(R.id.v_selected);
            vSelected.setVisibility(View.GONE);
            cover = itemView.findViewById(R.id.cover);
            cover.setVisibility(View.GONE);
            deleteBtn = itemView.findViewById(R.id.v_delete);
            deleteBtn.setVisibility(View.GONE);
        }
    }

}
