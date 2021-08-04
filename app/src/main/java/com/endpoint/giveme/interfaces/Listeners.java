package com.endpoint.giveme.interfaces;


public interface Listeners {

    interface SignUpListener{

        void openSheet();
        void closeSheet();
        void checkDataValid();
        void checkReadPermission();
        void checkCameraPermission();
        void male();
        void female();
    }

    interface BackListener
    {
        void back();
    }
    interface LoginListener{
        void validate();
        void showCountryDialog();
    }

    interface ProfileAction{
        void onReviews();
        void onFeedback();
        void onCoupons();
        void onAddCoupon();
        void onSetting();
        void onPayment();
        void onTelegram();
        void onNotification();
        void logout();
    }

    interface SettingAction{
        void onTone();
        void onComplaint();
        void onEditProfile();
        void onLanguageSetting();
        void onTerms();
        void onPrivacy();
        void onRate();
        void onTour();
        void onDelegate();
    }
}
