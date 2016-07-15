package in.lunchbox.firebase;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Holder class to maintain the items shown on the home screen
 * Created by adhanas on 4/19/2016.
 */
public class HistoryItemViewHolder extends RecyclerView.ViewHolder {

    CardView historyItemCardView;
    TextView historyItemTitleView;
    TextView historyItemDeliveryInfoView;
    TextView historyItemStatusView;
    /*Button orderAgainButton;
    Button trackDeliveryButton;
    Button cancelOrderButton;
    Button orderButton;*/

    public HistoryItemViewHolder(View itemView) {
        super(itemView);

        this.historyItemCardView = (CardView) itemView.findViewById(R.id.historyItemCard);
        this.historyItemTitleView = (TextView) itemView.findViewById(R.id.historyItemTitle);
        this.historyItemStatusView = (TextView) itemView.findViewById(R.id.historyItemStatus);
        this.historyItemDeliveryInfoView = (TextView) itemView.findViewById(R.id.historyItemDeliveryInfo);
        /*this.orderAgainButton = (Button) itemView.findViewById(R.id.orderAgain);
        this.trackDeliveryButton = (Button) itemView.findViewById(R.id.trackDelivery);
        this.cancelOrderButton = (Button) itemView.findViewById(R.id.cancelOrder);
        this.orderButton = (Button) itemView.findViewById(R.id.order);*/
    }
}
