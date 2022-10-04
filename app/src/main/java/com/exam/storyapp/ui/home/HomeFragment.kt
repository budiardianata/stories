/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.ui.home

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.exam.storyapp.MainViewModel
import com.exam.storyapp.R
import com.exam.storyapp.common.extensions.getStyledAppName
import com.exam.storyapp.common.util.Constant
import com.exam.storyapp.databinding.FragmentHomeBinding
import com.exam.storyapp.domain.model.Story
import com.exam.storyapp.ui.home.adapter.StoryAdapter
import com.exam.storyapp.ui.home.adapter.StoryLoadStateAdapter
import com.google.android.material.transition.MaterialElevationScale
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {
    private val binding by viewBinding(FragmentHomeBinding::bind)
    private val viewmodel by viewModels<HomeViewModel>()
    private val activityViewModel by activityViewModels<MainViewModel>()
    private val storyAdapter by lazy { StoryAdapter(::onStoryClicked) }
    private val navController by lazy { findNavController() }

    private val menuProvider by lazy {
        object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_main, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_language -> {
                        Intent(Settings.ACTION_LOCALE_SETTINGS).apply(::startActivity)
                        true
                    }
                    R.id.action_logout -> {
                        activityViewModel.logOut()
                        true
                    }
                    else -> menuItem.onNavDestinationSelected(navController)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setFragmentResultListener(Constant.REQ_REFRESH) { _, bundle ->
            bundle.getBoolean(Constant.KEY_REFRESH).let {
                if (it) {
                    binding.homeList.smoothScrollToPosition(0)
                    storyAdapter.refresh()
                }
            }
        }
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
        binding.apply {
            topAppBar.title = requireContext().getStyledAppName()
            homeList.run {
                setLayoutManager()
                adapter = storyAdapter.withLoadStateFooter(
                    StoryLoadStateAdapter(storyAdapter::retry),
                )
                setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
                    if (scrollY > oldScrollY) binding.fab.shrink()
                    else binding.fab.extend()
                }
            }
            swipeRefreshLayout.setOnRefreshListener { storyAdapter.refresh() }
            topAppBar.addMenuProvider(menuProvider, viewLifecycleOwner, Lifecycle.State.STARTED)
            binding.retryButton.setOnClickListener { storyAdapter.refresh() }
            fab.apply {
                setOnClickListener(::navigateToCreateStory)
            }
        }
//        viewLifecycleOwner.lifecycleScope.launch{
//            activityViewModel.isLogin.flowWithLifecycle(lifecycle, Lifecycle.State.CREATED).collect { isLogin ->
//                if (!isLogin) {
//                    findNavController().navigate(R.id.action_global_loginFragment)
//                }
//            }
//        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewmodel.stories.collect {
                        binding.swipeRefreshLayout.isRefreshing = false
                        storyAdapter.submitData(viewLifecycleOwner.lifecycle, it)
                    }
                }
                launch {
                    storyAdapter.loadStateFlow.collect { loadState ->
                        // Only show the list if refresh succeeds.
//                        val isListEmpty = loadState.refresh is LoadState.NotLoading && storyAdapter.itemCount == 0
                        val isListEmpty = (loadState.source.refresh is LoadState.NotLoading && loadState.append.endOfPaginationReached && storyAdapter.itemCount == 0) || loadState.source.refresh is LoadState.Error
                        binding.run {
                            // show empty list
                            errorMsg.isVisible = isListEmpty
                            homeList.isVisible = !isListEmpty
                            progressBar.isVisible = loadState.source.refresh is LoadState.Loading
                            retryButton.isVisible = loadState.source.refresh is LoadState.Error && storyAdapter.itemCount == 0
                            errorMsg.isVisible = retryButton.isVisible
                            if (isListEmpty) {
                                val errorState = loadState.source.append as? LoadState.Error
                                    ?: loadState.source.prepend as? LoadState.Error
                                    ?: loadState.append as? LoadState.Error
                                    ?: loadState.prepend as? LoadState.Error
                                errorState?.let { errorMsg.text = it.error.localizedMessage ?: it.error.message }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun RecyclerView.setLayoutManager() {
        layoutManager =
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                LinearLayoutManager(requireContext())
            } else {
                GridLayoutManager(requireContext(), 3)
            }
    }

    private fun onStoryClicked(extras: FragmentNavigator.Extras, story: Story) {
        exitTransition = MaterialElevationScale(false).apply {
            duration = resources.getInteger(R.integer.anim_duration_long).toLong()
            binding.fab.hide()
        }
        reenterTransition = MaterialElevationScale(true).apply {
            duration = resources.getInteger(R.integer.anim_duration_long).toLong()
            binding.fab.show()
        }
        navController.navigate(
            resId = R.id.action_home_to_detail,
            args = bundleOf(Constant.KEY_STORY to story),
            navOptions = null,
            navigatorExtras = extras,
        )
    }

    private fun navigateToCreateStory(view: View) {
        val extras = FragmentNavigatorExtras(
            view to getString(R.string.add_story_transition),
        )
        navController.navigate(R.id.action_home_to_add, null, null, extras)
    }
}
