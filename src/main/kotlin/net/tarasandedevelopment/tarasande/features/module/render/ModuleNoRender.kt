package net.tarasandedevelopment.tarasande.features.module.render

import net.minecraft.client.MinecraftClient
import net.minecraft.entity.EntityType
import net.minecraft.particle.ParticleType
import net.minecraft.particle.ParticleTypes
import net.minecraft.util.registry.Registry
import net.tarasandedevelopment.tarasande.base.features.module.Module
import net.tarasandedevelopment.tarasande.base.features.module.ModuleCategory
import net.tarasandedevelopment.tarasande.event.EventChunkOcclusion
import net.tarasandedevelopment.tarasande.event.EventParticle
import net.tarasandedevelopment.tarasande.screen.base.ScreenBetterParentPopupSettings
import net.tarasandedevelopment.tarasande.value.ValueBoolean
import net.tarasandedevelopment.tarasande.value.ValueMode
import net.tarasandedevelopment.tarasande.value.ValueRegistry
import net.tarasandedevelopment.tarasande.value.meta.ValueButton

class ModuleNoRender : Module("No render", "Disables rendering of certain things.", ModuleCategory.RENDER) {

    inner class NoRenderTypeOverlay : NoRenderType("Overlay") {

        val hurtCam = ValueBooleanNoRender(this, "Hurt cam", false)
        val portalOverlay = ValueBooleanNoRender(this, "Portal overlay", false)
        val spyglassOverlay = ValueBooleanNoRender(this, "Spyglass overlay", false)
        val noNausea = ValueBooleanNoRender(this, "Nausea overlay", false)
        val pumpkinOverlay = ValueBooleanNoRender(this, "Pumpkin overlay", false)
        val powderedSnowOverlay = ValueBooleanNoRender(this, "Powdered snow overlay", false)
        val fireOverlay = ValueBooleanNoRender(this, "Fire overlay", false)
        val waterOverlay = ValueBooleanNoRender(this, "Water overlay", false)
        val inWallOverlay = ValueBooleanNoRender(this, "In wall overlay", false)
        val vignette = ValueBooleanNoRender(this, "Vignette", false)
        val totemAnimation = ValueBooleanNoRender(this, "Totem animation", false)
        val eatParticles = ValueBooleanNoRender(this, "Eat particles", false)
    }

    val overlay = NoRenderTypeOverlay()

    inner class NoRenderTypeHUD : NoRenderType("Hud") {

        val bossBar = ValueBooleanNoRender(this, "Boss bar", false)
        val scoreboard = ValueBooleanNoRender(this, "Scoreboard", false)
        val crosshair = ValueBooleanNoRender(this, "Cross hair", false)
        val heldItemName = ValueBooleanNoRender(this, "Held item name", false)
        val potionIcons = ValueBooleanNoRender(this, "Potion icons", false)
    }

    val hud = NoRenderTypeHUD()

    inner class NoRenderTypeWorld : NoRenderType("World") {

        val weather = ValueBooleanNoRender(this, "Weather", false)
        val fog = ValueBooleanNoRender(this, "Fog", false)
        val enchantmentTableBook = ValueBooleanNoRender(this, "Enchantment table book", false)
        val signText = ValueBooleanNoRender(this, "Sign text", false)
        val blockBreakParticles = ValueBooleanNoRender(this, "Block break particles", false)
        val skylightUpdates = ValueBooleanNoRender(this, "Skylight updates", false)
        val fallingBlocks = ValueBooleanNoRender(this, "Falling blocks", false)
        val caveCulling = ValueBooleanNoRender(this, "Cave culling", false)
        val mapMarkers = ValueBooleanNoRender(this, "Map markers", false)
        val banners = ValueMode(this, "Banners", false, "All", "Pillar", "None")
        val fireworkExplosions = ValueBooleanNoRender(this, "Firework explosions", false)
        val particles = object : ValueRegistry<ParticleType<*>>(this, "Particles", Registry.PARTICLE_TYPE) {
            override fun getTranslationKey(key: Any?) = Registry.PARTICLE_TYPE.getId(key as ParticleType<*>?)!!.path
        }
        val barrierInvisibility = ValueBooleanNoRender(this, "Barrier invisibility", true)
    }

    val world = NoRenderTypeWorld()

    inner class NoRenderTypeEntity : NoRenderType("Entity") {

        val entities = object : ValueRegistry<EntityType<*>>(this, "Entities", Registry.ENTITY_TYPE) {
            override fun getTranslationKey(key: Any?) = (key as EntityType<*>).translationKey
        }
        val armor = ValueBooleanNoRender(this, "Armor", false)
        val mobInSpawner = ValueBooleanNoRender(this, "Mob in spawner", false)
        val deadEntities = ValueBooleanNoRender(this, "Dead entities", false)

        fun noEntity(entity: net.minecraft.entity.Entity): Boolean {
            return isEnabled() && entities.list.contains(entity.type)
        }
    }

    val entity = NoRenderTypeEntity()

    init {
        for (overlay in arrayOf(overlay, hud, world, entity)) {
            object : ValueButton(this, overlay.name) {
                override fun onChange() {
                    MinecraftClient.getInstance().setScreen(ScreenBetterParentPopupSettings(MinecraftClient.getInstance().currentScreen!!, overlay.name, overlay))
                }
            }
        }

        registerEvent(EventChunkOcclusion::class.java) {
            if (this.world.caveCulling.should()) {
                it.cancelled = true
            }
        }

        registerEvent(EventParticle::class.java) {
            if (world.weather.should() && it.effect == ParticleTypes.RAIN) {
                it.cancelled = true
            }

            if (world.fireworkExplosions.value && it.effect == ParticleTypes.FIREWORK) {
                it.cancelled = true
            }

            if (world.particles.list.contains(it.effect.type)) {
                it.cancelled = true
            }
        }
    }

    open class NoRenderType(val name: String)
    inner class ValueBooleanNoRender(owner: Any, name: String, value: Boolean) : ValueBoolean(owner, name, value) {
        fun should() = value && this@ModuleNoRender.enabled
    }
}