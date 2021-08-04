package com.endpoint.giveme.activities_fragments.activity_image;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.Transition;
import android.view.animation.LinearInterpolator;

import com.endpoint.giveme.R;
import com.endpoint.giveme.databinding.ActivityImageBinding;
import com.endpoint.giveme.language.Language;
import com.squareup.picasso.Picasso;

import io.paperdb.Paper;

public class ImageActivity extends AppCompatActivity {
    private ActivityImageBinding binding;
    private String lang;
    private String title;
    private String url="";

    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(Language.updateResources(newBase, Paper.book().read("lang", "ar")));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Transition transition = new Fade();
            transition.setInterpolator(new LinearInterpolator());
            transition.setDuration(200);
            getWindow().setEnterTransition(transition);
            getWindow().setExitTransition(transition);

        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_image);
        getDataFromIntent();
        initView();
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        url = intent.getStringExtra("url");

    }

    private void initView() {
        Paper.init(this);
        lang = Paper.book().read("lang", "ar");
        binding.setLang(lang);
        binding.llBack.setOnClickListener(v -> super.onBackPressed());
        binding.setName(title);
        Picasso.get().load(Uri.parse(url)).into(binding.photoView);



    }


}