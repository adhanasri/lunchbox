package Utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Util class to contain all methods related to number operations
 * Created by adhanas on 4/19/2016.
 */
public class NumberUtil {
    public static double roundTo2Decimals(double val) {
        return new BigDecimal(val).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
