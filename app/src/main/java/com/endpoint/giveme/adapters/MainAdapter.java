package com.endpoint.giveme.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.endpoint.giveme.R;
import com.endpoint.giveme.activities_fragments.activity_home.HomeActivity;
import com.endpoint.giveme.activities_fragments.activity_home.fragments.Fragment_Main;
import com.endpoint.giveme.databinding.MainCategoryDataRowBinding;
import com.endpoint.giveme.databinding.MainSliderRowBinding;
import com.endpoint.giveme.models.CategoryDataModel;
import com.endpoint.giveme.models.CategoryModel;
import com.endpoint.giveme.models.CustomPlaceDataModel;
import com.endpoint.giveme.models.MainItemData;
import com.endpoint.giveme.models.NearbyModel;
import com.endpoint.giveme.models.SliderModel;
import com.endpoint.giveme.remote.Api;
import com.endpoint.giveme.tags.Tags;
import com.ethanhua.skeleton.Skeleton;
import com.ethanhua.skeleton.SkeletonScreen;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int SLIDER = 1;
    private final int DATA = 2;

    private List<MainItemData> list;
    private Context context;
    private LayoutInflater inflater;
    private Fragment_Main fragment_main;
    private List<NearbyModel.Result> resultList;
    private boolean hasManyPages = false;
    private boolean isLoading = false;
    private String query = "food|restaurant|supermarket|bakery";
    private String next_page = "";
    private List<SliderModel.Data> sliderList;
    private SliderAdapter sliderAdapter;
    private SkeletonScreen skeletonPopular,skeletonCategory;
    private double user_lat=0.0,user_lng=0.0;
    private NearbyAdapter2 nearbyAdapter;
    private CategoryAdapter categoryAdapter;
    private List<CategoryModel> categoryModelList;
    private HomeActivity activity;
    private String lang;
    private Timer timer;
    private Task task;
    private MainSliderRowBinding mainSliderRowBinding;
    private String currency;

    public MainAdapter(List<MainItemData> list, Context context, Fragment_Main fragment_main, double user_lat, double user_lng,String currency) {
        this.list = list;
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.fragment_main = fragment_main;
        resultList = new ArrayList<>();
        sliderList = new ArrayList<>();
        categoryModelList = new ArrayList<>();
        this.user_lat = user_lat;
        this.user_lng = user_lng;
        Paper.init(context);
        lang = Paper.book().read("lang","ar");
        activity = (HomeActivity) context;
        this.currency = currency;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType==DATA){
            MainCategoryDataRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.main_category_data_row, parent, false);
            return new MyHolder(binding);
        }else {
            MainSliderRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.main_slider_row, parent, false);
            return new SliderHolder(binding);
        }



    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof MyHolder){
            MyHolder myHolder = (MyHolder) holder;
            myHolder.binding.recView.setLayoutManager(new GridLayoutManager(context,2));


            skeletonCategory = Skeleton.bind(myHolder.binding.recView)
                    .frozen(false)
                    .duration(1000)
                    .shimmer(true)
                    .count(8)
                    .load(R.layout.category_row)
                    .show();

            getCategory(myHolder.binding);

        }else if (holder instanceof SliderHolder){
            SliderHolder sliderHolder = (SliderHolder) holder;

            sliderHolder.binding.recViewPopular.setLayoutManager(new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false));
            sliderHolder.binding.recViewPopular.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (dx>0){
                        if (nearbyAdapter!=null){
                            int totalItem = nearbyAdapter.getItemCount();
                            LinearLayoutManager manager = (LinearLayoutManager) sliderHolder.binding.recViewPopular.getLayoutManager();
                            int pos = manager.findLastCompletelyVisibleItemPosition();
                            if (hasManyPages&&totalItem>=18&&(totalItem-pos==2)&&!isLoading){
                                isLoading = true;
                                loadMore();

                            }
                        }

                    }
                }
            });



            skeletonPopular = Skeleton.bind(sliderHolder.binding.recViewPopular)
                    .frozen(false)
                    .duration(1000)
                    .shimmer(true)
                    .count(3)
                    .load(R.layout.shop_row)
                    .show();

            addSliderImages(sliderHolder.binding);
            getNearByShops(sliderHolder.binding);


        }


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void stopTimer() {
        if (timer!=null){
            timer.purge();
            timer.cancel();
        }
        if (task!=null){
            task.cancel();
        }
    }

    public static class MyHolder extends RecyclerView.ViewHolder {
        public MainCategoryDataRowBinding binding;

        public MyHolder(@NonNull MainCategoryDataRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }

    public static class SliderHolder extends RecyclerView.ViewHolder {
        public MainSliderRowBinding binding;

        public SliderHolder(@NonNull MainSliderRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }


    @Override
    public int getItemViewType(int position) {
        MainItemData itemData = list.get(position);
        if (itemData.getType()==0){
            return SLIDER;
        }else {
            return DATA;
        }







    }



    private void addSliderImages(MainSliderRowBinding binding) {

        Api.getService(Tags.base_url)
                .getSlider()
                .enqueue(new Callback<SliderModel>() {
                    @Override
                    public void onResponse(Call<SliderModel> call, Response<SliderModel> response) {
                        binding.progBar.setVisibility(View.GONE);

                        if (response.isSuccessful() && response.body() != null) {
                            sliderList.clear();
                            sliderList.addAll(response.body().getData());
                            if (sliderList.size()>0){
                                binding.pager.setVisibility(View.VISIBLE);
                                updateSliderUi(binding);

                            }else {
                                binding.pager.setVisibility(View.GONE);

                            }

                        } else {

                            binding.pager.setVisibility(View.GONE);
                            binding.progBar.setVisibility(View.GONE);

                            try {
                                Log.e("error_code", response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<SliderModel> call, Throwable t) {
                        try {
                            Log.e("Error", t.getMessage());
                            binding.progBar.setVisibility(View.GONE);

                            Toast.makeText(context, context.getString(R.string.something), Toast.LENGTH_LONG).show();
                        } catch (Exception e) {

                        }
                    }
                });

    }

    private void updateSliderUi(MainSliderRowBinding binding) {
        if (sliderList.size() > 0) {

            sliderAdapter = new SliderAdapter(sliderList, context);
            binding.pager.setAdapter(sliderAdapter);

            int margin = (int) (context.getResources().getDisplayMetrics().density * 10);
            int padding = (int) (context.getResources().getDisplayMetrics().density * 40);
            /*binding.pager.setPageMargin(margin);
            binding.pager.setPadding(padding, 0, padding, 0);*/

            if (sliderList.size() > 1) {
                timer = new Timer();
                task = new Task(binding);
                timer.scheduleAtFixedRate(task, 3000, 3000);
            }
        }
    }

    private void getNearByShops(MainSliderRowBinding binding) {
        String loc = user_lat + "," + user_lng;
        Api.getService("https://maps.googleapis.com/maps/api/")
                .nearbyPlaceRankBy(loc, query, "distance", lang, "", context.getString(R.string.map_api_key))
                .enqueue(new Callback<NearbyModel>() {
                    @Override
                    public void onResponse(Call<NearbyModel> call, Response<NearbyModel> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            if (response.body().getStatus().equals("OK")) {

                                if (response.body().getNext_page_token() != null) {
                                    hasManyPages = true;
                                    next_page = response.body().getNext_page_token();
                                } else {
                                    hasManyPages = false;
                                    next_page = "";
                                }

                                if (response.body().getResults().size() > 0) {
                                    calculateDistance(response.body().getResults(),binding);

                                } else {
                                    binding.llPopular.setVisibility(View.GONE);

                                }
                            } else {
                                binding.llPopular.setVisibility(View.GONE);

                            }

                        } else {

                            skeletonPopular.hide();
                            binding.llPopular.setVisibility(View.GONE);

                            try {
                                Log.e("error_code", response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<NearbyModel> call, Throwable t) {
                        try {
                            Log.e("Error", t.getMessage());
                            skeletonPopular.hide();
                            binding.llPopular.setVisibility(View.GONE);


                            if (t.getMessage() != null) {
                                Log.e("error", t.getMessage());
                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(context, context.getString(R.string.something), Toast.LENGTH_LONG).show();
                                }
                                else if (t.getMessage().toLowerCase().contains("socket")||t.getMessage().toLowerCase().contains("canceled")){ }

                                else {
                                    Toast.makeText(context, t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }

                        } catch (Exception e) {

                        }
                    }
                });
    }

    private void loadMore() {

        resultList.add(null);
        nearbyAdapter.notifyItemInserted(resultList.size()-1);


        String loc = user_lat+","+user_lng;
        Log.e("loc",loc);
        Api.getService("https://maps.googleapis.com/maps/api/")
                .nearbyPlaceRankBy(loc,query,"distance",lang,next_page,context.getString(R.string.map_api_key))
                .enqueue(new Callback<NearbyModel>() {
                    @Override
                    public void onResponse(Call<NearbyModel> call, Response<NearbyModel> response) {
                        if (response.isSuccessful()&&response.body()!=null)
                        {
                            if (response.body().getStatus().equals("OK")){

                                if (response.body().getNext_page_token()!=null){
                                    hasManyPages = true;
                                    next_page = response.body().getNext_page_token();
                                }else {
                                    hasManyPages = false;
                                    next_page = "";
                                }
                                if (response.body().getResults().size()>0)
                                {

                                    calculateDistanceLoadMore(response.body().getResults());
                                }else {
                                    isLoading = false;

                                    if (resultList.get(resultList.size()-1)==null){
                                        resultList.remove(resultList.size()-1);
                                        nearbyAdapter.notifyItemRemoved(resultList.size()-1);
                                    }
                                }
                            }

                        }else
                        {

                            try {
                                Log.e("error_code",response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<NearbyModel> call, Throwable t) {
                        try {

                            if (t.getMessage() != null) {
                                Log.e("error", t.getMessage());
                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(context, context.getString(R.string.something), Toast.LENGTH_LONG).show();
                                }
                                else if (t.getMessage().toLowerCase().contains("socket")||t.getMessage().toLowerCase().contains("canceled")){ }

                                else {
                                    Toast.makeText(context, t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }

                        }catch (Exception e)
                        {

                        }
                    }
                });
    }

    private void calculateDistance(List<NearbyModel.Result> results,MainSliderRowBinding binding){
        this.mainSliderRowBinding = binding;
        for (int i =0 ;i<results.size();i++){
            NearbyModel.Result result = results.get(i);

            if (result!=null){

                LatLng user_location = new LatLng(user_lat,user_lng);
                LatLng place_location = new LatLng(result.getGeometry().getLocation().getLat(),result.getGeometry().getLocation().getLng());

                double distance = getDistance(user_location,place_location);

                result.setDistance(distance);
                resultList.add(result);
            }

        }



        if (resultList.size()>0){
            binding.tv.setVisibility(View.VISIBLE);
            getPlaceDataByGooglePlaceId(0);

        }else {
            binding.tv.setVisibility(View.GONE);
        }




    }

    private void calculateDistanceLoadMore(List<NearbyModel.Result> results) {

        List<NearbyModel.Result> resultListFiltered = new ArrayList<>();

        for (int i = 0; i < results.size(); i++) {
            NearbyModel.Result result = results.get(i);

            if (result != null) {


                result.setDistance(getDistance(new LatLng(user_lat, user_lng), new LatLng(result.getGeometry().getLocation().getLat(), result.getGeometry().getLocation().getLng())));
                resultListFiltered.add(result);

            }

        }



        getPlaceDataByGooglePlaceIdLoadMore(0,resultListFiltered);


    }

    private double getDistance(LatLng latLng1, LatLng latLng2) {
        return SphericalUtil.computeDistanceBetween(latLng1, latLng2) / 1000;
    }

    private void getCategory(MainCategoryDataRowBinding binding) {


        categoryModelList.clear();
        Api.getService(Tags.base_url)
                .getCategory()
                .enqueue(new Callback<CategoryDataModel>() {
                    @Override
                    public void onResponse(Call<CategoryDataModel> call, Response<CategoryDataModel> response) {
                        skeletonCategory.hide();
                        if (response.isSuccessful() && response.body() != null) {
                            categoryModelList.addAll(response.body().getData());
                            categoryAdapter = new CategoryAdapter(categoryModelList,context,fragment_main);
                            binding.recView.setAdapter(categoryAdapter);

                        } else {

                            skeletonCategory.hide();

                            try {
                                Log.e("error_code", response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<CategoryDataModel> call, Throwable t) {
                        try {
                            skeletonCategory.hide();
                            if (t.getMessage() != null) {
                                Log.e("error", t.getMessage());
                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(context, context.getString(R.string.something), Toast.LENGTH_LONG).show();
                                }
                                else if (t.getMessage().toLowerCase().contains("socket")||t.getMessage().toLowerCase().contains("canceled")){ }
                                else {
                                    Toast.makeText(context, t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (Exception e) {

                        }
                    }
                });


    }


    private void getPlaceDataByGooglePlaceId(int index)
    {
        if (index<resultList.size()){

            Api.getService(Tags.base_url)
                    .getCustomPlaceByGooglePlaceId(resultList.get(index).getPlace_id())
                    .enqueue(new Callback<CustomPlaceDataModel>() {
                        @Override
                        public void onResponse(Call<CustomPlaceDataModel> call, Response<CustomPlaceDataModel> response) {
                            if (response.isSuccessful()) {

                                NearbyModel.Result result = resultList.get(index);
                                result.setCustomPlaceModel(response.body().getData());
                                resultList.set(index,result);

                                int newIndex = index+1;
                                getPlaceDataByGooglePlaceId(newIndex);


                            } else {

                                int newIndex = index+1;
                                getPlaceDataByGooglePlaceId(newIndex);

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
                                int newIndex = index+1;
                                getPlaceDataByGooglePlaceId(newIndex);

                                Log.e("Error", t.getMessage());
                            } catch (Exception e) {

                            }
                        }
                    });
        }else {

            skeletonPopular.hide();
            nearbyAdapter = new NearbyAdapter2(resultList,context,fragment_main,currency);
            mainSliderRowBinding.recViewPopular.setAdapter(nearbyAdapter);
        }
    }


    private void getPlaceDataByGooglePlaceIdLoadMore(int index, List<NearbyModel.Result> results)
    {
        if (index<results.size()){

            Api.getService(Tags.base_url)
                    .getCustomPlaceByGooglePlaceId(resultList.get(index).getPlace_id())
                    .enqueue(new Callback<CustomPlaceDataModel>() {
                        @Override
                        public void onResponse(Call<CustomPlaceDataModel> call, Response<CustomPlaceDataModel> response) {
                            if (response.isSuccessful()) {

                                NearbyModel.Result result = results.get(index);
                                result.setCustomPlaceModel(response.body().getData());
                                results.set(index,result);

                                int newIndex = index+1;
                                getPlaceDataByGooglePlaceIdLoadMore(newIndex,results);


                            } else {

                                int newIndex = index+1;
                                getPlaceDataByGooglePlaceIdLoadMore(newIndex,results);

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
                                int newIndex = index+1;
                                getPlaceDataByGooglePlaceIdLoadMore(newIndex,results);

                                Log.e("Error", t.getMessage());
                            } catch (Exception e) {

                            }
                        }
                    });
        }else {

            isLoading = false;
            if (resultList.get(resultList.size()-1)==null){
                resultList.remove(resultList.size()-1);
                nearbyAdapter.notifyItemRemoved(resultList.size()-1);
            }
            int oldPos = resultList.size()-1;

            resultList.addAll(results);
            int newPos = resultList.size();
            nearbyAdapter.notifyItemRangeChanged(oldPos,newPos);
        }
    }

    private  class Task extends TimerTask{
        private MainSliderRowBinding binding;

        public Task(MainSliderRowBinding binding) {
            this.binding = binding;
        }

        @Override
        public void run() {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (binding.pager.getCurrentItem()<sliderList.size()-1){
                        binding.pager.setCurrentItem(binding.pager.getCurrentItem()+1);
                    }else {
                        binding.pager.setCurrentItem(0,false);
                    }
                }
            });
        }
    }
}
