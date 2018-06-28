package org.cafejojo.schaapi

import mu.KLogging
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.cafejojo.schaapi.models.project.JavaMavenProject
import java.io.File

abstract class Snippet {
    abstract fun addOptionsTo(options: Options): Options

    abstract fun setUp(cmd: CommandLine)
}

class MavenSnippet : Snippet() {
    lateinit var dir: File
    var repair = false

    override fun addOptionsTo(options: Options): Options =
        options
            .addOption(Option
                .builder()
                .longOpt("maven_dir")
                .desc("The directory to run Maven from.")
                .hasArg()
                .build())
            .addOption(Option
                .builder()
                .longOpt("repair_maven")
                .desc("Repairs the Maven installation.")
                .build())

    override fun setUp(cmd: CommandLine) {
        dir = File(cmd.getOptionValue("maven_dir") ?: JavaMavenProject.DEFAULT_MAVEN_HOME.absolutePath)
        repair = cmd.hasOption("repair_maven")
    }
}

class GitHubMinerSnippet : Snippet() {
    companion object : KLogging()

    lateinit var token: String
    var maxProjects = 0
    lateinit var groupId: String
    lateinit var artifactId: String
    lateinit var version: String

    override fun addOptionsTo(options: Options): Options {
        return options
            .addOption(Option
                .builder()
                .longOpt("github_oauth_token")
                .desc("Token of GitHub account used for searching.")
                .hasArg()
                .required()
                .build())
            .addOption(Option
                .builder()
                .longOpt("max_projects")
                .desc("Maximum amount of projects to download from GitHub.")
                .hasArg()
                .build())
            .addOption(Option
                .builder()
                .longOpt("library_group_id")
                .desc("Group id of library mined projects should have a dependency on.")
                .hasArg()
                .required()
                .build())
            .addOption(Option
                .builder()
                .longOpt("library_artifact_id")
                .desc("Artifact id of library mined projects should have a dependency on.")
                .hasArg()
                .required()
                .build())
            .addOption(Option
                .builder()
                .longOpt("library_version")
                .desc("Version of library mined projects should have a dependency on.")
                .hasArg()
                .required()
                .build())
            .addOption(Option
                .builder()
                .longOpt("sort_by_stargazers")
                .desc("True if GitHub projects should be sorted by stars.")
                .hasArg(false)
                .build())
            .addOption(Option
                .builder()
                .longOpt("sort_by_watchers")
                .desc("True if GitHub projects should be sorted by watchers.")
                .hasArg(false)
                .build())
    }

    override fun setUp(cmd: CommandLine) {
        token = cmd.getOptionValue("github_oauth_token")
        maxProjects = cmd.getOptionValue("max_projects", DEFAULT_MAX_PROJECTS).toInt()
        groupId = cmd.getOptionValue("library_group_id")
        artifactId = cmd.getOptionValue("library_artifact_id")
        version = cmd.getOptionValue("library_version")

        if (cmd.hasOption("sort_by_stargazers") && cmd.hasOption("sort_by_watchers")) {
            logger.error { "Cannot sort repositories on both stargazers and watchers." }
        }
    }
}

class DirectoryMinerSnippet : Snippet() {
    lateinit var userDirDir: File

    override fun addOptionsTo(options: Options): Options {
        return options
            .addOption(Option
                .builder("u")
                .longOpt("user_base_dir")
                .desc("The directory containing user project directories.")
                .hasArg()
                .required()
                .build())
    }

    override fun setUp(cmd: CommandLine) {
        userDirDir = File(cmd.getOptionValue("u"))
    }
}
