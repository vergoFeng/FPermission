package com.vergo.fpermission.lib.callback

import com.vergo.fpermission.lib.request.ForwardPrompt

/**
 * 拒绝权限且勾选可不再询问后的回调接口
 *
 * @author fhj
 * @since 2021/04/27
 */
interface ForwardToSettingCallback {
    fun onForwardToSettings(forwardPrompt: ForwardPrompt, deniedList: List<String>)
}