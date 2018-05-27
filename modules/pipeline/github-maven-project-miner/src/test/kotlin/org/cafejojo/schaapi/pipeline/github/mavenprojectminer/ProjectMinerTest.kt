package org.cafejojo.schaapi.pipeline.github.mavenprojectminer

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.nio.file.Files

class ProjectMinerTest : Spek({
    val output = Files.createTempDirectory("project-miner").toFile()

    afterGroup { output.deleteRecursively() }

    describe("when extracting project names from a json object returned by the GitHub v3 api") {
        it("should extract the full project name") {
            val json =
                """
                    {
                      "total_count": 7,
                      "incomplete_results": false,
                      "items": [
                        {
                          "name": "classes.js",
                          "repository": {
                            "id": 167174,
                            "name": "jquery",
                            "full_name": "jquery/jquery",
                            "owner": {
                              "login": "jquery",
                            },
                          },
                        },
                        {      "name": "otherclasses.js",
                          "repository": {
                            "id": 123123123,
                            "name": "jquery",
                            "full_name": "otherjquery/jquery",
                            "owner": {
                              "login": "otherjquery",
                            },
                          },
                        }
                      ]
                    }""".trimIndent()

            val names = ProjectMiner("", "", output, ::testProjectPacker)
                .getProjectNames(json)

            assertThat(names).contains("jquery/jquery")
            assertThat(names).contains("otherjquery/jquery")
        }
    }
})
