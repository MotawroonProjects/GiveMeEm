package com.endpoint.giveme.activities_fragments.activity_add_order_products;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.endpoint.giveme.R;
import com.endpoint.giveme.activities_fragments.activity_add_coupon.AddCouponActivity;
import com.endpoint.giveme.activities_fragments.activity_chat.ChatActivity;
import com.endpoint.giveme.activities_fragments.activity_map_delivery_location.MapDeliveryLocationActivity;
import com.endpoint.giveme.adapters.AddOrderImagesAdapter;
import com.endpoint.giveme.adapters.AddOrderSelectedProductAdapter;
import com.endpoint.giveme.adapters.SelectedAdditionProductAdapter;
import com.endpoint.giveme.databinding.ActivityAddOrderProductsBinding;
import com.endpoint.giveme.databinding.DialogAlertOrderBinding;
import com.endpoint.giveme.databinding.DialogSelectImage2Binding;
import com.endpoint.giveme.language.Language;
import com.endpoint.giveme.models.AddOrderProductsModel;
import com.endpoint.giveme.models.AddOrderTextModel;
import com.endpoint.giveme.models.AdditionModel;
import com.endpoint.giveme.models.CouponModel;
import com.endpoint.giveme.models.FavoriteLocationModel;
import com.endpoint.giveme.models.OfferSettingModel;
import com.endpoint.giveme.models.ProductModel;
import com.endpoint.giveme.models.SingleOrderDataModel;
import com.endpoint.giveme.models.UserModel;
import com.endpoint.giveme.preferences.Preferences;
import com.endpoint.giveme.remote.Api;
import com.endpoint.giveme.share.Common;
import com.endpoint.giveme.tags.Tags;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.paperdb.Paper;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddOrderProductActivity extends AppCompatActivity {
    private ActivityAddOrderProductsBinding binding;
    private AddOrderProductsModel addOrderProductsModel;
    private String lang;
    private final String READ_PERM = Manifest.permission.READ_EXTERNAL_STORAGE;
    private final String write_permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private final String camera_permission = Manifest.permission.CAMERA;
    private final int READ_REQ = 1, CAMERA_REQ = 2;
    private List<Uri> imagesList;
    private  AlertDialog dialog;
    private AddOrderImagesAdapter addOrderImagesAdapter;
    private UserModel userModel;
    private Preferences preferences;
    private String vat="0";
    private AddOrderSelectedProductAdapter addOrderSelectedProductAdapter;
    private String currency;
    private AddOrderTextModel addOrderTextModel;

    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(Language.updateResources(newBase, Paper.book().read("lang", "ar")));
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_order_products);
        getDataFromIntent();
        initView();
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        addOrderProductsModel = (AddOrderProductsModel) intent.getSerializableExtra("data");

    }

    private void initView() {
        addOrderTextModel  = new AddOrderTextModel();
        preferences = Preferences.getInstance();
        userModel = preferences.getUserData(this);
        imagesList = new ArrayList<>();
        Paper.init(this);
        lang = Paper.book().read("lang","ar");
        binding.setLang(lang);
        binding.setModel(addOrderProductsModel);
        binding.setPriceBeforeVat("0");
        binding.setTotalCost(String.format(Locale.ENGLISH,"%.2f",addOrderProductsModel.getTotal_cost()));
        currency = getString(R.string.sar);
        /*if (userModel!=null){
            currency = userModel.getUser().getCountry().getWord().getCurrency();
        }*/
        binding.setCurrency(currency);
        binding.setVat(vat);

        binding.recViewImages.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        addOrderImagesAdapter = new AddOrderImagesAdapter(imagesList,this);
        binding.recViewImages.setAdapter(addOrderImagesAdapter);

        binding.imageCamera.setOnClickListener(v -> createDialogAlert());
        binding.tvAddCoupon.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddCouponActivity.class);
            startActivityForResult(intent,100);
        });
        binding.close.setOnClickListener(v -> {super.onBackPressed();});
        binding.tvAddComment.setOnClickListener(v -> {
            binding.tvAddComment.setVisibility(View.GONE);
            binding.llNotes.setVisibility(View.VISIBLE);
        });
        binding.btnNext.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapDeliveryLocationActivity.class);
            intent.putExtra("data",addOrderProductsModel);
            startActivityForResult(intent,200);
        });
        binding.imageHideSheet.setOnClickListener(v -> {
            closeSheet();
        });
        binding.recViewProducts.setLayoutManager(new LinearLayoutManager(this));
        addOrderSelectedProductAdapter  = new AddOrderSelectedProductAdapter(addOrderProductsModel.getProductModelList(),this,currency);
        binding.recViewProducts.setAdapter(addOrderSelectedProductAdapter);

        addOrderTextModel.setUser_id(userModel.getUser().getId());
        addOrderTextModel.setCoupon_id("0");
        addOrderTextModel.setOrder_type("emdad_market");
        addOrderTextModel.setPayment("cash");
        addOrderTextModel.setPlace_id(addOrderProductsModel.getShop_id());
        addOrderTextModel.setMarket_id(addOrderProductsModel.getMarket_id());
        addOrderTextModel.setPlace_name(addOrderProductsModel.getShop_name());
        addOrderTextModel.setPlace_lat(addOrderProductsModel.getShop_lat());
        addOrderTextModel.setPlace_lng(addOrderProductsModel.getShop_lng());
        addOrderTextModel.setPlace_address(addOrderProductsModel.getShop_address());
        getVAT();
    }

    private void getVAT()
    {
        ProgressDialog dialog = Common.createProgressDialog(this,getString(R.string.wait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        Api.getService(Tags.base_url).getOfferSetting().enqueue(new Callback<OfferSettingModel>() {
            @Override
            public void onResponse(Call<OfferSettingModel> call, Response<OfferSettingModel> response) {
                dialog.dismiss();

                if (response.isSuccessful()) {
                    vat = response.body().getTax();
                    calculateTotalPrice();
                } else {
                    dialog.dismiss();
                    try {
                        Log.e("error_code vat",response.code()+ response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


            }

            @Override
            public void onFailure(Call<OfferSettingModel> call, Throwable t) {



                try {
                    dialog.dismiss();
                    if (t.getMessage() != null) {
                        Log.e("error", t.getMessage() + "__");

                        if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                            Toast.makeText(AddOrderProductActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                        }else if (t.getMessage().toLowerCase().contains("socket")||t.getMessage().toLowerCase().contains("canceled")){}
                        else {
                            Toast.makeText(AddOrderProductActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                        }
                    }


                } catch (Exception e) {

                }
            }
        });

    }
    private void sendOrderTextWithoutImage()
    {

        ProgressDialog dialog = Common.createProgressDialog(this,getString(R.string.wait));
        dialog.setCancelable(false);
        dialog.show();

        String notes = binding.edtComment.getText().toString();
        String order_text =convertOrderToText();
        if (!notes.isEmpty()){
         order_text = order_text+"\n"+"ملاحظات:-"+"\n"+notes;
        }
        addOrderTextModel.setOrder_text(order_text);
        addOrderTextModel.setComments(notes);
        Api.getService(Tags.base_url)
                .sendTextOrder(userModel.getUser().getToken(),userModel.getUser().getId(),addOrderTextModel.getOrder_type(),addOrderTextModel.getMarket_id(),addOrderTextModel.getPlace_id(),String.valueOf(addOrderProductsModel.getTotal_cost()),addOrderTextModel.getTo_address(),addOrderTextModel.getTo_lat(),addOrderTextModel.getTo_lng(),addOrderTextModel.getPlace_name(),addOrderTextModel.getPlace_address(),addOrderTextModel.getPlace_lat(),addOrderTextModel.getPlace_lng(),String.valueOf(addOrderTextModel.getTime()),addOrderTextModel.getCoupon_id(),addOrderTextModel.getOrder_text(),addOrderTextModel.getComments())
                .enqueue(new Callback<SingleOrderDataModel>() {
                    @Override
                    public void onResponse(Call<SingleOrderDataModel> call, Response<SingleOrderDataModel> response) {
                        dialog.dismiss();
                        if (response.isSuccessful()&&response.body()!=null)
                        {
                            Intent intent = getIntent();
                            intent.putExtra("order_id",response.body().getOrder().getId());
                            setResult(RESULT_OK,intent);
                            finish();
                        }else
                        {
                            if (response.code()==500)
                            {
                                Toast.makeText(AddOrderProductActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                            }else if(response.code()==406){
                               CreateDialogAlertOrder(AddOrderProductActivity.this,getString(R.string.no_courier));

                                // Toast.makeText(AddOrderProductActivity.this, R.string.no_courier, Toast.LENGTH_SHORT).show();
                            } else
                            {
                                Toast.makeText(AddOrderProductActivity.this,getString(R.string.failed), Toast.LENGTH_SHORT).show();
                            }

                            try {
                                Log.e("error",response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<SingleOrderDataModel> call, Throwable t) {
                        try {
                            dialog.dismiss();
                            if (t.getMessage() != null) {
                                Log.e("msg_category_error", t.getMessage() + "__");

                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(AddOrderProductActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(AddOrderProductActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }catch (Exception e)
                        {
                            Log.e("Error",e.getMessage()+"__");
                        }
                    }
                });
    }
    private void sendOrderTextWithImage()
    {
        ProgressDialog dialog = Common.createProgressDialog(this,getString(R.string.wait));
        dialog.setCancelable(false);
        dialog.show();
        String notes = binding.edtComment.getText().toString();
        String order_text =convertOrderToText();
        if (!notes.isEmpty()){
            order_text = order_text+"\n"+"ملاحظات:-"+"\n"+notes;
        }
        addOrderTextModel.setOrder_text(order_text);
        addOrderTextModel.setComments(notes);


        RequestBody user_id_part = Common.getRequestBodyText(String.valueOf(userModel.getUser().getId()));
        RequestBody order_type_part = Common.getRequestBodyText(addOrderTextModel.getOrder_type());

        RequestBody market_id_part = Common.getRequestBodyText(String.valueOf(addOrderTextModel.getMarket_id()));
        RequestBody google_place_id_part = Common.getRequestBodyText(addOrderTextModel.getPlace_id());
        RequestBody bill_cost_part = Common.getRequestBodyText(String.valueOf(addOrderProductsModel.getTotal_cost()));
        RequestBody client_address_part = Common.getRequestBodyText(addOrderTextModel.getTo_address());
        RequestBody client_lat_part = Common.getRequestBodyText(String.valueOf(addOrderTextModel.getTo_lat()));
        RequestBody client_lng_part = Common.getRequestBodyText(String.valueOf(addOrderTextModel.getTo_lng()));
        RequestBody market_name_part = Common.getRequestBodyText(addOrderTextModel.getPlace_name());
        RequestBody market_address_part = Common.getRequestBodyText(addOrderTextModel.getPlace_address());
        RequestBody market_lat_part = Common.getRequestBodyText(String.valueOf(addOrderTextModel.getPlace_lat()));
        RequestBody market_lng_part = Common.getRequestBodyText(String.valueOf(addOrderTextModel.getPlace_lng()));
        RequestBody arrival_time_part = Common.getRequestBodyText(String.valueOf(addOrderTextModel.getTime()));
        RequestBody coupon_id_part = Common.getRequestBodyText(addOrderTextModel.getCoupon_id());
        RequestBody details_part = Common.getRequestBodyText(addOrderTextModel.getOrder_text());
        RequestBody notes_part = Common.getRequestBodyText(addOrderTextModel.getComments());


        Api.getService(Tags.base_url)
                .sendTextOrderWithImage(userModel.getUser().getToken(),user_id_part,order_type_part,market_id_part,google_place_id_part,bill_cost_part,client_address_part,client_lat_part,client_lng_part,market_name_part,market_address_part,market_lat_part,market_lng_part,arrival_time_part,coupon_id_part,details_part,notes_part,getMultiPartImages())
                .enqueue(new Callback<SingleOrderDataModel>() {
                    @Override
                    public void onResponse(Call<SingleOrderDataModel> call, Response<SingleOrderDataModel> response) {
                        dialog.dismiss();
                        if (response.isSuccessful()&&response.body()!=null)
                        {
                            Intent intent =new Intent(AddOrderProductActivity.this, ChatActivity.class);
                            intent.putExtra("order_id",response.body().getOrder().getId());
                            startActivity(intent);
                            finish();
                        }else
                        {
                            if (response.code()==500)
                            {
                                Toast.makeText(AddOrderProductActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                            }else if(response.code()==406){
                              CreateDialogAlertOrder(AddOrderProductActivity.this,getString(R.string.no_courier));

                                // Toast.makeText(AddOrderProductActivity.this, R.string.no_courier, Toast.LENGTH_SHORT).show();
                            } else
                            {
                                Toast.makeText(AddOrderProductActivity.this,getString(R.string.failed), Toast.LENGTH_SHORT).show();
                            }

                            try {
                                Log.e("error",response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<SingleOrderDataModel> call, Throwable t) {
                        try {
                            dialog.dismiss();
                            if (t.getMessage() != null) {
                                Log.e("msg_category_error", t.getMessage() + "__");

                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(AddOrderProductActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(AddOrderProductActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }catch (Exception e)
                        {
                            Log.e("Error",e.getMessage()+"__");
                        }
                    }
                });

    }

    private void CreateDialogAlertOrder(Context context, String msg) {
        final AlertDialog dialog = new AlertDialog.Builder(context)
                .create();

        DialogAlertOrderBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_alert_order, null, false);

        binding.tvMsg.setText(msg);
        binding.btnCancel.setOnClickListener(v -> {
                    dialog.dismiss();
                    finish();
                }
        );

        dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_congratulation_animation;
        dialog.setCanceledOnTouchOutside(false);
        dialog.setView(binding.getRoot());
        dialog.show();
    }

    private List<MultipartBody.Part> getMultiPartImages()
    {
        List<MultipartBody.Part> parts = new ArrayList<>();
        for (Uri uri :imagesList){
            if (uri!=null){
                MultipartBody.Part part = Common.getMultiPartImage(this,uri,"images[]");
                parts.add(part);
            }

        }
        return parts;
    }
    private String convertOrderToText()
    {
        String order = "";
        for (int index = 0;index<addOrderProductsModel.getProductModelList().size();index++){
            ProductModel model = addOrderProductsModel.getProductModelList().get(index);

            if (model.getSelectedAdditions().size()>0){
                order = order+model.getTitle()+"("+model.getCount()+")"+"\n"+getAdditions(model);
            }else {
                order = order+model.getTitle()+"("+model.getCount()+")"+"\n";
            }
        }
        return order;
    }
    private String getAdditions(ProductModel productModel)
    {
        String additions="";
        for (int index=0;index<productModel.getSelectedAdditions().size();index++){
            AdditionModel additionModel = productModel.getSelectedAdditions().get(index);
            additions =additions+(index+1)+"-"+additionModel.getTitle()+"("+productModel.getCount()+")"+"\n";
        }

        if (additions.isEmpty()){
            return additions;
        }else {
            return getString(R.string.additions)+":-"+"\n"+additions;
        }
    }
    private void calculateTotalPrice()
    {
        double tax = addOrderProductsModel.getTotal_cost()*(Double.parseDouble(vat)/100);
        binding.setVat(String.format(Locale.ENGLISH,"%.2f",tax));
        double priceBeforeVat = addOrderProductsModel.getTotal_cost()-tax;
        binding.setPriceBeforeVat(String.format(Locale.ENGLISH,"%.2f",priceBeforeVat));

    }
    public void createDialogAlert()
    {
        dialog = new AlertDialog.Builder(this)
                .create();

        DialogSelectImage2Binding binding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.dialog_select_image2, null, false);
        binding.llCamera.setOnClickListener(v -> checkCameraPermission());
        binding.llGallery.setOnClickListener(v -> checkReadPermission());

        dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_congratulation_animation;
        dialog.setCanceledOnTouchOutside(false);
        dialog.setView(binding.getRoot());
        dialog.show();
    }
    public void checkReadPermission()
    {
        if (ActivityCompat.checkSelfPermission(this, READ_PERM) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{READ_PERM}, READ_REQ);
        } else {
            SelectImage(READ_REQ);
        }
    }
    public void checkCameraPermission()
    {


        if (ContextCompat.checkSelfPermission(this, write_permission) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, camera_permission) == PackageManager.PERMISSION_GRANTED
        ) {
            SelectImage(CAMERA_REQ);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{camera_permission, write_permission}, CAMERA_REQ);
        }
    }
    private void SelectImage(int req)
    {

        Intent intent = new Intent();

        if (req == READ_REQ) {
            intent.setAction(Intent.ACTION_PICK);
            intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setType("image/*");
            startActivityForResult(intent, req);

        } else if (req == CAMERA_REQ) {
            try {
                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, req);
            } catch (SecurityException e) {
                Toast.makeText(this, R.string.perm_image_denied, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, R.string.perm_image_denied, Toast.LENGTH_SHORT).show();

            }


        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == READ_REQ) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                SelectImage(requestCode);
            } else {
                Toast.makeText(this, getString(R.string.perm_image_denied), Toast.LENGTH_SHORT).show();
            }

        } else if (requestCode == CAMERA_REQ) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                SelectImage(requestCode);
            } else {
                Toast.makeText(this, getString(R.string.perm_image_denied), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == READ_REQ && resultCode == Activity.RESULT_OK && data != null) {

            Uri uri = data.getData();
            cropImage(uri);


        }
        else if (requestCode == CAMERA_REQ && resultCode == Activity.RESULT_OK && data != null) {

            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            Uri uri = getUriFromBitmap(bitmap);
            if (uri != null) {
                cropImage(uri);

            }


        }
        else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri uri = result.getUri();

                if (imagesList.size()>0){
                    imagesList.add(imagesList.size()-1,uri);
                    addOrderImagesAdapter.notifyItemInserted(imagesList.size()-1);

                }else {
                    imagesList.add(uri);
                    imagesList.add(null);
                    addOrderImagesAdapter.notifyItemRangeInserted(0,imagesList.size());
                }


                dialog.dismiss();

                binding.recViewImages.postDelayed(()->{
                    binding.recViewImages.smoothScrollToPosition(imagesList.size()-1);
                },100);


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
        else if (requestCode == 100 && resultCode == Activity.RESULT_OK && data != null){
            CouponModel couponModel = (CouponModel) data.getSerializableExtra("data");

            addOrderTextModel.setCoupon_id(String.valueOf(couponModel.getId()));
            if (couponModel.getCoupon_type().equals("per")){
                String discount = getString(R.string.you_got)+" "+couponModel.getCoupon_value()+"% "+getString(R.string.discount)+" "+getString(R.string.on_delivery);
                binding.tvCoupon.setText(discount);
            }else {
                String discount = getString(R.string.you_got)+" "+couponModel.getCoupon_value()+" "+userModel.getUser().getCountry().getWord().getCurrency()+" "+getString(R.string.discount)+" "+getString(R.string.on_delivery);;
                binding.tvCoupon.setText(discount);
            }
        }
        else if (requestCode == 200 && resultCode == Activity.RESULT_OK && data != null){
            FavoriteLocationModel favoriteLocationModel = (FavoriteLocationModel) data.getSerializableExtra("data");
            addOrderTextModel.setTo_address(favoriteLocationModel.getAddress());
            addOrderTextModel.setTo_lat(favoriteLocationModel.getLat());
            addOrderTextModel.setTo_lng(favoriteLocationModel.getLng());
            int time = data.getIntExtra("time",1);
            addOrderTextModel.setTime(time);
            if (imagesList.size()>0){
                sendOrderTextWithImage();
            }else {
               sendOrderTextWithoutImage();
            }
        }

    }
    private void cropImage(Uri uri)
    {

        CropImage.activity(uri).setAspectRatio(1,1).setGuidelines(CropImageView.Guidelines.ON).start(this);

    }
    private Uri getUriFromBitmap(Bitmap bitmap)
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        return Uri.parse(MediaStore.Images.Media.insertImage(this.getContentResolver(), bitmap, "", ""));
    }
    public void delete(int adapterPosition)
    {
        imagesList.remove(adapterPosition);
        if (imagesList.size()==1){
            imagesList.clear();
            addOrderImagesAdapter.notifyDataSetChanged();
        }else {
            addOrderImagesAdapter.notifyItemRemoved(adapterPosition);
        }
    }
    public void updateItemCount(ProductModel productModel, int pos)
    {
        List<ProductModel> productModelList = addOrderProductsModel.getProductModelList();
        productModelList.set(pos,productModel);
        addOrderProductsModel.setProductModelList(productModelList);
        addOrderProductsModel.setTotal_cost(getTotalOrderCost(productModelList));
        binding.setTotalCost(String.format(Locale.ENGLISH,"%.2f",addOrderProductsModel.getTotal_cost()));

        calculateTotalPrice();
    }
    private double getTotalOrderCost(List<ProductModel> productModelList)
    {
        double total=0.0;
        for (ProductModel model:productModelList){
            total +=(Double.parseDouble(model.getPrice())+getTotalCostAdditions(model.getSelectedAdditions()))*model.getCount();
        }

        return total;
    }
    private double getTotalCostAdditions(List<AdditionModel> selectedAdditionList)
    {
        double cost = 0.0;
        for (AdditionModel additionModel:selectedAdditionList){

            cost += Double.parseDouble(additionModel.getPrice());

        }
        return cost;
    }
    public void setItemProduct(ProductModel productModel)
    {
        Picasso.get().load(Uri.parse(Tags.IMAGE_URL+productModel.getImage())).into(binding.image, new com.squareup.picasso.Callback() {
            @Override
            public void onSuccess() {
                binding.flNoImage.setVisibility(View.GONE);
            }

            @Override
            public void onError(Exception e) {
                binding.image.setVisibility(View.GONE);
                binding.flNoImage.setVisibility(View.VISIBLE);
            }
        });
        binding.setProductModel(productModel);
        SelectedAdditionProductAdapter selectedAdditionProductAdapter = new SelectedAdditionProductAdapter(this,productModel.getSelectedAdditions(),currency);
        binding.recViewAddition.setLayoutManager(new LinearLayoutManager(this));
        binding.recViewAddition.setAdapter(selectedAdditionProductAdapter);
        openSheet();
    }
    private void openSheet()
    {
        binding.flSheet.clearAnimation();
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        binding.flSheet.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                binding.flSheet.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }
    private void closeSheet()
    {
        binding.flSheet.clearAnimation();
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        binding.flSheet.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                binding.flSheet.setVisibility(View.GONE);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }
    @Override
    public void onBackPressed()
    {
        if (binding.flSheet.getVisibility()==View.VISIBLE){
            closeSheet();
        }else {
            super.onBackPressed();

        }
    }
}