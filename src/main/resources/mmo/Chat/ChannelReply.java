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
import org.bukkit.entity.Player;

public class ChannelReply implements ChatFilter {

	@Override
	public String getName() {
		return "Reply";
	}

	@Override
	public Collection<Player> getRecipients(Player from, String message) {
		String to = null;
		for (String each : ChannelTell.tells.keySet()) {
			if (ChannelTell.tells.get(each).equalsIgnoreCase(from.getName())) {
				to = each;
				break;
			}
		}
		if (to != null) {
			ChannelTell.tells.put(from.getName(), to);
		}
		return null;
	}

	@Override
	public String checkRecipient(Player from, Player to, String message) {
		String name = ChannelTell.tells.get(from.getName());
		if (name != null) {
			if (name.equalsIgnoreCase(to.getName())) {
				return message;
			} else if (from.equals(to)) {
				return "(to " + mmoChat.mmo.getColor(to, from) + name + "&f) " + message;
			}
		}
		return null;
	}
}
