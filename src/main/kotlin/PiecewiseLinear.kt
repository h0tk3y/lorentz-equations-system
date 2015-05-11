/**
 * Created by Sergey on 09.05.2015.
 */

public data class Point(val x: Double, val y: Double)

public class PiecewiseLinear(private val points: Array<Point>) {
    init {
        if (points.size() < 2)
            throw IllegalArgumentException("At least two pieces needed")
    }

    fun invoke(x: Double): Double {
        if (x !in domain)
            throw IllegalArgumentException("Undefined outside $domain")
        for (i in points.indices) {
            if (x >= points[i].x && x <= points[i+1].x)
                return (points[i+1].y - points[i].y) * (x - points[i].x) + points[i].y
        }
        return 0.0
    }

    val domain: Range<Double>
        get() = DoubleRange(points.first().x, points.last().x)
}