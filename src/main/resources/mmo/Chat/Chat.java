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
import java.util.Set;
import mmo.Core.ArrayListString;
import mmo.Core.mmo;
import org.bukkit.entity.Player;

public class Chat {

	private final static ArrayListString channelList = new ArrayListString();
	private final static HashMap<String, String> playerChannel = new HashMap<String, String>();
	private final static HashMap<String, ArrayListString> playerHidden = new HashMap<String, ArrayListString>();
	protected static mmo mmo;

	public static void addChannel(String name) {
		if (!channelList.contains(name)) {
			channelList.add(name);
		}
	}

	public static boolean doChat(String channel, Player from, String message) {
		if (channel == null) {
			channel = getChannel(from);
		}
		ArrayListString check = new ArrayListString();
		channel = channelList.get(channel);
		boolean me = false, found = false;
		if (mmo.firstWord(message).equalsIgnoreCase("/me")) {
			message = mmo.removeFirstWord(message);
			me = true;
		}
		if (message.isEmpty()) {
			setChannel(from, channel);
			mmo.sendMessage(from, "Channel changed to %s", channel);
			return true;
		}
		String format = mmo.cfg.getString("channel." + channel + ".format" + (me ? "me" : ""), 
				  mmo.cfg.getString("default.format" + (me ? "me" : ""), me ? "[%1$s] %2$s*&f %3$s %4$s" : "[%1$s] %2$s%3$s&f: %4$s"));
		ArrayListString filters = new ArrayListString();
		for (String filter : Arrays.asList(mmo.cfg.getString("channel." + channel + ".filters", "Server").split(","))) {
			filters.add(filter);
		}
		mmoChatEventEvent event = new mmoChatEventEvent(from, filters, format, message);
		mmoChat.pm.callEvent(event);
		Set<Player> recipients = event.getRecipients();
		if (recipients.isEmpty()) {
			mmo.sendMessage(from, "You seem to be talking to yourself...");
		} else {
			for (Player to : recipients) {
				String msg = event.getMessage(to);
				if (msg != null && !msg.isEmpty()) {
					mmo.sendMessage(false, to, format, channel, mmo.getColor(to, from), from.getName(), msg);
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
		channel = channel == null ? mmo.cfg.getString("default_channel", "Chat") : channel;
		if (!channelList.contains(channel)) {
			mmo.log("ERROR - set 'default_channel' to one that exists...");
			return channelList.get(0);
		}
		return channelList.get(channel);
	}
}
