package com.example.nikitaverma.contentprovider;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import static com.example.nikitaverma.contentprovider.Constants.ACTION.ANDROID_CHANNEL_ID;
import static com.example.nikitaverma.contentprovider.Constants.ACTION.ANDROID_CHANNEL_NAME;
import static com.example.nikitaverma.contentprovider.MainActivity.mAlbumTvBar;
import static com.example.nikitaverma.contentprovider.MainActivity.mListviewposition;
import static com.example.nikitaverma.contentprovider.MainActivity.mPlayBar;
import static com.example.nikitaverma.contentprovider.MainActivity.mTitleTvBar;


public class MyMusicService extends Service implements MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener {
    public static Notification status;
    public static OnClickNotificationButton onClickNotificationButton;
    private static NotificationManager mManager;
    private final String LOG_TAG = "NotificationService";
    MainActivity mMainActivity = new MainActivity();
    private PhoneStateListener mPhoneStateListener;
    private TelephonyManager mTelephonyManager;
    private boolean isPausedCall;

    public static NotificationManager getManager(Context context) {
        if (mManager == null) {
            mManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mManager;
    }

    // MainActivity mainActivity = new MainActivity();
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannels();

        }

    }

    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        mTelephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        mPhoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String phoneNumber) {
                switch (state) {
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (MusicPlayer.mediaPlayer != null) {
                            MusicPlayer.mediaPlayer.pause();
                            isPausedCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        if (MusicPlayer.mediaPlayer != null) {
                            if (isPausedCall) {
                                MusicPlayer.mediaPlayer.start();
                                isPausedCall = false;
                            }
                        }
                        break;
                }
            }
        };
        RemoteViews views = new RemoteViews(getPackageName(),
                R.layout.status_bar);
        RemoteViews bigViews = new RemoteViews(getPackageName(),
                R.layout.status_bar_expanded);

        if (intent.getAction().equals("Notify")) {
            int duration = Integer.parseInt(intent.getStringExtra("Resume_Music_Player"));
            //   context = (Context) intent.getSerializableExtra("Listener");
            MusicPlayer.mediaPlayer.start();
            MusicPlayer.mediaPlayer.seekTo(duration);
            // MusicPlayer.mediaPlayer.setOnCompletionListener(this);
            showNotification();
           // Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();

        } else if (intent.getAction().equals(Constants.ACTION.PREV_ACTION)) {
           // Toast.makeText(this, "Clicked Previous", Toast.LENGTH_SHORT).show();
            mMainActivity.prevButtonClicked();
            MusicPlayer.mediaPlayer.stop();
            //    MusicPlayer.mediaPlayer = null;
            MusicPlayer.mediaPlayer = MediaPlayer.create(getApplicationContext(), Uri.parse(MainActivity.path));
            MusicPlayer.mediaPlayer.setOnCompletionListener(this);
            MusicPlayer.mediaPlayer.start();
            MusicPlayer.mediaPlayer.seekTo(0);
            Log.i(LOG_TAG, "Clicked Previous");
            MusicPlayer.mediaPlayer.start();

            if (MusicPlayer.mTitle != null) {
                MusicPlayer.mTitle.setText(MainActivity.songTitle);
                MusicPlayer.mAlbum.setText(MainActivity.albumName);
            }
            mTitleTvBar.setText(MainActivity.songTitle);
            mAlbumTvBar.setText(MainActivity.albumName);
            views.setImageViewResource(R.id.status_bar_play, R.drawable.ic_pause);
            bigViews.setImageViewResource(R.id.status_bar_play, R.drawable.ic_pause);
            mPlayBar.setImageResource(R.drawable.ic_pause);
            views.setTextViewText(R.id.status_bar_track_name, MainActivity.songTitle);
            bigViews.setTextViewText(R.id.status_bar_track_name, MainActivity.songTitle);

            views.setTextViewText(R.id.status_bar_artist_name, MainActivity.artistName);
            bigViews.setTextViewText(R.id.status_bar_artist_name, MainActivity.artistName);

            bigViews.setTextViewText(R.id.status_bar_album_name, MainActivity.albumName);
            status.contentView = views;
            status.bigContentView = bigViews;

            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status);
        } else if (intent.getAction().equals(Constants.ACTION.PLAY_ACTION)) {
          //  Toast.makeText(this, "Clicked Play", Toast.LENGTH_SHORT).show();
            if (MusicPlayer.mediaPlayer.isPlaying()) {

                MusicPlayer.mediaPlayer.pause();
                views.setImageViewResource(R.id.status_bar_play, R.drawable.ic_play);
                bigViews.setImageViewResource(R.id.status_bar_play, R.drawable.ic_play);
                status.contentView = views;
                status.bigContentView = bigViews;
                mPlayBar.setImageResource(R.drawable.ic_play);
                startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status);
                //  getManager().notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE ,status);
            } else {
                MusicPlayer.mediaPlayer.start();
                views.setImageViewResource(R.id.status_bar_play, R.drawable.ic_pause);
                bigViews.setImageViewResource(R.id.status_bar_play, R.drawable.ic_pause);
                status.contentView = views;
                status.bigContentView = bigViews;
                mPlayBar.setImageResource(R.drawable.ic_pause);
                startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status);
                // getManager().notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE ,status);
            }
            // mainActivity.mediaPlayer.seekTo(duration);
            Log.i(LOG_TAG, "Clicked Play");
        } else if (intent.getAction().equals(Constants.ACTION.NEXT_ACTION)) {
         //   Toast.makeText(this, "Clicked Next", Toast.LENGTH_SHORT).show();
            mMainActivity.nextButtonClicked();
            MusicPlayer.mediaPlayer.stop();
            //   MusicPlayer.mediaPlayer = null;
            MusicPlayer.mediaPlayer = MediaPlayer.create(getApplicationContext(), Uri.parse(MainActivity.path));
            MusicPlayer.mediaPlayer.setOnCompletionListener(this);
            MusicPlayer.mediaPlayer.start();
            MusicPlayer.mediaPlayer.seekTo(0);
            MusicPlayer.mediaPlayer.start();
            if (MusicPlayer.mTitle != null) {
                MusicPlayer.mTitle.setText(MainActivity.songTitle);
                MusicPlayer.mAlbum.setText(MainActivity.albumName);
            }
            mTitleTvBar.setText(MainActivity.songTitle);
            mAlbumTvBar.setText(MainActivity.albumName);
            views.setImageViewResource(R.id.status_bar_play, R.drawable.ic_pause);
            bigViews.setImageViewResource(R.id.status_bar_play, R.drawable.ic_pause);
            mPlayBar.setImageResource(R.drawable.ic_pause);
            views.setTextViewText(R.id.status_bar_track_name, MainActivity.songTitle);
            bigViews.setTextViewText(R.id.status_bar_track_name, MainActivity.songTitle);

            views.setTextViewText(R.id.status_bar_artist_name, MainActivity.artistName);
            bigViews.setTextViewText(R.id.status_bar_artist_name, MainActivity.artistName);

            bigViews.setTextViewText(R.id.status_bar_album_name, MainActivity.albumName);
            status.contentView = views;
            status.bigContentView = bigViews;

            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status);
            Log.i(LOG_TAG, "Clicked Next");
        } else if (intent.getAction().equals(
                Constants.ACTION.STOPFOREGROUND_ACTION)) {
            Log.i(LOG_TAG, "Received Stop Foreground Intent");
          //  Toast.makeText(this, "Service Stoped", Toast.LENGTH_SHORT).show();
            MusicPlayer.mediaPlayer.pause();
            //  MusicPlayer.mediaPlayer = null;
            MusicPlayer.mServiceIntent = null;
            stopForeground(true);
            mManager.cancel(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE);
            mManager = null;
            onClickNotificationButton = null;

            //  status.flags = Notification.FLAG_INSISTENT | Notification.FLAG_AUTO_CANCEL;
            //   mManager.cancel(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE);
            stopSelf();
            if(MusicPlayer.mStartBtn!=null)
            MusicPlayer.mStartBtn.setImageResource(R.drawable.ic_play);
            if(MainActivity.mPlayBar!=null)
                MainActivity.mPlayBar.setImageResource(R.drawable.ic_play);

            SharedPreferencesSource sp = new SharedPreferencesSource(getApplicationContext());
            sp.setData(mListviewposition,MusicPlayer.mediaPlayer.getCurrentPosition());
            MusicPlayer.mediaPlayer.release();
            MusicPlayer.mediaPlayer = null;
            Intent a = new Intent(Intent.ACTION_MAIN);
            a.addCategory(Intent.CATEGORY_HOME);
            a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(a);
        }
        if (onClickNotificationButton != null)
            onClickNotificationButton.onClickListener(this);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPhoneStateListener != null) {
            mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
     //   Toast.makeText(getApplicationContext(), "destroy", Toast.LENGTH_LONG).show();
        System.exit(1);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void showNotification() {
// Using RemoteViews to bind custom layouts into Notification
        RemoteViews views = new RemoteViews(getPackageName(),
                R.layout.status_bar);
        RemoteViews bigViews = new RemoteViews(getPackageName(),
                R.layout.status_bar_expanded);


// showing default album image
        views.setViewVisibility(R.id.status_bar_icon, View.VISIBLE);
        views.setViewVisibility(R.id.status_bar_album_art, View.GONE);
        bigViews.setImageViewBitmap(R.id.status_bar_album_art,
                Constants.getDefaultAlbumArt(this));

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Intent previousIntent = new Intent(this, MyMusicService.class);
        previousIntent.setAction(Constants.ACTION.PREV_ACTION);
        PendingIntent ppreviousIntent = PendingIntent.getService(this, 0,
                previousIntent, 0);

        Intent playIntent = new Intent(this, MyMusicService.class);
        playIntent.setAction(Constants.ACTION.PLAY_ACTION);
        PendingIntent pplayIntent = PendingIntent.getService(this, 0,
                playIntent, 0);


        Intent nextIntent = new Intent(this, MyMusicService.class);
        nextIntent.setAction(Constants.ACTION.NEXT_ACTION);
        // nextIntent.setAction("Klkj");
        PendingIntent pnextIntent = PendingIntent.getService(this, 0,
                nextIntent, 0);

        Intent closeIntent = new Intent(this, MyMusicService.class);
        closeIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
        PendingIntent pcloseIntent = PendingIntent.getService(this, 0,
                closeIntent, 0);

        views.setOnClickPendingIntent(R.id.status_bar_next, pnextIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_next, pnextIntent);

        views.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent);

        views.setOnClickPendingIntent(R.id.status_bar_prev, ppreviousIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_prev, ppreviousIntent);

        views.setOnClickPendingIntent(R.id.status_bar_collapse, pcloseIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_collapse, pcloseIntent);

//
        views.setTextViewText(R.id.status_bar_track_name, MainActivity.songTitle);
        bigViews.setTextViewText(R.id.status_bar_track_name, MainActivity.songTitle);

        views.setTextViewText(R.id.status_bar_artist_name, MainActivity.artistName);
        bigViews.setTextViewText(R.id.status_bar_artist_name, MainActivity.artistName);

        bigViews.setTextViewText(R.id.status_bar_album_name, MainActivity.albumName);

        views.setImageViewResource(R.id.status_bar_play, R.drawable.ic_pause);
        bigViews.setImageViewResource(R.id.status_bar_play, R.drawable.ic_pause);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            status = new Notification.Builder(this, ANDROID_CHANNEL_ID).setAutoCancel(true).build();
            status.contentView = views;
            status.bigContentView = bigViews;
            status.flags = Notification.FLAG_ONGOING_EVENT;
            status.icon = R.drawable.music_image;
            status.contentIntent = pendingIntent;
            status.vibrate = new long[]{4, 4, 4, 4};

            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status);
        } else {
            status = new Notification.Builder(this).setAutoCancel(true).build();
            status.contentView = views;
            status.bigContentView = bigViews;
            status.flags = Notification.FLAG_ONGOING_EVENT;
            status.icon = R.drawable.music_image;
            status.vibrate = new long[]{4, 4, 4, 4};

            // status.setDefaults(Notification.DEFAULT_SOUND)
            status.contentIntent = pendingIntent;
            //builder.vibrate =Long.parseLong(Notification.DEFAULT_ALL+"");
            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status);
        }


    }

    @TargetApi(Build.VERSION_CODES.O)
    public void createChannels() {
        NotificationChannel androidChannel = new NotificationChannel(ANDROID_CHANNEL_ID,
                ANDROID_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
        androidChannel.enableLights(true);
        androidChannel.enableVibration(false);
        androidChannel.setLightColor(Color.GREEN);
        androidChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        androidChannel.setVibrationPattern(new long[]{2, 2, 2, 2});
        androidChannel.enableVibration(false);
        getManager(getApplicationContext()).createNotificationChannel(androidChannel);

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

        views.setImageViewResource(R.id.status_bar_play, R.drawable.ic_pause);
        bigViews.setImageViewResource(R.id.status_bar_play, R.drawable.ic_pause);
        views.setTextViewText(R.id.status_bar_track_name, MainActivity.songTitle);
        bigViews.setTextViewText(R.id.status_bar_track_name, MainActivity.songTitle);

        views.setTextViewText(R.id.status_bar_artist_name, MainActivity.artistName);
        bigViews.setTextViewText(R.id.status_bar_artist_name, MainActivity.artistName);

        bigViews.setTextViewText(R.id.status_bar_album_name, MainActivity.albumName);
        status.contentView = views;
        status.bigContentView = bigViews;

        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status);
    }

    @Override
    public void onAudioFocusChange(int i) {

    }

}
