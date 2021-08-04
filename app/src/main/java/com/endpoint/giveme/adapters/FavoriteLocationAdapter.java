package com.endpoint.giveme.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.endpoint.giveme.R;
import com.endpoint.giveme.activities_fragments.activity_map_search.MapSearchActivity;
import com.endpoint.giveme.activities_fragments.activity_package_map.PackageMapActivity;
import com.endpoint.giveme.databinding.SavedLocationRowBinding;
import com.endpoint.giveme.models.FavoriteLocationModel;

import java.util.List;

public class FavoriteLocationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<FavoriteLocationModel> list;
    private Context context;
    private LayoutInflater inflater;
    private AppCompatActivity activity;

    public FavoriteLocationAdapter(List<FavoriteLocationModel> list, Context context) {
        this.list = list;
        this.context = context;
        inflater = LayoutInflater.from(context);
        activity = (AppCompatActivity) context;


    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        SavedLocationRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.saved_location_row, parent, false);
        return new MyHolder(binding);


    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        MyHolder myHolder = (MyHolder) holder;
        myHolder.binding.setModel(list.get(position));


        myHolder.itemView.setOnClickListener(v -> {
            FavoriteLocationModel model = list.get(myHolder.getAdapterPosition());
            if (activity instanceof MapSearchActivity){
                MapSearchActivity mapSearchActivity = (MapSearchActivity) activity;
                mapSearchActivity.setFavoriteItem(model);

            }else if (activity instanceof PackageMapActivity){
                PackageMapActivity packageMapActivity = (PackageMapActivity) activity;
                packageMapActivity.setFavoriteItem(model);

            }


        });


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyHolder extends RecyclerView.ViewHolder {
        public SavedLocationRowBinding binding;

        public MyHolder(@NonNull SavedLocationRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }




}
