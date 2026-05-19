package com.echo.app.data.model

data class EchoEmoji(val emoji: String, val label: String)

val ECHO_EMOJIS = listOf(
    EchoEmoji("😊", "开心"),
    EchoEmoji("😌", "平静"),
    EchoEmoji("🥰", "美好"),
    EchoEmoji("😢", "难过"),
    EchoEmoji("😤", "生气"),
    EchoEmoji("🥱", "累了"),
    EchoEmoji("😰", "焦虑"),
    EchoEmoji("🤯", "崩溃"),
    EchoEmoji("😶", "放空"),
    EchoEmoji("💪", "充实"),
    EchoEmoji("🫠", "麻木"),
    EchoEmoji("😴", "困了"),
)
