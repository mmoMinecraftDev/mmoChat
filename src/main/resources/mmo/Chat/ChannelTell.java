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

import java.util.Collection;
import java.util.HashMap;
import org.bukkit.entity.Player;

public class ChannelTell implements ChatFilter {

	public static HashMap<String, String> tells = new HashMap<String, String>();

	@Override
	public String getName() {
		return "Tell";
	}

	@Override
	public Collection<Player> getRecipients(Player from, String message) {
		int endIndex = message.indexOf(" ");
		if (endIndex == -1) {
			endIndex = message.length();
		}
		String name = message.substring(0, endIndex);
		Player to = mmoChat.mmo.server.getPlayer(name);
		if (to != null) {
			tells.put(from.getName(), to.getName());
		} else {
			tells.remove(from.getName());
		}
		return null;
	}

	@Override
	public String checkRecipient(Player from, Player to, String message) {
		String name = tells.get(from.getName());
		if (name != null) {
			if (name.equalsIgnoreCase(to.getName())) {
				int endIndex = message.indexOf(" ");
				return message.substring(endIndex == -1 ? message.length() : endIndex).trim();
			} else if (from.equals(to)) {
				int endIndex = message.indexOf(" ");
				return "(to " + mmoChat.mmo.getColor(to, from) + name + "&f) " + message.substring(endIndex == -1 ? message.length() : endIndex).trim();
			}
		}
		return null;
	}
}
