package com.vergo.fpermission.lib.request

import android.Manifest
import android.os.Build
import android.os.Environment
import android.provider.Settings
import com.vergo.fpermission.lib.FPermission

/**
 * 责任链模式
 * 请求权限任务的抽象类，并定义了重复的逻辑代码。
 *
 * @author fhj
 * @since 2021/04/27
 */
abstract class BaseTask(val pb: PermissionBuilder) {

    private var next: BaseTask? = null

    /**
     * 为explainReasonCallback提供特实例调用特定函数。
     */
    lateinit var explainPrompt: ExplainPrompt

    /**
     * 为forwardToSettingsCallback提供实例调用特定函数。
     */
    lateinit var forwardPrompt: ForwardPrompt

    /**
     * 设置下一任务
     */
    fun setNext(next: BaseTask?) {
        this.next = next
    }

    abstract fun request()
    abstract fun requestAgain(permissions: List<String>)

    /**
     * 当前任务执行结束方法
     * 判断是否有下一个任务，有就执行下一个任务，没有就将通知结果
     */
    fun finish() {
        if (next != null) {
            next!!.request()
        } else {
            val deniedList: MutableList<String> = ArrayList()
            deniedList.addAll(pb.deniedPermissions)
            deniedList.addAll(pb.permanentDeniedPermissions)
            deniedList.addAll(pb.permissionsWontRequest)
            if (pb.shouldRequestBackgroundLocationPermission()) {
                if (FPermission.isGranted(
                        pb.activity!!,
                        RequestBackgroundLocationPermission.ACCESS_BACKGROUND_LOCATION
                    )
                ) {
                    pb.grantedPermissions.add(RequestBackgroundLocationPermission.ACCESS_BACKGROUND_LOCATION)
                } else {
                    deniedList.add(RequestBackgroundLocationPermission.ACCESS_BACKGROUND_LOCATION)
                }
            }
            if (pb.shouldRequestSystemAlertWindowPermission()
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && pb.activity!!.applicationInfo.targetSdkVersion >= Build.VERSION_CODES.M
            ) {
                if (Settings.canDrawOverlays(pb.activity!!)) {
                    pb.grantedPermissions.add(Manifest.permission.SYSTEM_ALERT_WINDOW)
                } else {
                    deniedList.add(Manifest.permission.SYSTEM_ALERT_WINDOW)
                }
            }
            if (pb.shouldRequestWriteSettingsPermission()
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && pb.activity!!.applicationInfo.targetSdkVersion >= Build.VERSION_CODES.M
            ) {
                if (Settings.System.canWrite(pb.activity!!)) {
                    pb.grantedPermissions.add(Manifest.permission.WRITE_SETTINGS)
                } else {
                    deniedList.add(Manifest.permission.WRITE_SETTINGS)
                }
            }
            if (pb.shouldRequestManageExternalStoragePermission()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                    Environment.isExternalStorageManager()
                ) {
                    pb.grantedPermissions.add(RequestManageExternalStoragePermission.MANAGE_EXTERNAL_STORAGE)
                } else {
                    deniedList.add(RequestManageExternalStoragePermission.MANAGE_EXTERNAL_STORAGE)
                }
            }
            pb.requestCallback.onResult(
                deniedList.isEmpty(),
                ArrayList(pb.grantedPermissions),
                deniedList
            )
        }
    }
}