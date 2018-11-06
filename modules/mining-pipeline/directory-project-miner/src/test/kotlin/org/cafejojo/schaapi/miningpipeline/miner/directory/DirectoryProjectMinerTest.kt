package org.cafejojo.schaapi.miningpipeline.miner.directory

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import org.cafejojo.schaapi.models.Project
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.io.File
import java.lang.UnsupportedOperationException
import java.net.URLDecoder
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path

internal object DirectoryProjectMinerTest : Spek({
    fun getResourceAsFile(path: String) =
        File(URLDecoder.decode(DirectoryProjectMinerTest::class.java.getResource(path).path, "UTF-8"))

    describe("directory project miner") {
        lateinit var miner: DirectoryProjectMiner<Project>
        lateinit var packer: (File) -> Project

        beforeEachTest {
            packer = mock {
                on { it.invoke(any()) }.thenReturn(mock {})
            }
            miner = DirectoryProjectMiner(packer)
        }

        it("finds no projects if the directory is invalid") {
            miner.mine(DirectorySearchOptions(File("does_not_exist")))

            verifyNoMoreInteractions(packer)
        }

        it("finds a single file-based project") {
            miner.mine(DirectorySearchOptions(getResourceAsFile("/one-file-project")))

            verify(packer).invoke(any())
        }

        it("finds multiple file-based projects") {
            miner.mine(DirectorySearchOptions(getResourceAsFile("/multiple-file-projects")))

            verify(packer, times(3)).invoke(any())
        }

        it("finds a single directory-based project") {
            miner.mine(DirectorySearchOptions(getResourceAsFile("/one-directory-project")))

            verify(packer).invoke(any())
        }

        it("finds multiple directory-based projects") {
            miner.mine(DirectorySearchOptions(getResourceAsFile("/multiple-directory-projects")))

            verify(packer, times(3)).invoke(any())
        }

        it("finds both file- and directory-based projects") {
            miner.mine(DirectorySearchOptions(getResourceAsFile("/mixed-projects")))

            verify(packer, times(4)).invoke(any())
        }

        it("excludes hidden files") {
            miner.mine(DirectorySearchOptions(getResourceAsFile("/hidden-projects")))
            val path = getResourceAsFile("/hidden-projects/.hiddenfile").toPath()
            path.hideFileOnWindows()

            verify(packer, times(1)).invoke(any())
        }
    }
})

fun Path.hideFileOnWindows() {
    try {
        val hidden = Files.getAttribute(this, "dos:hidden", LinkOption.NOFOLLOW_LINKS) as Boolean?
        if (hidden != null && !hidden) {
            Files.setAttribute(this, "dos:hidden", true, LinkOption.NOFOLLOW_LINKS)
        }
    } catch (e: UnsupportedOperationException) {
        // DOS view not available on UNIX
    }
}
