package bytedance.com.tikshow.HoqiheChen.VideoPlay;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import bytedance.com.tikshow.HoqiheChen.MessageWatch.InfoActivity;
import bytedance.com.tikshow.MainActivity;
import bytedance.com.tikshow.R;
import bytedance.com.tikshow.antoniolq.CustomCameraActivity;
import bytedance.com.tikshow.antoniolq.Login;
import bytedance.com.tikshow.bean.Feed;
import bytedance.com.tikshow.bean.FeedResponse;
import bytedance.com.tikshow.network.IMiniDouyinService;
import bytedance.com.tikshow.network.RetrofitManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainPageActivity extends AppCompatActivity {

    private static final int REQUEST_CAPTURE = 1;
    private static final String TAG = "HoqiheChen";
    private RecyclerView mRecyclerView;
    private TextView textView;
    private MyAdapter mAdapter;
    MyLayoutManager myLayoutManager;
    private List<Feed> mFeeds = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO,Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CAPTURE);
        setContentView(R.layout.activity_page_main);
        initView();
        initListener();
        fetchFeed();
    }

    private void initView() {
        mRecyclerView = findViewById(R.id.recycler);
        myLayoutManager = new MyLayoutManager(this, OrientationHelper.VERTICAL, false);
        textView = findViewById(R.id.firstpage);
        mAdapter = new MyAdapter(this);
        mRecyclerView.setLayoutManager(myLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);

    }

    private void initListener() {
        myLayoutManager.setOnViewPagerListener(new OnViewPagerListener() {
            @Override
            public void onInitComplete() {
                fetchFeed();
            }

            @Override
            public void onPageRelease(boolean isNext, int position) {
                Log.e(TAG, "释放位置:" + position + " 下一页:" + isNext);
                int index = 0;
                if (isNext) {
                    index = 0;
                } else {
                    index = 1;
                }
                releaseVideo(index);
            }

            @Override
            public void onPageSelected(int position, boolean bottom) {
                Log.e(TAG, "选择位置:" + position + " 下一页:" + bottom);

                playVideo(0);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                fetchFeed();
                new Handler().postDelayed(new Runnable() {//模拟耗时操作
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);//取消刷新

                    }
                },2000);
            }
        });
    }

    class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
//        private int[] imgs = {R.mipmap.img_video_1, R.mipmap.img_video_2, R.mipmap.img_video_3, R.mipmap.img_video_4, R.mipmap.img_video_5, R.mipmap.img_video_6, R.mipmap.img_video_7, R.mipmap.img_video_8};
//        private int[] videos = {R.raw.video_1, R.raw.video_1, R.raw.video_1, R.raw.video_1, R.raw.video_1, R.raw.video_1, R.raw.video_1, R.raw.video_1};
        private int index = 0;
        private Context mContext;

        public MyAdapter(Context context) {
            this.mContext = context;
        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
//            holder.img_thumb.setImageResource(imgs[index]);
//            holder.videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + videos[index]));
            Glide.with(holder.img_thumb.getContext()).load(mFeeds.get(position).getImageUrl()).into(holder.img_thumb);
//            holder.img_thumb.setImageURI(Uri.parse(mFeeds.get(position).getImageUrl()));
            holder.videoView.setVideoURI(Uri.parse(mFeeds.get(position).getVideoUrl()));

        }

        @Override
        public int getItemCount() {
            return mFeeds.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView img_thumb;
            VideoView videoView;
            ImageView img_play;
            RelativeLayout rootView;

            public ViewHolder(View itemView) {
                super(itemView);
                img_thumb = itemView.findViewById(R.id.img_thumb);
                videoView = itemView.findViewById(R.id.video_view);
                img_play = itemView.findViewById(R.id.img_play);
                rootView = itemView.findViewById(R.id.root_view);
            }
        }
    }

    private void releaseVideo(int index) {
        View itemView = mRecyclerView.getChildAt(index);
        if(itemView == null) return;
        final VideoView videoView = itemView.findViewById(R.id.video_view);
        final ImageView imgThumb = itemView.findViewById(R.id.img_thumb);
        final ImageView imgPlay = itemView.findViewById(R.id.img_play);
        videoView.stopPlayback();
        imgThumb.animate().alpha(1).start();
        imgPlay.animate().alpha(0f).start();
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void playVideo(int position) {
        View itemView = mRecyclerView.getChildAt(position);
        final FullWindowVideoView videoView = itemView.findViewById(R.id.video_view);
        final ImageView imgPlay = itemView.findViewById(R.id.img_play);
        final ImageView imgThumb = itemView.findViewById(R.id.img_thumb);
        final RelativeLayout rootView = itemView.findViewById(R.id.root_view);
        final MediaPlayer[] mediaPlayer = new MediaPlayer[1];
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {

            }
        });
        videoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                mediaPlayer[0] = mp;
                mp.setLooping(true);
                imgThumb.animate().alpha(0).setDuration(200).start();
                return false;
            }
        });

        videoView.start();

        imgPlay.setOnClickListener(new View.OnClickListener() {
            boolean isPlaying = true;

            @Override
            public void onClick(View v) {
                if (videoView.isPlaying()) {
                    imgPlay.animate().alpha(0.7f).start();
                    videoView.pause();
                    isPlaying = false;
                } else {
                    imgPlay.animate().alpha(0f).start();
                    videoView.start();
                    isPlaying = true;
                }
            }
        });
    }

    public void HomePage(View view){
        startActivity(new Intent(this, MainActivity.class));
    }

    public void RecordVideo(View view){
        startActivity(new Intent(this, CustomCameraActivity.class));
    }
    public void Message(View view){
        startActivity(new Intent(this, InfoActivity.class));
    }

    public void loginPage(View view){
        startActivity(new Intent(this, Login.class));
    }
    public void fetchFeed() {

        RetrofitManager.get(IMiniDouyinService.HOST).create(IMiniDouyinService.class).fetchFeed().enqueue(new Callback<FeedResponse>() {
            @Override
            public void onResponse(Call<FeedResponse> call, Response<FeedResponse> response) {
                Log.d(TAG, "onResponse() called with: call = [" + call + "], response = [" + response.body() + "]");
                if (response.isSuccessful()) {
                    mFeeds = response.body().getFeeds();
                    mRecyclerView.getAdapter().notifyDataSetChanged();
                } else {
                    Log.d(TAG, "onResponse() called with: response.errorBody() = [" + response.errorBody() + "]");
                    Toast.makeText(MainPageActivity.this, "fetch feed failure!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<FeedResponse> call, Throwable t) {
                Log.d(TAG, "onFailure() called with: call = [" + call + "], t = [" + t + "]");
                Toast.makeText(MainPageActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();

            }
        });
    }
}
