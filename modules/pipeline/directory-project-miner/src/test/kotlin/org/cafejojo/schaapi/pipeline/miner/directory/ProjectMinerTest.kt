package org.cafejojo.schaapi.pipeline.miner.directory

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
import java.net.URLDecoder

internal class ProjectMinerTest : Spek({
    fun getResourceAsFile(path: String) =
        File(URLDecoder.decode(ProjectMinerTest::class.java.getResource(path).path, "UTF-8"))

    describe("directory project miner") {
        lateinit var miner: ProjectMiner
        lateinit var packer: (File) -> Project

        beforeEachTest {
            packer = mock {
                on { it.invoke(any()) }.thenReturn(mock {})
            }
            miner = ProjectMiner(packer)
        }

        it("finds no projects if the directory is invalid") {
            miner.mine(SearchOptions(File("does_not_exist")))

            verifyNoMoreInteractions(packer)
        }

        it("finds a single file-based project") {
            miner.mine(SearchOptions(getResourceAsFile("/one-file-project")))

            verify(packer, times(1)).invoke(any())
        }

        it("finds multiple file-based projects") {
            miner.mine(SearchOptions(getResourceAsFile("/multiple-file-projects")))

            verify(packer, times(3)).invoke(any())
        }

        it("finds a single directory-based project") {
            miner.mine(SearchOptions(getResourceAsFile("/one-directory-project")))

            verify(packer, times(1)).invoke(any())
        }

        it("finds multiple directory-based projects") {
            miner.mine(SearchOptions(getResourceAsFile("/multiple-directory-projects")))

            verify(packer, times(3)).invoke(any())
        }

        it("finds both file- and directory-based projects") {
            miner.mine(SearchOptions(getResourceAsFile("/mixed-projects")))

            verify(packer, times(4)).invoke(any())
        }
    }
})
