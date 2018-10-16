package com.example.nikitaverma.contentprovider;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Constants {
    public interface ACTION {
        public static String MAIN_ACTION = "com.marothiatechs.customnotification.action.main";
        public static String PAUSE_ACTION = "com.marothiatechs.customnotification.action.pause";
        public static String PREV_ACTION = "com.marothiatechs.customnotification.action.prev";
        public static String PLAY_ACTION = "com.marothiatechs.customnotification.action.play";
        public static String PLAY_ACTION_0 = "com.marothiatechs.customnotification.action.play0";
        public static String NEXT_ACTION = "com.marothiatechs.customnotification.action.next";
        public static String STARTFOREGROUND_ACTION = "com.marothiatechs.customnotification.action.startforeground";
        public static String STOPFOREGROUND_ACTION = "com.marothiatechs.customnotification.action.stopforeground";
        String ANDROID_CHANNEL_ID = "com.example.nikitaverma.notification.ANDROID";
        String IOS_CHANNEL_ID = "com.example.nikitaverma.notification.IOS";
        String ANDROID_CHANNEL_NAME = "ANDROID CHANNEL";
        String IOS_CHANNEL_NAME = "IOS CHANNEL";
        String groupId = "groupid101";
        CharSequence groupName = "Channel Name";
        String LISTVIEWPOSITION = "LISTVIEWPOSITION";
        String CURRENT_POSITION = "CURRENT POSITION";

    }

    public interface NOTIFICATION_ID {
        int FOREGROUND_SERVICE = 101;
    }

    public static Bitmap getDefaultAlbumArt(Context context) {
        Bitmap bm = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        try {
            bm = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.music_image, options);
        } catch (Error ee) {
        } catch (Exception e) {
        }
        return bm;
    }

}