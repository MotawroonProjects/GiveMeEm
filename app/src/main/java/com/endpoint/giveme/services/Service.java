package com.endpoint.giveme.services;

import com.endpoint.giveme.models.BalanceModel;
import com.endpoint.giveme.models.CategoryDataModel;
import com.endpoint.giveme.models.CountryDataModel;
import com.endpoint.giveme.models.CouponDataModel;
import com.endpoint.giveme.models.CustomPlaceDataModel;
import com.endpoint.giveme.models.CustomPlaceDataModel2;
import com.endpoint.giveme.models.FeedbackDataModel;
import com.endpoint.giveme.models.MessageDataModel;
import com.endpoint.giveme.models.NearbyModel;
import com.endpoint.giveme.models.NotificationDataModel;
import com.endpoint.giveme.models.OfferSettingModel;
import com.endpoint.giveme.models.OffersDataModel;
import com.endpoint.giveme.models.OrdersDataModel;
import com.endpoint.giveme.models.PlaceDetailsModel;
import com.endpoint.giveme.models.PlaceDirectionModel;
import com.endpoint.giveme.models.PlaceGeocodeData;
import com.endpoint.giveme.models.PlaceMapDetailsData;
import com.endpoint.giveme.models.RangeOfferModel;
import com.endpoint.giveme.models.SettingModel;
import com.endpoint.giveme.models.ShopDepartmentDataModel;
import com.endpoint.giveme.models.SingleMessageDataModel;
import com.endpoint.giveme.models.SingleOrderDataModel;
import com.endpoint.giveme.models.SliderModel;
import com.endpoint.giveme.models.UnReadCountModel;
import com.endpoint.giveme.models.UserModel;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface Service {

    @GET("place/findplacefromtext/json")
    Call<PlaceMapDetailsData> searchOnMap(@Query(value = "inputtype") String inputtype,
                                          @Query(value = "input") String input,
                                          @Query(value = "fields") String fields,
                                          @Query(value = "language") String language,
                                          @Query(value = "key") String key
    );


    @GET("place/nearbysearch/json")
    Call<NearbyModel> nearbyPlaceRankBy(@Query(value = "location") String location,
                                        @Query(value = "keyword") String keyword,
                                        @Query(value = "rankby") String rankby,
                                        @Query(value = "language") String language,
                                        @Query(value = "pagetoken") String pagetoken,
                                        @Query(value = "key") String key
    );

    @GET("place/nearbysearch/json")
    Call<NearbyModel> nearbyPlaceInDistance(@Query(value = "location") String location,
                                            @Query(value = "keyword") String keyword,
                                            @Query(value = "radius") int radius,
                                            @Query(value = "language") String language,
                                            @Query(value = "pagetoken") String pagetoken,
                                            @Query(value = "key") String key
    );

    @GET("geocode/json")
    Call<PlaceGeocodeData> getGeoData(@Query(value = "latlng") String latlng,
                                      @Query(value = "language") String language,
                                      @Query(value = "key") String key);

    @GET("place/details/json")
    Call<PlaceDetailsModel> getPlaceDetails(@Query(value = "placeid") String placeid,
                                            @Query(value = "fields") String fields,
                                            @Query(value = "language") String language,
                                            @Query(value = "key") String key
    );


    @GET("api/countries")
    Call<CountryDataModel> getCountries(@Query(value = "lang") String lang);

    @GET
    Call<ResponseBody> getFullUrl(@Url String url);


    @FormUrlEncoded
    @POST("api/login")
    Call<UserModel> login(@Field("phone_code") String phone_code,
                          @Field("phone") String phone
    );

    @FormUrlEncoded
    @POST("api/register")
    Call<UserModel> signUpWithoutImage(@Field("name") String name,
                                       @Field("email") String email,
                                       @Field("phone_code") String phone_code,
                                       @Field("phone") String phone,
                                       @Field("gender") String gender,
                                       @Field("date_of_birth") String date_of_birth,
                                       @Field("id_country") String id_country,
                                       @Field("software_type") String software_type

    );

    @Multipart
    @POST("api/register")
    Call<UserModel> signUpWithImage(@Part("name") RequestBody name,
                                    @Part("email") RequestBody email,
                                    @Part("phone_code") RequestBody phone_code,
                                    @Part("phone") RequestBody phone,
                                    @Part("gender") RequestBody gender,
                                    @Part("date_of_birth") RequestBody date_of_birth,
                                    @Part("id_country") RequestBody id_country,
                                    @Part("software_type") RequestBody software_type,
                                    @Part MultipartBody.Part image
    );


    @FormUrlEncoded
    @POST("api/update-firebase")
    Call<ResponseBody> updatePhoneToken(@Header("Authorization") String user_token,
                                        @Field("phone_token") String phone_token,
                                        @Field("user_id") int user_id,
                                        @Field("software_type") String software_type
    );

    @FormUrlEncoded
    @POST("api/logout")
    Call<ResponseBody> logout(@Header("Authorization") String user_token,
                              @Field("phone_token") String firebase_token,
                              @Field("software_type") String software_type


    );

    @GET("api/slider")
    Call<SliderModel> getSlider();

    @GET("api/slider")
    Call<SliderModel> getMarketSlider(@Query("type") String type);

    @GET("api/get-profile")
    Call<UserModel> getUserById(@Header("Authorization") String user_token,
                                @Query(value = "lang") String lang,
                                @Query(value = "user_id") int user_id);


    @FormUrlEncoded
    @POST("api/update-receive-notifications")
    Call<UserModel> updateReceiveNotification(@Header("Authorization") String user_token,
                                              @Field("user_id") int user_id,
                                              @Field("receive_notifications") String receive_notifications


    );


    @GET("api/category")
    Call<CategoryDataModel> getCategory();

    @GET("api/get-place-by-google-id")
    Call<CustomPlaceDataModel> getCustomPlaceByGooglePlaceId(@Query(value = "google_place_id") String google_place_id);

    @GET("api/place-by-category")
    Call<CustomPlaceDataModel2> getCustomShops(@Query(value = "department_id") String department_id,
                                               @Query(value = "page") int page,
                                               @Query(value = "pagination") String pagination,
                                               @Query(value = "limit_per_page") int limit_per_page
    );

    @GET("api/get-place-departments-products")
    Call<ShopDepartmentDataModel> getShopDepartmentProduct(@Query(value = "market_id") String market_id);

    @GET("api/offer-settings")
    Call<OfferSettingModel> getOfferSetting();


    @FormUrlEncoded
    @POST("api/create-order")
    Call<SingleOrderDataModel> sendTextOrder(@Header("Authorization") String user_token,
                                             @Field("user_id") int user_id,
                                             @Field("order_type") String order_type,
                                             @Field("market_id") int market_id,
                                             @Field("google_place_id") String google_place_id,
                                             @Field("bill_cost") String bill_cost,
                                             @Field("client_address") String client_address,
                                             @Field("client_latitude") double client_latitude,
                                             @Field("client_longitude") double client_longitude,
                                             @Field("market_name") String market_name,
                                             @Field("market_address") String market_address,
                                             @Field("market_latitude") double market_latitude,
                                             @Field("market_longitude") double market_longitude,
                                             @Field("order_time_arrival") String order_time_arrival,
                                             @Field("coupon_id") String coupon_id,
                                             @Field("details") String details,
                                             @Field("notes") String notes


    );

    @Multipart
    @POST("api/create-order")
    Call<SingleOrderDataModel> sendTextOrderWithImage(@Header("Authorization") String user_token,
                                                      @Part("user_id") RequestBody user_id,
                                                      @Part("order_type") RequestBody order_type,
                                                      @Part("market_id") RequestBody market_id,
                                                      @Part("google_place_id") RequestBody google_place_id,
                                                      @Part("bill_cost") RequestBody bill_cost,
                                                      @Part("client_address") RequestBody client_address,
                                                      @Part("client_latitude") RequestBody client_latitude,
                                                      @Part("client_longitude") RequestBody client_longitude,
                                                      @Part("market_name") RequestBody market_name,
                                                      @Part("market_address") RequestBody market_address,
                                                      @Part("market_latitude") RequestBody market_latitude,
                                                      @Part("market_longitude") RequestBody market_longitude,
                                                      @Part("order_time_arrival") RequestBody order_time_arrival,
                                                      @Part("coupon_id") RequestBody coupon_id,
                                                      @Part("details") RequestBody details,
                                                      @Part("notes") RequestBody notes,
                                                      @Part List<MultipartBody.Part> images


    );


    @FormUrlEncoded
    @POST("api/update-location")
    Call<ResponseBody> updateLocation(@Header("Authorization") String user_token,
                                      @Field("user_id") int user_id,
                                      @Field("latitude") double latitude,
                                      @Field("longitude") double longitude
    );


    @GET("api/get-client-orders")
    Call<OrdersDataModel> getClientOrder(@Header("Authorization") String user_token,
                                         @Query(value = "user_id") int user_id,
                                         @Query(value = "type") String type,
                                         @Query(value = "page") int page,
                                         @Query(value = "pagination") String pagination,
                                         @Query(value = "limit_per_page") int limit_per_page
    );

    @GET("api/get-one-order")
    Call<SingleOrderDataModel> getSingleOrder(@Header("Authorization") String user_token,
                                              @Query(value = "order_id") int order_id,
                                              @Query(value = "driver_id") int driver_id

    );


    @GET("api/get-count-unread")
    Call<UnReadCountModel> getNotificationCount(@Header("Authorization") String user_token,
                                                @Query(value = "user_id") int user_id
    );

    @FormUrlEncoded
    @POST("api/get-read-notifications")
    Call<ResponseBody> readNotification(@Header("Authorization") String user_token,
                                        @Field("user_id") int user_id
    );

    @GET("api/get-driver-orders")
    Call<OrdersDataModel> getDriverDeliveryOrder(@Header("Authorization") String user_token,
                                                 @Query(value = "user_id") int user_id,
                                                 @Query(value = "page") int page,
                                                 @Query(value = "pagination") String pagination,
                                                 @Query(value = "limit_per_page") int limit_per_page
    );

    @GET("api/show-driver-offers")
    Call<OffersDataModel> getClientOffers(@Header("Authorization") String user_token,
                                          @Query(value = "user_id") int user_id,
                                          @Query(value = "order_id") int order_id,
                                          @Query(value = "page") int page,
                                          @Query(value = "pagination") String pagination,
                                          @Query(value = "limit_per_page") int limit_per_page
    );

    @FormUrlEncoded
    @POST("api/range-offer")
    Call<RangeOfferModel> getOfferRange(@Header("Authorization") String user_token,
                                        @Field("client_id") int client_id,
                                        @Field("driver_id") int driver_id,
                                        @Field("order_id") int order_id,
                                        @Field("distance") double distance

    );

    @GET("api/get-driver-new-orders")
    Call<OrdersDataModel> getDriverComingOrder(@Header("Authorization") String user_token,
                                               @Query(value = "user_id") int user_id,
                                               @Query(value = "page") int page,
                                               @Query(value = "pagination") String pagination,
                                               @Query(value = "limit_per_page") int limit_per_page
    );

    @FormUrlEncoded
    @POST("api/create-offer")
    Call<ResponseBody> sendDriverOffer(@Header("Authorization") String user_token,
                                       @Field("client_id") int client_id,
                                       @Field("driver_id") int driver_id,
                                       @Field("order_id") int order_id,
                                       @Field("offer_value") String offer_value,
                                       @Field("min_offer") String min_offer,
                                       @Field("action") String action


    );

    @FormUrlEncoded
    @POST("api/driver-leave-order")
    Call<ResponseBody> driverLeaveOrder(@Header("Authorization") String user_token,
                                        @Field("client_id") int client_id,
                                        @Field("driver_id") int driver_id,
                                        @Field("order_id") int order_id,
                                        @Field("reason_driver") String reason_driver

    );

    @FormUrlEncoded
    @POST("api/client-accept-offer")
    Call<ResponseBody> clientAcceptOffer(@Header("Authorization") String user_token,
                                         @Field("client_id") int client_id,
                                         @Field("driver_id") int driver_id,
                                         @Field("order_id") int order_id,
                                         @Field("offer_id") int offer_id

    );

    @FormUrlEncoded
    @POST("api/client-cancel-order")
    Call<ResponseBody> clientDeleteOrder(@Header("Authorization") String user_token,
                                         @Field("client_id") int client_id,
                                         @Field("order_id") int order_id,
                                         @Field("reason") String reason

    );

    @FormUrlEncoded
    @POST("api/client-cancel-order")
    Call<ResponseBody> clientCancelOrder(@Header("Authorization") String user_token,
                                         @Field("client_id") int client_id,
                                         @Field("order_id") int order_id

    );

    @FormUrlEncoded
    @POST("api/client-refuse-offer")
    Call<ResponseBody> clientRefuseOffer(@Header("Authorization") String user_token,
                                         @Field("client_id") int client_id,
                                         @Field("driver_id") int driver_id,
                                         @Field("order_id") int order_id,
                                         @Field("offer_id") int offer_id,
                                         @Field("get_new_offer") String get_new_offer

    );


    @FormUrlEncoded
    @POST("api/send-order-to-other-drivers")
    Call<ResponseBody> changeDriver(@Header("Authorization") String user_token,
                                    @Field("client_id") int client_id,
                                    @Field("order_id") int order_id,
                                    @Field("reason") String reason

    );

    @GET("api/get-notifications")
    Call<NotificationDataModel> getNotification(@Header("Authorization") String user_token,
                                                @Query(value = "user_id") int user_id,
                                                @Query(value = "page") int page,
                                                @Query(value = "pagination") String pagination,
                                                @Query(value = "limit_per_page") int limit_per_page
    );


    @FormUrlEncoded
    @POST("api/cancel-offer")
    Call<ResponseBody> driverCancelOffer(@Header("Authorization") String user_token,
                                         @Field("driver_id") int driver_id,
                                         @Field("order_id") int order_id
    );

    @GET("api/sttings")
    Call<SettingModel> getSetting(@Query(value = "lang") String lang);


    @FormUrlEncoded
    @POST("api/update-profile")
    Call<UserModel> updateProfileWithoutImage(@Header("Authorization") String user_token,
                                              @Field("user_id") int user_id,
                                              @Field("name") String name,
                                              @Field("email") String email,
                                              @Field("phone") String phone,
                                              @Field("phone_code") String phone_code,
                                              @Field("gender") String gender,
                                              @Field("date_of_birth") String date_of_birth


    );

    @Multipart
    @POST("api/update-profile")
    Call<UserModel> updateProfileWithImage(@Header("Authorization") String user_token,
                                           @Part("user_id") RequestBody user_id,
                                           @Part("name") RequestBody name,
                                           @Part("email") RequestBody email,
                                           @Part("phone") RequestBody phone,
                                           @Part("phone_code") RequestBody phone_code,
                                           @Part("gender") RequestBody gender,
                                           @Part("date_of_birth") RequestBody date_of_birth,
                                           @Part MultipartBody.Part logo


    );

    @GET("api/get-room-msg")
    Call<MessageDataModel> getChatMessages(@Header("Authorization") String user_token,
                                           @Query(value = "room_id") String room_id,
                                           @Query(value = "user_id") int user_id,
                                           @Query(value = "order_id") int order_id,
                                           @Query(value = "user_type") String user_type,
                                           @Query(value = "page") int page,
                                           @Query(value = "pagination") String pagination,
                                           @Query(value = "limit_per_page") int limit_per_page
    );

    @FormUrlEncoded
    @POST("api/send-chat-msg")
    Call<SingleMessageDataModel> sendChatMessage(@Header("Authorization") String user_token,
                                                 @Field("room_id") int room_id,
                                                 @Field("from_user_id") int from_user_id,
                                                 @Field("to_user_id") int to_user_id,
                                                 @Field("type") String type,
                                                 @Field("message") String message


    );

    @Multipart
    @POST("api/send-chat-msg")
    Call<SingleMessageDataModel> sendChatAttachment(@Header("Authorization") String user_token,
                                                    @Part("room_id") RequestBody room_id,
                                                    @Part("from_user_id") RequestBody from_user_id,
                                                    @Part("to_user_id") RequestBody to_user_id,
                                                    @Part("type") RequestBody type,
                                                    @Part("message") RequestBody message,
                                                    @Part MultipartBody.Part attachment
    );


    @Multipart
    @POST("api/attach-bill")
    Call<MessageDataModel> addBillWithImage(@Header("Authorization") String user_token,
                                            @Part("driver_id") RequestBody driver_id,
                                            @Part("client_id") RequestBody client_id,
                                            @Part("order_id") RequestBody order_id,
                                            @Part("bill_cost") RequestBody bill_cost,
                                            @Part("message") RequestBody message,
                                            @Part MultipartBody.Part attachment
    );

    @Multipart
    @POST("api/attach-bill")
    Call<MessageDataModel> addBillWithoutImage(@Header("Authorization") String user_token,
                                               @Part("driver_id") RequestBody driver_id,
                                               @Part("client_id") RequestBody client_id,
                                               @Part("order_id") RequestBody order_id,
                                               @Part("bill_cost") RequestBody bill_cost,
                                               @Part("message") RequestBody message
    );


    @FormUrlEncoded
    @POST("api/change-order-status")
    Call<SingleMessageDataModel> changeOrderStatus(@Header("Authorization") String user_token,
                                                   @Field("driver_id") int driver_id,
                                                   @Field("client_id") int client_id,
                                                   @Field("order_id") int order_id,
                                                   @Field("order_status") String order_status
    );

    @FormUrlEncoded
    @POST("api/end-order")
    Call<ResponseBody> driverRate(@Header("Authorization") String user_token,
                                  @Field("driver_id") int driver_id,
                                  @Field("client_id") int client_id,
                                  @Field("order_id") int order_id,
                                  @Field("rate") int rate,
                                  @Field("reason") int reason,
                                  @Field("comment") String comment

    );

    @FormUrlEncoded
    @POST("api/client-end-order")
    Call<ResponseBody> clientRate(@Header("Authorization") String user_token,
                                  @Field("driver_id") int driver_id,
                                  @Field("client_id") int client_id,
                                  @Field("order_id") int order_id,
                                  @Field("rate") int rate,
                                  @Field("reason") int reason,
                                  @Field("comment") String comment

    );

    @GET("api/Get-coupon")
    Call<CouponDataModel> checkCoupon(@Query(value = "coupon_num") String coupon_num);

    @GET("api/get-comments")
    Call<FeedbackDataModel> getFeedback(@Header("Authorization") String user_token,
                                        @Query(value = "user_type") String user_type,
                                        @Query(value = "user_id") int user_id,
                                        @Query(value = "page") int page,
                                        @Query(value = "pagination") String pagination,
                                        @Query(value = "limit_per_page") int limit_per_page);

    @GET("api/get-driver-location")
    Call<UserModel> getDriverLocation(@Header("Authorization") String user_token,
                                      @Query(value = "driver_id") int driver_id);

    @FormUrlEncoded
    @POST("api/update-order-location")
    Call<ResponseBody> updateDriverLocation(@Header("Authorization") String user_token,
                                            @Field("driver_id") int driver_id,
                                            @Field("order_id") int order_id,
                                            @Field("latitude") double latitude,
                                            @Field("longitude") double longitude


    );


    @GET("api/get-user-balance")
    Call<BalanceModel> getUserBalance(@Header("Authorization") String user_token,
                                      @Query("user_id") int user_id);


    @GET("directions/json")
    Call<PlaceDirectionModel> getDirection(@Query("origin") String origin,
                                           @Query("destination") String destination,
                                           @Query("transit_mode") String transit_mode,
                                           @Query("key") String key
    );

    @FormUrlEncoded
    @POST("api/delete-notification")
    Call<ResponseBody> deleteNotification(@Header("Authorization") String user_token,
                                          @Field("notification_id") int notification_id
    );


    @FormUrlEncoded
    @POST("api/delete-user-notification")
    Call<ResponseBody> deleteAllNotification(@Header("Authorization") String user_token,
                                             @Field("user_id") int user_id
    );

    @FormUrlEncoded
    @POST("api/delete-user-logo")
    Call<UserModel> deleteUserImage(@Header("Authorization") String user_token,
                                    @Field("user_id") int user_id
    );
}