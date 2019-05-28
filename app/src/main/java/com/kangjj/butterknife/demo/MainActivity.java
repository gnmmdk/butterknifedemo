package com.kangjj.butterknife.demo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.kangjj.annotation.BindView;
import com.kangjj.annotation.OnClick;
import com.kangjj.butterknife.library.ButterKnife;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.tv1)
    TextView tv1;
    @BindView(R.id.tv2)
    TextView tv2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
    }

    @OnClick(R.id.tv1)
    public void click(View view) {
        startActivity(new Intent(this,NextActivity.class));
    }

    @OnClick(R.id.tv2)
    public void click2() {
        Toast.makeText(this, "OnClick -> no parameter", Toast.LENGTH_SHORT).show();
    }
}
