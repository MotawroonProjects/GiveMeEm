package com.endpoint.giveme.models;

import android.content.Context;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.ObservableField;

import com.endpoint.giveme.BR;
import com.endpoint.giveme.R;


public class LoginModel extends BaseObservable {
    private String phone;
    public ObservableField<String> error_phone = new ObservableField<>();


    public boolean isDataValid(Context context)
    {
        if (!phone.trim().isEmpty())
        {
            error_phone.set(null);
            return true;
        }else
            {
                error_phone.set(context.getString(R.string.field_required));
                return false;
            }
    }
    public LoginModel() {
        setPhone("");
    }

    @Bindable
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
        notifyPropertyChanged(BR.phone);
    }
}
