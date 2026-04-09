package com.designspark.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.designspark.data.local.AppDatabase
import com.designspark.data.local.entity.AnnotationEntity
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
class AnnotationDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: AnnotationDao
    private lateinit var projectDao: ProjectDao
    private lateinit var insightDao: GeneratedInsightDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.annotationDao()
        projectDao = db.projectDao()
        insightDao = db.generatedInsightDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insert_and_getByInsight_returns_annotation() = runTest {
        insertParents()
        dao.insert(annotationEntity("a1", "insight-1", "Useful observation"))

        val result = dao.getByInsight("insight-1").first()

        assertEquals(1, result.size)
        assertEquals("a1", result[0].id)
        assertEquals("Useful observation", result[0].note)
    }

    @Test
    fun getByInsight_returns_all_annotations_for_insight() = runTest {
        insertParents()
        dao.insert(annotationEntity("a1", "insight-1", "Note 1"))
        dao.insert(annotationEntity("a2", "insight-1", "Note 2"))

        val result = dao.getByInsight("insight-1").first()

        assertEquals(2, result.size)
    }

    @Test
    fun getByInsight_returns_empty_for_unknown_insight() = runTest {
        val result = dao.getByInsight("nonexistent").first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun getByInsight_does_not_return_other_insights_annotations() = runTest {
        insertParents()
        dao.insert(annotationEntity("a1", "insight-1", "Note for insight 1"))

        val result = dao.getByInsight("nonexistent-insight").first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun deleteById_removes_annotation() = runTest {
        insertParents()
        dao.insert(annotationEntity("a1", "insight-1", "Note 1"))
        dao.insert(annotationEntity("a2", "insight-1", "Note 2"))

        dao.deleteById("a1")

        val result = dao.getByInsight("insight-1").first()
        assertEquals(1, result.size)
        assertEquals("a2", result[0].id)
    }

    @Test
    fun annotations_are_cascade_deleted_when_parent_insight_is_deleted() = runTest {
        insertParents()
        dao.insert(annotationEntity("a1", "insight-1", "Note"))

        insightDao.deleteByProjectId("proj-1")

        val result = dao.getByInsight("insight-1").first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun annotations_are_cascade_deleted_when_grandparent_project_is_deleted() = runTest {
        insertParents()
        dao.insert(annotationEntity("a1", "insight-1", "Note"))

        projectDao.delete("proj-1")

        val result = dao.getByInsight("insight-1").first()
        assertTrue(result.isEmpty())
    }

    private suspend fun insertParents() {
        projectDao.insert(ProjectEntity(
            id = "proj-1",
            title = "Test Project",
            userGroup = "Users",
            context = "Context",
            stage = "NOTHING",
            createdAt = 1000L,
            updatedAt = 1000L,
            status = "DRAFT",
            isSynced = false
        ))
        insightDao.insertAll(listOf(GeneratedInsightEntity(
            id = "insight-1",
            projectId = "proj-1",
            type = "PERSONA",
            title = "Test Insight",
            content = "{}",
            riskLevel = null,
            orderIndex = 0,
            generatedAt = 1000L
        )))
    }

    private fun annotationEntity(id: String, insightId: String, note: String) = AnnotationEntity(
        id = id,
        insightId = insightId,
        note = note,
        createdAt = 1000L
    )
}
