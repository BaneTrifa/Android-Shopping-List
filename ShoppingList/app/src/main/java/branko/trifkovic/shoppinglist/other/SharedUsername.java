package branko.trifkovic.shoppinglist.other;

// Singleton pattern - shares variable username between Welcome Acivity and MyService class
public class SharedUsername {
    private static SharedUsername instance;
    private String mUsername;

    private SharedUsername() {
    }

    public static SharedUsername getInstance() {
        if (instance == null) {
            instance = new SharedUsername();
        }
        return instance;
    }

    public String getSharedVariable() {
        return mUsername;
    }

    public void setSharedVariable(String sharedVariable) {
        this.mUsername = sharedVariable;
    }
}
