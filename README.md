# TikTok Mod Lab

A personal Android companion app for experimenting with a local plugin system.

## Current features

- Russian-language interface.
- Central plugin catalog in `PluginRegistry`.
- `PluginManager` for reading and saving plugin states locally.
- Plugin switches persist after the app is restarted.
- First working local plugin: **Тема Mod Lab**, which changes the companion app's appearance.
- Other catalog entries are clearly marked as upcoming prototypes.

## Important boundary

This is a separate companion app. It does not patch, inject into, repackage, or bypass protections in the official TikTok app. Plugin functionality currently runs only inside Mod Lab. No TikTok account credentials, session cookies, or private API access are collected.

Any future TikTok integration must use documented, authorized interfaces and respect platform terms.

## Build

Open this repository in Android Studio with Android SDK 36 installed, then build the `app` module.

Package: `com.example.tiktokmodlab`
