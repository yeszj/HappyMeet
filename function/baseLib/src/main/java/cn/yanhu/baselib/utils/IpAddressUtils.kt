package cn.yanhu.baselib.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import cn.yanhu.baselib.utils.ext.logcom
import cn.zj.netrequest.application.ApplicationProxy
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.util.Collections

object IpAddressUtils {

    val ip: String?
        get() {
            val ip: String?
            val conMann = ApplicationProxy.instance.getApplication()
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val mobileNetworkInfo = conMann.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
            val wifiNetworkInfo = conMann.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            if (mobileNetworkInfo?.isConnected == true) {
                ip = localIpAddress
                return ip
            } else if (wifiNetworkInfo?.isConnected == true) {
                val wifiManager =
                    ApplicationProxy.instance.getApplication().applicationContext.getSystemService(
                        Context.WIFI_SERVICE
                    ) as WifiManager
                val wifiInfo = wifiManager.connectionInfo
                val ipAddress = wifiInfo.ipAddress
                ip = intToIp(ipAddress)
                return ip
            }
            return ""
        }

    private val localIpAddress: String?
        //    如果连接的是移动网络，第二步，获取本地ip地址：getLocalIpAddress();这样获取的是ipv4格式的ip地址。
        get() {
            try {
                val ipv4: String
                val nilist: ArrayList<*> = Collections.list(NetworkInterface.getNetworkInterfaces())
                for (ni in nilist) {
                    val ialist: ArrayList<*> =
                        Collections.list((ni as NetworkInterface).inetAddresses)
                    for (address in ialist) {
                        val address_ = address as InetAddress
                        if (!address_.isLoopbackAddress && address is Inet4Address) {
                            ipv4 = address_.hostAddress
                            return ipv4
                        }
                    }
                }
            } catch (ex: SocketException) {
                logcom(ex.toString())
            }
            return null
        }

    //    如果连接的是WI-FI网络，第三步，获取WI-FI ip地址：intToIp(ipAddress);
    private fun intToIp(ipInt: Int): String {
        val sb = StringBuilder()
        sb.append(ipInt and 0xFF).append('.')
        sb.append(ipInt shr 8 and 0xFF).append('.')
        sb.append(ipInt shr 16 and 0xFF).append('.')
        sb.append(ipInt shr 24 and 0xFF)
        return sb.toString()
    }
}