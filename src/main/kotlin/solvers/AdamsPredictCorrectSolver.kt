/**
 * Created by Sergey on 11.05.2015.
 */

public class AdamsPredictCorrectSolver : Solver {

    override fun solve(system: EquationsSystem, dt: Double, maxT: Double): List<(Double) -> Double> {
        val nodesY = Array(system.size, { arrayListOf(system.y0[it]) })
        for (n in 0..3) { // calculate initial approx with RK4 method
            val prevI = nodesY[0].lastIndex
            val k1 = Array(system.size) {
                dt * system.f(it)(nodesY.map { it[prevI] })
            }
            val k2 = Array(system.size) {
                dt * system.f(it)(nodesY.mapIndexed { i, list -> list[prevI] + k1[i]/2})
            }
            val k3 = Array(system.size) {
                dt * system.f(it)(nodesY.mapIndexed { i, list -> list[prevI] + k2[i]/2})
            }
            val k4 = Array(system.size) {
                dt * system.f(it)(nodesY.mapIndexed { i, list -> list[prevI] + k3[i]})
            }
            for (i in nodesY.indices) {
                nodesY[i] add nodesY[i][prevI] + 1.0/6 * (k1[i] + 2*k2[i] + 2*k3[i] + k4[i])
            }
        }
        for (t in dt*4..maxT step dt) { // t is prev iteration time, not t_i+1 but t_i
            val prevI = nodesY[0].lastIndex

            val prevs0 = nodesY map { it[prevI] }
            val prevs1 = nodesY map { it[prevI - 1] }
            val prevs2 = nodesY map { it[prevI - 2] }
            val prevs3 = nodesY map { it[prevI - 3] }

            val predict = nodesY.indices map { i ->
                val f = system.f(i)
                nodesY[i][prevI] + dt * (55.0*f(prevs0) - 59.0*f(prevs1) + 37.0*f(prevs2) - 9*f(prevs3)) / 24.0
            }
            for (i in nodesY.indices) {
                val f = system.f(i)
                nodesY[i] add
                        nodesY[i][prevI] + dt * (9.0*f(predict) + 19.0*f(prevs0) - 5.0*f(prevs1) + f(prevs2)) / 24.0
            }
        }
        val pieces = nodesY[0].size()
        val result = nodesY.indices map { i ->
            PiecewiseLinear(Array(pieces, { piece ->
                Point(dt * piece, nodesY[i][piece])
            }))
        }
        return result map { it -> { x: Double -> it(x) } }
    }

}