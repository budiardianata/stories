/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.ui.register

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
import com.exam.storyapp.databinding.FragmentRegisterBinding
import com.exam.storyapp.domain.model.FormState
import com.exam.storyapp.domain.model.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment : Fragment(R.layout.fragment_register) {
    private val binding by viewBinding(FragmentRegisterBinding::bind)
    private val viewModel by viewModels<RegisterViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            loginButton.setOnClickListener { findNavController().navigateUp() }
            edRegisterName.doAfterTextChanged {
                viewModel.dispatchEvent(RegisterEvent.NameChange(it.toString()))
            }
            edRegisterEmail.doAfterTextChanged {
                viewModel.dispatchEvent(RegisterEvent.EmailChange(it.toString()))
            }
            edRegisterPassword.doAfterTextChanged {
                viewModel.dispatchEvent(RegisterEvent.PasswordChange(it.toString()))
            }
            registerButton.setOnClickListener {
                viewModel.dispatchEvent(RegisterEvent.Register)
            }
            playAnimation()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.formState
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.RESUMED)
                .collectLatest {
                    when (it) {
                        is FormState.Submit -> {
                            when (val state = it.submitState) {
                                is UiState.Loading -> {
                                    binding.formEnable(false)
                                }
                                is UiState.Error -> {
                                    binding.formEnable(true)
                                    requireActivity().showSnackbar(
                                        state.exception.toString(requireContext()),
                                    )
                                }
                                is UiState.Success -> {
                                    binding.formEnable(false)
                                    requireActivity().showSnackbar(
                                        state.data,
                                    )
                                    findNavController().navigateUp()
                                }
                            }
                        }
                        is FormState.Validating -> {
                            binding.registerButton.isEnabled = it.isValid
                        }
                    }
                }
        }
    }

    private fun FragmentRegisterBinding.playAnimation() {
        val durationLong = resources.getInteger(R.integer.anim_duration_long).toLong()
        val email = layEmail.fadeInAnim(durationLong)
        val password = layPassword.fadeInAnim(durationLong)
        AnimatorSet().apply {
            play(loginButton.fadeInAnim(durationLong))
                .with(registerButton.fadeInAnim(durationLong))
                .after(password)
            play(email).with(layName.fadeInAnim(durationLong)).with(password)
            play(titleRegister.fadeInAnim(500)).before(email)
            startDelay = 500
        }.start()
    }

    private fun FragmentRegisterBinding.formEnable(isEnable: Boolean) {
        registerButton.isLoading = isEnable.not()
        edRegisterEmail.isEnabled = isEnable
        edRegisterPassword.isEnabled = isEnable
        edRegisterName.isEnabled = isEnable
    }
}
