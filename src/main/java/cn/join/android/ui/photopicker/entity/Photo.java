package cn.join.android.ui.photopicker.entity;

/**
 * Created by donglua on 15/6/30.
 */
public class Photo {

    private String path;

    public Photo(String path) {
        this.path = path;
    }

    public Photo() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Photo photo = (Photo) o;

        return path != null ? path.equals(photo.path) : photo.path == null;

    }

    @Override
    public int hashCode() {
        return path != null ? path.hashCode() : 0;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
