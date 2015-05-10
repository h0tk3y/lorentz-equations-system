/**
 * Created by Sergey on 09.05.2015.
 */

trait Solver {
    open fun solve(system: EquationsSystem, dt: Double, maxT: Double): List<(Double) -> Double>
}