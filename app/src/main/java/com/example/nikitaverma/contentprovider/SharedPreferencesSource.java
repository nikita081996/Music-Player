package com.example.nikitaverma.contentprovider;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;

import static com.example.nikitaverma.contentprovider.Constants.ACTION.CURRENT_POSITION;
import static com.example.nikitaverma.contentprovider.Constants.ACTION.LISTVIEWPOSITION;

/**
 * SharedPreference Utility Class
 */
public class SharedPreferencesSource extends AppCompatActivity {
    SharedPreferences sp;

    public SharedPreferencesSource(Context context){
        sp = context.getSharedPreferences("SAVED DATA", Context.MODE_PRIVATE);
    }

    public int[] getData(){
        int[] data = new int[2];

        data[0] = sp.getInt(LISTVIEWPOSITION,0);
        data[1] = sp.getInt(CURRENT_POSITION,0);
        return data;
    }

    public void setData(int lvp,int CURRENTPOSITION){
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(LISTVIEWPOSITION,lvp);
        editor.putInt(CURRENT_POSITION,CURRENTPOSITION);
        editor.apply();
        //      editor.commit();
    }

}
