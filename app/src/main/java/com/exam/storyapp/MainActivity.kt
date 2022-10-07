/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.exam.storyapp.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private val binding by viewBinding(ActivityMainBinding::bind, R.id.container)
    private val viewModel by viewModels<MainViewModel>()
    private val navigation by lazy {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navHostFragment.navController
    }
    private val homeDestinationIds = setOf(R.id.homeFragment, R.id.storiesMapsFragment)
    private val authDestinationIds = setOf(R.id.loginFragment, R.id.registerFragment)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        binding.bottomNavigationView.setupWithNavController(navigation)
        navigation.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id in homeDestinationIds) {
                showBottomNav()
            } else {
                hideBottomNav()
            }
        }
        lifecycleScope.launch {
            viewModel.isLogin
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).collect {
                    if (it && authDestinationIds.contains(navigation.currentDestination?.id)) {
                        navigation.navigate(R.id.action_global_homeFragment)
                    } else if (it.not() && authDestinationIds.contains(navigation.currentDestination?.id)
                        .not()
                    ) { navigation.navigate(R.id.action_global_loginFragment) }
                }
        }
    }

    private fun hideBottomNav() {
        binding.bottomNavigationView.animate()
            .translationY(binding.bottomNavigationView.height.toFloat())
            .setDuration(resources.getInteger(R.integer.anim_duration_short).toLong())
            .setInterpolator(AccelerateInterpolator(2f))
            .withEndAction {
                binding.bottomNavigationView.visibility = View.GONE
            }
    }

    private fun showBottomNav() {
        binding.bottomNavigationView.animate()
            .translationY(0f)
            .setDuration(resources.getInteger(R.integer.anim_duration_short).toLong())
            .setInterpolator(DecelerateInterpolator(2f))
            .withEndAction {
                binding.bottomNavigationView.visibility = View.VISIBLE
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navigation.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigation.handleDeepLink(intent)
    }
}
