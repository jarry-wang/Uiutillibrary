package cn.join.android.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import cn.join.android.R;
import cn.join.android.ui.widget.HandyTextView;


public class ToastUtil {

    private Context context;

    public ToastUtil(Context context) {
        this.context = context;
    }

    Toast toast;

    public synchronized void showToast(String text) {
        // TODO Auto-generated method stub
        View toastRoot = LayoutInflater.from(context).inflate(
                R.layout.common_toast, null);
        ((HandyTextView) toastRoot.findViewById(R.id.toast_text)).setText(text);
        if (toast == null) {
            toast = new Toast(context);
            toast.setDuration(Toast.LENGTH_SHORT);
        }
        toast.setView(toastRoot);
        toast.show();
    }

}
