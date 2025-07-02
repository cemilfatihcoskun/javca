package com.sstek.javca.presentation.launcher

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sstek.javca.presentation.login.LogInActivity
import com.sstek.javca.presentation.main.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.permissionx.guolindev.PermissionX
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LauncherActivity : AppCompatActivity() {

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
            .request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    proceed()
                } else {
                    showPermissionDeniedDialog()
                }
            }
    }

    private fun proceed() {
        val isLoggedIn = FirebaseAuth.getInstance().currentUser != null
        val intent = if (isLoggedIn) {
            Intent(this, MainActivity::class.java)
        } else {
            Intent(this, LogInActivity::class.java)
        }
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

