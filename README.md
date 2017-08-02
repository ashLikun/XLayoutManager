# **FrameLayoutManager**
卡片布局的RecycleView to LayoutManage,类似画廊


### 1.用法
使用前，对于Android Studio的用户，可以选择添加:
     compile 'com.github.ashLikun:FrameLayoutManager:1.0.0'//FrameLayoutManager

     layoutManager.setOnLayoutListener(new DefaultZoomOnLayoutListener());
     layoutManager.setMaxVisibleItems(3);

     recyclerView.setLayoutManager(layoutManager);
     // we expect only fixed sized item for now
     recyclerView.setHasFixedSize(true);
     // sample adapter with random data
     recyclerView.setAdapter(adapter);
     // enable center post scrolling
     recyclerView.addOnScrollListener(new FrameScrollListener());
     // enable center post touching on item and item click listener

     layoutManager.setOnItemClickListener(new OnItemClickListener(recyclerView) {
         @Override
         protected void onCenterItemClicked(@NonNull RecyclerView recyclerView, @NonNull FrameLayoutManager carouselLayoutManager, @NonNull View v) {
             Toast.makeText(CarouselPreviewActivity.this, "aaaaa" + carouselLayoutManager.getCenterItemPosition(), Toast.LENGTH_SHORT).show();
         }
     });
     layoutManager.addOnItemSelectionListener(new FrameLayoutManager.OnCenterItemSelectionListener() {

         @Override
         public void onCenterItemChanged(final int adapterPosition) {
             if (FrameLayoutManager.INVALID_POSITION != adapterPosition) {
                 final int value = adapter.mPosition[adapterPosition];
             }
         }
     });


### 混肴
####

