package com.marcobrenes.githubtrending.presentation.browse

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.marcobrenes.githubtrending.domain.interactor.bookmark.BookmarkProject
import com.marcobrenes.githubtrending.domain.interactor.bookmark.UnbookmarkProject
import com.marcobrenes.githubtrending.domain.interactor.browse.GetProjects
import com.marcobrenes.githubtrending.domain.model.Project
import com.marcobrenes.githubtrending.presentation.BrowseProjectsViewModel
import com.marcobrenes.githubtrending.presentation.mapper.ProjectViewMapper
import com.marcobrenes.githubtrending.presentation.model.ProjectView
import com.marcobrenes.githubtrending.presentation.state.ResourceState
import com.marcobrenes.githubtrending.presentation.test.factory.DataFactory
import com.marcobrenes.githubtrending.presentation.test.factory.ProjectFactory
import com.nhaarman.mockitokotlin2.*
import io.reactivex.observers.DisposableObserver
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Captor
import kotlin.test.assertEquals

@RunWith(JUnit4::class)
class BrowseProjectsViewModelTest {

    @get:Rule val instantTaskExecutorRule = InstantTaskExecutorRule()
    private var getProjects: GetProjects = mock()
    private var bookmarkProject: BookmarkProject = mock()
    private var unbookmarkProject: UnbookmarkProject = mock()
    private var projectMapper: ProjectViewMapper = mock()

    private var projectViewModel = BrowseProjectsViewModel(getProjects,
            bookmarkProject, unbookmarkProject, projectMapper)

    @Captor private val captor = argumentCaptor<DisposableObserver<List<Project>>>()

    @Test fun fetchProjectsExecutesUseCase() {
        projectViewModel.fetchProjects()
        verify(getProjects, times(1)).execute(any(), eq(null))
    }

    @Test fun fetchProjectsReturnsSuccess() {
        val projects = ProjectFactory.makeProjectList(2)
        val projectViews = ProjectFactory.makeProjectViewList(2)
        stubProjectMapperMapToView(projectViews[0], projects[0])
        stubProjectMapperMapToView(projectViews[1], projects[1])
        projectViewModel.fetchProjects()
        verify(getProjects).execute(captor.capture(), eq(null))
        captor.firstValue.onNext(projects)
        assertEquals(ResourceState.SUCCESS, projectViewModel.getProjects().value?.status)
    }

    @Test fun fetchProjectsReturnsData() {
        val projects = ProjectFactory.makeProjectList(2)
        val projectViews = ProjectFactory.makeProjectViewList(2)
        stubProjectMapperMapToView(projectViews[0], projects[0])
        stubProjectMapperMapToView(projectViews[1], projects[1])
        projectViewModel.fetchProjects()
        verify(getProjects).execute(captor.capture(), eq(null))
        captor.firstValue.onNext(projects)
        assertEquals(projectViews, projectViewModel.getProjects().value?.data)
    }

    @Test fun fetchProjectsReturnsError() {
        projectViewModel.fetchProjects()
        verify(getProjects).execute(captor.capture(), eq(null))
        captor.firstValue.onError(RuntimeException())
        assertEquals(ResourceState.ERROR, projectViewModel.getProjects().value?.status)
    }

    @Test fun fetchProjectsReturnsMessageForError() {
        val errorMessage = DataFactory.randomString()
        projectViewModel.fetchProjects()
        verify(getProjects).execute(captor.capture(), eq(null))
        captor.firstValue.onError(RuntimeException(errorMessage))
        assertEquals(errorMessage, projectViewModel.getProjects().value?.message)
    }

    private fun stubProjectMapperMapToView(projectView: ProjectView, project: Project) {
        whenever(projectMapper.mapToView(project)) doReturn projectView
    }
}
