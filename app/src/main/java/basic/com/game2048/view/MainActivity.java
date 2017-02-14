package basic.com.game2048.view;

import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;

import java.util.HashMap;
import java.util.Map;

import basic.com.game2048.R;

/**
 * Created by basic on 2017/2/7.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        SparseArray<String> array = new SparseArray<>();
        array.put(5, "e");
        array.put(1, "a");
        array.put(4, "d");
        array.put(3, "c");
        array.put(2, "b");

        for (int i = 0; i < 5; i++){
            int key = array.keyAt(i);
            Log.d("wls", ">>>>>>>>>: arrar: " + array.get(key));
        }
    }
}
