package com.ashlikun.xlayoutmanage.sample.activity;


import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ashlikun.xlayoutmanage.sample.R;
import com.bumptech.glide.Glide;

/**
 * @author　　: 李坤
 * 创建时间: 2018/10/26 17:40
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：
 */
public class SkidRightActivity_2 extends AppCompatActivity {
    private ImageView mImgBg;
    private ImageView mImgGif;
    private TextView mTvTitle;
    private int mImgPath;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skid_2);
        mImgBg = findViewById(R.id.img_bg);
        mTvTitle = findViewById(R.id.tv_title);
        mImgGif = findViewById(R.id.img_gif);
        if (getIntent() != null) {
            mImgPath = getIntent().getIntExtra("img", R.mipmap.skid_right_3);
            String title = getIntent().getStringExtra("title");
            mTvTitle.setText(title);
            Glide.with(this).asBitmap().load(mImgPath).into(mImgBg);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Glide.with(SkidRightActivity_2.this).asGif().load(mImgPath).into(mImgGif);
                }
            }, 1000);

        }
    }


    @Override
    public void onBackPressed() {
        mImgGif.setVisibility(View.INVISIBLE);
        super.onBackPressed();
    }
}
