package branko.trifkovic.shoppinglist.other;

public class User {
    private String mUsername;
    private String mEmail;
    private String mPassword;

    public User(String mUsername, String mEmail, String mPassword) {
        this.mUsername = mUsername;
        this.mEmail = mEmail;
        this.mPassword = mPassword;
    }

    public String getmUsername() {
        return mUsername;
    }

    public String getmEmail() {
        return mEmail;
    }

    public String getmPassword() {
        return mPassword;
    }
}
