package in.lunchbox.firebase;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import Model.AdminMenuItem;
import Model.MenuItem;
import Utils.Constants;
import Utils.NumberUtil;


/**
 * Fragment to maintain Meals items on the Menu
 */
public class MealsFragment extends Fragment {

    public List<MenuItem> menuItems;

    public TextView mTotalAmountTextView;

    private OnFragmentInteractionListener mListener;

    public MealsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_meals, container, false);

        RecyclerView mealsListView = (RecyclerView) view.findViewById(R.id.mealsListView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mealsListView.setLayoutManager(layoutManager);

        initializeData();

        MenuItemRVAdapter menuItemRVAdapter = new MenuItemRVAdapter(menuItems);
        menuItemRVAdapter.setmTotalAmountView(mTotalAmountTextView);
        mealsListView.setAdapter(menuItemRVAdapter);

        return view;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
         void onFragmentInteraction(Uri uri);
    }

    /**
     * Initialize the meals items for the meals fragment
     */
    private void initializeData(){
        menuItems = new ArrayList<>();

        Firebase menuRef = new Firebase(Constants.FIREBASE_URL + "/menu");
        menuRef.orderByChild("name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> childrenSnapshots = dataSnapshot.getChildren();
                for (DataSnapshot child : childrenSnapshots) {
                    AdminMenuItem item = child.getValue(AdminMenuItem.class);
                    if (item.getType().equals(Constants.MEALS_TYPE) && item.getStatus().equals(Constants.ENABLED)){
                        menuItems.add(new MenuItem(item.getName(), item.getDescription(), item.getPrice(), 0));
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }

    public void setmTotalAmountTextView(TextView mTotalAmountTextView) {
        this.mTotalAmountTextView = mTotalAmountTextView;
    }
}
