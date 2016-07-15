package in.lunchbox.firebase.websync;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Create new user on the web database
 * Created by adhanas on 4/26/2016.
 */
public interface CreateNewUserAPI {

    @FormUrlEncoded
    @POST("/android_connect/create_user.php")
    Call<ResponseBody> createUser(
            @Field("firstName") String firstName,
            @Field("lastName") String lastName,
            @Field("phone") Long phone,
            @Field("address") String address,
            @Field("username") String username,
            @Field("password") String password,
            @Field("firebaseUserID") String userID);

}
