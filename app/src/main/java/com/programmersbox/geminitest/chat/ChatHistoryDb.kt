package com.programmersbox.geminitest.chat

import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.TextPart
import com.google.ai.client.generativeai.type.asTextOrNull
import io.realm.kotlin.MutableRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.asFlow
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmUUID
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlinx.coroutines.flow.mapNotNull

class HistoryList : RealmObject {
    var historyList = realmListOf<History>()
}

class History : RealmObject {
    @PrimaryKey
    var uuid: RealmUUID = RealmUUID.random()
    var messageList = realmListOf<MessageItem>()
}

class MessageItem : RealmObject {
    var role: String? = null
    var message: String = ""
    var timestamp: Long = System.currentTimeMillis()
}

class ChatHistoryDb(
    name: String = Realm.DEFAULT_FILE_NAME,
) {
    private val realm by lazy {
        Realm.open(
            RealmConfiguration.Builder(
                setOf(
                    HistoryList::class,
                    History::class,
                    MessageItem::class
                )
            )
                .schemaVersion(1)
                .name(name)
                .migration({ })
                //.deleteRealmIfMigrationNeeded()
                .build()
        )
    }

    private val list = realm.initDbBlocking { HistoryList() }

    fun getHistory() = list
        .asFlow()
        .mapNotNull { it.obj?.historyList }

    suspend fun saveMessages(messageList: List<Content>) {
        realm.updateInfo<HistoryList> { history ->
            history?.historyList?.apply {
                add(
                    History().also { h ->
                        h.messageList.addAll(
                            messageList.map { content ->
                                MessageItem().apply {
                                    role = content.role
                                    message = content.parts.find { it is TextPart }?.asTextOrNull().orEmpty()
                                }
                            }
                        )
                    }
                )
            }
        }
    }
}

private suspend inline fun <reified T : RealmObject> Realm.updateInfo(crossinline block: MutableRealm.(T?) -> Unit) {
    query(T::class).first().find()?.also { info ->
        write { block(findLatest(info)) }
    }
}

private inline fun <reified T : RealmObject> Realm.initDbBlocking(crossinline default: () -> T): T {
    val f = query(T::class).first().find()
    return f ?: writeBlocking { copyToRealm(default()) }
}