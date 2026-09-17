# Spendly — personal expense tracker (Android)

Reads Monzo push notifications, pops a "what did you buy?" card, stores
everything locally, and shows a monthly dashboard. Built with Kotlin, Room
(local DB, nothing leaves your phone), and no third-party chart library
(the pie chart is a plain custom View).

## Build the APK (about 2 minutes)

1. Install **Android Studio** (free): https://developer.android.com/studio
2. Open Android Studio → **Open** → select this `spendly-android` folder.
3. Let it sync Gradle (first sync downloads the Android SDK bits it needs —
   takes a minute or two, only happens once).
4. Plug in your Android phone via USB with **USB debugging** enabled
   (Settings → About phone → tap "Build number" 7 times → Developer options →
   USB debugging), or just build an APK file directly:
   - Menu: **Build → Build App Bundle(s) / APK(s) → Build APK(s)**
   - When it finishes, click **locate** in the notification — you'll find
     `app-debug.apk` in `app/build/outputs/apk/debug/`.
5. Copy that `.apk` to your phone (USB, email to yourself, Google Drive —
   whatever's easiest) and open it on the phone to install. You'll need to
   allow "install unknown apps" for whichever app you used to transfer it —
   Android will prompt you the first time.

## First-time setup on your phone

1. Open Spendly, tap **"Grant notification access"** — this opens Android's
   notification-access settings. Find **Spendly** in the list and turn it on.
   (This is the permission that lets it read notification text — Android
   shows a scary-sounding warning here because the permission is powerful;
   that's expected.)
2. Spend something with your Monzo card. Within a second or two the "what
   did you buy?" card should pop up.
3. Tap a category, optionally add a note, hit save. It'll show up correctly
   categorised in the dashboard and the pie chart.

## Only tuned for Monzo right now

The notification parser only listens to Monzo's package (`co.uk.getmondo`).
To add another bank, open `BankNotificationListener.kt`:

- Add its Android package name to `BANK_PACKAGES`.
- Send yourself a test payment and check `adb logcat` for the notification
  text if the parser doesn't pick it up — different banks phrase alerts
  differently ("£4.20 at Tesco" vs "You spent £4.20 at TESCO STORES"), so
  `AMOUNT_REGEX` / `MERCHANT_REGEX` may need small tweaks per bank.

## What's real vs. what's a placeholder

- **Real and working**: the notification listener, the Room database, the
  categorise popup, the monthly total, the pie chart, manual entry.
- **Not included**: iCloud/multi-device sync, budgets/alerts, export to
  CSV, editing past entries from the list (tap re-opens the categorise card
  but there's no delete button yet) — all straightforward to add if you
  want them next.

## Package structure

```
app/src/main/java/com/spendly/app/
  BankNotificationListener.kt   — catches Monzo notifications, parses them
  data/
    Transaction.kt              — Room entity + category list
    TransactionDao.kt
    AppDatabase.kt
  ui/
    MainActivity.kt             — dashboard
    CategorizeActivity.kt       — the popup card
    TransactionAdapter.kt       — recycler list
    PieChartView.kt             — custom donut chart
```
