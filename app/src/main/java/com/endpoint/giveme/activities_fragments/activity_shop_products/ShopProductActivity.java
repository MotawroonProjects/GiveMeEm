package com.endpoint.giveme.activities_fragments.activity_shop_products;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.endpoint.giveme.BuildConfig;
import com.endpoint.giveme.R;
import com.endpoint.giveme.activities_fragments.activity_add_order_products.AddOrderProductActivity;
import com.endpoint.giveme.activities_fragments.activity_chat.ChatActivity;
import com.endpoint.giveme.activities_fragments.activity_login.LoginActivity;
import com.endpoint.giveme.adapters.AdditionProductAdapter;
import com.endpoint.giveme.adapters.CustomHoursAdapter;
import com.endpoint.giveme.adapters.HoursAdapter;
import com.endpoint.giveme.adapters.shop_products_adapters.ProductCategoryAdapter;
import com.endpoint.giveme.adapters.shop_products_adapters.ProductSectionAdapter;
import com.endpoint.giveme.databinding.ActivityShopProductsBinding;
import com.endpoint.giveme.databinding.DialogHoursBinding;
import com.endpoint.giveme.language.Language;
import com.endpoint.giveme.models.AddOrderProductsModel;
import com.endpoint.giveme.models.AdditionModel;
import com.endpoint.giveme.models.CustomPlaceModel;
import com.endpoint.giveme.models.CustomShopDataModel;
import com.endpoint.giveme.models.HourModel;
import com.endpoint.giveme.models.PlaceDetailsModel;
import com.endpoint.giveme.models.ProductModel;
import com.endpoint.giveme.models.ShopDepartmentDataModel;
import com.endpoint.giveme.models.ShopDepartments;
import com.endpoint.giveme.models.UserModel;
import com.endpoint.giveme.preferences.Preferences;
import com.endpoint.giveme.remote.Api;
import com.endpoint.giveme.tags.Tags;
import com.google.android.material.appbar.AppBarLayout;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShopProductActivity extends AppCompatActivity {
    private ActivityShopProductsBinding binding;
    private CustomShopDataModel placeModel;
    private String lang;
    private List<CustomPlaceModel.Days> daysModelList;
    private List<HourModel> hourModelList;
    private ProductSectionAdapter productSectionAdapter;
    private boolean canSend = false;
    private UserModel userModel;
    private Preferences preferences;
    private String currency;
    private ProductCategoryAdapter categoryAdapter;
    private boolean clicked = false;
    private double totalOrderCost = 0;
    private ProductModel selectedProduct;
    private int childPos = -1;
    private int parentPos = -1;
    private int count = 1;
    private List<ShopDepartments> shopDepartmentsList;
    private AddOrderProductsModel addOrderProductsModel;
    private List<AdditionModel> selectedAdditionList;
    private SparseArray<AdditionModel> sparseArray;


    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(Language.updateResources(newBase, Paper.book().read("lang", "ar")));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_shop_products);
        getDataFromIntent();
        initView();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        userModel = preferences.getUserData(this);

    }

    private void getDataFromIntent() {

        Intent intent = getIntent();
        placeModel = (CustomShopDataModel) intent.getSerializableExtra("data");

    }

    private void initView() {
        sparseArray = new SparseArray<>();
        selectedAdditionList = new ArrayList<>();
        addOrderProductsModel = new AddOrderProductsModel();
        shopDepartmentsList = new ArrayList<>();
        preferences = Preferences.getInstance();
        userModel = preferences.getUserData(this);
        daysModelList = new ArrayList<>();
        hourModelList = new ArrayList<>();

        currency = getString(R.string.sar);
        /*if (userModel != null) {
            currency = userModel.getUser().getCountry().getWord().getCurrency();
        }*/
        binding.setCurrency(currency);

        Paper.init(this);
        lang = Paper.book().read("lang", "ar");
        binding.setLang(lang);

        binding.recView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager manager = (LinearLayoutManager) binding.recView.getLayoutManager();

                if (!clicked) {
                    int pos = manager.findFirstVisibleItemPosition();

                    if (categoryAdapter != null) {
                        categoryAdapter.setSelectedPos(pos);
                        binding.recViewDepartment.scrollToPosition(pos);
                    }
                }

                clicked = false;


            }
        });
        binding.appBar.addOnOffsetChangedListener((AppBarLayout.BaseOnOffsetChangedListener) (appBarLayout, verticalOffset) -> {
            int total = appBarLayout.getTotalScrollRange() + verticalOffset;
            if (total == 0) {
                if (placeModel.getShopDepartmentsList() != null && placeModel.getShopDepartmentsList().size() > 0) {
                    binding.recViewDepartment.setVisibility(View.VISIBLE);
                    binding.tvMenu.setVisibility(View.INVISIBLE);

                } else {
                    binding.recViewDepartment.setVisibility(View.INVISIBLE);
                    binding.tvMenu.setVisibility(View.VISIBLE);


                }
            } else {
                binding.tvMenu.setVisibility(View.VISIBLE);
                binding.recViewDepartment.setVisibility(View.INVISIBLE);


            }
        });

        binding.flBack.setOnClickListener(v -> finish());


        binding.tvShow.setOnClickListener(v -> {

            if (daysModelList.size() > 0) {
                createDialogAlertDays();
            } else {

                if (hourModelList.size() > 0) {
                    createDialogAlertHours();
                } else {
                    Toast.makeText(this, R.string.work_hour_not_aval, Toast.LENGTH_SHORT).show();
                }
            }


        });

        binding.imageShare.setOnClickListener(v -> {
            Uri uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID+".provider",createFile());
            String url = getString(R.string.can_order)+"\n"+Tags.base_url+"place/details/"+placeModel.getShop_id();
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT,url);
            intent.putExtra(Intent.EXTRA_STREAM,uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent,"Share"));
        });


        binding.consReview.setOnClickListener(v -> {
            if (userModel == null) {
                Intent intent = new Intent(this, LoginActivity.class);
                intent.putExtra("from", false);
                startActivity(intent);
            } else {

            }
        });

        binding.flBack.setOnClickListener(v -> {
            super.onBackPressed();
        });

        binding.imageHideSheet.setOnClickListener(v -> {
            closeSheet();
        });

        binding.tvIncrease.setOnClickListener(v -> {
            count++;
            this.selectedProduct.setCount(count);
            binding.tvCount.setText(String.valueOf(count));
            double total = getTotalItemCost(selectedProduct) * count;
            this.selectedProduct.setTotal_cost(total);
            binding.tvTotalCost.setText(String.format("%s %s", total, currency));

        });

        binding.tvDecrease.setOnClickListener(v -> {
            if (count > 1) {
                count--;
                this.selectedProduct.setCount(count);
                binding.tvCount.setText(String.valueOf(count));
                double total = getTotalItemCost(selectedProduct) * count;
                this.selectedProduct.setTotal_cost(total);

                binding.tvTotalCost.setText(String.format("%s %s", total, currency));
            }

        });

        binding.btnAddProduct.setOnClickListener(v -> {
            ShopDepartments departments = shopDepartmentsList.get(parentPos);
            departments.setCount(count);
            selectedProduct.setCount(count);
            departments.getProducts_list().set(childPos, selectedProduct);
            shopDepartmentsList.set(parentPos, departments);
            productSectionAdapter.notifyItemChanged(parentPos);
            List<ProductModel> productModelList = addOrderProductsModel.getProductModelList();

            if (productModelList.size() > 0) {

                int pos = isSelectedProductListHasItem(productModelList, selectedProduct);
                selectedProduct.setTotal_cost(getTotalItemCost(selectedProduct));
                if (pos != -1) {
                    productModelList.set(pos, selectedProduct);
                } else {
                    productModelList.add(selectedProduct);
                }

            } else {
                selectedProduct.setTotal_cost(getTotalItemCost(selectedProduct));
                productModelList.add(selectedProduct);

            }
            addOrderProductsModel.setProductModelList(productModelList);
            totalOrderCost = getTotalOrderCost(addOrderProductsModel.getProductModelList());

            updateTotalUi();
            closeSheet();
            count = 1;
            childPos = -1;
            parentPos = -1;
            selectedProduct = null;

        });
        binding.flChooseFromMenu.setOnClickListener(v -> {
            if (userModel != null) {
                if (canSend) {
                    addOrderProductsModel.setUser_id(userModel.getUser().getId());
                    addOrderProductsModel.setShop_id(placeModel.getShop_id());
                    addOrderProductsModel.setMarket_id(placeModel.getMarket_id());
                    addOrderProductsModel.setShop_name(placeModel.getShop_name());
                    addOrderProductsModel.setShop_address(placeModel.getShop_address());
                    addOrderProductsModel.setShop_lat(placeModel.getShop_lat());
                    addOrderProductsModel.setShop_lng(placeModel.getShop_lng());

                    Intent intent = new Intent(this, AddOrderProductActivity.class);
                    intent.putExtra("data", addOrderProductsModel);
                    startActivityForResult(intent, 100);
                }
            } else {
                Intent intent = new Intent(this, LoginActivity.class);
                intent.putExtra("from", false);
                startActivity(intent);
            }
        });

        if (placeModel.getPlace_type().equals("custom")) {
            updateUI();
        } else {
            getPlaceDetails();
        }

        getDepartments();
        updateTotalUi();
    }


    private void updateUI() {
        if (placeModel.getDays() != null && placeModel.getDays().size() > 0) {
            binding.tvStatus.setTextColor(ContextCompat.getColor(this, R.color.gray11));
            binding.icon.setColorFilter(ContextCompat.getColor(this, R.color.gray11));
            daysModelList.clear();
            daysModelList.addAll(placeModel.getDays());
            binding.tvHours.setText(String.format("%s%s%s", daysModelList.get(0).getFrom_time(), "-", daysModelList.get(0).getTo_time()));


        } else {
            if (placeModel.getHourModelList() != null && placeModel.getHourModelList().size() > 0) {
                binding.tvStatus.setTextColor(ContextCompat.getColor(this, R.color.gray11));
                binding.icon.setColorFilter(ContextCompat.getColor(this, R.color.gray11));
                hourModelList.clear();
                hourModelList.addAll(placeModel.getHourModelList());
                binding.tvHours.setText(hourModelList.get(0).getTime());

            } else {
                binding.tvStatus.setTextColor(ContextCompat.getColor(this, R.color.color_rose));
                binding.icon.setColorFilter(ContextCompat.getColor(this, R.color.color_rose));

            }


        }


        binding.setModel(placeModel);
        binding.imageShare.setVisibility(View.VISIBLE);
        binding.llContainer.setVisibility(View.VISIBLE);


    }

    private void updateTotalUi() {

        addOrderProductsModel.setTotal_cost(totalOrderCost);
        binding.tvPrice.setText(String.format("%s %s %s", getString(R.string.cost_with_tax), totalOrderCost, currency));

        if (addOrderProductsModel != null && addOrderProductsModel.getProductModelList() != null && addOrderProductsModel.getProductModelList().size() > 0) {
            canSend = true;
            binding.iconHand.setVisibility(View.GONE);
            binding.tvChooseFromMenu.setText(R.string.complete_order);
            binding.flChooseFromMenu.setBackgroundResource(R.color.colorPrimary);
            binding.tvPrice.setVisibility(View.VISIBLE);
        } else {
            canSend = false;

            binding.iconHand.setVisibility(View.VISIBLE);
            binding.tvChooseFromMenu.setText(R.string.choose_from_menu_first);
            binding.flChooseFromMenu.setBackgroundResource(R.color.gray12);
            binding.tvPrice.setVisibility(View.GONE);
        }
    }

    private void getDepartments() {
        Api.getService(Tags.base_url).getShopDepartmentProduct(String.valueOf(placeModel.getMarket_id())).enqueue(new Callback<ShopDepartmentDataModel>() {
            @Override
            public void onResponse(Call<ShopDepartmentDataModel> call, Response<ShopDepartmentDataModel> response) {
                binding.progBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {

                    if (response.body() != null && response.body().getData() != null && response.body().getData().size() > 0) {
                        updateDepartmentsUi(response.body().getData());

                    }
                } else {

                    try {
                        Log.e("error_code", response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


            }

            @Override
            public void onFailure(Call<ShopDepartmentDataModel> call, Throwable t) {


                try {
                    binding.progBar.setVisibility(View.GONE);


                    if (t.getMessage() != null) {
                        Log.e("error", t.getMessage() + "__");

                        if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                            Toast.makeText(ShopProductActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                        } else if (t.getMessage().toLowerCase().contains("socket") || t.getMessage().toLowerCase().contains("canceled")) {
                        } else {
                            Toast.makeText(ShopProductActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                        }
                    }


                } catch (Exception e) {

                }
            }
        });

    }

    private void updateDepartmentsUi(List<ShopDepartments> data) {
        shopDepartmentsList.clear();
        shopDepartmentsList.addAll(data);
        placeModel.setShopDepartmentsList(data);
        binding.setModel(placeModel);
        categoryAdapter = new ProductCategoryAdapter(this, data);
        binding.recViewDepartment.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.recViewDepartment.setAdapter(categoryAdapter);
        binding.recView.scrollToPosition(0);
        ////////////////////////////////////////

        binding.recView.setNestedScrollingEnabled(true);
        productSectionAdapter = new ProductSectionAdapter(this, currency, shopDepartmentsList);
        binding.recView.setLayoutManager(new LinearLayoutManager(this));
        binding.recView.setAdapter(productSectionAdapter);

    }

    private void getPlaceDetails() {

        String fields = "opening_hours,photos,reviews";

        Api.getService("https://maps.googleapis.com/maps/api/")
                .getPlaceDetails(placeModel.getShop_id(), fields, lang, getString(R.string.map_api_key))
                .enqueue(new Callback<PlaceDetailsModel>() {
                    @Override
                    public void onResponse(Call<PlaceDetailsModel> call, Response<PlaceDetailsModel> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getResult() != null && response.body().getResult().getOpening_hours() != null && response.body().getResult().getOpening_hours().getWeekday_text() != null) {
                            placeModel.setHourModelList(getHours(response.body()));
                            updateUI();
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
                            Toast.makeText(ShopProductActivity.this, getString(R.string.something), Toast.LENGTH_LONG).show();
                        } catch (Exception e) {

                        }
                    }
                });
    }


    private List<HourModel> getHours(PlaceDetailsModel placeDetailsModel) {
        List<HourModel> list = new ArrayList<>();

        if (placeDetailsModel != null && placeDetailsModel.getResult() != null && placeDetailsModel.getResult().getOpening_hours() != null && placeDetailsModel.getResult().getOpening_hours().getWeekday_text() != null && placeDetailsModel.getResult().getOpening_hours().getWeekday_text().size() > 0) {

            for (String time : placeDetailsModel.getResult().getOpening_hours().getWeekday_text()) {

                String day = time.split(":", 2)[0].trim();
                String t = time.split(":", 2)[1].trim();
                HourModel hourModel = new HourModel(day, t);
                list.add(hourModel);
            }


        }

        return list;
    }

    private void createDialogAlertHours() {
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .create();
        DialogHoursBinding binding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.dialog_hours, null, false);
        binding.recVeiw.setLayoutManager(new LinearLayoutManager(this));
        HoursAdapter adapter = new HoursAdapter(hourModelList, this);
        binding.recVeiw.setAdapter(adapter);

        binding.btnCancel.setOnClickListener(v -> dialog.dismiss()

        );
        dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_congratulation_animation;
        dialog.setCanceledOnTouchOutside(false);
        dialog.setView(binding.getRoot());
        dialog.show();
    }

    private void createDialogAlertDays() {
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .create();
        DialogHoursBinding binding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.dialog_hours, null, false);
        binding.recVeiw.setLayoutManager(new LinearLayoutManager(this));
        CustomHoursAdapter adapter = new CustomHoursAdapter(daysModelList, this);
        binding.recVeiw.setAdapter(adapter);

        binding.btnCancel.setOnClickListener(v -> dialog.dismiss()

        );
        dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_congratulation_animation;
        dialog.setCanceledOnTouchOutside(false);
        dialog.setView(binding.getRoot());
        dialog.show();
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

    public void setSelectedDepartmentPosition(int adapterPosition) {
        clicked = true;
        binding.recView.scrollToPosition(adapterPosition);
    }

    public void setProductData(ProductModel model, int adapterPosition, int parentPos) {

        this.selectedProduct = model;
        this.childPos = adapterPosition;
        this.parentPos = parentPos;
        count = 1;
        selectedAdditionList = new ArrayList<>();
        binding.tvCount.setText(String.valueOf(count));
        Picasso.get().load(Uri.parse(Tags.IMAGE_URL + model.getImage())).into(binding.image, new com.squareup.picasso.Callback() {
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
        binding.setProductModel(model);
        AdditionProductAdapter additionProductAdapter = new AdditionProductAdapter(this, model.getAddtions(), currency);
        binding.recViewAddition.setLayoutManager(new LinearLayoutManager(this));
        binding.recViewAddition.setAdapter(additionProductAdapter);
        openSheet();
    }

    public void setAdditionItem(AdditionModel additionModel, int pos, boolean isSelected) {
        if (isSelected) {
            selectedAdditionList.add(additionModel);
        } else {
            int additionPos = getAdditionPosition(additionModel);
            if (additionPos!=-1){
                selectedAdditionList.remove(additionPos);

            }


        }

        selectedProduct.setSelectedAdditions(selectedAdditionList);
        double total = getTotalItemCost(selectedProduct) * count;
        binding.tvTotalCost.setText(String.format("%s %s", total, currency));

    }
    private int getAdditionPosition(AdditionModel additionModel){
        int pos = -1;
        for (int index = 0;index<selectedProduct.getSelectedAdditions().size();index++){
            AdditionModel model = selectedProduct.getSelectedAdditions().get(index);
            if (model.getId()==additionModel.getId()){
                pos = index;
                return pos;
            }
        }

        return pos;

    }
    public void deleteSelectedItem(int parentPos, int childPos, ProductModel model) {

        List<ProductModel> productModelList = addOrderProductsModel.getProductModelList();
        ShopDepartments departments = shopDepartmentsList.get(parentPos);
        model.setCount(0);
        departments.getProducts_list().set(childPos, model);
        departments.setCount(0);
        shopDepartmentsList.set(parentPos, departments);
        productSectionAdapter.notifyItemChanged(parentPos);


        int pos = isSelectedProductListHasItem(productModelList, model);
        if (pos != -1) {
            productModelList.remove(pos);
            addOrderProductsModel.setProductModelList(productModelList);
            totalOrderCost = getTotalOrderCost(addOrderProductsModel.getProductModelList());
        }
        updateTotalUi();


    }

    private void openSheet() {
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

    private void closeSheet() {
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

    private int isSelectedProductListHasItem(List<ProductModel> productModelList, ProductModel productModel) {
        int pos = -1;
        for (int index = 0; index < productModelList.size(); index++) {
            ProductModel productModel2 = productModelList.get(index);
            if (productModel.getId() == productModel2.getId()) {
                pos = index;
                return pos;
            }
        }
        return pos;

    }

    private double getTotalOrderCost(List<ProductModel> productModelList) {
        double total = 0.0;
        for (ProductModel model : productModelList) {
            total += (Double.parseDouble(model.getPrice()) + getTotalCostAdditions(model.getSelectedAdditions())) * model.getCount();
        }

        return total;
    }

    private double getTotalCostAdditions(List<AdditionModel> selectedAdditionList) {
        double cost = 0.0;
        for (AdditionModel additionModel : selectedAdditionList) {

            cost += Double.parseDouble(additionModel.getPrice());

        }
        return cost;
    }


    private double getTotalItemCost(ProductModel productModel) {
        double total = (Double.parseDouble(productModel.getPrice()) + getTotalItemCostAdditions(productModel));
        return total;
    }

    private double getTotalItemCostAdditions(ProductModel productModel) {
        double cost = 0.0;
        for (AdditionModel additionModel : productModel.getSelectedAdditions()) {
            cost += Double.parseDouble(additionModel.getPrice());
        }
        return cost;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            int order_id = data.getIntExtra("order_id", 0);
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("order_id", order_id);
            startActivity(intent);
            finish();

        }
    }

    @Override
    public void onBackPressed() {
        if (binding.flSheet.getVisibility() == View.VISIBLE) {
            closeSheet();
        } else {
            super.onBackPressed();

        }
    }


}