package Utils;

import android.util.Patterns;

/**
 * Util class to contain all the validations
 * Created by adhanas on 4/18/2016.
 */
public class ValidationUtil {

    public static boolean isEmailValid(String email){
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isPasswordValid(String password) {
        boolean valid = true;
        String passwordPattern = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{4,8}$";
        if (!password.matches(passwordPattern)){
            valid = false;
        }

        return valid;
    }

    public static boolean isPhoneValid(String phone) {
        boolean valid = true;
        String phoneNumberPatter = "^((\\+){0,1}91(\\s){0,1}(\\-){0,1}(\\s){0,1}){0,1}[2-9](\\s){0,1}(\\-){0,1}(\\s){0,1}[1-9]{1}[0-9]{8}$";
        if (!phone.matches(phoneNumberPatter)){
            valid = false;
        }
        return valid;
    }
}
