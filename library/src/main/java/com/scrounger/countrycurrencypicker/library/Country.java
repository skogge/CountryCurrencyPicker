/*
 * Copyright (C) 2017 Scrounger
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.scrounger.countrycurrencypicker.library;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Country implements Parcelable {

    //region Members
    private String code;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @DrawableRes
    private Integer flagId;

    @NonNull
    @DrawableRes
    public Integer getFlagId() {
        return flagId;
    }

    public void setFlagId(@DrawableRes Integer flagId) {
        this.flagId = flagId;
    }

    private Currency currency;

    @Nullable
    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    private Locale locale;

    public Locale getLocale() {
        if (locale == null) {
            return new Locale("", code);
        }

        return locale;
    }
    //endregion

    //region Constructor
    public Country(String code, String name, @DrawableRes Integer flagId) {
        this.code = code;
        this.name = name;
        this.flagId = flagId;
    }
    //endregion

    //region Retrieve
    @Nullable
    public static Country getCountry(String countryCode, Context context) {
        try {
            Locale locale = new Locale("", countryCode);

            return new Country(countryCode,
                    locale.getDisplayName(),
                    Helper.getFlagDrawableId(countryCode, context));
        } catch (NullPointerException e) {
            return null;
        }
    }

    @Nullable
    public static Country getCountryByName(final String countryName, Context context) {
        try {

            Locale locale = CollectionUtil.first(Arrays.asList(Locale.getAvailableLocales()), new CollectionUtil.Func1<Locale, Boolean>() {

                @Override
                public Boolean call(Locale input) {
                    return input.getDisplayCountry(Locale.US).equals(countryName);
                }
            });

            return new Country(locale.getCountry(),
                    locale.getDisplayName(),
                    Helper.getFlagDrawableId(locale.getCountry(), context));
        } catch (NullPointerException e) {
            return null;
        }
    }

    @Nullable
    public static Country getCountryWithCurrency(String countryCode, Context context) {
        Country country = getCountry(countryCode, context);

        if (country != null) {
            Currency currency = Currency.getCurrency(countryCode, context);
            if (currency != null) {
                //z.B. Antarktis is null -> keine Währung
                country.setCurrency(currency);
                return country;
            }
        }
        return null;
    }

    public static ArrayList<Country> listAll(Context context, final String filter) {
        ArrayList<Country> list = new ArrayList<>();

        for (String countryCode : Locale.getISOCountries()) {
            Country country = getCountry(countryCode, context);

            list.add(country);
        }

        sortList(list);

        if (filter != null && filter.length() > 0) {
            return new ArrayList<>(CollectionUtil.filter(list, new CollectionUtil.Func1<Country, Boolean>() {
                @Override
                public Boolean call(Country input) {
                    return input.getName().toLowerCase().contains(filter.toLowerCase());
                }
            }));
        } else {
            return list;
        }
    }

    public static ArrayList<Country> listAllWithCurrencies(Context context, final String filter) {
        ArrayList<Country> list = new ArrayList<>();

        for (String countryCode : Locale.getISOCountries()) {
            Country country = getCountryWithCurrency(countryCode, context);

            if (country != null) {
                //z.B. Antarktis is null -> keine Währung
                list.add(country);
            }
        }

        sortList(list);

        if (filter != null && filter.length() > 0) {
            return new ArrayList<>(CollectionUtil.filter(list, new CollectionUtil.Func1<Country, Boolean>() {
                @Override
                public Boolean call(Country input) {
                    return input.getName().toLowerCase().contains(filter.toLowerCase()) ||
                            input.getCurrency().getName().toLowerCase().contains(filter.toLowerCase()) ||
                            input.getCurrency().getSymbol().toLowerCase().contains(filter.toLowerCase());
                }
            }));
        } else {
            return list;
        }
    }
    //endregion

    //region Functions
    private static void sortList(ArrayList<Country> list) {
        Collections.sort(list, new Comparator<Country>() {
            @Override
            public int compare(Country ccItem, Country ccItem2) {
                return Helper.removeAccents(ccItem.getName()).toLowerCase().compareTo(Helper.removeAccents(ccItem2.getName()).toLowerCase());
            }
        });
    }
    //endregion


    @Override
    public String toString() {
        return "Country{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", flagId=" + flagId +
                ", currency=" + currency +
                ", locale=" + locale +
                '}';
    }

    //region Parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.code);
        dest.writeString(this.name);
        dest.writeValue(this.flagId);
        dest.writeParcelable(this.currency, flags);
        dest.writeSerializable(this.locale);
    }

    private Country(Parcel in) {
        this.code = in.readString();
        this.name = in.readString();
        this.flagId = (Integer) in.readValue(Integer.class.getClassLoader());
        this.currency = in.readParcelable(Currency.class.getClassLoader());
        this.locale = (Locale) in.readSerializable();
    }

    public static final Creator<Country> CREATOR = new Creator<Country>() {
        @Override
        public Country createFromParcel(Parcel source) {
            return new Country(source);
        }

        @Override
        public Country[] newArray(int size) {
            return new Country[size];
        }
    };
    //endregion

}
