package com.designspark.domain.usecase

import com.designspark.domain.model.Annotation
import com.designspark.domain.repository.ProjectRepository
import java.util.UUID
import javax.inject.Inject

class SaveAnnotationUseCase @Inject constructor(
    private val repository: ProjectRepository
) {
    suspend operator fun invoke(insightId: String, note: String): Result<Unit> = runCatching {
        require(note.isNotBlank()) { "Annotation note must not be empty" }
        repository.saveAnnotation(
            Annotation(
                id = UUID.randomUUID().toString(),
                insightId = insightId,
                note = note.trim(),
                createdAt = System.currentTimeMillis()
            )
        )
    }
}
