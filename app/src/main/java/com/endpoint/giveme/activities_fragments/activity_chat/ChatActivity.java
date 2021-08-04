package com.endpoint.giveme.activities_fragments.activity_chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.endpoint.giveme.R;
import com.endpoint.giveme.activities_fragments.activity_add_bill.AddBillActivity;
import com.endpoint.giveme.activities_fragments.activity_delegate_add_offer.DelegateAddOfferActivity;
import com.endpoint.giveme.activities_fragments.activity_driver_update_location.DriverUpdateLocationActivity;
import com.endpoint.giveme.activities_fragments.activity_follow_order.FollowOrderActivity;
import com.endpoint.giveme.activities_fragments.activity_image.ImageActivity;
import com.endpoint.giveme.activities_fragments.activity_map_show_location.MapShowLocationActivity;
import com.endpoint.giveme.activities_fragments.activity_resend_order.ResendOrderTextActivity;
import com.endpoint.giveme.activities_fragments.activity_sign_up_delegate.SignUpDelegateActivity;
import com.endpoint.giveme.activities_fragments.activity_splash_loading.SplashLoadingActivity;
import com.endpoint.giveme.adapters.ChatActionAdapter;
import com.endpoint.giveme.adapters.ChatAdapter;
import com.endpoint.giveme.adapters.OffersAdapter;
import com.endpoint.giveme.adapters.RateReasonAdapter;
import com.endpoint.giveme.databinding.ActivityChatBinding;
import com.endpoint.giveme.language.Language;
import com.endpoint.giveme.models.ChatActionModel;
import com.endpoint.giveme.models.DefaultSettings;
import com.endpoint.giveme.models.FromToLocationModel;
import com.endpoint.giveme.models.MessageDataModel;
import com.endpoint.giveme.models.MessageModel;
import com.endpoint.giveme.models.NotFireModel;
import com.endpoint.giveme.models.OffersDataModel;
import com.endpoint.giveme.models.OffersModel;
import com.endpoint.giveme.models.OrderModel;
import com.endpoint.giveme.models.RateModel;
import com.endpoint.giveme.models.RateReason;
import com.endpoint.giveme.models.SettingModel;
import com.endpoint.giveme.models.SingleMessageDataModel;
import com.endpoint.giveme.models.SingleOrderDataModel;
import com.endpoint.giveme.models.UserModel;
import com.endpoint.giveme.preferences.Preferences;
import com.endpoint.giveme.remote.Api;
import com.endpoint.giveme.share.Common;
import com.endpoint.giveme.tags.Tags;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.paperdb.Paper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {
    private ActivityChatBinding binding;
    private final int IMG_REQ = 1;
    private final int CAMERA_REQ = 2;
    private final int MIC_REQ = 3;
    private final String READ_PERM = Manifest.permission.READ_EXTERNAL_STORAGE;
    private final String WRITE_PERM = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private final String CAMERA_PERM = Manifest.permission.CAMERA;
    private final String MIC_PERM = Manifest.permission.RECORD_AUDIO;
    private long audio_total_seconds = 0;
    private MediaRecorder recorder;
    private String audio_path = "";
    private Handler handler;
    private Runnable runnable;
    private Preferences preferences;
    private UserModel userModel;
    private int order_id;
    private OrderModel orderModel;
    private boolean isDataChanged = false;
    private int offer_current_page = 1;
    private boolean offer_isLoading = false;
    private List<OffersModel> offersModelList;
    private OffersAdapter offersAdapter;
    private String currency = "";
    private String lang;
    private List<ChatActionModel> actionReasonList;
    private ChatActionAdapter chatActionAdapter;
    private ChatActionModel chatActionModel = null;
    private OffersModel offersModel = null;
    private int reasonType = 0;
    private DefaultSettings defaultSettings;
    private List<MessageModel> messageModelList;
    private ChatAdapter adapter;
    private boolean isNewMessage = false;
    private boolean loadData = true;
    private RateReasonAdapter rateReasonAdapter;
    private RateModel rateModel;
    private SettingModel settingModel;
    private boolean isFromFireBase = false;
    private boolean intentFromFirebase = false;

    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(Language.updateResources(newBase, Paper.book().read("lang", "ar")));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat);
        getDataFromIntent();
        initView();
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        order_id = intent.getIntExtra("order_id", 0);
        intentFromFirebase = intent.getBooleanExtra("fromFirebase", false);
        Log.e("order_id", order_id + "_");
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        new Handler(Looper.myLooper()).postDelayed(() -> {
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.cancel(Tags.not_tag, Tags.not_id);
        }, 200);
        messageModelList = new ArrayList<>();
        actionReasonList = new ArrayList<>();
        Paper.init(this);
        lang = Paper.book().read("lang", "ar");
        offersModelList = new ArrayList<>();
        preferences = Preferences.getInstance();
        userModel = preferences.getUserData(this);
        defaultSettings = preferences.getAppSetting(this);
        if (defaultSettings == null) {
            defaultSettings = new DefaultSettings();
        }
        binding.setLang(lang);
        currency = getString(R.string.sar);
        /*if (userModel != null) {
            currency = userModel.getUser().getCountry().getWord().getCurrency();
        }*/
        binding.recViewOffers.setLayoutManager(new LinearLayoutManager(this));
        offersAdapter = new OffersAdapter(offersModelList, this, currency);
        binding.recViewOffers.setAdapter(offersAdapter);
        binding.recView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    LinearLayoutManager manager = (LinearLayoutManager) binding.recView.getLayoutManager();
                    int last_item_pos = manager.findLastCompletelyVisibleItemPosition();
                    int total_items_count = binding.recView.getAdapter().getItemCount();
                    if (last_item_pos == (total_items_count - 2) && !offer_isLoading) {
                        int page = offer_current_page + 1;
                        loadMoreOffer(page);
                    }
                }
            }
        });
        binding.imageChooser.setOnClickListener(v -> {
            if (binding.expandedLayout.isExpanded()) {
                binding.expandedLayout.collapse(true);

            } else {
                binding.expandedLayout.expand(true);

            }
        });
        binding.btnHide.setOnClickListener(v -> {
            binding.expandedLayout.collapse(true);
        });
        binding.imgGallery.setOnClickListener(v -> {
            checkGalleryPermission();

        });
        binding.imageCamera.setOnClickListener(v -> {
            checkCameraPermission();
        });
        binding.imageRecord.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (isMicReady()) {
                    createMediaRecorder();

                } else {
                    checkMicPermission();
                }
            } else if (event.getAction() == MotionEvent.ACTION_UP) {

                if (isMicReady()) {
                    try {
                        recorder.stop();
                        stopTimer();
                        Log.e("ddd", "fff");
                        sendAttachment(audio_path, "", "voice");
                    } catch (Exception e) {
                        stopTimer();
                        Log.e("error1", e.getMessage() + "___");
                    }

                }

            }

            return true;
        });
        binding.llBack.setOnClickListener(v -> onBackPressed());
        binding.tvReadyDeliverOrder.setOnClickListener(v -> {
            navigateToDriverAddOffer();
        });
        chatActionAdapter = new ChatActionAdapter(actionReasonList, this);
        binding.recViewAction.setLayoutManager(new LinearLayoutManager(this));
        binding.recViewAction.setAdapter(chatActionAdapter);
        binding.btnActionOk.setOnClickListener(v ->
        {
            if (chatActionModel != null) {

                switch (reasonType) {
                    case 1:
                        leaveOrder(chatActionModel);
                        break;
                    case 2:
                        changeDriver(chatActionModel);
                        break;
                    case 3:
                        if (chatActionModel.getAction().equals(getString(R.string.delv_is_high))) {
                            if (offersModel != null) {

                                if (Double.parseDouble(offersModel.getOffer_value()) > Double.parseDouble(offersModel.getMin_offer())) {
                                    clientRefuseOffer(offersModel, "yes");

                                } else {
                                    deleteOrder(chatActionModel);

                                }

                            } else {
                                deleteOrder(chatActionModel);

                            }

                        } else if (chatActionModel.getAction().equals(getString(R.string.no_need_order)) || chatActionModel.getAction().equals(getString(R.string.another_reason))) {
                            deleteOrder(chatActionModel);

                        } else {

                            if (offersModel!=null)
                            {
                                clientRefuseOffer(offersModel, "no");

                            }else {
                                deleteOrder(chatActionModel);

                            }

                        }
                        break;
                }


                closeSheet();
            }
        });
        binding.btnActionCancel.setOnClickListener(v ->
        {
            closeSheet();
        });

        binding.btnDriverCancel.setOnClickListener(v -> {
            driverCancelOffer();
        });
        binding.btnDriverBack.setOnClickListener(v -> {
            driverCancelOffer();
        });
        binding.tvLeaveOrder.setOnClickListener(v -> {
            driverLeaveOrderActions();
            closeDriverActionSheet();
        });
        binding.btnCancel.setOnClickListener(v ->
                //clientCancelOrder()
                deleteOrderActionBeforeDriverAcceptOrderActions(null)
        );
        binding.imageMore.setOnClickListener(v -> {
            if (userModel.getUser().getUser_type().equals("client") || (userModel.getUser().getUser_type().equals("driver") && userModel.getUser().getId() == orderModel.getClient().getId())) {
                //changeDriverActions();

                deleteOrderActions(null);
            } else {
                openDriverActionSheet();
            }

        });
        binding.btnDriverActionCancel.setOnClickListener(v -> closeDriverActionSheet());
        binding.btnDriverAnotherOffer.setOnClickListener(v -> {
            navigateToDriverAddOffer();
        });
        binding.tvShare.setOnClickListener(v -> {
            closeDriverActionSheet();
            share();
        });
        binding.tvEndOrder.setOnClickListener(v -> {
            closeDriverActionSheet();
            endOrder();
        });
        binding.flCall.setOnClickListener(v -> {
            if (userModel.getUser().getUser_type().equals("client") || (userModel.getUser().getUser_type().equals("driver") && userModel.getUser().getId() == orderModel.getClient().getId())) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + orderModel.getDriver().getPhone_code() + orderModel.getDriver().getPhone()));
                startActivity(intent);
            } else {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + orderModel.getClient().getPhone_code() + orderModel.getClient().getPhone()));
                startActivity(intent);

            }
        });
        binding.btnBill.setOnClickListener(v -> {
            String orderStatus = orderModel.getOrder_status();
            switch (orderStatus) {
                case "accept_driver":
                    Intent intent = new Intent(this, AddBillActivity.class);
                    intent.putExtra("data", orderModel);
                    startActivityForResult(intent, 200);
                    break;
                case "bill_attach":
                    changeOrderStatus("order_collected");
                    break;
                case "order_collected":
                    changeOrderStatus("reach_to_client");
                    break;
                case "reach_to_client":
                    closeDriverActionSheet();
                    endOrder();
                    break;
            }

        });
        binding.flMap.setOnClickListener(v -> {
            if (userModel.getUser().getUser_type().equals("client") || (userModel.getUser().getUser_type().equals("driver") && userModel.getUser().getId() == orderModel.getClient().getId())) {
                Intent intent = new Intent(this, FollowOrderActivity.class);
                intent.putExtra("data", orderModel);
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, DriverUpdateLocationActivity.class);
                intent.putExtra("data", orderModel);
                startActivity(intent);

            }
        });
        adapter = new ChatAdapter(messageModelList, this, userModel.getUser().getId());
        binding.recView.setLayoutManager(new LinearLayoutManager(this));
        binding.recView.setAdapter(adapter);
        binding.imageSend.setOnClickListener(v -> {
            String message = binding.edtMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                binding.edtMessage.setText(null);
                sendChatText(message);
            }
        });
        binding.edtRateComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    binding.tvCount.setText(String.format(Locale.ENGLISH, "%s/%s", "0", "150"));
                } else {
                    binding.tvCount.setText(String.format(Locale.ENGLISH, "%s/%s", s.toString().length(), "150"));

                }
            }
        });
        binding.tvNotNow.setOnClickListener(v -> {
            closeRateActionSheet();
        });
        binding.tvNotNow.setPaintFlags(binding.tvNotNow.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        binding.btnRate.setOnClickListener(v -> {
            String comment = binding.edtRateComment.getText().toString();
            rateModel.setComment(comment);
            rate();
        });

        binding.recViewRateReason.setLayoutManager(new GridLayoutManager(this, 2));
        rateReasonAdapter = new RateReasonAdapter(new ArrayList<>(), this, null);
        binding.recViewRateReason.setAdapter(rateReasonAdapter);

        binding.emoji1.setOnClickListener(v -> {
            rate1UI();
        });
        binding.emoji2.setOnClickListener(v -> {
            rate2UI();
        });
        binding.emoji3.setOnClickListener(v -> {
            rate3UI();
        });
        binding.emoji4.setOnClickListener(v -> {
            rate4UI();
        });
        binding.emoji5.setOnClickListener(v -> {
            rate5UI();
        });

        binding.flComplain.setOnClickListener(v -> {
            if (settingModel != null) {
                Intent intent = new Intent(this, SignUpDelegateActivity.class);
                String url = Tags.base_url + settingModel.getSettings().getSubmit_the_complaint();
                intent.putExtra("url", url);
                startActivity(intent);
            } else {
                getSetting();
            }
        });

        binding.btnResend.setOnClickListener(v -> {
            Intent intent = new Intent(this, ResendOrderTextActivity.class);
            intent.putExtra("data", orderModel);
            startActivity(intent);
        });
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        getOrderById(null);

    }

    private void getOrderById(ProgressDialog dialog) {
        Api.getService(Tags.base_url).getSingleOrder(userModel.getUser().getToken(), order_id, userModel.getUser().getId())
                .enqueue(new Callback<SingleOrderDataModel>() {
                    @Override
                    public void onResponse(Call<SingleOrderDataModel> call, Response<SingleOrderDataModel> response) {
                        binding.progBarData.setVisibility(View.GONE);
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                        if (response.isSuccessful()) {
                            orderModel = response.body().getOrder();
                            binding.setModel(orderModel);
                            updateUi(orderModel);

                        } else {
                            if (dialog != null) {
                                dialog.dismiss();
                            }
                            binding.progBarData.setVisibility(View.GONE);

                            try {
                                Log.e("error_code", response.code() + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<SingleOrderDataModel> call, Throwable t) {
                        try {
                            if (dialog != null) {
                                dialog.dismiss();
                            }
                            binding.progBarData.setVisibility(View.GONE);

                            if (t.getMessage() != null) {
                                Log.e("error", t.getMessage() + "__");

                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(ChatActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else if (t.getMessage().toLowerCase().contains("socket") || t.getMessage().toLowerCase().contains("canceled")) {
                                } else {
                                    Toast.makeText(ChatActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                }
                            }


                        } catch (Exception e) {

                        }
                    }
                });
    }

    private void updateUi(OrderModel orderModel) {

        rateClear();

        binding.tvEndOrder.setVisibility(View.GONE);

        if (userModel.getUser().getUser_type().equals("client") || (userModel.getUser().getUser_type().equals("driver") && userModel.getUser().getId() == orderModel.getClient().getId())) {
            if (orderModel.getDriver() != null) {
                preferences.create_chat_user_id(this, String.valueOf(orderModel.getDriver().getId()));

            }

        } else {

            if (orderModel.getClient() != null) {
                preferences.create_chat_user_id(this, String.valueOf(orderModel.getClient().getId()));

            }

            binding.imageMore.setVisibility(View.VISIBLE);
            if (orderModel.getDriver_last_offer() == null) {
                binding.tvReadyDeliverOrder.setVisibility(View.VISIBLE);
                binding.tvMsgLeft.setText(orderModel.getDetails());
                binding.tvMsgLeft.setVisibility(View.VISIBLE);
            } else {
                binding.tvReadyDeliverOrder.setVisibility(View.GONE);
                if (orderModel.getOrder_status().equals("new_order") || orderModel.getOrder_status().equals("pennding") || orderModel.getOrder_status().equals("have_offer")) {
                    binding.tvMsgLeft.setText(orderModel.getDetails());
                    binding.tvMsgLeft.setVisibility(View.VISIBLE);

                } else {
                    binding.flDriverOffers.setVisibility(View.GONE);
                    binding.tvMsgLeft.setVisibility(View.GONE);


                }
                binding.tvDriverOfferPrice.setText(String.format(Locale.ENGLISH, "%s %s %s %s", getString(R.string.your_offer_of), orderModel.getDriver_last_offer().getOffer_value(), currency, getString(R.string.sent_to_the_client_please_wait_until_he_accepts_your_offer_thank_you)));
            }

        }


        String status = orderModel.getOrder_status();
        Log.e("status", status);
        switch (status) {
            case "new_order":
                binding.orderStatus.setBackgroundResource(R.drawable.pending_bg);
                binding.orderStatus.setText(getString(R.string.pending));
                binding.btnResend.setVisibility(View.GONE);
                binding.imageRecord.setVisibility(View.GONE);
                binding.imageChooser.setVisibility(View.GONE);
                binding.imageSend.setVisibility(View.GONE);
                binding.msgContent.setVisibility(View.GONE);
                binding.llBill.setVisibility(View.GONE);

                if (userModel.getUser().getUser_type().equals("client") || (userModel.getUser().getUser_type().equals("driver") && userModel.getUser().getId() == orderModel.getClient().getId())) {
                    binding.tvMsgRight.setText(orderModel.getDetails());
                    binding.tvMsgRight.setVisibility(View.VISIBLE);
                    binding.imageMore.setVisibility(View.VISIBLE);

                    binding.flOffers.setVisibility(View.VISIBLE);
                    binding.llOfferData.setVisibility(View.VISIBLE);
                    binding.llComingOffer.setVisibility(View.GONE);
                } else {
                    binding.imageMore.setVisibility(View.GONE);

                    binding.tvMsgLeft.setText(orderModel.getDetails());
                    binding.tvMsgLeft.setVisibility(View.VISIBLE);
                }


                break;
            case "pennding":
                binding.orderStatus.setBackgroundResource(R.drawable.pending_bg);
                binding.orderStatus.setText(getString(R.string.pending));
                binding.btnResend.setVisibility(View.GONE);
                binding.imageRecord.setVisibility(View.GONE);
                binding.imageChooser.setVisibility(View.GONE);
                binding.imageSend.setVisibility(View.GONE);
                binding.msgContent.setVisibility(View.GONE);
                binding.llBill.setVisibility(View.GONE);

                if (userModel.getUser().getUser_type().equals("client") || (userModel.getUser().getUser_type().equals("driver") && userModel.getUser().getId() == orderModel.getClient().getId())) {
                    binding.tvMsgRight.setText(orderModel.getDetails());
                    binding.tvMsgRight.setVisibility(View.VISIBLE);
                    binding.imageMore.setVisibility(View.VISIBLE);


                } else {
                    binding.tvMsgLeft.setText(orderModel.getDetails());
                    binding.tvMsgLeft.setVisibility(View.VISIBLE);
                    binding.imageMore.setVisibility(View.GONE);
                }


                break;

            case "all_driver_refuse_offer":

                binding.orderStatus.setBackgroundResource(R.drawable.rejected_bg);
                binding.orderStatus.setText(getString(R.string.cancel));
                binding.imageRecord.setVisibility(View.GONE);
                binding.imageChooser.setVisibility(View.GONE);
                binding.imageSend.setVisibility(View.GONE);
                binding.msgContent.setVisibility(View.GONE);
                binding.tvCanceled.setVisibility(View.VISIBLE);
                binding.flCall.setVisibility(View.INVISIBLE);
                binding.tvReadyDeliverOrder.setVisibility(View.GONE);
                binding.llBill.setVisibility(View.GONE);
                binding.btnDriverCancel.setVisibility(View.GONE);
                binding.imageMore.setVisibility(View.GONE);
                closeSheet();

                binding.flMap.setVisibility(View.INVISIBLE);

                if (userModel.getUser().getUser_type().equals("client")) {
                    binding.btnResend.setVisibility(View.VISIBLE);

                } else if (userModel.getUser().getUser_type().equals("driver") && userModel.getUser().getId() == orderModel.getClient().getId()) {
                    binding.btnResend.setVisibility(View.VISIBLE);

                } else {
                    binding.btnResend.setVisibility(View.GONE);

                }
                if (loadData) {
                    if (orderModel.getRoom_id() != null && !orderModel.getRoom_id().isEmpty()) {
                        getChatMessages(orderModel.getRoom_id());

                    }
                }


                binding.flOffers.setVisibility(View.GONE);
                binding.llOfferData.setVisibility(View.GONE);
                binding.llComingOffer.setVisibility(View.GONE);
                offersModelList.clear();
                offersAdapter.notifyDataSetChanged();

                break;

            case "have_offer":
                binding.orderStatus.setBackgroundResource(R.drawable.pending_bg);
                binding.orderStatus.setText(getString(R.string.pending));
                binding.btnResend.setVisibility(View.GONE);
                binding.imageRecord.setVisibility(View.GONE);
                binding.imageChooser.setVisibility(View.GONE);
                binding.imageSend.setVisibility(View.GONE);
                binding.msgContent.setVisibility(View.GONE);
                binding.llBill.setVisibility(View.GONE);

                if (userModel.getUser().getUser_type().equals("client") || (userModel.getUser().getUser_type().equals("driver") && userModel.getUser().getId() == orderModel.getClient().getId())) {
                    binding.tvMsgRight.setText(orderModel.getDetails());
                    binding.tvMsgRight.setVisibility(View.VISIBLE);
                    binding.imageMore.setVisibility(View.VISIBLE);

                    if (Integer.parseInt(orderModel.getOffers_count()) > 0) {
                        binding.flOffers.setVisibility(View.VISIBLE);
                        binding.llOfferData.setVisibility(View.GONE);
                        binding.llComingOffer.setVisibility(View.VISIBLE);
                        if (loadData) {
                            getOffers();

                        }

                    } else {

                        binding.flOffers.setVisibility(View.VISIBLE);
                        binding.llOfferData.setVisibility(View.VISIBLE);
                        binding.llComingOffer.setVisibility(View.GONE);

                    }
                } else {

                    binding.imageMore.setVisibility(View.GONE);

                    if (orderModel.getDriver_last_offer() != null) {

                        if (orderModel.getDriver_last_offer().getStatus().equals("refuse")) {
                            binding.flClientRefuseOffer.setVisibility(View.VISIBLE);
                            binding.flOffers.setVisibility(View.GONE);
                            binding.flDriverOffers.setVisibility(View.GONE);
                        } else if (orderModel.getDriver_last_offer().getStatus().equals("new")) {
                            binding.flDriverOffers.setVisibility(View.VISIBLE);
                            binding.flClientRefuseOffer.setVisibility(View.GONE);
                        }


                    } else {
                        binding.flDriverOffers.setVisibility(View.GONE);
                        binding.tvReadyDeliverOrder.setVisibility(View.VISIBLE);

                    }
                    binding.tvMsgLeft.setText(orderModel.getDetails());
                    binding.tvMsgLeft.setVisibility(View.VISIBLE);
                }


                break;
            case "accept_driver":
                binding.orderStatus.setBackgroundResource(R.drawable.done_bg);
                binding.orderStatus.setText(R.string.picking_order);
                binding.btnResend.setVisibility(View.GONE);
                binding.imageRecord.setVisibility(View.VISIBLE);
                binding.imageChooser.setVisibility(View.VISIBLE);
                binding.imageSend.setVisibility(View.VISIBLE);
                binding.msgContent.setVisibility(View.VISIBLE);
                binding.consUserData.setVisibility(View.VISIBLE);
                binding.flOffers.setVisibility(View.GONE);
                binding.llOfferData.setVisibility(View.GONE);
                binding.llComingOffer.setVisibility(View.GONE);

                if (userModel.getUser().getUser_type().equals("client") || (userModel.getUser().getUser_type().equals("driver") && userModel.getUser().getId() == orderModel.getClient().getId())) {
                    binding.imageMore.setVisibility(View.VISIBLE);
                    binding.llBill.setVisibility(View.GONE);

                } else {
                    binding.imageMore.setVisibility(View.GONE);
                    binding.llBill.setVisibility(View.VISIBLE);


                }

                if (loadData) {
                    updateUserUi();
                    if (orderModel.getRoom_id() != null && !orderModel.getRoom_id().isEmpty()) {
                        getChatMessages(orderModel.getRoom_id());

                    }
                }

                break;

            case "bill_attach":
                binding.orderStatus.setBackgroundResource(R.drawable.rounded_primary_dark);
                binding.orderStatus.setText(getString(R.string.picking_order));
                binding.btnResend.setVisibility(View.GONE);
                binding.imageRecord.setVisibility(View.VISIBLE);
                binding.imageChooser.setVisibility(View.VISIBLE);
                binding.imageSend.setVisibility(View.VISIBLE);
                binding.msgContent.setVisibility(View.VISIBLE);
                binding.consUserData.setVisibility(View.VISIBLE);
                binding.flOffers.setVisibility(View.GONE);
                binding.llOfferData.setVisibility(View.GONE);
                binding.llComingOffer.setVisibility(View.GONE);

                if (userModel.getUser().getUser_type().equals("client") || (userModel.getUser().getUser_type().equals("driver") && userModel.getUser().getId() == orderModel.getClient().getId())) {
                    binding.llBill.setVisibility(View.GONE);

                } else {
                    binding.llBill.setVisibility(View.VISIBLE);


                }


                binding.btnBill.setText(R.string.received);
                binding.tvBillStatus.setText(R.string.click_on_receive);
                binding.imageMore.setVisibility(View.GONE);
                closeSheet();
                if (loadData) {
                    updateUserUi();
                    if (orderModel.getRoom_id() != null && !orderModel.getRoom_id().isEmpty()) {
                        getChatMessages(orderModel.getRoom_id());
                    }
                }

                break;
            case "order_collected":

                binding.orderStatus.setBackgroundResource(R.drawable.done_bg);
                binding.orderStatus.setText(R.string.delivering2);
                binding.btnResend.setVisibility(View.GONE);
                binding.imageRecord.setVisibility(View.VISIBLE);
                binding.imageChooser.setVisibility(View.VISIBLE);
                binding.imageSend.setVisibility(View.VISIBLE);
                binding.msgContent.setVisibility(View.VISIBLE);
                binding.consUserData.setVisibility(View.VISIBLE);
                binding.llBill.setVisibility(View.GONE);
                binding.flOffers.setVisibility(View.GONE);
                binding.llOfferData.setVisibility(View.GONE);
                binding.llComingOffer.setVisibility(View.GONE);
                binding.btnBill.setText(R.string.on_location);
                binding.btnDriverCancel.setVisibility(View.GONE);
                binding.imageMore.setVisibility(View.GONE);


                if (userModel.getUser().getUser_type().equals("client") || (userModel.getUser().getUser_type().equals("driver") && userModel.getUser().getId() == orderModel.getClient().getId())) {
                    binding.llBill.setVisibility(View.GONE);

                } else {
                    binding.llBill.setVisibility(View.VISIBLE);


                }

                closeSheet();

                binding.tvBillStatus.setText(R.string.click_on_reached_location);
                binding.flMap.setVisibility(View.VISIBLE);
                if (loadData) {
                    updateUserUi();
                    if (orderModel.getRoom_id() != null && !orderModel.getRoom_id().isEmpty()) {
                        getChatMessages(orderModel.getRoom_id());

                    }
                }

                break;
            case "reach_to_client":
                binding.orderStatus.setBackgroundResource(R.drawable.done_bg);
                binding.orderStatus.setText(R.string.on_location);
                binding.btnResend.setVisibility(View.GONE);
                binding.imageRecord.setVisibility(View.VISIBLE);
                binding.imageChooser.setVisibility(View.VISIBLE);
                binding.imageSend.setVisibility(View.VISIBLE);
                binding.msgContent.setVisibility(View.VISIBLE);
                binding.consUserData.setVisibility(View.VISIBLE);
                binding.flOffers.setVisibility(View.GONE);
                binding.llOfferData.setVisibility(View.GONE);
                binding.llComingOffer.setVisibility(View.GONE);
                binding.btnDriverCancel.setVisibility(View.GONE);
                binding.imageMore.setVisibility(View.GONE);


                if (userModel.getUser().getUser_type().equals("client") || (userModel.getUser().getUser_type().equals("driver") && userModel.getUser().getId() == orderModel.getClient().getId())) {
                    binding.llBill.setVisibility(View.GONE);

                } else {
                    binding.llBill.setVisibility(View.VISIBLE);


                }

                closeSheet();

                binding.btnBill.setText(getString(R.string.delivered));
                binding.tvBillStatus.setText(R.string.click_on_deliverd);
                binding.tvEndOrder.setVisibility(View.VISIBLE);
                binding.flMap.setVisibility(View.INVISIBLE);

                rateModel = new RateModel();

                if (loadData) {
                    updateUserUi();
                    if (orderModel.getRoom_id() != null && !orderModel.getRoom_id().isEmpty()) {
                        getChatMessages(orderModel.getRoom_id());

                    }
                }

                /*if ((userModel.getUser().getUser_type().equals("driver") && userModel.getUser().getId() != orderModel.getClient().getId())) {
                    binding.flCall.setVisibility(View.VISIBLE);
                }*/

                binding.flCall.setVisibility(View.VISIBLE);


                break;

            case "driver_end_rate":
                binding.orderStatus.setBackgroundResource(R.drawable.done_bg);
                binding.orderStatus.setText(getString(R.string.delivered));
                binding.btnResend.setVisibility(View.GONE);
                binding.imageRecord.setVisibility(View.GONE);
                binding.imageChooser.setVisibility(View.GONE);
                binding.imageSend.setVisibility(View.GONE);
                binding.msgContent.setVisibility(View.GONE);
                binding.llBill.setVisibility(View.GONE);
                binding.btnDriverCancel.setVisibility(View.GONE);
                binding.imageMore.setVisibility(View.GONE);

                closeSheet();

                binding.flCall.setVisibility(View.INVISIBLE);
                binding.flMap.setVisibility(View.INVISIBLE);

                if (loadData) {
                    updateUserUi();
                    if (orderModel.getRoom_id() != null && !orderModel.getRoom_id().isEmpty()) {
                        getChatMessages(orderModel.getRoom_id());

                    }
                }


                /*if (intentFromFirebase) {
                    onBackPressed();
                } else {
                    if (isFromFireBase){
                        setResult(RESULT_OK);
                        finish();
                    }
                }*/
                break;
            case "client_end_and_rate":
                binding.orderStatus.setBackgroundResource(R.drawable.done_bg);
                binding.orderStatus.setText(getString(R.string.delivered));
                binding.imageRecord.setVisibility(View.GONE);
                binding.imageChooser.setVisibility(View.GONE);
                binding.imageSend.setVisibility(View.GONE);
                binding.msgContent.setVisibility(View.GONE);
                binding.flCall.setVisibility(View.INVISIBLE);
                binding.llBill.setVisibility(View.GONE);
                binding.btnDriverCancel.setVisibility(View.GONE);
                binding.imageMore.setVisibility(View.GONE);
                closeSheet();

                binding.flMap.setVisibility(View.INVISIBLE);

                if (userModel.getUser().getUser_type().equals("client")) {
                    binding.btnResend.setVisibility(View.VISIBLE);

                } else if (userModel.getUser().getUser_type().equals("driver") && userModel.getUser().getId() == orderModel.getClient().getId()) {
                    binding.btnResend.setVisibility(View.VISIBLE);

                } else {
                    binding.btnResend.setVisibility(View.GONE);

                }


                if (loadData) {
                    updateUserUi();
                    if (orderModel.getRoom_id() != null && !orderModel.getRoom_id().isEmpty()) {
                        getChatMessages(orderModel.getRoom_id());

                    }
                }

                break;

            case "order_driver_back":
                binding.orderStatus.setBackgroundResource(R.drawable.rejected_bg);
                binding.orderStatus.setText(getString(R.string.cancel));
                binding.imageRecord.setVisibility(View.GONE);
                binding.imageChooser.setVisibility(View.GONE);
                binding.imageSend.setVisibility(View.GONE);
                binding.msgContent.setVisibility(View.GONE);
                binding.tvCanceled.setVisibility(View.VISIBLE);
                binding.tvReadyDeliverOrder.setVisibility(View.GONE);
                binding.flCall.setVisibility(View.INVISIBLE);
                binding.llBill.setVisibility(View.GONE);
                binding.btnDriverCancel.setVisibility(View.GONE);
                binding.imageMore.setVisibility(View.GONE);
                closeSheet();

                binding.flMap.setVisibility(View.INVISIBLE);

                if (userModel.getUser().getUser_type().equals("client")) {
                    binding.btnResend.setVisibility(View.VISIBLE);

                } else if (userModel.getUser().getUser_type().equals("driver") && userModel.getUser().getId() == orderModel.getClient().getId()) {
                    binding.btnResend.setVisibility(View.VISIBLE);

                } else {
                    binding.btnResend.setVisibility(View.GONE);

                }

                if (loadData) {
                    updateUserUi();
                    if (orderModel.getRoom_id() != null && !orderModel.getRoom_id().isEmpty()) {
                        getChatMessages(orderModel.getRoom_id());

                    }
                }

                break;
            case "client_cancel":
                binding.orderStatus.setBackgroundResource(R.drawable.rejected_bg);
                binding.orderStatus.setText(getString(R.string.canceled));
                binding.imageRecord.setVisibility(View.GONE);
                binding.imageChooser.setVisibility(View.GONE);
                binding.imageSend.setVisibility(View.GONE);
                binding.msgContent.setVisibility(View.GONE);
                binding.tvCanceled.setVisibility(View.VISIBLE);
                binding.flCall.setVisibility(View.INVISIBLE);
                binding.tvReadyDeliverOrder.setVisibility(View.GONE);
                binding.llBill.setVisibility(View.GONE);
                binding.btnDriverCancel.setVisibility(View.GONE);
                binding.imageMore.setVisibility(View.GONE);
                closeSheet();

                binding.flMap.setVisibility(View.INVISIBLE);

                if (userModel.getUser().getUser_type().equals("client")) {
                    binding.btnResend.setVisibility(View.VISIBLE);

                } else if (userModel.getUser().getUser_type().equals("driver") && userModel.getUser().getId() == orderModel.getClient().getId()) {
                    binding.btnResend.setVisibility(View.VISIBLE);

                } else {
                    binding.btnResend.setVisibility(View.GONE);

                }

                if (userModel.getUser().getUser_type().equals("client") || (userModel.getUser().getUser_type().equals("driver") && userModel.getUser().getId() == orderModel.getClient().getId())) {
                    binding.tvMsgRight.setText(orderModel.getDetails());
                    binding.tvMsgRight.setVisibility(View.VISIBLE);

                } else {
                    binding.tvMsgLeft.setText(orderModel.getDetails());
                    binding.tvMsgLeft.setVisibility(View.VISIBLE);
                }
                break;
            case "cancel_for_late":

                binding.orderStatus.setBackgroundResource(R.drawable.rejected_bg);
                binding.orderStatus.setText(getString(R.string.cancel));
                binding.imageRecord.setVisibility(View.GONE);
                binding.imageChooser.setVisibility(View.GONE);
                binding.imageSend.setVisibility(View.GONE);
                binding.msgContent.setVisibility(View.GONE);
                binding.tvCanceled.setVisibility(View.VISIBLE);
                binding.flCall.setVisibility(View.INVISIBLE);
                binding.tvReadyDeliverOrder.setVisibility(View.GONE);
                binding.llBill.setVisibility(View.GONE);
                binding.btnDriverCancel.setVisibility(View.GONE);
                binding.imageMore.setVisibility(View.GONE);
                closeSheet();

                binding.flMap.setVisibility(View.INVISIBLE);

                if (userModel.getUser().getUser_type().equals("client")) {
                    binding.btnResend.setVisibility(View.VISIBLE);

                } else if (userModel.getUser().getUser_type().equals("driver") && userModel.getUser().getId() == orderModel.getClient().getId()) {
                    binding.btnResend.setVisibility(View.VISIBLE);

                } else {
                    binding.btnResend.setVisibility(View.GONE);

                }
                if (loadData) {
                    if (orderModel.getRoom_id() != null && !orderModel.getRoom_id().isEmpty()) {
                        getChatMessages(orderModel.getRoom_id());

                    }
                }

                break;


        }


    }

    private void updateUserUi() {
        binding.tvMsgLeft.setVisibility(View.GONE);
        binding.tvMsgRight.setVisibility(View.GONE);


        if (userModel.getUser().getUser_type().equals("client") || (userModel.getUser().getUser_type().equals("driver") && userModel.getUser().getId() == orderModel.getClient().getId())) {
            Picasso.get().load(Uri.parse(Tags.IMAGE_URL + orderModel.getDriver().getLogo())).placeholder(R.drawable.user_avatar).fit().into(binding.userImage);
            binding.tvName.setText(orderModel.getDriver().getName());
            binding.rateBar.setRating(Float.parseFloat(orderModel.getDriver().getRate()));

            /*if (orderModel.getOrder_status().equals("accept_driver")||orderModel.getOrder_status().equals("bill_attach")||orderModel.getOrder_status().equals("order_collected")||orderModel.getOrder_status().equals("reach_to_client")){

            }else {
                binding.flCall.setVisibility(View.GONE);

            }*/

            binding.flCall.setVisibility(View.VISIBLE);

            binding.llBill.setVisibility(View.GONE);
            double offer_value = 0.0;
            if (orderModel.getOrder_offer() != null) {
                offer_value = Double.parseDouble(orderModel.getOrder_offer().getOffer_value()) + Double.parseDouble(orderModel.getOrder_offer().getTax_value());

            }
            binding.tvOfferValue.setText(String.format(Locale.ENGLISH, "%.2f %s", offer_value, currency));
        } else {

            Picasso.get().load(Uri.parse(Tags.IMAGE_URL + orderModel.getClient().getLogo())).placeholder(R.drawable.user_avatar).fit().into(binding.userImage);

            String name = "";


            if (orderModel.getClient().getName().length() == 2) {
                name = orderModel.getClient().getName().substring(0, 1) + "**";
            } else if (orderModel.getClient().getName().length() >= 3) {
                String[] s = orderModel.getClient().getName().split(" ");
                name = s[0].substring(0, 2) + "**" + s[0].substring(s[0].length() - 1);

            } else {
                name = orderModel.getClient().getName();
            }

            binding.tvName.setText(name);
            binding.rateBar.setRating(Float.parseFloat(orderModel.getClient().getRate()));
            //binding.llBill.setVisibility(View.VISIBLE);
            binding.flCall.setVisibility(View.VISIBLE);

            /*if (orderModel.getOrder_status().equals("reach_to_client")){
                binding.flCall.setVisibility(View.VISIBLE);
            }else {
                binding.flCall.setVisibility(View.INVISIBLE);
            }*/
            double offer_value = 0.0;
            if (orderModel.getDriver_last_offer() != null) {
                offer_value = Double.parseDouble(orderModel.getDriver_last_offer().getOffer_value());
            }
            binding.tvOfferValue.setText(String.format(Locale.ENGLISH, "%.2f %s", offer_value, currency));
        }


    }

    private void changeOrderStatus(String status) {
        ProgressDialog dialog = Common.createProgressDialog(this, getString(R.string.wait));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
        Api.getService(Tags.base_url).changeOrderStatus(userModel.getUser().getToken(), orderModel.getDriver().getId(), orderModel.getClient().getId(), order_id, status)
                .enqueue(new Callback<SingleMessageDataModel>() {
                    @Override
                    public void onResponse(Call<SingleMessageDataModel> call, Response<SingleMessageDataModel> response) {
                        dialog.dismiss();
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                isDataChanged = true;
                                orderModel.setOrder_status(status);
                                binding.setModel(orderModel);
                                loadData = false;
                                updateUi(orderModel);

                                messageModelList.add(response.body().getData());
                                adapter.notifyItemChanged(messageModelList.size());
                                binding.recView.postDelayed(() -> binding.recView.smoothScrollToPosition(messageModelList.size() - 1), 200);
                                isNewMessage = true;

                            }
                        } else {
                            dialog.dismiss();
                            try {
                                Log.e("error_code", response.code() + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<SingleMessageDataModel> call, Throwable t) {
                        try {
                            dialog.dismiss();
                            if (t.getMessage() != null) {
                                Log.e("error", t.getMessage() + "__");

                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(ChatActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else if (t.getMessage().toLowerCase().contains("socket") || t.getMessage().toLowerCase().contains("canceled")) {
                                } else {
                                    Toast.makeText(ChatActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                }
                            }


                        } catch (Exception e) {

                        }
                    }
                });
    }

    public void navigateToDriverAddOffer() {

        Intent intent = new Intent(this, DelegateAddOfferActivity.class);
        double pick_up_distance = getDistance(new LatLng(Double.parseDouble(userModel.getUser().getLatitude()), Double.parseDouble(userModel.getUser().getLongitude())), new LatLng(Double.parseDouble(orderModel.getMarket_latitude()), Double.parseDouble(orderModel.getMarket_longitude())));
        double drop_off_distance = getDistance(new LatLng(Double.parseDouble(userModel.getUser().getLatitude()), Double.parseDouble(userModel.getUser().getLongitude())), new LatLng(Double.parseDouble(orderModel.getClient_latitude()), Double.parseDouble(orderModel.getClient_longitude())));

        double from_loc_to_loc_distance = getDistance(new LatLng(Double.parseDouble(orderModel.getClient_latitude()), Double.parseDouble(orderModel.getClient_longitude())), new LatLng(Double.parseDouble(orderModel.getMarket_latitude()), Double.parseDouble(orderModel.getMarket_longitude())));
        FromToLocationModel fromToLocationModel = new FromToLocationModel(Double.parseDouble(orderModel.getMarket_latitude()), Double.parseDouble(orderModel.getMarket_longitude()), orderModel.getMarket_address(), pick_up_distance, Double.parseDouble(orderModel.getClient_latitude()), Double.parseDouble(orderModel.getClient_longitude()), orderModel.getClient_address(), drop_off_distance, from_loc_to_loc_distance, Double.parseDouble(userModel.getUser().getLatitude()), Double.parseDouble(userModel.getUser().getLongitude()));
        intent.putExtra("data", fromToLocationModel);
        intent.putExtra("user_token", userModel.getUser().getToken());
        intent.putExtra("client_id", orderModel.getClient().getId());
        intent.putExtra("order_id", order_id);
        intent.putExtra("driver_id", userModel.getUser().getId());
        startActivityForResult(intent, 100);
    }

    public void driverLeaveOrderActions() {
        reasonType = 1;
        binding.tvActionType.setText(R.string.withdraw_order);
        actionReasonList.clear();
        ChatActionModel chatActionModel1 = new ChatActionModel(getString(R.string.shop_location_remote));
        actionReasonList.add(chatActionModel1);
        ChatActionModel chatActionModel2 = new ChatActionModel(getString(R.string.I_do_not_wish_deliver_order));
        actionReasonList.add(chatActionModel2);
        chatActionAdapter.notifyDataSetChanged();
        openSheet();

    }

    private void changeDriverActions() {
        reasonType = 2;
        /*binding.tvActionType.setText(R.string.change_driver);
        actionReasonList.clear();
        ChatActionModel chatActionModel1 = new ChatActionModel("المندوب غير مناسب");
        actionReasonList.add(chatActionModel1);
        ChatActionModel chatActionModel2 = new ChatActionModel("المندوب طلب التواصل خارج التطبيق");
        actionReasonList.add(chatActionModel2);
        ChatActionModel chatActionModel3 = new ChatActionModel("المندوب لم يقبل الدفع الالكتروني");
        actionReasonList.add(chatActionModel3);
        ChatActionModel chatActionModel4 = new ChatActionModel("سبب آخر");
        actionReasonList.add(chatActionModel4);
        chatActionAdapter.notifyDataSetChanged();*/
        openSheet();
    }

    //after driver accept order
    public void deleteOrderActions(OffersModel offersModel) {

        this.offersModel = offersModel;
        reasonType = 3;
        binding.tvActionType.setText(R.string.delete_order);
        actionReasonList.clear();
        ChatActionModel chatActionModel1 = new ChatActionModel(getString(R.string.the_request_dosnot_answer));
        actionReasonList.add(chatActionModel1);
        ChatActionModel chatActionModel2 = new ChatActionModel(getString(R.string.delegate_requested_communication_outside_application));
        actionReasonList.add(chatActionModel2);
        ChatActionModel chatActionModel3 = new ChatActionModel(getString(R.string.delegate_isnot_serious));
        actionReasonList.add(chatActionModel3);
        ChatActionModel chatActionModel4 = new ChatActionModel(getString(R.string.delegate_requet_cancel));
        actionReasonList.add(chatActionModel4);
        ChatActionModel chatActionModel5 = new ChatActionModel(getString(R.string.delegate_refuse_online_payment));
        actionReasonList.add(chatActionModel5);
        ChatActionModel chatActionModel6 = new ChatActionModel(getString(R.string.change_delegate));
        actionReasonList.add(chatActionModel6);
        ChatActionModel chatActionModel7 = new ChatActionModel(getString(R.string.no_need_order));
        actionReasonList.add(chatActionModel7);
        ChatActionModel chatActionModel8 = new ChatActionModel(getString(R.string.another_reason));
        actionReasonList.add(chatActionModel8);
        chatActionAdapter.notifyDataSetChanged();

        openSheet();
    }

    public void deleteOrderActionBeforeDriverAcceptOrderActions(OffersModel offersModel) {
        this.offersModel = offersModel;
        reasonType = 3;
        binding.tvActionType.setText(R.string.delete_order);
        actionReasonList.clear();
        ChatActionModel chatActionModel1 = new ChatActionModel(getString(R.string.delv_is_high));
        actionReasonList.add(chatActionModel1);
        ChatActionModel chatActionModel2 = new ChatActionModel(getString(R.string.no_need_order));
        actionReasonList.add(chatActionModel2);
        ChatActionModel chatActionModel3 = new ChatActionModel(getString(R.string.another_reason));
        actionReasonList.add(chatActionModel3);
        chatActionAdapter.notifyDataSetChanged();
        openSheet();
    }

    public void setReason(ChatActionModel chatActionModel) {
        this.chatActionModel = chatActionModel;

    }

    private void leaveOrder(ChatActionModel chatActionModel) {
        int driver_id = 0;
        if (orderModel != null && orderModel.getDriver() != null) {
            driver_id = orderModel.getDriver().getId();
        }
        ProgressDialog dialog = Common.createProgressDialog(this, getString(R.string.wait));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
        Api.getService(Tags.base_url).driverLeaveOrder(userModel.getUser().getToken(), orderModel.getClient().getId(), driver_id, order_id, chatActionModel.getAction())
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        dialog.dismiss();
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                if (intentFromFirebase) {
                                    onBackPressed();
                                } else {
                                    setResult(RESULT_OK);
                                    finish();
                                }


                            }
                        } else {
                            dialog.dismiss();
                            try {
                                Log.e("error_code", response.code() + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        try {
                            dialog.dismiss();
                            if (t.getMessage() != null) {
                                Log.e("error", t.getMessage() + "__");

                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(ChatActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else if (t.getMessage().toLowerCase().contains("socket") || t.getMessage().toLowerCase().contains("canceled")) {
                                } else {
                                    Toast.makeText(ChatActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                }
                            }


                        } catch (Exception e) {

                        }
                    }
                });
    }

    private void changeDriver(ChatActionModel chatActionModel) {
        ProgressDialog dialog = Common.createProgressDialog(this, getString(R.string.wait));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
        Api.getService(Tags.base_url).changeDriver(userModel.getUser().getToken(), orderModel.getClient().getId(), order_id, chatActionModel.getAction())
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        dialog.dismiss();
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                if (intentFromFirebase) {
                                    onBackPressed();
                                } else {
                                    setResult(RESULT_OK);
                                    finish();
                                }

                            }
                        } else {
                            dialog.dismiss();
                            try {
                                Log.e("error_code", response.code() + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        try {
                            dialog.dismiss();
                            if (t.getMessage() != null) {
                                Log.e("error", t.getMessage() + "__");

                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(ChatActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else if (t.getMessage().toLowerCase().contains("socket") || t.getMessage().toLowerCase().contains("canceled")) {
                                } else {
                                    Toast.makeText(ChatActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                }
                            }


                        } catch (Exception e) {

                        }
                    }
                });
    }

    private void deleteOrder(ChatActionModel chatActionModel) {
        ProgressDialog dialog = Common.createProgressDialog(this, getString(R.string.wait));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
        Api.getService(Tags.base_url).clientDeleteOrder(userModel.getUser().getToken(), orderModel.getClient().getId(), order_id, chatActionModel.getAction())
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        dialog.dismiss();
                        if (response.isSuccessful()) {
                            if (response.body() != null) {

                                if (intentFromFirebase) {
                                    onBackPressed();
                                } else {
                                    setResult(RESULT_OK);
                                    finish();
                                }


                            }
                        } else {
                            dialog.dismiss();
                            try {
                                Log.e("error_code", response.code() + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        try {
                            dialog.dismiss();
                            if (t.getMessage() != null) {
                                Log.e("error", t.getMessage() + "__");

                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(ChatActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else if (t.getMessage().toLowerCase().contains("socket") || t.getMessage().toLowerCase().contains("canceled")) {
                                } else {
                                    Toast.makeText(ChatActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                }
                            }


                        } catch (Exception e) {

                        }
                    }
                });
    }

    public void clientAcceptOffer(OffersModel offersModel) {

        ProgressDialog dialog = Common.createProgressDialog(this, getString(R.string.wait));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
        Api.getService(Tags.base_url).clientAcceptOffer(userModel.getUser().getToken(), orderModel.getClient().getId(), Integer.parseInt(offersModel.getDriver_id()), order_id, offersModel.getId())
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        dialog.dismiss();
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                isDataChanged = true;
                                getOrderById(dialog);
                            }
                        } else {
                            dialog.dismiss();
                            try {
                                Log.e("error_code", response.code() + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        try {
                            dialog.dismiss();
                            if (t.getMessage() != null) {
                                Log.e("error", t.getMessage() + "__");

                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(ChatActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else if (t.getMessage().toLowerCase().contains("socket") || t.getMessage().toLowerCase().contains("canceled")) {
                                } else {
                                    Toast.makeText(ChatActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                }
                            }


                        } catch (Exception e) {

                        }
                    }
                });

    }

    public void clientRefuseOffer(OffersModel offersModel, String type) {
        int driver_id = 0;
        if (offersModel != null && offersModel.getDriver_id() != null) {
            driver_id = Integer.parseInt(offersModel.getDriver_id());
        } else {
            if (orderModel != null && orderModel.getDriver() != null) {
                driver_id = orderModel.getDriver().getId();
            }
        }
        ProgressDialog dialog = Common.createProgressDialog(this, getString(R.string.wait));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
        Api.getService(Tags.base_url).clientRefuseOffer(userModel.getUser().getToken(), orderModel.getClient().getId(), driver_id, order_id, offersModel.getId(), type)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        dialog.dismiss();
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                isDataChanged = true;
                                getOrderById(dialog);
                            }
                        } else {
                            dialog.dismiss();
                            try {
                                Log.e("error_code", response.code() + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        try {
                            dialog.dismiss();
                            if (t.getMessage() != null) {
                                Log.e("error", t.getMessage() + "__");

                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(ChatActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else if (t.getMessage().toLowerCase().contains("socket") || t.getMessage().toLowerCase().contains("canceled")) {
                                } else {
                                    Toast.makeText(ChatActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                }
                            }


                        } catch (Exception e) {

                        }
                    }
                });

    }

    public void driverCancelOffer() {
        ProgressDialog dialog = Common.createProgressDialog(this, getString(R.string.wait));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
        Api.getService(Tags.base_url).driverCancelOffer(userModel.getUser().getToken(), userModel.getUser().getId(), order_id)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            dialog.dismiss();
                            if (response.body() != null) {
                                if (intentFromFirebase) {
                                    onBackPressed();
                                } else {
                                    isDataChanged = true;
                                    setResult(RESULT_OK);
                                    finish();
                                }

                            }
                        } else {
                            dialog.dismiss();
                            try {
                                Log.e("error_code", response.code() + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        try {
                            dialog.dismiss();
                            if (t.getMessage() != null) {
                                Log.e("error", t.getMessage() + "__");

                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(ChatActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else if (t.getMessage().toLowerCase().contains("socket") || t.getMessage().toLowerCase().contains("canceled")) {
                                } else {
                                    Toast.makeText(ChatActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                }
                            }


                        } catch (Exception e) {

                        }
                    }
                });

    }

    private void share() {
    }

    private void endOrder() {
        openRateSheet();
    }

    private void rate() {

        ProgressDialog dialog = Common.createProgressDialog(this, getString(R.string.wait));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
        Api.getService(Tags.base_url).driverRate(userModel.getUser().getToken(), orderModel.getDriver().getId(), orderModel.getClient().getId(), order_id, rateModel.getRate(), rateModel.getReason(), rateModel.getComment())
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        dialog.dismiss();
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                binding.flRate.setVisibility(View.GONE);
                                onBackPressed();


                            }
                        } else {
                            dialog.dismiss();
                            try {
                                Log.e("error_code", response.code() + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        try {
                            dialog.dismiss();
                            if (t.getMessage() != null) {
                                Log.e("error", t.getMessage() + "__");

                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(ChatActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else if (t.getMessage().toLowerCase().contains("socket") || t.getMessage().toLowerCase().contains("canceled")) {
                                } else {
                                    Toast.makeText(ChatActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                }
                            }


                        } catch (Exception e) {

                        }
                    }
                });


    }

    private void rateClear() {

        Picasso.get().load(Tags.IMAGE_URL + orderModel.getClient().getLogo()).placeholder(R.drawable.user_avatar).into(binding.clientImage);
        binding.emoji1.setImageResource(R.drawable.sad1);
        binding.emoji2.setImageResource(R.drawable.sad1);
        binding.emoji3.setImageResource(R.drawable.sad3);
        binding.emoji4.setImageResource(R.drawable.smile1);
        binding.emoji5.setImageResource(R.drawable.smile3);
        binding.tv1.setTextColor(ContextCompat.getColor(this, R.color.gray8));
        binding.tv2.setTextColor(ContextCompat.getColor(this, R.color.gray8));
        binding.tv3.setTextColor(ContextCompat.getColor(this, R.color.gray8));
        binding.tv4.setTextColor(ContextCompat.getColor(this, R.color.gray8));
        binding.tv5.setTextColor(ContextCompat.getColor(this, R.color.gray8));
        binding.btnRate.setBackgroundResource(R.drawable.small_rounded_gray8);
        binding.btnRate.setText(getString(R.string.rate_first));
        binding.edtRateComment.setText(null);
        rateReasonAdapter.addData(new ArrayList<>());
        rateModel = new RateModel();
    }

    private void rate1UI() {

        binding.emoji1.setImageResource(R.drawable.sad2);
        binding.emoji2.setImageResource(R.drawable.sad1);
        binding.emoji3.setImageResource(R.drawable.sad3);
        binding.emoji4.setImageResource(R.drawable.smile1);
        binding.emoji5.setImageResource(R.drawable.smile3);
        binding.tv1.setTextColor(ContextCompat.getColor(this, R.color.black));
        binding.tv2.setTextColor(ContextCompat.getColor(this, R.color.gray8));
        binding.tv3.setTextColor(ContextCompat.getColor(this, R.color.gray8));
        binding.tv4.setTextColor(ContextCompat.getColor(this, R.color.gray8));
        binding.tv5.setTextColor(ContextCompat.getColor(this, R.color.gray8));
        /*List<RateReason> rateReasonList = new ArrayList<>();
        rateReasonList.add(new RateReason(1,"يعاكس",false));
        rateReasonList.add(new RateReason(2,"غير مهزب",false));
        rateReasonList.add(new RateReason(3,"مدخن",false));
        rateReasonList.add(new RateReason(4,"متأخر",false));
        rateReasonList.add(new RateReason(5,"غير ملتزم بالتعليمات الصحية",false));
        rateReasonAdapter.addData(rateReasonList);*/
        binding.btnRate.setBackgroundResource(R.drawable.small_rounded_primary);
        binding.btnRate.setText(getString(R.string.send));
        rateModel.setRate(1);


    }

    private void rate2UI() {
        Picasso.get().load(Tags.IMAGE_URL + orderModel.getClient().getLogo()).placeholder(R.drawable.user_avatar).into(binding.clientImage);
        binding.emoji1.setImageResource(R.drawable.sad1);
        binding.emoji2.setImageResource(R.drawable.sad2);
        binding.emoji3.setImageResource(R.drawable.sad3);
        binding.emoji4.setImageResource(R.drawable.smile1);
        binding.emoji5.setImageResource(R.drawable.smile3);
        binding.tv1.setTextColor(ContextCompat.getColor(this, R.color.gray8));
        binding.tv2.setTextColor(ContextCompat.getColor(this, R.color.black));
        binding.tv3.setTextColor(ContextCompat.getColor(this, R.color.gray8));
        binding.tv4.setTextColor(ContextCompat.getColor(this, R.color.gray8));
        binding.tv5.setTextColor(ContextCompat.getColor(this, R.color.gray8));
        /*List<RateReason> rateReasonList = new ArrayList<>();
        rateReasonList.add(new RateReason(1,"يعاكس",false));
        rateReasonList.add(new RateReason(2,"غير مهزب",false));
        rateReasonList.add(new RateReason(3,"مدخن",false));
        rateReasonList.add(new RateReason(4,"متأخر",false));
        rateReasonList.add(new RateReason(5,"غير ملتزم بالتعليمات الصحية",false));
        rateReasonAdapter.addData(rateReasonList);*/
        binding.btnRate.setBackgroundResource(R.drawable.small_rounded_primary);
        binding.btnRate.setText(getString(R.string.send));
        rateModel.setRate(2);

    }

    private void rate3UI() {
        binding.emoji1.setImageResource(R.drawable.sad1);
        binding.emoji2.setImageResource(R.drawable.sad1);
        binding.emoji3.setImageResource(R.drawable.sad4);
        binding.emoji4.setImageResource(R.drawable.smile1);
        binding.emoji5.setImageResource(R.drawable.smile3);
        binding.tv1.setTextColor(ContextCompat.getColor(this, R.color.gray8));
        binding.tv2.setTextColor(ContextCompat.getColor(this, R.color.gray8));
        binding.tv3.setTextColor(ContextCompat.getColor(this, R.color.black));
        binding.tv4.setTextColor(ContextCompat.getColor(this, R.color.gray8));
        binding.tv5.setTextColor(ContextCompat.getColor(this, R.color.gray8));
       /* List<RateReason> rateReasonList = new ArrayList<>();
        rateReasonList.add(new RateReason(1,"يعاكس",false));
        rateReasonList.add(new RateReason(2,"غير مهزب",false));
        rateReasonList.add(new RateReason(3,"مدخن",false));
        rateReasonList.add(new RateReason(4,"متأخر",false));
        rateReasonList.add(new RateReason(5,"غير ملتزم بالتعليمات الصحية",false));
        rateReasonAdapter.addData(rateReasonList);*/
        binding.btnRate.setBackgroundResource(R.drawable.small_rounded_primary);
        binding.btnRate.setText(getString(R.string.send));
        rateModel.setRate(3);


    }

    private void rate4UI() {
        binding.emoji1.setImageResource(R.drawable.sad1);
        binding.emoji2.setImageResource(R.drawable.sad1);
        binding.emoji3.setImageResource(R.drawable.sad3);
        binding.emoji4.setImageResource(R.drawable.smile2);
        binding.emoji5.setImageResource(R.drawable.smile3);
        binding.tv1.setTextColor(ContextCompat.getColor(this, R.color.gray8));
        binding.tv2.setTextColor(ContextCompat.getColor(this, R.color.gray8));
        binding.tv3.setTextColor(ContextCompat.getColor(this, R.color.gray8));
        binding.tv4.setTextColor(ContextCompat.getColor(this, R.color.black));
        binding.tv5.setTextColor(ContextCompat.getColor(this, R.color.gray8));
       /* List<RateReason> rateReasonList = new ArrayList<>();
        rateReasonList.add(new RateReason(6,"يضايق",false));
        rateReasonList.add(new RateReason(4,"متأخر",false));
        rateReasonAdapter.addData(rateReasonList);*/
        binding.btnRate.setBackgroundResource(R.drawable.small_rounded_primary);
        binding.btnRate.setText(getString(R.string.send));
        rateModel.setRate(4);


    }

    private void rate5UI() {
        binding.emoji1.setImageResource(R.drawable.sad1);
        binding.emoji2.setImageResource(R.drawable.sad1);
        binding.emoji3.setImageResource(R.drawable.sad3);
        binding.emoji4.setImageResource(R.drawable.smile1);
        binding.emoji5.setImageResource(R.drawable.smile4);
        binding.tv1.setTextColor(ContextCompat.getColor(this, R.color.gray8));
        binding.tv2.setTextColor(ContextCompat.getColor(this, R.color.gray8));
        binding.tv3.setTextColor(ContextCompat.getColor(this, R.color.gray8));
        binding.tv4.setTextColor(ContextCompat.getColor(this, R.color.gray8));
        binding.tv5.setTextColor(ContextCompat.getColor(this, R.color.black));
       /* List<RateReason> rateReasonList = new ArrayList<>();
        rateReasonList.add(new RateReason(7,"خدمة سريعة",false));
        rateReasonList.add(new RateReason(8,"محترم",false));
        rateReasonList.add(new RateReason(9,"إحترافي",false));
        rateReasonList.add(new RateReason(10,"متجاوب",false));
        rateReasonAdapter.addData(rateReasonList);*/
        binding.btnRate.setBackgroundResource(R.drawable.small_rounded_primary);
        binding.btnRate.setText(getString(R.string.send));
        rateModel.setRate(5);


    }

    private void openSheet() {
        binding.flAction.clearAnimation();
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        binding.flAction.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                binding.flAction.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void closeSheet() {

        binding.flAction.clearAnimation();
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        binding.flAction.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                binding.flAction.setVisibility(View.GONE);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void openDriverActionSheet() {
        binding.flDriverAction.clearAnimation();
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        binding.flDriverAction.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                binding.flDriverAction.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void closeDriverActionSheet() {

        binding.flDriverAction.clearAnimation();
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        binding.flDriverAction.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                binding.flDriverAction.setVisibility(View.GONE);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void openRateSheet() {
        rateClear();

        binding.flRate.clearAnimation();
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        binding.flRate.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                binding.flRate.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void closeRateActionSheet() {

        binding.flRate.clearAnimation();
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        binding.flRate.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                binding.flRate.setVisibility(View.GONE);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void getSetting() {
        ProgressDialog dialog = Common.createProgressDialog(this, getString(R.string.wait));
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        Api.getService(Tags.base_url).getSetting(lang)
                .enqueue(new Callback<SettingModel>() {
                    @Override
                    public void onResponse(Call<SettingModel> call, Response<SettingModel> response) {
                        dialog.dismiss();
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                settingModel = response.body();
                                Intent intent = new Intent(ChatActivity.this, SignUpDelegateActivity.class);
                                String url = Tags.base_url + settingModel.getSettings().getSubmit_the_complaint();
                                intent.putExtra("url", url);
                                startActivity(intent);

                            }
                        } else {

                            dialog.dismiss();

                            try {
                                Log.e("error_code", response.code() + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<SettingModel> call, Throwable t) {
                        try {
                            dialog.dismiss();

                            if (t.getMessage() != null) {
                                Log.e("error", t.getMessage() + "__");

                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(ChatActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else if (t.getMessage().toLowerCase().contains("socket") || t.getMessage().toLowerCase().contains("canceled")) {
                                } else {
                                    Toast.makeText(ChatActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                }
                            }


                        } catch (Exception e) {

                        }
                    }
                });
    }

    private void sendAttachment(String file_uri, String message, String attachment_type) {
        binding.expandedLayout.collapse(true);
        Intent intent = new Intent(this, ServiceUploadAttachment.class);
        intent.putExtra("file_uri", file_uri);
        intent.putExtra("user_token", userModel.getUser().getToken());
        intent.putExtra("user_id", userModel.getUser().getId());

        if (userModel.getUser().getUser_type().equals("driver")) {
            intent.putExtra("to_user_id", orderModel.getClient().getId());
        } else {
            intent.putExtra("to_user_id", orderModel.getDriver().getId());
        }
        intent.putExtra("message", message);
        intent.putExtra("room_id", Integer.parseInt(orderModel.getRoom_id()));
        intent.putExtra("attachment_type", attachment_type);
        startService(intent);


    }

    private void sendChatText(String message) {

        String to_user_id = "0";

        if (userModel.getUser().getUser_type().equals("client") || (userModel.getUser().getUser_type().equals("driver") && userModel.getUser().getId() == orderModel.getClient().getId())) {
            to_user_id = String.valueOf(orderModel.getDriver().getId());
        } else {
            to_user_id = String.valueOf(orderModel.getClient().getId());
        }


        Api.getService(Tags.base_url)
                .sendChatMessage(userModel.getUser().getToken(), Integer.parseInt(orderModel.getRoom_id()), userModel.getUser().getId(), Integer.parseInt(to_user_id), "message", message)
                .enqueue(new Callback<SingleMessageDataModel>() {
                    @Override
                    public void onResponse(Call<SingleMessageDataModel> call, Response<SingleMessageDataModel> response) {
                        binding.progBar.setVisibility(View.GONE);
                        if (response.isSuccessful()) {

                            if (response.body() != null && response.body().getData() != null) {
                                isNewMessage = true;
                                MessageModel model = response.body().getData();
                                messageModelList.add(model);
                                adapter.notifyItemInserted(messageModelList.size());
                            }


                        }

                    }

                    @Override
                    public void onFailure(Call<SingleMessageDataModel> call, Throwable t) {
                        try {
                            binding.progBar.setVisibility(View.GONE);
                            if (t.getMessage() != null) {
                                Log.e("Error", t.getMessage());

                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(ChatActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else if (t.getMessage().contains("socket")) {

                                } else {
                                    Toast.makeText(ChatActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (Exception e) {
                        }
                    }
                });
    }

    private void getChatMessages(String room_id) {
        defaultSettings.setRoom_id(Integer.parseInt(room_id));
        preferences.createUpdateAppSetting(this, defaultSettings);

        Api.getService(Tags.base_url)
                .getChatMessages(userModel.getUser().getToken(), room_id, userModel.getUser().getId(), order_id, userModel.getUser().getUser_type(), 1, "on", 40)
                .enqueue(new Callback<MessageDataModel>() {
                    @Override
                    public void onResponse(Call<MessageDataModel> call, Response<MessageDataModel> response) {
                        binding.progBar.setVisibility(View.GONE);
                        if (response.isSuccessful()) {

                            if (response.body() != null && response.body().getData() != null) {

                                if (response.body().getData().size() > 0) {
                                    messageModelList.clear();
                                    messageModelList.addAll(response.body().getData());
                                    adapter.notifyDataSetChanged();
                                    binding.recView.postDelayed(() -> binding.recView.smoothScrollToPosition(messageModelList.size() - 1), 200);

                                }
                            }


                        } else {
                            if (response.code() == 500) {
                                Toast.makeText(ChatActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ChatActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                            }

                            try {
                                Log.e("error code", response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    }

                    @Override
                    public void onFailure(Call<MessageDataModel> call, Throwable t) {
                        try {
                            binding.progBar.setVisibility(View.GONE);
                            if (t.getMessage() != null) {
                                Log.e("Error", t.getMessage());

                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(ChatActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else if (t.getMessage().contains("socket")) {

                                } else {
                                    Toast.makeText(ChatActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (Exception e) {
                        }
                    }
                });
    }

    public void setRateItem(RateReason reason) {
        rateModel.setReason(reason.getId());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAttachmentSuccess(MessageModel messageModel) {
        messageModelList.add(messageModel);
        adapter.notifyItemChanged(messageModelList.size());
        binding.recView.postDelayed(() -> binding.recView.smoothScrollToPosition(messageModelList.size() - 1), 200);
        isNewMessage = true;
        if (Integer.parseInt(messageModel.getFrom_user_id()) == userModel.getUser().getId()) {
            deleteFile();

        }
        getOrderById(null);
    }

    @Subscribe
    public void onOrderUpdated(NotFireModel notFireModel) {
        isFromFireBase = true;
        if (notFireModel.getType().equals("order_other")) {
            onBackPressed();
        } else {
            getOrderById(null);

        }
    }

    private void deleteFile() {
        try {
            if (!audio_path.isEmpty()) {
                File file = new File(audio_path);
                if (file.exists()) {
                    file.delete();
                }
            }
        } catch (Exception e) {
        }

    }


    private void getOffers() {

        Api.getService(Tags.base_url).getClientOffers(userModel.getUser().getToken(), userModel.getUser().getId(), order_id, 1, "on", 10)
                .enqueue(new Callback<OffersDataModel>() {
                    @Override
                    public void onResponse(Call<OffersDataModel> call, Response<OffersDataModel> response) {
                        if (response.isSuccessful()) {
                            offersModelList.clear();
                            if (response.body() != null) {
                                offer_current_page = response.body().getCurrent_page();
                                binding.llOfferData.setVisibility(View.GONE);
                                updateDataDistance(response.body().getData(), false);


                            }
                        } else {
                            try {
                                Log.e("error_code", response.code() + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<OffersDataModel> call, Throwable t) {
                        try {
                            if (t.getMessage() != null) {
                                Log.e("error", t.getMessage() + "__");

                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(ChatActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else if (t.getMessage().toLowerCase().contains("socket") || t.getMessage().toLowerCase().contains("canceled")) {
                                } else {
                                    Toast.makeText(ChatActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                }
                            }


                        } catch (Exception e) {

                        }
                    }
                });

    }

    private void loadMoreOffer(int page) {
        offersModelList.add(null);
        offersAdapter.notifyItemInserted(offersModelList.size() - 1);
        offer_isLoading = true;

        Api.getService(Tags.base_url).getClientOffers(userModel.getUser().getToken(), userModel.getUser().getId(), order_id, page, "on", 10)
                .enqueue(new Callback<OffersDataModel>() {
                    @Override
                    public void onResponse(Call<OffersDataModel> call, Response<OffersDataModel> response) {
                        offer_isLoading = false;
                        if (offersModelList.get(offersModelList.size() - 1) == null) {
                            offersModelList.remove(offersModelList.size() - 1);
                            offersAdapter.notifyItemRemoved(offersModelList.size() - 1);
                        }
                        if (response.isSuccessful()) {
                            if (response.body() != null) {

                                if (response.body().getData().size() > 0) {
                                    offer_current_page = response.body().getCurrent_page();
                                    updateDataDistance(response.body().getData(), true);
                                }


                            }
                        } else {
                            offer_isLoading = false;
                            if (offersModelList.get(offersModelList.size() - 1) == null) {
                                offersModelList.remove(offersModelList.size() - 1);
                                offersAdapter.notifyItemRemoved(offersModelList.size() - 1);
                            }
                            try {
                                Log.e("error_code", response.code() + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<OffersDataModel> call, Throwable t) {
                        offer_isLoading = false;
                        if (offersModelList.get(offersModelList.size() - 1) == null) {
                            offersModelList.remove(offersModelList.size() - 1);
                            offersAdapter.notifyItemRemoved(offersModelList.size() - 1);
                        }
                        try {
                            if (t.getMessage() != null) {
                                Log.e("error", t.getMessage() + "__");

                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(ChatActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else if (t.getMessage().toLowerCase().contains("socket") || t.getMessage().toLowerCase().contains("canceled")) {
                                } else {
                                    Toast.makeText(ChatActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                }
                            }


                        } catch (Exception e) {

                        }
                    }
                });
    }

    private void updateDataDistance(List<OffersModel> data, boolean isLoadMore) {
        LatLng place_location = new LatLng(Double.parseDouble(orderModel.getMarket_latitude()), Double.parseDouble(orderModel.getMarket_longitude()));
        for (int index = 0; index < data.size(); index++) {
            OffersModel offersModel = data.get(index);
            offersModel.setDistance(calculateDistance(place_location, new LatLng(Double.parseDouble(offersModel.getDriver().getLatitude()), Double.parseDouble(offersModel.getDriver().getLongitude()))));
            offersModel.setOrder_time(orderModel.getOrder_time_arrival());
            data.set(index, offersModel);
        }

        if (!isLoadMore) {
            offersModelList.clear();
            offersModelList.addAll(data);
            offersAdapter.notifyDataSetChanged();
        } else {
            int old_pos = offersModelList.size() - 1;
            offersModelList.addAll(data);
            int new_pos = offersModelList.size();
            offersAdapter.notifyItemRangeInserted(old_pos, new_pos);
        }
    }

    private String calculateDistance(LatLng latLng1, LatLng latLng2) {
        return String.format(Locale.ENGLISH, "%s %s", String.format(Locale.ENGLISH, "%.2f", (SphericalUtil.computeDistanceBetween(latLng1, latLng2) / 1000)), getString(R.string.km));

    }

    private double getDistance(LatLng latLng1, LatLng latLng2) {
        return SphericalUtil.computeDistanceBetween(latLng1, latLng2) / 1000;
    }

    private void createMediaRecorder() {

        String audio_name = "AUD" + System.currentTimeMillis() + ".mp3";

        File file = new File(Tags.audio_path);
        boolean isFolderCreate;

        if (!file.exists()) {
            isFolderCreate = file.mkdir();
        } else {
            isFolderCreate = true;
        }


        if (isFolderCreate) {
            startTimer();
            binding.recordTime.setVisibility(View.VISIBLE);
            // createVibration();
            audio_path = file.getAbsolutePath() + "/" + audio_name;
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setAudioChannels(1);
            recorder.setOutputFile(audio_path);
            try {
                recorder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            recorder.start();
        } else {
            Toast.makeText(this, "Unable to create sound file on your device", Toast.LENGTH_SHORT).show();
        }


    }

    private void checkCameraPermission() {
        if (ActivityCompat.checkSelfPermission(this, CAMERA_PERM) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, WRITE_PERM) == PackageManager.PERMISSION_GRANTED) {
            selectImage(CAMERA_REQ);

        } else {

            ActivityCompat.requestPermissions(this, new String[]{CAMERA_PERM, WRITE_PERM}, CAMERA_REQ);

        }

    }

    private void checkGalleryPermission() {
        if (ActivityCompat.checkSelfPermission(this, READ_PERM) == PackageManager.PERMISSION_GRANTED) {
            selectImage(IMG_REQ);

        } else {
            ActivityCompat.requestPermissions(this, new String[]{READ_PERM}, IMG_REQ);

        }

    }

    private void checkMicPermission() {
        if (ActivityCompat.checkSelfPermission(this, MIC_PERM) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, WRITE_PERM) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{MIC_PERM, WRITE_PERM}, MIC_REQ);

        }

    }

    private boolean isMicReady() {

        if (ActivityCompat.checkSelfPermission(this, MIC_PERM) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, WRITE_PERM) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        return false;

    }

    private void selectImage(int req) {

        Intent intent = new Intent();
        if (req == IMG_REQ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                intent.setFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

            } else {
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);


            }
            intent.setType("image/*");


        } else if (req == CAMERA_REQ) {

            intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

        }

        startActivityForResult(intent, req);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == IMG_REQ) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage(requestCode);
            } else {
                Toast.makeText(this, "Access image denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == CAMERA_REQ) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                selectImage(requestCode);
            } else {
                Toast.makeText(this, "Access camera denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == MIC_REQ) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(this, "Access voice denied", Toast.LENGTH_SHORT).show();
            }
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMG_REQ && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            sendAttachment(uri.toString(), "", "image");

        } else if (requestCode == CAMERA_REQ && resultCode == RESULT_OK && data != null) {

            Bitmap bitmap = (Bitmap) data.getExtras().get("data");

            Uri uri = getUriFromBitmap(bitmap);
            sendAttachment(uri.toString(), "", "image");

        } else if (requestCode == 100 && resultCode == RESULT_OK) {
            binding.tvReadyDeliverOrder.setVisibility(View.GONE);
            getOrderById(null);
        } else if (requestCode == 200 && resultCode == RESULT_OK && data != null) {
            List<MessageModel> list = (List<MessageModel>) data.getSerializableExtra("data");
            int oldPos = messageModelList.size() - 1;
            messageModelList.addAll(list);
            int newPos = messageModelList.size();
            adapter.notifyItemRangeChanged(oldPos, newPos);
            binding.recView.postDelayed(() -> binding.recView.smoothScrollToPosition(messageModelList.size() - 1), 200);
            orderModel.setOrder_status("bill_attach");
            binding.setModel(orderModel);
            loadData = false;
            updateUi(orderModel);
            isDataChanged = true;


        }

    }

    private Uri getUriFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream);
        return Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "", ""));

    }

    private void startTimer() {
        handler = new Handler();
        runnable = () -> {
            audio_total_seconds += 1;
            binding.recordTime.setText(getRecordTimeFormat(audio_total_seconds));
            startTimer();
        };

        handler.postDelayed(runnable, 1000);
    }

    private void stopTimer() {
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
        audio_total_seconds = 0;
        if (runnable != null) {
            handler.removeCallbacks(runnable);

        }
        handler = null;
        binding.recordTime.setText("00:00:00");
        binding.recordTime.setVisibility(View.GONE);
    }

    private String getRecordTimeFormat(long seconds) {
        int hours = (int) (seconds / 3600);
        int minutes = (int) ((seconds % 3600) / 60);
        int second = (int) (seconds % 60);
        return String.format(Locale.ENGLISH, "%02d:%02d:%02d", hours, minutes, second);

    }

    @Override
    public void onBackPressed() {
        if (binding.flAction.getVisibility() == View.VISIBLE) {
            closeSheet();
        } else if (binding.flDriverAction.getVisibility() == View.VISIBLE) {
            closeDriverActionSheet();
        } else if (binding.flRate.getVisibility() == View.VISIBLE) {
            closeRateActionSheet();
        } else if (intentFromFirebase) {
            Intent intent = new Intent(this, SplashLoadingActivity.class);
            startActivity(intent);
            finish();
        } else {
            setResult(RESULT_OK);
            finish();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        preferences.create_chat_user_id(this, "");
    }

    public void setLocationItem(MessageModel model) {
        String address = "";
        double lat = 0.0;
        double lng = 0.0;
        if (model.getType().equals("from_location")) {
            address = orderModel.getClient_address();
            lat = Double.parseDouble(orderModel.getClient_latitude());
            lng = Double.parseDouble(orderModel.getClient_longitude());
        } else {
            address = orderModel.getMarket_address();
            lat = Double.parseDouble(orderModel.getMarket_latitude());
            lng = Double.parseDouble(orderModel.getMarket_longitude());
        }

        Intent intent = new Intent(this, MapShowLocationActivity.class);
        intent.putExtra("address", address);
        intent.putExtra("lat", lat);
        intent.putExtra("lng", lng);
        startActivity(intent);
    }

    public void openImage(String url, RoundedImageView image) {
        Intent intent = new Intent(this, ImageActivity.class);
        intent.putExtra("title", getString(R.string.back));
        intent.putExtra("url", url);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(this, image, image.getTransitionName());
            startActivity(intent, optionsCompat.toBundle());

        } else {
            startActivity(intent);

        }
    }


}