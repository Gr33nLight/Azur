package musicstream.gr33napps.com.musicstream;

/**
 * Created by W8 on 31/12/2015.
 */
public class Song extends VKSong {
    public Song(String title, String artist, String mp3, String id, String ownid) {
        super(title, artist, mp3, id, ownid);
    }

    public Song(String title, String artist, String id, String ownid) {
        super(title, artist, id, ownid);
    }

    public Song() {
    }

    public Song(String title, String artist) {
        super(title, artist);
    }

    public String toString(){
        return this.getArtist() + " " + this.getTitle() + " " + this.getMp3();
    }
}
