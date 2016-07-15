package in.lunchbox.firebase;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import Model.CustomerInfo;
import Model.MenuItem;
import Utils.Constants;

public class MenuActivity extends AppCompatActivity implements MealsFragment.OnFragmentInteractionListener, SnacksFragment.OnFragmentInteractionListener{

    TextView mTotalAmountView;
    String customerLatitude;
    String customerLongitude;

    CustomerInfo customerInfo = null;

    SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (null != getSupportActionBar()){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mSharedPreferences = getSharedPreferences(Constants.MyPREFERENCES, Context.MODE_PRIVATE);

        Intent intent = getIntent();
        customerLatitude = intent.getStringExtra("customerLatitude");
        customerLongitude = intent.getStringExtra("customerLongitude");
        if (null != customerLatitude && null != customerLongitude){
            Log.i("Customer Latitude", customerLatitude);
            Log.i("Customer Longitude", customerLongitude);
        }

        if (null != intent.getSerializableExtra("customerInfo")){
            customerInfo = (CustomerInfo) intent.getSerializableExtra("customerInfo");
        }

        mTotalAmountView = (TextView) findViewById(R.id.totalAmountText);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        TabLayout mTabLayout = (TabLayout) findViewById(R.id.tabs);
        if (null != mTabLayout){
            mTabLayout.setupWithViewPager(viewPager);
        }

        Button mConfirmMenuButton = (Button) findViewById(R.id.confirmMenu);
        if (mConfirmMenuButton != null){
            mConfirmMenuButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    confirmMenuItems();
                }

            });
        }



    }

    /**
     * Set up the pages for tabs on Menu screes
     * @param viewPager ViewPager
     */
    private void setupViewPager(ViewPager viewPager) {
        SnacksFragment snacksFragment = new SnacksFragment();
        snacksFragment.setmTotalAmountTextView(mTotalAmountView);
        MealsFragment mealsFragment = new MealsFragment();
        mealsFragment.setmTotalAmountTextView(mTotalAmountView);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(mealsFragment, "Meals");
        adapter.addFragment(snacksFragment, "Snacks");
        viewPager.setAdapter(adapter);
    }

    /**
     * Class to handle the tabs on Menu screen
     */
    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

    }

    /**
     * Confirm menu items selected and move to next screen
     */
    private void confirmMenuItems(){
        List<MenuItem> orderItems = new ArrayList<>();
        // get items selected and cart information
        Double totalAmount = getItemsSelectedFromTabs(orderItems);

        if (!totalAmount.equals(Double.valueOf("0.00"))){
            // send data to order activity
            gotoOrderActivity(customerInfo, orderItems, totalAmount, Double.valueOf(customerLatitude), Double.valueOf(customerLongitude));
        } else {
            Toast.makeText(MenuActivity.this, "Please add at least one item to your box", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Get items selected on Menu screen from all the tabs and cart information
     * @param orderItems List<MenuItem>
     * @return Double
     */
    private Double getItemsSelectedFromTabs(List<MenuItem> orderItems){
        Double totalAmount = Double.valueOf("0.00");
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment fragment : fragments){
            RecyclerView recyclerView = (RecyclerView) fragment.getView().findViewById(R.id.mealsListView);
            MenuItemRVAdapter rvAdapter = (MenuItemRVAdapter)recyclerView.getAdapter();
            List<MenuItem> menuItems = rvAdapter.getMenuItems();
            for (MenuItem menuItem : menuItems){
                if (menuItem.getQuantity() > 0){
                    orderItems.add(menuItem);
                    Log.i(menuItem.getName(), Integer.toString(menuItem.getQuantity()));
                    totalAmount = totalAmount + menuItem.getQuantity() * menuItem.getPrice();
                    Log.i("Price ", Double.toString(menuItem.getQuantity() * menuItem.getPrice()));
                }
            }
        }
        return totalAmount;
    }

    /**
     * Navigate to Order screen
     * @param customerInfo CustomerInfo
     * @param orderItems List<MenuItem>
     * @param totalAmount Double
     * @param latitude Double
     * @param longitude Double
     */
    private void gotoOrderActivity(CustomerInfo customerInfo, List<MenuItem> orderItems, Double totalAmount, Double latitude, Double longitude){
        Intent intent = new Intent(getApplicationContext(), OrderActivity.class);
        intent.putExtra("customerInfo", customerInfo);
        intent.putExtra("orderItems", (Serializable) orderItems);
        intent.putExtra("totalAmount", totalAmount);
        intent.putExtra("deliveryLatitude", latitude);
        intent.putExtra("deliveryLongitude", longitude);
        intent.putExtra("showBackButton", false);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_customer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                break;
            case R.id.action_signout:
                Firebase ref = new Firebase(Constants.FIREBASE_URL);
                ref.unauth();
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.clear();
                editor.apply();
                Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(loginIntent);
                break;
            case R.id.action_settings:

                break;
            case R.id.action_contact:
                Intent contactIntent = new Intent(getApplicationContext(), ContactActivity.class);
                startActivity(contactIntent);
                break;
        }
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

}
