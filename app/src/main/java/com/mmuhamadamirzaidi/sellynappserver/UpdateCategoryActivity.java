package com.mmuhamadamirzaidi.sellynappserver;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

public class UpdateCategoryActivity extends AppCompatActivity {

    EditText add_category_name;
    ImageView save_category, add_image_category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_category);

        add_category_name = (EditText) findViewById(R.id.add_category_name);

        save_category = (ImageView) findViewById(R.id.save_category);
        add_image_category = (ImageView) findViewById(R.id.add_image_category);

        Intent intent = getIntent();
        String intentKey = intent.getStringExtra("key");
        String intentItem = intent.getStringExtra("item");

//        add_category_name.setText(itemethlm[\p[p]].getName());

        save_category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
