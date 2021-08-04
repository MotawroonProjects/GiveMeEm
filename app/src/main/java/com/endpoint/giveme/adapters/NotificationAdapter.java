package com.endpoint.giveme.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.endpoint.giveme.R;
import com.endpoint.giveme.activities_fragments.activity_home.fragments.Fragment_Notifications;
import com.endpoint.giveme.databinding.LoadMoreRowBinding;
import com.endpoint.giveme.databinding.NotificationRowBinding;
import com.endpoint.giveme.models.NotificationDataModel;
import com.endpoint.giveme.share.Time_Ago;

import java.util.List;

import io.paperdb.Paper;

public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int DATA = 1;
    private final int LOAD = 2;
    private List<NotificationDataModel.NotificationModel> list;
    private Context context;
    private LayoutInflater inflater;
    private Fragment_Notifications fragment_notifications;
    private String lang;


    public NotificationAdapter(List<NotificationDataModel.NotificationModel> list, Context context, Fragment_Notifications fragment_notifications) {
        this.list = list;
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.fragment_notifications = fragment_notifications;
        Paper.init(context);
        lang = Paper.book().read("lang","ar");
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType==DATA){
            NotificationRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.notification_row, parent, false);
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
            NotificationDataModel.NotificationModel model = list.get(position);
            myHolder.binding.setModel(model);
            myHolder.binding.setLang(lang);
            myHolder.binding.tvDate.setText(Time_Ago.getTimeAgo(Long.parseLong(model.getNotification_date())*1000,context));
            myHolder.itemView.setOnClickListener(v -> {
                NotificationDataModel.NotificationModel model2 = list.get(myHolder.getAdapterPosition());
                fragment_notifications.setItemData(model2);

            });

            myHolder.binding.tvDelete.setOnClickListener(v -> {
                NotificationDataModel.NotificationModel model2 = list.get(myHolder.getAdapterPosition());
                fragment_notifications.deleteNotification(model2,myHolder.getAdapterPosition());
            });

        }else if (holder instanceof LoadMoreHolder){
            LoadMoreHolder loadMoreHolder = (LoadMoreHolder) holder;
            loadMoreHolder.binding.prgBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(context,R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
            loadMoreHolder.binding.prgBar.setIndeterminate(true);
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyHolder extends RecyclerView.ViewHolder {
        private NotificationRowBinding binding;

        public MyHolder(NotificationRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;


        }

    }

    public static class LoadMoreHolder extends RecyclerView.ViewHolder {
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
