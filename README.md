# 🏆 Saiem Sports App

একটি প্রফেশনাল Android স্পোর্টস অ্যাপ যা লাইভ ফুটবল ও ক্রিকেট স্ট্রিমিং, লাইভ স্কোর এবং স্ট্যান্ডিং টেবিল প্রদান করে।

## ✨ ফিচার

### 📺 লাইভ স্ট্রিমিং (Sportzfy Style)
- **মাল্টি-সোর্স স্ট্রিম** – Firebase Firestore + Stream API + Web Scraper
- **অটো-ফলব্যাক** – একটি সোর্স কাজ না করলে স্বয়ংক্রিয়ভাবে পরেরটিতে যায়
- **HLS/M3U8/MP4/DASH** সব ফরম্যাট সাপোর্ট
- **ExoPlayer Media3** দিয়ে স্মুথ স্ট্রিমিং
- **Picture-in-Picture (PiP)** মোড সাপোর্ট

### ⚽🏏 লাইভ স্কোর (Sofascore Style)
- ফুটবল: Premier League, Champions League, La Liga, Serie A, Bundesliga, Ligue 1
- ক্রিকেট: CricAPI দিয়ে লাইভ স্কোর
- ম্যাচ স্ট্যাটাস, মিনিট, স্কোর রিয়েল-টাইম আপডেট
- স্ট্যান্ডিং টেবিল

### 📡 TV চ্যানেল গ্রিড
- স্পোর্টস চ্যানেলের গ্রিড ভিউ
- HD/SD কোয়ালিটি ইন্ডিকেটর
- Firebase থেকে চ্যানেল লিস্ট

### 🎨 UI/UX
- **ডার্ক থিম** – প্রফেশনাল কালো/সবুজ কালার স্কিম
- Shimmer loading effect
- Swipe-to-refresh
- অটো-ফুলস্ক্রিন ভিডিও প্লেয়ার

## 🏗️ আর্কিটেকচার

```
MVVM + Repository Pattern + Hilt DI

app/
├── data/
│   ├── model/          # Match, Team, StreamSource, TvChannel, Standing…
│   ├── remote/
│   │   ├── api/        # FootballDataApiService, CricketApiService, StreamApiService
│   │   └── scraper/    # StreamScraper, ScoreScraper (Jsoup)
│   ├── local/          # Room Database (MatchDao, ChannelDao)
│   └── repository/     # SportsRepository (single source of truth)
├── ui/
│   ├── home/           # HomeFragment + MatchAdapter
│   ├── scores/         # LiveScoresFragment + StandingsAdapter
│   ├── channels/       # ChannelsFragment + ChannelGridAdapter
│   ├── player/         # VideoPlayerActivity + StreamSelectorAdapter
│   └── splash/         # SplashActivity
├── viewmodel/          # HomeViewModel, LiveScoreViewModel, StreamViewModel
└── di/                 # AppModule (Hilt)
```

## ⚙️ সেটআপ

### ১. Firebase সেটআপ
1. [Firebase Console](https://console.firebase.google.com) এ প্রজেক্ট তৈরি করুন
2. Android app যোগ করুন (`com.saiem.sportsapp`)
3. `google-services.json` ডাউনলোড করে `app/` ফোল্ডারে রাখুন
4. Firestore Database তৈরি করুন

### ২. API Keys
Firebase Remote Config-এ এই keys সেট করুন:
- `football_data_api_key` – [football-data.org](https://football-data.org) থেকে ফ্রি key নিন
- `cricket_api_key` – [cricapi.com](https://cricapi.com) থেকে ফ্রি key নিন

### ৩. Firestore Collections
```
streams/
  {docId}: { matchId, streamUrl, title, quality, language, priority, isActive, referer }

tv_channels/
  {docId}: { name, logo, streamUrl, category, country, quality, isActive }
```

### ৪. বিল্ড করুন
```bash
./gradlew assembleDebug
```

## 🔧 টেকনোলজি স্ট্যাক

| লাইব্রেরি | কাজ |
|-----------|-----|
| Kotlin + Coroutines | Async programming |
| Hilt | Dependency Injection |
| Retrofit + OkHttp | REST API |
| Firebase Firestore | Stream sources DB |
| Firebase RemoteConfig | API key management |
| ExoPlayer (Media3) | Video streaming |
| Room | Local caching |
| Jsoup | Web scraping fallback |
| Glide | Image loading |
| Shimmer | Loading animation |

## 📱 স্ক্রিনশট

> Dark theme, TV channel grid এবং live score UI

---
Made with ❤️ by Saiem
