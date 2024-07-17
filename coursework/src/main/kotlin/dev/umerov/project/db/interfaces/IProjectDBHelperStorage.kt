package dev.umerov.project.db.interfaces

import dev.umerov.project.model.db.CoinOperation
import dev.umerov.project.model.db.CoinOperationType
import dev.umerov.project.model.db.Register
import dev.umerov.project.model.db.RegisterType
import kotlinx.coroutines.flow.Flow

interface IProjectDBHelperStorage {
    fun addOperation(operation: CoinOperation): Flow<Boolean>
    fun updateOperation(operation: CoinOperation): Flow<Boolean>
    fun removeOperation(dbId: Long): Flow<Boolean>

    fun fetchRegister(@RegisterType type: Int): Flow<Register>
    fun fetchCoinOperations(@CoinOperationType type: Int): Flow<ArrayList<CoinOperation>>
    fun fetchCoinOperationsAllForBackup(): Flow<ArrayList<CoinOperation>>
}