package musicstream.gr33napps.com.musicstream;

/**
 * Created by bruno on 1/5/2016.
 */
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Constants {
    public interface ACTION {
        String MAIN_ACTION = "com.gr33napps.azur.action.main";
        String INIT_ACTION = "com.gr33napps.azur.action.init";
        String PREV_ACTION = "com.gr33napps.azur.action.prev";
        String PLAY_ACTION = "com.gr33napps.azur.action.play";
        String NEXT_ACTION = "com.gr33napps.azur.action.next";
        String STARTFOREGROUND_ACTION = "com.gr33napps.azur.action.startforeground";
        String STOPFOREGROUND_ACTION = "com.gr33napps.azur.action.stopforeground";

    }

    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 101;
    }

    public static Bitmap getDefaultAlbumArt(Context context) {
        Bitmap bm = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        try {
            bm = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.no_art_placeholder, options);
        } catch (Error | Exception e) {
            e.printStackTrace();
        }
        return bm;
    }

}