package cn.join.android.ui.materialdesign.views;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.join.android.ui.materialdesign.utils.Utils;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;
import cn.join.android.R;


public class ButtonFloat extends Button {

    int sizeIcon = 24;
    int sizeRadius = 28;


    ImageView icon; // Icon of float button
    Drawable drawableIcon;

    public boolean isShow = false;

    float showPosition;
    float hidePosition;


    public ButtonFloat(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundResource(R.drawable.background_button_float);
        setBackgroundColor(backgroundColor);
        sizeRadius = attrs.getAttributeIntValue(MATERIALDESIGNXML, "buttonSize", 0);
        sizeIcon = attrs.getAttributeIntValue(MATERIALDESIGNXML, "buttonIconSize", 0);
        setDefaultProperties();
        icon = new ImageView(context);
        icon.setAdjustViewBounds(true);
        icon.setScaleType(ScaleType.CENTER_CROP);
        if (drawableIcon != null) {
            icon.setImageDrawable(drawableIcon);
        }
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(Utils.dpToPx(sizeIcon, getResources()), Utils.dpToPx(sizeIcon, getResources()));
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        icon.setLayoutParams(params);
        addView(icon);

    }

    protected void setDefaultProperties() {

        rippleSpeed = Utils.dpToPx(3, getResources());
        rippleSize = Utils.dpToPx(5, getResources());
        setMinimumWidth(Utils.dpToPx(sizeRadius * 2, getResources()));
        setMinimumHeight(Utils.dpToPx(sizeRadius * 2, getResources()));
        super.background = R.drawable.background_button_float;
//		super.setDefaultProperties();
    }


    // Set atributtes of XML to View
    protected void setAttributes(AttributeSet attrs) {
        //Set background Color
        // Color by resource
        int bacgroundColor = attrs.getAttributeResourceValue(ANDROIDXML, "background", -1);
        if (bacgroundColor != -1) {
            setBackgroundColor(getResources().getColor(bacgroundColor));
        } else {
            // Color by hexadecimal
            background = attrs.getAttributeIntValue(ANDROIDXML, "background", -1);
            if (background != -1)
                setBackgroundColor(background);
        }

        // Set Ripple Color
        // Color by resource
        int rippleColor = attrs.getAttributeResourceValue(MATERIALDESIGNXML,
                "rippleColor", -1);
        if (rippleColor != -1) {
            setRippleColor(getResources().getColor(rippleColor));
        } else {
            // Color by hexadecimal
            int background = attrs.getAttributeIntValue(MATERIALDESIGNXML, "rippleColor", -1);
            if (background != -1)
                setRippleColor(background);
            else
                setRippleColor(makePressColor());
        }
        // Icon of button
        int iconResource = attrs.getAttributeResourceValue(MATERIALDESIGNXML, "iconDrawable", -1);
        if (iconResource != -1)
            drawableIcon = getResources().getDrawable(iconResource);
        final boolean animate = attrs.getAttributeBooleanValue(MATERIALDESIGNXML, "animate", false);
        post(new Runnable() {

            @Override
            public void run() {
                showPosition = ViewHelper.getY(ButtonFloat.this) - Utils.dpToPx(24, getResources());
                hidePosition = ViewHelper.getY(ButtonFloat.this) + getHeight() * 3;
                if (animate) {
                    ViewHelper.setY(ButtonFloat.this, hidePosition);
                    show();
                }
            }
        });

    }

    Integer height;
    Integer width;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (x != -1) {
            Rect src = new Rect(0, 0, getWidth(), getHeight());
            Rect dst = new Rect(Utils.dpToPx(1, getResources()), Utils.dpToPx(2, getResources()), getWidth() - Utils.dpToPx(1, getResources()), getHeight() - Utils.dpToPx(2, getResources()));
            canvas.drawBitmap(cropCircle(makeCircle()), src, dst, null);
            invalidate();
        }
    }


    public ImageView getIcon() {
        return icon;
    }

    public void setIcon(ImageView icon) {
        this.icon = icon;
    }

    public Drawable getDrawableIcon() {
        return drawableIcon;
    }

    public void setDrawableIcon(Drawable drawableIcon) {
        this.drawableIcon = drawableIcon;
        try {
            icon.setBackgroundDrawable(drawableIcon);
        } catch (NoSuchMethodError e) {
            icon.setBackgroundDrawable(drawableIcon);
        }
    }

    public Bitmap cropCircle(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff000000;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    @Override
    public TextView getTextView() {
        return null;
    }

    public void setRippleColor(int rippleColor) {
        this.rippleColor = rippleColor;
    }

    public void show() {
//		ObjectAnimator animator = ObjectAnimator.ofFloat(ButtonFloat.this, "y", showPosition);
//		animator.setInterpolator(new AccelerateDecelerateInterpolator());
//		animator.setDuration(1500);
//		animator.start();
        setVisibility(View.VISIBLE);
        ObjectAnimator anim = ObjectAnimator.ofFloat(ButtonFloat.this, "translationY", 200, 0).setDuration(500);
        anim.start();
        isShow = true;
    }

    public void hide() {

        ObjectAnimator animator = ObjectAnimator.ofFloat(ButtonFloat.this, "y", hidePosition);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(1500);
        animator.start();

        isShow = false;
    }

    public boolean isShow() {
        return isShow;
    }

}
