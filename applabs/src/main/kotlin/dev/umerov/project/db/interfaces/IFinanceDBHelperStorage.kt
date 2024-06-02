package dev.umerov.project.db.interfaces

import dev.umerov.project.model.main.labs.FinanceBalance
import dev.umerov.project.model.main.labs.FinanceOperation
import dev.umerov.project.model.main.labs.FinanceWallet
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

interface IFinanceDBHelperStorage {
    fun getWallets(isCreditCard: Boolean, id: Long? = null): Single<List<FinanceWallet>>
    fun updateWallet(wallet: FinanceWallet): Completable
    fun deleteWallet(id: Long): Completable
    fun getOperations(ownerId: Long): Single<ArrayList<FinanceOperation>>
    fun updateOperation(operation: FinanceOperation): Completable
    fun deleteOperation(id: Long): Completable

    fun fetchBalance(): Single<FinanceBalance>
}