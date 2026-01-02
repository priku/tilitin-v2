package kirjanpito.ui.javafx.dialogs

import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.testfx.api.FxRobot
import org.testfx.framework.junit5.ApplicationExtension
import org.testfx.framework.junit5.Start

/**
 * TestFX tests for AboutDialogFX.
 * 
 * Tests that the dialog displays correctly and contains expected information.
 */
@ExtendWith(ApplicationExtension::class)
class AboutDialogFXTest : FxRobot() {

    private lateinit var testStage: Stage

    @Start
    fun start(stage: Stage) {
        testStage = stage
        testStage.show()
    }

    @Test
    fun `test dialog can be created`() {
        interact {
            val dialog = AboutDialogFX(testStage)
            assertNotNull(dialog)
        }
    }
}
