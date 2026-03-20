package org.delcom.repositories

import org.delcom.entities.Review

interface IReviewRepository {
    suspend fun getByWisataId(wisataId: String): List<Review>
    suspend fun getById(reviewId: String): Review?
    suspend fun create(review: Review): String
    suspend fun update(userId: String, reviewId: String, newReview: Review): Boolean
    suspend fun delete(userId: String, reviewId: String): Boolean
    suspend fun getAverageRating(wisataId: String): Double
}