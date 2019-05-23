package org.linphone.bean;

/**
 * Created by miao on 2018/11/15.
 */
public class LoginInfoOut {
    /**
     * username : 4089
     * password : huawei123
     * mac : 1234-4567-7890
     * ip : 192.168.1.1
     */

    private String username;
    private String password;
    private String mac;
    private String ip;

    public LoginInfoOut() {
    }

    public LoginInfoOut(String username, String password, String mac, String ip) {

        this.username = username;
        this.password = password;
        this.mac = mac;
        this.ip = ip;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
