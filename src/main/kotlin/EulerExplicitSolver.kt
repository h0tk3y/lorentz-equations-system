import java.util.ArrayList

/**
 * Created by Sergey on 09.05.2015.
 */

class EulerExplicitSolver(val deltaT: Double, val maxT: Double) : Solver {
    override fun solve(system: EquationsSystem, dt: Double, maxT: Double): List<(Double) -> Double> {
        val nodesY = Array(system.size, { arrayListOf(system.y0[it]) })
        for (t in 0.0..maxT step deltaT) {
            val prevI = nodesY[0].lastIndex
            for (i in nodesY.indices) {
                val yPrev = nodesY[i].last()
                val y = yPrev + deltaT * system.f(i)(Array(nodesY.size(), { nodesY[it][prevI] }).asList())
                nodesY[i] add y
            }
        }
        val pieces = nodesY[0].size()
        val result = Array(nodesY.size(), { i ->
            PiecewiseLinear(Array(pieces, { piece ->
                Point(deltaT * piece, nodesY[i][piece])
            }))
        })
        return result map { it -> { x: Double -> it(x) } }
    }

}

class EulerLorentzImplicitSolver(): Solver {
    override fun solve(system: EquationsSystem, dt: Double, maxT: Double): List<(Double) -> Double> {
        if (system !is LorentzEquationsSystem)
            throw IllegalArgumentException("This solver can only solve Lorentz systems")

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