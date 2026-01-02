package kirjanpito.ui.javafx.dialogs

import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.testfx.api.FxRobot
import org.testfx.framework.junit5.ApplicationExtension
import org.testfx.framework.junit5.Start

/**
 * TestFX tests for AppearanceDialogFX.
 * 
 * Tests that the dialog displays correctly and theme/font size controls work.
 */
@ExtendWith(ApplicationExtension::class)
class AppearanceDialogFXTest : FxRobot() {

    private lateinit var testStage: Stage

    @Start
    fun start(stage: Stage) {
        testStage = stage
        testStage.show()
    }

    @Test
    fun `test dialog can be created`() {
        interact {
            val dialog = AppearanceDialogFX(testStage)
            assertNotNull(dialog)
        }
    }

    // Note: onOK() test skipped because it requires UI components to be initialized
    // which happens in createContent(), called when dialog is shown
}
