package com.endpoint.giveme.activities_fragments.activity_custom_shop_details;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.transition.Fade;
import android.transition.Transition;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.endpoint.giveme.BuildConfig;
import com.endpoint.giveme.R;
import com.endpoint.giveme.activities_fragments.activity_add_order_text.AddOrderTextActivity;
import com.endpoint.giveme.activities_fragments.activity_image.ImageActivity;
import com.endpoint.giveme.activities_fragments.activity_login.LoginActivity;
import com.endpoint.giveme.adapters.CustomHoursAdapter;
import com.endpoint.giveme.adapters.CustomImagePagerAdapter;
import com.endpoint.giveme.adapters.MenuImageAdapter;
import com.endpoint.giveme.databinding.ActivityCustomShopDetailsBinding;
import com.endpoint.giveme.databinding.DialogHoursBinding;
import com.endpoint.giveme.language.Language;
import com.endpoint.giveme.models.CustomPlaceModel;
import com.endpoint.giveme.models.UserModel;
import com.endpoint.giveme.preferences.Preferences;
import com.endpoint.giveme.tags.Tags;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.paperdb.Paper;

public class CustomShopDetailsActivity extends AppCompatActivity {
    private ActivityCustomShopDetailsBinding binding;
    private CustomImagePagerAdapter imagePagerAdapter;
    private List<CustomPlaceModel.Gallery> photosModelList;
    private CustomPlaceModel placeModel;
    private String lang;
    private List<CustomPlaceModel.Days> hourModelList;
    private boolean canSend = false;
    private UserModel userModel;
    private Preferences preferences;
    private List<CustomPlaceModel.MenuImage> menuImageList;
    private MenuImageAdapter menuImageAdapter;
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
        binding = DataBindingUtil.setContentView(this, R.layout.activity_custom_shop_details);
        getDataFromIntent();
        initView();
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
        userModel = preferences.getUserData(this);

    }

    private void getDataFromIntent()
    {

        Intent intent = getIntent();
        placeModel = (CustomPlaceModel) intent.getSerializableExtra("data");

    }
    private void initView()
    {
        menuImageList = new ArrayList<>();
        preferences = Preferences.getInstance();
        userModel = preferences.getUserData(this);
        hourModelList = new ArrayList<>();
        photosModelList = new ArrayList<>();
        String currency = getString(R.string.sar);
       /* if (userModel != null) {
            currency = userModel.getUser().getCountry().getWord().getCurrency();
        }*/
        binding.setCurrency(currency);

        Paper.init(this);
        lang = Paper.book().read("lang","ar");
        binding.setLang(lang);
        binding.setDistance("");
        binding.flBack.setOnClickListener(v -> finish());
        binding.progBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(this,R.color.colorPrimary), PorterDuff.Mode.SRC_IN);



        binding.tvShow.setOnClickListener(v -> {
            if (hourModelList.size()>0){
                createDialogAlert();
            }else {
                Toast.makeText(this, R.string.work_hour_not_aval, Toast.LENGTH_SHORT).show();
            }
        });

        binding.imageShare.setOnClickListener(v -> {
            Uri uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID+".provider",createFile());
            String url = getString(R.string.can_order)+"\n"+Tags.base_url+"place/details/"+placeModel.getGoogle_place_id();
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT,url);
            intent.putExtra(Intent.EXTRA_STREAM,uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent,"Share"));
        });

        binding.btnNext.setOnClickListener(v -> {
            canSend = true;
            if (canSend){

                if (userModel!=null){
                    Intent intent = new Intent(this, AddOrderTextActivity.class);
                    intent.putExtra("data",placeModel);
                    startActivityForResult(intent,100);
                }else {

                }


            }
        });

        binding.consReview.setOnClickListener(v -> {
            if (userModel==null){
                Intent intent = new Intent(this, LoginActivity.class);
                intent.putExtra("from",false);
                startActivity(intent);
            }else {

            }
        });

        binding.flBack.setOnClickListener(v -> {super.onBackPressed();});

        updateUI();
    }

    private void updateUI()
    {
        if (placeModel.getGallary()!=null&&placeModel.getGallary().size()>0){
            photosModelList.clear();
            photosModelList.addAll(placeModel.getGallary());
            imagePagerAdapter = new CustomImagePagerAdapter(photosModelList,this);
            binding.pager.setAdapter(imagePagerAdapter);
            binding.tab.setupWithViewPager(binding.pager);

            for(int i=0; i < binding.tab.getTabCount(); i++) {
                View tab = ((ViewGroup) binding.tab.getChildAt(0)).getChildAt(i);
                ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) tab.getLayoutParams();
                p.setMargins(2, 0, 2, 0);
                tab.requestLayout();
            }
        }

        if (placeModel.getDays() != null&&placeModel.getDays().size()>0) {
            placeModel.setOpen(isOpen());
            binding.tvStatus.setTextColor(ContextCompat.getColor(this,R.color.gray11));
            binding.icon.setColorFilter(ContextCompat.getColor(this,R.color.gray11));
            placeModel.setOpen(true);
            hourModelList.clear();
            hourModelList.addAll(placeModel.getDays());
            if (hourModelList.size()>0){
                binding.tvHours.setText(String.format("%s%s%s",hourModelList.get(0).getFrom_time(),"-",hourModelList.get(0).getTo_time()));

            }


        } else {
            binding.tvStatus.setTextColor(ContextCompat.getColor(this,R.color.color_rose));
            binding.icon.setColorFilter(ContextCompat.getColor(this,R.color.color_rose));


            placeModel.setOpen(false);

        }
        Picasso.get().load(Uri.parse(placeModel.getLogo())).fit().into(binding.image);





        binding.setDistance(String.format(Locale.ENGLISH,"%.2f",placeModel.getDistance()));
        binding.setModel(placeModel);
        binding.ll.setVisibility(View.VISIBLE);
        binding.progBar.setVisibility(View.GONE);
        binding.ll.setVisibility(View.VISIBLE);
        binding.imageShare.setVisibility(View.VISIBLE);


        if (placeModel.getMenu()!=null&&placeModel.getMenu()!=null&&placeModel.getMenu().size()>0){
            menuImageList.clear();
            menuImageList.addAll(placeModel.getMenu());
            binding.recView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
            menuImageAdapter = new MenuImageAdapter(menuImageList,this);
            binding.recView.setAdapter(menuImageAdapter);

        }

    }

    private void createDialogAlert()
    {
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .create();

        DialogHoursBinding binding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.dialog_hours, null, false);
        binding.recVeiw.setLayoutManager(new LinearLayoutManager(this));
        CustomHoursAdapter adapter = new CustomHoursAdapter(hourModelList,this);
        binding.recVeiw.setAdapter(adapter);

        binding.btnCancel.setOnClickListener(v -> dialog.dismiss()

        );
        dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_congratulation_animation;
        dialog.setCanceledOnTouchOutside(false);
        dialog.setView(binding.getRoot());
        dialog.show();
    }

    private boolean isOpen(){
        if (placeModel.getDays()!=null&&placeModel.getDays().size()>0&&placeModel.getDays().get(0).getStatus().equals("open")) {
            return true;
        }

        return false;
    }

    private File createFile(){
        File file = null;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.logo_text);
        file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),System.currentTimeMillis()+".png");
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG,90,outputStream);
            outputStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==100&&resultCode==RESULT_OK){

        }
    }

    public void setMenuItem(CustomPlaceModel.MenuImage menuImage, RoundedImageView image) {
        Intent intent = new Intent(this, ImageActivity.class);
        intent.putExtra("title",placeModel.getName());
        intent.putExtra("url",Tags.IMAGE_URL+menuImage.getImage());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(this,image,image.getTransitionName());
            startActivity(intent,optionsCompat.toBundle());

        }else {
            startActivity(intent);

        }



    }
}