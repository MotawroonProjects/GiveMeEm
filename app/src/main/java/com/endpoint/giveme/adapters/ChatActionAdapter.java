package com.endpoint.giveme.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.endpoint.giveme.R;
import com.endpoint.giveme.activities_fragments.activity_chat.ChatActivity;
import com.endpoint.giveme.databinding.ChatActionRowBinding;
import com.endpoint.giveme.models.ChatActionModel;

import java.util.List;

public class ChatActionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ChatActionModel> list;
    private Context context;
    private LayoutInflater inflater;
    private int selected_pos = -1;
    private int previous_pos = -1;
    private ChatActivity activity;

    public ChatActionAdapter(List<ChatActionModel> list, Context context) {
        this.list = list;
        this.context = context;
        inflater = LayoutInflater.from(context);
        activity = (ChatActivity) context;



    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        ChatActionRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.chat_action_row, parent, false);
        return new MyHolder(binding);


    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MyHolder myHolder = (MyHolder) holder;
        ChatActionModel chatActionModel = list.get(position);
        myHolder.binding.setModel(chatActionModel);
        if (chatActionModel.isSelected()){
            myHolder.binding.imageChecked.setVisibility(View.VISIBLE);
        }else {
            myHolder.binding.imageChecked.setVisibility(View.INVISIBLE);

        }
        myHolder.itemView.setOnClickListener(v -> {
            selected_pos = holder.getAdapterPosition();
            if (previous_pos!=-1){
                ChatActionModel chatActionModel2 = list.get(previous_pos);
                chatActionModel2.setSelected(false);
                list.set(previous_pos,chatActionModel2);
                notifyItemChanged(previous_pos);
            }


            ChatActionModel chatActionModel3 = list.get(selected_pos);
            chatActionModel3.setSelected(true);
            list.set(selected_pos,chatActionModel3);
            activity.setReason(chatActionModel3);
            notifyItemChanged(selected_pos);
            previous_pos = selected_pos;

        });



    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyHolder extends RecyclerView.ViewHolder {
        public ChatActionRowBinding binding;

        public MyHolder(@NonNull ChatActionRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }




}
