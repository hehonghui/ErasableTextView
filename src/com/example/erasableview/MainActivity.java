
package com.example.erasableview;

import android.app.Activity;
import android.os.Bundle;

import com.example.erasable.R;

public class MainActivity extends Activity {

    private ErasableTextView mTextView = null;

    /**
     * (非 Javadoc)
     * 
     * @Title: onCreate
     * @Description:
     * @param savedInstanceState
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = ((ErasableTextView) findViewById(R.id.text_view));
        // 如果要设置宽度和高度则必须在调用setErasable之前
        mTextView.setErasableWidth(235);
        mTextView.setErasableHeight(80);
        mTextView.setErasable(true);

    }
}
