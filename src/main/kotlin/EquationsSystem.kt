/**
 * Created by Sergey on 09.05.2015.
 */

trait EquationsSystem {
    public fun f(i: Int): (vararg List<Double>) -> Double = get(i)
    public fun get(i: Int): (vararg List<Double>) -> Double

    public val size: Int
    public val y0: DoubleArray
}