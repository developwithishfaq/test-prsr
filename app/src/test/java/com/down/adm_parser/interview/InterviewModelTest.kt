package com.down.adm_parser.interview

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.down.adm_parser.interview.domain.GetData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class InterviewModelTest {

    private var testDispatcher = StandardTestDispatcher()

    @Mock
    lateinit var getData: GetData

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
    }

    @Test
    fun test_loadData() = runTest {
        Mockito.`when`(getData.getStudents()).thenReturn(emptyList())
        val sut = InterviewModel(getData)
        sut.loadData()
        val result = sut.students
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}