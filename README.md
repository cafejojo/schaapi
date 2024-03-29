<p align="center">
    <br>
    <img src="https://user-images.githubusercontent.com/15815208/41853512-38dd02c4-788e-11e8-8e56-7035e66eabb7.png" width="400">
    <br>
    Early detection of breaking changes based on API usage
    <br>
    <br>
    <br>
</p>
    
[![Travis build status][travis-status]](https://travis-ci.org/cafejojo/schaapi)
[![AppVeyor build status][appveyor-status]](https://ci.appveyor.com/project/CafeJojo/schaapi/branch/master)
[![Codecov][codecov-status]](https://codecov.io/gh/cafejojo/schaapi/)
[![Schaap][schaap-status]](https://github.com/cafejojo/schaapi)
[![Built with love][baby-dont-hurt-me]](https://github.com/cafejojo/)

**Schaapi** ensures **S**afe **Cha**nges for **API**s of libraries. It focuses on detection of semantic breaking changes, but provides a general-purpose pipeline that is also applicable to the detection of other types of breaking changes. The default implementation detects incompatibilities between Java libraries and Java projects using that library.

## Requirements and Installation
Schaapi requires JRE 8 and has been tested on Windows and Unix systems.

## Usage
Run the JAR as `java -jar schaapi.jar <flavor> <args>` or run a local build with `gradlew :application:run --args='<flavor> <args>'`.

Set the system property `log.level` to `debug` to enable debug output in the log file. 

### Pipeline Flavor
Schaapi allows you to mine projects from different sources, such as GitHub. In particular, it recognizes two such pipeline "flavors": `directory` (locally sourced projects) and `github` (projects mined from GitHub). Each of these have their own behavior and options. Click on one of the flavors below for a list of command-line options.

<details>
<summary>Directory flavor options</summary>
<p>

```
usage: schaapi -o <arg> [--delete_old_output] -l <arg>
       [--skip_user_compile] [--maven_dir <arg>] [--repair_maven] -u <arg>
       [--library_type <arg>] [--user_compile_timeout <arg>]
       [--pattern_detector_minimum_count <arg>]
       [--pattern_detector_maximum_sequence_length <arg>]
       [--pattern_minimum_library_usage_count <arg>]
       [--test_generator_parallel] [--test_generator_disable_output]
       [--test_generator_timeout <arg>]
 -o,--output_dir <arg>                                 The output
                                                       directory.
    --delete_old_output                                Deletes the output
                                                       directory before
                                                       running the
                                                       pipeline.
 -l,--library_dir <arg>                                The library
                                                       directory.
    --skip_user_compile                                Skip compilation of
                                                       user projects.
    --maven_dir <arg>                                  The directory to
                                                       run Maven from.
    --repair_maven                                     Repairs the Maven
                                                       installation.
 -u,--user_base_dir <arg>                              The directory
                                                       containing user
                                                       project
                                                       directories.
    --library_type <arg>                               The type of
                                                       library.
                                                       [javamaven,
                                                       javajar]
    --user_compile_timeout <arg>                       The maximum number
                                                       of seconds the
                                                       compilation of a
                                                       user project may
                                                       take. Set to 0 to
                                                       disable the
                                                       timeout.
    --pattern_detector_minimum_count <arg>             The minimum number
                                                       of occurrences for
                                                       a statement to be
                                                       considered
                                                       frequent.
    --pattern_detector_maximum_sequence_length <arg>   The maximum length
                                                       of sequences to be
                                                       considered for
                                                       pattern detection.
    --pattern_minimum_library_usage_count <arg>        The minimum number
                                                       of library usages
                                                       per method.
    --test_generator_parallel                          True if test
                                                       generator should
                                                       run in parallel.
                                                       Requires that test
                                                       generator output is
                                                       disabled.
    --test_generator_disable_output                    True if test
                                                       generator output
                                                       should be hidden.
    --test_generator_timeout <arg>                     The time limit per
                                                       pattern for the
                                                       test generator.
```

</p>
</details>

<details>
<summary>GitHub flavor options</summary>
<p>

```
usage: schaapi -o <arg> [--delete_old_output] -l <arg> [--maven_dir <arg>]
       [--repair_maven] --github_oauth_token <arg> [--max_projects <arg>]
       --library_group_id <arg> --library_artifact_id <arg>
       --library_version <arg> [--sort_by_stargazers] [--sort_by_watchers]
       [--version_verification_timeout <arg>] [--library_type <arg>]
       [--user_compile_timeout <arg>]
       [--pattern_minimum_library_usage_count <arg>]
       [--pattern_detector_minimum_count <arg>]
       [--pattern_detector_maximum_sequence_length <arg>]
       [--test_generator_parallel] [--test_generator_disable_output]
       [--test_generator_timeout <arg>]
 -o,--output_dir <arg>                                 The output
                                                       directory.
    --delete_old_output                                Deletes the output
                                                       directory before
                                                       running the
                                                       pipeline.
 -l,--library_dir <arg>                                The library
                                                       directory.
    --maven_dir <arg>                                  The directory to
                                                       run Maven from.
    --repair_maven                                     Repairs the Maven
                                                       installation.
    --github_oauth_token <arg>                         Token of GitHub
                                                       account used for
                                                       searching.
    --max_projects <arg>                               Maximum amount of
                                                       projects to
                                                       download from
                                                       GitHub.
    --library_group_id <arg>                           Group id of library
                                                       mined projects
                                                       should have a
                                                       dependency on.
    --library_artifact_id <arg>                        Artifact id of
                                                       library mined
                                                       projects should
                                                       have a dependency
                                                       on.
    --library_version <arg>                            Version of library
                                                       mined projects
                                                       should have a
                                                       dependency on.
    --sort_by_stargazers                               True if GitHub
                                                       projects should be
                                                       sorted by stars.
    --sort_by_watchers                                 True if GitHub
                                                       projects should be
                                                       sorted by watchers.
    --version_verification_timeout <arg>               The maximum number
                                                       of seconds the
                                                       verification that a
                                                       project uses the
                                                       library may take.
                                                       Set to 0 to disable
                                                       the timeout.
    --library_type <arg>                               The type of
                                                       library.
                                                       [javamaven,
                                                       javajar]
    --user_compile_timeout <arg>                       The maximum number
                                                       of seconds the
                                                       compilation of a
                                                       user project may
                                                       take. Set to 0 to
                                                       disable the
                                                       timeout.
    --pattern_minimum_library_usage_count <arg>        The minimum number
                                                       of library usages
                                                       per method.
    --pattern_detector_minimum_count <arg>             The minimum number
                                                       of occurrences for
                                                       a statement to be
                                                       considered
                                                       frequent.
    --pattern_detector_maximum_sequence_length <arg>   The maximum length
                                                       of sequences to be
                                                       considered for
                                                       pattern detection.
    --test_generator_parallel                          True if test
                                                       generator should
                                                       run in parallel.
                                                       Requires that test
                                                       generator output is
                                                       disabled.
    --test_generator_disable_output                    True if test
                                                       generator output
                                                       should be hidden.
    --test_generator_timeout <arg>                     The time limit per
                                                       pattern for the
                                                       test generator.
```

</p>
</details>

### Library Type
Schaapi can work with different library formats. This "library type" can be one of the following: `javajar` (a library JAR is provided) or `javamaven` (the Java Maven project of that library is provided).

## Pipeline Stages
### 1 Mining Pipeline
| Stage                                             | Description |
| ------------------------------------------------- | --- |
| **1.1 Mine Projects**                             | Mine version control or library distribution platforms for projects using your software version |
| **1.2 Compile Projects**                          | Compile user projects |
| **1.3 Analyse Usage per Project**                 | Create library usage graphs of user projects |
| **1.4 Find Usage Patterns across all Projects**   | Find common library usage patterns across usage graphs |
| **1.5 Filter Found Patterns**                     | Filter relevant library usage patterns |
| **1.6 Generate Tests**                            | Generate regression tests for library usage patterns |

#### 1.1 Mine Projects
| | |
| ------------------ | ------------- |
| Description        | Mine version control or library distribution platforms for projects using your software version |
| Interface          | `org.cafejojo.schaapi.miningpipeline.ProjectMiner` |
| Implementations    | **[GitHub Project Miner](https://github.com/cafejojo/schaapi/tree/master/modules/mining-pipeline/github-project-miner)**<br>Mines projects from GitHub using its code search API.<br>**[Directory Project Miner](https://github.com/cafejojo/schaapi/tree/master/modules/mining-pipeline/directory-project-miner)**<br>'Mines' projects from the file system, finding all projects in one folder. |

#### 1.2 Compile Projects
| | |
| ------------------ | ------------- |
| Description        | Compile user projects |
| Interface          | `org.cafejojo.schaapi.miningpipeline.ProjectCompiler` |
| Implementations    | **[Java Maven Project Compiler](https://github.com/cafejojo/schaapi/tree/master/modules/mining-pipeline/java-maven-project-compiler)**<br>Compiles Java [Maven](https://www.apache.org/) projects, by fetching their dependencies and compiling the source code.<br>**[Java JAR Project Compiler](https://github.com/cafejojo/schaapi/tree/master/modules/mining-pipeline/java-jar-project-compiler)**<br>Compiles Java JAR projects, by inspecting the classes contained in the JAR. |

#### 1.3 Analyse Usage per Project
| | |
| ------------------ | ------------- |
| Description        | Create library usage graphs of user projects |
| Interface          | `org.cafejojo.schaapi.miningpipeline.LibraryUsageGraphGenerator` |
| Implementations    | **[Jimple Library Usage Graph Generator](https://github.com/cafejojo/schaapi/tree/master/modules/mining-pipeline/jimple-library-usage-graph-generator)**<br>Generates library usage graphs for Java using [Soot](https://github.com/Sable/soot) Jimple control flow graphs. |

#### 1.4 Find Usage Patterns across all Projects
| | |
| ------------------ | ------------- |
| Description        | Find common library usage patterns across usage graphs |
| Interface          | `org.cafejojo.schaapi.miningpipeline.PatternDetector` |
| Implementations    | **[PrefixSpan Pattern Detector](https://github.com/cafejojo/prefix-span-pattern-detector)**<br>Identifies frequent sequential patterns in graphs, using the [PrefixSpan](https://ieeexplore.ieee.org/abstract/document/1339268/) algorithm.<br>**[SPAM Pattern Detector](https://github.com/cafejojo/spam-pattern-detector)**<br>Identifies frequent sequential patterns in graphs, using the [SPAM](https://dl.acm.org/citation.cfm?id=775109) algorithm.<br>**[CCSpan Pattern Detector](https://github.com/cafejojo/schaapi/tree/master/modules/mining-pipeline/ccspan-pattern-detector)**<br>Identifies frequent sequential patterns in graphs, using the [CCSpan](https://dl.acm.org/citation.cfm?id=775109) algorithm. |

#### 1.5 Filter Found Patterns
| | |
| ------------------ | ------------- |
| Description        | Filter relevant library usage patterns |
| Interface          | `org.cafejojo.schaapi.miningpipeline.PatternFilterRule` |
| Implementations    | **[Jimple Pattern Filters](https://github.com/cafejojo/schaapi/tree/master/modules/mining-pipeline/jimple-pattern-filter)**<br>Collection of filters that work with [Soot](https://github.com/Sable/soot) Jimple based library usage graphs. |

#### 1.6 Generate Tests
| | |
| ------------------ | ------------- |
| Description        | Generate regression tests for library usage patterns |
| Interface          | `org.cafejojo.schaapi.miningpipeline.TestGenerator` |
| Implementations    | **[Jimple EvoSuite Test Generator](https://github.com/cafejojo/schaapi/tree/master/modules/mining-pipeline/jimple-evosuite-test-generator)**<br>Generates testable classes based on [Soot](https://github.com/Sable/soot) Jimple based patterns, and generates tests for those using [EvoSuite](http://www.evosuite.org/). |

### 2 Validation Pipeline
| Stage                      | Description |
| -------------------------- | --- |
| **2.1 Execute Tests**      | Run generated tests against a new library version |
| **2.2 Notify Developers**  | Notify library developer of possibly affected users |

#### 2.1 Execute Tests
| | |
| ------------------ | ------------- |
| Description        | Run generated tests against a new library version |
| Interface          | `org.cafejojo.schaapi.validationpipeline.TestRunner` |
| Implementations    | **[JUnit Test Runner](https://github.com/cafejojo/schaapi/tree/master/modules/validation-pipeline/junit-test-runner)**<br>Executes a JUnit test suite and reports on the results |

#### 2.2 Notify Developers
| | |
| ------------------ | ------------- |
| Description        | Notify library developer of possibly affected users |
| Interface          | `--` |
| Implementations    | |

## Changelog
Please see [releases](../../releases) for more information on what has changed recently.

## Testing
``` bash
$ ./gradlew check
```

## Documentation
``` bash
$ ./gradlew dokka
```

## Contributing
Please see [CONTRIBUTING](CONTRIBUTING.md) for details.

## Security
If you discover any security-related issues, please email security@cafejojo.org instead of using the issue tracker.

## Credits
- [Joël Abrahams](https://github.com/JSAbrahams)
- [Georgios Andreadis](https://github.com/gandreadis)
- [Casper Boone](https://github.com/casperboone)
- [F.W. Dekker](https://github.com/FWDekker)
- [All contributors](../../contributors)

## License
The MIT License (MIT). Please see the [license file](LICENSE) for more information.

[travis-status]: https://img.shields.io/travis/cafejojo/schaapi/master.svg?style=for-the-badge&logo=data:image/png;base64,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
[appveyor-status]: https://img.shields.io/appveyor/ci/CafeJojo/schaapi/master.svg?style=for-the-badge&logo=windows
[codecov-status]: https://img.shields.io/codecov/c/github/cafejojo/schaapi/master.svg?style=for-the-badge
[schaap-status]: https://img.shields.io/badge/Contains-%F0%9F%90%91-FF69B4.svg?style=for-the-badge
[baby-dont-hurt-me]: https://img.shields.io/badge/built%20with-%E2%9D%A4%EF%B8%8F-red.svg?style=for-the-badge
