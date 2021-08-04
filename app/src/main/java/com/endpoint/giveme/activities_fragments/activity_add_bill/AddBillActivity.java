package com.endpoint.giveme.activities_fragments.activity_add_bill;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.endpoint.giveme.R;
import com.endpoint.giveme.databinding.ActivityAddBillBinding;
import com.endpoint.giveme.databinding.DialogSelectImage2Binding;
import com.endpoint.giveme.language.Language;
import com.endpoint.giveme.models.MessageDataModel;
import com.endpoint.giveme.models.MessageModel;
import com.endpoint.giveme.models.OrderModel;
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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.paperdb.Paper;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddBillActivity extends AppCompatActivity {
    private ActivityAddBillBinding binding;
    private String lang;
    private boolean canSelect = false;
    private OrderModel orderModel;
    private UserModel userModel;
    private Preferences preferences;
    private double delivery_cost = 0.0;
    private double product_cost = 0.0;
    private double total_cost = 0.0;
    private final String READ_PERM = Manifest.permission.READ_EXTERNAL_STORAGE;
    private final String write_permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private final String camera_permission = Manifest.permission.CAMERA;
    private final int READ_REQ = 1, CAMERA_REQ = 2;
    private Uri uri = null;
    private AlertDialog dialog;
    private List<MessageModel> messageModelList;

    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(Language.updateResources(newBase, Paper.book().read("lang", "ar")));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_bill);
        getDataFromIntent();
        initView();
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        orderModel = (OrderModel) intent.getSerializableExtra("data");
    }

    private void initView() {
        messageModelList = new ArrayList<>();
        preferences = Preferences.getInstance();
        userModel = preferences.getUserData(this);
        Paper.init(this);
        lang = Paper.book().read("lang","ar");
        binding.setLang(lang);
        binding.close.setOnClickListener(v -> finish());
        binding.edtOrderCost.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().trim().isEmpty()){
                    updateTotalCost(0);
                    product_cost = 0;
                    canSelect = false;
                    binding.btnSend.setBackgroundColor(ContextCompat.getColor(AddBillActivity.this,R.color.gray11));
                }else {
                    canSelect = true;
                    binding.btnSend.setBackgroundColor(ContextCompat.getColor(AddBillActivity.this,R.color.colorPrimary));
                    product_cost = Double.parseDouble(s.toString());
                    updateTotalCost(product_cost);
                }

            }
        });
        delivery_cost = Double.parseDouble(orderModel.getOrder_offer().getOffer_value())+Double.parseDouble(orderModel.getOrder_offer().getTax_value());
        binding.tvCost.setText(String.format(Locale.ENGLISH,"%s %s", delivery_cost,getString(R.string.sar)));
        binding.icon.setOnClickListener(v -> {
            uri = null;
            binding.image.setImageBitmap(null);
            binding.image.setVisibility(View.GONE);
            binding.icon.setVisibility(View.GONE);
            binding.llImage.setVisibility(View.VISIBLE);
        });
        binding.cardView.setOnClickListener(v -> createDialogAlert());
        updateTotalCost(0);

        binding.btnSend.setOnClickListener(v -> {
            if (canSelect){
                double discount = 0.0;
                double discountValue=0.0;
                if (orderModel.getCoupon()!=null){
                    if (orderModel.getCoupon().getCoupon_type().equals("per")){
                        discountValue = delivery_cost*(Double.parseDouble(orderModel.getCoupon().getCoupon_value())/100);
                    }else {
                        discountValue = Double.parseDouble(orderModel.getCoupon().getCoupon_value());

                    }
                    discount = discountValue;
                }

                double delivery_cost_after_discount = delivery_cost-discount;
                double total_cost2 = delivery_cost_after_discount+product_cost;
                String message= "قام المندوب " +orderModel.getDriver().getName()+" بإصدار فاتورة"+"\n"+"تكلفة المشتريات :"+product_cost+" "+getString(R.string.sar)+"\n"+"تكلفة التوصيل :"+delivery_cost+getString(R.string.sar)+"\n"+"قيمة الخصم :"+discountValue+getString(R.string.sar)+"\n"+"مجموع تكلفة التوصيل :"+delivery_cost_after_discount+getString(R.string.sar)+"\n"+"المجموع الكلي:"+total_cost2+getString(R.string.sar);

                if (uri==null){
                    Common.CreateDialogAlert(this,getString(R.string.ch_bill));
                    //AddBillWithoutImage(message,product_cost);

                }else {
                    AddBillWithImage(uri.toString(),message,product_cost);

                }
            }
        });


    }

    private void updateTotalCost(double cost) {
        total_cost = this.delivery_cost +cost;
        binding.tvTotalCost.setText(String.format(Locale.ENGLISH,"%s %s",total_cost,getString(R.string.sar)));
    }


    public void createDialogAlert()
    {
        dialog = new AlertDialog.Builder(this)
                .create();

        DialogSelectImage2Binding binding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.dialog_select_image2, null, false);
        binding.llCamera.setOnClickListener(v -> {
            dialog.dismiss();
            checkCameraPermission();
        });
        binding.llGallery.setOnClickListener(v ->{
            dialog.dismiss();
            checkReadPermission();
        } );

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
            intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == READ_REQ && resultCode == Activity.RESULT_OK && data != null) {
            dialog.dismiss();

            Uri uri = data.getData();
            if (uri!=null){
                this.uri = uri;
                binding.llImage.setVisibility(View.GONE);
                binding.icon.setVisibility(View.VISIBLE);
                binding.image.setVisibility(View.VISIBLE);
                Picasso.get().load(uri).into(binding.image);
            }
            //cropImage(uri);


        }
        else if (requestCode == CAMERA_REQ && resultCode == Activity.RESULT_OK && data != null) {
            dialog.dismiss();

            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            Uri uri = getUriFromBitmap(bitmap);
            if (uri != null) {
                this.uri = uri;
                binding.llImage.setVisibility(View.GONE);
                binding.icon.setVisibility(View.VISIBLE);
                binding.image.setVisibility(View.VISIBLE);
                Picasso.get().load(uri).into(binding.image);
                //cropImage(uri);

            }


        }
        else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                uri = result.getUri();
                binding.llImage.setVisibility(View.GONE);
                binding.icon.setVisibility(View.VISIBLE);
                binding.image.setVisibility(View.VISIBLE);
                Picasso.get().load(uri).into(binding.image);

                dialog.dismiss();



            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }




    }

    private void cropImage(Uri uri) {

        CropImage.activity(uri).setAspectRatio(1,1).setGuidelines(CropImageView.Guidelines.ON).start(this);

    }

    private Uri getUriFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        return Uri.parse(MediaStore.Images.Media.insertImage(this.getContentResolver(), bitmap, "", ""));
    }


    private void AddBillWithImage(String file_uri, String message, double product_cost) {

        ProgressDialog dialog = Common.createProgressDialog(this,getString(R.string.wait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        RequestBody client_id_part = Common.getRequestBodyText(String.valueOf(orderModel.getClient().getId()));
        RequestBody driver_id_part = Common.getRequestBodyText(String.valueOf(orderModel.getDriver().getId()));
        RequestBody order_id_part = Common.getRequestBodyText(String.valueOf(orderModel.getId()));
        RequestBody cost_part = Common.getRequestBodyText(String.valueOf(product_cost));
        RequestBody message_part = Common.getRequestBodyText(message);

        MultipartBody.Part file_part = Common.getMultiPartImage(this, Uri.parse(file_uri), "bill_image");
        Api.getService(Tags.base_url).addBillWithImage(userModel.getUser().getToken(), driver_id_part, client_id_part,order_id_part,cost_part,message_part,file_part)
                .enqueue(new Callback<MessageDataModel>() {
                    @Override
                    public void onResponse(Call<MessageDataModel> call, Response<MessageDataModel> response) {
                        dialog.dismiss();
                        if (response.isSuccessful() && response.body() != null) {
                            messageModelList.addAll(response.body().getData());
                            Intent intent = getIntent();
                            intent.putExtra("data", (Serializable) messageModelList);
                            setResult(RESULT_OK,intent);
                            finish();
                        } else {
                            dialog.dismiss();
                            try {
                                Log.e("error_bill",response.code()+"__"+response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (response.code() == 500) {

                                Toast.makeText(AddBillActivity.this, "Server Error", Toast.LENGTH_SHORT).show();

                            } else {
                                Toast.makeText(AddBillActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();


                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<MessageDataModel> call, Throwable t) {

                        try {
                            dialog.dismiss();


                            if (t.getMessage() != null) {
                                Log.e("msg_chat_error", t.getMessage() + "__");

                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {

                                    Toast.makeText(AddBillActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(AddBillActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();


                                }
                            }
                        } catch (Exception e) {

                        }
                    }
                });


    }

    private void AddBillWithoutImage(String message, double product_cost) {
        ProgressDialog dialog = Common.createProgressDialog(this,getString(R.string.wait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        RequestBody client_id_part = Common.getRequestBodyText(String.valueOf(orderModel.getClient().getId()));
        RequestBody driver_id_part = Common.getRequestBodyText(String.valueOf(orderModel.getDriver().getId()));
        RequestBody order_id_part = Common.getRequestBodyText(String.valueOf(orderModel.getId()));
        RequestBody cost_part = Common.getRequestBodyText(String.valueOf(product_cost));
        RequestBody message_part = Common.getRequestBodyText(message);
        Api.getService(Tags.base_url).addBillWithoutImage(userModel.getUser().getToken(), driver_id_part, client_id_part,order_id_part,cost_part,message_part)
                .enqueue(new Callback<MessageDataModel>() {
                    @Override
                    public void onResponse(Call<MessageDataModel> call, Response<MessageDataModel> response) {
                        dialog.dismiss();
                        if (response.isSuccessful() && response.body() != null) {
                            messageModelList.addAll(response.body().getData());
                            Intent intent = getIntent();
                            intent.putExtra("data", (Serializable) messageModelList);
                            setResult(RESULT_OK,intent);
                            finish();
                        } else {
                            dialog.dismiss();
                            try {
                                Log.e("error_bill",response.code()+"__"+response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (response.code() == 500) {

                                Toast.makeText(AddBillActivity.this, "Server Error", Toast.LENGTH_SHORT).show();

                            } else {
                                Toast.makeText(AddBillActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();


                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<MessageDataModel> call, Throwable t) {

                        try {
                            dialog.dismiss();


                            if (t.getMessage() != null) {
                                Log.e("msg_chat_error", t.getMessage() + "__");

                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {

                                    Toast.makeText(AddBillActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(AddBillActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();


                                }
                            }
                        } catch (Exception e) {

                        }
                    }
                });

    }

}