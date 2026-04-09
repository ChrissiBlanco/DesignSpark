package com.designspark.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.designspark.data.local.AppDatabase
import com.designspark.data.local.entity.GeneratedInsightEntity
import com.designspark.data.local.entity.ProjectEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GeneratedInsightDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: GeneratedInsightDao
    private lateinit var projectDao: ProjectDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.generatedInsightDao()
        projectDao = db.projectDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertAll_and_getByProjectId_returns_matching_insights() = runTest {
        projectDao.insert(projectEntity("proj-1"))
        projectDao.insert(projectEntity("proj-2"))
        dao.insertAll(listOf(
            insightEntity("i1", "proj-1", 0),
            insightEntity("i2", "proj-1", 1),
            insightEntity("i3", "proj-2", 0)
        ))

        val result = dao.getByProjectId("proj-1").first()

        assertEquals(2, result.size)
        assertTrue(result.all { it.projectId == "proj-1" })
    }

    @Test
    fun getByProjectId_returns_insights_ordered_by_order_index() = runTest {
        projectDao.insert(projectEntity("proj-1"))
        dao.insertAll(listOf(
            insightEntity("i3", "proj-1", 2),
            insightEntity("i1", "proj-1", 0),
            insightEntity("i2", "proj-1", 1)
        ))

        val result = dao.getByProjectId("proj-1").first()

        assertEquals(listOf("i1", "i2", "i3"), result.map { it.id })
    }

    @Test
    fun getByProjectId_returns_empty_list_for_unknown_project() = runTest {
        val result = dao.getByProjectId("nonexistent").first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun insertAll_with_replace_strategy_overwrites_existing() = runTest {
        projectDao.insert(projectEntity("proj-1"))
        dao.insertAll(listOf(insightEntity("i1", "proj-1", 0, type = "PERSONA")))
        dao.insertAll(listOf(insightEntity("i1", "proj-1", 0, type = "METHOD_CARD")))

        val result = dao.getByProjectId("proj-1").first()

        assertEquals(1, result.size)
        assertEquals("METHOD_CARD", result[0].type)
    }

    @Test
    fun deleteByProjectId_removes_all_matching_insights() = runTest {
        projectDao.insert(projectEntity("proj-1"))
        dao.insertAll(listOf(
            insightEntity("i1", "proj-1", 0),
            insightEntity("i2", "proj-1", 1)
        ))
        dao.deleteByProjectId("proj-1")

        val result = dao.getByProjectId("proj-1").first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun deleteByProjectId_does_not_affect_other_projects() = runTest {
        projectDao.insert(projectEntity("proj-1"))
        projectDao.insert(projectEntity("proj-2"))
        dao.insertAll(listOf(
            insightEntity("i1", "proj-1", 0),
            insightEntity("i2", "proj-2", 0)
        ))
        dao.deleteByProjectId("proj-1")

        val remaining = dao.getByProjectId("proj-2").first()

        assertEquals(1, remaining.size)
        assertEquals("i2", remaining[0].id)
    }

    @Test
    fun insights_are_cascade_deleted_when_parent_project_is_deleted() = runTest {
        projectDao.insert(projectEntity("proj-1"))
        dao.insertAll(listOf(
            insightEntity("i1", "proj-1", 0),
            insightEntity("i2", "proj-1", 1)
        ))

        projectDao.delete("proj-1")

        val result = dao.getByProjectId("proj-1").first()
        assertTrue(result.isEmpty())
    }

    private fun projectEntity(id: String) = ProjectEntity(
        id = id,
        title = "Test Project",
        userGroup = "Users",
        context = "Context",
        stage = "NOTHING",
        createdAt = 1000L,
        updatedAt = 1000L,
        status = "DRAFT",
        isSynced = false
    )

    private fun insightEntity(
        id: String,
        projectId: String,
        orderIndex: Int,
        type: String = "PERSONA"
    ) = GeneratedInsightEntity(
        id = id,
        projectId = projectId,
        type = type,
        title = "Insight $id",
        content = "{}",
        riskLevel = null,
        orderIndex = orderIndex,
        generatedAt = 1000L
    )
}
