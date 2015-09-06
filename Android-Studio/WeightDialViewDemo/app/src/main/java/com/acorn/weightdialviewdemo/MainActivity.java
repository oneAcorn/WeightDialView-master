package com.acorn.weightdialviewdemo;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.acorn.weightdiallibrary.WeightDialView;
import com.acorn.weightdialviewdemo.utils.ImageUtil;


public class MainActivity extends ActionBarActivity {

    private WeightDialView weightDialView, weightDialView2;
    private float curNumber;
    private boolean flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // testIV= (ImageView) findViewById(R.id.test_iv);
        // WeightScaleDrawable drawable=new WeightScaleDrawable();
        // drawable.setBounds(0,0,200,200);
        // testIV.setImageDrawable(drawable);
        curNumber = 32.33f;
        weightDialView = (WeightDialView) findViewById(R.id.weightdialview);
        weightDialView2 = (WeightDialView) findViewById(R.id.weightdialview2);
        weightDialView.setText(String.valueOf(curNumber));
        weightDialView.setCircle(61);
        weightDialView.setScale(33);
        weightDialView.setTextSize(16);
        weightDialView
                .setOnScaleChangeListener(new WeightDialView.OnScaleChangeListener() {
                    @Override
                    public void onScaleChange(int newScale,
                                              boolean isClockwise, int circles) {
                        weightDialView.setText("" + circles + "." + newScale
                                + "kg");
                    }
                });

        findViewById(R.id.change_btn).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (flag)
                            weightDialView.setCircleBackground(null);
                        else
                            weightDialView.setCircleBackground(ImageUtil
                                    .drawableToBitamp(getResources()
                                            .getDrawable(R.mipmap.bg2)));
                        flag = !flag;
                    }
                });

        weightDialView2.setScale(2);
        weightDialView2.setTextSize(22);
        weightDialView2.setCircleBackground(ImageUtil
                .drawableToBitamp(getResources().getDrawable(R.mipmap.bg)));
        weightDialView2.setScaleLineColor(0xff000000);
        weightDialView2
                .setOnScaleChangeListener(new WeightDialView.OnScaleChangeListener() {
                    @Override
                    public void onScaleChange(int newScale,
                                              boolean isClockwise, int circles) {
                        weightDialView2.setText("" + newScale + ","
                                + (isClockwise ? "t" : "f") + "," + circles);
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
