package org.linphone.activity;

/**
 * Created by miao on 2018/11/13.
 */
public class LoginAccountData {
    private String account;
    private String password;
    private int id;
    private int state;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public LoginAccountData() {

    }


    @Override
    public String toString() {
        return "[" + id + "::" + account + "::" + password + "::" + state + "]";
    }

    public LoginAccountData(String account, String password, int id, int state) {
        this.account = account;
        this.password = password;
        this.id = id;
        this.state = state;
    }
}
