package musicstream.gr33napps.com.musicstream;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

/**
 * Created by Cristy on 04/01/2016.
 */
public class RemoteControlReceiver extends BroadcastReceiver {
    private String TAG="Debug";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "got action!!!!");
        if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
            KeyEvent event = (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

            if (KeyEvent.KEYCODE_MEDIA_PLAY == event.getKeyCode()) {
                // Handle key press.
            }
        }
    }
}