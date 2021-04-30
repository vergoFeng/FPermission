package com.vergo.fpermission.lib.request

import com.vergo.fpermission.lib.FPermission
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.MutableSet
import kotlin.collections.forEach
import kotlin.collections.isNullOrEmpty

/**
 * 普通权限请求任务
 *
 * @author fhj
 * @since 2021/04/28
 */
class RequestNormalPermissions internal constructor(permissionBuilder: PermissionBuilder) :
    BaseTask(permissionBuilder) {

    init {
        explainPrompt = ExplainPrompt(pb, this)
        forwardPrompt = ForwardPrompt(pb, this)
    }

    override fun request() {
        val requestList: MutableList<String> = ArrayList()
        pb.normalPermissions.forEach {
            if(FPermission.isGranted(pb.activity!!, it)) {
                pb.grantedPermissions.add(it)
            } else {
                requestList.add(it)
            }
        }
        if(requestList.isNullOrEmpty()) {  // 所有权限都已同意
            finish()
            return
        }

        if(pb.explainReasonBeforeRequest && pb.explainCallback != null) {
            pb.explainReasonBeforeRequest = false
            pb.deniedPermissions.addAll(requestList)
            pb.explainCallback!!.onExplain(explainPrompt, requestList, true)
        } else {
            // 立即执行请求。始终请求所有权限，无论是否已授予它们，以防用户在“设置”中将其关闭。
            pb.requestNow(pb.normalPermissions, this)
        }
    }

    override fun requestAgain(permissions: List<String>) {
        val permissionsToRequestAgain: MutableSet<String> = HashSet(pb.grantedPermissions)
        permissionsToRequestAgain.addAll(permissions)
        pb.requestNow(permissionsToRequestAgain, this)
    }
}