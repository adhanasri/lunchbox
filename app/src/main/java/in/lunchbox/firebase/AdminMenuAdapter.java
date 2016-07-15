package in.lunchbox.firebase;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.firebase.client.Firebase;

import java.util.ArrayList;

import Model.AdminMenuItem;
import Utils.Constants;

/**
 * Adapter to maintain the menu items shown in the Admin Menu screen
 * Created by adhanas on 6/17/2016.
 */
public class AdminMenuAdapter extends ArrayAdapter<AdminMenuItem>{

    private Activity context;
    private ArrayList<AdminMenuItem> listItems;

    EditText mTitleView;
    EditText mDescriptionView;
    EditText mPriceView;
    RadioButton mMealsButton;
    RadioButton mSnacksButton;

    Firebase menuItemRef;

    public AdminMenuAdapter(Activity context, int resource, ArrayList<AdminMenuItem> listItems) {
        super(context, resource, listItems);
        this.listItems = listItems;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = context.getLayoutInflater();
            v = vi.inflate(R.layout.admin_menu_item, null);
        }


        AdminMenuItem item = listItems.get(position);

        // populate list items with the menu information
        TextView itemTitleView = (TextView) v.findViewById(R.id.menuItemTitle);
        TextView itemDescView = (TextView) v.findViewById(R.id.menuItemDescription);
        TextView itemPriceView = (TextView) v.findViewById(R.id.menuItemPrice);
        SwitchCompat itemStatusSwitch = (SwitchCompat) v.findViewById(R.id.itemStatusSwitch);

        itemTitleView.setText(item.getName());
        itemDescView.setText(item.getDescription());
        itemPriceView.setText(String.format(context.getString(R.string.rupee_symbol), Double.toString(item.getPrice())));
        itemStatusSwitch.setChecked(item.getStatus().equals(Constants.ENABLED));

        // update menu item status based on the switch
        itemStatusSwitch.setTag(position);
        itemStatusSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String id = listItems.get((Integer)buttonView.getTag()).getId();
                menuItemRef = new Firebase(Constants.FIREBASE_URL + "/menu/" + id);
                menuItemRef.child("status").setValue(
                        listItems.get((Integer)buttonView.getTag()).getStatus().equals(Constants.ENABLED) ? Constants.DISABLED : Constants.ENABLED);
            }
        });

        // on click allow edit on menu item
        v.setTag(position);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuItemRef = new Firebase(Constants.FIREBASE_URL + "/menu/" + listItems.get((Integer)v.getTag()).getId());
                modifyMenuItem(v);
            }
        });

        return v;
    }

    private void modifyMenuItem(View v) {

        final View popupView = LayoutInflater.from(v.getContext()).inflate(R.layout.menu_popup, null);

        mTitleView = (EditText) popupView.findViewById(R.id.title);
        mDescriptionView = (EditText) popupView.findViewById(R.id.description);
        mPriceView = (EditText) popupView.findViewById(R.id.price);
        mMealsButton = (RadioButton) popupView.findViewById(R.id.meals);
        mSnacksButton = (RadioButton) popupView.findViewById(R.id.snacks);

        mTitleView.setText(listItems.get((Integer) v.getTag()).getName());
        mDescriptionView.setText(listItems.get((Integer) v.getTag()).getDescription());
        mPriceView.setText(listItems.get((Integer) v.getTag()).getPrice().toString());
        mMealsButton.setChecked(listItems.get((Integer) v.getTag()).getType().equals(Constants.MEALS_TYPE));
        mSnacksButton.setChecked(listItems.get((Integer) v.getTag()).getType().equals(Constants.SNACKS_TYPE));

        // build pop up dialog
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(v.getContext());
        dialogBuilder.setView(popupView)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        menuItemRef.child("name").setValue(mTitleView.getText().toString());
                        menuItemRef.child("description").setValue(mDescriptionView.getText().toString());
                        menuItemRef.child("price").setValue(Double.valueOf(mPriceView.getText().toString()));
                        String type = mMealsButton.isChecked() ? Constants.MEALS_TYPE : Constants.SNACKS_TYPE;
                        menuItemRef.child("type").setValue(type);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        final AlertDialog dialog = dialogBuilder.create();

        // show popup
        dialog.show();


    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }
}
