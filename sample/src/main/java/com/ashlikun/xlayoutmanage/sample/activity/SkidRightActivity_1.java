package com.ashlikun.xlayoutmanage.sample.activity;


import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.ashlikun.xlayoutmanage.sample.MyApplication;
import com.ashlikun.xlayoutmanage.sample.R;
import com.ashlikun.xlayoutmanage.skidright.SkidRightLayoutManager;
import com.bumptech.glide.Glide;

import java.util.List;

/**
 * @author　　: 李坤
 * 创建时间: 2018/10/26 17:39
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：
 */
public class SkidRightActivity_1 extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private SkidRightLayoutManager mSkidRightLayoutManager;
    private MyAdapter adapter = new MyAdapter();

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skid_1);


        initView();
    }


    private void initView() {
        mRecyclerView = findViewById(R.id.recycler_view);

        mSkidRightLayoutManager = new SkidRightLayoutManager(this, 0.8f, 0.9f);
        mRecyclerView.setLayoutManager(mSkidRightLayoutManager);
        mRecyclerView.setAdapter(adapter);
        findViewById(R.id.action1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.notifyItemChanged(0,"aaaaa");
            }
        });
    }

    /**
     * 适配器
     */
    class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private int[] imgs = {
                R.mipmap.skid_right_1,
                R.mipmap.skid_right_2,
                R.mipmap.skid_right_3,
                R.mipmap.skid_right_4,
                R.mipmap.skid_right_5,
                R.mipmap.skid_right_6,
                R.mipmap.skid_right_7,

        };
        private String[] imgs2 = {
                "https://sipapp.510gow.com/images/dc94c6d3b176562bd400e8cd0cd5bb39.jpg",
                "https://sipapp.510gow.com/images/3.jpg",
                "https://sipapp.510gow.com/images/2.jpg",
                "https://sipapp.510gow.com/images/ab33c401fc1510d8a2274c4197cd67ff.jpg"
        };
        String[] titles = {"第一个", "第二个", "第三个", "第四个", "第五个", "第六个"};

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(MyApplication.sContext).inflate(R.layout.item_skid_right_1, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
            super.onBindViewHolder(holder, position, payloads);
            Log.e("aaaaa",payloads.toString());
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            Log.e("aaaa", "position = " + position);
            Glide.with(MyApplication.sContext).load(imgs2[position % 4]).into(holder.imgBg);
            holder.tvTitle.setText(titles[position % 6]);
            holder.imgBg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Intent intent = new Intent(SkidRightActivity_1.this, SkidRightActivity_2.class);
//                    intent.putExtra("img", imgs[position % 7]);
//                    intent.putExtra("title", titles[position % 6]);
//                    Pair<View, String> p1 = Pair.create((View) holder.imgBg, "img_view_1");
//                    Pair<View, String> p2 = Pair.create((View) holder.tvTitle, "title_1");
//                    Pair<View, String> p3 = Pair.create((View) holder.tvBottom, "tv_bottom");
//                    ActivityOptionsCompat options = ActivityOptionsCompat.
//                            makeSceneTransitionAnimation(SkidRightActivity_1.this, p1, p2, p3);
//                    startActivity(intent, options.toBundle());
                    notifyItemChanged(position,"aaaaa");
                }
            });
        }

        @Override
        public int getItemCount() {
            return 4;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imgBg;
            TextView tvTitle;
            TextView tvBottom;

            public ViewHolder(View itemView) {
                super(itemView);
                imgBg = itemView.findViewById(R.id.img_bg);
                tvTitle = itemView.findViewById(R.id.tv_title);
                tvBottom = itemView.findViewById(R.id.tv_bottom);
            }
        }
    }
}
