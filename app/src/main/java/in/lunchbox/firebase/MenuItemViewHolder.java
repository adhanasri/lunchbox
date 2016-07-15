package in.lunchbox.firebase;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * Holder class to maintain the items shown on the Menu screen
 * Created by adhanas on 4/20/2016.
 */
public class MenuItemViewHolder extends RecyclerView.ViewHolder {
    CardView mCardView;
    TextView mMenuItemTitleView;
    TextView mMenuItemDescView;
    TextView mPriceView;
    ImageButton mAddButton;
    ImageButton mSubtractButton;
    TextView mQuantityView;

    public MenuItemViewHolder(View itemView) {
        super(itemView);

        this.mCardView = (CardView) itemView.findViewById(R.id.menuItemCard);
        this.mMenuItemTitleView = (TextView) itemView.findViewById(R.id.menuItemTitle);
        this.mMenuItemDescView = (TextView) itemView.findViewById(R.id.menuItemDescription);
        this.mPriceView = (TextView) itemView.findViewById(R.id.menuItemPrice);
        this.mQuantityView = (TextView) itemView.findViewById(R.id.itemQuantity);
        this.mAddButton = (ImageButton) itemView.findViewById(R.id.add);
        this.mSubtractButton = (ImageButton) itemView.findViewById(R.id.subtract);
    }
}
