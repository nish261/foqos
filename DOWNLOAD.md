# Quick Download Guide 📥

## Easiest Way: GitHub Releases

**Best for most users - Direct APK download, no extraction needed!**

1. Go to **[Releases](https://github.com/nish261/sonahai/releases)**
2. Click the latest release
3. Download `foqos-vX.X.X.apk` from the Assets section
4. Install on your device

✅ **No ZIP extraction required!**

---

## Alternative: GitHub Actions Build

**For developers who want the absolute latest commit**

⚠️ **Important:** GitHub Actions artifacts are **ALWAYS ZIPPED**

### Steps:
1. Go to **[Actions](https://github.com/nish261/sonahai/actions)**
2. Click the latest successful workflow (green checkmark ✓)
3. Scroll to **Artifacts** section at the bottom
4. Download `foqos-debug.zip` or `foqos-release-unsigned.zip`
5. **Extract the ZIP file** on your computer
6. Install the `.apk` file inside

### Why is it zipped?
GitHub Actions automatically wraps all artifacts in ZIP files for security and consistency. This is GitHub's behavior, not the app.

---

## Building Locally

**For developers who want to build from source**

```bash
cd /Users/nishchalasri/foqos
./gradlew assembleDebug
# APK location: app/build/outputs/apk/debug/app-debug.apk
```

✅ **No ZIP - direct APK output!**

---

## Recommended Approach

For regular users: **Use GitHub Releases** (Option 1)
- Direct APK download
- No extraction needed  
- Version tagged releases
- Cleaner filenames

For developers: **Build locally** or use Actions
- Latest code
- Debug builds available

---

## Creating a Release (For Maintainers)

To create a new release with direct APK:

```bash
# Tag the version
git tag v1.0.0
git push origin v1.0.0

# GitHub Actions will automatically:
# 1. Build the APK
# 2. Create a release
# 3. Attach the APK file (NOT zipped!)
```

The release workflow (`.github/workflows/release.yml`) handles everything automatically.

---

## TL;DR

**Want an APK right now?**

Releases → Latest → Download APK → Install ✨

**No ZIP extraction needed with releases!**
