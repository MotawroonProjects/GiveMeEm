package com.endpoint.giveme.activities_fragments.activity_shop_details;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.endpoint.giveme.BuildConfig;
import com.endpoint.giveme.R;
import com.endpoint.giveme.activities_fragments.activity_add_order_text.AddOrderTextActivity;
import com.endpoint.giveme.activities_fragments.activity_image.ImageActivity;
import com.endpoint.giveme.activities_fragments.activity_login.LoginActivity;
import com.endpoint.giveme.adapters.HoursAdapter;
import com.endpoint.giveme.adapters.ImagePagerAdapter;
import com.endpoint.giveme.adapters.MenuImageAdapter;
import com.endpoint.giveme.databinding.ActivityShopDetailsBinding;
import com.endpoint.giveme.databinding.DialogHoursBinding;
import com.endpoint.giveme.language.Language;
import com.endpoint.giveme.models.CustomPlaceDataModel;
import com.endpoint.giveme.models.CustomPlaceModel;
import com.endpoint.giveme.models.HourModel;
import com.endpoint.giveme.models.NearbyModel;
import com.endpoint.giveme.models.PhotosModel;
import com.endpoint.giveme.models.PlaceDetailsModel;
import com.endpoint.giveme.models.UserModel;
import com.endpoint.giveme.preferences.Preferences;
import com.endpoint.giveme.remote.Api;
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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShopDetailsActivity extends AppCompatActivity {
    private ActivityShopDetailsBinding binding;
    private ImagePagerAdapter imagePagerAdapter;
    private List<PhotosModel> photosModelList;
    private NearbyModel.Result placeModel;
    private String lang;
    private List<HourModel> hourModelList;
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
        binding = DataBindingUtil.setContentView(this, R.layout.activity_shop_details);
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
        placeModel = (NearbyModel.Result) intent.getSerializableExtra("data");

    }
    private void initView()
    {

        menuImageList = new ArrayList<>();
        preferences = Preferences.getInstance();
        userModel = preferences.getUserData(this);
        hourModelList = new ArrayList<>();
        photosModelList = new ArrayList<>();
        String currency = getString(R.string.sar);
      /*  if (userModel != null) {
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
            String url = getString(R.string.can_order)+"\n"+Tags.base_url+"place/details/"+placeModel.getPlace_id();
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
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.putExtra("from", false);
                    startActivity(intent);
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

        getPlaceDetails();
    }
    private void getPlaceDetails()
    {

        String fields = "opening_hours,photos,reviews";

        Api.getService("https://maps.googleapis.com/maps/api/")
                .getPlaceDetails(placeModel.getPlace_id(), fields, lang, getString(R.string.map_api_key))
                .enqueue(new Callback<PlaceDetailsModel>() {
                    @Override
                    public void onResponse(Call<PlaceDetailsModel> call, Response<PlaceDetailsModel> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            getPlaceDataByGooglePlaceId(response.body(),placeModel.getPlace_id());
                        } else {

                            try {
                                Log.e("error_code", response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<PlaceDetailsModel> call, Throwable t) {
                        try {

                            Log.e("Error", t.getMessage());
                            Toast.makeText(ShopDetailsActivity.this, getString(R.string.something), Toast.LENGTH_LONG).show();
                        } catch (Exception e) {

                        }
                    }
                });
    }
    private void updateHoursUI(PlaceDetailsModel body)
    {

        if (body.getResult().getReviews()!=null&&body.getResult().getReviews().size()>0){
            placeModel.setReviews(body.getResult().getReviews());

        }else {
            placeModel.setReviews(new ArrayList<>());
        }
        if (body.getResult().getPhotos()!=null&&body.getResult().getPhotos().size()>0){
            placeModel.setPhotosModels(body.getResult().getPhotos());
            photosModelList.clear();
            photosModelList.addAll(placeModel.getPhotosModels());
            imagePagerAdapter = new ImagePagerAdapter(photosModelList,this);
            binding.pager.setAdapter(imagePagerAdapter);
            binding.tab.setupWithViewPager(binding.pager);

            for(int i=0; i < binding.tab.getTabCount(); i++) {
                View tab = ((ViewGroup) binding.tab.getChildAt(0)).getChildAt(i);
                ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) tab.getLayoutParams();
                p.setMargins(2, 0, 2, 0);
                tab.requestLayout();
            }

        }else {
            placeModel.setPhotosModels(new ArrayList<>());
        }

        if (body.getResult().getOpening_hours() != null) {
            placeModel.setOpen(body.getResult().getOpening_hours().isOpen_now());
            if (body.getResult().getOpening_hours().getWeekday_text()!=null&&body.getResult().getOpening_hours().getWeekday_text().size()>0){
                placeModel.setWork_hours(body.getResult().getOpening_hours());
                binding.tvStatus.setTextColor(ContextCompat.getColor(this,R.color.gray11));
                binding.icon.setColorFilter(ContextCompat.getColor(this,R.color.gray11));
                placeModel.setOpen(true);
                hourModelList.clear();
                hourModelList.addAll(getHours());
                if (hourModelList.size()>0){
                    binding.tvHours.setText(hourModelList.get(0).getTime());

                }

            }else {
                binding.tvStatus.setTextColor(ContextCompat.getColor(this,R.color.color_rose));
                binding.icon.setColorFilter(ContextCompat.getColor(this,R.color.color_rose));

                placeModel.setOpen(false);

            }


        } else {
            binding.tvStatus.setTextColor(ContextCompat.getColor(this,R.color.color_rose));
            binding.icon.setColorFilter(ContextCompat.getColor(this,R.color.color_rose));


            placeModel.setOpen(false);

        }

        if (placeModel.getCustomPlaceModel()!=null&&placeModel.getCustomPlaceModel().getLogo()!=null&&!placeModel.getCustomPlaceModel().getLogo().isEmpty()&&!placeModel.getCustomPlaceModel().getLogo().equals("0")){
            Picasso.get().load(Uri.parse(Tags.IMAGE_URL+placeModel.getCustomPlaceModel().getLogo())).fit().into(binding.image);

        }else {
            if (placeModel.getPhotos()!=null){
                if (placeModel.getPhotos().size()>0)
                {
                    String url = Tags.IMAGE_Places_URL+placeModel.getPhotos().get(0).getPhoto_reference()+"&key="+getString(R.string.map_api_key);
                    Picasso.get().load(Uri.parse(url)).fit().into(binding.image);

                }else
                {
                    Picasso.get().load(Uri.parse(placeModel.getIcon())).fit().into(binding.image);

                }
            }
            else {
                Picasso.get().load(Uri.parse(placeModel.getIcon())).fit().into(binding.image);

            }
        }




        binding.setDistance(String.format(Locale.ENGLISH,"%.2f",placeModel.getDistance()));
        binding.setModel(placeModel);
        binding.ll.setVisibility(View.VISIBLE);
        binding.progBar.setVisibility(View.GONE);
        binding.ll.setVisibility(View.VISIBLE);
        binding.imageShare.setVisibility(View.VISIBLE);


        if (placeModel.getCustomPlaceModel()!=null&&placeModel.getCustomPlaceModel().getMenu()!=null&&placeModel.getCustomPlaceModel().getMenu().size()>0){
            menuImageList.clear();
            menuImageList.addAll(placeModel.getCustomPlaceModel().getMenu());
            binding.recView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
            menuImageAdapter = new MenuImageAdapter(menuImageList,this);
            binding.recView.setAdapter(menuImageAdapter);

        }

    }
    private List<HourModel> getHours()
    {
        List<HourModel> list = new ArrayList<>();

        for (String time: placeModel.getWork_hours().getWeekday_text()){

            String day = time.split(":", 2)[0].trim();
            String t = time.split(":",2)[1].trim();
            HourModel hourModel = new HourModel(day,t);
            list.add(hourModel);




        }

        return list;
    }

    private File createFile(){
        File file = null;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.logo_white_text);
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

    private void getPlaceDataByGooglePlaceId(PlaceDetailsModel body, String place_id) {
        Api.getService(Tags.base_url).getCustomPlaceByGooglePlaceId(place_id).enqueue(new Callback<CustomPlaceDataModel>() {
            @Override
            public void onResponse(Call<CustomPlaceDataModel> call, Response<CustomPlaceDataModel> response) {
                if (response.isSuccessful()) {

                    try {
                        placeModel.setCustomPlaceModel(response.body().getData());
                        updateHoursUI(body);


                    }catch (Exception e){}



                } else {
                    updateHoursUI(body);

                    try {
                        Log.e("error_code", response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


            }

            @Override
            public void onFailure(Call<CustomPlaceDataModel> call, Throwable t) {



                try {


                    updateHoursUI(body);


                    if (t.getMessage() != null) {
                        Log.e("error", t.getMessage() + "__");

                        if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                            Toast.makeText(ShopDetailsActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                        }else if (t.getMessage().toLowerCase().contains("socket")||t.getMessage().toLowerCase().contains("canceled")){}
                        else {
                            Toast.makeText(ShopDetailsActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                        }
                    }


                } catch (Exception e) {

                }
            }
        });

    }

    private void createDialogAlert()
    {
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .create();

        DialogHoursBinding binding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.dialog_hours, null, false);
        binding.recVeiw.setLayoutManager(new LinearLayoutManager(this));
        HoursAdapter adapter = new HoursAdapter(hourModelList,this);
        binding.recVeiw.setAdapter(adapter);

        binding.btnCancel.setOnClickListener(v -> dialog.dismiss()

        );
        dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_congratulation_animation;
        dialog.setCanceledOnTouchOutside(false);
        dialog.setView(binding.getRoot());
        dialog.show();
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

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(this,image,image.getTransitionName());
            startActivity(intent,optionsCompat.toBundle());

        }else {
            startActivity(intent);

        }



    }
}