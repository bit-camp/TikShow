package bytedance.com.tikshow.antoniolq;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;

import bytedance.com.tikshow.R;

public class VideoPlay extends AppCompatActivity {

    private static final int REQUEST_VIDEO_CAPTURE = 1;

    private static final int REQUEST_EXTERNAL_CAMERA = 101;

    public VideoView mVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);
        Bundle bundle = this.getIntent().getExtras();
        String name = bundle.getString("path");
        mVideoView = findViewById(R.id.play);
        File testFile = new File(name);
        if (testFile.exists()) {
            mVideoView.setVideoPath(name);
            mVideoView.start();
        }
    }
}
