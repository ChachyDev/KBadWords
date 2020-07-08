package club.chachy.kbadwords.features

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User

class BadWordAction(val user: User, val message: Message, val guild: Guild)