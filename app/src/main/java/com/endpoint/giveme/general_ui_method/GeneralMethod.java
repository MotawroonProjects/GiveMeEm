package com.endpoint.giveme.general_ui_method;

import android.net.Uri;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.databinding.BindingAdapter;

import com.endpoint.giveme.R;
import com.endpoint.giveme.models.NearbyModel;
import com.endpoint.giveme.tags.Tags;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;
import com.iarcuschin.simpleratingbar.SimpleRatingBar;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class GeneralMethod {

    @BindingAdapter("error")
    public static void errorValidation(View view, String error) {
        if (view instanceof EditText) {
            EditText ed = (EditText) view;
            ed.setError(error);
        } else if (view instanceof TextView) {
            TextView tv = (TextView) view;
            tv.setError(error);


        }
    }


    @BindingAdapter("placeStoreImage")
    public static void placeStoreImage(View view, NearbyModel.Result result) {
        if (view instanceof CircleImageView) {
            CircleImageView imageView = (CircleImageView) view;


            //https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=CmRaAAAA_sTOr-cDhIZuoi4D0YpT9EmNM2UWY5Vb2fDcqO-_SQ9Qmf2dyNFg_gtSs-dXu4puk6Q957hhfPKvsfhY8w7W2aL-w9HfPrHlitd1PAA-vxb85ZDYEdxIleV8y9_uQQwzEhC-npYoXzXwrwrrxasjKXLQGhTi_joH1kyW7hzZIAIY5c04_27qUg&key=AIzaSyA6QI378BHt9eqBbiJKtqWHTSAZxcSwN3Q
            if (result.getPhotos()!=null){
                if (result.getPhotos().size()>0)
                {
                    String url = Tags.IMAGE_Places_URL+result.getPhotos().get(0).getPhoto_reference()+"&key="+view.getContext().getString(R.string.map_api_key);
                    Picasso.get().load(Uri.parse(url)).fit().into(imageView);

                }else
                {
                    Picasso.get().load(Uri.parse(result.getIcon())).fit().into(imageView);

                }
            }else {
                Picasso.get().load(Uri.parse(result.getIcon())).fit().into(imageView);

            }



        } else if (view instanceof RoundedImageView) {
            RoundedImageView imageView = (RoundedImageView) view;

            if (result.getPhotos()!=null){
                if (result.getPhotos().size()>0)
                {
                    String url = Tags.IMAGE_Places_URL+result.getPhotos().get(0).getPhoto_reference()+"&key="+view.getContext().getString(R.string.map_api_key);
                    Picasso.get().load(Uri.parse(url)).fit().into(imageView);

                }else
                {
                    Picasso.get().load(Uri.parse(result.getIcon())).fit().into(imageView);

                }
            }else {
                Picasso.get().load(Uri.parse(result.getIcon())).fit().into(imageView);

            }

        } else if (view instanceof ImageView) {
            ImageView imageView = (ImageView) view;

            if (result.getPhotos()!=null){
                if (result.getPhotos().size()>0)
                {
                    String url = Tags.IMAGE_Places_URL+result.getPhotos().get(0).getPhoto_reference()+"&key="+view.getContext().getString(R.string.map_api_key);
                    Picasso.get().load(Uri.parse(url)).fit().into(imageView);

                }else
                {
                    Picasso.get().load(Uri.parse(result.getIcon())).fit().into(imageView);

                }
            }else {
                Picasso.get().load(Uri.parse(result.getIcon())).fit().into(imageView);

            }

        }

    }


    @BindingAdapter("placeStoreIcon")
    public static void placeStoreIcon(View view, String reference) {
        if (view instanceof CircleImageView) {
            CircleImageView imageView = (CircleImageView) view;

            String url = Tags.IMAGE_Places_URL+reference+"&key="+view.getContext().getString(R.string.map_api_key);
            Picasso.get().load(Uri.parse(url)).fit().into(imageView);



        } else if (view instanceof RoundedImageView) {
            RoundedImageView imageView = (RoundedImageView) view;

            String url = Tags.IMAGE_Places_URL+reference+"&key="+view.getContext().getString(R.string.map_api_key);
            Picasso.get().load(Uri.parse(url)).fit().into(imageView);

        } else if (view instanceof ImageView) {
            ImageView imageView = (ImageView) view;

            String url = Tags.IMAGE_Places_URL+reference+"&key="+view.getContext().getString(R.string.map_api_key);
            Picasso.get().load(Uri.parse(url)).fit().into(imageView);

        }

    }

    @BindingAdapter("chat_image")
    public static void chat_image(View view, String endPoint) {
        if (view instanceof CircleImageView) {
            CircleImageView imageView = (CircleImageView) view;
            if (endPoint!=null){
                Picasso.get().load(Tags.IMAGE_URL+endPoint).into(imageView);

            }


        } else if (view instanceof RoundedImageView) {
            RoundedImageView imageView = (RoundedImageView) view;
            if (endPoint!=null){
                Picasso.get().load(Tags.IMAGE_URL+endPoint).into(imageView);

            }
        } else if (view instanceof ImageView) {
            ImageView imageView = (ImageView) view;
            if (endPoint!=null){
                Picasso.get().load(Tags.IMAGE_URL+endPoint).into(imageView);

            }

        }

    }


    @BindingAdapter("distance")
    public static void distance(TextView view,double distance){
        view.setText(String.format(Locale.ENGLISH,"%.2f %s",distance,view.getContext().getString(R.string.km)));
    }





    @BindingAdapter("image")
    public static void image(View view, String endPoint) {
        if (view instanceof CircleImageView) {
            CircleImageView imageView = (CircleImageView) view;
            if (endPoint != null) {

                Picasso.get().load(Uri.parse(Tags.IMAGE_URL + endPoint)).placeholder(R.drawable.image_avatar).into(imageView);
            } else {
                Picasso.get().load(R.drawable.image_avatar).into(imageView);

            }
        } else if (view instanceof RoundedImageView) {
            RoundedImageView imageView = (RoundedImageView) view;

            if (endPoint != null) {

                Picasso.get().load(Uri.parse(Tags.IMAGE_URL + endPoint)).placeholder(R.drawable.image_avatar).into(imageView);
            } else {
                Picasso.get().load(R.drawable.image_avatar).into(imageView);

            }
        } else if (view instanceof ImageView) {
            ImageView imageView = (ImageView) view;

            if (endPoint != null) {

                Picasso.get().load(Uri.parse(Tags.IMAGE_URL + endPoint)).placeholder(R.drawable.image_avatar).into(imageView);
            } else {
                Picasso.get().load(R.drawable.image_avatar).into(imageView);

            }
        }

    }

    @BindingAdapter("order_image")
    public static void order_image(View view, String endPoint) {
        if (view instanceof CircleImageView) {
            CircleImageView imageView = (CircleImageView) view;
            if (endPoint != null) {

                Picasso.get().load(Uri.parse(Tags.IMAGE_URL + endPoint)).placeholder(R.drawable.image_delivery_scooter).into(imageView);
            } else {
                Picasso.get().load(R.drawable.image_delivery_scooter).into(imageView);

            }
        } else if (view instanceof RoundedImageView) {
            RoundedImageView imageView = (RoundedImageView) view;

            if (endPoint != null) {

                Picasso.get().load(Uri.parse(Tags.IMAGE_URL + endPoint)).placeholder(R.drawable.image_delivery_scooter).into(imageView);
            } else {
                Picasso.get().load(R.drawable.image_delivery_scooter).into(imageView);

            }
        } else if (view instanceof ImageView) {
            ImageView imageView = (ImageView) view;

            if (endPoint != null) {

                Picasso.get().load(Uri.parse(Tags.IMAGE_URL + endPoint)).placeholder(R.drawable.image_delivery_scooter).into(imageView);
            } else {
                Picasso.get().load(R.drawable.image_delivery_scooter).into(imageView);

            }
        }

    }

    @BindingAdapter("menuImage")
    public static void menuImage(View view, String endPoint) {
        if (view instanceof CircleImageView) {
            CircleImageView imageView = (CircleImageView) view;
            if (endPoint != null) {

                Picasso.get().load(Uri.parse(Tags.IMAGE_URL + endPoint)).into(imageView);
            } else {
                Picasso.get().load(R.drawable.image_delivery_scooter).into(imageView);

            }
        } else if (view instanceof RoundedImageView) {
            RoundedImageView imageView = (RoundedImageView) view;

            if (endPoint != null) {

                Picasso.get().load(Uri.parse(Tags.IMAGE_URL + endPoint)).into(imageView);
            } else {
                Picasso.get().load(R.drawable.image_delivery_scooter).into(imageView);

            }
        } else if (view instanceof ImageView) {
            ImageView imageView = (ImageView) view;

            if (endPoint != null) {

                Picasso.get().load(Uri.parse(Tags.IMAGE_URL + endPoint)).into(imageView);
            } else {
                Picasso.get().load(R.drawable.image_delivery_scooter).into(imageView);

            }
        }

    }

    @BindingAdapter("rate")
    public static void rate(SimpleRatingBar ratingBar,double rate) {
        ratingBar.setRating((float) rate);

    }


    @BindingAdapter("day")
    public static void getDay(TextView textView,int day) {

        switch (day){
            case 1:
                textView.setText(textView.getContext().getString(R.string.mon));
                break;
            case 2:
                textView.setText(textView.getContext().getString(R.string.tue));
                break;
            case 3:
                textView.setText(textView.getContext().getString(R.string.wed));
                break;
            case 4:
                textView.setText(textView.getContext().getString(R.string.thur));
                break;
            case 5:
                textView.setText(textView.getContext().getString(R.string.fri));
                break;
            case 6:
                textView.setText(textView.getContext().getString(R.string.sat));
                break;
            case 7:
                textView.setText(textView.getContext().getString(R.string.sun));
                break;
        }

    }


    @BindingAdapter("delivery_time")
    public static void getDeliveryTime(TextView textView,String delivery_time) {

        switch (delivery_time){
            case "1":
                textView.setText(String.format(Locale.ENGLISH,"%s %s",textView.getContext().getString(R.string.within2),textView.getContext().getString(R.string.hour1)));
                break;
            case "2":
                textView.setText(String.format(Locale.ENGLISH,"%s %s",textView.getContext().getString(R.string.within2),textView.getContext().getString(R.string.hour2)));

                break;
            case "3":
                textView.setText(String.format(Locale.ENGLISH,"%s %s",textView.getContext().getString(R.string.within2),textView.getContext().getString(R.string.hour3)));
                break;
            case "4":
                textView.setText(String.format(Locale.ENGLISH,"%s %s",textView.getContext().getString(R.string.within2),textView.getContext().getString(R.string.day1)));
                break;
            case "5":
                textView.setText(String.format(Locale.ENGLISH,"%s %s",textView.getContext().getString(R.string.within2),textView.getContext().getString(R.string.day2)));
                break;
            case "6":
                textView.setText(String.format(Locale.ENGLISH,"%s %s",textView.getContext().getString(R.string.within2),textView.getContext().getString(R.string.day3)));
                break;

        }

    }

    @BindingAdapter({"driver_lat","driver_lng","place_lat","place_lng"})
    public static void distance(TextView textView,double driver_lat,double driver_lng,double place_lat,double place_lng){
       double distance = SphericalUtil.computeDistanceBetween(new LatLng(driver_lat,driver_lng), new LatLng(place_lat,place_lng)) / 1000;
       textView.setText(String.format(Locale.ENGLISH,"%s %s",String.format(Locale.ENGLISH,"%.2f",distance),textView.getContext().getString(R.string.km)));
    }
}










