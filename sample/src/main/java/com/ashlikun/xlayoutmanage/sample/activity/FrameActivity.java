package com.ashlikun.xlayoutmanage.sample.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.ashlikun.xlayoutmanage.frame.FrameLayoutManager;
import com.ashlikun.xlayoutmanage.sample.R;
import com.ashlikun.xlayoutmanage.sample.databinding.ItemViewBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Random;

public class FrameActivity extends AppCompatActivity {
    private RecyclerView listVertical;
    private RecyclerView listHorizontal;
    private FloatingActionButton action1;
    private FloatingActionButton action2;
    final TestAdapter adapter = new TestAdapter(false);
    final TestAdapter adapterVertical = new TestAdapter(true);

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_carousel_preview);
        listVertical = findViewById(R.id.list_vertical);
        listHorizontal = findViewById(R.id.list_horizontal);
        action1 = findViewById(R.id.action1);
        action2 = findViewById(R.id.action2);


        action1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listHorizontal.smoothScrollToPosition(adapter.getItemCount() - 2);
                // listVertical.smoothScrollToPosition(adapter.getItemCount() - 2);
            }
        });
        action2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listHorizontal.scrollToPosition(1);
                listVertical.scrollToPosition(1);
            }
        });
        initRecyclerView(listHorizontal, new FrameLayoutManager(FrameLayoutManager.HORIZONTAL, false), adapter);
        initRecyclerView(listVertical, new FrameLayoutManager(FrameLayoutManager.VERTICAL, true), adapterVertical);

    }

    private void initRecyclerView(final RecyclerView recyclerView, final FrameLayoutManager layoutManager, final TestAdapter adapter) {
        // enable zoom effect. this line can be customized
        layoutManager.setMaxVisibleItems(3);

        recyclerView.setLayoutManager(layoutManager);
        // we expect only fixed sized item for now
        recyclerView.setHasFixedSize(true);
        // sample adapter with random data
        recyclerView.setAdapter(adapter);
        // enable center post scrolling
        layoutManager.addOnItemSelectionListener(new FrameLayoutManager.OnCenterItemSelectionListener() {

            @Override
            public void onCenterItemChanged(final int adapterPosition) {
                if (FrameLayoutManager.INVALID_POSITION != adapterPosition) {
                    final int value = adapter.mPosition[adapterPosition];
                }
            }
        });
    }

    private final class TestAdapter extends RecyclerView.Adapter<TestViewHolder> {

        @SuppressWarnings("UnsecureRandomNumberGeneration")
        private final Random mRandom = new Random();
        private final int[] mColors;
        private final int[] mPosition;
        private int mItemsCount = 10;
        private boolean isVertical = false;

        TestAdapter(boolean b) {
            this.isVertical = b;
            mColors = new int[mItemsCount];
            mPosition = new int[mItemsCount];
            for (int i = 0; mItemsCount > i; ++i) {
                //noinspection MagicNumber
                mColors[i] = Color.argb(255, mRandom.nextInt(256), mRandom.nextInt(256), mRandom.nextInt(256));
                mPosition[i] = i;
            }
        }

        @Override
        public TestViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
            return new TestViewHolder(ItemViewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(final TestViewHolder holder, final int position) {
            holder.mItemViewBinding.cItem1.setText(String.valueOf(mPosition[position]));
            holder.mItemViewBinding.cItem2.setText(String.valueOf(mPosition[position]));
            holder.itemView.setBackgroundColor(mColors[position]);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (FrameLayoutManager.handleItemClick(isVertical ? listVertical : listHorizontal, v, position)) {
                        Toast.makeText(v.getContext(), isVertical ? "垂直的点击" : "水平的点击" + position, Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }

        @Override
        public int getItemCount() {
            return mItemsCount;
        }
    }

    private static class TestViewHolder extends RecyclerView.ViewHolder {

        private final ItemViewBinding mItemViewBinding;

        TestViewHolder(final ItemViewBinding itemViewBinding) {
            super(itemViewBinding.getRoot());

            mItemViewBinding = itemViewBinding;
        }
    }
}