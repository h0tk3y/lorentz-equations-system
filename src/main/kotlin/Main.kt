import org.math.plot.Plot2DPanel
import org.math.plot.Plot3DPanel
import org.math.plot.plots.ScatterPlot
import org.math.plot.render.AbstractDrawer
import java.awt.*
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Arrays
import java.util.Locale
import javax.swing.*
import kotlin.platform.platformStatic
import kotlin.swing.*

/**
 * Created by Sergey on 09.05.2015.
 */

class Demo() {

    val defaultR = 28.0
    val defaultX0 = 10.0
    val defaultY0 = 10.0
    val defaultZ0 = 10.0

    val defaultSigma = 10.0
    val defaultB = 8.0 / 3
    val defaultDt = 0.01
    val defaultMaxT = 75.0

    val solvers = mapOf(Pair("Explicit Euler solver", EulerExplicitSolver()),
                        Pair("Implicit Euler solver", EulerLorentzImplicitSolver()),
                        Pair("RK4 solver", RK4Solver()),
                        Pair("Adams predict-correct solver", AdamsPredictCorrectSolver()))
    val solversNames: Array<String> = Array(solvers.keySet().size(), { solvers.keySet().toList()[it] })

    val format = NumberFormat.getNumberInstance(Locale.getDefault())
    val rInput = JFormattedTextField(format)
    val x0Input = JFormattedTextField(format)
    val y0Input = JFormattedTextField(format)
    val z0Input = JFormattedTextField(format)
    val dtInput = JFormattedTextField(format)
    val maxTInput = JFormattedTextField(format)
    val btnApply = JButton("Apply")
    val comboSolver = JComboBox(solversNames)
    val cbLine = JCheckBox("Lines")

    init {
        rInput.setValue(defaultR)
        x0Input.setValue(defaultX0)
        y0Input.setValue(defaultY0)
        z0Input.setValue(defaultZ0)
        dtInput.setValue(defaultDt)
        maxTInput.setValue(defaultMaxT)

        val inputs = listOf(rInput, x0Input, y0Input, z0Input, dtInput, maxTInput)
        inputs forEach { input ->
            input.addFocusListener(object : FocusListener {
                override fun focusLost(e: FocusEvent) {}

                override fun focusGained(e: FocusEvent) {
                    SwingUtilities.invokeLater {
                        input.selectAll()
                    }
                }
            })
            input.addActionListener { apply() }
        }
        btnApply.addActionListener { apply() }

        comboSolver.setSelectedIndex(0)
        comboSolver.addActionListener { apply() }
        cbLine.addActionListener { apply() }
    }

    val controls = panel {
        setLayout(BoxLayout(this, BoxLayout.LINE_AXIS))

        add(JLabel("r = "))
        add(rInput)
        add(Box.createRigidArea(Dimension(10, 10)))

        add(JLabel("x = "))
        add(x0Input)
        add(Box.createRigidArea(Dimension(10, 10)))

        add(JLabel("y = "))
        add(y0Input)
        add(Box.createRigidArea(Dimension(10, 10)))

        add(JLabel("z = "))
        add(z0Input)
        add(Box.createRigidArea(Dimension(10, 10)))

        add(JLabel("Δt = "))
        add(dtInput)
        add(Box.createRigidArea(Dimension(10, 10)))

        add(JLabel("tMax = "))
        add(maxTInput)
        add(Box.createRigidArea(Dimension(10, 10)))

        add(comboSolver)
        add(Box.createRigidArea(Dimension(10, 10)))

        add(btnApply)

        setPreferredSize(Dimension(300, 24))
        setMaximumSize(Dimension(300, 24))
    }

    val plot3d = Plot3DPanel()
    val plot2d = Plot2DPanel()

    init {
        plot2d.setAxisLabel(0, "t")
        plot2d.setAxisLabel(1, "f(t)")
        plot3d.plotToolBar.add(cbLine)
    }

    init {
        plot3d.setPreferredSize(Dimension(500, 400))
    }

    val window = frame("Lorentz equations system") {
        minimumWidth = 650
        minimumHeight = 550

        add(panel {
            setLayout(BorderLayout())

            add(controls, BorderLayout.NORTH)

            val tabs = JTabbedPane()
            tabs.add("Phase space", plot3d)
            tabs.add("Coordinates", plot2d)
            add(tabs)
        })

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
    }

    fun start() {
        window.setVisible(true)
        apply()
    }

    fun apply() {
        val solver = solvers[comboSolver.getSelectedItem()]!!

        val r = (rInput.getValue() as Number).toDouble()
        val x0 = (x0Input.getValue() as Number).toDouble()
        val y0 = (y0Input.getValue() as Number).toDouble()
        val z0 = (z0Input.getValue() as Number).toDouble()
        val dt = (dtInput.getValue() as Number).toDouble()
        val maxT = (maxTInput.getValue() as Number).toDouble()

        val system = LorentzEquationsSystem(defaultSigma, defaultB, r, x0, y0, z0)
        val solution = solver.solve(system, dt, maxT)
        val (xs, ys, zs) = coordinatesFromSolution(solution, dt, maxT)

        if (listOf(xs, ys, zs) any { it any { x -> !java.lang.Double.isFinite(x) } }) {
            JOptionPane.showMessageDialog(window, "Calculated data is not finite, aborting");
            return
        }

        SwingUtilities.invokeLater {
            plot3d.removeAllPlots()
            if (cbLine.isSelected()) {
                plot3d.addLinePlot("Phase space", xs, ys, zs)
            } else {
                plot3d.addScatterPlot("Phase space", xs, ys, zs)
            }

            val ts = DoubleArray(xs.size())
            for (i in xs.indices)
                ts[i] = i * dt
            plot2d.removeAllPlots()
            plot2d.plotLegend.setVisible(true)
            plot2d.addLinePlot("x(t)", Color.RED, ts, xs)
            plot2d.addLinePlot("y(t)", Color.GREEN, ts, ys)
            plot2d.addLinePlot("z(t)", Color.BLUE, ts, zs)
        }
    }

    data class CoordinatesArray(val xs: DoubleArray, val ys: DoubleArray, val zs: DoubleArray)

    fun coordinatesFromSolution(s: List<(Double) -> Double>, dt: Double, maxT: Double): CoordinatesArray {
        val n = Math.ceil(maxT / dt).toInt()
        val xs = DoubleArray(n)
        val ys = DoubleArray(n)
        val zs = DoubleArray(n)

        for (i in 0..n-1) {
            xs[i] = s[0](i * dt)
            ys[i] = s[1](i * dt)
            zs[i] = s[2](i * dt)
        }

        return CoordinatesArray(xs, ys, zs)
    }

    companion object {
        platformStatic fun main(args: Array<String>) {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            Demo().start()
        }
    }
}
