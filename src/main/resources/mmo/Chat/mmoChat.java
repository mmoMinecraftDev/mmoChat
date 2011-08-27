/*
 * This file is part of mmoMinecraft (http://code.google.com/p/mmo-minecraft/).
 * 
 * mmoMinecraft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package mmo.Chat;

import java.util.Set;
import mmo.Core.mmo;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Wolf;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.getspout.spoutapi.event.spout.SpoutCraftEnableEvent;
import org.getspout.spoutapi.event.spout.SpoutListener;

public class mmoChat extends JavaPlugin {

	protected static Server server;
	protected static PluginManager pm;
	protected static PluginDescriptionFile description;
	protected static mmo mmo;

	@Override
	public void onEnable() {
		server = getServer();
		pm = server.getPluginManager();
		description = getDescription();

		mmo = mmo.create(this);
		mmo.mmoChat = true;
		mmo.setPluginName("Chat");

		mmo.log("loading " + description.getFullName());

		mmo.cfg.getBoolean("auto_update", true);
		mmo.cfg.getString("default_channel", "Chat");

		mmo.cfg.getBoolean("channel.Chat.enabled", true);
		mmo.cfg.getBoolean("channel.Chat.command", true);
		mmo.cfg.getBoolean("channel.Chat.log", true);
		mmo.cfg.getString("channel.Chat.filters", "Server");

		mmo.cfg.getBoolean("channel.Shout.enabled", true);
		mmo.cfg.getBoolean("channel.Shout.command", true);
		mmo.cfg.getBoolean("channel.Shout.log", true);
		mmo.cfg.getString("channel.Shout.filters", "World");

		mmo.cfg.getBoolean("channel.Yell.enabled", true);
		mmo.cfg.getBoolean("channel.Yell.command", true);
		mmo.cfg.getBoolean("channel.Yell.log", true);
		mmo.cfg.getString("channel.Yell.filters", "Yell");

		mmo.cfg.getBoolean("channel.Say.enabled", true);
		mmo.cfg.getBoolean("channel.Say.command", true);
		mmo.cfg.getBoolean("channel.Say.log", true);
		mmo.cfg.getString("channel.Say.filters", "Say");

		mmo.cfg.getBoolean("channel.Party.enabled", false);
		mmo.cfg.getBoolean("channel.Party.command", false);
		mmo.cfg.getBoolean("channel.Party.log", false);
		mmo.cfg.getString("channel.Party.filters", "Party");

//		mmo.cfg.getBoolean("channel.Tell.enabled", true);
//		mmo.cfg.getBoolean("channel.Tell.command", true);
//		mmo.cfg.getBoolean("channel.Tell.log", false);
//		mmo.cfg.getString("channel.Tell.filters", "Tell");

//		mmo.cfg.getBoolean("channel.Reply.enabled", true);
//		mmo.cfg.getBoolean("channel.Reply.command", true);
//		mmo.cfg.getBoolean("channel.Reply.log", false);
//		mmo.cfg.getString("channel.Reply.filters", "Reply");

		mmo.cfg.save();

		mmoPlayerListener mpl = new mmoPlayerListener();
		pm.registerEvent(Type.PLAYER_CHAT, mpl, Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_COMMAND_PREPROCESS, mpl, Priority.Normal, this);

		Chat.addFilter(new ChannelDisabled());
		Chat.addFilter(new ChannelSay());
		Chat.addFilter(new ChannelYell());
		Chat.addFilter(new ChannelWorld());
		Chat.addFilter(new ChannelServer());
//		Chat.addFilter(new ChannelTell());
//		Chat.addFilter(new ChannelReply());

		for (String channel : mmo.cfg.getKeys("channel")) {
			Chat.addChannel(channel);
		}
	}

	@Override
	public void onDisable() {
		mmo.log("Disabled " + description.getFullName());
		mmo.autoUpdate();
		mmo.mmoChat = false;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return false;
		}
		Player player = (Player) sender;
		if (command.getName().equalsIgnoreCase("channel")) {
			String channel;
			if (args.length > 0 && (channel = Chat.findChannel(args[0])) != null) {
				if (args.length > 1) {
					if (args[1].equalsIgnoreCase("hide")) {
						Chat.hideChannel(player, channel);
						return true;
					} else if (args[1].equalsIgnoreCase("show")) {
						Chat.hideChannel(player, channel);
						return true;
					}
				} else {
					if (Chat.setChannel(player, channel)) {
						mmo.sendMessage(player, "Channel changed to %s", Chat.getChannel(player));
					} else {
						mmo.sendMessage(player, "Unknown channel");
					}
					return true;
				}
			}
		}
		return false;
	}

	public class mmoPlayerListener extends PlayerListener {

		@Override
		public void onPlayerChat(PlayerChatEvent event) {
			if (Chat.doChat(null, event.getPlayer(), event.getMessage())) {
				event.setCancelled(true);
			}
		}

		@Override
		public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
			String message = event.getMessage();
			int endIndex = message.indexOf(" ");
			if (endIndex == -1) {
				endIndex = message.length();
			}
			String channel = message.substring(1, endIndex);
			if (channel.equalsIgnoreCase("me")
					  && Chat.doChat(null, event.getPlayer(), event.getMessage())) {
				event.setCancelled(true);
			} else if ((channel = Chat.findChannel(channel)) != null
					  && mmo.cfg.getBoolean("channel." + channel + ".command", true)
					  && Chat.doChat(channel, event.getPlayer(), message.substring(1 + channel.length()).trim())) {
				event.setCancelled(true);
			}
		}
	}
}
