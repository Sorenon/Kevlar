package net.sorenon.kevlar.init

import net.fabricmc.api.ModInitializer
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import net.sorenon.kevlar.item.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger


class KevlarMod : ModInitializer {
    companion object {
        val LOGGER: Logger = LogManager.getLogger("kevlar")

        val GRAVITY_GUN_ITEM = GravityGun(
            Item.Settings().group(
                ItemGroup.TOOLS
            )
        )

        val PHYS_GUN_ITEM = PhysGun(
            Item.Settings().group(
                ItemGroup.TOOLS
            )
        )

        val BALL_ITEM = BallItem(
            Item.Settings().group(
                ItemGroup.TOOLS
            )
        )

        val BLOCK_ITEM = MiniBlockItem(
            Item.Settings().group(
                ItemGroup.TOOLS
            )
        )

        val BARREL_ITEM = BarrelItem(
            Item.Settings().group(
                ItemGroup.TOOLS
            )
        )

        val S2C_UPDATE_RIGIDBODY_STATES = Identifier("kevlar", "update_rb")
        val S2C_CREATE_OR_UPDATE_RB = Identifier("kevlar", "create_or_update_rb")
        val S2C_REMOVE_RB = Identifier("kevlar", "remove_rb")
        val S2C_GRAB_RB = Identifier("kevlar", "grab_rb")
    }

    override fun onInitialize() {
        registerItem(GRAVITY_GUN_ITEM, "gravity_gun")
        registerItem(PHYS_GUN_ITEM, "phys_gun")
        registerItem(BALL_ITEM, "ball")
        registerItem(BLOCK_ITEM, "mini_block")
        registerItem(BARREL_ITEM, "barrel")
    }

    private fun registerItem(item: Item, name: String) {
        Registry.register(
            Registry.ITEM,
            Identifier("kevlar", name),
            item
        )
    }
}