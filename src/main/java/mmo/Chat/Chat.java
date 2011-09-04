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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import mmo.Core.ArrayListString;
import mmo.Core.MMO;
import mmo.Core.MMOChatEvent;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public class Chat {

	private final static ArrayListString channelList = new ArrayListString();
	private final static HashMap<String, String> playerChannel = new HashMap<String, String>();
	private final static HashMap<String, ArrayListString> playerHidden = new HashMap<String, ArrayListString>();
	protected static MMO mmo;

	public static void addChannel(String name) {
		if (!channelList.contains(name)) {
			channelList.add(name);
		}
	}

	public static boolean doChat(String channel, Player from, String message) {
		if (channel == null) {
			channel = getChannel(from);
		}
		channel = channelList.get(channel);
		if (!mmo.cfg.getBoolean("channel." + channel + ".enabled", true)) {
			return false;
		}
		boolean me = false;
		if (MMO.firstWord(message).equalsIgnoreCase("/me")) {
			message = MMO.removeFirstWord(message);
			me = true;
		}
		if (message.isEmpty()) {
			setChannel(from, channel);
			mmo.sendMessage(from, "Channel changed to %s", channel);
			return true;
		}
		String format = mmo.cfg.getString("channel." + channel + ".format" + (me ? "me" : ""));
		if (format == null) {
			format = mmo.cfg.getString("default.format" + (me ? "me" : ""));
		}
		if (format == null) {
			format = me ? "[%1$s] * %2$s %4$s" : "[%1$s] %2$s: %4$s";
		}
		ArrayListString filters = new ArrayListString();
		for (String filter : Arrays.asList(mmo.cfg.getString("channel." + channel + ".filters", "Server").split(","))) {
			filters.add(filter);
		}
		MMOChatEventEvent event = new MMOChatEventEvent(from, filters, format, message);
		MMOChat.pm.callEvent(event);
		Set<Player> recipients = event.getRecipients();
		if (recipients.isEmpty()) {
			mmo.sendMessage(from, "You seem to be talking to yourself...");
		} else {
			if (mmo.cfg.getBoolean("channel." + channel + ".log", false)) {
				mmo.log("[%1$s] %2$s: %3$s", channel, from.getName(), message);
			}
			for (Player to : recipients) {
				String msg = event.getMessage(to);
				if (msg != null && !msg.isEmpty()) {
					String fmt = event.getFormat(to);
					mmo.sendMessage(false, to, fmt,
							  channel,
							  MMO.getColor(to, from) + from.getName() + ChatColor.WHITE,
							  MMO.getColor(from, to) + to.getName() + ChatColor.WHITE,
							  msg);
				}
			}
		}
		return true;
	}

	public static boolean hideChannel(Player player, String channel) {
		if (channelList.contains(channel)) {
			ArrayListString list = playerHidden.get(player.getName());
			if (list == null) {
				playerHidden.put(player.getName(), list = new ArrayListString());
			}
			if (!list.contains(channel)) {
				list.add(channel);
			}
			return true;
		}
		return false;
	}

	public static boolean showChannel(Player player, String channel) {
		if (channelList.contains(channel)) {
			ArrayListString list = playerHidden.get(player.getName());
			if (list == null) {
				playerHidden.put(player.getName(), list = new ArrayListString());
			}
			if (list.contains(channel)) {
				list.remove(channel);
			}
			return true;
		}
		return false;
	}

	public static boolean setChannel(Player player, String channel) {
		return setChannel(player.getName(), channel);
	}

	public static boolean setChannel(String name, String channel) {
		if (channelList.contains(channel)) {
			playerChannel.put(name, channel);
			ChatDB row = mmo.plugin.getDatabase().find(ChatDB.class).where().ieq("player", name).findUnique();
//			if (party.isParty() || !party.invites.isEmpty()) {
				if (row == null) {
					row = new ChatDB();
					row.setPlayer(name);
				}
				row.setChannel(channel);
				mmo.plugin.getDatabase().save(row);
//			} else if (row != null) {
//				mmo.plugin.getDatabase().delete(row);
//			}
			return true;
		}
		return false;
	}

	protected static void load() {
		try {
			for (ChatDB row : mmo.plugin.getDatabase().find(ChatDB.class).setAutofetch(true).findList()) {
				playerChannel.put(row.getPlayer(), row.getChannel());
			}
		} catch (Exception e) {
		}
	}

	public static String findChannel(String channel) {
		if (channelList.contains(channel)) {
			return channelList.get(channel);
		}
		return null;
	}

	public static String getChannel(Player player) {
		String channel = playerChannel.get(player.getName());
		channel = channel == null ? mmo.cfg.getString("default_channel", "Chat") : channel;
		if (!channelList.contains(channel)) {
			mmo.log("ERROR - set 'default_channel' to one that exists...");
			return channelList.get(0);
		}
		return channelList.get(channel);
	}

	private static class MMOChatEventEvent extends Event implements MMOChatEvent {

		private static final long serialVersionUID = -6501777115289445148L;
		
		private final ArrayListString filters;
		private final HashMap<Player, String> messages = new HashMap<Player, String>();
		private final HashMap<Player, String> formats = new HashMap<Player, String>();
		private final HashSet<Player> recipients;
		protected Player player;
		private String message;
		private String format;
		private boolean cancel = false;

		public MMOChatEventEvent(final Player player, final ArrayListString filters, final String format, final String message) {
			super("mmoChatEvent");
			this.recipients = new HashSet<Player>(Arrays.asList(player.getServer().getOnlinePlayers()));
			this.player = player;
			this.format = format;
			this.message = message;
			this.filters = filters;
		}

		@Override
		public boolean hasFilter(String filter) {
			return filters.contains(filter);
		}

		@Override
		public void setMessage(String message) {
			this.message = message;
		}

		@Override
		public void setMessage(Player player, String message) {
			messages.put(player, message);
		}

		@Override
		public String getMessage() {
			return message;
		}

		@Override
		public String getMessage(Player player) {
			if (messages.containsKey(player)) {
				return messages.get(player);
			}
			return getMessage();
		}

		@Override
		public void setFormat(String format) {
			this.format = format;
		}

		@Override
		public void setFormat(Player player, String format) {
			formats.put(player, format);
		}

		@Override
		public String getFormat() {
			return format;
		}

		@Override
		public String getFormat(Player player) {
			if (formats.containsKey(player)) {
				return formats.get(player);
			}
			return getFormat();
		}

		@Override
		public HashSet<Player> getRecipients() {
			return messages.isEmpty() ? recipients : new HashSet<Player>(messages.keySet());
		}

		@Override
		public boolean isCancelled() {
			return cancel;
		}

		@Override
		public void setCancelled(boolean cancel) {
			this.cancel = cancel;
		}

		@Override
		public Player getPlayer() {
			return player;
		}
	}
}