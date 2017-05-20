package com.example.slidingpanellayout;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.slidingpanellayout.widget.SlidingPanelLayout;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private SlidingPanelLayout mSlidingPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSlidingPanel = (SlidingPanelLayout) findViewById(R.id.sliding_panel);
    }


    @Override
    public void onBackPressed() {
        if(mSlidingPanel.getPanelState()
                == SlidingPanelLayout.STATE_OPENED_ENTIRELY) {
            mSlidingPanel.openPanel();
        } else {
            super.onBackPressed();
        }
    }
}
