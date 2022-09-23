/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.exam.storyapp.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
internal class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<MainViewModel>()
    private val navigation by lazy { findNavController(R.id.nav_host_fragment) }
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lifecycleScope.launch {
            viewModel.isLogin.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).collect {
                val unAuth = arrayListOf(R.id.loginFragment, R.id.registerFragment)
                if (it && unAuth.contains(navigation.currentDestination?.id)) {
                    navigation.navigate(R.id.action_global_homeFragment)
                } else if (it.not() && unAuth.contains(navigation.currentDestination?.id).not()) {
                    navigation.navigate(R.id.action_global_loginFragment)
                }
            }
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
