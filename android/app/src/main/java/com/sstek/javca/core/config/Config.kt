package com.sstek.javca.core.config

// Her firebase ip değiştiği zaman bunu res/xml/network_security_config.xml dosyasına
// ekleyip o adresi güvenli kabul etmesi gerekiyor.
// TODO(Bu problemi çöz)

// sudo ufw allow <PORT> tüm portlar girilerek
// güvenlik duvarından dolayı ulaşamama problemi çözülmeli
object Config {
    val FIREBASE_HOST_IP = "192.168.1.53"

    val FIREBASE_AUTHENTICATION_PORT = 9099
    val FIREBASE_DATABASE_PORT = 9000

    val CAMERA_WIDTH: Int = 1280
    val CAMERA_HEIGHT: Int = 720
    val CAMERA_FPS: Int = 30
}