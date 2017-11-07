package mods.eln.node.six;

import mods.eln.generic.GenericItemBlockUsingDamageDescriptor;
import mods.eln.ghost.GhostGroup;
import mods.eln.misc.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;

import static mods.eln.i18n.I18N.tr;

public class SixNodeDescriptor extends GenericItemBlockUsingDamageDescriptor implements IItemRenderer {
    public Class ElementClass, RenderClass;
    public VoltageLevelColor voltageLevelColor = VoltageLevelColor.None;

    public SixNodeDescriptor(String name, Class ElementClass, Class RenderClass) {
        super(name);
        this.ElementClass = ElementClass;
        this.RenderClass = RenderClass;
    }

    public SixNodeDescriptor(String name, Class ElementClass, Class RenderClass, String iconName) {
        super(name, iconName);
        this.ElementClass = ElementClass;
        this.RenderClass = RenderClass;
    }

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return voltageLevelColor != VoltageLevelColor.None;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return false;
    }

    public boolean shouldUseRenderHelperEln(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return false;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        if (getIcon() == null)
            return;

        voltageLevelColor.drawIconBackground(type);

        // remove "eln:" to add the full path replace("eln:", "textures/blocks/") + ".png";
        String icon = getIcon().getIconName().substring(4);
        UtilsClient.drawIcon(type, new ResourceLocation("eln", "textures/blocks/" + icon + ".png"));
    }

    public boolean hasVolume() {
        return false;
    }

    public boolean canBePlacedOnSide(EntityPlayer player, Coordonate c, Direction side) {
        return canBePlacedOnSide(player, side);
    }

    public boolean canBePlacedOnSide(EntityPlayer player, Direction side) {
        if (placeDirection != null) {
            for (Direction d : placeDirection) {
                if (d == side)
                    return true;
            }
            Utils.addChatMessage(player, tr("You can't place this block at this side"));
            return false;
        }
        return true;
    }


    public void setGhostGroup(GhostGroup ghostGroup) {
        this.ghostGroup = ghostGroup;
    }

    protected GhostGroup ghostGroup = null;

    public boolean hasGhostGroup() {
        return ghostGroup != null;
    }

    public GhostGroup getGhostGroup(Direction side, LRDU front) {
        if (ghostGroup == null) return null;
        return ghostGroup.newRotate(side, front);
    }

    public int getGhostGroupUuid() {
        return -1;
    }

    public void setPlaceDirection(Direction d) {
        placeDirection = new Direction[]{d};
    }

    public void setPlaceDirection(Direction[] d) {
        placeDirection = d;
    }

    protected Direction[] placeDirection = null;

    public String checkCanPlace(Coordonate coord, Direction direction, LRDU front) {
        if (placeDirection != null) {
            boolean ok = false;
            for (Direction d : placeDirection) {
                if (d == direction) {
                    ok = true;
                    break;
                }
            }
            if (!ok)
                return tr("You can't place this block at this side");
        }
        GhostGroup ghostGroup = getGhostGroup(direction, front);
        if (ghostGroup != null && !ghostGroup.canBePloted(coord))
            return tr("Not enough space for this block");
        return null;
    }

    public LRDU getFrontFromPlace(Direction side, EntityPlayer player) {
        switch (side) {
            case YN:
            case YP:
                Direction viewDirection = Utils.entityLivingHorizontalViewDirection(player);
                LRDU front = side.getLRDUGoingTo(viewDirection);
                return side == Direction.YN ? front : front.inverse();

            default:
                return LRDU.Up;
        }
    }
}
