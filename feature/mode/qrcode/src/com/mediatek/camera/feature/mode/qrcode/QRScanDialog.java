package com.mediatek.camera.feature.mode.qrcode;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;


import com.mediatek.camera.R;

public class QRScanDialog extends Dialog {

    private TextView mContent;
    private Button mCancel;
    private Button mCopy;
    private Button mOpen;

    private OnClickBottomListener onClickBottomListener;
    private String mContentString;

    protected QRScanDialog(@NonNull Context context) {
        super(context,  R.style.QRDialogStyle);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qrcode_dialog);
        mCancel = findViewById(R.id.qr_cancel);
        mContent = findViewById(R.id.qr_content);
        mCopy = findViewById(R.id.qr_copy);
        mOpen = findViewById(R.id.qr_open);
        mCopy.setText(getContext().getText(R.string.qrcode_mode_copy));
        mOpen.setText(getContext().getText(R.string.qrcode_mode_open));
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickBottomListener.onCancelClick();
            }
        });
        mCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickBottomListener.onCopyClick(mContentString);
            }
        });
        mOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickBottomListener.onOpenUrlClick(mContentString);
            }
        });
    }

    public QRScanDialog setOnClickBottomListener(OnClickBottomListener onClickBottomListener) {
        this.onClickBottomListener = onClickBottomListener;
        return this;
    }
    public interface OnClickBottomListener{

         void onCancelClick();

         void onOpenUrlClick(String url);

         void onCopyClick(String url);
    }

    public QRScanDialog setContent(String content){
        mContentString=content;
        if(content.indexOf("https://") == 0||content.indexOf("http://") == 0){
            mOpen.setVisibility(View.VISIBLE);
            mContent.setText(getContext().getText(R.string.qrcode_mode_url) + mContentString);
        }else {
            mOpen.setVisibility(View.GONE);
            mContent.setText(mContentString);
        }

        return this;
    }

}
