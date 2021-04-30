package com.vergo.fpermission.lib.request

import com.vergo.fpermission.lib.dialog.BaseDialog

class ForwardPrompt(private val pb: PermissionBuilder, private val task: BaseTask) {
    @JvmOverloads
    fun showForwardDialog(
        permissions: List<String>,
        message: String,
        positiveText: String = "同意",
        negativeText: String? = "拒绝"
    ) {
        pb.showHandlerPermissionDialog(task, false, permissions, message, positiveText, negativeText)
    }

    fun showForwardDialog(baseDialog: BaseDialog) {
        pb.showHandlerPermissionDialog(task, false, baseDialog)
    }
}