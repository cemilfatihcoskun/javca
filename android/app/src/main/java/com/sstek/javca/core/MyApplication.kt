package com.sstek.javca.core

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.sstek.javca.launcher.presentation.LauncherActivity
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication: Application()