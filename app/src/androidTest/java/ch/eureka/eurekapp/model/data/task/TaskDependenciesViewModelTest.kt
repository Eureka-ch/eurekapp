package ch.eureka.eurekapp.model.data.task

import ch.eureka.eurekapp.model.data.project.Member
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.project.ProjectRepository
import ch.eureka.eurekapp.model.data.project.ProjectRole
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.model.data.user.UserRepository
import ch.eureka.eurekapp.model.tasks.TaskDependenciesViewModel
import ch.eureka.eurekapp.utils.FirestoreRepositoryTest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class TaskDependenciesViewModelTest {
    private class UserFakeRepository: UserRepository{
        override fun getUserById(userId: String): Flow<User?> {
            val defaultUser = User().copy(uid = userId)
            return flowOf(
                defaultUser
            )
        }

        override fun getCurrentUser(): Flow<User?> {
            return flowOf(User().copy(uid="default-user-id"))
        }

        override suspend fun saveUser(user: User): Result<Unit> {
            return Result.success(Unit)
        }

        override suspend fun updateLastActive(userId: String): Result<Unit> {
            return Result.success(Unit)
        }
    }

    private class TasksFakeRepository: TaskRepository{
        override fun getTaskById(
            projectId: String,
            taskId: String
        ): Flow<Task?> {
            val defaultTask = Task().copy(taskID = taskId, projectId = projectId)
            return flowOf(defaultTask)
        }

        override fun getTasksInProject(projectId: String): Flow<List<Task>> {
            val defaultTask1 = Task().copy(taskID = projectId + "1", projectId = projectId)
            val defaultTask2 = Task().copy(taskID = projectId + "2", projectId = projectId)
            val defaultTask3 = Task().copy(taskID = projectId + "3", projectId = projectId)

            return flowOf(listOf(defaultTask1, defaultTask2, defaultTask3))
        }

        override fun getTasksForCurrentUser(): Flow<List<Task>> {
            return flowOf(listOf())
        }

        override suspend fun createTask(task: Task): Result<String> {
            return Result.success(task.taskID)
        }

        override suspend fun updateTask(task: Task): Result<Unit> {
            return Result.success(Unit)
        }

        override suspend fun deleteTask(
            projectId: String,
            taskId: String
        ): Result<Unit> {
            return Result.success(Unit)
        }

        override suspend fun assignUser(
            projectId: String,
            taskId: String,
            userId: String
        ): Result<Unit> {
            return Result.success(Unit)
        }

        override suspend fun unassignUser(
            projectId: String,
            taskId: String,
            userId: String
        ): Result<Unit> {
            return Result.success(Unit)
        }

    }

    private class ProjectFakeRepository: ProjectRepository{
        override fun getProjectById(projectId: String): Flow<Project?> {
            val defaultProject = Project(projectId = projectId, memberIds = listOf("user1", "user2"))
            return flowOf(defaultProject)
        }

        override fun getProjectsForCurrentUser(skipCache: Boolean): Flow<List<Project>> {
            TODO("Not yet implemented")
        }

        override suspend fun createProject(
            project: Project,
            creatorId: String,
            creatorRole: ProjectRole
        ): Result<String> {
            TODO("Not yet implemented")
        }

        override suspend fun updateProject(project: Project): Result<Unit> {
            TODO("Not yet implemented")
        }

        override suspend fun deleteProject(projectId: String): Result<Unit> {
            TODO("Not yet implemented")
        }

        override fun getMembers(projectId: String): Flow<List<Member>> {
            TODO("Not yet implemented")
        }

        override suspend fun addMember(
            projectId: String,
            userId: String,
            role: ProjectRole
        ): Result<Unit> {
            TODO("Not yet implemented")
        }

        override suspend fun removeMember(
            projectId: String,
            userId: String
        ): Result<Unit> {
            TODO("Not yet implemented")
        }

        override suspend fun updateMemberRole(
            projectId: String,
            userId: String,
            role: ProjectRole
        ): Result<Unit> {
            TODO("Not yet implemented")
        }

    }


    @Test
    fun testGetDependentTasksForTaskWorks(){
        runBlocking {
            val viewModel = TaskDependenciesViewModel(
                tasksRepository = TasksFakeRepository(),
                usersRepository = UserFakeRepository(),
                projectsRepository = ProjectFakeRepository()
            )

            val dummyTask = Task(dependingOnTasks = listOf("task1", "task2", "task3"))

            //This should return three dummy tasks with id the corresponding element of the list

            val list = viewModel.getDependentTasksForTask("test-project-id", dummyTask)

            list.forEachIndexed { i, taskFlow ->
                assertEquals("test-project-id", taskFlow.first()!!.projectId)
                assertEquals("task${i+1}", taskFlow.first()!!.taskID)
            }
        }
    }

    @Test
    fun getProjectUsersWorks(){
        runBlocking {
            val viewModel = TaskDependenciesViewModel(
                tasksRepository = TasksFakeRepository(),
                usersRepository = UserFakeRepository(),
                projectsRepository = ProjectFakeRepository()
            )

            assertEquals("user1", viewModel.getProjectUsers("test-project-id")
                .first().get(0).first()!!.uid)
        }
    }

    @Test
    fun getTaskFromRepositoryWorks(){
        runBlocking {
            val viewModel = TaskDependenciesViewModel(
                tasksRepository = TasksFakeRepository(),
                usersRepository = UserFakeRepository(),
                projectsRepository = ProjectFakeRepository()
            )

            val task = viewModel.getTaskFromRepository("test-project-id", "task1")
            assertEquals("task1", task.first()!!.taskID)
        }
    }
}