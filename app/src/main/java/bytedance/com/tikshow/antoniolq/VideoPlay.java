package bytedance.com.tikshow.antoniolq;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import bytedance.com.tikshow.R;
import bytedance.com.tikshow.bean.PostVideoResponse;
import bytedance.com.tikshow.network.IMiniDouyinService;
import bytedance.com.tikshow.network.RetrofitManager;
import bytedance.com.tikshow.utils.ResourceUtils;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VideoPlay extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private static final int PICK_VIDEO = 2;

    public VideoView mVideoView;

    public Uri mSelectedImage;

    private Uri mSelectedVideo;

    private Button image;

    private Button post;

    private String imagepath;

    private EditText student_id;

    private EditText username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);
        Bundle bundle = this.getIntent().getExtras();
        imagepath = bundle.getString("path");
        int pathflag = bundle.getInt("flag");
        mVideoView = findViewById(R.id.play);
        mSelectedVideo = Uri.parse(imagepath);
        mVideoView.setVideoURI(mSelectedVideo);
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
                mp.setLooping(true);
            }
        });
        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mVideoView.setVideoURI(mSelectedVideo);
            }
        });
        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Toast.makeText(VideoPlay.this, "error: " + what, Toast.LENGTH_LONG).show();
                Log.e("VideoPlay", "error: " + what + " extra: " + extra);
                return false;
            }
        });
        MediaController mediaController = new MediaController(this);
        mVideoView.setMediaController(mediaController);
        image = findViewById(R.id.image);
        image.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });
        post = findViewById(R.id.post);
        username = (EditText) findViewById(R.id.user_name);
        student_id = (EditText) findViewById(R.id.student_id);
        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mSelectedImage!=null && mSelectedVideo != null)
                {
                    postVideo();
                }
            }
        });
    }
    public String getRealPathFromURI(Uri contentUri) {

        // can post image
        String [] proj={MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery( contentUri,
                proj, // Which columns to return
                null,       // WHERE clause; which rows to return (all rows)
                null,       // WHERE clause selection arguments (none)
                null); // Order-by clause (ascending by name)
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        return cursor.getString(column_index);
    }
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("LIU", "onActivityResult() called with: requestCode = [" + requestCode + "], resultCode = [" + resultCode + "], data = [" + data + "]");

        if (resultCode == RESULT_OK && null != data) {

            if (requestCode == PICK_IMAGE) {
                mSelectedImage = data.getData();
                Log.d("LIU", "selectedImage = " + mSelectedImage);
                image.setText("封面已上传");
            }
        }
    }
    public void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),
                PICK_IMAGE);
    }
    private void postVideo() {
        post.setText("上传中...");
        post.setEnabled(false);
        RetrofitManager.get(IMiniDouyinService.HOST).create(IMiniDouyinService.class).createVideo(student_id.getText().toString(), username.getText().toString(), getMultipartFromUri("cover_image", mSelectedImage,1), getMultipartFromUri("video", mSelectedVideo,0)).enqueue(new Callback<PostVideoResponse>() {
            @Override
            public void onResponse(Call<PostVideoResponse> call, Response<PostVideoResponse> response) {
                Log.d("LIU", "onResponse() called with: call = [" + call + "], response = [" + response.body() + "]");
                String toast;
                if (response.isSuccessful()) {
                    toast = "Post Success!";
                    post.setText("上传");
                    startActivity(new Intent(VideoPlay.this,CustomCameraActivity.class));
                    finish();
                } else {
                    Log.d("LIU", "onResponse() called with: response.errorBody() = [" + response.errorBody() + "]");
                    toast = "Post Failure...";
                    post.setText("上传");
                }
                Toast.makeText(VideoPlay.this, toast, Toast.LENGTH_LONG).show();
                post.setEnabled(true);
            }

            @Override public void onFailure(Call<PostVideoResponse> call, Throwable t) {
                Log.d("LIU", "onFailure() called with: call = [" + call + "], t = [" + t + "]");
                Toast.makeText(VideoPlay.this, t.getMessage(), Toast.LENGTH_LONG).show();
                post.setText("上传");
                post.setEnabled(true);
            }
        });
    }

    private MultipartBody.Part getMultipartFromUri(String name, Uri uri,int flag) {
        // if NullPointerException thrown, try to allow storage permission in system settings
        if  (flag == 1)
        {
            File f = new File(ResourceUtils.getRealPath(VideoPlay.this, uri));
            RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), f);
            return MultipartBody.Part.createFormData(name, f.getName(), requestFile);
        }
        else
        {
            File f = new File(imagepath);
            RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), f);
            return MultipartBody.Part.createFormData(name, f.getName(), requestFile);
        }
        }
}
