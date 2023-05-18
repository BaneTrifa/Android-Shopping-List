package branko.trifkovic.shoppinglist.itemListAdapter;

public class OneShoppingListElement {
    private String mTask;
    private boolean mDoneTask;
    private String id;


    public OneShoppingListElement(String task, boolean done, String id) {
        this.mTask = task;
        this.mDoneTask = done;
        this.id = id;
    }

    public String getTask() {
        return mTask;
    }

    public String getId() {
        return id;
    }

    public boolean ismDoneTask() {
        return mDoneTask;
    }

    public void setmDoneTask(boolean mDoneTask) {
        this.mDoneTask = mDoneTask;
    }

}
