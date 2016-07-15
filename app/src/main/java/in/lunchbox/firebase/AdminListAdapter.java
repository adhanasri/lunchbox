package in.lunchbox.firebase;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import Model.AdminListItem;

/**
 * Adapter to maintain the list items shown in the Admin Home screen
 * Created by adhanas on 4/10/2016.
 */
public class AdminListAdapter extends ArrayAdapter<AdminListItem> {


    private ArrayList<AdminListItem> listItems;

    private Activity context;


    public AdminListAdapter(Activity context, int resource, ArrayList<AdminListItem> listItems) {
        super(context, resource, listItems);
        this.listItems = listItems;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = context.getLayoutInflater();
            v = vi.inflate(R.layout.admin_list_item, null);
        }


        AdminListItem item = listItems.get(position);

        // populate list items with the order information
        TextView textView1 = (TextView) v.findViewById(R.id.orderIDText);
        TextView textView2 = (TextView) v.findViewById(R.id.orderDetailsText);
        if (!item.getOrderID().equals("0")){
            textView1.setText(String.format(v.getContext().getString(R.string.boxID_label), item.getOrderID()));
            textView2.setText(item.getOrderDetails());
        } else {
            textView1.setText(" ");
            textView2.setText(" ");
        }

        return v;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }
}
