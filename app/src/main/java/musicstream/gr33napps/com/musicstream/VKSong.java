package musicstream.gr33napps.com.musicstream;

/**
 * Created by bruno on 12/8/2015.
 */
public class VKSong {
    public String title = "Untitled", artist = "Unknown artist", mp3 = "null",id = "", ownid = "";

    public VKSong(String title, String artist, String mp3, String id, String ownid) {
        this.title = title;
        this.artist = artist;
        this.mp3 = mp3;
        this.id = id;
        this.ownid = ownid;
    }

    public VKSong(String title, String artist, String id, String ownid) {
        this.title = title;
        this.artist = artist;
        this.id = id;
        this.ownid = ownid;
    }

    public VKSong(String title, String artist) {
        this.title = title;
        this.artist = artist;
    }

    public VKSong() {
    }

    public String getTitle() {
        return title;
    }

    public String getId() {
        return id;
    }

    public String getOwnid() {
        return ownid;
    }

    public String getArtist() {
        return artist;
    }

    public String getMp3() {
        return mp3;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setMp3(String mp3) {
        this.mp3 = mp3;
    }

    public void setOwnid(String ownid) {
        this.ownid = ownid;
    }

    public void setId(String id) {
        this.id = id;
    }
}

