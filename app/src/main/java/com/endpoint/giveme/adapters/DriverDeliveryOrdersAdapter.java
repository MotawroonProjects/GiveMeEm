package com.endpoint.giveme.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.endpoint.giveme.R;
import com.endpoint.giveme.activities_fragments.activity_home.fragments.fragment_driver_order.Fragment_Driver_Deliver_Order;
import com.endpoint.giveme.databinding.DriverDeliveryRowBinding;
import com.endpoint.giveme.databinding.LoadMoreRowBinding;
import com.endpoint.giveme.models.OrderModel;
import com.endpoint.giveme.models.UserModel;
import com.endpoint.giveme.preferences.Preferences;
import com.endpoint.giveme.tags.Tags;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.paperdb.Paper;

public class DriverDeliveryOrdersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int DATA = 1;
    private final int LOAD = 2;
    private List<OrderModel> list;
    private Context context;
    private LayoutInflater inflater;
    private Fragment fragment;
    private String lang;
    private Preferences preferences;
    private UserModel userModel;

    public DriverDeliveryOrdersAdapter(List<OrderModel> list, Context context, Fragment fragment) {
        this.list = list;
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.fragment = fragment;
        Paper.init(context);
        lang = Paper.book().read("lang","ar");
        preferences = Preferences.getInstance();
        userModel = preferences.getUserData(context);
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType==DATA){
            DriverDeliveryRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.driver_delivery_row, parent, false);
            return new MyHolder(binding);
        }else {
            LoadMoreRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.load_more_row, parent, false);
            return new LoadMoreHolder(binding);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof MyHolder){
            MyHolder myHolder = (MyHolder) holder;
            OrderModel orderModel = list.get(position);
            myHolder.binding.setModel(orderModel);
            myHolder.binding.setLang(lang);

            if (userModel.getUser().getUser_type().equals("client") || (userModel.getUser().getUser_type().equals("driver") && userModel.getUser().getId() == orderModel.getClient().getId())) {
                if (orderModel.getDriver()!=null){
                    Picasso.get().load(Uri.parse(Tags.IMAGE_URL + orderModel.getDriver().getLogo())).placeholder(R.drawable.user_avatar).fit().into(myHolder.binding.userImage);

                }
            } else {

                Picasso.get().load(Uri.parse(Tags.IMAGE_URL + orderModel.getClient().getLogo())).placeholder(R.drawable.user_avatar).fit().into(myHolder.binding.userImage);
            }

            String timeType = orderModel.getOrder_time_arrival();
            long order_time = Long.parseLong(orderModel.getOrder_date())*1000;
            Calendar calendar = Calendar.getInstance();
            Calendar calendarNow = Calendar.getInstance();
            calendar.setTimeInMillis(order_time);
            switch (timeType){
                case "1":
                    calendar.add(Calendar.HOUR_OF_DAY,1);
                    break;
                case "2":
                    calendar.add(Calendar.HOUR_OF_DAY,2);

                    break;
                case "3":
                    calendar.add(Calendar.HOUR_OF_DAY,3);

                    break;
                case "4":
                    calendar.add(Calendar.DAY_OF_MONTH,1);

                    break;
                case "5":
                    calendar.add(Calendar.DAY_OF_MONTH,2);

                    break;
                case "6":
                    calendar.add(Calendar.DAY_OF_MONTH,3);

                    break;
            }

            String status = orderModel.getOrder_status();
            switch (status){
                case "new_order":
                case "have_offer":
                case "pennding":
                case "order_driver_back":
                case "client_cancel":
                case "cancel_for_late":
                    myHolder.binding.llDistance.setVisibility(View.VISIBLE);
                    myHolder.binding.llOrderStatus.setVisibility(View.GONE);
                    myHolder.binding.progBar.setProgress(0);

                    break;

                case "accept_driver":

                case "bill_attach":
                    myHolder.binding.llDistance.setVisibility(View.VISIBLE);
                    myHolder.binding.llOrderStatus.setVisibility(View.VISIBLE);
                    myHolder.binding.tvStatus.setText(R.string.picking_order);
                    myHolder.binding.progBar.setProgress(1);
                    if (calendarNow.getTime().before(calendar.getTime())){
                        myHolder.binding.llLate.setVisibility(View.GONE);
                    }else {
                        myHolder.binding.loadView.setVisibility(View.VISIBLE);
                        myHolder.binding.llLate.setVisibility(View.VISIBLE);
                        myHolder.binding.tvLateTime.setText(getLateTime(calendar.getTimeInMillis()));

                    }
                    if (userModel.getUser().getUser_type().equals("client") || (userModel.getUser().getUser_type().equals("driver") && userModel.getUser().getId() == orderModel.getClient().getId())) {

                        if (orderModel.getOrder_offer()!=null){
                            double deliveryCost = Double.parseDouble(orderModel.getOrder_offer().getOffer_value())+Double.parseDouble(orderModel.getOrder_offer().getTax_value());
                            myHolder.binding.tvDeliveryCost.setText(String.format(Locale.ENGLISH,"%s %s %s",context.getString(R.string.delivery_cost),context.getString(R.string.sar)));

                        }else if (orderModel.getDriver_last_offer()!=null){
                            double deliveryCost = Double.parseDouble(orderModel.getDriver_last_offer().getOffer_value())+Double.parseDouble(orderModel.getDriver_last_offer().getTax_value());
                            myHolder.binding.tvDeliveryCost.setText(String.format(Locale.ENGLISH,"%s %s %s",context.getString(R.string.delivery_cost),deliveryCost,context.getString(R.string.sar)));

                        }

                    }else {
                        if (orderModel.getOrder_offer()!=null){
                            double deliveryCost = Double.parseDouble(orderModel.getOrder_offer().getOffer_value());
                            myHolder.binding.tvDeliveryCost.setText(String.format(Locale.ENGLISH,"%s %s %s",context.getString(R.string.delivery_cost),deliveryCost,context.getString(R.string.sar)));

                        }else if (orderModel.getDriver_last_offer()!=null){
                            double deliveryCost = Double.parseDouble(orderModel.getDriver_last_offer().getOffer_value());
                            myHolder.binding.tvDeliveryCost.setText(String.format(Locale.ENGLISH,"%s %s %s",context.getString(R.string.delivery_cost),deliveryCost,context.getString(R.string.sar)));

                        }

                    }

                    break;

                case "order_collected":
                    myHolder.binding.llDistance.setVisibility(View.VISIBLE);
                    myHolder.binding.llOrderStatus.setVisibility(View.VISIBLE);
                    myHolder.binding.tvStatus.setText(R.string.delivering2);
                    myHolder.binding.progBar.setProgress(2);
                    if (calendarNow.getTime().before(calendar.getTime())){
                        myHolder.binding.llLate.setVisibility(View.GONE);
                    }else {
                        myHolder.binding.loadView.setVisibility(View.VISIBLE);
                        myHolder.binding.llLate.setVisibility(View.VISIBLE);
                        myHolder.binding.tvLateTime.setText(getLateTime(calendar.getTimeInMillis()));

                    }
                    if (userModel.getUser().getUser_type().equals("client") || (userModel.getUser().getUser_type().equals("driver") && userModel.getUser().getId() == orderModel.getClient().getId())) {

                        if (orderModel.getOrder_offer()!=null){
                            double deliveryCost = Double.parseDouble(orderModel.getOrder_offer().getOffer_value())+Double.parseDouble(orderModel.getOrder_offer().getTax_value());
                            myHolder.binding.tvDeliveryCost.setText(String.format(Locale.ENGLISH,"%s %s %s",context.getString(R.string.delivery_cost),deliveryCost,context.getString(R.string.sar)));

                        }else if (orderModel.getDriver_last_offer()!=null){
                            double deliveryCost = Double.parseDouble(orderModel.getDriver_last_offer().getOffer_value())+Double.parseDouble(orderModel.getDriver_last_offer().getTax_value());
                            myHolder.binding.tvDeliveryCost.setText(String.format(Locale.ENGLISH,"%s %s %s",context.getString(R.string.delivery_cost),deliveryCost,context.getString(R.string.sar)));

                        }

                    }else {
                        if (orderModel.getOrder_offer()!=null){
                            double deliveryCost = Double.parseDouble(orderModel.getOrder_offer().getOffer_value());
                            myHolder.binding.tvDeliveryCost.setText(String.format(Locale.ENGLISH,"%s %s %s",context.getString(R.string.delivery_cost),deliveryCost,context.getString(R.string.sar)));

                        }else if (orderModel.getDriver_last_offer()!=null){
                            double deliveryCost = Double.parseDouble(orderModel.getDriver_last_offer().getOffer_value());
                            myHolder.binding.tvDeliveryCost.setText(String.format(Locale.ENGLISH,"%s %s %s",context.getString(R.string.delivery_cost),deliveryCost,context.getString(R.string.sar)));

                        }

                    }

                    break;
                case "reach_to_client":
                    myHolder.binding.llDistance.setVisibility(View.VISIBLE);
                    myHolder.binding.llOrderStatus.setVisibility(View.VISIBLE);
                    myHolder.binding.tvStatus.setText(R.string.on_location);
                    myHolder.binding.progBar.setProgress(3);

                    SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd/MM/yyyy hh:mm aa", Locale.ENGLISH);
                    String df1 = dateFormat1.format(new Date(calendarNow.getTimeInMillis()));

                    SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd/MM/yyyy hh:mm aa", Locale.ENGLISH);
                    String df2 = dateFormat2.format(new Date(calendar.getTimeInMillis()));

                    if (calendarNow.getTime().before(calendar.getTime())){
                        myHolder.binding.llLate.setVisibility(View.GONE);
                    }else {
                        myHolder.binding.loadView.setVisibility(View.VISIBLE);
                        myHolder.binding.llLate.setVisibility(View.VISIBLE);
                        myHolder.binding.tvLateTime.setText(getLateTime(calendar.getTimeInMillis()));

                    }

                    if (userModel.getUser().getUser_type().equals("client") || (userModel.getUser().getUser_type().equals("driver") && userModel.getUser().getId() == orderModel.getClient().getId())) {

                        if (orderModel.getOrder_offer()!=null){
                            double deliveryCost = Double.parseDouble(orderModel.getOrder_offer().getOffer_value())+Double.parseDouble(orderModel.getOrder_offer().getTax_value());
                            myHolder.binding.tvDeliveryCost.setText(String.format(Locale.ENGLISH,"%s %s %s",context.getString(R.string.delivery_cost),deliveryCost,context.getString(R.string.sar)));

                        }else if (orderModel.getDriver_last_offer()!=null){
                            double deliveryCost = Double.parseDouble(orderModel.getDriver_last_offer().getOffer_value())+Double.parseDouble(orderModel.getDriver_last_offer().getTax_value());
                            myHolder.binding.tvDeliveryCost.setText(String.format(Locale.ENGLISH,"%s %s %s",context.getString(R.string.delivery_cost),deliveryCost,context.getString(R.string.sar)));

                        }

                    }else {
                        if (orderModel.getOrder_offer()!=null){
                            double deliveryCost = Double.parseDouble(orderModel.getOrder_offer().getOffer_value());
                            myHolder.binding.tvDeliveryCost.setText(String.format(Locale.ENGLISH,"%s %s %s",context.getString(R.string.delivery_cost),deliveryCost,context.getString(R.string.sar)));

                        }else if (orderModel.getDriver_last_offer()!=null){
                            double deliveryCost = Double.parseDouble(orderModel.getDriver_last_offer().getOffer_value());
                            myHolder.binding.tvDeliveryCost.setText(String.format(Locale.ENGLISH,"%s %s %s",context.getString(R.string.delivery_cost),deliveryCost,context.getString(R.string.sar)));

                        }

                    }

                    break;
                case "driver_end_rate":
                case "client_end_and_rate":
                    myHolder.binding.llDistance.setVisibility(View.VISIBLE);
                    myHolder.binding.llOrderStatus.setVisibility(View.VISIBLE);
                    myHolder.binding.tvStatus.setText(R.string.delivered);
                    myHolder.binding.progBar.setProgress(4);

                    break;


            }



            myHolder.itemView.setOnClickListener(v -> {
                OrderModel orderModel1 = list.get(holder.getAdapterPosition());
                if (fragment instanceof Fragment_Driver_Deliver_Order){
                    Fragment_Driver_Deliver_Order fragment_driver_deliver_order = (Fragment_Driver_Deliver_Order) fragment;
                    fragment_driver_deliver_order.setItemData(orderModel1);
                }

            });

        }else if (holder instanceof LoadMoreHolder){
            LoadMoreHolder loadMoreHolder = (LoadMoreHolder) holder;
            loadMoreHolder.binding.prgBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(context,R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
            loadMoreHolder.binding.prgBar.setIndeterminate(true);
        }

    }

    private String getLateTime(long order_time)
    {
        int second = 1000;
        int minute = second * 60;
        int hour = minute * 60;
        int day = hour * 24;
        Calendar calendarNow = Calendar.getInstance();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(order_time);
        long diff = calendarNow.getTimeInMillis()-calendar.getTimeInMillis();
        if (diff < 5*minute) {
            return context.getString(R.string.min5_late);

        }else if (diff < 16*minute) {
            return context.getString(R.string.min15_late);

        } else if (diff < 35 * minute) {
            return context.getString(R.string.min30_late);

        } else if (diff < 2*hour) {
            return context.getString(R.string.hour1_late);

        } else if (diff < 3 * hour) {
            return context.getString(R.string.hour2_late);

        }else if (diff < 4 * hour) {
            return context.getString(R.string.hour3_late);

        } else if (diff < 2*day) {

            return context.getString(R.string.day1_late);

        } else if (diff < 3 * day) {
            return context.getString(R.string.day2_late);
        }else if (diff < 4 * day) {
            return context.getString(R.string.day3_late);
        } else {
            return context.getString(R.string.more_days3_late);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyHolder extends RecyclerView.ViewHolder
    {
        private DriverDeliveryRowBinding binding;

        public MyHolder(DriverDeliveryRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;


        }

    }

    public static class LoadMoreHolder extends RecyclerView.ViewHolder
    {
        private LoadMoreRowBinding binding;

        public LoadMoreHolder(LoadMoreRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;


        }

    }

    @Override
    public int getItemViewType(int position) {
        if (list.get(position)==null){
            return LOAD;
        }else {
            return DATA;
        }
    }
}
