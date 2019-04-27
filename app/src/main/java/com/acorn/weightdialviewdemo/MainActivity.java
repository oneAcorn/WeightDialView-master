package com.acorn.weightdialviewdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.acorn.weightdiallibrary.WeightDialView;
import com.acorn.weightdialviewdemo.utils.ImageUtil;

import java.text.DecimalFormat;


public class MainActivity extends AppCompatActivity {
    private WeightDialView weightDialView;
    private Button bgBtn, listenerBtn;
    private TextView listenerTv;
    private TextView statusTv;
    private WeightDialView.OnScaleChangeListener mScaleChangeListener = new WeightDialView.OnScaleChangeListener() {
        @Override
        public void onScaleChange(int newScale, boolean isClockwise, int circles) {
            listenerTv.setText("当前刻度:" + newScale + ",圈数:" + circles + ",顺时针:" + isClockwise);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        weightDialView = findViewById(R.id.weightdialview);
        bgBtn = findViewById(R.id.background_btn);
        listenerBtn = findViewById(R.id.listener_btn);
        listenerTv = findViewById(R.id.listener_tv);
        statusTv = findViewById(R.id.status_tv);

        weightDialView.setCircle(0);
        weightDialView.setScale(0);
        weightDialView.setTextSize(16);

        notifyStatus();
    }

    private void notifyStatus() {
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        statusTv.setText(String.format("当前总刻度:%s,指针与圆心距离比:%s", weightDialView.getTotalScale(), decimalFormat.format(1 - weightDialView.getThumbDistance())));
    }

    public void addTotalScale(View view) {
        try {
            weightDialView.setTotalScale(weightDialView.getTotalScale() + 10);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        notifyStatus();
    }

    public void reduceTotalScale(View view) {
        try {
            weightDialView.setTotalScale(weightDialView.getTotalScale() - 10);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        notifyStatus();
    }

    public void showScaleLine(View view) {
        weightDialView.showScaleLine();
    }

    public void hideScaleLine(View view) {
        weightDialView.hideScaleLine();
    }

    private boolean flag = false;

    public void toggleBackground(View view) {
        if (flag) {
            weightDialView.setCircleBackground(null);
        } else {
            weightDialView.setCircleBackground(ImageUtil
                    .drawableToBitamp(getResources()
                            .getDrawable(R.mipmap.watch_dial)));
            weightDialView.setTotalScale(12);
        }
        bgBtn.setText(flag ? "显示背景图" : "隐藏背景图");
        notifyStatus();
        flag = !flag;
    }

    public void addThumbDistance(View view) {
        try {
            weightDialView.setThumbDistance(weightDialView.getThumbDistance() - 0.05f);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        notifyStatus();
    }

    public void reduceThumbDistance(View view) {
        try {
            weightDialView.setThumbDistance(weightDialView.getThumbDistance() + 0.05f);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        notifyStatus();
    }

    private boolean listenerFlag;

    public void toggleListener(View view) {
        weightDialView.setOnScaleChangeListener(listenerFlag ? null : mScaleChangeListener);
        listenerBtn.setText(listenerFlag ? "设置监听器" : "取消监听器");
        listenerTv.setText(listenerFlag ? "" : "当前刻度:" + weightDialView.getCurScale() + ",圈数:" + weightDialView.getCircle() + ",顺时针:" + weightDialView.isClockwise());
        listenerFlag = !listenerFlag;
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
