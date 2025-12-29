package dev.lvstrng.argon.module.modules.render;

import dev.lvstrng.argon.event.events.WorldRenderListener;
import dev.lvstrng.argon.module.Category;
import dev.lvstrng.argon.module.Module;
import dev.lvstrng.argon.module.setting.BooleanSetting;
import dev.lvstrng.argon.module.setting.ColorSetting;
import dev.lvstrng.argon.module.setting.ModeSetting;
import dev.lvstrng.argon.module.setting.NumberSetting;
import dev.lvstrng.argon.utils.RenderUtils;
import dev.lvstrng.argon.utils.FriendUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

public class StaticHitboxes extends Module implements WorldRenderListener {

    private final ModeSetting<RenderMode> mode = new ModeSetting<>("Render Mode", RenderMode.Outline, RenderMode.class);
    private final NumberSetting lineWidth = new NumberSetting("Line Width", 0.1, 5.0, 2.0, 0.1);

    // Color Settings
    private final ColorSetting playersColor = new ColorSetting("Players Color", new Color(255, 255, 255, 255));
    private final ColorSetting friendsColor = new ColorSetting("Friends Color", new Color(0, 255, 100, 255));
    private final ColorSetting hostileColor = new ColorSetting("Hostiles Color", new Color(255, 0, 0, 255));
    private final ColorSetting passiveColor = new ColorSetting("Passives Color", new Color(0, 255, 255, 255));
    private final ColorSetting itemsColor = new ColorSetting("Items Color", new Color(255, 255, 0, 255));
    private final ColorSetting crystalsColor = new ColorSetting("Crystals Color", new Color(148, 0, 211, 255));
    private final ColorSetting projectilesColor = new ColorSetting("Projectiles Color", new Color(255, 165, 0, 255));
    private final ColorSetting vehiclesColor = new ColorSetting("Vehicles Color", new Color(128, 128, 128, 255));
    private final ColorSetting otherColor = new ColorSetting("Other Color", new Color(160, 160, 160, 255));

    // Entity Filters
    private final BooleanSetting players = new BooleanSetting("Players", true);
    private final BooleanSetting friends = new BooleanSetting("Friends", true);
    private final BooleanSetting hostiles = new BooleanSetting("Hostiles", true);
    private final BooleanSetting passive = new BooleanSetting("Passives", false);
    private final BooleanSetting items = new BooleanSetting("Items", false);
    private final BooleanSetting crystals = new BooleanSetting("Crystals", true);
    private final BooleanSetting projectiles = new BooleanSetting("Projectiles", false);
    private final BooleanSetting vehicles = new BooleanSetting("Vehicles", false);
    private final BooleanSetting others = new BooleanSetting("Others", false);
    private final BooleanSetting self = new BooleanSetting("Self", false);

    // Fill Settings
    private final NumberSetting fillOpacity = new NumberSetting("Fill Opacity", 0, 255, 40, 1);

    public StaticHitboxes() {
        super("Static Hitboxes", "Renders the hitboxes of entities in the world", -1, Category.RENDER);
        addSettings(
                mode, lineWidth,
                playersColor, friendsColor, hostileColor, passiveColor, itemsColor,
                crystalsColor, projectilesColor, vehiclesColor, otherColor,
                players, friends, hostiles, passive, items, crystals,
                projectiles, vehicles, others, self,
                fillOpacity
        );
    }

    @Override
    public void onEnable() {
        eventManager.add(WorldRenderListener.class, this);
    }

    @Override
    public void onDisable() {
        eventManager.remove(WorldRenderListener.class, this);
    }

    @Override
    public void onWorldRender(WorldRenderEvent event) {
        if (mc.world == null || mc.player == null) return;

        for (Entity entity : mc.world.getEntities()) {
            if (!shouldRender(entity)) continue;

            // Use the entity's bounding box. It's already correctly positioned.
            Box box = entity.getBoundingBox();
            
            Color color = getColorForEntity(entity);
            
            // Adjust alpha for fill based on setting
            Color fillColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), fillOpacity.getValueInt());

            switch (mode.getMode()) {
                case Outline:
                    RenderUtils.drawOutlineBox(event.getMatrices(), box, color, lineWidth.getValueFloat());
                    break;
                case Fill:
                    RenderUtils.drawFilledBox(event.getMatrices(), box, fillColor);
                    break;
                case OutlineAndFill:
                    RenderUtils.drawFilledBox(event.getMatrices(), box, fillColor);
                    RenderUtils.drawOutlineBox(event.getMatrices(), box, color, lineWidth.getValueFloat());
                    break;
            }
        }
    }

    private boolean shouldRender(Entity entity) {
        if (entity == mc.player && !self.getValue()) return false;
        if (entity instanceof PlayerEntity player) {
            if (FriendUtils.isFriend(player)) return friends.getValue();
            return players.getValue();
        }
        if (entity instanceof HostileEntity) return hostiles.getValue();
        if (entity instanceof PassiveEntity || entity instanceof AnimalEntity) return passive.getValue();
        if (entity instanceof ItemEntity) return items.getValue();
        if (entity instanceof EndCrystalEntity) return crystals.getValue();
        if (entity instanceof ProjectileEntity && !(entity instanceof EndCrystalEntity)) return projectiles.getValue();
        if (entity instanceof BoatEntity || entity instanceof MinecartEntity) return vehicles.getValue();
        return others.getValue();
    }

    private Color getColorForEntity(Entity entity) {
        if (entity == mc.player) return playersColor.getColor();
        if (entity instanceof PlayerEntity player && FriendUtils.isFriend(player)) return friendsColor.getColor();
        if (entity instanceof PlayerEntity) return playersColor.getColor();
        if (entity instanceof HostileEntity) return hostileColor.getColor();
        if (entity instanceof PassiveEntity || entity instanceof AnimalEntity) return passiveColor.getColor();
        if (entity instanceof ItemEntity) return itemsColor.getColor();
        if (entity instanceof EndCrystalEntity) return crystalsColor.getColor();
        if (entity instanceof ProjectileEntity) return projectilesColor.getColor();
        if (entity instanceof BoatEntity || entity instanceof MinecartEntity) return vehiclesColor.getColor();
        return otherColor.getColor();
    }

    public enum RenderMode { Outline, Fill, OutlineAndFill }
}
