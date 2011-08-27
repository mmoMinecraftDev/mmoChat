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
import java.util.Collection;
import org.bukkit.entity.Player;

public class ChannelSay implements ChatFilter {

	@Override
	public String getName() {
		return "Say";
	}

	@Override
	public Collection<Player> getRecipients(Player from, String message) {
		ArrayList<Player> recipients = new ArrayList<Player>();
		for (Player player : from.getWorld().getPlayers()) {
			if (from.getLocation().distance(player.getLocation()) < 25) {
				recipients.add(player);
			}
		}
		return recipients;
	}

	@Override
	public String checkRecipient(Player from, Player to, String message) {
		if (from.getWorld() == to.getWorld() && from.getLocation().distance(to.getLocation()) < 25) {
			return message;
		}
		return null;
	}
}
