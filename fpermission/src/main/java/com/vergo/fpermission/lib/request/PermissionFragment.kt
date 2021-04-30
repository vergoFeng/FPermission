package com.vergo.fpermission.lib.request

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.fragment.app.Fragment
import com.vergo.fpermission.lib.FPermission

/**
 * 透明的fragment，用来请求权限
 *
 * @author fhj
 * @since 2021/04/28
 */
class PermissionFragment : Fragment() {
    companion object {
        /**
         * 请求正常权限的代码。
         */
        const val REQUEST_NORMAL_PERMISSIONS = 1

        /**
         * 请求ACCESS_BACKGROUND_LOCATION权限的代码。
         */
        const val REQUEST_BACKGROUND_LOCATION_PERMISSION = 2

        /**
         * 转发当前应用程序设置页面的代码。
         */
        const val FORWARD_TO_SETTINGS = 3

        /**
         * 请求SYSTEM_ALERT_WINDOW权限的代码。
         */
        const val ACTION_SYSTEM_ALERT_WINDOW_PERMISSION = 4

        /**
         * 请求WRITE_SETTINGS权限的代码。
         */
        const val ACTION_WRITE_SETTINGS_PERMISSION = 5

        /**
         * 请求MANAGE_EXTERNAL_STORAGE权限的代码。
         */
        const val ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION = 6
    }

    private var pb: PermissionBuilder? = null
    private var task: BaseTask? = null

    /**
     * 通过调用 requestPermissions() 立即请求普通权限，
     * 并在ActivityCompat.OnRequestPermissionsResultCallback中处理请求结果。
     */
    fun requestNow(
        permissions: Set<String>,
        permissionBuilder: PermissionBuilder,
        baseTask: BaseTask
    ) {
        pb = permissionBuilder
        task = baseTask
        requestPermissions(permissions.toTypedArray(), REQUEST_NORMAL_PERMISSIONS)
    }

    fun requestAccessBackgroundLocationNow(
        permissionBuilder: PermissionBuilder,
        baseTask: BaseTask
    ) {
        pb = permissionBuilder
        task = baseTask
        requestPermissions(
            arrayOf(RequestBackgroundLocationPermission.ACCESS_BACKGROUND_LOCATION),
            REQUEST_BACKGROUND_LOCATION_PERMISSION
        )
    }

    /**
     * 申请 SYSTEM_ALERT_WINDOW 权限。
     * 在Android M及更高版本上，它是启动Action为Settings.ACTION_MANAGE_OVERLAY_PERMISSION的Intent来请求的。
     */
    fun requestSystemAlertWindowPermissionNow(
        permissionBuilder: PermissionBuilder,
        baseTask: BaseTask
    ) {
        pb = permissionBuilder
        task = baseTask
        if (!Settings.canDrawOverlays(requireContext())) {
            startActivityForResult(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION),
                ACTION_SYSTEM_ALERT_WINDOW_PERMISSION
            )
        } else {
            onRequestSystemAlertWindowPermissionResult()
        }
    }

    /**
     * 申请WRITE_SETTINGS权限。
     * 在Android M及更高版本上，它是启动Action为Settings.ACTION_MANAGE_WRITE_SETTINGS的Intent来请求的。
     */
    fun requestWriteSettingsPermissionNow(
        permissionBuilder: PermissionBuilder,
        baseTask: BaseTask
    ) {
        pb = permissionBuilder
        task = baseTask
        if (!Settings.System.canWrite(requireContext())) {
            startActivityForResult(
                Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS),
                ACTION_WRITE_SETTINGS_PERMISSION
            )
        } else {
            onRequestWriteSettingsPermissionNow()
        }
    }

    /**
     * 申请MANAGE_EXTERNAL_STORAGE权限。
     * 在Android R及更高版本上，它是启动Action为Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION的Intent来请求的。
     */
    fun requestManageExternalStoragePermissionNow(
        permissionBuilder: PermissionBuilder,
        baseTask: BaseTask
    ) {
        pb = permissionBuilder
        task = baseTask
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            startActivityForResult(
                Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION),
                ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
            )
        } else {
            onRequestManageExternalStoragePermissionResult()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_NORMAL_PERMISSIONS) {
            onRequestNormalPermissionsResult(permissions, grantResults)
        } else if (requestCode == REQUEST_BACKGROUND_LOCATION_PERMISSION) {
            onRequestBackgroundLocationPermissionResult()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (checkNull()) return
        when (requestCode) {
            FORWARD_TO_SETTINGS -> task!!.requestAgain(ArrayList(pb!!.forwardPermissions))
            ACTION_SYSTEM_ALERT_WINDOW_PERMISSION -> onRequestSystemAlertWindowPermissionResult()
            ACTION_WRITE_SETTINGS_PERMISSION -> onRequestWriteSettingsPermissionNow()
            ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION -> onRequestManageExternalStoragePermissionResult()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (checkNull()) {
            // 当PermissionFragment销毁时，请关闭正在显示的对话框，以避免窗口泄漏问题。
            if (pb!!.currentDialog != null && pb!!.currentDialog!!.isShowing) {
                pb!!.currentDialog!!.dismiss()
            }
        }
    }

    /**
     * 处理普通权限请求的结果。
     */
    private fun onRequestNormalPermissionsResult(
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (checkNull()) return
        if (permissions.size == grantResults.size) {
            // 为了用户的安全，程序永远不会拥有授予的权限，因为用户可能会在设置中关闭某些权限。
            // 因此，每次请求时，都必须再次请求已经授予的权限，并刷新授予的权限集。
            pb!!.grantedPermissions.clear()
            permissions.forEachIndexed { index, permission ->
                if (grantResults[index] == PackageManager.PERMISSION_GRANTED) {
                    pb!!.grantedPermissions.add(permission)
                    pb!!.deniedPermissions.remove(permission)
                    pb!!.permanentDeniedPermissions.remove(permission)
                } else {
                    val shouldShowRationale = shouldShowRequestPermissionRationale(permission)
                    if (shouldShowRationale) {
                        // 拒绝权限
                        pb!!.deniedPermissions.add(permission)
                    } else {
                        // 拒绝并勾选了不再询问，永久拒绝
                        pb!!.deniedPermissions.remove(permission)
                        pb!!.permanentDeniedPermissions.add(permission)
                    }
                }
            }
            val allGrant = pb!!.grantedPermissions.size == pb!!.normalPermissions.size
            if (allGrant) {
                // 如果授予所有权限，直接完成当前任务
                task?.finish()
            } else {
                // 是否应该完成任务
                var shouldFinishTheTask = true
                if (pb!!.explainCallback != null && pb!!.deniedPermissions.isNotEmpty()) {
                    shouldFinishTheTask = false
                    pb!!.explainCallback!!.onExplain(
                        task!!.explainPrompt,
                        ArrayList(pb!!.deniedPermissions),
                        false
                    )
                } else if (pb!!.forwardToSettingCallback != null && pb!!.permanentDeniedPermissions.isNotEmpty()) {
                    shouldFinishTheTask = false
                    pb!!.forwardToSettingCallback!!.onForwardToSettings(
                        task!!.forwardPrompt,
                        ArrayList(pb!!.permanentDeniedPermissions)
                    )
                }
                // 如果未调用 onExplainRequestReason 或 onForwardToSetting，则直接完成任务。
                // 或者如果调用了 onExplainRequestReason 或 onForwardToSetting，
                // 但并未在回调中调用内置的dialog，则也直接完成任务。
                if (shouldFinishTheTask || !pb!!.showDialogCalled) {
                    task!!.finish()
                }
                pb!!.showDialogCalled = false
            }
        }
    }

    /**
     * 处理ACCESS_BACKGROUND_LOCATION权限请求的结果。
     */
    private fun onRequestBackgroundLocationPermissionResult() {
        if (checkNull()) return
        if (FPermission.isGranted(
                requireActivity(),
                RequestBackgroundLocationPermission.ACCESS_BACKGROUND_LOCATION
            )
        ) {
            pb!!.grantedPermissions.add(RequestBackgroundLocationPermission.ACCESS_BACKGROUND_LOCATION)
            pb!!.deniedPermissions.remove(RequestBackgroundLocationPermission.ACCESS_BACKGROUND_LOCATION)
            pb!!.permanentDeniedPermissions.remove(RequestBackgroundLocationPermission.ACCESS_BACKGROUND_LOCATION)
            task?.finish()
        } else {
            // 是否应该完成任务
            var shouldFinishTheTask = true
            val shouldShowRationale =
                shouldShowRequestPermissionRationale(RequestBackgroundLocationPermission.ACCESS_BACKGROUND_LOCATION)
            if (pb!!.explainCallback != null && shouldShowRationale) {
                shouldFinishTheTask = false
                pb!!.explainCallback!!.onExplain(
                    task!!.explainPrompt,
                    listOf(RequestBackgroundLocationPermission.ACCESS_BACKGROUND_LOCATION),
                    false
                )
            } else if (pb!!.forwardToSettingCallback != null && !shouldShowRationale) {
                shouldFinishTheTask = false
                pb!!.forwardToSettingCallback!!.onForwardToSettings(
                    task!!.forwardPrompt,
                    listOf(
                        RequestBackgroundLocationPermission.ACCESS_BACKGROUND_LOCATION
                    )
                )
            }
            // 如果未调用 onExplainRequestReason 或 onForwardToSetting，则直接完成任务。
            // 或者如果调用了 onExplainRequestReason 或 onForwardToSetting，
            // 但并未在回调中调用内置的dialog，则也直接完成任务。
            if (shouldFinishTheTask || !pb!!.showDialogCalled) {
                task!!.finish()
            }
            pb!!.showDialogCalled = false
        }
    }

    /**
     * 处理 SYSTEM_ALERT_WINDOW 权限请求的结果。
     */
    private fun onRequestSystemAlertWindowPermissionResult() {
        if (checkNull()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(requireContext())) {
                task!!.finish()
            } else if (pb!!.explainCallback != null) {
                pb!!.explainCallback!!.onExplain(
                    task!!.explainPrompt,
                    listOf(Manifest.permission.SYSTEM_ALERT_WINDOW),
                    false
                )
            }
        } else {
            task!!.finish()
        }
    }

    /**
     * 处理 WRITE_SETTINGS 权限请求的结果。
     */
    private fun onRequestWriteSettingsPermissionNow() {
        if (checkNull()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.System.canWrite(requireContext())) {
                task!!.finish()
            } else if (pb!!.explainCallback != null) {
                pb!!.explainCallback!!.onExplain(
                    task!!.explainPrompt,
                    listOf(Manifest.permission.WRITE_SETTINGS),
                    false
                )
            }
        } else {
            task!!.finish()
        }
    }

    /**
     * 处理 MANAGE_EXTERNAL_STORAGE 权限请求的结果。
     */
    private fun onRequestManageExternalStoragePermissionResult() {
        if (checkNull()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                task!!.finish()
            } else if (pb!!.explainCallback != null) {
                pb!!.explainCallback!!.onExplain(
                    task!!.explainPrompt,
                    listOf(Manifest.permission.MANAGE_EXTERNAL_STORAGE),
                    false
                )
            }
        } else {
            task!!.finish()
        }
    }

    /**
     * 在某些设备上，在不可预测的情况下（例如GC），PermissionBuilder和BaseTask可能会变为空。
     * 它们此时不应为null，因此在这种情况下我们什么也不能做。
     *
     * @return PermissionBuilder和BaseTask是否有为null的情况。如果没有，我们就不应该再做任何逻辑了。
     */
    private fun checkNull(): Boolean {
        return pb == null || task == null
    }
}