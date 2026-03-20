package org.delcom.repositories

import org.delcom.dao.ReviewDAO
import org.delcom.entities.Review
import org.delcom.helpers.reviewDAOToModel
import org.delcom.helpers.suspendTransaction
import org.delcom.tables.ReviewTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.avg
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import java.util.UUID

class ReviewRepository : IReviewRepository {
    override suspend fun getByWisataId(wisataId: String): List<Review> = suspendTransaction {
        ReviewDAO
            .find { ReviewTable.wisataId eq UUID.fromString(wisataId) }
            .map(::reviewDAOToModel)
    }

    override suspend fun getById(reviewId: String): Review? = suspendTransaction {
        ReviewDAO
            .find { ReviewTable.id eq UUID.fromString(reviewId) }
            .limit(1)
            .map(::reviewDAOToModel)
            .firstOrNull()
    }

    override suspend fun create(review: Review): String = suspendTransaction {
        val dao = ReviewDAO.new {
            wisataId = UUID.fromString(review.wisataId)
            userId = UUID.fromString(review.userId)
            rating = review.rating
            komentar = review.komentar
            createdAt = review.createdAt
            updatedAt = review.updatedAt
        }
        dao.id.value.toString()
    }

    override suspend fun update(userId: String, reviewId: String, newReview: Review): Boolean = suspendTransaction {
        val dao = ReviewDAO
            .find {
                (ReviewTable.id eq UUID.fromString(reviewId)) and
                        (ReviewTable.userId eq UUID.fromString(userId))
            }
            .limit(1)
            .firstOrNull()

        if (dao != null) {
            dao.rating = newReview.rating
            dao.komentar = newReview.komentar
            dao.updatedAt = newReview.updatedAt
            true
        } else false
    }

    override suspend fun delete(userId: String, reviewId: String): Boolean = suspendTransaction {
        val rowsDeleted = ReviewTable.deleteWhere {
            (ReviewTable.id eq UUID.fromString(reviewId)) and
                    (ReviewTable.userId eq UUID.fromString(userId))
        }
        rowsDeleted >= 1
    }

    override suspend fun getAverageRating(wisataId: String): Double = suspendTransaction {
        ReviewTable
            .select(ReviewTable.rating.avg())
            .where { ReviewTable.wisataId eq UUID.fromString(wisataId) }
            .firstOrNull()
            ?.get(ReviewTable.rating.avg())
            ?.toDouble() ?: 0.0
    }
}