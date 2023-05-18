package branko.trifkovic.shoppinglist.allListAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import branko.trifkovic.shoppinglist.R;

public class CustomAdapterAllLists extends BaseAdapter {
    private Context mContext;
    private ArrayList<AllShoppingListsElement> mElement;


    public CustomAdapterAllLists(Context mContext) {
        this.mContext = mContext;
        mElement = new ArrayList<AllShoppingListsElement>();
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
        } catch(IndexOutOfBoundsException e) {
            e.printStackTrace();
        }

        return rv;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }


    private class ViewHolder {
        public TextView mTitleTextView;
        public TextView mSharedTextView;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;

        if(view == null) {
            LayoutInflater inflater = (LayoutInflater)
                    mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.elemt_row_all_shopping_lists, null);

            viewHolder = new ViewHolder();
            viewHolder.mTitleTextView = view.findViewById(R.id.shoppingListTitleTextView);
            viewHolder.mSharedTextView = view.findViewById(R.id.shoppingListSharedTextView);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        AllShoppingListsElement el = (AllShoppingListsElement) getItem(i);
        viewHolder.mTitleTextView.setText(el.getmTitle().toString());
        viewHolder.mSharedTextView.setText(el.getmShared().toString());

        return view;
    }

    public void addElement(AllShoppingListsElement character) {
        mElement.add(character);
        notifyDataSetChanged();
    }
    public void removeElement(AllShoppingListsElement character) {
        mElement.remove(character);
        notifyDataSetChanged();
    }

    public void update(AllShoppingListsElement[] lists) {
        mElement.clear();
        if(lists != null) {
            for(AllShoppingListsElement list : lists) {
                mElement.add(list);
            }
        }

        notifyDataSetChanged();
    }

    public void clear() {
        mElement.clear();
        notifyDataSetChanged();
    }
}
