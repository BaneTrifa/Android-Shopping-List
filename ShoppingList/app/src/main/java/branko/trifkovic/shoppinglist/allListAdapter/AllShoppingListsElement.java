package branko.trifkovic.shoppinglist.allListAdapter;

public class AllShoppingListsElement {
    private String mTitle;
    private String mShared;
    private String mOwner;

    public AllShoppingListsElement(String mTitle, String mShared, String mOwner) {
        this.mTitle = mTitle;
        this.mShared = mShared;
        this.mOwner = mOwner;
    }

    public String getmTitle() {
        return mTitle;
    }

    public String getmShared() {
        return mShared;
    }

    public String getmOwner() {
        return mOwner;
    }
}
