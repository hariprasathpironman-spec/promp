# Build APK from Your Phone

You can build your APK using **GitHub Actions** (a free cloud build service) — no laptop needed!

---

## What You Need
- A phone with internet
- A **free GitHub account** (sign up at github.com on your phone browser)
- About 10 minutes of your time

---

## Step-by-Step Instructions

### Step 1: Create a GitHub Repository
1. Open your phone browser and go to **github.com**
2. Sign in or create a free account
3. Tap the **+** button (top right) → **New repository**
4. Name it something like `prompter-app`
5. Make it **Public** (or Private if you prefer)
6. Tap **Create repository**

### Step 2: Upload Your Project Files
**Option A — If you have a friend with a computer:**
- Send them this folder
- Ask them to upload all files to the GitHub repo
- That's it! They just need to drag and drop the files.

**Option B — From your phone (a bit tedious but possible):**
1. Go to your new GitHub repo in your phone browser
2. Tap **"Add file"** → **"Upload files"**
3. Select files from your phone
4. For nested folders (like `app/src/...`), you need to create each folder path manually on GitHub

**Option C — Best for phone users:**
1. Install **Termux** app from F-Droid (free)
2. In Termux, run these commands:
   ```bash
   pkg install git
   git clone https://github.com/YOUR_USERNAME/prompter-app.git
   cd prompter-app
   # Copy your project files here using your file manager
   git add .
   git commit -m "Add project files"
   git push origin main
   ```

### Step 3: Trigger the Build
1. Go to your GitHub repo
2. Tap **"Actions"** tab at the top
3. You should see the workflow called **"Build APK"**
4. Tap **"Run workflow"** → **"Run workflow"**
5. Wait 5-10 minutes for the build to finish

### Step 4: Download Your APK
1. After the build finishes, go to the **Actions** tab again
2. Tap on the latest completed run (green checkmark)
3. Scroll down to **"Artifacts"**
4. Tap **"prompter-debug-apk"** to download the APK
5. Install it on your phone!

---

## Important Notes

- **API Key:** The app needs a Gemini API key to work. You can add one later in the app settings, or create a `.env` file in your repo with:
  ```
  GEMINI_API_KEY=your_actual_key_here
  ```
  Get your key from: https://aistudio.google.com/app/apikey

- **This is a Debug APK:** It will work fine for personal use. For publishing on Play Store, you'd need a signed release APK.

- **Build Time:** The first build takes about 5-10 minutes. GitHub builds it for free in the cloud.

---

## Alternative: Build at an Internet Cafe
If you can access a computer at an internet cafe or library:
1. Download and install [Android Studio](https://developer.android.com/studio) (it's free)
2. Open this project folder in Android Studio
3. Create a `.env` file with your Gemini API key
4. Go to **Build → Build Bundle(s) / APK(s) → Build APK(s)**
5. Done! The APK will be in `app/build/outputs/apk/debug/app-debug.apk`

---

## Need Help?
If you get stuck, the most common issues are:
1. Missing `.env` file — the app will still build, but won't work without the API key
2. Upload issues — ask a friend with a computer to upload the files to GitHub for you
