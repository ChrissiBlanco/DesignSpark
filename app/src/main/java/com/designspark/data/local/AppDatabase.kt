package com.designspark.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.designspark.data.local.dao.AnnotationDao
import com.designspark.data.local.dao.GeneratedInsightDao
import com.designspark.data.local.dao.ProjectDao
import com.designspark.data.local.entity.AnnotationEntity
import com.designspark.data.local.entity.GeneratedInsightEntity
import com.designspark.data.local.entity.ProjectEntity

@Database(
    entities = [ProjectEntity::class, GeneratedInsightEntity::class, AnnotationEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun generatedInsightDao(): GeneratedInsightDao
    abstract fun annotationDao(): AnnotationDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS annotations")
                db.execSQL("DROP TABLE IF EXISTS generated_insights")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `projects_new` (
                      `id` TEXT NOT NULL,
                      `title` TEXT NOT NULL,
                      `description` TEXT NOT NULL,
                      `created_at` INTEGER NOT NULL,
                      `updated_at` INTEGER NOT NULL,
                      `stage1_complete` INTEGER NOT NULL DEFAULT 0,
                      PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO `projects_new` (`id`, `title`, `description`, `created_at`, `updated_at`, `stage1_complete`)
                    SELECT `id`, `title`,
                      `user_group` || x'0a' || `context` || x'0a' || `stage`,
                      `created_at`, `updated_at`, 0
                    FROM `projects`
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE `projects`")
                db.execSQL("ALTER TABLE `projects_new` RENAME TO `projects`")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `generated_insights` (
                      `id` TEXT NOT NULL,
                      `project_id` TEXT NOT NULL,
                      `type` TEXT NOT NULL,
                      `title` TEXT NOT NULL,
                      `content` TEXT NOT NULL,
                      `quadrant` TEXT,
                      `order_index` INTEGER NOT NULL,
                      `generated_at` INTEGER NOT NULL,
                      PRIMARY KEY(`id`),
                      FOREIGN KEY(`project_id`) REFERENCES `projects`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_generated_insights_project_id` ON `generated_insights` (`project_id`)"
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `annotations` (
                      `id` TEXT NOT NULL,
                      `insight_id` TEXT NOT NULL,
                      `note` TEXT NOT NULL,
                      `created_at` INTEGER NOT NULL,
                      PRIMARY KEY(`id`),
                      FOREIGN KEY(`insight_id`) REFERENCES `generated_insights`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_annotations_insight_id` ON `annotations` (`insight_id`)"
                )
            }
        }
    }
}
