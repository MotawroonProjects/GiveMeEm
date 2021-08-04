package com.endpoint.giveme.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.endpoint.giveme.R;
import com.endpoint.giveme.activities_fragments.activity_user_feedback.UserFeedbackActivity;
import com.endpoint.giveme.databinding.FeedbackRowBinding;
import com.endpoint.giveme.databinding.LoadMoreRowBinding;
import com.endpoint.giveme.models.FeedbackModel;

import java.util.List;

public class FeedbackAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int DATA = 1;
    private final int LOAD = 2;
    private List<FeedbackModel> list;
    private Context context;
    private LayoutInflater inflater;
    private UserFeedbackActivity activity;
    private String userType;


    public FeedbackAdapter(List<FeedbackModel> list, Context context,String userType) {
        this.list = list;
        this.context = context;
        inflater = LayoutInflater.from(context);
        activity = (UserFeedbackActivity) context;
        this.userType = userType;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType==DATA){
            FeedbackRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.feedback_row, parent, false);
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
            FeedbackModel feedbackModel= list.get(position);
            myHolder.binding.setModel(feedbackModel);
            myHolder.binding.setUserType(userType);
            int rate = Integer.parseInt(feedbackModel.getRate());
            myHolder.binding.rateBar.setRating(rate);
            myHolder.binding.tvRate.setText(String.valueOf((double)rate));

            String name ="";

            Log.e("user_type",userType);
            if (userType.equals("driver")){
                if (feedbackModel.getDriver().getName().length()==2){
                    name = feedbackModel.getClient().getName().substring(0,1)+"**";
                }else if (feedbackModel.getClient().getName().length()>=3){
                    String[] s = feedbackModel.getClient().getName().split(" ");
                    name = s[0].substring(0,2)+"**"+s[0].substring(s[0].length()-1);

                }else {
                    name = feedbackModel.getClient().getName();
                }

            }else {
                name = feedbackModel.getDriver().getName();

            }
            myHolder.binding.tvName.setText(name);

            if (feedbackModel.getComment()!=null&&!feedbackModel.getComment().isEmpty()){
                myHolder.binding.llComment.setVisibility(View.VISIBLE);
                myHolder.binding.tvComment.setText(feedbackModel.getComment());

            }

            if (feedbackModel.getReason()!=null&&!feedbackModel.getReason().isEmpty()){
                myHolder.binding.llReason.setVisibility(View.VISIBLE);
                String reason = feedbackModel.getReason();
                switch (reason){
                    case "1":
                        myHolder.binding.tvReason.setText(context.getString(R.string.flirt));
                        myHolder.binding.tvStatus.setText(R.string.someting_dont_like);
                        break;
                    case "2":
                        myHolder.binding.tvReason.setText(context.getString(R.string.rude));
                        myHolder.binding.tvStatus.setText(R.string.someting_dont_like);
                        break;
                    case "3":
                        myHolder.binding.tvReason.setText(context.getString(R.string.smoked));
                        myHolder.binding.tvStatus.setText(R.string.someting_dont_like);

                        break;
                    case "4":
                        myHolder.binding.tvReason.setText(context.getString(R.string.late));
                        myHolder.binding.tvStatus.setText(R.string.someting_dont_like);

                        break;
                    case "5":
                        myHolder.binding.tvReason.setText(context.getString(R.string.not_adhering));
                        myHolder.binding.tvStatus.setText(R.string.someting_dont_like);

                        break;
                    case "6":
                        myHolder.binding.tvReason.setText(context.getString(R.string.get_angry));
                        myHolder.binding.tvStatus.setText(R.string.someting_dont_like);

                        break;
                    case "7":
                        myHolder.binding.tvReason.setText(context.getString(R.string.fast_service));
                        myHolder.binding.tvStatus.setText(R.string.something_you_like);


                        break;
                    case "8":
                        myHolder.binding.tvReason.setText(context.getString(R.string.respectable));
                        myHolder.binding.tvStatus.setText(R.string.something_you_like);

                        break;
                    case "9":
                        myHolder.binding.tvReason.setText(context.getString(R.string.professional));
                        myHolder.binding.tvStatus.setText(R.string.something_you_like);

                        break;
                    case "10":
                        myHolder.binding.tvReason.setText(context.getString(R.string.responsive));
                        myHolder.binding.tvStatus.setText(R.string.something_you_like);

                        break;
                }
            }

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
        private FeedbackRowBinding binding;

        public MyHolder(FeedbackRowBinding binding) {
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
