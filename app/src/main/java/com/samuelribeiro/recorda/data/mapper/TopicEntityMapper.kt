package com.samuelribeiro.recorda.data.mapper

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.samuelribeiro.recorda.data.source.local.TopicEntity
import com.samuelribeiro.recorda.data.source.local.TopicStatus
import com.samuelribeiro.recorda.domain.model.Flashcard
import com.samuelribeiro.recorda.domain.model.Topic
import javax.inject.Inject

/** Converts between [TopicEntity] (flashcards as JSON) and the [Topic] domain model. */
class TopicEntityMapper @Inject constructor(
    private val gson: Gson,
) {
    private val flashcardListType = object : TypeToken<List<Flashcard>>() {}.type

    fun toDomain(entity: TopicEntity): Topic = Topic(
        id = entity.id,
        name = entity.name,
        flashcards = decodeFlashcards(entity.flashcardsJson),
    )

    fun toEntity(topic: Topic, status: TopicStatus = TopicStatus.DONE): TopicEntity = TopicEntity(
        id = topic.id,
        name = topic.name,
        flashcardsJson = gson.toJson(topic.flashcards),
        status = status,
    )

    private fun decodeFlashcards(json: String): List<Flashcard> =
        if (json.isBlank()) emptyList() else gson.fromJson(json, flashcardListType) ?: emptyList()
}
