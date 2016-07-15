package in.lunchbox.firebase;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import Utils.Constants;

public class ContactActivity extends AppCompatActivity {

    TextView mAddressView;
    TextView mPhoneView;
    TextView mEmailView;
    TextView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        if (null != getSupportActionBar()){
            getSupportActionBar().setTitle("Contact Us");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mAddressView = (TextView) findViewById(R.id.contactAddress);
        mAddressView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMapsWithStoreLocation();
            }
        });

        mPhoneView = (TextView) findViewById(R.id.contactPhone);
        mPhoneView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callStore();
            }
        });

        mEmailView = (TextView) findViewById(R.id.contactEmail);
        mEmailView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailAdmin();
            }
        });

        mWebView = (TextView) findViewById(R.id.contactWeb);
        mWebView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoWebSite();
            }
        });
    }

    /**
     * Method to open google maps to show the store location
     */
    private void openMapsWithStoreLocation(){
        Uri uri = Uri.parse("geo:" + Constants.storeLatitude + "," + Constants.storeLongitude);
        Intent mapsIntent = new Intent(Intent.ACTION_VIEW, uri);
        mapsIntent.setPackage("com.google.android.apps.maps");
        if (mapsIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapsIntent);
        }
    }

    /**
     * Method to allow the customer to call the store
     */
    private void callStore(){
        Intent phoneIntent = new Intent(Intent.ACTION_DIAL);
        phoneIntent.setData(Uri.parse("tel:" + mPhoneView.getText().toString()));
        startActivity(phoneIntent);
    }

    /**
     * Method to allow customer to email admin
     */
    private void emailAdmin(){
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {mEmailView.getText().toString()});
        startActivity(emailIntent);
    }

    /**
     * Method to open browser with the web address
     */
    private void gotoWebSite(){
        Intent webIntent = new Intent(Intent.ACTION_VIEW);
        webIntent.setData(Uri.parse(mWebView.getText().toString()));
        startActivity(webIntent);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }
}
