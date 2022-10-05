package net.tarasandedevelopment.tarasande.screen.clientmenu.accountmanager.subscreens

import com.mojang.authlib.Environment
import com.mojang.authlib.yggdrasil.YggdrasilEnvironment
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import net.tarasandedevelopment.tarasande.TarasandeMain
import net.tarasandedevelopment.tarasande.base.screen.clientmenu.accountmanager.account.Account
import net.tarasandedevelopment.tarasande.base.screen.clientmenu.accountmanager.account.AccountInfo
import net.tarasandedevelopment.tarasande.base.screen.clientmenu.accountmanager.account.TextFieldInfo
import net.tarasandedevelopment.tarasande.mixin.accessor.IScreen
import net.tarasandedevelopment.tarasande.screen.base.ScreenBetter
import net.tarasandedevelopment.tarasande.screen.clientmenu.ElementMenuScreenAccountManager
import net.tarasandedevelopment.tarasande.screen.widget.textfields.TextFieldWidgetPassword
import net.tarasandedevelopment.tarasande.screen.widget.textfields.TextFieldWidgetPlaceholder
import org.lwjgl.glfw.GLFW
import java.awt.Color
import java.lang.reflect.Constructor
import java.util.function.Consumer

class ScreenBetterAccount(
    prevScreen: Screen,
    val name: String,
    private val accountConsumer: Consumer<Account>,
) : ScreenBetter(prevScreen) {

    private val textFields: ArrayList<TextFieldWidget> = ArrayList()
    private var implementationClass: Class<out Account> = TarasandeMain.get().managerClientMenu.get(ElementMenuScreenAccountManager::class.java).screenBetterAccountManager.managerAccount.list[0]

    private var environment: Environment? = null

    private var submitButton: ButtonWidget? = null

    override fun init() {
        textFields.clear()
        children().clear()
        (this as IScreen).also {
            it.tarasande_getDrawables().clear()
            it.tarasande_getSelectables().clear()
        }

        super.init()

        addDrawableChild(
            ButtonWidget(
                5,
                5,
                100,
                20,
                Text.of((implementationClass.annotations[0] as AccountInfo).name)
            ) { button ->
                val accountManager = TarasandeMain.get().managerClientMenu.get(ElementMenuScreenAccountManager::class.java).screenBetterAccountManager

                implementationClass = accountManager.managerAccount.list[(accountManager.managerAccount.list.indexOf(implementationClass) + 1) % accountManager.managerAccount.list.size]
                init()
                button.message = Text.of(implementationClass.name)
            })

        addDrawableChild(ButtonWidget(5, 30, 100, 20, Text.of("Environment")) {
            client?.setScreen(ScreenBetterEnvironment(this, environment) { environment = it })
        })

        var constructor: Constructor<*>? = null
        for (c in implementationClass.constructors) {
            if (constructor == null || c.parameters.size > constructor.parameters.size) {
                constructor = c
            }
        }
        val parameters = constructor?.parameters!!
        for (i in parameters.indices) {
            val parameterType = parameters[i]
            if (parameterType.isAnnotationPresent(TextFieldInfo::class.java)) {
                val textFieldInfo: TextFieldInfo = parameterType.getAnnotation(TextFieldInfo::class.java)
                if (textFieldInfo.hidden) {
                    textFields.add(
                        addDrawableChild(
                            TextFieldWidgetPassword(
                                textRenderer,
                                width / 2 - 150,
                                (height * 0.25f + i * 25).toInt(),
                                300,
                                20,
                                Text.of(textFieldInfo.name)
                            ).also { it.setMaxLength(Int.MAX_VALUE); it.text = textFieldInfo.default })
                    )
                } else {
                    textFields.add(
                        addDrawableChild(
                            TextFieldWidgetPlaceholder(
                                textRenderer,
                                width / 2 - 150,
                                (height * 0.25f + i * 25).toInt(),
                                300,
                                20,
                                Text.of(textFieldInfo.name)
                            ).also { it.setMaxLength(Int.MAX_VALUE); it.text = textFieldInfo.default })
                    )
                }
            }
        }

        addDrawableChild(ButtonWidget(width / 2 - 50, 25 + (height * 0.75f).toInt(), 100, 20, Text.of(name)) {
            val account = (implementationClass.getDeclaredConstructor().newInstance() as Account).create(textFields.map { it.text })
            account.environment = environment ?: YggdrasilEnvironment.PROD.environment
            accountConsumer.accept(account)
            close()
        }.also { submitButton = it })

        this.addDrawableChild(ButtonWidget(5, this.height - 25, 20, 20, Text.of("<-")) { this.close() })
    }

    override fun render(matrices: MatrixStack?, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(matrices, mouseX, mouseY, delta)
        drawCenteredText(matrices, textRenderer, name, width / 2, 8 - textRenderer.fontHeight / 2, Color.white.rgb)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        var focused = false
        for (textField in textFields)
            if (textField.isFocused)
                focused = true
        if (hasControlDown() && keyCode == GLFW.GLFW_KEY_V && !focused) {
            val clipboardContent = GLFW.glfwGetClipboardString(client?.window?.handle!!)
            if (clipboardContent != null) {
                val parts = clipboardContent.split(":")
                if (parts.size == textFields.size)
                    for ((index, textField) in textFields.withIndex())
                        textField.text = parts[index]
            }
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER)
            submitButton?.onPress()
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

}