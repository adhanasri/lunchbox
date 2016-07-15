package in.lunchbox.firebase.websync;

import Model.Order;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Create new order on the web database
 * Created by adhanas on 4/26/2016.
 */
public interface CreateNewOrderAPI {

    @FormUrlEncoded
    @POST("/lunchbox/create_order.php")
    Call<Response> createOrder(@Body Order order);
}
