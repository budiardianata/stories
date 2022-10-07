/*
 * Copyright (C) 2022 Budi Ardianata.
 */
package com.exam.storyapp.common.extensions

import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.ItemSnapshotList
import androidx.paging.PagingData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun <Input : Any> PagingData<Input>.collectData(differ: DiffUtil.ItemCallback<Input>): ItemSnapshotList<Input> {
    val listUpdateCallback = object : ListUpdateCallback {
        override fun onInserted(position: Int, count: Int) {}
        override fun onRemoved(position: Int, count: Int) {}
        override fun onMoved(fromPosition: Int, toPosition: Int) {}
        override fun onChanged(position: Int, count: Int, payload: Any?) {}
    }
    val asyncPagingDiffer = AsyncPagingDataDiffer(
        diffCallback = differ,
        updateCallback = listUpdateCallback,
        mainDispatcher = StandardTestDispatcher(),
        workerDispatcher = UnconfinedTestDispatcher()
    )

    // execute flow data into AsyncPagingDataDiffer
    asyncPagingDiffer.submitData(this)

    return asyncPagingDiffer.snapshot()
}
