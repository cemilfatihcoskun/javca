package com.sstek.javca.launcher.presentation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sstek.javca.main.presentation.MainActivity
import com.sstek.javca.auth.presentation.login.LogInActivity
import com.google.firebase.auth.FirebaseAuth
import com.permissionx.guolindev.PermissionX
import com.sstek.javca.auth.domain.usecase.ReloadAuthUseCase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.sstek.javca.R

@AndroidEntryPoint
class LauncherActivity() : AppCompatActivity() {
    private val viewModel: LauncherViewModel by viewModels()
    private lateinit var textViewInternetStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkPermissions()

        if (isInternetAvailable()) {
            proceed()
        } else {
            setContentView(R.layout.activity_launcher)
            textViewInternetStatus = findViewById(R.id.textViewInternetStatus)
            textViewInternetStatus.visibility = View.VISIBLE
        }
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
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
            .request { allGranted, grantedList, deniedList ->
                if (!allGranted) {
                    showPermissionDeniedDialog()
                }
            }
    }

    private fun proceed() {
        viewModel.reloadAuth(
            onSuccess = {
                navigateToMain()
            },
            onFailure = {
                navigateToLogin()
            }
        )
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        //intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LogInActivity::class.java)
        //intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
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

}

