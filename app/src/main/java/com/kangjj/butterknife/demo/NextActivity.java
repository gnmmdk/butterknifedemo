package com.kangjj.butterknife.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.kangjj.annotation.OnClick;
import com.kangjj.butterknife.library.ButterKnife;

public class NextActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn)
    public void click(View view){
        Toast.makeText(this,"test click",Toast.LENGTH_SHORT).show();
    }
}
