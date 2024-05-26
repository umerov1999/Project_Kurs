package dev.umerov.project.db.interfaces

import dev.umerov.project.model.db.CoinOperation
import dev.umerov.project.model.db.CoinOperationType
import dev.umerov.project.model.db.Register
import dev.umerov.project.model.db.RegisterType
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

interface IProjectDBHelperStorage {
    fun addOperation(operation: CoinOperation): Completable
    fun updateOperation(operation: CoinOperation): Completable
    fun removeOperation(dbId: Long): Completable

    fun fetchRegister(@RegisterType type: Int): Single<Register>
    fun fetchCoinOperations(@CoinOperationType type: Int): Single<ArrayList<CoinOperation>>
    fun fetchCoinOperationsAllForBackup(): Single<ArrayList<CoinOperation>>
}