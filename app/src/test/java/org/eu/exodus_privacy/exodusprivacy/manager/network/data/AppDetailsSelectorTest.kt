package org.eu.exodus_privacy.exodusprivacy.manager.network.data

import org.eu.exodus_privacy.exodusprivacy.objects.Source
import org.junit.Assert
import org.junit.Test

class AppDetailsSelectorTest {

    private fun details(
        source: String,
        versionCode: String,
        trackers: List<Int>,
    ) = AppDetails(source = source, version_code = versionCode, trackers = trackers)

    private val fdroidReport = details("fdroid", "100", trackers = emptyList())
    private val googleReport = details("google", "100", trackers = listOf(1, 2))

    @Test
    fun `prefers the report matching the installed source for the same version code`() {
        val result = selectAppDetails(listOf(googleReport, fdroidReport), versionCode = 100, source = Source.FDROID)
        Assert.assertEquals(fdroidReport, result)
    }

    @Test
    fun `prefers the Google report for a Google install`() {
        val result = selectAppDetails(listOf(fdroidReport, googleReport), versionCode = 100, source = Source.GOOGLE)
        Assert.assertEquals(googleReport, result)
    }

    @Test
    fun `falls back to the highest version code of the matching source when no exact version match`() {
        val fdroidOld = details("fdroid", "90", trackers = emptyList())
        val fdroidNew = details("fdroid", "110", trackers = emptyList())
        val googleNewest = details("google", "120", trackers = listOf(1, 2))

        val result =
            selectAppDetails(
                listOf(fdroidOld, googleNewest, fdroidNew),
                versionCode = 999,
                source = Source.FDROID,
            )

        Assert.assertEquals(fdroidNew, result)
    }

    @Test
    fun `ignores source for non-store installs and matches on version code`() {
        val result = selectAppDetails(listOf(googleReport, fdroidReport), versionCode = 100, source = Source.USER)
        // No source preference: first version-code match wins, preserving previous behaviour.
        Assert.assertEquals(googleReport, result)
    }

    @Test
    fun `falls back to the highest version code overall when source is unknown and no exact match`() {
        val older = details("google", "50", trackers = listOf(1))
        val newer = details("google", "70", trackers = listOf(1, 2))
        val result = selectAppDetails(listOf(older, newer), versionCode = 999, source = Source.SYSTEM)
        Assert.assertEquals(newer, result)
    }

    @Test
    fun `returns an empty AppDetails for an empty list`() {
        val result = selectAppDetails(emptyList(), versionCode = 100, source = Source.GOOGLE)
        Assert.assertEquals(AppDetails(), result)
    }

    @Test
    fun `single report is always returned`() {
        val result = selectAppDetails(listOf(googleReport), versionCode = 100, source = Source.FDROID)
        Assert.assertEquals(googleReport, result)
    }
}
