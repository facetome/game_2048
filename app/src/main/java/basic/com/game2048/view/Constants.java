package basic.com.game2048.view;

import android.graphics.Color;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by basic on 2017/2/8.
 */
public final class Constants {
    private static final Map<Integer, String> COLOR_MAP = new HashMap<>();

    static {
        COLOR_MAP.put(2, "#EEE4DA");
        COLOR_MAP.put(4, "#EDE0C8");
        COLOR_MAP.put(8, "#F2B179");
        COLOR_MAP.put(16, "#F49563");
        COLOR_MAP.put(32, "#F5794D");
        COLOR_MAP.put(64, "#F55D37");
        COLOR_MAP.put(128, "#EEE863");
        COLOR_MAP.put(256, "#EDB04D");
        COLOR_MAP.put(512, "#ECB04D");
        COLOR_MAP.put(1024, "#EB9437");
        COLOR_MAP.put(2048, "#EA7821");
    }

    /**
     * 返回指定数据的颜色.
     *
     * @param value
     * @return
     */
    public static int getColorInValue(int value) {
        return Color.parseColor(COLOR_MAP.get(value));
    }
}
