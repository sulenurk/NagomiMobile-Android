package com.sklabs.nagomi

object AppRuntimeState {
    @Volatile
    var hasUiProcess: Boolean = false
}
