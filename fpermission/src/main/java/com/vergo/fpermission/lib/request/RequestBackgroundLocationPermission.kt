package com.vergo.fpermission.lib.request

import android.Manifest
import android.os.Build
import com.vergo.fpermission.lib.FPermission

/**
 * Android R上请求ACCESS_BACKGROUND_LOCATION权限任务
 *
 * @author fhj
 * @since 2021/04/28
 */
class RequestBackgroundLocationPermission internal constructor(permissionBuilder: PermissionBuilder) :
    BaseTask(permissionBuilder) {

    companion object {
        const val ACCESS_BACKGROUND_LOCATION = "android.permission.ACCESS_BACKGROUND_LOCATION"
    }

    init {
        explainPrompt = ExplainPrompt(pb, this)
        forwardPrompt = ForwardPrompt(pb, this)
    }

    override fun request() {
        if(pb.shouldRequestBackgroundLocationPermission()) {
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                // 如果应用程序在Android10下运行，则没有ACCESS_BACKGROUND_LOCATION权限。
                // 将其从请求列表中删除，但会将其作为拒绝权限添加到请求回调中。
                pb.specialPermissions.remove(ACCESS_BACKGROUND_LOCATION)
                pb.permissionsWontRequest.add(ACCESS_BACKGROUND_LOCATION)
            }
            if(FPermission.isGranted(pb.activity!!, ACCESS_BACKGROUND_LOCATION)) {
                finish()
                return
            }
            val accessFindLocationGranted = FPermission.isGranted(pb.activity!!, Manifest.permission.ACCESS_FINE_LOCATION)
            val accessCoarseLocationGranted = FPermission.isGranted(pb.activity!!, Manifest.permission.ACCESS_COARSE_LOCATION)
            if(accessFindLocationGranted || accessCoarseLocationGranted) {
                if(pb.explainCallback != null) {
                    pb.explainCallback!!.onExplain(explainPrompt, listOf(ACCESS_BACKGROUND_LOCATION), true)
                } else {
                    pb.requestAccessBackgroundLocationNow(this)
                }
                return
            }
        }
        finish()
    }

    override fun requestAgain(permissions: List<String>) {
        pb.requestAccessBackgroundLocationNow(this)
    }
}