package net.sorenon.kevlar.init

import net.fabricmc.api.ModInitializer
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import net.sorenon.kevlar.item.BallItem
import net.sorenon.kevlar.item.GravityGun
import net.sorenon.kevlar.item.PhysGun
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
    }

    override fun onInitialize() {
        registerItem(GRAVITY_GUN_ITEM, "gravity_gun")
        registerItem(PHYS_GUN_ITEM, "phys_gun")
        registerItem(BALL_ITEM, "ball")
    }

    private fun registerItem(item: Item, name: String) {
        Registry.register(
            Registry.ITEM,
            Identifier("kevlar", name),
            item
        )
    }
}