# HotelMobileApp

Android app for searching hotels and booking rooms.

![Kotlin](https://img.shields.io/badge/Kotlin-1.9-7F52FF?logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-min%20SDK%2029-3DDC84?logo=android&logoColor=white)
![Retrofit](https://img.shields.io/badge/Retrofit-2.9-48B983)
![Stripe](https://img.shields.io/badge/Stripe-635BFF?logo=stripe&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-8.9-02303A?logo=gradle&logoColor=white)

## Overview

HotelMobileApp is the guest client of the Hotel project. It uses the [Hotel-back](https://github.com/XXXDoriXXX/Hotel-back) REST API for all data.

| Repository | Role |
| --- | --- |
| [Hotel-back](https://github.com/XXXDoriXXX/Hotel-back) | REST API used by this app |
| [Hotel-front-web](https://github.com/XXXDoriXXX/Hotel-front-web) | Web panel for hotel owners, [live demo](https://hotel-front-web.vercel.app) |
| [HotelMobileApp](https://github.com/XXXDoriXXX/HotelMobileApp) | This Android app |
| [HotelFastApi](https://github.com/XXXDoriXXX/HotelFastApi) | Earlier prototype of the API (legacy) |

```
HotelMobileApp (Retrofit) ──> Hotel-back (FastAPI) ──> PostgreSQL
```

## Features

- Registration and login, session kept on the device
- Hotel search with filters, hotel details with photos, amenities and ratings
- Room list and room details with images and booked dates
- Booking with date selection and card payment through Stripe
- Booking history, booking details, refund requests
- Favorite hotels
- Profile editing and avatar
- Share a hotel link and open its location in maps
- English and Ukrainian languages, light and dark themes

## Tech stack

Kotlin, Android SDK (min 29, target 34, compile 35), View Binding, Retrofit and OkHttp, Gson, Glide, Stripe Android SDK, Google Play Services Location, Material Components, Lottie, Shimmer, Navigation component.

## Getting started

Requirements: Android Studio with JDK 11 or newer and an emulator or a device running Android 10 (API 29) or newer.

1. Clone the repository:
   ```bash
   git clone https://github.com/XXXDoriXXX/HotelMobileApp.git
   ```
2. Open the project in Android Studio and let Gradle sync.
3. Set the API address in `app/src/main/java/com/example/hotelapp/Holder/apiHolder.kt` (`BASE_URL`). To use a local backend from the Android emulator, use `http://10.0.2.2:8000`.
4. Run the `app` configuration, or build from the command line:
   ```bash
   ./gradlew assembleDebug
   ```

## Configuration

| Setting | Where | Description |
| --- | --- | --- |
| `BASE_URL` | `Holder/apiHolder.kt` | Base URL of the Hotel-back API |
| Firebase config | `app/google-services.json` | Firebase project file, replace it with your own if you fork the project |

Payments need a running Hotel-back instance configured with Stripe keys.

## Author

[XXXDoriXXX](https://github.com/XXXDoriXXX)
