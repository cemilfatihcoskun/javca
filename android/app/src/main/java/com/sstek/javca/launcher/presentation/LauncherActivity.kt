package com.sstek.javca.launcher.presentation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.permissionx.guolindev.PermissionX
import com.sstek.javca.R
import com.sstek.javca.auth.presentation.login.LogInActivity
import com.sstek.javca.main.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LauncherActivity : AppCompatActivity() {

    private val viewModel: LauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkPermissions()
    }

    private fun checkPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.INTERNET,
            Manifest.permission.SYSTEM_ALERT_WINDOW
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            permissions.add(Manifest.permission.FOREGROUND_SERVICE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        PermissionX.init(this)
            .permissions(permissions)
            .onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(
                    deniedList,
                    "Bu izinlere ihtiyacımız var",
                    "Tamam",
                    "İptal"
                )
            }
            .onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(
                    deniedList,
                    "Lütfen ayarlardan izinleri açın",
                    "Ayarlar",
                    "İptal"
                )
            }
            .request { allGranted, _, _ ->
                if (!allGranted) {
                    showPermissionDeniedDialog()
                } else {
                    checkInternet()
                }
            }
    }

    private fun checkInternet() {
        if (!isInternetAvailable()) {
            showNoInternetDialog()
        } else {
            checkAuth()
        }
    }

    private fun checkAuth() {
        viewModel.reloadAuth(
            onSuccess = { navigateToMain() },
            onFailure = { navigateToLogin() }
        )
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LogInActivity::class.java))
        finish()
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun showPermissionDeniedDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("İzinler Reddedildi")
            .setMessage("Gerekli izinleri vermediğiniz için uygulama kapatılıyor.")
            .setCancelable(false)
            .setPositiveButton("Tamam") { _, _ ->
                finish()
            }
            .show()
    }

    private fun showNoInternetDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("İnternet Bağlantısı Yok")
            .setMessage("Uygulamayı kullanabilmek için internet bağlantısı gereklidir.")
            .setCancelable(false)
            .setPositiveButton("Tamam") { _, _ ->
                finish()
            }
            .show()
    }
}
