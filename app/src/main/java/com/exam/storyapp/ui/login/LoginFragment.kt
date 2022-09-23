/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.ui.login

import android.animation.AnimatorSet
import android.os.Bundle
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.exam.storyapp.R
import com.exam.storyapp.common.extensions.fadeInAnim
import com.exam.storyapp.common.extensions.showSnackbar
import com.exam.storyapp.databinding.FragmentLoginBinding
import com.exam.storyapp.domain.model.FormState
import com.exam.storyapp.domain.model.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login) {

    private val binding by viewBinding(FragmentLoginBinding::bind)
    private val loginViewModel by viewModels<LoginViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            edLoginEmail.doAfterTextChanged {
                performEvent(LoginEvent.EmailChange(it.toString()))
            }
            edLoginPassword.doAfterTextChanged {
                performEvent(LoginEvent.PasswordChange(it.toString()))
            }
            loginButton.setOnClickListener {
                performEvent(LoginEvent.PerformLogin)
            }
            registerButton.setOnClickListener {
                findNavController().navigate(R.id.action_login_to_register)
            }
            playAnimation()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            loginViewModel.loginState
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collectLatest {
                    when (it) {
                        is FormState.Submit -> {
                            when (val state = it.submitState) {
                                UiState.Loading -> {
                                    binding.formEnable(false)
                                }
                                is UiState.Error -> {
                                    binding.formEnable(true)
                                    requireActivity().showSnackbar(
                                        state.exception.toString(requireContext()),
                                    )
                                }
                                is UiState.Success -> {
                                    binding.formEnable(true)
                                }
                            }
                        }
                        is FormState.Validating -> binding.loginButton.isEnabled = it.isValid
                    }
                }
        }
    }

    private fun FragmentLoginBinding.playAnimation() {
        val durationLong = resources.getInteger(R.integer.anim_duration_long).toLong()
        val email = layEmail.fadeInAnim(durationLong)
        val password = layPassword.fadeInAnim(durationLong)
        AnimatorSet().apply {
            play(loginButton.fadeInAnim(durationLong))
                .with(registerButton.fadeInAnim(durationLong))
                .after(password)
            play(email).with(password)
            play(titleLogin.fadeInAnim(500)).before(email)
            startDelay = 500
        }.start()
    }

    private fun FragmentLoginBinding.formEnable(isEnable: Boolean) {
        loginButton.isLoading = isEnable.not()
        edLoginEmail.isEnabled = isEnable
        edLoginPassword.isEnabled = isEnable
    }

    private fun performEvent(action: LoginEvent) {
        loginViewModel.dispatchEvent(action)
    }
}
