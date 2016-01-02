package musicstream.gr33napps.com.musicstream;

import java.io.IOException;

/**
 * Created by bruno on 12/11/2015.
 */

public interface Callbacks {
    void successCallback(String response) throws IOException;

    void failCallback(String response);
}
