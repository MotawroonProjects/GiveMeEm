package com.endpoint.giveme.activities_fragments.activity_intro_slider;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.transition.Fade;
import android.transition.Transition;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.LinearInterpolator;

import com.endpoint.giveme.R;
import com.endpoint.giveme.activities_fragments.activity_login.LoginActivity;
import com.endpoint.giveme.adapters.IntroAdapter;
import com.endpoint.giveme.databinding.ActivityIntroSliderBinding;
import com.endpoint.giveme.language.Language;
import com.endpoint.giveme.models.DefaultSettings;
import com.endpoint.giveme.preferences.Preferences;

import io.paperdb.Paper;

public class IntroSliderActivity extends AppCompatActivity {
    private ActivityIntroSliderBinding binding;
    private IntroAdapter adapter;
    private Preferences preferences;
    private int type = 0;


    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(Language.updateResources(newBase,Paper.book().read("lang","ar")));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Transition transition = new Fade();
            transition.setInterpolator(new LinearInterpolator());
            transition.setDuration(1000);
            getWindow().setEnterTransition(transition);
            getWindow().setExitTransition(transition);

        }

        binding = DataBindingUtil.setContentView(this,R.layout.activity_intro_slider);
        getDataFromIntent();
        initView();
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();;
        type = intent.getIntExtra("type",0);
    }

    private void initView() {

        preferences = Preferences.getInstance();
        binding.tab.setupWithViewPager(binding.pager);
        adapter = new IntroAdapter(this);
        binding.pager.setAdapter(adapter);
        binding.pager.setOffscreenPageLimit(3);


        if (type==0){
            binding.tvTitle.setText(Html.fromHtml(getString(R.string.welcome_in_emdad)));
            binding.tvContent.setText(getString(R.string.we_deliver_order));

        }else {
            binding.tvTitle.setText("");
            binding.tvContent.setText(Html.fromHtml(getString(R.string.get_all_service)));
            binding.tvContent.setTextSize(TypedValue.COMPLEX_UNIT_DIP,18.0f);
            binding.tvContent.setTextColor(ContextCompat.getColor(this,R.color.gray11));
        }




        binding.pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (positionOffset!=0){
                    AlphaAnimation alphaAnimation = new AlphaAnimation(0,positionOffset);
                    binding.llContent.startAnimation(alphaAnimation);
                }
            }

            @Override
            public void onPageSelected(int position) {


                switch (position){
                    case 0:
                        binding.btnStart.setVisibility(View.GONE);
                        binding.cons.setVisibility(View.VISIBLE);

                        if (type==0){
                            binding.tvTitle.setText(Html.fromHtml(getString(R.string.welcome_in_emdad)));
                            binding.tvContent.setText(getString(R.string.we_deliver_order));

                        }else {
                            binding.tvTitle.setText("");
                            binding.tvContent.setText(Html.fromHtml(getString(R.string.get_all_service)));

                        }


                        break;
                    case 1:
                        binding.btnStart.setVisibility(View.GONE);
                        binding.cons.setVisibility(View.VISIBLE);

                        if (type==0){
                            binding.tvTitle.setText(Html.fromHtml(getString(R.string.get_discounts)));
                            binding.tvContent.setText(getString(R.string.many_discount));

                        }else {
                            binding.tvTitle.setText("");
                            binding.tvContent.setText(Html.fromHtml(getString(R.string.when_it_comes)));

                        }

                        break;

                    case 2:
                        binding.btnStart.setVisibility(View.VISIBLE);
                        binding.cons.setVisibility(View.GONE);

                        if (type==0){
                            binding.tvTitle.setText(Html.fromHtml(getString(R.string.easy_comm)));
                            binding.tvContent.setText(getString(R.string.delegate_contact));

                        }else {
                            binding.tvTitle.setText("");
                            binding.tvContent.setText(Html.fromHtml(getString(R.string.free_order_discussion)));

                        }

                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        binding.btnNext.setOnClickListener(v -> {
            if (binding.pager.getCurrentItem()<adapter.getCount()){
                binding.pager.setCurrentItem(binding.pager.getCurrentItem()+1,true);
            }
        });

        binding.btnSkip.setOnClickListener(v -> {
            if (type==0){
                start();

            }else {
                finish();
            }

        });


        binding.btnStart.setOnClickListener(v -> {
            if (type==0){
                start();

            }else {
                finish();
            }
        });

    }

    private void start(){
        DefaultSettings defaultSettings = preferences.getAppSetting(this);
        if (defaultSettings!=null){
            defaultSettings.setShowIntroSlider(false);
            preferences.createUpdateAppSetting(this,defaultSettings);
        }


        Intent intent = new Intent(this, LoginActivity.class);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this,binding.imageCar,binding.imageCar.getTransitionName());
            startActivity(intent,options.toBundle());

        }else {
            startActivity(intent);

        }




    }
}