package com.endpoint.giveme.models;

import android.content.Context;
import android.net.Uri;
import android.util.Patterns;
import android.widget.Toast;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.ObservableField;

import com.endpoint.giveme.BR;
import com.endpoint.giveme.R;


public class SignUpModel extends BaseObservable {
    private Uri image;
    private String name;
    private String email;
    private String year;
    private String gender;
    private String phone_code;
    private String phone;
    private String country_id;
    private boolean isAcceptTerms;
    
    public ObservableField<String> error_name = new ObservableField<>();
    public ObservableField<String> error_email = new ObservableField<>();


    public boolean isDataValid(Context context)
    {
        if (!name.trim().isEmpty()&&
                !email.trim().isEmpty()&&
                !year.trim().isEmpty()&&
                !gender.trim().isEmpty()&&
                Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()&&
                isAcceptTerms
        ){
            error_name.set(null);
            error_email.set(null);

            return true;
        }else
            {
                if (name.trim().isEmpty())
                {
                    error_name.set(context.getString(R.string.field_required));

                }else
                    {
                        error_name.set(null);

                    }

                if (email.trim().isEmpty())
                {
                    error_email.set(context.getString(R.string.field_required));

                }else if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches())
                {
                    error_email.set(context.getString(R.string.inv_email));

                }else {
                    error_email.set(null);

                }
                if (year.trim().isEmpty())
                {
                    Toast.makeText(context, R.string.ch_year, Toast.LENGTH_SHORT).show();
                }

                if (gender.trim().isEmpty())
                {
                    Toast.makeText(context, R.string.ch_gender, Toast.LENGTH_SHORT).show();
                }

                if (!isAcceptTerms){
                    Toast.makeText(context, R.string.accept_terms, Toast.LENGTH_SHORT).show();
                }
                return false;
            }
    }
    public SignUpModel() {
        setName("");
        setEmail("");
        setYear("");
        setGender("");
        setImage(null);
        isAcceptTerms = false;
    }



    @Bindable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        notifyPropertyChanged(BR.name);

    }


    @Bindable
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        notifyPropertyChanged(BR.email);
    }

    public Uri getImage() {
        return image;
    }

    public void setImage(Uri image) {
        this.image = image;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone_code() {
        return phone_code;
    }

    public void setPhone_code(String phone_code) {
        this.phone_code = phone_code;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCountry_id() {
        return country_id;
    }

    public void setCountry_id(String country_id) {
        this.country_id = country_id;
    }

    public boolean isAcceptTerms() {
        return isAcceptTerms;
    }

    public void setAcceptTerms(boolean acceptTerms) {
        isAcceptTerms = acceptTerms;
    }
}

