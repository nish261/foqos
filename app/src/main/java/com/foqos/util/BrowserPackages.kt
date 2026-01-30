package com.foqos.util

object BrowserPackages {
    val COMMON_BROWSERS = setOf(
        "com.android.chrome",
        "org.mozilla.firefox",
        "com.brave.browser",
        "com.microsoft.emmx",
        "com.opera.browser",
        "com.opera.mini.native",
        "com.duckduckgo.mobile.android",
        "com.kiwibrowser.browser",
        "org.chromium.chrome",
        "com.sec.android.app.sbrowser", // Samsung Internet
        "com.UCMobile.intl",
        "com.ecosia.android",
        "org.mozilla.focus",
        "com.vivaldi.browser",
        "com.yandex.browser",
        "com.qwant.liberty",
        "com.ghostery.android.ghostery"
    )

    fun isBrowser(packageName: String): Boolean {
        return packageName in COMMON_BROWSERS
    }
}
