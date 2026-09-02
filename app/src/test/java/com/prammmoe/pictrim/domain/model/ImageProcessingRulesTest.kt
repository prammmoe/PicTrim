package com.prammmoe.pictrim.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ImageProcessingRulesTest {
    @Test fun `width keeps original aspect ratio`() = assertEquals(1920 to 1440, ImageProcessingRules.dimensions(4000, 3000, ResizeOptions(width = 1920)))
    @Test fun `percentage resizes both dimensions`() = assertEquals(1000 to 750, ImageProcessingRules.dimensions(4000, 3000, ResizeOptions(percentage = 25)))
    @Test fun `free dimensions retain both requested values`() = assertEquals(1000 to 1000, ImageProcessingRules.dimensions(4000, 3000, ResizeOptions(width = 1000, height = 1000, keepAspectRatio = false)))
    @Test fun `target size supports lossy formats only`() { assertTrue(ImageProcessingRules.supportsTargetSize(OutputFormat.JPEG)); assertTrue(ImageProcessingRules.supportsTargetSize(OutputFormat.WEBP)); assertFalse(ImageProcessingRules.supportsTargetSize(OutputFormat.PNG)) }
    @Test fun `mime maps supported output formats`() { assertEquals(OutputFormat.JPEG, ImageProcessingRules.formatFromMime("image/jpeg")); assertEquals(OutputFormat.PNG, ImageProcessingRules.formatFromMime("image/png")); assertNull(ImageProcessingRules.formatFromMime("image/gif")) }
    @Test fun `saving percentage is calculated from byte sizes`() = assertEquals(85, ImageProcessingRules.savingsPercent(4_800_000, 720_000))
}
