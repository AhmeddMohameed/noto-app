package com.example.data.repository

import com.example.data.local.dao.FolderDao
import com.example.data.local.entity.FolderEntity
import com.example.domain.model.Folder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface FolderRepository {
    fun getAllFolders(): Flow<List<Folder>>
    fun getFolderById(id: Long): Flow<Folder?>
    suspend fun insertFolder(folder: Folder): Long
    suspend fun updateFolder(folder: Folder)
    suspend fun deleteFolder(folder: Folder)
    suspend fun deleteFolderById(id: Long)
}

class FolderRepositoryImpl(
    private val folderDao: FolderDao
) : FolderRepository {

    override fun getAllFolders(): Flow<List<Folder>> {
        return folderDao.getAllFolders().map { list -> list.map { it.toDomain() } }
    }

    override fun getFolderById(id: Long): Flow<Folder?> {
        return folderDao.getFolderById(id).map { it?.toDomain() }
    }

    override suspend fun insertFolder(folder: Folder): Long {
        return folderDao.insertFolder(FolderEntity.fromDomain(folder))
    }

    override suspend fun updateFolder(folder: Folder) {
        folderDao.updateFolder(FolderEntity.fromDomain(folder))
    }

    override suspend fun deleteFolder(folder: Folder) {
        folderDao.deleteFolder(FolderEntity.fromDomain(folder))
    }

    override suspend fun deleteFolderById(id: Long) {
        folderDao.deleteFolderById(id)
    }
}
