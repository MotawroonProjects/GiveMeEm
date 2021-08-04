package com.endpoint.giveme.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.endpoint.giveme.R;
import com.endpoint.giveme.models.CustomPlaceModel;
import com.endpoint.giveme.tags.Tags;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CustomImagePagerAdapter extends PagerAdapter {
    private List<CustomPlaceModel.Gallery> list;
    private Context context;
    private LayoutInflater inflater;

    public CustomImagePagerAdapter(List<CustomPlaceModel.Gallery> list, Context context) {
        this.list = list;
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view ==object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        View view = inflater.inflate(R.layout.restaurant_image_row,container,false);
        RoundedImageView imageView = view.findViewById(R.id.image);
        String url = Tags.IMAGE_URL+list.get(position).getImage();
        Picasso.get().load(Uri.parse(url)).into(imageView);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
