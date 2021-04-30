package com.vergo.fpermission.lib.request

import com.vergo.fpermission.lib.dialog.BaseDialog

class ExplainPrompt(private val pb: PermissionBuilder, private val task: BaseTask) {
    @JvmOverloads
    fun showExplainDialog(
        permissions: List<String>,
        message: String,
        positiveText: String = "同意",
        negativeText: String? = "拒绝"
    ) {
        pb.showHandlerPermissionDialog(task, true, permissions, message, positiveText, negativeText)
    }

    fun showExplainDialog(baseDialog: BaseDialog) {
        pb.showHandlerPermissionDialog(task, true, baseDialog)
    }
}