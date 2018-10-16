package com.example.nikitaverma.contentprovider;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static com.example.nikitaverma.contentprovider.MusicPlayer.mServiceIntent;
import static com.example.nikitaverma.contentprovider.MusicPlayer.mediaPlayer;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, MediaPlayer.OnCompletionListener {

    public static RecyclerView mRecyclerView;
    public static String songTitle;
    public static String artistName;
    public static String albumName;
    public static TextView mTitleTvBar;
    public static TextView mAlbumTvBar;
    public static ImageButton mPlayBar;
    static int mListviewposition = 0;
    static int mArrayListSize = 0;
    static String path;
    static MusicAdapter mMusicAdapter;
    List<MediaModel> mArrayList = new ArrayList();
    private Toolbar mToolbar;
    private ImageButton mNextBar;
    public static int seekMediaPlayer = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mPlayBar = findViewById(R.id.play_bar);
        mNextBar = findViewById(R.id.next_bar);
        mPlayBar.setOnClickListener(this);
        mNextBar.setOnClickListener(this);
        mTitleTvBar = findViewById(R.id.title_tv_bar);
        mAlbumTvBar = findViewById(R.id.album_tv_bar);
        //  mListView = findViewById(R.id.list_view);
        requestPermission();
    }

    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat
                    .requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        } else {
            //  Toast.makeText(getApplicationContext(), "Permission has already been granted", Toast.LENGTH_LONG).show();
            // Permission has already been granted
            fetchMusic();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 0:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    fetchMusic();
                } else {
                    // Permission Denied
                    Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_SHORT)
                            .show();
                    finish();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    void fetchContacts() {
        ArrayList<String> arrayList = new ArrayList<>();
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME;

        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(uri, projection, selection, selectionArgs, sortOrder);

        while (cursor.moveToNext()) {
            arrayList.add(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)) +
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
        }

    }

    void fetchMusic() {

        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

        String[] projection = {MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID};

        Cursor cursor = getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                null);

        while (cursor.moveToNext()) {
            MediaModel mediaModel = new MediaModel();
            mediaModel.setTitle(cursor.getString(0));
            mediaModel.setAlbum(cursor.getString(5));
            mediaModel.setData(cursor.getString(2));
            mediaModel.setArtist(cursor.getString(1));
            mArrayList.add(mediaModel);
        }
        cursor.close();
        Toast.makeText(getApplicationContext(), path, Toast.LENGTH_LONG).show();
        mMusicAdapter = new MusicAdapter(mArrayList, getApplicationContext());
        mRecyclerView.setAdapter(mMusicAdapter);

        if (path == null) {
            SharedPreferencesSource sp = new SharedPreferencesSource(getApplicationContext());
            int[] data = sp.getData();
            mListviewposition = data[0];
            seekMediaPlayer = data[1];
        }
        path = mArrayList.get(mListviewposition).getData();

        songTitle = mArrayList.get(mListviewposition).getTitle();
        albumName = mArrayList.get(mListviewposition).getAlbum();
        artistName = mArrayList.get(mListviewposition).getArtist();
        mAlbumTvBar.setText(albumName);
        mTitleTvBar.setText(songTitle);
        mArrayListSize = mArrayList.size();
    }

    public void onRecyclerItemClick(View view, Context context, String path, int position) {
        if (mediaPlayer != null) {
            seekMediaPlayer = mediaPlayer.getCurrentPosition();
            mediaPlayer.pause();
            mediaPlayer = null;
            //  MusicPlayer.mProgressbar.setProgress(0);
        }
        if(mListviewposition!=position)
            seekMediaPlayer = 0;
        mListviewposition = position;
        songTitle = mMusicAdapter.getSongTitle(mListviewposition);
        albumName = mMusicAdapter.getAlbum(mListviewposition);
        artistName = mMusicAdapter.getArtist(mListviewposition);
        Intent intent = new Intent(context, MusicPlayer.class);
        intent.putExtra("path", path);
        view.getContext().startActivity(intent);

    }

    public void nextButtonClicked() {
        if (mArrayListSize == mListviewposition + 1) {
            mListviewposition = 0;
        } else {
            mListviewposition = mListviewposition + 1;
        }
        path = mMusicAdapter.getPath(mListviewposition);
        songTitle = mMusicAdapter.getSongTitle(mListviewposition);
        albumName = mMusicAdapter.getAlbum(mListviewposition);
        artistName = mMusicAdapter.getArtist(mListviewposition);
    }

    public void prevButtonClicked() {
        if (mListviewposition == 0) {
            mListviewposition = mArrayListSize - 1;
        } else {
            mListviewposition = mListviewposition - 1;
        }
        path = mMusicAdapter.getPath(mListviewposition);
        songTitle = mMusicAdapter.getSongTitle(mListviewposition);
        albumName = mMusicAdapter.getAlbum(mListviewposition);
        artistName = mMusicAdapter.getArtist(mListviewposition);

    }

    @Override
    public void onBackPressed() {
        this.finish();
        // Toast.makeText(getApplicationContext(), "backpressed", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onStart() {
        super.onStart();
         Toast.makeText(getApplicationContext(), "Start", Toast.LENGTH_SHORT).show();
         if(mediaPlayer!=null && mediaPlayer.isPlaying()){
             mPlayBar.setImageResource(R.drawable.ic_play);
         }

    }

    @Override
    protected void onStop() {
        super.onStop();
          Toast.makeText(getApplicationContext(), "Stop", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Toast.makeText(getApplicationContext(), "Destroy MainActivity", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
           Toast.makeText(getApplicationContext(), "saveInstancestate", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
           Toast.makeText(getApplicationContext(), "Pause", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
           Toast.makeText(getApplicationContext(), "Resume", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
           Toast.makeText(getApplicationContext(), "Restart", Toast.LENGTH_SHORT).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.play_bar:
                if (mediaPlayer == null) {
                    nullMediaInitialize();
                } else {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                        mPlayBar.setImageResource(R.drawable.ic_play);
                        if (mServiceIntent != null) {
                            notificationStatus();
                        } else {
                            if (mediaPlayer != null && mediaPlayer.isPlaying() && mServiceIntent == null) {
                                mServiceIntent = new Intent(getApplicationContext(), MyMusicService.class);
                                // MyMusicService.onClickNotificationButton = this;
                                mServiceIntent.setAction("Notify");
                                mServiceIntent.putExtra("Resume_Music_Player", mediaPlayer.getCurrentPosition() + "");
                                //   mServiceIntent.putExtra("Listener", (Serializable) this);
                                startService(mServiceIntent);
                            }
                        }
                    } else {
                        mPlayBar.setImageResource(R.drawable.ic_pause);
                        mediaPlayer.start();
                        notificationStatus();
                    }
                }
                break;

            case R.id.next_bar:
                if (mediaPlayer != null) {
                    nextButtonClicked();
                    MusicPlayer.mediaPlayer.stop();
                    //  MusicPlayer.mediaPlayer = null;
                    MusicPlayer.mediaPlayer = MediaPlayer.create(getApplicationContext(), Uri.parse(MainActivity.path));
                    MusicPlayer.mediaPlayer.setOnCompletionListener(this);
                    MusicPlayer.mediaPlayer.start();
                    MusicPlayer.mediaPlayer.seekTo(0);
                    mPlayBar.setImageResource(R.drawable.ic_pause);
                    mAlbumTvBar.setText(albumName);
                    mTitleTvBar.setText(songTitle);
                    notificationStatus();
                } else {
                    nextButtonClicked();
                    mAlbumTvBar.setText(albumName);
                    mTitleTvBar.setText(songTitle);
                    // notificationStatus();
                }
                break;
        }
        // notificationStatus();
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

        } else {
            // MusicPlayer.mediaPlayer.start();
            views.setImageViewResource(R.id.status_bar_play, R.drawable.ic_pause);
            bigViews.setImageViewResource(R.id.status_bar_play, R.drawable.ic_pause);
        }


        views.setTextViewText(R.id.status_bar_track_name, MainActivity.songTitle);
        bigViews.setTextViewText(R.id.status_bar_track_name, MainActivity.songTitle);

        views.setTextViewText(R.id.status_bar_artist_name, MainActivity.artistName);
        bigViews.setTextViewText(R.id.status_bar_artist_name, MainActivity.artistName);

        bigViews.setTextViewText(R.id.status_bar_album_name, MainActivity.albumName);

        MyMusicService.status.contentView = views;
        MyMusicService.status.bigContentView = bigViews;
        MyMusicService.getManager(getApplicationContext()).notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, MyMusicService.status);

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        RemoteViews views = new RemoteViews(getPackageName(),
                R.layout.status_bar);
        RemoteViews bigViews = new RemoteViews(getPackageName(),
                R.layout.status_bar_expanded);

        nextButtonClicked();
        MusicPlayer.mediaPlayer.stop();
        //  MusicPlayer.mediaPlayer = null;
        MusicPlayer.mediaPlayer = MediaPlayer.create(getApplicationContext(), Uri.parse(MainActivity.path));
        MusicPlayer.mediaPlayer.setOnCompletionListener(this);
        MusicPlayer.mediaPlayer.start();
        MusicPlayer.mediaPlayer.seekTo(0);
        mPlayBar.setImageResource(R.drawable.ic_pause);
        /*mStartBtn.setImageResource(R.drawable.ic_pause);
        mTitle.setText(songTitle);
        mAlbum.setText(albumName);*/
        mTitleTvBar.setText(songTitle);
        mAlbumTvBar.setText(albumName);
        views.setTextViewText(R.id.status_bar_track_name, songTitle);
        bigViews.setTextViewText(R.id.status_bar_track_name, songTitle);

        views.setTextViewText(R.id.status_bar_artist_name, MainActivity.artistName);
        bigViews.setTextViewText(R.id.status_bar_artist_name, MainActivity.artistName);

        bigViews.setTextViewText(R.id.status_bar_album_name, albumName);

        MyMusicService.status.contentView = views;
        MyMusicService.status.bigContentView = bigViews;
        MyMusicService.getManager(getApplicationContext()).notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, MyMusicService.status);

    }

    void nullMediaInitialize() {
        mediaPlayer = MediaPlayer.create(getApplicationContext(), Uri.parse(path));
//                    mTitle.setText(MainActivity.songTitle);
//                    mAlbum.setText(MainActivity.albumName);
        mediaPlayer.setOnCompletionListener(this);
        mAlbumTvBar.setText(albumName);
        mTitleTvBar.setText(songTitle);
        mPlayBar.setImageResource(R.drawable.ic_pause);
        mediaPlayer.seekTo(seekMediaPlayer);
        seekMediaPlayer = 0;
        mediaPlayer.start();

        if (mServiceIntent != null) {
            // stopService(mServiceIntent);
            mServiceIntent = null;
        }
        if (mediaPlayer != null && mediaPlayer.isPlaying() && mServiceIntent == null) {
            mServiceIntent = new Intent(getApplicationContext(), MyMusicService.class);
            // MyMusicService.onClickNotificationButton = this;
            mServiceIntent.setAction("Notify");
            mServiceIntent.putExtra("Resume_Music_Player", mediaPlayer.getCurrentPosition() + "");
            //   mServiceIntent.putExtra("Listener", (Serializable) this);
            startService(mServiceIntent);
        }
    }

}
