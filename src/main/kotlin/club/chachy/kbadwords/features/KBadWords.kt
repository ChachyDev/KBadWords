package club.chachy.kbadwords.features

import club.chachy.kbadwords.utils.launchCoroutine
import club.minnced.jda.reactor.on
import dev.cubxity.libs.kdp.KDP
import dev.cubxity.libs.kdp.feature.KDPFeature
import dev.cubxity.libs.kdp.feature.install
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.MessageUpdateEvent

class KBadWords {
    var badWords = this::class.java
        .getResourceAsStream("/default/badwords.txt")
        .bufferedReader()
        .readText()
        .split("\n")

    var actionOnBadWord: (suspend (User, Message, Guild?) -> Unit)? = null

    var deleteMessage = true

    fun processEvent(event: MessageReceivedEvent) {
        event.message.contentRaw.split(" ").forEach {
            if (it.replace("[^a-zA-Z0-9]".toRegex(), "") in badWords) {
                if (deleteMessage) {
                    event.message.delete().complete()
                }
                if (actionOnBadWord != null) {
                    launchCoroutine("BadWord Action Coroutine") {
                        actionOnBadWord?.invoke(event.author, event.message, event.guild)
                    }
                    return@forEach
                }
            }
        }
    }

    fun processEvent(event: MessageUpdateEvent) {
        event.message.contentRaw.split(" ").forEach {
            if (it.replace("[^a-zA-Z0-9]".toRegex(), "") in badWords) {
                if (deleteMessage) {
                    event.message.delete().complete()
                }
                if (actionOnBadWord != null) {
                    launchCoroutine("BadWord Action Coroutine") {
                        actionOnBadWord?.invoke(event.author, event.message, event.guild)
                    }
                    return@forEach
                }
            }
        }
    }

    companion object : KDPFeature<KDP, KBadWords, KBadWords> {
        override val key: String = "kbadwords.features.antibadword"

        override fun install(pipeline: KDP, configure: KBadWords.() -> Unit): KBadWords {
            val feature = KBadWords().apply(configure)
            with(pipeline.manager) {
                on<MessageReceivedEvent>().subscribe { feature.processEvent(it) }
                on<MessageUpdateEvent>().subscribe { feature.processEvent(it) }
            }
            return feature
        }

    }
}

/**
 * Get or install [KBadWords] feature and apply [opt] on it
 */
fun KDP.kBadWords(opt: KBadWords.() -> Unit = {}): KBadWords = (features[KBadWords.key] as KBadWords?
    ?: install(KBadWords)).apply(opt)

fun onBadWord(function: suspend (User, Message, Guild?) -> Unit): (suspend (User, Message, Guild?) -> Unit) = function