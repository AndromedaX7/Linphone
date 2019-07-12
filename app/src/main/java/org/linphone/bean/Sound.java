package org.linphone.bean;

/**
 * Created by 62420 on 2019/7/11 15:40.
 */
public class Sound {

    private String name;
    private String type;
    private String id;
    private String location;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = "android.resource://org.linphone/raw/" + location;
    }
}
