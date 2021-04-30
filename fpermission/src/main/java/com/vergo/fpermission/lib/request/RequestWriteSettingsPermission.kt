package com.vergo.fpermission.lib.request

import android.Manifest
import android.os.Build
import android.provider.Settings

/**
 * 请求android.permission.WRITE_SETTINGS权限任务
 *
 * @author fhj
 * @since 2021/04/29
 */
class RequestWriteSettingsPermission internal constructor(permissionBuilder: PermissionBuilder) :
        BaseTask(permissionBuilder) {

    init {
        explainPrompt = ExplainPrompt(pb, this)
        forwardPrompt = ForwardPrompt(pb, this)
    }

    override fun request() {
        if(pb.shouldRequestSystemAlertWindowPermission()) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(pb.activity)) {
                    finish()
                    return
                }
                if(pb.explainCallback != null) {
                    pb.explainCallback!!.onExplain(explainPrompt, listOf(Manifest.permission.WRITE_SETTINGS), true)
                } else {
                    // 如果没有实现explainCallback，则无法请求 WRITE_SETTINGS 权限，因为用户无法理解原因。
                    finish()
                }
            } else {
                pb.grantedPermissions.add(Manifest.permission.WRITE_SETTINGS)
                pb.specialPermissions.remove(Manifest.permission.WRITE_SETTINGS)
                finish()
            }
        } else {
            finish()
        }
    }

    override fun requestAgain(permissions: List<String>) {
        pb.requestWriteSettingsPermissionNow(this)
    }
}