package in.lunchbox.firebase;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import Model.MenuItem;
import Utils.NumberUtil;

/**
 * Adapter to maintain the menu items view and actions shown on the Menu screen
 * Created by adhanas on 4/20/2016.
 */
public class MenuItemRVAdapter extends RecyclerView.Adapter<MenuItemViewHolder> {

    public List<MenuItem> menuItems;
    public Double totalAmount = NumberUtil.roundTo2Decimals(Double.valueOf("0.00"));
    public TextView mTotalAmountView;

    public MenuItemRVAdapter(List<MenuItem> menuItems){
        this.menuItems = menuItems;
    }
    @Override
    public MenuItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, null);
        return new MenuItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MenuItemViewHolder holder, final int position) {
        holder.mMenuItemTitleView.setText(menuItems.get(position).getName());
        holder.mMenuItemDescView.setText(menuItems.get(position).getDescription());
        holder.mPriceView.setText(String.format(holder.mPriceView.getContext().getString(R.string.rupee_symbol), Double.toString(menuItems.get(position).getPrice())));

        holder.mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItem(position, holder.mQuantityView);
            }
        });

        holder.mSubtractButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subtractItem(position, holder.mQuantityView);
            }
        });
    }

    /**
     * Add selected item
     * @param position int
     * @param quantityView TextView
     */
    private void addItem(int position, TextView quantityView){
        Integer itemQuantity = Integer.parseInt(quantityView.getText().toString());
        itemQuantity++;
        menuItems.get(position).setQuantity(itemQuantity);
        quantityView.setText(Integer.toString(itemQuantity));
        totalAmount = Double.valueOf(mTotalAmountView.getText().toString()) + (menuItems.get(position).getPrice());
        mTotalAmountView.setText(totalAmount.toString());
        notifyDataSetChanged();
    }

    /**
     * Subtract selected item
     * @param position int
     * @param quantityView TextView
     */
    private void subtractItem(int position, TextView quantityView){
        Integer itemQuantity = Integer.parseInt(quantityView.getText().toString());
        if(itemQuantity > 0){
            itemQuantity--;
            menuItems.get(position).setQuantity(itemQuantity);
            quantityView.setText(Integer.toString(itemQuantity));
            totalAmount = Double.valueOf(mTotalAmountView.getText().toString()) - (menuItems.get(position).getPrice());
            mTotalAmountView.setText(totalAmount.toString());
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        return menuItems.size();
    }

    public List<MenuItem> getMenuItems() {
        return menuItems;
    }

    public void setmTotalAmountView(TextView mTotalAmountView) {
        this.mTotalAmountView = mTotalAmountView;
    }
}
