package in.lunchbox.firebase.websync;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import Utils.JSONParser;

/**
 * Create new user on the web database
 * Created by adhanas on 4/24/2016.
 */
public class CreateNewUser extends AsyncTask<String, Void, String> {

    private static final String TAG = CreateNewUser.class.getSimpleName();

    JSONParser jsonParser = new JSONParser();

    // url to create new user
    private static String url_create_user = "http://api.androidhive.info/android_connect/create_product.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... credentials) {

        String email = credentials[0];
        String password = credentials[1];
        String userID = credentials[2];

        // Building Parameters
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("username", email));
        params.add(new BasicNameValuePair("password", password));
        params.add(new BasicNameValuePair("firebaseID", userID));

        // getting JSON Object
        // Note that create product url accepts POST method
        JSONObject json = jsonParser.makeHttpRequest(url_create_user,
                "POST", params);

        // check log cat fro response
        Log.d("Create Response", json.toString());

        // check for success tag
        try {
            int success = json.getInt(TAG_SUCCESS);

            if (success == 1) {
                // successfully created user
                Log.i(TAG, "User created successfully");
                Log.i(TAG, email);
            } else {
                // failed to create user
                Log.i(TAG, "User creation failed");
                Log.e(TAG, email);
            }
        } catch (JSONException e) {
            Log.e("JSONException", e.getMessage());
        }

        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }
}
