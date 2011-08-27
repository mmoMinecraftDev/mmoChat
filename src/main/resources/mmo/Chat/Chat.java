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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import mmo.Core.ArrayListString;
import org.bukkit.entity.Player;

public class Chat {

	private final static ArrayListString channelList = new ArrayListString();
	private final static HashMap<String, ChatFilter> filterList = new HashMap<String, ChatFilter>();
	private final static HashMap<String, String> playerChannel = new HashMap<String, String>();
	private final static HashMap<String, ArrayListString> playerHidden = new HashMap<String, ArrayListString>();

	public static void addChannel(String name) {
		if (!channelList.contains(name)) {
			channelList.add(name);
		}
	}

	public static void addFilter(ChatFilter filter) {
		filterList.put(filter.getName(), filter);
	}

	public static boolean doChat(String channel, Player from, String message) {
		if (channel == null) {
			channel = getChannel(from);
		}
		ArrayListString check = new ArrayListString();
		channel = channelList.get(channel);
		boolean me = false, found = false;
		int endIndex = message.indexOf(" ");
		if (endIndex == -1) {
			endIndex = message.length();
		}
		if (message.substring(0, endIndex).equalsIgnoreCase("/me")) {
			message = message.substring(endIndex).trim();
			me = true;
		}
		if (message.isEmpty()) {
			setChannel(from, channel);
			mmoChat.mmo.sendMessage(from, "Channel changed to %s", channel);
			return true;
		}
		ArrayList<Player> recipients = new ArrayList(Arrays.asList(mmoChat.mmo.server.getOnlinePlayers()));
		String filters = mmoChat.mmo.cfg.getString("channel." + channel + ".filters", "Server");
		for (String filter : Arrays.asList(filters.split(","))) {
			ChatFilter next = filterList.get(filter);
			if (next != null) {
				Collection<Player> keep = next.getRecipients(from, message);
				if (keep != null) {
					recipients.retainAll(keep);
				} else {
					check.add(filter);
				}
			} else {
				return false;
			}
		}
		if (mmoChat.mmo.cfg.getBoolean("channel." + channel + ".log", false)) {
			mmoChat.mmo.log("[%1$s] %2$s: %3$s", channel, from.getName(), message);
		}
		for (Player to : recipients) {
			if (check.isEmpty()) {
				mmoChat.mmo.sendMessage(false, to, "[%1$s] " + (me ? "%2$s*&f %3$s" : "%2$s%3$s&f:") + " %4$s", channel, mmoChat.mmo.getColor(to, from), from.getName(), message);
				found = true;
			} else {
				for (String filter : check) {
					ChatFilter next = filterList.get(filter);
					String msg = next.checkRecipient(from, to, message);
					if (msg != null && !msg.isEmpty()) {
						mmoChat.mmo.sendMessage(false, to, "[%1$s] " + (me ? "%2$s*&f %3$s" : "%2$s%3$s&f:") + " %4$s", channel, mmoChat.mmo.getColor(to, from), from.getName(), msg);
						found = true;
					}
				}
			}
		}
		if (!found) {
			mmoChat.mmo.sendMessage(from, "You seem to be talking to yourself...");
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
		if (channelList.contains(channel)) {
			playerChannel.put(player.getName(), channel);
			return true;
		}
		return false;
	}

	public static String findChannel(String channel) {
		if (channelList.contains(channel)) {
			return channelList.get(channel);
		}
		return null;
	}

	public static String getChannel(Player player) {
		String channel = playerChannel.get(player.getName());
		channel = channel == null ? mmoChat.mmo.cfg.getString("default_channel", "Chat") : channel;
		if (!channelList.contains(channel)) {
			mmoChat.mmo.log("ERROR - set 'default_channel' to one that exists...");
			return channelList.get(0);
		}
		return channelList.get(channel);
	}
}
