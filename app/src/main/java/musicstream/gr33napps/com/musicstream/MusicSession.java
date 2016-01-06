package musicstream.gr33napps.com.musicstream;

import android.content.Context;
import android.support.v4.media.session.MediaSessionCompat;

/**
 * Created by W8 on 04/01/2016.
 */
public class MusicSession extends MediaSessionCompat {
    public MusicSession(Context context, String tag) {
        super(context, tag);
    }


}
