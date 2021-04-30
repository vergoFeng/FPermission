package com.vergo.fpermission.lib.callback

import com.vergo.fpermission.lib.request.ExplainPrompt

/**
 * 拒绝权限后的回调接口
 *
 * @author fhj
 * @since 2021/04/27
 */
interface ExplainCallback {
    fun onExplain(expainPrompt: ExplainPrompt, deniedList: List<String>, beforeRequest: Boolean)
}