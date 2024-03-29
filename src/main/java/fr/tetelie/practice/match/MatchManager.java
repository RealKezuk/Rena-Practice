package fr.tetelie.practice.match;

import fr.tetelie.practice.Practice;
import fr.tetelie.practice.arena.ArenaManager;
import fr.tetelie.practice.fight.FightType;
import fr.tetelie.practice.inventory.Kit;
import fr.tetelie.practice.inventory.MatchPreviewInventory;
import fr.tetelie.practice.ladder.Ladder;
import fr.tetelie.practice.player.PlayerManager;
import fr.tetelie.practice.player.PlayerSatus;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class MatchManager {

    static @Getter
    List<MatchManager> all;

    static
    {
        all = new ArrayList<>();
    }

    private UUID uuid1;
    private UUID uuid2;
    private @Setter MatchStatus matchStatus = MatchStatus.STARTING;
    private FightType fightType;
    private ArenaManager arena;
    private Ladder ladder;
    private int startTimer = 5;

    public MatchManager(MatchType matchType, UUID uuid1, UUID uuid2, FightType fightType, String ladderDisplayName){
        this.uuid1 = uuid1;
        this.uuid2 = uuid2;
        this.fightType = fightType;
        Player player1 = Bukkit.getPlayer(uuid1);
        Player player2 = Bukkit.getPlayer(uuid2);
        PlayerManager playerManager1 = PlayerManager.getPlayerManagers().get(uuid1);
        PlayerManager playerManager2 = PlayerManager.getPlayerManagers().get(uuid2);
        Ladder ladder = Ladder.getLadder(ladderDisplayName);
        this.ladder = ladder;
        ArenaManager arena = ArenaManager.getRandomArena(ladder.arenaType());
        this.arena = arena;
        playerManager1.leaveQueue();
        Bukkit.broadcastMessage("Create new match: matchtype: " + matchType.toString() +" fighttype:"+fightType.toString()+" ladder:"+ladder.name()+" arena:" + arena.getName());
        playerManager1.removePreviewInventory();
        playerManager2.removePreviewInventory();
        playerManager1.removeDuel();
        playerManager2.removeDuel();
        playerManager1.setPlayerSatus(PlayerSatus.FIGHT);
        playerManager2.setPlayerSatus(PlayerSatus.FIGHT);
        player1.teleport(arena.getLoc1());
        player2.teleport(arena.getLoc2());
        player1.setNoDamageTicks(10);
        player1.setMaximumNoDamageTicks(10);
        player2.setNoDamageTicks(10);
        player2.setMaximumNoDamageTicks(10);
        if(ladder instanceof Kit)
        {
            Kit kit = (Kit) ladder;
            playerManager1.sendKit(kit);
            playerManager2.sendKit(kit);
        }
        Practice.getInstance().fight.get(ladderDisplayName).getFightPlayer().get(fightType).add(this);
        playerManager1.setCurrentFight(this);
        playerManager2.setCurrentFight(this);

        new BukkitRunnable() {
            public void run() {
                if (matchStatus == MatchStatus.STARTING) {
                    if (startTimer == 0) {
                        player1.sendMessage("§6Duel starting against §e" + player2.getName());
                        player2.sendMessage("§6Duel starting against §e" + player1.getName());
                        player1.playSound(player1.getLocation(), Sound.FIREWORK_LARGE_BLAST, 1f, 1f);
                        player2.playSound(player2.getLocation(), Sound.FIREWORK_LARGE_BLAST, 1f, 1f);
                        matchStatus = MatchStatus.PLAYING;
                        // set match start millis
                        this.cancel();
                    } else {
                        sendGlobalMessage("§6Duel starting in §7" + startTimer + "§6 second(s)", player1, player2);
                        player1.playSound(player1.getLocation(), Sound.NOTE_PIANO, 1f, 1f);
                        player2.playSound(player2.getLocation(), Sound.NOTE_PIANO, 1f, 1f);
                        startTimer--;
                    }
                } else {
                    this.cancel();
                }
            }
        }.runTaskTimer(Practice.getInstance(), 20L, 20L);
        all.add(this);
    }

    public void end(MatchEndCause cause, Player player, PlayerManager playerManager)
    {
        UUID uuid = this.getUuid2() == player.getUniqueId() ? this.getUuid1() : this.getUuid2();
        Player player2 = Bukkit.getPlayer(uuid);
        PlayerManager playerManager2 = PlayerManager.getPlayerManagers().get(uuid);
        new MatchPreviewInventory(player2);
        player2.getInventory().clear();
        this.setMatchStatus(MatchStatus.FINISHING);
        if(cause == MatchEndCause.KILL) {
            new MatchPreviewInventory(player);
            player.teleport(player2.getLocation());
            if(player.getKiller() != null) sendGlobalMessage("§e"+player.getName()+" §6has been slain by §e" + player.getKiller().getName(), player, player2);
        } else {
            player2.sendMessage("§6Your opponent has leave the server!");
        }
        Bukkit.getScheduler().runTaskLater(Practice.getInstance(), (Runnable) new Runnable() {
            public void run() {
                destroy();

                String[] resultMessage = new String[]{
                    "§7§m----------------------------",
                    "§6Winner§7: §a" + player2.getName(),
                    "§6Loser§7: §c" + player.getName(),
                    "§7§m----------------------------"
                };

                // clickable message soon
                //String inventoriesMessage = "§6Inventories§7(click to view)§6: §e" + player2.getName()+"§7, §e"+player.getName();

                TextComponent inventoriesMessage = new TextComponent("§6Inventories§7(click to view)§6: §e");
                TextComponent name1 = new TextComponent("§e"+player2.getName());
                name1.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/inventory " + player2.getUniqueId()));
                name1.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§eClick to view this inventory.").create()));
                inventoriesMessage.addExtra(name1);
                inventoriesMessage.addExtra("§7, §e");
                TextComponent name2 = new TextComponent("§e"+player.getName());
                name2.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/inventory " + player.getUniqueId()));
                name2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§eClick to view this inventory.").create()));
                inventoriesMessage.addExtra(name2);

                sendGlobalMessage(resultMessage, player2);

                //replace soon
                sendGlobalMessage(inventoriesMessage, player2);

                playerManager2.reset(player2, GameMode.SURVIVAL);
                playerManager2.teleport(player2, Practice.getInstance().spawn);
                playerManager2.setPlayerSatus(PlayerSatus.FREE);
                if(cause == MatchEndCause.KILL)
                {
                    sendGlobalMessage(resultMessage, player);
                    sendGlobalMessage(inventoriesMessage, player);
                    playerManager.reset(player, GameMode.SURVIVAL);
                    playerManager.teleport(player, Practice.getInstance().spawn);
                    playerManager.sendKit(Practice.getInstance().respawnKit);
                    playerManager2.sendKit(Practice.getInstance().respawnKit);
                    playerManager.setCurrentDuelPlayer(playerManager2.getUuid());
                    playerManager2.setCurrentDuelPlayer(playerManager.getUuid());
                    playerManager.setPlayerSatus(PlayerSatus.FREE);
                }else{
                    playerManager2.sendKit(Practice.getInstance().spawnKit);
                }
            }
        }, 60);
    }

    private void destroy()
    {
        if(Practice.getInstance().fight.get(ladder.displayName()).getFightPlayer().get(fightType).contains(this)) Practice.getInstance().fight.get(ladder.displayName()).getFightPlayer().get(fightType).remove(this);
        // TODO remove this form all list after 30 seconds
    }

    private void sendGlobalMessage(String message, Player... players) { for(Player player : players) player.sendMessage(message); }
    private void sendGlobalMessage(String[] message, Player... players) { for(Player player : players) player.sendMessage(message); }
    private void sendGlobalMessage(TextComponent message, Player... players) { for(Player player : players) player.spigot().sendMessage(message); }

}
