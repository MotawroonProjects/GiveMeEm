package com.endpoint.giveme.adapters;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.endpoint.giveme.R;
import com.endpoint.giveme.activities_fragments.activity_add_order_products.AddOrderProductActivity;
import com.endpoint.giveme.activities_fragments.activity_add_order_text.AddOrderTextActivity;
import com.endpoint.giveme.activities_fragments.activity_resend_order.ResendOrderTextActivity;
import com.endpoint.giveme.databinding.AddOrderImagesMoreRowBinding;
import com.endpoint.giveme.databinding.AddOrderImagesRowBinding;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AddOrderImagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int DATA = 1;
    private final int ADDMORE = 2;
    private List<Uri> list;
    private Context context;
    private LayoutInflater inflater;
    private AppCompatActivity activity;

    public AddOrderImagesAdapter(List<Uri> list, Context context) {
        this.list = list;
        this.context = context;
        inflater = LayoutInflater.from(context);
        activity = (AppCompatActivity) context;

    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType==DATA){
            AddOrderImagesRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.add_order_images_row, parent, false);
            return new MyHolder(binding);
        }else {
            AddOrderImagesMoreRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.add_order_images_more_row, parent, false);
            return new AddOrderMoreMoreHolder(binding);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof MyHolder){
            MyHolder myHolder = (MyHolder) holder;
            Uri uri = list.get(position);
            Picasso.get().load(uri).fit().into(myHolder.binding.image);
            myHolder.binding.cardViewDelete.setOnClickListener(v -> {
                if (activity instanceof AddOrderTextActivity){
                    AddOrderTextActivity addOrderTextActivity = (AddOrderTextActivity) activity;
                    addOrderTextActivity.delete(myHolder.getAdapterPosition());

                }else if (activity instanceof AddOrderProductActivity){
                    AddOrderProductActivity addOrderProductActivity = (AddOrderProductActivity) activity;
                    addOrderProductActivity.delete(myHolder.getAdapterPosition());

                }else if (activity instanceof ResendOrderTextActivity){
                    ResendOrderTextActivity resendOrderTextActivity = (ResendOrderTextActivity) activity;
                    resendOrderTextActivity.delete(myHolder.getAdapterPosition());

                }
            });

        }else if (holder instanceof AddOrderMoreMoreHolder){
            AddOrderMoreMoreHolder addOrderMoreMoreHolder = (AddOrderMoreMoreHolder) holder;
            addOrderMoreMoreHolder.itemView.setOnClickListener(v -> {

                if (activity instanceof AddOrderTextActivity){
                    AddOrderTextActivity addOrderTextActivity = (AddOrderTextActivity) activity;
                    addOrderTextActivity.createDialogAlert();

                }else if (activity instanceof AddOrderProductActivity){
                    AddOrderProductActivity addOrderProductActivity = (AddOrderProductActivity) activity;
                    addOrderProductActivity.createDialogAlert();

                }else if (activity instanceof ResendOrderTextActivity){
                    ResendOrderTextActivity resendOrderTextActivity = (ResendOrderTextActivity) activity;
                    resendOrderTextActivity.createDialogAlert();

                }

            });
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyHolder extends RecyclerView.ViewHolder {
        private AddOrderImagesRowBinding binding;

        public MyHolder(AddOrderImagesRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;


        }

    }

    public static class AddOrderMoreMoreHolder extends RecyclerView.ViewHolder {
        private AddOrderImagesMoreRowBinding binding;

        public AddOrderMoreMoreHolder(AddOrderImagesMoreRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;


        }

    }

    @Override
    public int getItemViewType(int position) {
        if (list.get(position)==null){
            Log.e("yy","yy");
            return ADDMORE;
        }else {
            return DATA;
        }
    }
}
