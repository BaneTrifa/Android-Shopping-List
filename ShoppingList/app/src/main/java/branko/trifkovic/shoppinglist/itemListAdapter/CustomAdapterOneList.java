package branko.trifkovic.shoppinglist.itemListAdapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import branko.trifkovic.shoppinglist.activities.MainActivity;
import branko.trifkovic.shoppinglist.activities.WelcomeActivity;
import branko.trifkovic.shoppinglist.other.DbHelper;
import branko.trifkovic.shoppinglist.R;
import branko.trifkovic.shoppinglist.other.HttpHelper;

public class CustomAdapterOneList extends BaseAdapter {
    Context mContext;
    ArrayList<OneShoppingListElement> mElement;
    private static final String DB_NAME = "shared_list_app.db";

    public CustomAdapterOneList(Context mContext) {
        this.mContext = mContext;
        mElement = new ArrayList<OneShoppingListElement>();
    }

    @Override
    public int getCount() {
        return mElement.size();
    }

    @Override
    public Object getItem(int i) {
        Object rv = null;

        try {
            rv = mElement.get(i);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }


        return rv;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    private class ViewHolder {
        TextView taskTextView;
        CheckBox taskDoneCheckBox;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;

        if(view == null) {
            //inflate the layout for each list row
            LayoutInflater inflater = (LayoutInflater)
                    mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.element_row_one_shoping_list, null);

            viewHolder = new ViewHolder();
            viewHolder.taskTextView = (TextView) view.findViewById(R.id.task);
            viewHolder.taskDoneCheckBox = (CheckBox) view.findViewById(R.id.doneTaskCheckBox);

            view.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) view.getTag();
        }


        OneShoppingListElement element = (OneShoppingListElement) getItem(i);

        viewHolder.taskTextView.setText(element.getTask());
        viewHolder.taskDoneCheckBox.setChecked(element.ismDoneTask());


        if(viewHolder.taskDoneCheckBox.isChecked()) {
            viewHolder.taskTextView.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            viewHolder.taskTextView.setPaintFlags( Paint.ANTI_ALIAS_FLAG);
        }

        viewHolder.taskDoneCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DbHelper db = new DbHelper(mContext, DB_NAME, null, 1);

                if(viewHolder.taskDoneCheckBox.isChecked()) {
                    viewHolder.taskTextView.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                    element.setmDoneTask(true);
                    db.updateItemChecked(element.getId(), true);
                    updateCheckBox(element.getId(), true);
                } else {
                    db.updateItemChecked(element.getId(), false);
                    updateCheckBox(element.getId(), false);
                    viewHolder.taskTextView.setPaintFlags( Paint.ANTI_ALIAS_FLAG);
                    element.setmDoneTask(false);
                }
            }
        });

        return view;
    }

    private void updateCheckBox(String id, boolean isChecked) {
        HttpHelper httpHelper = new HttpHelper();

        String URL = mContext.getResources().getString(R.string.POST_CHANGE_DONE_URL);

        new Thread(new Runnable() {
            public void run() {
                try {
                    JSONObject httpBody = new JSONObject();
                    httpBody.put("task_id", id);
                    httpBody.put("is_done", isChecked);

                    httpHelper.postJSONObjectFromURL(URL, httpBody);

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void addElement(OneShoppingListElement el) {
        mElement.add(el);
        notifyDataSetChanged();
    }
    public void removeElement(OneShoppingListElement el) {
        mElement.remove(el);
        notifyDataSetChanged();
    }

    public void update(OneShoppingListElement[] items) {
        mElement.clear();
        if(items != null) {
            for(OneShoppingListElement item : items) {
                mElement.add(item);
            }
        }

        notifyDataSetChanged();
    }

    public void clear() {
        mElement.clear();
        notifyDataSetChanged();
    }
}
