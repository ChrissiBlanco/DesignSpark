package com.designspark.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.designspark.data.local.entity.ProjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: ProjectEntity)

    @Update
    suspend fun update(entity: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT * FROM projects ORDER BY updated_at DESC")
    fun getAll(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id")
    fun getById(id: String): Flow<ProjectEntity?>

    @Query(
        "UPDATE projects SET stage1_complete = :complete, updated_at = :updatedAt WHERE id = :projectId"
    )
    suspend fun updateStage1Complete(projectId: String, complete: Boolean, updatedAt: Long)
}
