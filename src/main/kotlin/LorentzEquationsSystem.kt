/**
 * Created by Sergey on 09.05.2015.
 */

public class LorenzEquationsSystem(val sigma: Double, val b: Double, val r: Double,
                                    val x0: Double, y0: Double, z0: Double)
: EquationsSystem {

    override val y0: DoubleArray = doubleArray(x0, y0, z0)
    override val size: Int = 3

    override fun get(i: Int): (List<Double>) -> Double = when (i) {
        0 -> { y -> -sigma * y[0] + sigma * y[1] }
        1 -> { y -> y[0] * (r - y[2]) - y[1] }
        2 -> { y -> y[0] * y[1] - b * y[2] }
        else -> throw IllegalArgumentException()
    }

}