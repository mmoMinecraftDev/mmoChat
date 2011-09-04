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

import java.util.HashMap;
import java.util.HashSet;

import mmo.Core.MMO;
import mmo.Core.MMOChatEvent;
import mmo.Core.MMOListener;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Channels extends MMOListener {

	public static HashMap<String, String> tells = new HashMap<String, String>();

	@Override
	public void onMMOChat(MMOChatEvent event) {
		Player from = event.getPlayer();
		HashSet<Player> recipients = event.getRecipients();
		if (event.hasFilter("Disabled")) {
			recipients.clear();
		}
		if (event.hasFilter("Say")) {
			for (Player to : new HashSet<Player>(recipients)) {
				if (from.getWorld() != to.getWorld()
						  || from.getLocation().distance(to.getLocation()) > 25) {
					recipients.remove(to);
				}
			}
		}
		if (event.hasFilter("Yell")) {
			for (Player to : new HashSet<Player>(recipients)) {
				if (from.getWorld() != to.getWorld()
						  || from.getLocation().distance(to.getLocation()) > 300) {
					recipients.remove(to);
				}
			}
		}
		if (event.hasFilter("World")) {
			for (Player to : new HashSet<Player>(recipients)) {
				if (from.getWorld() != to.getWorld()) {
					recipients.remove(to);
				}
			}
		}
		if (event.hasFilter("Server")) {
			// Should really refresh the target list...
		}
		boolean isTell = event.hasFilter("Tell");
		if (isTell || event.hasFilter("Reply")) {
			Player to = MMO.server.getPlayer(
					  isTell
					  ? MMO.firstWord(event.getMessage())
					  : tells.get(from.getName()));
			if (isTell) {
				event.setMessage(MMO.removeFirstWord(event.getMessage()));
			}
			HashSet<Player> keep = new HashSet<Player>();
			if (to != null) {
				tells.put(to.getName(), from.getName());
				keep.add(from);
				keep.add(to);
				event.setFormat(to, event.getFormat().replaceAll("%2\\$s", "%2\\$s&f tells you"));
				event.setFormat(from, event.getFormat().replaceAll("%2\\$s", "You tell " + MMO.getColor(from, to) + to.getName() + ChatColor.WHITE));
			} else {
				tells.remove(from.getName());
			}
			recipients.retainAll(keep);
		}
	}
}