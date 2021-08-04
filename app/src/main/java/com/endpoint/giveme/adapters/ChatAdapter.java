package com.endpoint.giveme.adapters;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.endpoint.giveme.R;
import com.endpoint.giveme.activities_fragments.activity_chat.ChatActivity;
import com.endpoint.giveme.databinding.ChatImageLeftRowBinding;
import com.endpoint.giveme.databinding.ChatImageRightRowBinding;
import com.endpoint.giveme.databinding.ChatLocationLeftBinding;
import com.endpoint.giveme.databinding.ChatLocationRightBinding;
import com.endpoint.giveme.databinding.ChatMessageAudioLeftRowBinding;
import com.endpoint.giveme.databinding.ChatMessageAudioRightRowBinding;
import com.endpoint.giveme.databinding.ChatMsgLeftBinding;
import com.endpoint.giveme.databinding.ChatMsgRightBinding;
import com.endpoint.giveme.models.MessageModel;

import com.endpoint.giveme.tags.Tags;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int msg_left = 1;
    private final int msg_right = 2;
    private final int img_left = 3;
    private final int img_right = 4;
    private final int sound_left = 7;
    private final int sound_right = 8;
    private final int location_left = 9;
    private final int location_right = 10;
    private final int load = 11;
    private LayoutInflater inflater;
    private List<MessageModel> list;
    private Context context;
    private int current_user_id;
    private ChatActivity activity;
    private SparseArray<MediaPlayer> mediaPlayerList;
    private int selected_pos = -1;
    private Handler handler;
    private Runnable runnable;


    public ChatAdapter(List<MessageModel> list, Context context, int current_user_id) {
        this.list = list;
        this.context = context;
        this.current_user_id = current_user_id;
        inflater = LayoutInflater.from(context);
        activity = (ChatActivity) context;
        mediaPlayerList = new SparseArray<>();
        handler = new Handler();



    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == msg_left) {
            ChatMsgLeftBinding binding = DataBindingUtil.inflate(inflater, R.layout.chat_msg_left, parent, false);
            return new HolderMsgLeft(binding);
        } else if (viewType == msg_right) {
            ChatMsgRightBinding binding = DataBindingUtil.inflate(inflater, R.layout.chat_msg_right, parent, false);
            return new HolderMsgRight(binding);
        } else if (viewType == img_left) {
            ChatImageLeftRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.chat_image_left_row, parent, false);
            return new HolderImageLeft(binding);
        } else if (viewType == img_right) {
            ChatImageRightRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.chat_image_right_row, parent, false);
            return new HolderImageRight(binding);
        }  else if (viewType == sound_left) {
            ChatMessageAudioLeftRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.chat_message_audio_left_row, parent, false);
            return new SoundLeftHolder(binding);

        } else if (viewType == sound_right){
            ChatMessageAudioRightRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.chat_message_audio_right_row, parent, false);
            return new SoundRightHolder(binding);
        }else if (viewType == location_left){
            ChatLocationLeftBinding binding = DataBindingUtil.inflate(inflater, R.layout.chat_location_left, parent, false);
            return new LocationLeftHolder(binding);
        }else {
            ChatLocationRightBinding binding = DataBindingUtil.inflate(inflater, R.layout.chat_location_right, parent, false);
            return new LocationRightHolder(binding);
        }




    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        MessageModel model = list.get(position);

        if (!model.isLoaded()){

            if (holder instanceof SoundLeftHolder){

                SoundLeftHolder soundLeftHolder = (SoundLeftHolder) holder;
                MessageModel model2 = list.get(soundLeftHolder.getAdapterPosition());

                createMediaPlayer(model2,soundLeftHolder.getAdapterPosition());



            }else if (holder instanceof SoundRightHolder){
                SoundRightHolder soundRightHolder = (SoundRightHolder) holder;

                MessageModel model2 = list.get(soundRightHolder.getAdapterPosition());

                createMediaPlayer(model2,soundRightHolder.getAdapterPosition());

            }


        }else {

            if (holder instanceof SoundLeftHolder){

                SoundLeftHolder soundLeftHolder = (SoundLeftHolder) holder;
                soundLeftHolder.binding.imagePlay.setVisibility(View.VISIBLE);
                soundLeftHolder.binding.progBar.setVisibility(View.GONE);

                MediaPlayer mediaPlayer = mediaPlayerList.get(soundLeftHolder.getAdapterPosition());

                if (selected_pos==soundLeftHolder.getAdapterPosition()){

                    if (mediaPlayer.isPlaying()){

                        soundLeftHolder.binding.imagePlay.setImageResource(R.drawable.ic_pause);

                    }else {

                        soundLeftHolder.binding.imagePlay.setImageResource(R.drawable.ic_play);

                    }
                }else {
                    if (mediaPlayer.isPlaying()){
                        mediaPlayer.pause();
                        soundLeftHolder.binding.imagePlay.setImageResource(R.drawable.ic_play);

                    }
                }

            }else if (holder instanceof SoundRightHolder){
                SoundRightHolder soundRightHolder = (SoundRightHolder) holder;
                soundRightHolder.binding.imagePlay.setVisibility(View.VISIBLE);
                soundRightHolder.binding.progBar.setVisibility(View.GONE);
                MediaPlayer mediaPlayer = mediaPlayerList.get(soundRightHolder.getAdapterPosition());

                if (selected_pos==soundRightHolder.getAdapterPosition()){

                    if (mediaPlayer.isPlaying()){

                        soundRightHolder.binding.imagePlay.setImageResource(R.drawable.ic_pause);

                    }else {

                        soundRightHolder.binding.imagePlay.setImageResource(R.drawable.ic_play);

                    }
                }else {
                    if (mediaPlayer.isPlaying()){
                        mediaPlayer.pause();
                        soundRightHolder.binding.imagePlay.setImageResource(R.drawable.ic_play);

                    }
                }




            }
        }




        ///////////////////



        if (holder instanceof HolderMsgLeft) {
            HolderMsgLeft holderMsgLeft = (HolderMsgLeft) holder;
            holderMsgLeft.binding.setModel(model);
            holderMsgLeft.binding.tvMessageDate.setText(getTime(Long.parseLong(model.getDate())*1000));

        } else if (holder instanceof HolderMsgRight) {
            HolderMsgRight holderMsgRight = (HolderMsgRight) holder;
            holderMsgRight.binding.setModel(model);
            holderMsgRight.binding.tvMessageDate.setText(getTime(Long.parseLong(model.getDate())*1000));


        } else if (holder instanceof HolderImageLeft) {
            HolderImageLeft holderImageLeft = (HolderImageLeft) holder;
            holderImageLeft.binding.setModel(model);
            holderImageLeft.binding.tvTime.setText(getTime(Long.parseLong(model.getDate())*1000));
            holderImageLeft.itemView.setOnClickListener(v -> {
                MessageModel messageModel = list.get(holderImageLeft.getAdapterPosition());
                String url = Tags.IMAGE_URL+messageModel.getImage();
                activity.openImage(url,holderImageLeft.binding.image);
            });


        } else if (holder instanceof HolderImageRight) {
            HolderImageRight holderImageRight = (HolderImageRight) holder;
            holderImageRight.binding.setModel(model);
            holderImageRight.binding.tvTime.setText(getTime(Long.parseLong(model.getDate())*1000));

            holderImageRight.itemView.setOnClickListener(v -> {
                MessageModel messageModel = list.get(holderImageRight.getAdapterPosition());
                String url = Tags.IMAGE_URL+messageModel.getImage();
                activity.openImage(url,holderImageRight.binding.image);
            });



        }else if (holder instanceof LocationLeftHolder) {
            LocationLeftHolder locationLeftHolder = (LocationLeftHolder) holder;
            locationLeftHolder.binding.setModel(model);
            locationLeftHolder.binding.tvMessageDate.setText(getTime(Long.parseLong(model.getDate())*1000));
            locationLeftHolder.itemView.setOnClickListener(v -> {
                MessageModel model2 = list.get(locationLeftHolder.getAdapterPosition());
                activity.setLocationItem(model2);

            });



        }else if (holder instanceof LocationRightHolder) {
            LocationRightHolder locationRightHolder = (LocationRightHolder) holder;
            locationRightHolder.binding.setModel(model);
            locationRightHolder.binding.tvMessageDate.setText(getTime(Long.parseLong(model.getDate())*1000));

            locationRightHolder.itemView.setOnClickListener(v -> {
                MessageModel model2 = list.get(locationRightHolder.getAdapterPosition());
                activity.setLocationItem(model2);

            });



        }else if (holder instanceof SoundRightHolder) {

            SoundRightHolder soundRightHolder = (SoundRightHolder) holder;
            MessageModel messageModel = list.get(holder.getAdapterPosition());
            soundRightHolder.BindData(messageModel);
            soundRightHolder.binding.tvMessageDate.setText(getTime(Long.parseLong(model.getDate())*1000));

            soundRightHolder.binding.imagePlay.setOnClickListener(v -> {
                MediaPlayer mediaPlayer = mediaPlayerList.get(soundRightHolder.getAdapterPosition());

                if (mediaPlayer.isPlaying()){
                    pausePlay(mediaPlayer,soundRightHolder.getAdapterPosition());
                }else {
                    playSound(soundRightHolder.getAdapterPosition());

                }
            });



        } else if (holder instanceof SoundLeftHolder) {
            SoundLeftHolder soundLeftHolder = (SoundLeftHolder) holder;
            MessageModel messageModel = list.get(holder.getAdapterPosition());
            soundLeftHolder.BindData(messageModel);
            soundLeftHolder.binding.tvMessageDate.setText(getTime(Long.parseLong(model.getDate())*1000));

            soundLeftHolder.binding.imagePlay.setOnClickListener(v -> {
                MediaPlayer mediaPlayer = mediaPlayerList.get(soundLeftHolder.getAdapterPosition());

                if (mediaPlayer.isPlaying()){
                    pausePlay(mediaPlayer,soundLeftHolder.getAdapterPosition());
                }else {
                    playSound(soundLeftHolder.getAdapterPosition());

                }
            });
        }



    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private String getTime(long time) {
        return new SimpleDateFormat("hh:mm aa", Locale.ENGLISH).format(new Date(time));
    }


    public static class HolderMsgLeft extends RecyclerView.ViewHolder {
        private ChatMsgLeftBinding binding;

        public HolderMsgLeft(@NonNull ChatMsgLeftBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public static  class HolderMsgRight extends RecyclerView.ViewHolder {
        private ChatMsgRightBinding binding;

        public HolderMsgRight(@NonNull ChatMsgRightBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public static class HolderImageLeft extends RecyclerView.ViewHolder {
        private ChatImageLeftRowBinding binding;

        public HolderImageLeft(@NonNull ChatImageLeftRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public static class HolderImageRight extends RecyclerView.ViewHolder {
        private ChatImageRightRowBinding binding;

        public HolderImageRight(@NonNull ChatImageRightRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public static class SoundRightHolder extends RecyclerView.ViewHolder {

        private ChatMessageAudioRightRowBinding binding;


        public SoundRightHolder(ChatMessageAudioRightRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }


        public void BindData(final MessageModel messageModel) {

            binding.progBar2.setMax(messageModel.getMax_duration());
            binding.progBar2.setProgress(messageModel.getProgress());

        }






    }

    public static class SoundLeftHolder extends RecyclerView.ViewHolder {
        private ChatMessageAudioLeftRowBinding binding;


        public SoundLeftHolder(ChatMessageAudioLeftRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }

        public void BindData(final MessageModel messageModel) {

            binding.progBar2.setMax(messageModel.getMax_duration());
            binding.progBar2.setProgress(messageModel.getProgress());



        }









    }


    public static class LocationRightHolder extends RecyclerView.ViewHolder {

        private ChatLocationRightBinding binding;


        public LocationRightHolder(ChatLocationRightBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }









    }

    public static class LocationLeftHolder extends RecyclerView.ViewHolder {
        private ChatLocationLeftBinding binding;


        public LocationLeftHolder(ChatLocationLeftBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }











    }



    private void createMediaPlayer(MessageModel messageModel,int pos) {
        new AudioTask(messageModel,pos).execute(messageModel);


    }

    private void playSound(int pos){
        this.selected_pos = pos;
        MediaPlayer mediaPlayer = mediaPlayerList.get(pos);
        mediaPlayer.start();
        notifyDataSetChanged();
        new ProgressTask(mediaPlayer,pos).execute(list.get(pos));
        mediaPlayer.setOnCompletionListener(mp -> {
            notifyItemChanged(pos);
        });



    }

    private void pausePlay(MediaPlayer mediaPlayer,int pos){
        mediaPlayer.pause();
        notifyItemChanged(pos);

    }

    public void stopPlay() {

        MediaPlayer mediaPlayer =  mediaPlayerList.get(selected_pos);

        if (mediaPlayer!=null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    @Override
    public int getItemViewType(int position) {

        MessageModel messageModel = list.get(position);

        if (messageModel==null){
            return load;
        }else {

            if (messageModel.getType().equals("message")){

                if (Integer.parseInt(messageModel.getFrom_user_id())==current_user_id){

                    return msg_right;
                }else {
                    return msg_left;
                }
            }else if (messageModel.getType().equals("image")){

                if (Integer.parseInt(messageModel.getFrom_user_id())==current_user_id){

                    return img_right;
                }else {
                    return img_left;
                }
            }else if (messageModel.getType().equals("voice")){

                if (Integer.parseInt(messageModel.getFrom_user_id())==current_user_id){

                    return sound_right;
                }else {
                    return sound_left;
                }
            }else if (messageModel.getType().equals("from_location")||messageModel.getType().equals("to_location")){

                if (Integer.parseInt(messageModel.getFrom_user_id())==current_user_id){

                    return location_right;
                }else {
                    return location_left;
                }
            }else {

               return 0;
            }


        }


    }




    private class AudioTask extends AsyncTask<MessageModel,Void,MediaPlayer>{
        private MessageModel messageModel;
        int pos;

        public AudioTask(MessageModel messageModel, int pos) {
            this.messageModel = messageModel;
            this.pos = pos;
        }

        @Override
        protected MediaPlayer doInBackground(MessageModel... messageModels) {
            MediaPlayer mediaPlayer = new MediaPlayer();
            try {

                mediaPlayer.setDataSource(Tags.IMAGE_URL + messageModels[0].getVoice());
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setVolume(100.0f, 100.0f);
                mediaPlayer.setLooping(false);
                mediaPlayer.prepareAsync();
                mediaPlayer.setOnPreparedListener(mp -> onPostExecute(mediaPlayer));





            } catch (IOException e) {
                Log.e("eeeex", e.getMessage());


            }

            return null;
        }

        @Override
        protected void onPostExecute(MediaPlayer mediaPlayer) {
            super.onPostExecute(mediaPlayer);
            if (mediaPlayer!=null){
                mediaPlayerList.put(pos,mediaPlayer);
                messageModel.setLoaded(true);
                messageModel.setMax_duration(mediaPlayer.getDuration());
                list.set(pos,messageModel);
                notifyItemChanged(pos);
            }


        }
    }

    private class ProgressTask extends AsyncTask<MessageModel,Integer,Void>{
        private MediaPlayer mediaPlayer;
        private int pos;

        public ProgressTask(MediaPlayer mediaPlayer, int pos) {
            this.mediaPlayer = mediaPlayer;
            this.pos = pos;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (handler!=null&&runnable!=null){
                handler.removeCallbacks(runnable);
            }

        }

        @Override
        protected Void doInBackground(MessageModel... messageModels) {

            runnable = () -> {
                int progress = mediaPlayer.getCurrentPosition();
                if (mediaPlayer.isPlaying()){
                    publishProgress(progress);
                    doInBackground(messageModels[0]);

                }else {
                    publishProgress(0);

                    handler.removeCallbacks(runnable);
                }

            };
            handler.postDelayed(runnable,100);
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            MessageModel model = list.get(pos);
            model.setProgress(values[0]);
            list.set(pos,model);
            notifyItemChanged(pos);
        }
    }


}
