package com.example.storyapp.main_story

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.recyclerview.widget.ListUpdateCallback
import com.example.storyapp.DataDummy
import com.example.storyapp.MainDispatcherRule
import com.example.storyapp.data.repository.UserModel
import com.example.storyapp.data.repository.UserRepository
import com.example.storyapp.data.repository.response.ListStoryItem
import com.example.storyapp.getOrAwaitValue
import com.example.storyapp.main_story.adapter.StoryAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class MainViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRules = MainDispatcherRule()

    @Mock
    private lateinit var userStoryRepository: UserRepository

    private val dummyStoriesResponse = DataDummy.generateDummyStories()

    @Test
    fun `getStories Not Null Return Success`() = runTest {
        val data: PagingData<ListStoryItem> = StoryPagingSource.snapshot(dummyStoriesResponse.listStory)
        val expectedStories = MutableLiveData<PagingData<ListStoryItem>>()
        expectedStories.value = data
        Mockito.`when`(userStoryRepository.getStories()).thenReturn(expectedStories)

        // Mocking the getSession function to return a valid Flow of UserModel
        val dummyUserModel = UserModel("test@example.com", "token", true)
        Mockito.`when`(userStoryRepository.getSession()).thenReturn(flowOf(dummyUserModel))

        val listStoryViewModel = MainViewModel(userStoryRepository)
        val actualStories: PagingData<ListStoryItem> = listStoryViewModel.storyResponse.getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )

        differ.submitData(actualStories)

        Assert.assertNotNull(differ.snapshot())
        Assert.assertEquals(dummyStoriesResponse.listStory, differ.snapshot())
        Assert.assertEquals(dummyStoriesResponse.listStory!!.size, differ.snapshot().size)
        // Assert that the first data item matches
        Assert.assertEquals(dummyStoriesResponse.listStory!![0], differ.snapshot()[0])
    }

    @Test
    fun `getStories Empty Data Return Zero`() = runTest {
        // Mock an empty PagingData
        val data: PagingData<ListStoryItem> = StoryPagingSource.snapshot(emptyList())
        val expectedStories = MutableLiveData<PagingData<ListStoryItem>>()
        expectedStories.value = data
        Mockito.`when`(userStoryRepository.getStories()).thenReturn(expectedStories)

        // Mocking the getSession function to return a valid Flow of UserModel
        val dummyUserModel = UserModel("test@example.com", "token", true)
        Mockito.`when`(userStoryRepository.getSession()).thenReturn(flowOf(dummyUserModel))

        val listStoryViewModel = MainViewModel(userStoryRepository)
        val actualStories: PagingData<ListStoryItem> = listStoryViewModel.storyResponse.getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )

        differ.submitData(actualStories)

        Assert.assertNotNull(differ.snapshot())
        Assert.assertTrue(differ.snapshot().isEmpty())
    }
}

class StoryPagingSource : PagingSource<Int, ListStoryItem>() {
    companion object {
        fun snapshot(items: List<ListStoryItem>?): PagingData<ListStoryItem> {
            return PagingData.from(items!!)
        }
    }
    override fun getRefreshKey(state: PagingState<Int, ListStoryItem>): Int? {
        return state.anchorPosition
    }
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ListStoryItem> {
        return LoadResult.Page(emptyList(), null, null)
    }
}

val noopListUpdateCallback = object : ListUpdateCallback {
    override fun onInserted(position: Int, count: Int) {}
    override fun onRemoved(position: Int, count: Int) {}
    override fun onMoved(fromPosition: Int, toPosition: Int) {}
    override fun onChanged(position: Int, count: Int, payload: Any?) {}
}
