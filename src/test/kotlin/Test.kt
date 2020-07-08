import club.chachy.kbadwords.features.kBadWords
import club.chachy.kbadwords.features.onBadWord
import dev.cubxity.libs.kdp.kdp
import dev.cubxity.libs.kdp.serialization.DefaultSerializationFactory
import kotlinx.coroutines.delay
import net.dv8tion.jda.api.JDABuilder

fun main(args: Array<String>) {
    val kdp = kdp {
        kBadWords {
            deleteMessage = true

            actionOnBadWord = onBadWord { user, message, guild ->
                if (guild != null) {
                    val msg = message.channel.sendMessage("Please don't say that word ${user.asMention}!").complete()
                    delay(5000)
                    msg.delete().complete()
                }
            }
        }

        serializationFactory = DefaultSerializationFactory()

        init()
    }

    JDABuilder.createDefault(args[args.indexOf("--token") + 1])
        .setEventManager(kdp.manager)
        .build()
}

