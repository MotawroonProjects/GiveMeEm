package com.endpoint.giveme.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.endpoint.giveme.R;

import java.util.ArrayList;
import java.util.List;

public class IntroAdapter extends PagerAdapter {
    private List<Integer> imageList;
    private Context context;
    private LayoutInflater inflater;

    public IntroAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        imageList = new ArrayList<>();
        imageList.add(R.drawable.slider1);
        imageList.add(R.drawable.slider2);
        imageList.add(R.drawable.slider3);

    }

    @Override
    public int getCount() {
        return imageList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return object.equals(view);
    }



    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = inflater.inflate(R.layout.intro_row,container,false);
        ImageView imageView = view.findViewById(R.id.image);
        imageView.setImageResource(imageList.get(position));
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
