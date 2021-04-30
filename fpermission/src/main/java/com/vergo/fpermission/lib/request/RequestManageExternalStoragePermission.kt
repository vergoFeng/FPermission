package com.vergo.fpermission.lib.request

import android.os.Build
import android.os.Environment

/**
 * 请求android.permission.MANAGE_EXTERNAL_STORAGE权限任务
 *
 * @author fhj
 * @since 2021/04/28
 */
class RequestManageExternalStoragePermission internal constructor(permissionBuilder: PermissionBuilder) :
        BaseTask(permissionBuilder) {
    companion object {
        const val MANAGE_EXTERNAL_STORAGE = "android.permission.MANAGE_EXTERNAL_STORAGE";
    }

    init {
        explainPrompt = ExplainPrompt(pb, this)
        forwardPrompt = ForwardPrompt(pb, this)
    }

    override fun request() {
        if(pb.shouldRequestManageExternalStoragePermission() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                finish()
                return
            }
            if(pb.explainCallback != null) {
                pb.explainCallback!!.onExplain(explainPrompt, listOf(MANAGE_EXTERNAL_STORAGE), true)
            } else {
                // 如果没有实现explainCallback，则无法请求MANAGE_EXTERNAL_STORAGE权限，因为用户无法理解原因。
                finish()
            }
        } else {
            finish()
        }
    }

    override fun requestAgain(permissions: List<String>) {
        pb.requestManageExternalStoragePermissionNow(this)
    }
}