package org.cafejojo.schaapi.pipeline.github.mavenprojectminer

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.nio.file.Files

class ProjectMinerTest : Spek({
    val output = Files.createTempDirectory("project-miner").toFile()

    afterGroup { output.deleteRecursively() }

    describe("when extracting project names from a json object returned by the github v3 api") {
        it("should extract the full project name") {
            val json =
                "{\n" +
                    "  \"total_count\": 7,\n" +
                    "  \"incomplete_results\": false,\n" +
                    "  \"items\": [\n" +
                    "    {\n" +
                    "      \"name\": \"classes.js\",\n" +
                    "      \"repository\": {\n" +
                    "        \"id\": 167174,\n" +
                    "        \"name\": \"jquery\",\n" +
                    "        \"full_name\": \"jquery/jquery\",\n" +
                    "        \"owner\": {\n" +
                    "          \"login\": \"jquery\",\n" +
                    "        },\n" +
                    "      },\n" +
                    "    },\n" +
                    "    {" +
                    "      \"name\": \"otherclasses.js\",\n" +
                    "      \"repository\": {\n" +
                    "        \"id\": 123123123,\n" +
                    "        \"name\": \"jquery\",\n" +
                    "        \"full_name\": \"otherjquery/jquery\",\n" +
                    "        \"owner\": {\n" +
                    "          \"login\": \"otherjquery\",\n" +
                    "        },\n" +
                    "      },\n" +
                    "    }\n" +

                    "  ]\n" +
                    "}"

            val names = ProjectMiner("", "", output, ::testProjectPacker)
                .getProjectNames(json)

            assertThat(names).contains("jquery/jquery")
            assertThat(names).contains("otherjquery/jquery")
        }
    }
})
