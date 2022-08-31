package su.mandora.tarasande.screen.accountmanager.account

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import com.mojang.authlib.minecraft.MinecraftSessionService
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.NoticeScreen
import net.minecraft.client.util.Session
import net.minecraft.text.Text
import net.minecraft.util.Util
import su.mandora.tarasande.TarasandeMain
import su.mandora.tarasande.base.screen.accountmanager.account.Account
import su.mandora.tarasande.base.screen.accountmanager.account.AccountInfo
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.*
import java.nio.charset.StandardCharsets
import java.sql.Timestamp
import java.util.*
import java.util.concurrent.ThreadLocalRandom

@AccountInfo("Microsoft", true)
class AccountMicrosoft : Account() {
    private val oauthAuthorizeUrl = "https://login.live.com/oauth20_authorize.srf"
    private val oauthTokenUrl = "https://login.live.com/oauth20_token.srf"
    private val xboxAuthenticateUrl = "https://user.auth.xboxlive.com/user/authenticate"
    private val xboxAuthorizeUrl = "https://xsts.auth.xboxlive.com/xsts/authorize"
    private val minecraftLoginUrl = "https://api.minecraftservices.com/authentication/login_with_xbox"
    private val minecraftProfileUrl = "https://api.minecraftservices.com/minecraft/profile"
    private val clientId = "54fd49e4-2103-4044-9603-2b028c814ec3"
    private val scope = "XboxLive.signin offline_access"
    private val redirectUriBase = "http://localhost:"
    private var cancelled = false

    var service: MinecraftSessionService? = null

    private var msAuthProfile: MSAuthProfile? = null
    private var redirectUri: String? = null
    private var code: String? = null

    private fun randomPort(): Int = ThreadLocalRandom.current().nextInt(0, Short.MAX_VALUE.toInt() * 2 /* unsigned */)

    private fun setupHttpServer(): ServerSocket {
        return try {
            val serverSocket = ServerSocket(randomPort())
            if (!serverSocket.isBound)
                throw IllegalStateException("Not bound")
            val t = Thread({
                try {
                    val socket = serverSocket.accept()
                    Thread.sleep(100L) // some browsers are slow for some reason
                    val bufferedReader = BufferedReader(InputStreamReader(socket.getInputStream()))
                    val content = StringBuilder()
                    while (bufferedReader.ready())
                        content.append(bufferedReader.read().toChar())
                    code = try {
                        content.toString().split("code=")[1].split(" ")[0].split("&")[0]
                    } catch (t: Throwable) {
                        t.printStackTrace()
                        cancelled = true
                        return@Thread
                    } // hack
                    socket.getOutputStream().write("""HTTP/2 200 OK
content-type: text/plain

You can close this page now.""".toByteArray())
                    socket.close()
                    serverSocket.close()
                } catch (t: Throwable) {
                    t.printStackTrace()
                    serverSocket.close()
                }
            }, "Microsoft login http server")
            t.start()
            serverSocket
        } catch (t: Throwable) {
            setupHttpServer()
        }
    }

    override fun logIn() {
        code = null
        cancelled = false
        if (msAuthProfile != null &&
            (msAuthProfile?.xboxLiveAuth            ?.notAfter?.time?.compareTo(System.currentTimeMillis())?.let { it < 0 } == true ||
             msAuthProfile?.xboxLiveSecurityTokens  ?.notAfter?.time?.compareTo(System.currentTimeMillis())?.let { it < 0 } == true)
        ) {
            msAuthProfile = msAuthProfile?.renew() // use refresh token to update the account
        }

        if (msAuthProfile == null) {
            val serverSocket = setupHttpServer()
            val prevScreen = MinecraftClient.getInstance().currentScreen
            RenderSystem.recordRenderCall {
                MinecraftClient.getInstance().setScreen(NoticeScreen({ cancelled = true }, Text.of("Microsoft Login"), Text.of("Your webbrowser should've opened.\nPlease authorize yourself!\nClosing this screen will cancel the process!"), Text.of("Cancel"), false))
            }
            redirectUri = redirectUriBase + serverSocket.localPort
            Util.getOperatingSystem().open(URI(oauthAuthorizeUrl + "?" +
                    "redirect_uri=" + redirectUri + "&" +
                    "scope=" + URLEncoder.encode(scope, StandardCharsets.UTF_8) + "&" +
                    "response_type=code&" +
                    "client_id=" + clientId
            ))
            while (code == null) {
                Thread.sleep(100L)
                if(cancelled) {
                    RenderSystem.recordRenderCall {
                        MinecraftClient.getInstance().setScreen(prevScreen)
                    }
                    throw IllegalStateException("Cancelled")
                }
            }
            RenderSystem.recordRenderCall {
                MinecraftClient.getInstance().setScreen(prevScreen)
            }
            msAuthProfile = buildFromCode(code!!)
        }

        service = YggdrasilAuthenticationService(Proxy.NO_PROXY, "", environment).createMinecraftSessionService()
        if (msAuthProfile != null) {
            session = msAuthProfile?.asSession()!!
        } else {
            throw IllegalStateException("WHAT THE FUCK")
        }
    }

    private fun buildFromCode(code: String): MSAuthProfile {
        val str = post(oauthTokenUrl, 60 * 1000, HashMap<String, String>().also {
            it["client_id"] = clientId
            it["code"] = code
            it["grant_type"] = "authorization_code"
            it["redirect_uri"] = redirectUri!!
            it["scope"] = scope
        })
        val oAuthToken = TarasandeMain.get().gson.fromJson(str, JsonObject::class.java)
        return buildFromOAuthToken(oAuthToken)
    }

    private fun buildFromRefreshToken(refreshToken: String): MSAuthProfile {
        val oAuthToken = TarasandeMain.get().gson.fromJson(post(oauthTokenUrl, 60 * 1000, HashMap<String, String>().also {
            it["client_id"] = clientId
            it["refresh_token"] = refreshToken
            it["grant_type"] = "refresh_token"
            it["redirect_uri"] = redirectUri!!
            it["scope"] = scope
        }), JsonObject::class.java)
        return buildFromOAuthToken(oAuthToken)
    }

    private fun buildFromOAuthToken(oAuthToken: JsonObject): MSAuthProfile {
        val req = JsonObject()
        val reqProps = JsonObject()
        reqProps.addProperty("AuthMethod", "RPS")
        reqProps.addProperty("SiteName", "user.auth.xboxlive.com")
        reqProps.addProperty("RpsTicket", "d=" + oAuthToken["access_token"])
        req.add("Properties", reqProps)
        req.addProperty("RelyingParty", "http://auth.xboxlive.com")
        req.addProperty("TokenType", "JWT")
        val xboxLiveAuth = TarasandeMain.get().gson.fromJson(post(xboxAuthenticateUrl, 60 * 1000, "application/json", req.toString()), JsonObject::class.java)
        return buildFromXboxLive(oAuthToken, xboxLiveAuth)
    }

    private fun buildFromXboxLive(oAuthToken: JsonObject, xboxLiveAuth: JsonObject): MSAuthProfile {
        val req = JsonObject()
        val reqProps = JsonObject()
        val userTokens = JsonArray()
        userTokens.add(xboxLiveAuth["Token"].asString)
        reqProps.add("UserTokens", userTokens)
        reqProps.addProperty("SandboxId", "RETAIL")
        req.add("Properties", reqProps)
        req.addProperty("RelyingParty", "rp://api.minecraftservices.com/")
        req.addProperty("TokenType", "JWT")
        val xboxLiveSecurityTokens = TarasandeMain.get().gson.fromJson(post(xboxAuthorizeUrl, 60 * 1000, "application/json", req.toString()), JsonObject::class.java)
        return buildFromXboxLiveSecurityTokens(oAuthToken, xboxLiveAuth, xboxLiveSecurityTokens)
    }

    private fun buildFromXboxLiveSecurityTokens(oAuthToken: JsonObject, xboxLiveAuth: JsonObject, xboxLiveSecurityTokens: JsonObject): MSAuthProfile {
        val req = JsonObject()
        req.addProperty("identityToken", "XBL3.0 x=" + xboxLiveSecurityTokens.getAsJsonObject("DisplayClaims").getAsJsonArray("xui")[0].asJsonObject["uhs"].asString + ";" + xboxLiveSecurityTokens["Token"].asString)
        val minecraftLogin = TarasandeMain.get().gson.fromJson(post(minecraftLoginUrl, 60 * 1000, "application/json", req.toString()), JsonObject::class.java)
        return buildFromMinecraftLogin(oAuthToken, xboxLiveAuth, xboxLiveSecurityTokens, minecraftLogin)
    }

    private fun buildFromMinecraftLogin(oAuthToken: JsonObject, xboxLiveAuth: JsonObject, xboxLiveSecurityTokens: JsonObject, minecraftLogin: JsonObject): MSAuthProfile {
        val minecraftProfile = TarasandeMain.get().gson.fromJson(get(minecraftProfileUrl, 60 * 1000, HashMap<String, String>().also {
            it["Authorization"] = "Bearer " + minecraftLogin["access_token"].asString
        }), JsonObject::class.java)
        return MSAuthProfile(oAuthToken, xboxLiveAuth, xboxLiveSecurityTokens, minecraftLogin, minecraftProfile)
    }

    operator fun get(url: String, timeout: Int, headers: HashMap<String, String>): String {
        val urlConnection = URL(url).openConnection() as HttpURLConnection
        headers.forEach(urlConnection::setRequestProperty)
        urlConnection.readTimeout = timeout
        urlConnection.connectTimeout = timeout
        urlConnection.requestMethod = "GET"
        urlConnection.connect()
        return String(urlConnection.inputStream.readAllBytes())
    }

    fun post(url: String, timeout: Int, arguments: HashMap<String, String>): String {
        val argumentsStr = StringBuilder()
        arguments.forEach {
            argumentsStr.append(URLEncoder.encode(it.key, StandardCharsets.UTF_8)).append("=").append(URLEncoder.encode(it.value, StandardCharsets.UTF_8)).append("&")
        }
        return post(url, timeout, "application/x-www-form-urlencoded", argumentsStr.substring(0, argumentsStr.length - 1))
    }

    fun post(url: String, timeout: Int, contentType: String, input: String): String {
        val urlConnection = URL(url).openConnection() as HttpURLConnection
        urlConnection.readTimeout = timeout
        urlConnection.connectTimeout = timeout
        urlConnection.requestMethod = "POST"
        urlConnection.doOutput = true
        urlConnection.setFixedLengthStreamingMode(input.length)
        urlConnection.setRequestProperty("Content-Type", contentType)
        urlConnection.setRequestProperty("Accept", contentType)
        urlConnection.connect()
        urlConnection.outputStream.write(input.toByteArray(StandardCharsets.UTF_8))
        urlConnection.outputStream.flush()
        return String(urlConnection.inputStream.readAllBytes())
    }

    override fun getDisplayName(): String {
        return if (session != null) session?.username!! else "Unnamed Microsoft-account"
    }

    override fun getSessionService(): MinecraftSessionService? = service

    override fun save(): JsonArray {
        val jsonArray = JsonArray()
        if (msAuthProfile != null)
            jsonArray.add(TarasandeMain.get().gson.toJsonTree(msAuthProfile))
        return jsonArray
    }

    override fun load(jsonArray: JsonArray): Account {
        return AccountMicrosoft().also { if (!jsonArray.isEmpty) it.msAuthProfile = TarasandeMain.get().gson.fromJson(jsonArray[0], MSAuthProfile::class.java) }
    }

    override fun create(credentials: List<String>) = AccountMicrosoft()

    inner class MSAuthProfile(oAuthToken: JsonObject, xboxLiveAuth: JsonObject, xboxLiveSecurityTokens: JsonObject, minecraftLogin: JsonObject, minecraftProfile: JsonObject) {
        private var oAuthToken: OAuthToken? = null
        var xboxLiveAuth: XboxLiveAuth? = null
        var xboxLiveSecurityTokens: XboxLiveSecurityTokens? = null
        private var minecraftLogin: MinecraftLogin? = null
        private var minecraftProfile: MinecraftProfile? = null

        init {
            val gson = TarasandeMain.get().gson
            this.oAuthToken = gson.fromJson(oAuthToken, OAuthToken::class.java)
            this.xboxLiveAuth = gson.fromJson(xboxLiveAuth, XboxLiveAuth::class.java)
            this.xboxLiveSecurityTokens = gson.fromJson(xboxLiveSecurityTokens, XboxLiveSecurityTokens::class.java)
            this.minecraftLogin = gson.fromJson(minecraftLogin, MinecraftLogin::class.java)
            this.minecraftProfile = gson.fromJson(minecraftProfile, MinecraftProfile::class.java)
        }

        fun asSession() = Session(
            minecraftProfile?.name,
            minecraftProfile?.id,
            minecraftLogin?.accessToken,
            Optional.of(xboxLiveAuth?.token!!),
            Optional.of(AccountMicrosoft().clientId), // I hate the jvm, I hate the bytecode, I hate the language, I hate me, I hate everything!
            Session.AccountType.MSA
        )

        fun renew(): MSAuthProfile { // I have no clue why I have to do this, but it crashes because "this" is null otherwise ._.
            val microsoft = AccountMicrosoft()
            microsoft.redirectUri = microsoft.redirectUriBase + microsoft.randomPort()
            return microsoft.buildFromRefreshToken(oAuthToken?.refreshToken!!)
        }

        override fun toString(): String {
            return "MSAuthProfile(oAuthToken=$oAuthToken, xboxLiveAuth=$xboxLiveAuth, xboxLiveSecurityTokens=$xboxLiveSecurityTokens, minecraftLogin=$minecraftLogin, minecraftProfile=$minecraftProfile)"
        }

        inner class OAuthToken {
            @SerializedName("token_type")
            var tokenType: String? = null

            @SerializedName("expires_in")
            var expiresIn = 0

            @SerializedName("scope")
            var scope: String? = null

            @SerializedName("access_token")
            var accessToken: String? = null

            @SerializedName("refresh_token")
            var refreshToken: String? = null

            @SerializedName("authentication_token")
            var authenticationToken: String? = null

            @SerializedName("user_id")
            var userId: String? = null

            override fun toString(): String {
                return "OAuthToken(tokenType=$tokenType, expiresIn=$expiresIn, scope=$scope, accessToken=$accessToken, refreshToken=$refreshToken, authenticationToken=$authenticationToken, userId=$userId)"
            }
        }

        inner class XboxLiveAuth {
            @SerializedName("IssueInstant")
            var issueInstant: Timestamp? = null

            @SerializedName("NotAfter")
            var notAfter: Timestamp? = null

            @SerializedName("Token")
            var token: String? = null

            @SerializedName("DisplayClaims")
            var displayClaims: DisplayClaim? = null

            override fun toString(): String {
                return "XboxLiveAuth(issueInstant=$issueInstant, notAfter=$notAfter, token=$token, displayClaims=$displayClaims)"
            }

            inner class DisplayClaim {
                @SerializedName("xui")
                var xui: Array<Xui>? = null

                override fun toString(): String {
                    return "DisplayClaim(xui=${xui?.contentToString()})"
                }

                inner class Xui {
                    @SerializedName("uhs")
                    var uhs: String? = null

                    override fun toString(): String {
                        return "Xui(uhs=$uhs)"
                    }
                }
            }
        }

        inner class XboxLiveSecurityTokens {
            @SerializedName("IssueInstant")
            var issueInstant: Timestamp? = null

            @SerializedName("NotAfter")
            var notAfter: Timestamp? = null

            @SerializedName("Token")
            var token: String? = null

            @SerializedName("DisplayClaims")
            var displayClaims: DisplayClaim? = null

            override fun toString(): String {
                return "XboxLiveSecurityTokens(issueInstant=$issueInstant, notAfter=$notAfter, token=$token, displayClaims=$displayClaims)"
            }

            inner class DisplayClaim {
                @SerializedName("xui")
                var xui: Array<Xui>? = null

                override fun toString(): String {
                    return "DisplayClaim(xui=${xui?.contentToString()})"
                }

                inner class Xui {
                    @SerializedName("uhs")
                    var uhs: String? = null

                    override fun toString(): String {
                        return "Xui(uhs=$uhs)"
                    }
                }
            }
        }

        inner class MinecraftLogin {
            @SerializedName("username")
            var username: String? = null

            @SerializedName("roles")
            var roles: Array<Any>? = null

            @SerializedName("access_token")
            var accessToken: String? = null

            @SerializedName("token_type")
            var tokenType: String? = null

            @SerializedName("expires_in")
            var expiresIn = 0

            override fun toString(): String {
                return "MinecraftLogin(username=$username, roles=${roles?.contentToString()}, accessToken=$accessToken, tokenType=$tokenType, expiresIn=$expiresIn)"
            }
        }

        inner class MinecraftProfile {
            @SerializedName("id")
            var id: String? = null

            @SerializedName("name")
            var name: String? = null

            @SerializedName("skins")
            var skins: Array<Skin>? = null

            @SerializedName("capes")
            var capes: Array<Cape>? = null

            override fun toString(): String {
                return "MinecraftProfile(id=$id, name=$name, skins=${skins?.contentToString()}, capes=${capes?.contentToString()})"
            }

            inner class Skin {
                @SerializedName("id")
                var id: String? = null

                @SerializedName("state")
                var state: String? = null

                @SerializedName("url")
                var url: String? = null

                @SerializedName("variant")
                var variant: String? = null

                override fun toString(): String {
                    return "Skin(id=$id, state=$state, url=$url, variant=$variant)"
                }
            }

            inner class Cape {
                @SerializedName("id")
                var id: String? = null

                @SerializedName("state")
                var state: String? = null

                @SerializedName("url")
                var url: String? = null

                @SerializedName("alias")
                var alias: String? = null

                override fun toString(): String {
                    return "Cape(id=$id, state=$state, url=$url, alias=$alias)"
                }
            }
        }
    }

}