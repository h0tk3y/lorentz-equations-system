import org.math.plot.Plot2DPanel
import org.math.plot.Plot3DPanel
import org.math.plot.plots.ScatterPlot
import org.math.plot.render.AbstractDrawer
import java.awt.*
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale
import javax.swing.*
import kotlin.platform.platformStatic
import kotlin.swing.*

/**
 * Created by Sergey on 09.05.2015.
 */

class Demo() {

    val defaultR = 28.0
    val defaultX0 = 3.0
    val defaultY0 = 3.0
    val defaultZ0 = 3.0

    val defaultSigma = 10.0
    val defaultB = 8.0 / 3
    val defaultDt = 0.01
    val defaultMaxT = 75.0

    val format = NumberFormat.getNumberInstance(Locale.getDefault())
    val rInput = JFormattedTextField(format)
    val x0Input = JFormattedTextField(format)
    val y0Input = JFormattedTextField(format)
    val z0Input = JFormattedTextField(format)
    val dtInput = JFormattedTextField(format)
    val btnApply = JButton("Apply")

    init {
        rInput.setValue(defaultR)
        x0Input.setValue(defaultX0)
        y0Input.setValue(defaultY0)
        z0Input.setValue(defaultZ0)
        dtInput.setValue(defaultDt)

        val inputs = listOf(rInput, x0Input, y0Input, z0Input, dtInput)
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

        add(btnApply)

        setPreferredSize(Dimension(300, 24))
        setMaximumSize(Dimension(300, 24))
    }

    val plot3d = Plot3DPanel()
    val plot2d = Plot2DPanel()

    init {
        plot2d.setAxisLabel(0, "t")
        plot2d.setAxisLabel(1, "f(t)")
    }

    init {
        plot3d.setPreferredSize(Dimension(500, 400))
    }

    val window = frame("Main") {
        minimumWidth = 600
        minimumHeight = 500

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

    var currentSolver = EulerExplicitSolver(defaultDt, defaultMaxT)

    fun apply() {
        val r = (rInput.getValue() as Number).toDouble()
        val x0 = (x0Input.getValue() as Number).toDouble()
        val y0 = (y0Input.getValue() as Number).toDouble()
        val z0 = (z0Input.getValue() as Number).toDouble()
        val dt = (dtInput.getValue() as Number).toDouble()

        val system = LorentzEquationsSystem(defaultSigma, defaultB, r, x0, y0, z0)
        val solution = currentSolver.solve(system)
        val (xs, ys, zs) = coordsFromSolution(solution, dt, defaultMaxT)

        if (listOf(xs, ys, zs) any { it any { x -> !java.lang.Double.isFinite(x) } }) {
            JOptionPane.showMessageDialog(window, "Calculated data is not finite, aborting");
            return
        }

        SwingUtilities.invokeLater {
            plot3d.removeAllPlots()
            plot3d.addScatterPlot("Phase space", xs, ys, zs)

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

    data class CoordsArray(val xs: DoubleArray, val ys: DoubleArray, val zs: DoubleArray)

    fun coordsFromSolution(s: List<(Double) -> Double>, dt: Double, maxT: Double): CoordsArray {
        val xs = arrayListOf<Double>()
        val ys = arrayListOf<Double>()
        val zs = arrayListOf<Double>()

        for (t in 0.0..maxT step dt) {
            xs add s[0](t)
            ys add s[1](t)
            zs add s[2](t)
        }

        val xss = DoubleArray(xs.size())
        val yss = DoubleArray(ys.size())
        val zss = DoubleArray(zs.size())
        for (i in xs.indices) {
            xss[i] = xs[i]
            yss[i] = ys[i]
            zss[i] = zs[i]
        }

        return CoordsArray(xss, yss, zss)
    }

    companion object {
        platformStatic fun main(args: Array<String>) {
            //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            Demo().start()
        }
    }
}
