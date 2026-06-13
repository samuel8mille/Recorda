package com.samuelribeiro.recorda.data.mapper

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.samuelribeiro.recorda.data.source.local.TopicEntity
import com.samuelribeiro.recorda.domain.model.Flashcard
import com.samuelribeiro.recorda.domain.model.MemoryDeck
import com.samuelribeiro.recorda.domain.model.MindMapNode
import com.samuelribeiro.recorda.domain.model.Topic
import com.samuelribeiro.recorda.domain.model.TopicContent
import javax.inject.Inject

/** Converts between [TopicEntity] (flashcards/mind map/content/memory deck as JSON) and the [Topic] domain model. */
class TopicEntityMapper @Inject constructor(
    private val gson: Gson,
) {
    private val flashcardListType = object : TypeToken<List<Flashcard>>() {}.type
    private val mindMapNodeType = object : TypeToken<MindMapNode>() {}.type
    private val topicContentType = object : TypeToken<TopicContent>() {}.type
    private val memoryDeckType = object : TypeToken<MemoryDeck>() {}.type

    fun toDomain(entity: TopicEntity): Topic = Topic(
        id = entity.id,
        name = entity.name,
        flashcards = decodeFlashcards(entity.flashcardsJson),
        mindMap = decodeMindMap(entity.mindMapJson),
        content = decodeContent(entity.contentJson),
        memoryDeck = decodeMemoryDeck(entity.memoryCardsJson),
    )

    /** Converts a [Topic] domain model into its persistable [TopicEntity]. */
    fun toEntity(topic: Topic): TopicEntity = TopicEntity(
        id = topic.id,
        name = topic.name,
        flashcardsJson = gson.toJson(topic.flashcards),
        mindMapJson = topic.mindMap?.let { gson.toJson(it) },
        contentJson = topic.content?.let { gson.toJson(it) },
        memoryCardsJson = topic.memoryDeck?.let { gson.toJson(it) },
    )

    private fun decodeFlashcards(json: String): List<Flashcard> =
        if (json.isBlank()) emptyList() else gson.fromJson(json, flashcardListType) ?: emptyList()

    private fun decodeMindMap(json: String?): MindMapNode? =
        json?.takeIf { it.isNotBlank() }?.let { gson.fromJson(it, mindMapNodeType) }

    private fun decodeContent(json: String?): TopicContent? =
        json?.takeIf { it.isNotBlank() }?.let { gson.fromJson(it, topicContentType) }

    private fun decodeMemoryDeck(json: String?): MemoryDeck? =
        json?.takeIf { it.isNotBlank() }?.let { gson.fromJson(it, memoryDeckType) }
}
