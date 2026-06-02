package com.mascotasperdidas.app.app.util

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.core.content.PermissionChecker

object PermissionUtils {

    data class PermissionDef(
        val labelRes: Int,
        val permission: String,
    )

    fun getRequiredPermissions(): List<PermissionDef> = buildList {
        add(PermissionDef(com.mascotasperdidas.app.R.string.permissions_camera, Manifest.permission.CAMERA))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(
                PermissionDef(
                    com.mascotasperdidas.app.R.string.permissions_notifications,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(
                PermissionDef(
                    com.mascotasperdidas.app.R.string.permissions_storage,
                    Manifest.permission.READ_MEDIA_IMAGES
                )
            )
        } else {
            add(
                PermissionDef(
                    com.mascotasperdidas.app.R.string.permissions_storage,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            )
        }
        add(
            PermissionDef(
                com.mascotasperdidas.app.R.string.permissions_location,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    }

    fun isPermissionGranted(context: Context, permission: String): Boolean {
        return PermissionChecker.checkSelfPermission(context, permission) ==
            PermissionChecker.PERMISSION_GRANTED
    }
}
