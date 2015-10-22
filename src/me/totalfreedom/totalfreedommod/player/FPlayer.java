package me.totalfreedom.totalfreedommod.player;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import me.totalfreedom.totalfreedommod.TotalFreedomMod;
import me.totalfreedom.totalfreedommod.config.ConfigEntry;
import me.totalfreedom.totalfreedommod.util.FUtil;
import net.pravian.aero.util.Ips;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class FPlayer
{
    public static final long AUTO_PURGE_TICKS = 5L * 60L * 20L;

    @Getter
    private final String name;
    @Getter
    private final String ip;
    //
    @Setter
    private Player player;
    //
    private BukkitTask unmuteTask;
    private BukkitTask unfreezeTask;
    private Location freezeLocation;
    private boolean isHalted = false;
    private int messageCount = 0;
    private int totalBlockDestroy = 0;
    private int totalBlockPlace = 0;
    private int freecamDestroyCount = 0;
    private int freecamPlaceCount = 0;
    private boolean isCaged = false;
    private Location cagePosition;
    private final List<TFM_BlockData> cageHistory = new ArrayList<TFM_BlockData>();
    private Material cageOuterMaterial = Material.GLASS;
    private Material cageInnerMatterial = Material.AIR;
    private boolean isOrbiting = false;
    private double orbitStrength = 10.0;
    private boolean mobThrowerEnabled = false;
    private EntityType mobThrowerEntity = EntityType.PIG;
    private double mobThrowerSpeed = 4.0;
    private final List<LivingEntity> mobThrowerQueue = new ArrayList<LivingEntity>();
    private BukkitTask mp44ScheduleTask = null;
    private boolean mp44Armed = false;
    private boolean mp44Firing = false;
    private BukkitTask lockupScheduleTask = null;
    private String lastMessage = "";
    private boolean inAdminchat = false;
    private boolean allCommandsBlocked = false;
    private boolean verifiedSuperadminId = false;
    private String lastCommand = "";
    private boolean cmdspyEnabled = false;
    private String tag = null;
    private int warningCount = 0;
    //
    @Getter
    @Setter
    private VerifyStage verifyStage = VerifyStage.NONE;
    @Getter
    @Setter
    private String newVerifyPassword = null;

    public FPlayer(Player player)
    {
        this(player.getName(), Ips.getIp(player));
    }

    private FPlayer(String name, String ip)
    {
        this.name = name;
        this.ip = ip;
    }

    public Player getPlayer()
    {
        if (player != null && !player.isOnline())
        {
            player = null;
        }

        if (player == null)
        {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers())
            {
                if (Ips.getIp(onlinePlayer).equals(ip))
                {
                    player = onlinePlayer;
                    break;
                }
            }
        }

        return player;
    }

    public boolean isOrbiting()
    {
        return isOrbiting;
    }

    public void startOrbiting(double strength)
    {
        this.isOrbiting = true;
        this.orbitStrength = strength;
    }

    public void stopOrbiting()
    {
        this.isOrbiting = false;
    }

    public double orbitStrength()
    {
        return orbitStrength;
    }

    public void setCaged(boolean state)
    {
        this.isCaged = state;
    }

    public void setCaged(boolean state, Location location, Material outer, Material inner)
    {
        this.isCaged = state;
        this.cagePosition = location;
        this.cageOuterMaterial = outer;
        this.cageInnerMatterial = inner;
    }

    public boolean isCaged()
    {
        return isCaged;
    }

    public Material getCageMaterial(CageLayer layer)
    {
        switch (layer)
        {
            case OUTER:
                return this.cageOuterMaterial;
            case INNER:
                return this.cageInnerMatterial;
            default:
                return this.cageOuterMaterial;
        }
    }

    public Location getCagePos()
    {
        return cagePosition;
    }

    public void clearHistory()
    {
        cageHistory.clear();
    }

    public void insertHistoryBlock(Location location, Material material)
    {
        cageHistory.add(new TFM_BlockData(location, material));
    }

    public void regenerateHistory()
    {
        for (TFM_BlockData blockdata : this.cageHistory)
        {
            blockdata.location.getBlock().setType(blockdata.material);
        }
    }

    public Location getFreezeLocation()
    {
        return freezeLocation;
    }

    public boolean isFrozen()
    {
        return unfreezeTask != null;
    }

    public void setFrozen(boolean freeze)
    {
        cancel(unfreezeTask);
        unfreezeTask = null;
        freezeLocation = null;

        if (player.getGameMode() != GameMode.CREATIVE)
        {
            FUtil.setFlying(player, false);
        }

        if (!freeze)
        {
            return;
        }

        freezeLocation = player.getLocation(); // Blockify location
        FUtil.setFlying(player, true); // Avoid infinite falling

        unfreezeTask = new BukkitRunnable()
        {
            @Override
            public void run()
            {
                FUtil.adminAction("TotalFreedom", "Unfreezing " + player.getName(), false);
                setFrozen(false);
            }

        }.runTaskLater(TotalFreedomMod.plugin, AUTO_PURGE_TICKS);
    }

    public void resetMsgCount()
    {
        this.messageCount = 0;
    }

    public int incrementAndGetMsgCount()
    {
        return this.messageCount++;
    }

    public int incrementAndGetBlockDestroyCount()
    {
        return this.totalBlockDestroy++;
    }

    public void resetBlockDestroyCount()
    {
        this.totalBlockDestroy = 0;
    }

    public int incrementAndGetBlockPlaceCount()
    {
        return this.totalBlockPlace++;
    }

    public void resetBlockPlaceCount()
    {
        this.totalBlockPlace = 0;
    }

    public int incrementAndGetFreecamDestroyCount()
    {
        return this.freecamDestroyCount++;
    }

    public void resetFreecamDestroyCount()
    {
        this.freecamDestroyCount = 0;
    }

    public int incrementAndGetFreecamPlaceCount()
    {
        return this.freecamPlaceCount++;
    }

    public void resetFreecamPlaceCount()
    {
        this.freecamPlaceCount = 0;
    }

    public void enableMobThrower(EntityType mobThrowerCreature, double mobThrowerSpeed)
    {
        this.mobThrowerEnabled = true;
        this.mobThrowerEntity = mobThrowerCreature;
        this.mobThrowerSpeed = mobThrowerSpeed;
    }

    public void disableMobThrower()
    {
        this.mobThrowerEnabled = false;
    }

    public EntityType mobThrowerCreature()
    {
        return this.mobThrowerEntity;
    }

    public double mobThrowerSpeed()
    {
        return this.mobThrowerSpeed;
    }

    public boolean mobThrowerEnabled()
    {
        return this.mobThrowerEnabled;
    }

    public void enqueueMob(LivingEntity mob)
    {
        mobThrowerQueue.add(mob);
        if (mobThrowerQueue.size() > 4)
        {
            LivingEntity oldmob = mobThrowerQueue.remove(0);
            if (oldmob != null)
            {
                oldmob.damage(500.0);
            }
        }
    }

    public void startArrowShooter(TotalFreedomMod plugin)
    {
        this.stopArrowShooter();
        this.mp44ScheduleTask = new ArrowShooter(this.player).runTaskTimer(plugin, 1L, 1L);
        this.mp44Firing = true;
    }

    public void stopArrowShooter()
    {
        if (this.mp44ScheduleTask != null)
        {
            this.mp44ScheduleTask.cancel();
            this.mp44ScheduleTask = null;
        }
        this.mp44Firing = false;
    }

    public void armMP44()
    {
        this.mp44Armed = true;
        this.stopArrowShooter();
    }

    public void disarmMP44()
    {
        this.mp44Armed = false;
        this.stopArrowShooter();
    }

    public boolean isMP44Armed()
    {
        return this.mp44Armed;
    }

    public boolean toggleMP44Firing()
    {
        this.mp44Firing = !this.mp44Firing;
        return mp44Firing;
    }

    public boolean isMuted()
    {
        return unmuteTask != null;
    }

    public void setMuted(boolean muted)
    {
        cancel(unmuteTask);
        unmuteTask = null;

        if (!muted)
        {
            return;
        }

        unmuteTask = new BukkitRunnable()
        {
            @Override
            public void run()
            {
                FUtil.adminAction("TotalFreedom", "Unmuting " + player.getName(), false);
                setMuted(false);
            }
        }.runTaskLater(TotalFreedomMod.plugin, AUTO_PURGE_TICKS);
    }

    public boolean isHalted()
    {
        return this.isHalted;
    }

    public void setHalted(boolean halted)
    {
        this.isHalted = halted;

        if (halted)
        {
            player.setOp(false);
            player.setGameMode(GameMode.SURVIVAL);
            FUtil.setFlying(player, false);
            TotalFreedomMod.plugin.esb.setNickname(player.getName(), player.getName());
            player.closeInventory();
            player.setTotalExperience(0);

            stopOrbiting();
            setFrozen(true);
            setMuted(true);

            player.sendMessage(ChatColor.GRAY + "You have been halted, don't move!");
        }
        else
        {
            player.setOp(true);
            player.setGameMode(GameMode.CREATIVE);
            setFrozen(false);
            setMuted(false);

            player.sendMessage(ChatColor.GRAY + "You are no longer halted.");
        }

    }

    public BukkitTask getLockupScheduleID()
    {
        return this.lockupScheduleTask;
    }

    public void setLockupScheduleID(BukkitTask id)
    {
        this.lockupScheduleTask = id;
    }

    public void setLastMessage(String message)
    {
        this.lastMessage = message;
    }

    public String getLastMessage()
    {
        return lastMessage;
    }

    public void setAdminChat(boolean inAdminchat)
    {
        this.inAdminchat = inAdminchat;
    }

    public boolean inAdminChat()
    {
        return this.inAdminchat;
    }

    public boolean allCommandsBlocked()
    {
        return this.allCommandsBlocked;
    }

    public void setCommandsBlocked(boolean commandsBlocked)
    {
        this.allCommandsBlocked = commandsBlocked;
    }

    // If someone logs in to telnet or minecraft, and they are an admin, make sure that they are using a username that is associated with their IP.
    // After the check for this is done in TFM_PlayerListener, never change it elsewhere.
    public boolean isSuperadminIdVerified()
    {
        return this.verifiedSuperadminId;
    }

    // If someone logs in to telnet or minecraft, and they are an admin, make sure that they are using a username that is associated with their IP.
    // After the check for this is done in TFM_PlayerListener, never change it elsewhere.
    public void setSuperadminIdVerified(boolean verifiedSuperadminId)
    {
        this.verifiedSuperadminId = verifiedSuperadminId;
    }

    public String getLastCommand()
    {
        return lastCommand;
    }

    public void setLastCommand(String lastCommand)
    {
        this.lastCommand = lastCommand;
    }

    public void setCommandSpy(boolean enabled)
    {
        this.cmdspyEnabled = enabled;
    }

    public boolean cmdspyEnabled()
    {
        return cmdspyEnabled;
    }

    public void setTag(String tag)
    {
        if (tag == null)
        {
            this.tag = null;
        }
        else
        {
            this.tag = FUtil.colorize(tag) + ChatColor.WHITE;
        }
    }

    public String getTag()
    {
        return this.tag;
    }

    public int getWarningCount()
    {
        return this.warningCount;
    }

    public void incrementWarnings()
    {
        this.warningCount++;

        if (this.warningCount % 2 == 0)
        {
            this.player.getWorld().strikeLightning(this.player.getLocation());
            FUtil.playerMsg(this.player, ChatColor.RED + "You have been warned at least twice now, make sure to read the rules at " + ConfigEntry.SERVER_BAN_URL.getString());
        }
    }

    public void cancel(BukkitTask task)
    {
        if (task == null)
        {
            return;
        }

        try
        {
            task.cancel();
        }
        catch (Exception ex)
        {
        }
    }

    public enum CageLayer
    {
        INNER, OUTER
    }

    private class TFM_BlockData
    {
        public Material material;
        public Location location;

        private TFM_BlockData(Location location, Material material)
        {
            this.location = location;
            this.material = material;
        }
    }

    private class ArrowShooter extends BukkitRunnable
    {
        private Player player;

        private ArrowShooter(Player player)
        {
            this.player = player;
        }

        @Override
        public void run()
        {
            Arrow shot = player.launchProjectile(Arrow.class);
            shot.setVelocity(shot.getVelocity().multiply(2.0));
        }
    }
}