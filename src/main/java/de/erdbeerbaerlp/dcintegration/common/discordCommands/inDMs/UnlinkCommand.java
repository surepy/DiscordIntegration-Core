package de.erdbeerbaerlp.dcintegration.common.discordCommands.inDMs;

import de.erdbeerbaerlp.dcintegration.common.storage.Configuration;
import de.erdbeerbaerlp.dcintegration.common.storage.PlayerLinkController;
import de.erdbeerbaerlp.dcintegration.common.util.MessageUtils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.erdbeerbaerlp.dcintegration.common.util.Variables.discord_instance;

public class UnlinkCommand extends DMCommand {
    @Override
    public String getName() {
        return "unlink";
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public String getDescription() {
        return Configuration.instance().localization.commands.descriptions.unlink;
    }

    @Override
    public void execute(String[] args, MessageReceivedEvent ev) {
        MessageChannel channel = ev.getChannel();
        User author = ev.getAuthor();
        Configuration config = Configuration.instance();
        Configuration.Localization.Linking localization = config.localization.linking;
        String prefix = config.commands.prefix;

        // user is not linked to a Minecraft account.
        if (!PlayerLinkController.isDiscordLinked(author.getId())) {
            String link_method = localization.linkMethodIngame;

            if (config.linking.whitelistMode)
                link_method = localization.linkMethodWhitelist.replace("%prefix%", config.commands.prefix);

            channel.sendMessage(localization.notLinked.replace("%method%", link_method)).queue();
            return;
        }

        UUID author_uuid = PlayerLinkController.getPlayerFromDiscord(author.getId());

        @SuppressWarnings("ConstantConditions")
        final boolean result = PlayerLinkController.unlinkPlayer(author.getId(), author_uuid);

        // kick the player if in server.
        if (discord_instance.srv.getPlayers().containsKey(author_uuid)) {
            discord_instance.srv.runMcCommand("kick " + author_uuid + " " + localization.unlinked, ev);
        }

        channel.sendMessage(localization.unlinkSuccessful).queue();
    }

    @Override
    public boolean canUserExecuteCommand(User user) {
        return !Configuration.instance().linking.unlinkRequiresAdmin;
    }
}
