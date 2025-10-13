package ch.eureka.eurekapp.model.data.chat

/**
 * Data class representing a chat channel within a project.
 *
 * Chat channels provide organized communication spaces for project members. Messages are stored as
 * a subcollection under each channel.
 *
 * Note: This file was co-authored by Claude Code.
 *
 * @property channelID Unique identifier for the chat channel.
 * @property projectId ID of the project this channel belongs to.
 * @property name The name of the chat channel.
 * @property createdBy User ID of the person who created this channel.
 */
data class ChatChannel(
    val channelID: String = "",
    val projectId: String = "",
    val name: String = "",
    val createdBy: String = ""
)
