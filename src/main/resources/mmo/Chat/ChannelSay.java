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

import java.util.HashSet;
import mmo.Core.mmoChatEvent;
import mmo.Core.mmoListener;
import org.bukkit.entity.Player;

public class ChannelSay extends mmoListener {

	@Override
	public void onMMOChat(mmoChatEvent event) {
		if (event.hasFilter("Say")) {
			Player from = event.getPlayer();
			HashSet<Player> recipients = event.getRecipients();
			for (Player to : new HashSet<Player>(recipients)) {
				if (from.getWorld() != to.getWorld()
						  || from.getLocation().distance(to.getLocation()) > 25) {
					recipients.remove(to);
				}
			}
		}
	}
}
