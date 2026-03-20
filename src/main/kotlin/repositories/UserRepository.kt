package org.delcom.repositories

import org.delcom.dao.UserDAO
import org.delcom.entities.User
import org.delcom.helpers.suspendTransaction
import org.delcom.helpers.userDAOToModel
import org.delcom.tables.UserTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import java.util.UUID

class UserRepository(private val baseUrl: String) : IUserRepository {
    override suspend fun getById(userId: String): User? = suspendTransaction {
        UserDAO
            .find { UserTable.id eq UUID.fromString(userId) }
            .limit(1)
            .map { userDAOToModel(it, baseUrl) }
            .firstOrNull()
    }

    override suspend fun getByEmail(email: String): User? = suspendTransaction {
        UserDAO
            .find { UserTable.email eq email }
            .limit(1)
            .map { userDAOToModel(it, baseUrl) }
            .firstOrNull()
    }

    override suspend fun create(user: User): String = suspendTransaction {
        val dao = UserDAO.new {
            name = user.name
            email = user.email
            password = user.password
            createdAt = user.createdAt
            updatedAt = user.updatedAt
        }
        dao.id.value.toString()
    }

    override suspend fun update(id: String, newUser: User): Boolean = suspendTransaction {
        val dao = UserDAO
            .find { UserTable.id eq UUID.fromString(id) }
            .limit(1)
            .firstOrNull()

        if (dao != null) {
            dao.name = newUser.name
            dao.email = newUser.email
            dao.password = newUser.password
            dao.photo = newUser.photo
            dao.updatedAt = newUser.updatedAt
            true
        } else false
    }

    override suspend fun delete(id: String): Boolean = suspendTransaction {
        val rowsDeleted = UserTable.deleteWhere {
            UserTable.id eq UUID.fromString(id)
        }
        rowsDeleted >= 1
    }
}