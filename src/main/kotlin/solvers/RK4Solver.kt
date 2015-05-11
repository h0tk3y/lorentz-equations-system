/**
 * Created by Sergey on 11.05.2015.
 */

public class RK4Solver : Solver {
    override fun solve(system: EquationsSystem, dt: Double, maxT: Double): List<(Double) -> Double> {
        val nodesY = Array(system.size, { arrayListOf(system.y0[it]) })
        for (t in 0.0..maxT step dt) { // t is prev iteration time, not t_i+1 but t_i
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
        val pieces = nodesY[0].size()
        val result = Array(nodesY.size(), { i ->
            PiecewiseLinear(Array(pieces, { piece ->
                Point(dt * piece, nodesY[i][piece])
            }))
        })
        return result map { it -> { x: Double -> it(x) } }
    }

}