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
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Currency implements Parcelable {

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

    private String symbol;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    @DrawableRes
    private Integer flagId;

    @NonNull
    @DrawableRes
    public Integer getFlagId() {
        return flagId;
    }

    private ArrayList<Country> countries;

    @Nullable
    public ArrayList<Country> getCountries() {
        return countries;
    }

    public void setCountries(ArrayList<Country> countries) {
        this.countries = countries;
    }

    private ArrayList<String> countriesNames = null;

    @Nullable
    public ArrayList<String> getCountriesNames() {
        return countriesNames;
    }

    public void setCountriesNames(ArrayList<String> countriesNames) {
        this.countriesNames = countriesNames;
    }
    //endregion

    //region Constructor
    public Currency(String code, String name, String symbol, @DrawableRes Integer flagId) {
        this.code = code;
        this.name = name;
        this.symbol = symbol;
        this.flagId = flagId;
    }

    public Currency(String code, String name, String symbol, Integer flagId,
            ArrayList<Country> countries) {
        this.code = code;
        this.name = name;
        this.symbol = symbol;
        this.flagId = flagId;
        this.countries = countries;
    }

    //endregion

    //region Retrieve
    @Nullable
    public static Currency getCurrency(String countryCode, Context context) {
        Locale locale = new Locale("", countryCode);

        java.util.Currency currency = null;
        try {
            currency = java.util.Currency.getInstance(locale);
        } catch (IllegalArgumentException e) {
            // Unsupported currency
        }

        if (currency != null) {
            return new Currency(
                    currency.getCurrencyCode(),
                    Build.VERSION.SDK_INT >= 19 ? currency.getDisplayName()
                            : currency.getCurrencyCode(),
                    currency.getSymbol(),
                    Helper.getFlagDrawableId(currency.getCurrencyCode(), context));
        }
        return null;
    }

    @Contract("_, _ -> !null")
    public static Currency getCurrency(java.util.Currency currency, Context context) {
        return new Currency(
                currency.getCurrencyCode(),
                Build.VERSION.SDK_INT >= 19 ? currency.getDisplayName()
                        : currency.getCurrencyCode(),
                currency.getSymbol(),
                Helper.getFlagDrawableId(currency.getCurrencyCode(), context));
    }

    private static ArrayList<Country> tmpCountries;

    @Nullable
    public static Currency getCurrencyWithCountries(java.util.Currency currency, Context context) {
        final Currency myCurrency = getCurrency(currency, context);

        if (tmpCountries == null) {
            tmpCountries = Country.listAllWithCurrencies(context, null);
        }


        ArrayList<Country> foundCountries = new ArrayList<>(CollectionUtil.filter(tmpCountries,
                new CollectionUtil.Func1<Country, Boolean>() {
                    @Override
                    public Boolean call(Country country) {
                        return country.getCurrency().getCode().equals(myCurrency.getCode());
                    }
                }));

        if (foundCountries.size() > 0) {
            myCurrency.setCountries(foundCountries);

            ArrayList<String> names = new ArrayList<>();
            for (Country country : foundCountries) {
                names.add(country.getName());
            }
            myCurrency.setCountriesNames(names);

            return myCurrency;
        }
        return null;
    }

    public static ArrayList<Currency> listAll(Context context, final String filter) {
        ArrayList<Currency> list = new ArrayList<>();

        for (java.util.Currency currency : getAllCurrencies()) {
            list.add(getCurrency(currency, context));
        }

        sortList(list);

        if (filter != null && filter.length() > 0) {
            return new ArrayList<>(
                    CollectionUtil.filter(list, new CollectionUtil.Func1<Currency, Boolean>() {
                        @Override
                        public Boolean call(Currency currency) {
                            return currency.getName().toLowerCase().contains(filter.toLowerCase())
                                    ||
                                    currency.getSymbol().toLowerCase().contains(
                                            filter.toLowerCase());
                        }
                    }));
        } else {
            return list;
        }
    }

    public static ArrayList<Currency> listAllWithCountries(final Context context,
            final String filter) {
        ArrayList<Currency> list = new ArrayList<>();

        for (java.util.Currency currency : getAllCurrencies()) {
            Currency myCurrency = getCurrencyWithCountries(currency, context);

            if (myCurrency != null) {
                list.add(myCurrency);
            }
        }

        sortList(list);

        if (filter != null && filter.length() > 0) {
            return new ArrayList<>(CollectionUtil.filter(list,
                    new CollectionUtil.Func1<Currency, Boolean>() {
                        @Override
                        public Boolean call(Currency input) {
                            return input.getName().toLowerCase().contains(filter.toLowerCase()) ||
                                    input.getSymbol().toLowerCase().contains(filter.toLowerCase()) ||
                                    CollectionUtil.contains(input.getCountries(), new CollectionUtil.Func1<Country, Boolean>() {
                                        @Override
                                        public Boolean call(Country country) {
                                            return country.getName()
                                                    .toLowerCase().contains(filter.toLowerCase());
                                        }
                                    });
                        }
                    }));
        } else {
            return list;
        }
    }
    //endregion

    //region Functions
    private static void sortList(ArrayList<Currency> list) {
        Collections.sort(list, new Comparator<Currency>() {
            @Override
            public int compare(Currency ccItem, Currency ccItem2) {
                return Helper.removeAccents(ccItem.getName()).toLowerCase().compareTo(
                        Helper.removeAccents(ccItem2.getName()).toLowerCase());
            }
        });
    }
    //endregion

    @Override
    public String toString() {
        return "Currency{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", symbol='" + symbol + '\'' +
                ", flagId=" + flagId +
                ", countries=" + countries +
                ", countriesNames=" + countriesNames +
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
        dest.writeString(this.symbol);
        dest.writeValue(this.flagId);
        dest.writeTypedList(this.countries);
        dest.writeStringList(this.countriesNames);
    }

    private Currency(Parcel in) {
        this.code = in.readString();
        this.name = in.readString();
        this.symbol = in.readString();
        this.flagId = (Integer) in.readValue(Integer.class.getClassLoader());
        this.countries = in.createTypedArrayList(Country.CREATOR);
        this.countriesNames = in.createStringArrayList();
    }

    public static final Creator<Currency> CREATOR = new Creator<Currency>() {
        @Override
        public Currency createFromParcel(Parcel source) {
            return new Currency(source);
        }

        @Override
        public Currency[] newArray(int size) {
            return new Currency[size];
        }
    };
    //endregion

    public static Set<java.util.Currency> getAllCurrencies() {
        if (Build.VERSION.SDK_INT >= 19) {
            return java.util.Currency.getAvailableCurrencies();
        }

        Set<java.util.Currency> currencies = new HashSet<>();
        Locale[] locales = Locale.getAvailableLocales();

        for (Locale loc : locales) {
            try {
                java.util.Currency currency = java.util.Currency.getInstance(loc);

                if (currency != null) {
                    currencies.add(currency);
                }
            } catch (Exception exc) {
                // Locale not found
            }
        }

        return currencies;
    }

}
