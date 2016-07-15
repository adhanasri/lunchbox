package in.lunchbox.firebase;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ListViewCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;

import Model.AdminMenuItem;
import Utils.Constants;

public class AdminMenuActivity extends AppCompatActivity {

    Firebase ref;

    ListViewCompat mMenuListView;
    ArrayList<AdminMenuItem> menuItemsList;

    AdminMenuAdapter listAdapter;

    EditText mTitleView;
    EditText mDescriptionView;
    EditText mPriceView;
    RadioButton mMealsButton;
    RadioButton mSnacksButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_menu);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (null != getSupportActionBar()){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (null != fab){
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addMenuItem();
                }
            });
        }

        ref = new Firebase(Constants.FIREBASE_URL);

        mMenuListView = (ListViewCompat) findViewById(R.id.adminMenuList);

        menuItemsList = new ArrayList<>();

        listAdapter = new AdminMenuAdapter(this, android.R.layout.simple_list_item_2, menuItemsList);

        mMenuListView.setAdapter(listAdapter);

        updateMenuItems();

    }

    /**
     * Method to show the pop up to add new menu items
     */
    private void addMenuItem() {

        final View popupView = getLayoutInflater().inflate(R.layout.menu_popup, null);

        // build pop up dialog
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(popupView)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Firebase menuRef = new Firebase(Constants.FIREBASE_URL + "/menu");
                        Firebase menuItemRef = menuRef.push();
                        String type = mMealsButton.isChecked() ? Constants.MEALS_TYPE : Constants.SNACKS_TYPE;
                        AdminMenuItem menuItem = new AdminMenuItem(mTitleView.getText().toString(),
                                                        mDescriptionView.getText().toString(),
                                                        Double.valueOf(mPriceView.getText().toString()),
                                                        Constants.ENABLED, type);
                        menuItem.setId("1");
                        menuItemRef.setValue(menuItem);
                        dialog.dismiss();
                        Toast.makeText(AdminMenuActivity.this, "Item added successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        final AlertDialog dialog = dialogBuilder.create();

        mTitleView = (EditText) popupView.findViewById(R.id.title);
        mDescriptionView = (EditText) popupView.findViewById(R.id.description);
        mPriceView = (EditText) popupView.findViewById(R.id.price);
        mMealsButton = (RadioButton) popupView.findViewById(R.id.meals);
        mSnacksButton = (RadioButton) popupView.findViewById(R.id.snacks);

        // show popup
        dialog.show();
    }

    /**
     * Method to read the menu list from firebase to populate the admin menu screen
     */
    private void updateMenuItems() {

        Firebase menuRef = new Firebase(Constants.FIREBASE_URL + "/menu");
        menuRef.orderByChild("name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                menuItemsList.clear();
                Iterable<DataSnapshot> childrenSnapshots = dataSnapshot.getChildren();
                for (DataSnapshot child : childrenSnapshots) {
                    AdminMenuItem item = child.getValue(AdminMenuItem.class);
                    item.setId(child.getKey());
                    menuItemsList.add(item);
                }
                //addBufferListItems();
                listAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    /**
     * Method to add dummy list items to allow the admin to scroll items above the android nav bar
     */
    private void addBufferListItems(){
        AdminMenuItem item = new AdminMenuItem("", "", Double.valueOf("0"), "", "");
        menuItemsList.add(item);
        menuItemsList.add(item);
    }

}
