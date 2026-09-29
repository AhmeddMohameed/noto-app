package com.example.data.repository

import com.example.data.local.dao.TagDao
import com.example.data.local.entity.TagEntity
import com.example.domain.model.Tag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface TagRepository {
    fun getAllTags(): Flow<List<Tag>>
    suspend fun insertTag(tag: Tag): Long
    suspend fun deleteTag(tag: Tag)
    suspend fun deleteTagById(id: Long)
}

class TagRepositoryImpl(
    private val tagDao: TagDao
) : TagRepository {

    override fun getAllTags(): Flow<List<Tag>> {
        return tagDao.getAllTags().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertTag(tag: Tag): Long {
        return tagDao.insertTag(TagEntity.fromDomain(tag))
    }

    override suspend fun deleteTag(tag: Tag) {
        tagDao.deleteTag(TagEntity.fromDomain(tag))
    }

    override suspend fun deleteTagById(id: Long) {
        tagDao.deleteTagById(id)
    }
}
