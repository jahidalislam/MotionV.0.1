# Motion Studio – Android APK

## ধাপ ১: GitHub-এ আপলোড
1. github.com-এ নতুন একটি repository বানান (যেমন: `motion-studio`), Public বা Private যেকোনোটা।
2. zip ফাইলটি extract করুন।
3. Repository পেজে **Add file → Upload files** চাপুন, extract করা সব ফাইল ও ফোল্ডার ড্র্যাগ করে দিন, তারপর **Commit changes**।
   - `.github` ফোল্ডারটি যেন আপলোড হয় সেটা নিশ্চিত করুন। না হলে **Add file → Create new file**-এ নাম দিন `.github/workflows/build-apk.yml` এবং ভেতরে `.github/workflows/build-apk.yml` ফাইলের কনটেন্ট পেস্ট করুন।

## ধাপ ২: APK তৈরি
- Commit করলেই **Actions** ট্যাবে "Build Android APK" চলবে। প্রায় ৫-১০ মিনিট লাগে।
- সবুজ টিক এলে **Releases** পেজে যান — সেখানে `app-debug.apk` ফাইল পাবেন।

## ধাপ ৩: ফোনে ইনস্টল
1. ফোনের ব্রাউজারে GitHub Releases পেজ খুলে `app-debug.apk` ডাউনলোড করুন।
2. ডাউনলোড শেষে ফাইলটিতে ট্যাপ করুন। প্রথমবার বললে **Install unknown apps** / "এই উৎস থেকে ইনস্টলের অনুমতি" দিন।
3. **Install** চাপুন। অ্যাপ আইকন হোম স্ক্রিনে আসবে।

প্রয়োজনীয়তা: Android 10 বা তার উপরের ভার্সন।

## পরে আপডেট করলে
ফাইল বদলে আবার commit করলেই নতুন APK তৈরি ও Releases-এ যুক্ত হবে।
