package de.softmanufaktur.chatgpt

import com.intellij.util.ui.JBUI
import java.awt.*
import java.io.IOException
import java.io.ObjectInputStream
import java.io.Serializable
import kotlin.math.max

/**
 * Ordnet die Komponenten Vertikal an
 */
class VerticalFlowLayout() : LayoutManager, Serializable {
    var myInsets: Insets = JBUI.insets(5)

    override fun addLayoutComponent(name: String, comp: Component) {}

    override fun removeLayoutComponent(comp: Component) {}

    override fun preferredLayoutSize(target: Container): Dimension {
        synchronized(target.treeLock) {
            val dim = Dimension(0, 0)
            val nmembers = target.componentCount

            for (i in 0 until nmembers) {
                val m = target.getComponent(i)
                if (m.isVisible) {
                    val d = m.preferredSize
                    dim.width = max(dim.width.toDouble(), d.width.toDouble()).toInt()

                    dim.height += d.height
                }
            }
            dim.width += myInsets.left + myInsets.right
            dim.height += myInsets.top + myInsets.bottom
            return dim
        }
    }

    override fun minimumLayoutSize(target: Container): Dimension {
        synchronized(target.treeLock) {
            val dim = Dimension(0, 0)
            val nmembers = target.componentCount

            for (i in 0 until nmembers) {
                val m = target.getComponent(i)
                if (m.isVisible) {
                    val d = m.minimumSize
                    dim.width = max(dim.width.toDouble(), d.width.toDouble()).toInt()

                    dim.height += d.height
                }
            }
            dim.width += myInsets.left + myInsets.right
            dim.height += myInsets.top + myInsets.bottom
            return dim
        }
    }

    override fun layoutContainer(target: Container) {
        synchronized(target.treeLock) {
            val compWidth = target.width - (myInsets.left + myInsets.right)
            val nmembers = target.componentCount
            var y = myInsets.top
            val targetHeight = target.height

            val preferredOverallHeight = preferredLayoutSize(target).getHeight()
            var currentPreferredOverallHeight = preferredOverallHeight
            for (i in 0 until nmembers) {
                val m = target.getComponent(i)
                if (m.isVisible) {
                    // Wenn die currentPreferredOverallHeight größer ist als die targetHeight

                    var preferredCompHeight = m.preferredSize.getHeight()
                    val oldPreferredCompHeight = preferredCompHeight
                    if (currentPreferredOverallHeight > targetHeight) {
                        val minimumSize = m.minimumSize.getHeight()
                        // double difference = preferredCompHeight - minimumSize;
                        // Ist die aktuelle totale Größe noch größer als die TargetHeight?
                        // if (currentPreferredOverallHeight > targetHeight) {
                        preferredCompHeight -= (currentPreferredOverallHeight - targetHeight)
                        if (preferredCompHeight < minimumSize) {
                            preferredCompHeight = minimumSize
                        }
                        if (preferredCompHeight > oldPreferredCompHeight) {
                            preferredCompHeight = oldPreferredCompHeight
                        }
                        currentPreferredOverallHeight -= oldPreferredCompHeight - preferredCompHeight
                        //                        } else {
                        //                            preferredCompHeight = targetHeight - y - insets.bottom;
                        //                        }
                    }

                    m.setSize(compWidth, preferredCompHeight.toInt())
                    m.setLocation(myInsets.left, y)
                    y = (y + preferredCompHeight).toInt()
                }
            }
        }
    }

    @Throws(IOException::class, ClassNotFoundException::class)
    private fun readObject(stream: ObjectInputStream) {
        stream.defaultReadObject()
    }

    fun setInsets(insets: Insets) {
        this.myInsets = insets
    }

    companion object {
        const val LEFT: Int = 0
        const val CENTER: Int = 1
        const val RIGHT: Int = 2
        private const val serialVersionUID = -726000875583282631L
    }
}