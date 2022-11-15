package com.example.ecoroute.ui

import android.content.pm.PackageManager
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.example.ecoroute.R
import com.mapbox.vision.mobile.core.utils.SystemInfoUtils.isVisionSupported
import com.mapbox.vision.mobile.core.utils.SystemInfoUtils.obtainSystemInfo
import com.mapbox.vision.utils.VisionLogger.Companion.e


abstract class BaseActivity : AppCompatActivity() {
    protected abstract fun onPermissionsGranted()
    protected abstract fun initViews()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isVisionSupported()) {
            val textView = TextView(this)
            val padding = dpToPx(20f).toInt()
            textView.setPadding(padding, padding, padding, padding)
            textView.movementMethod = LinkMovementMethod.getInstance()
            textView.isClickable = true
            textView.text = HtmlCompat.fromHtml(
                getString(R.string.vision_not_supported_message),
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
            AlertDialog.Builder(this)
                .setTitle(R.string.vision_not_supported_title)
                .setView(textView)
                .setCancelable(false)
                .show()
            e(
                "BoardNotSupported",
                "System Info: [" + obtainSystemInfo() + "]"
            )
        }
        initViews()
        title = getString(R.string.app_name) + " " + this.javaClass.simpleName
        if (!allPermissionsGranted()) {
            requestPermissions(requiredPermissions, PERMISSIONS_REQUEST_CODE)
        } else {
            onPermissionsGranted()
        }
    }

    protected fun allPermissionsGranted(): Boolean {
        for (permission in requiredPermissions) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // PERMISSION_FOREGROUND_SERVICE was added for targetSdkVersion >= 28, it is normal and always granted, but should be added to the Manifest file
                // on devices with Android < P(9) checkSelfPermission(PERMISSION_FOREGROUND_SERVICE) can return PERMISSION_DENIED, but in fact it is GRANTED, so skip it
                // https://developer.android.com/guide/components/services#Foreground
                if (permission == PERMISSION_FOREGROUND_SERVICE) {
                    continue
                }
                return false
            }
        }
        return true
    }

    private val requiredPermissions: Array<String>
        get() {
            val permissions: Array<String> = try {
                val info = packageManager.getPackageInfo(
                    packageName, PackageManager.GET_PERMISSIONS
                )
                val requestedPermissions = info.requestedPermissions
                if (requestedPermissions != null && requestedPermissions.isNotEmpty()) {
                    requestedPermissions
                } else {
                    arrayOf()
                }
            } catch (e: PackageManager.NameNotFoundException) {
                arrayOf()
            }
            return permissions
        }

    private fun dpToPx(dp: Float): Float {
        return dp * applicationContext.resources.displayMetrics.density
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (allPermissionsGranted() && requestCode == PERMISSIONS_REQUEST_CODE) {
            onPermissionsGranted()
        }
    }

    companion object {
        private const val PERMISSION_FOREGROUND_SERVICE = "android.permission.FOREGROUND_SERVICE"
        private const val PERMISSIONS_REQUEST_CODE = 123
    }
}