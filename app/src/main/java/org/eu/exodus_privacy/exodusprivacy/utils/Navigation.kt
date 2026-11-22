package org.eu.exodus_privacy.exodusprivacy.utils

import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import org.eu.exodus_privacy.exodusprivacy.R

val animatedNavOptionsBuilder = NavOptions.Builder()
    .setEnterAnim(R.animator.fragment_enter)
    .setExitAnim(R.animator.fragment_exit)
    .setPopEnterAnim(R.animator.fragment_pop_enter)
    .setPopExitAnim(R.animator.fragment_pop_exit)

val animatedNavOptions = animatedNavOptionsBuilder.build()

fun NavController.safeNavigate(
    direction: NavDirections,
    navOptions: NavOptions = animatedNavOptions,
) {
    currentDestination?.getAction(direction.actionId)?.run {
        navigate(direction, navOptions = navOptions)
    }
}
