/**
 * Created by Sergey on 09.05.2015.
 */

fun Double.format(digits: Int) = java.lang.String.format("%.${digits}f", this)