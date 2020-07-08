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
    // Create a default list of badwords
    var badWords = this::class.java
        .getResourceAsStream("/default/badwords.txt")
        .bufferedReader()
        .readText()
        .split("\n")

    /**
     * Executed when the processor finds a bad word.
     */
    var actionOnBadWord: (suspend (User, Message, Guild?) -> Unit)? = null

    /**
     * Creates an option for message deletion
     */
    var deleteMessage = true

    /**
     * Creates an option for ignoring bots
     */
    var ignoreBots = false


    /**
     * The method to detect if it contains a word from the badWords list.
     *
     * @param event The event which contains message, author, guild and many other objects.
     * @author ChachyDev
     * @since 1.0
     */
    fun processEvent(event: MessageReceivedEvent) {
        if (ignoreBots && event.author.isBot) return
        event.message.contentRaw.split(" ").forEach {
            if (it.replace("[^a-zA-Z0-9]".toRegex(), "") in badWords) {
                if (deleteMessage) {
                    event.message.delete().complete()
                }
                if (actionOnBadWord != null) {
                    launchCoroutine("Action") {
                        actionOnBadWord?.invoke(event.author, event.message, event.guild)
                    }
                }
                return@forEach
            }
        }
    }

    /**
     * The method to detect if it contains a word from the badWords list.
     *
     * @param event The event which contains message, author, guild and many other objects.
     * @author ChachyDev
     * @since 1.0
     */
    fun processEvent(event: MessageUpdateEvent) {
        if (ignoreBots && event.author.isBot) return
        event.message.contentRaw.split(" ").forEach {
            if (it.replace("[^a-zA-Z0-9]".toRegex(), "") in badWords) {
                if (deleteMessage) {
                    event.message.delete().complete()
                }
                if (actionOnBadWord != null) {
                    launchCoroutine("Action") {
                        actionOnBadWord?.invoke(event.author, event.message, event.guild)
                    }
                }
                return@forEach
            }
        }
    }

    /**
     * The KDP Feature Object
     *
     * @author chachy
     * @since 1.0
     */
    companion object : KDPFeature<KDP, KBadWords, KBadWords> {
        override val key: String = "kbadwords.features.antibadword"

        override fun install(pipeline: KDP, configure: KBadWords.() -> Unit): KBadWords {
            val feature = KBadWords().apply(configure)
            with(pipeline.manager) {
                // Subscribe event processors
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

/**
 * Function helper that is executed in KBadWords#processEvent
 */
fun onBadWord(function: suspend (User, Message, Guild?) -> Unit): (suspend (User, Message, Guild?) -> Unit) = function