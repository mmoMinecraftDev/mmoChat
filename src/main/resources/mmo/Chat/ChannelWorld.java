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

public class ChannelWorld implements ChatFilter {

	@Override
	public String getName() {
		return "Server";
	}

	@Override
	public Collection<Player> getRecipients(Player from, String message) {
		return from.getWorld().getPlayers();
	}

	@Override
	public String checkRecipient(Player from, Player to, String message) {
		return from.getWorld() == to.getWorld() ? message : null;
	}
}
