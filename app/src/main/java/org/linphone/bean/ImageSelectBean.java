package org.linphone.bean;

import android.net.Uri;

public class ImageSelectBean {
    private boolean checked;
    private Uri uri;
    private String path;

    public ImageSelectBean(Uri uri,String path) {
        this.uri = uri;
        this.path = path;
        checked = false;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}

