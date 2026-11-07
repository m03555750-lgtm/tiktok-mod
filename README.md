# TikTok Mod Lab

A personal Android prototype for experimenting with a plugin-style companion app.

## Current scope

- Android application shell.
- Russian-language plugin list with enable/disable switches.
- Demonstration plugins only; switches do not modify TikTok.
- No TikTok account credentials, session cookies, or private API access are collected.

## Important boundary

This project is a separate companion app. It does not patch, inject into, repackage, or bypass protections in the official TikTok app. Any future TikTok integration must use documented, authorized interfaces and respect platform terms.

## Build

Open this repository in Android Studio with Android SDK 36 installed, then build the `app` module.

Package: `com.example.tiktokmodlab`
