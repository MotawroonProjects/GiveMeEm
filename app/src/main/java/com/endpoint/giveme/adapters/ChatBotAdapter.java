package com.endpoint.giveme.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.endpoint.giveme.R;
import com.endpoint.giveme.activities_fragments.activity_add_order.AddOrderActivity;
import com.endpoint.giveme.databinding.BotAddCouponRowBinding;
import com.endpoint.giveme.databinding.BotChooseStoreRowBinding;
import com.endpoint.giveme.databinding.BotCouponDetailsRowBinding;
import com.endpoint.giveme.databinding.BotCouponRowBinding;
import com.endpoint.giveme.databinding.BotDropLocationDetailtsRowBinding;
import com.endpoint.giveme.databinding.BotDropLocationPackageDetailtsRowBinding;
import com.endpoint.giveme.databinding.BotDropLocationPackageDetailtsRowBindingImpl;
import com.endpoint.giveme.databinding.BotDropLocationRowBinding;
import com.endpoint.giveme.databinding.BotEmptyRowBinding;
import com.endpoint.giveme.databinding.BotFinishOrderRowBinding;
import com.endpoint.giveme.databinding.BotGreetingRowBinding;
import com.endpoint.giveme.databinding.BotHelpRowBinding;
import com.endpoint.giveme.databinding.BotNeedsRowBinding;
import com.endpoint.giveme.databinding.BotNewOrderRowBinding;
import com.endpoint.giveme.databinding.BotOrderDetailtsRowBinding;
import com.endpoint.giveme.databinding.BotPaymentDetailsRowBinding;
import com.endpoint.giveme.databinding.BotPaymentRowBinding;
import com.endpoint.giveme.databinding.BotPickUpLocationDetailtsRowBinding;
import com.endpoint.giveme.databinding.BotPlaceDetailtsRowBinding;
import com.endpoint.giveme.databinding.BotShareLocationDetailtsRowBinding;
import com.endpoint.giveme.databinding.BotShareLocationRowBinding;
import com.endpoint.giveme.databinding.BotStoreDetailsRowBinding;
import com.endpoint.giveme.databinding.BotTypingRowBinding;
import com.endpoint.giveme.databinding.BotWelcomRowBinding;
import com.endpoint.giveme.models.ChatBotModel;
import com.endpoint.giveme.tags.Tags;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;

import io.paperdb.Paper;

public class ChatBotAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int greeting = 1;
    public static final int welcome = 2;
    public static final int help = 3;
    public static final int new_order = 4;
    public static final int store = 5;
    public static final int store_details = 6;
    public static final int place_details = 7;
    public static final int needs = 8;
    public static final int order_details = 9;
    public static final int drop_off_location = 10;
    public static final int drop_location_details = 11;
    public static final int use_coupon = 12;
    public static final int add_coupon = 13;
    public static final int coupon_details = 14;
    public static final int payment = 15;
    public static final int payment_details = 16;
    public static final int finish_order = 17;
    public static final int typing = 18;
    public static final int empty = 19;
    public static final int share_location = 20;
    public static final int drop_off_location_package_details = 21;
    public static final int share_location_details = 22;
    public static final int pick_up_location_details = 23;

    public Context context;
    public List<ChatBotModel>  list;
    public LayoutInflater inflater;
    public String user_name;
    public String user_image;
    public String time_type;
    private AddOrderActivity activity;
    private String lang;

    public ChatBotAdapter(Context context, List<ChatBotModel> list, String user_name, String user_image,String time_type) {
        this.context = context;
        this.list = list;
        this.user_name = user_name;
        this.user_image = user_image;
        this.time_type = time_type;
        inflater = LayoutInflater.from(context);
        activity = (AddOrderActivity) context;
        Paper.init(context);
        lang = Paper.book().read("lang","ar");

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType==greeting){
            BotGreetingRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.bot_greeting_row,parent,false);
            return new GreetingHolder(binding);
        }else if (viewType==welcome){
            BotWelcomRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.bot_welcom_row,parent,false);
            return new WelcomeHolder(binding);
        }else if (viewType==help){
            BotHelpRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.bot_help_row,parent,false);
            return new HelpHolder(binding);
        }else if (viewType==new_order){
            BotNewOrderRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.bot_new_order_row,parent,false);
            return new NewOrderHolder(binding);

        }else if (viewType==store){
            BotChooseStoreRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.bot_choose_store_row,parent,false);
            return new StoreHolder(binding);
        }else if (viewType==store_details){
            BotStoreDetailsRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.bot_store_details_row,parent,false);
            return new StoreDetailsHolder(binding);
        }else if (viewType==needs){
            BotNeedsRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.bot_needs_row,parent,false);
            return new NeedsHolder(binding);
        }else if (viewType==order_details){
            BotOrderDetailtsRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.bot_order_detailts_row,parent,false);
            return new OrderDetailsHolder(binding);
        }else if (viewType==drop_off_location){
            BotDropLocationRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.bot_drop_location_row,parent,false);
            return new DropOffLocationHolder(binding);
        }else if (viewType==drop_location_details){
            BotDropLocationDetailtsRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.bot_drop_location_detailts_row,parent,false);
            return new DropLocationDetailsHolder(binding);
        }else if (viewType==share_location_details){
            BotShareLocationDetailtsRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.bot_share_location_detailts_row,parent,false);
            return new ShareLocationDetailsHolder(binding);
        }else if (viewType==share_location){
            BotShareLocationRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.bot_share_location_row,parent,false);
            return new ShareLocationHolder(binding);
        }else if (viewType==drop_off_location_package_details){
            BotDropLocationPackageDetailtsRowBindingImpl binding = DataBindingUtil.inflate(inflater, R.layout.bot_drop_location_package_detailts_row,parent,false);
            return new DropLocationPackageDetailsHolder(binding);
        }else if (viewType==pick_up_location_details){
            BotPickUpLocationDetailtsRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.bot_pick_up_location_detailts_row,parent,false);
            return new PickUpLocationDetailsHolder(binding);
        }else if (viewType==use_coupon){
            BotCouponRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.bot_coupon_row,parent,false);
            return new UseCouponHolder(binding);
        }else if (viewType==add_coupon){
            BotAddCouponRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.bot_add_coupon_row,parent,false);
            return new AddCouponHolder(binding);
        }else if (viewType==coupon_details){
            BotCouponDetailsRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.bot_coupon_details_row,parent,false);
            return new CouponDetailsHolder(binding);
        }else if (viewType==payment){
            BotPaymentRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.bot_payment_row,parent,false);
            return new PaymentHolder(binding);
        }else if (viewType==payment_details){
            BotPaymentDetailsRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.bot_payment_details_row,parent,false);
            return new PaymentDetailsHolder(binding);
        }else if (viewType==finish_order){
            BotFinishOrderRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.bot_finish_order_row,parent,false);
            return new FinishHolder(binding);
        }else if (viewType==place_details){
            BotPlaceDetailtsRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.bot_place_detailts_row,parent,false);
            return new PlaceDetailsHolder(binding);
        }else if (viewType==empty){
            BotEmptyRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.bot_empty_row,parent,false);
            return new EmptyHolder(binding);
        }else {
            BotTypingRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.bot_typing_row,parent,false);
            return new TypingHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatBotModel chatBotModel = list.get(position);
        if (holder instanceof GreetingHolder){
            GreetingHolder greetingHolder = (GreetingHolder) holder;
            if (time_type.equals("am")){
                greetingHolder.binding.tvMsg.setText(String.format("%s %s",context.getString(R.string.good_morning)+" ",user_name));
            }else {
                greetingHolder.binding.tvMsg.setText(String.format("%s %s",context.getString(R.string.good_afternoon)+" ",user_name));

            }

        }
        else if (holder instanceof WelcomeHolder){

            WelcomeHolder welcomeHolder = (WelcomeHolder) holder;
            welcomeHolder.binding.tvMsg.setText(String.format("%s%s \n %s",context.getString(R.string.welcome_experience),getEmoji(0x1F60D),context.getString(R.string.we_deliver_anything_city)));

        }
        else if (holder instanceof HelpHolder){
            HelpHolder helpHolder = (HelpHolder) holder;

            if (chatBotModel.isEnabled()){
                helpHolder.binding.tvNewOrder.setAlpha(1f);
            }else {
                helpHolder.binding.tvNewOrder.setAlpha(.5f);

            }

            if (chatBotModel.isEnabled()){
                helpHolder.binding.tvPackage.setAlpha(1f);
            }else {
                helpHolder.binding.tvPackage.setAlpha(.5f);

            }

            if (chatBotModel.isEnabled()){
                helpHolder.binding.tvPreviousOrder.setAlpha(1f);
            }else {
                helpHolder.binding.tvPreviousOrder.setAlpha(.5f);

            }


            helpHolder.binding.tvNewOrder.setOnClickListener(v -> {
                if (chatBotModel.isEnabled()){
                    activity.addOrder_Package(context.getString(R.string.new_order),helpHolder.getAdapterPosition());

                }
            });

            helpHolder.binding.tvPackage.setOnClickListener(v -> {

                if (chatBotModel.isEnabled()){
                    activity.addOrder_Package(context.getString(R.string.package_delivery),helpHolder.getAdapterPosition());

                }

            });


        }
        else if (holder instanceof NewOrderHolder){
            NewOrderHolder newOrderHolder = (NewOrderHolder) holder;
            newOrderHolder.binding.tvNewOrder.setText(chatBotModel.getText());
            if (user_image==null){
                Picasso.get().load(R.drawable.image_avatar).fit().placeholder(R.drawable.image_avatar).into(newOrderHolder.binding.image);

            }else {
                Picasso.get().load(Uri.parse(Tags.IMAGE_URL+user_image)).fit().placeholder(R.drawable.image_avatar).into(newOrderHolder.binding.image);

            }

        }
        else if (holder instanceof StoreHolder){
            StoreHolder storeHolder = (StoreHolder) holder;


            if (chatBotModel.isEnabled()){
                storeHolder.binding.tvShopList.setAlpha(1f);
            }else {
                storeHolder.binding.tvShopList.setAlpha(.5f);

            }

            if (chatBotModel.isEnabled()){
                storeHolder.binding.tvLocation.setAlpha(1f);
            }else {
                storeHolder.binding.tvLocation.setAlpha(.5f);

            }



            storeHolder.binding.tvShopList.setOnClickListener(v -> {
                if (chatBotModel.isEnabled()){
                    activity.openShops_Maps(storeHolder.getAdapterPosition(),context.getString(R.string.shop_list));

                }
            });

            storeHolder.binding.tvLocation.setOnClickListener(v -> {
                if (chatBotModel.isEnabled()){

                    activity.openShops_Maps(storeHolder.getAdapterPosition(),context.getString(R.string.location_on_map));

                }
            });

        }
        else if (holder instanceof StoreDetailsHolder){
            StoreDetailsHolder storeDetailsHolder = (StoreDetailsHolder) holder;
            storeDetailsHolder.binding.tvStoreDetails.setText(chatBotModel.getText());
            if (user_image==null){
                Picasso.get().load(R.drawable.image_avatar).fit().placeholder(R.drawable.image_avatar).into(storeDetailsHolder.binding.image);

            }else {
                Picasso.get().load(Uri.parse(Tags.IMAGE_URL+user_image)).fit().placeholder(R.drawable.image_avatar).into(storeDetailsHolder.binding.image);

            }


        }
        else if (holder instanceof PlaceDetailsHolder){
            PlaceDetailsHolder placeDetailsHolder = (PlaceDetailsHolder) holder;
            placeDetailsHolder.binding.tvName.setText(chatBotModel.getText());
            placeDetailsHolder.binding.tvDistance.setText(String.format(Locale.ENGLISH,"%s %s",String.format(Locale.ENGLISH,"%.2f",chatBotModel.getDistance()),context.getString(R.string.km)));
            placeDetailsHolder.binding.setIcon(chatBotModel.getImage_url());
            placeDetailsHolder.binding.tvRate.setText(String.valueOf(chatBotModel.getRate()));

            if (user_image==null){
                Picasso.get().load(R.drawable.image_avatar).fit().placeholder(R.drawable.image_avatar).into(placeDetailsHolder.binding.image);

            }else {
                Picasso.get().load(Uri.parse(Tags.IMAGE_URL+user_image)).fit().placeholder(R.drawable.image_avatar).into(placeDetailsHolder.binding.image);

            }


        }
        else if (holder instanceof NeedsHolder){
            NeedsHolder needsHolder = (NeedsHolder) holder;


            if (chatBotModel.isEnabled()){
                needsHolder.binding.tvNeed.setAlpha(1f);
            }else {
                needsHolder.binding.tvNeed.setAlpha(.5f);

            }

            needsHolder.binding.tvNeed.setOnClickListener(v -> {
                if (chatBotModel.isEnabled()){
                    activity.writeOrderDetails(needsHolder.getAdapterPosition());

                }
            });
        }
        else if (holder instanceof OrderDetailsHolder){
            OrderDetailsHolder orderDetailsHolder = (OrderDetailsHolder) holder;
            orderDetailsHolder.binding.tvDetails.setText(chatBotModel.getText());
            orderDetailsHolder.binding.setLang(lang);
            if (user_image==null){
                Picasso.get().load(R.drawable.image_avatar).fit().placeholder(R.drawable.image_avatar).into(orderDetailsHolder.binding.image);

            }else {
                Picasso.get().load(Uri.parse(Tags.IMAGE_URL+user_image)).fit().placeholder(R.drawable.image_avatar).into(orderDetailsHolder.binding.image);

            }
            orderDetailsHolder.binding.llChange.setOnClickListener(v -> {
                activity.changeOrderDetails(orderDetailsHolder.getAdapterPosition());
            });

        }
        else if (holder instanceof DropOffLocationHolder){
            DropOffLocationHolder dropOffLocationHolder = (DropOffLocationHolder) holder;
            if (chatBotModel.isEnabled()){
                dropOffLocationHolder.binding.tvOpenMap.setAlpha(1f);
            }else {
                dropOffLocationHolder.binding.tvOpenMap.setAlpha(.5f);

            }
            dropOffLocationHolder.binding.tvOpenMap.setOnClickListener(v -> {
                if (chatBotModel.isEnabled()){
                    activity.openDropOffLocationMap(dropOffLocationHolder.getAdapterPosition());
                }
            });
        }
        else if (holder instanceof ShareLocationHolder){
            ShareLocationHolder shareLocationHolder = (ShareLocationHolder) holder;
            if (chatBotModel.isEnabled()){
                shareLocationHolder.binding.tvShareLocation.setAlpha(1f);
            }else {
                shareLocationHolder.binding.tvShareLocation.setAlpha(.5f);

            }
            shareLocationHolder.binding.tvShareLocation.setOnClickListener(v -> {
                if (chatBotModel.isEnabled()){
                    activity.shareLocation(shareLocationHolder.getAdapterPosition());

                }
            });
        }
        else if (holder instanceof DropLocationDetailsHolder){
            DropLocationDetailsHolder dropLocationDetailsHolder = (DropLocationDetailsHolder) holder;
            dropLocationDetailsHolder.binding.tvAddress.setText(chatBotModel.getTo_address());
            if (user_image==null){
                Picasso.get().load(R.drawable.image_avatar).fit().placeholder(R.drawable.image_avatar).into(dropLocationDetailsHolder.binding.image);

            }else {
                Picasso.get().load(Uri.parse(Tags.IMAGE_URL+user_image)).fit().placeholder(R.drawable.image_avatar).into(dropLocationDetailsHolder.binding.image);

            }



        }else if (holder instanceof ShareLocationDetailsHolder){
            ShareLocationDetailsHolder pickUpLocationDetailsHolder = (ShareLocationDetailsHolder) holder;
            pickUpLocationDetailsHolder.binding.tvAddress.setText(chatBotModel.getFrom_address());

            if (user_image==null){
                Picasso.get().load(R.drawable.image_avatar).fit().placeholder(R.drawable.image_avatar).into(pickUpLocationDetailsHolder.binding.image);

            }else {
                Picasso.get().load(Uri.parse(Tags.IMAGE_URL+user_image)).fit().placeholder(R.drawable.image_avatar).into(pickUpLocationDetailsHolder.binding.image);

            }


        }else if (holder instanceof PickUpLocationDetailsHolder){
            PickUpLocationDetailsHolder pickUpLocationDetailsHolder = (PickUpLocationDetailsHolder) holder;
            pickUpLocationDetailsHolder.binding.tvAddress.setText(chatBotModel.getFrom_address());

        }
        else if (holder instanceof DropLocationPackageDetailsHolder){
            DropLocationPackageDetailsHolder dropLocationPackageDetailsHolder = (DropLocationPackageDetailsHolder) holder;
            dropLocationPackageDetailsHolder.binding.tvDistance.setText(String.format(Locale.ENGLISH,"%.2f %s",chatBotModel.getDistance(),context.getString(R.string.km)));
            if (user_image==null){
                Picasso.get().load(R.drawable.image_avatar).fit().placeholder(R.drawable.image_avatar).into(dropLocationPackageDetailsHolder.binding.image);

            }else {
                Picasso.get().load(Uri.parse(Tags.IMAGE_URL+user_image)).fit().placeholder(R.drawable.image_avatar).into(dropLocationPackageDetailsHolder.binding.image);

            }

        }
        else if (holder instanceof UseCouponHolder){
            UseCouponHolder useCouponHolder = (UseCouponHolder) holder;


        }
        else if (holder instanceof AddCouponHolder){
            AddCouponHolder addCouponHolder = (AddCouponHolder) holder;


            if (chatBotModel.isEnabled()){
                addCouponHolder.binding.tvAddCoupon.setAlpha(1f);
            }else {
                addCouponHolder.binding.tvAddCoupon.setAlpha(.5f);

            }

            if (chatBotModel.isEnabled()){
                addCouponHolder.binding.tvNoCoupon.setAlpha(1f);
            }else {
                addCouponHolder.binding.tvNoCoupon.setAlpha(.5f);

            }


            addCouponHolder.binding.cardViewAddCoupon.setOnClickListener(v -> {
                if (chatBotModel.isEnabled()){
                    activity.addCoupon(context.getString(R.string.add_coupon),addCouponHolder.getAdapterPosition());
                }

            });

            addCouponHolder.binding.cardViewNoCoupon.setOnClickListener(v -> {
                if (chatBotModel.isEnabled()){
                    activity.addCoupon(context.getString(R.string.don_t_have_coupon),addCouponHolder.getAdapterPosition());

                }

            });

        }
        else if (holder instanceof CouponDetailsHolder){
            CouponDetailsHolder couponDetailsHolder = (CouponDetailsHolder) holder;
            couponDetailsHolder.binding.tvCoupon.setText(chatBotModel.getText());

            if (user_image==null){
                Picasso.get().load(R.drawable.image_avatar).fit().placeholder(R.drawable.image_avatar).into(couponDetailsHolder.binding.image);

            }else {
                Picasso.get().load(Uri.parse(Tags.IMAGE_URL+user_image)).fit().placeholder(R.drawable.image_avatar).into(couponDetailsHolder.binding.image);

            }
        }
        else if (holder instanceof PaymentHolder){
            PaymentHolder paymentHolder = (PaymentHolder) holder;

            if (chatBotModel.isEnabled()){
                paymentHolder.binding.tvPayment.setAlpha(1f);
            }else {
                paymentHolder.binding.tvPayment.setAlpha(.5f);

            }

            paymentHolder.binding.tvPayment.setOnClickListener(v -> {
                if (chatBotModel.isEnabled()){
                    activity.payment(paymentHolder.getAdapterPosition());
                }
            });
        }
        else if (holder instanceof PaymentDetailsHolder){
            PaymentDetailsHolder paymentDetailsHolder = (PaymentDetailsHolder) holder;
            paymentDetailsHolder.binding.tvCoupon.setText(chatBotModel.getText());

            if (user_image==null){
                Picasso.get().load(R.drawable.image_avatar).fit().placeholder(R.drawable.image_avatar).into(paymentDetailsHolder.binding.image);

            }else {
                Picasso.get().load(Uri.parse(Tags.IMAGE_URL+user_image)).fit().placeholder(R.drawable.image_avatar).into(paymentDetailsHolder.binding.image);

            }
        }
        else if (holder instanceof FinishHolder){
            FinishHolder finishHolder = (FinishHolder) holder;
            finishHolder.binding.tvCancel.setOnClickListener(v -> {
                activity.cancelOrder();
            });
            finishHolder.binding.tvSubmit.setOnClickListener(v -> {
                activity.submitOrder();
            });
        }
        else if (holder instanceof TypingHolder){
            TypingHolder typingHolder = (TypingHolder) holder;
        }
        else if (holder instanceof EmptyHolder){
            EmptyHolder emptyHolder = (EmptyHolder) holder;
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private String getEmoji(int unicode){
        return new String(Character.toChars(unicode));
    }



    private static class GreetingHolder extends RecyclerView.ViewHolder {
        private BotGreetingRowBinding binding;
        public GreetingHolder(@NonNull BotGreetingRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private static class WelcomeHolder extends RecyclerView.ViewHolder {
        private BotWelcomRowBinding binding;
        public WelcomeHolder(@NonNull BotWelcomRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }

    private static class HelpHolder extends RecyclerView.ViewHolder {
        private BotHelpRowBinding binding;
        public HelpHolder(@NonNull BotHelpRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }

    private static class NewOrderHolder extends RecyclerView.ViewHolder {
        private BotNewOrderRowBinding binding;
        public NewOrderHolder(@NonNull BotNewOrderRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }

    private static class StoreHolder extends RecyclerView.ViewHolder {
        private BotChooseStoreRowBinding binding;
        public StoreHolder(@NonNull BotChooseStoreRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }

    private static class StoreDetailsHolder extends RecyclerView.ViewHolder {
        private BotStoreDetailsRowBinding binding;
        public StoreDetailsHolder(@NonNull BotStoreDetailsRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }

    private static class PlaceDetailsHolder extends RecyclerView.ViewHolder {
        private BotPlaceDetailtsRowBinding binding;
        public PlaceDetailsHolder(@NonNull BotPlaceDetailtsRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }

    private static class NeedsHolder extends RecyclerView.ViewHolder {
        private BotNeedsRowBinding binding;
        public NeedsHolder(@NonNull BotNeedsRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }

    private static class OrderDetailsHolder extends RecyclerView.ViewHolder {
        private BotOrderDetailtsRowBinding binding;
        public OrderDetailsHolder(@NonNull BotOrderDetailtsRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }

    private static class DropOffLocationHolder extends RecyclerView.ViewHolder {
        private BotDropLocationRowBinding binding;
        public DropOffLocationHolder(@NonNull BotDropLocationRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }

    private static class ShareLocationHolder extends RecyclerView.ViewHolder {
        private BotShareLocationRowBinding binding;
        public ShareLocationHolder(@NonNull BotShareLocationRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }


    private static class DropLocationDetailsHolder extends RecyclerView.ViewHolder {
        private BotDropLocationDetailtsRowBinding binding;
        public DropLocationDetailsHolder(@NonNull BotDropLocationDetailtsRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }

    private static class PickUpLocationDetailsHolder extends RecyclerView.ViewHolder {
        private BotPickUpLocationDetailtsRowBinding binding;
        public PickUpLocationDetailsHolder(@NonNull BotPickUpLocationDetailtsRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }

    private static class ShareLocationDetailsHolder extends RecyclerView.ViewHolder {
        private BotShareLocationDetailtsRowBinding binding;
        public ShareLocationDetailsHolder(@NonNull BotShareLocationDetailtsRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }

    private static class DropLocationPackageDetailsHolder extends RecyclerView.ViewHolder {
        private BotDropLocationPackageDetailtsRowBinding binding;
        public DropLocationPackageDetailsHolder(@NonNull BotDropLocationPackageDetailtsRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }

    private static class UseCouponHolder extends RecyclerView.ViewHolder {
        private BotCouponRowBinding binding;
        public UseCouponHolder(@NonNull BotCouponRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }

    private static class AddCouponHolder extends RecyclerView.ViewHolder {
        private BotAddCouponRowBinding binding;
        public AddCouponHolder(@NonNull BotAddCouponRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }

    private static class CouponDetailsHolder extends RecyclerView.ViewHolder {
        private BotCouponDetailsRowBinding binding;
        public CouponDetailsHolder(@NonNull BotCouponDetailsRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }

    private static class PaymentHolder extends RecyclerView.ViewHolder {
        private BotPaymentRowBinding binding;
        public PaymentHolder(@NonNull BotPaymentRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }

    private static class PaymentDetailsHolder extends RecyclerView.ViewHolder {
        private BotPaymentDetailsRowBinding binding;
        public PaymentDetailsHolder(@NonNull BotPaymentDetailsRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }

    private static class FinishHolder extends RecyclerView.ViewHolder {
        private BotFinishOrderRowBinding binding;
        public FinishHolder(@NonNull BotFinishOrderRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }

    private static class TypingHolder extends RecyclerView.ViewHolder {
        private BotTypingRowBinding binding;
        public TypingHolder(@NonNull BotTypingRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;


        }
    }

    private static class EmptyHolder extends RecyclerView.ViewHolder {
        private BotEmptyRowBinding binding;
        public EmptyHolder(@NonNull BotEmptyRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;


        }
    }


    @Override
    public int getItemViewType(int position) {
        ChatBotModel chatBotModel = list.get(position);
        if (chatBotModel == null){
            return typing;
        }else {
            return chatBotModel.getType();
        }

    }
}
