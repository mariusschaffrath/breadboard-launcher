// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugin.localization;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.flutter.embedding.engine.systemchannels.LocalizationChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Android implementation of the localization plugin with compatibility for Android 5.0/5.1 (API 21/22).
 */
public class LocalizationPlugin {
  @NonNull private final LocalizationChannel localizationChannel;
  @NonNull private final Context context;

  @SuppressLint({"AppBundleLocaleChanges", "DiscouragedApi"})
  @VisibleForTesting
  final LocalizationChannel.LocalizationMessageHandler localizationMessageHandler =
      new LocalizationChannel.LocalizationMessageHandler() {
        @Override
        public String getStringResource(@NonNull String key, @Nullable String localeString) {
          Context localContext = context;
          String stringToReturn = null;

          if (localeString != null) {
            Locale locale = localeFromString(localeString);
            Configuration config = new Configuration(context.getResources().getConfiguration());
            config.setLocale(locale);
            localContext = context.createConfigurationContext(config);
          }

          String packageName = context.getPackageName();
          int resId = localContext.getResources().getIdentifier(key, "string", packageName);
          if (resId != 0) {
            stringToReturn = localContext.getResources().getString(resId);
          }

          return stringToReturn;
        }
      };

  public LocalizationPlugin(
      @NonNull Context context, @NonNull LocalizationChannel localizationChannel) {
    this.context = context;
    this.localizationChannel = localizationChannel;
    this.localizationChannel.setLocalizationMessageHandler(localizationMessageHandler);
  }

  @SuppressWarnings("deprecation")
  @Nullable
  public Locale resolveNativeLocale(@Nullable List<Locale> supportedLocales) {
    if (supportedLocales == null || supportedLocales.isEmpty()) {
      return null;
    }

    Locale preferredLocale = null;
    try {
      if (Build.VERSION.SDK_INT >= 24) {
        Object localeList = context.getResources().getConfiguration().getClass().getMethod("getLocales").invoke(context.getResources().getConfiguration());
        if (localeList != null) {
          int count = (int) localeList.getClass().getMethod("size").invoke(localeList);
          for (int index = 0; index < count; ++index) {
            Locale candidate = (Locale) localeList.getClass().getMethod("get", int.class).invoke(localeList, index);
            for (Locale locale : supportedLocales) {
              if (candidate != null && candidate.equals(locale)) {
                return locale;
              }
            }
          }
        }
      }
    } catch (Throwable ignored) {
    }

    try {
      preferredLocale = context.getResources().getConfiguration().locale;
      if (preferredLocale != null) {
        for (Locale locale : supportedLocales) {
          if (preferredLocale.equals(locale)) {
            return locale;
          }
        }
        for (Locale locale : supportedLocales) {
          if (preferredLocale.getLanguage().equals(locale.toString())) {
            return locale;
          }
        }
      }
    } catch (Throwable ignored) {
    }

    return supportedLocales.get(0);
  }

  @SuppressWarnings("deprecation")
  public void sendLocalesToFlutter(@NonNull Configuration config) {
    List<Locale> locales = new ArrayList<>();
    try {
      if (Build.VERSION.SDK_INT >= 24) {
        Object localeList = config.getClass().getMethod("getLocales").invoke(config);
        if (localeList != null) {
          int count = (int) localeList.getClass().getMethod("size").invoke(localeList);
          for (int index = 0; index < count; ++index) {
            Locale locale = (Locale) localeList.getClass().getMethod("get", int.class).invoke(localeList, index);
            if (locale != null) {
              locales.add(locale);
            }
          }
        }
      }
    } catch (Throwable ignored) {
    }

    if (locales.isEmpty()) {
      try {
        if (config.locale != null) {
          locales.add(config.locale);
        } else {
          locales.add(Locale.getDefault());
        }
      } catch (Throwable ignored) {
        locales.add(Locale.getDefault());
      }
    }

    localizationChannel.sendLocales(locales);
  }

  @NonNull
  public static Locale localeFromString(@NonNull String localeString) {
    localeString = localeString.replace('_', '-');
    String parts[] = localeString.split("-", -1);
    String languageCode = parts[0];
    String scriptCode = "";
    String countryCode = "";
    int index = 1;
    if (parts.length > index && parts[index].length() == 4) {
      scriptCode = parts[index];
      index++;
    }
    if (parts.length > index && parts[index].length() >= 2 && parts[index].length() <= 3) {
      countryCode = parts[index];
      index++;
    }
    return new Locale(languageCode, countryCode, scriptCode);
  }
}
