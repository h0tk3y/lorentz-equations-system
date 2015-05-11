import java.util.ArrayList

/**
 * Created by Sergey on 09.05.2015.
 */

class EulerExplicitSolver() : Solver {
    override fun solve(system: EquationsSystem, dt: Double, maxT: Double): List<(Double) -> Double> {
        val nodesY = Array(system.size, { arrayListOf(system.y0[it]) })
        for (t in 0.0..maxT step dt) {
            val prevI = nodesY[0].lastIndex
            for (i in nodesY.indices) {
                val yPrev = nodesY[i].last()
                val y = yPrev + dt * system.f(i)(Array(nodesY.size(), { nodesY[it][prevI] }).asList())
                nodesY[i] add y
            }
        }
        val pieces = nodesY[0].size()
        val result = Array(nodesY.size(), { i ->
            PiecewiseLinear(Array(pieces, { piece ->
                Point(dt * piece, nodesY[i][piece])
            }))
        })
        return result map { it -> { x: Double -> it(x) } }
    }

}

class EulerLorenzImplicitSolver() : Solver {
    override fun solve(system: EquationsSystem, dt: Double, maxT: Double): List<(Double) -> Double> {
        if (system !is LorenzEquationsSystem)
            throw IllegalArgumentException("This solver can only solve Lorenz systems")

        val si = system.sigma
        val r = system.r
        val b = system.b

        val nodesY = Array(system.size, { arrayListOf(system.y0[it]) })
        for (t in 0.0..maxT step dt) {
            val prevI = nodesY[0].lastIndex
            val x0 = nodesY[0][prevI]
            val y0 = nodesY[1][prevI]
            val z0 = nodesY[2][prevI]

            val x1 = (x0 + dt * x0 + dt * si * y0) / (1 + dt * (1 + si) + dt * dt * si * (1 - r + z0))
            val y1 = (y0 + dt * (r * x0 + si * y0 - x0 * z0)) / (1 + dt * (1 + si) + dt * dt * si * (1 - r + z0))
            val z1 = (z0 + (dt pow 4) * si * si * z0 * ((1 - r + z0) pow 2) + dt * (x0 * y0 + 2 * (1 + si) * z0) +
                    dt * dt * ((1 + si) * x0 * y0 + si * y0 * y0 + z0 + 4 * si * z0 + si * si * z0 - x0 * x0 * z0 + 2 * si * z0 * z0 +
                            r * (x0 * x0 - 2 * si * z0)) + (dt pow 3) * ((-x0 * x0) * z0 + r * (x0 * x0 + si * x0 * y0 - 2 * si * (1 + si) * z0) +
                    si * si * (y0 * y0 + 2 * z0 * (1 + z0)) + si * (2 * z0 * (1 + z0) + x0 * (y0 - y0 * z0)))) /
                    ((1 + b * dt) * ((1 + dt * (1 + si) + dt * dt * si * (1 - r + z0)) pow 2))

            //the above is the analytic solution by Wolfram Mathematica

            nodesY[0] add x1
            nodesY[1] add y1
            nodesY[2] add z1
        }
        val pieces = nodesY[0].size()
        val result = Array(nodesY.size(), { i ->
            PiecewiseLinear(Array(pieces, { piece ->
                Point(dt * piece, nodesY[i][piece])
            }))
        })
        return result map { it -> { x: Double -> it(x) } }
    }

    fun Double.pow(i: Int) = Math.pow(this, i.toDouble())
}