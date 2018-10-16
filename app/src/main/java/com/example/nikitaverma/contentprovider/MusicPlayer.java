package com.example.nikitaverma.contentprovider;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import static com.example.nikitaverma.contentprovider.MainActivity.albumName;
import static com.example.nikitaverma.contentprovider.MainActivity.mAlbumTvBar;
import static com.example.nikitaverma.contentprovider.MainActivity.mPlayBar;
import static com.example.nikitaverma.contentprovider.MainActivity.mTitleTvBar;
import static com.example.nikitaverma.contentprovider.MainActivity.seekMediaPlayer;
import static com.example.nikitaverma.contentprovider.MainActivity.songTitle;
import static com.example.nikitaverma.contentprovider.MyMusicService.status;

public class MusicPlayer extends AppCompatActivity implements View.OnClickListener, MediaPlayer.OnCompletionListener, SeekBar.OnSeekBarChangeListener, OnClickNotificationButton, Serializable {

    public static TextView mTitle;
    public static TextView mAlbum;
    static MediaPlayer mediaPlayer;
    static SeekBar mProgressbar;
    static Intent mServiceIntent;
    public static ImageButton mStartBtn;
    private ImageButton mStopBtn;
    private ImageButton mPauseBtn;
    private String mPath = null;
    private Toolbar mToolbar;
    private TextView mRunningTime;
    private TextView mEndTime;
    private MainActivity mMainActivity = new MainActivity();
    private int changeSeekBarProgress = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        Intent intent = getIntent();
        mPath = intent.getStringExtra("path");
        mRunningTime = findViewById(R.id.running_time);
        mEndTime = findViewById(R.id.end_time);
        mStartBtn = findViewById(R.id.start);
        mStopBtn = findViewById(R.id.stop);
        mPauseBtn = findViewById(R.id.pause);
        mProgressbar = findViewById(R.id.progress_bar);
        mTitle = findViewById(R.id.text_title);
        mAlbum = findViewById(R.id.text_album);
        // mProgressbar.setOnSeekBarChangeListener(this);
        mStartBtn.setOnClickListener(this);
        mStopBtn.setOnClickListener(this);
        mPauseBtn.setOnClickListener(this);
        mProgressbar.setOnSeekBarChangeListener(this);

        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(getApplicationContext(), Uri.parse(mPath));
            mTitle.setText(songTitle);
            mAlbum.setText(albumName);
            mAlbumTvBar.setText(albumName);
            mTitleTvBar.setText(songTitle);
            mStartBtn.setImageResource(R.drawable.ic_pause);
            mPlayBar.setImageResource(R.drawable.ic_pause);
            mediaPlayer.seekTo(MainActivity.seekMediaPlayer);
            seekMediaPlayer = 0;
            mediaPlayer.start();

            if (mServiceIntent != null) {
                // stopService(mServiceIntent);
                mServiceIntent = null;
            }
            if (mediaPlayer != null && mediaPlayer.isPlaying() && mServiceIntent == null) {
                mServiceIntent = new Intent(getApplicationContext(), MyMusicService.class);
                MyMusicService.onClickNotificationButton = this;
                mServiceIntent.setAction("Notify");
                mServiceIntent.putExtra("Resume_Music_Player", mediaPlayer.getCurrentPosition() + "");
                //   mServiceIntent.putExtra("Listener", (Serializable) this);
                startService(mServiceIntent);
            }
        }

        mediaPlayer.setOnCompletionListener(this);

        new MyTask().execute();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.start:
                if (mediaPlayer == null) {
                    mediaPlayer = MediaPlayer.create(getApplicationContext(), Uri.parse(mPath));
                    new MyTask().execute();
                    mediaPlayer.start();
                } else if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                } else {
                    mediaPlayer.start();
                }
                new MyTask().execute();
                notificationStatus();
                break;

            case R.id.stop:
                if (mediaPlayer != null) {
                    mMainActivity.nextButtonClicked();
                    MusicPlayer.mediaPlayer.stop();
                    //  MusicPlayer.mediaPlayer = null;
                    MusicPlayer.mediaPlayer = MediaPlayer.create(getApplicationContext(), Uri.parse(MainActivity.path));
                    MusicPlayer.mediaPlayer.setOnCompletionListener(this);
                    MusicPlayer.mediaPlayer.start();
                    MusicPlayer.mediaPlayer.seekTo(0);
                    new MyTask().execute();
                }
                notificationStatus();
                break;

            case R.id.pause:
                if (mediaPlayer != null) {
                    mMainActivity.prevButtonClicked();
                    MusicPlayer.mediaPlayer.stop();
                    //MusicPlayer.mediaPlayer = null;
                    MusicPlayer.mediaPlayer = MediaPlayer.create(getApplicationContext(), Uri.parse(MainActivity.path));
                    MusicPlayer.mediaPlayer.setOnCompletionListener(this);
                    MusicPlayer.mediaPlayer.start();
                    MusicPlayer.mediaPlayer.seekTo(0);
                    new MyTask().execute();
                }
                notificationStatus();
                break;

            default:
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                mServiceIntent = null;
        }

        //  new MyTask().execute();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onPause() {
        super.onPause();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onStop() {
        super.onStop();
      //  notificationStatus();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void notificationStatus() {
        RemoteViews views = new RemoteViews(getPackageName(),
                R.layout.status_bar);
        RemoteViews bigViews = new RemoteViews(getPackageName(),
                R.layout.status_bar_expanded);

        if (!mediaPlayer.isPlaying()) {
            // MusicPlayer.mediaPlayer.pause();
            views.setImageViewResource(R.id.status_bar_play, R.drawable.ic_play);
            bigViews.setImageViewResource(R.id.status_bar_play, R.drawable.ic_play);
            mPlayBar.setImageResource(R.drawable.ic_play);
            mStartBtn.setImageResource(R.drawable.ic_play);

            // notificationManager.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status);
        } else {
            // MusicPlayer.mediaPlayer.start();
            views.setImageViewResource(R.id.status_bar_play, R.drawable.ic_pause);
            bigViews.setImageViewResource(R.id.status_bar_play, R.drawable.ic_pause);
            mPlayBar.setImageResource(R.drawable.ic_pause);
            mStartBtn.setImageResource(R.drawable.ic_pause);

            // notificationManager.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status);
        }

        mTitle.setText(songTitle);
        mAlbum.setText(albumName);
        mAlbumTvBar.setText(albumName);
        mTitleTvBar.setText(songTitle);
        views.setTextViewText(R.id.status_bar_track_name, songTitle);
        bigViews.setTextViewText(R.id.status_bar_track_name, songTitle);

        views.setTextViewText(R.id.status_bar_artist_name, MainActivity.artistName);
        bigViews.setTextViewText(R.id.status_bar_artist_name, MainActivity.artistName);

        bigViews.setTextViewText(R.id.status_bar_album_name, albumName);

        status.contentView = views;
        status.bigContentView = bigViews;
        MyMusicService.getManager(getApplicationContext()).notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status);

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        RemoteViews views = new RemoteViews(getPackageName(),
                R.layout.status_bar);
        RemoteViews bigViews = new RemoteViews(getPackageName(),
                R.layout.status_bar_expanded);

        mMainActivity.nextButtonClicked();
        MusicPlayer.mediaPlayer.stop();
        //  MusicPlayer.mediaPlayer = null;
        MusicPlayer.mediaPlayer = MediaPlayer.create(getApplicationContext(), Uri.parse(MainActivity.path));
        MusicPlayer.mediaPlayer.setOnCompletionListener(this);
        MusicPlayer.mediaPlayer.start();
        MusicPlayer.mediaPlayer.seekTo(0);
        mPlayBar.setImageResource(R.drawable.ic_pause);
        mStartBtn.setImageResource(R.drawable.ic_pause);
        mTitle.setText(songTitle);
        mAlbum.setText(albumName);
        mTitleTvBar.setText(songTitle);
        mAlbumTvBar.setText(albumName);
        views.setTextViewText(R.id.status_bar_track_name, songTitle);
        bigViews.setTextViewText(R.id.status_bar_track_name, songTitle);

        views.setTextViewText(R.id.status_bar_artist_name, MainActivity.artistName);
        bigViews.setTextViewText(R.id.status_bar_artist_name, MainActivity.artistName);

        bigViews.setTextViewText(R.id.status_bar_album_name, albumName);

        status.contentView = views;
        status.bigContentView = bigViews;
        MyMusicService.getManager(getApplicationContext()).notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status);

        new MyTask().execute();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onClickListener(Object listener) {
        if (mServiceIntent != null) {
            new MyTask().execute();
            if (!mediaPlayer.isPlaying()) {
                mStartBtn.setImageResource(R.drawable.ic_play);
            } else {
                mStartBtn.setImageResource(R.drawable.ic_pause);
            }
        } else {
            super.onBackPressed();
            finishAffinity();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

        changeSeekBarProgress = i;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

        MusicPlayer.mediaPlayer.seekTo(changeSeekBarProgress);
        //  mProgressbar.setProgress(changeSeekBarProgress);

    }

    class MyTask extends AsyncTask<Void, Integer, Void> {         //doInBackground return type input(URL), publishProgress input, doInBackground return type

        @Override
        protected void onPreExecute() {
            if (mediaPlayer != null) {
                mProgressbar.setMax(mediaPlayer.getDuration());
                String time = String.format("%02d : %02d ",
                        TimeUnit.MILLISECONDS.toMinutes(mediaPlayer.getDuration()),
                        TimeUnit.MILLISECONDS.toSeconds(mediaPlayer.getDuration()) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(mediaPlayer.getDuration())));
                mEndTime.setText(time);
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (mediaPlayer != null) {
                while (mediaPlayer.isPlaying()) {
                    publishProgress(mediaPlayer.getCurrentPosition());
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            String time = String.format("%02d : %02d ",
                    TimeUnit.MILLISECONDS.toMinutes(values[0]),
                    TimeUnit.MILLISECONDS.toSeconds(values[0]) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(values[0])));
            mRunningTime.setText(time);
            mProgressbar.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Toast.makeText(getApplicationContext(), "Destroy Music Player", Toast.LENGTH_SHORT).show();
    }
}
