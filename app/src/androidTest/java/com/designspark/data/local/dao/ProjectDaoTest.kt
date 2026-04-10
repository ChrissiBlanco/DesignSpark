package com.designspark.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.designspark.data.local.AppDatabase
import com.designspark.data.local.entity.ProjectEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProjectDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: ProjectDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.projectDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insert_and_getById_returns_inserted_entity() = runTest {
        val entity = projectEntity("p1", "My App")
        dao.insert(entity)

        val result = dao.getById("p1").first()

        assertNotNull(result)
        assertEquals("p1", result?.id)
        assertEquals("My App", result?.title)
        assertEquals("Students\nMobile learning", result?.description)
    }

    @Test
    fun getById_returns_null_for_missing_id() = runTest {
        val result = dao.getById("nonexistent").first()

        assertNull(result)
    }

    @Test
    fun getAll_returns_all_inserted_entities() = runTest {
        dao.insert(projectEntity("p1", "App A", updatedAt = 2000L))
        dao.insert(projectEntity("p2", "App B", updatedAt = 1000L))

        val result = dao.getAll().first()

        assertEquals(2, result.size)
    }

    @Test
    fun getAll_orders_by_updated_at_descending() = runTest {
        dao.insert(projectEntity("p1", "App A", updatedAt = 1000L))
        dao.insert(projectEntity("p2", "App B", updatedAt = 2000L))

        val result = dao.getAll().first()

        assertEquals("p2", result[0].id)
        assertEquals("p1", result[1].id)
    }

    @Test
    fun getAll_returns_empty_list_when_no_entities() = runTest {
        val result = dao.getAll().first()

        assertEquals(emptyList<ProjectEntity>(), result)
    }

    @Test
    fun update_changes_entity_fields() = runTest {
        dao.insert(projectEntity("p1", "Original Title"))
        val updated = projectEntity("p1", "Updated Title")
        dao.update(updated)

        val result = dao.getById("p1").first()

        assertEquals("Updated Title", result?.title)
    }

    @Test
    fun updateStage1Complete_updates_flag_and_timestamp() = runTest {
        dao.insert(projectEntity("p1", "App"))
        dao.updateStage1Complete("p1", true, 5000L)

        val result = dao.getById("p1").first()

        assertEquals(true, result?.stage1Complete)
        assertEquals(5000L, result?.updatedAt)
    }

    @Test
    fun delete_removes_entity() = runTest {
        dao.insert(projectEntity("p1", "App A"))
        dao.delete("p1")

        val result = dao.getById("p1").first()

        assertNull(result)
    }

    @Test
    fun delete_does_not_affect_other_entities() = runTest {
        dao.insert(projectEntity("p1", "App A"))
        dao.insert(projectEntity("p2", "App B"))
        dao.delete("p1")

        val all = dao.getAll().first()

        assertEquals(1, all.size)
        assertEquals("p2", all[0].id)
    }

    private fun projectEntity(id: String, title: String, updatedAt: Long = 1000L) = ProjectEntity(
        id = id,
        title = title,
        description = "Students\nMobile learning",
        createdAt = 1000L,
        updatedAt = updatedAt,
        stage1Complete = false
    )
}
