package org.com.syun0521.minecraft.narouskinpacks.skin

// Skinクラスは、ゲーム内で特定の動作をした時のパーティクルエフェクトに関連するプロパティとメソッドをまとめたクラスです。
// このクラスには、パーティクルの種類、オフセット、速度、前方オフセット、および量に関するプロパティが含まれます。
// これらのプロパティを設定するためのメソッドを提供し、toStringメソッドをオーバーライドして簡単に表現できるようにしています。
data class Skin(
    var skinName: String,
    var particle: String,
    var type: String,
    var amount: Int,
    var x: Double,
    var y: Double,
    var z: Double,
    var speed: Double,
    var forwardOffset: Double
) {
    fun setOffsets(x: Double, y: Double, z: Double) {
        this.x = x
        this.y = y
        this.z = z
    }

    override fun toString(): String {
        return "Skin(skinName='$skinName', particle='$particle', type='$type', amount=$amount, x=$x, y=$y, z=$z, speed=$speed, forwardOffset=$forwardOffset)"
    }
}
