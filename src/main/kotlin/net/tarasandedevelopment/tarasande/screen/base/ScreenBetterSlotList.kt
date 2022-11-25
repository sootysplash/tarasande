package net.tarasandedevelopment.tarasande.screen.base

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget
import net.minecraft.client.gui.widget.EntryListWidget
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import net.tarasandedevelopment.tarasande.util.render.font.FontWrapper

open class ScreenBetterSlotList(private val top: Int, private val bottom: Int, var entryWidth: Int, private val entryHeight: Int) : ScreenBetter(null) {

    var slotList: AlwaysSelectedEntryListWidgetScreenBetterSlotListWidget? = null
    var listProvider: AlwaysSelectedEntryListWidgetScreenBetterSlotListWidget.ListProvider? = null
    var selected: Int = 0

    constructor(top: Int, entryWidth: Int, entryHeight: Int) : this(top, -10, entryWidth, entryHeight)
    constructor(top: Int, entryHeight: Int) : this(top, 220, entryHeight)

    fun provideElements(provider: AlwaysSelectedEntryListWidgetScreenBetterSlotListWidget.ListProvider) {
        this.listProvider = provider
    }

    fun renderTitle(matrices: MatrixStack?, title: String) {
        FontWrapper.textShadow(matrices, title, width / 2F, top / 2 - (FontWrapper.fontHeight() / 2F), scale = 2F, centered = true)
    }

    override fun init() {
        if (this.listProvider == null) return

        this.addDrawableChild(AlwaysSelectedEntryListWidgetScreenBetterSlotListWidget(this, client!!, this.listProvider, width, height, top, height - bottom - top, entryWidth, entryHeight).also {
            this.slotList = it
            this.slotList?.reload()
        })

        super.init()
    }
}

class AlwaysSelectedEntryListWidgetScreenBetterSlotListWidget(val parent: ScreenBetterSlotList, minecraft: MinecraftClient, private val listProvider: ListProvider?, width: Int, height: Int, top: Int, bottom: Int, private val entryWidth: Int, entryHeight: Int)
    : AlwaysSelectedEntryListWidget<EntryScreenBetterSlotListEntry>(minecraft, width, height, top, bottom, entryHeight) {

    fun reload() {
        this.clearEntries()
        if (this.listProvider == null) return

        for (entry in this.listProvider.get()) {
            entry.parentList = this
            this.addEntry(entry)
        }
    }

    interface ListProvider {
        fun get(): List<EntryScreenBetterSlotListEntry>
    }

    override fun getRowWidth() = entryWidth
    override fun getScrollbarPositionX() = this.width / 2 + (this.entryWidth / 2) + 14

    override fun appendNarrations(builder: NarrationMessageBuilder?) {
    }
}

open class EntryScreenBetterSlotListEntry : AlwaysSelectedEntryListWidget.Entry<EntryScreenBetterSlotListEntry>() {
    var parentList: EntryListWidget<EntryScreenBetterSlotListEntry>? = null
    private var lastClick: Long = 0
    private var index = 0

    open fun dontSelectAnything() = false

    open fun isSelected(): Boolean {
        if (this.dontSelectAnything()) {
            return false
        }
        return (this.parentList!! as AlwaysSelectedEntryListWidgetScreenBetterSlotListWidget).parent.selected == index
    }

    open fun renderEntry(matrices: MatrixStack, index: Int, entryWidth: Int, entryHeight: Int, mouseX: Int, mouseY: Int, hovered: Boolean) {
    }

    open fun onDoubleClickEntry(mouseX: Double, mouseY: Double, mouseButton: Int) {
    }

    open fun onSingleClickEntry(mouseX: Double, mouseY: Double, mouseButton: Int) {
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (!dontSelectAnything()) {
            (this.parentList!! as AlwaysSelectedEntryListWidgetScreenBetterSlotListWidget).parent.selected = this.index
            this.parentList!!.setSelected(this)
        }

        this.onSingleClickEntry(mouseX, mouseY, button)
        if (System.currentTimeMillis() - lastClick < 300) {
            onDoubleClickEntry(mouseX, mouseY, button)
        }
        lastClick = System.currentTimeMillis()
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun render(matrices: MatrixStack?, index: Int, y: Int, x: Int, entryWidth: Int, entryHeight: Int, mouseX: Int, mouseY: Int, hovered: Boolean, tickDelta: Float) {
        this.index = index

        matrices?.push()
        matrices?.translate(x.toDouble(), y.toDouble(), 0.0)
        this.renderEntry(matrices!!, index, entryWidth, entryHeight, mouseX, mouseY, hovered)
        matrices.pop()

        if (this.isSelected())
            this.parentList!!.setSelected(this)
    }

    override fun getNarration() = Text.empty()
}
