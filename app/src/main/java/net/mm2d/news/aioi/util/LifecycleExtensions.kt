/*
 * Copyright (c) 2024 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.news.aioi.util

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner

fun LifecycleOwner.doOnStop(
    block: () -> Unit,
) {
    if (!lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
        block()
        return
    }
    lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onStop(
            owner: LifecycleOwner,
        ) {
            lifecycle.removeObserver(this)
            block()
        }
    })
}
