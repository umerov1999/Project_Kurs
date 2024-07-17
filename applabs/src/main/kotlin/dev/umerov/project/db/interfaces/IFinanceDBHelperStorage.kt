package dev.umerov.project.db.interfaces

import dev.umerov.project.model.main.labs.FinanceBalance
import dev.umerov.project.model.main.labs.FinanceOperation
import dev.umerov.project.model.main.labs.FinanceWallet
import kotlinx.coroutines.flow.Flow

interface IFinanceDBHelperStorage {
    fun getWallets(isCreditCard: Boolean, id: Long? = null): Flow<List<FinanceWallet>>
    fun updateWallet(wallet: FinanceWallet): Flow<Boolean>
    fun deleteWallet(id: Long): Flow<Boolean>
    fun getOperations(ownerId: Long): Flow<ArrayList<FinanceOperation>>
    fun updateOperation(operation: FinanceOperation): Flow<Boolean>
    fun deleteOperation(id: Long): Flow<Boolean>

    fun fetchBalance(): Flow<FinanceBalance>
}