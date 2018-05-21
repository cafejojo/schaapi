# 🐑 schaapi
[![Travis build status](https://img.shields.io/travis/cafejojo/schaapi/master.svg?style=for-the-badge&logo=travis)](https://travis-ci.org/cafejojo/schaapi)
[![AppVeyor build status](https://img.shields.io/appveyor/ci/CafeJojo/schaapi/master.svg?style=for-the-badge&logo=appveyor)](https://ci.appveyor.com/project/CafeJojo/schaapi/branch/master)
[![Codecov](https://img.shields.io/codecov/c/github/cafejojo/schaapi/master.svg?style=for-the-badge)](https://codecov.io/gh/cafejojo/schaapi/)
[![Schaap](https://img.shields.io/badge/Contains-%F0%9F%90%91-FF69B4.svg?style=for-the-badge)](https://github.com/cafejojo/schaapi)
[![Built with love](https://img.shields.io/badge/built%20with-%E2%9D%A4%EF%B8%8F-red.svg?style=for-the-badge)](https://github.com/cafejojo/)

Schaapi ensures Safe Changes for APIs of libraries. It specifically focuses on semantic breaking changes, but provides a general purpose pipeline that is also applicable to detection of other types of breaking changes. The default implementation detects incompatibilities between Java libraries and Java projects using that library.

## Requirements and Installation
Schaapi requires JRE 8 and has been tested on Windows and Unix systems.

## Usage
Execute `./gradlew :application:run` with the following command line options:

```
usage: schaapi -o <arg> -l <arg> -u <arg> [--maven_dir <arg>]
       [--repair_maven] [--pattern_detector_minimum_count <arg>]
       [--test_generator_enable_output] [--test_generator_timeout <arg>]
 -o,--output_dir <arg>                       The output directory.
 -l,--library_dir <arg>                      The library directory.
 -u,--user_dirs <arg>                        The user directories,
                                             separated by semi-colons.
    --maven_dir <arg>                        The directory to run Maven
                                             from.
    --repair_maven                           Repairs the Maven
                                             installation.
    --pattern_detector_minimum_count <arg>   The minimum number of
                                             occurrences for a statement
                                             to be considered frequent.
    --test_generator_enable_output           True if test generator output
                                             should be shown.
    --test_generator_timeout <arg>           The time limit for the test
                                             generator.
```

## Pipeline Stages
### 1 Periodic Project Mining
| Stage                                         | Description |
| --------------------------------------------- | --- |
| **1.1 Project Mining**                            | Mine version control or library distribution platforms for projects using your software version |
| **1.2 Analyse Usage per Project**                 | Create library usage graphs of user projects |
| **1.3 Find Usage Patterns across all Projects**   | Find common library usage patterns across usage graphs |
| **1.4 Filter Found Patterns**                     | Filter relevant library usage patterns |
| **1.5 Generate Tests**                            | Generate regressions tests for library usage patterns |

#### 1.1 Project Mining
| | |
| ------------------ | ------------- |
| Description        | Mine version control or library distribution platforms for projects using your software version |
| Interface          | `org.cafejojo.schaapi.pipeline.ProjectMiner` |
| Implementations    | |

#### 1.2 Analyse Usage per Project
| | |
| ------------------ | ------------- |
| Description        | Create library usage graphs of user projects |
| Interface          | `org.cafejojo.schaapi.pipeline.LibraryUsageGraphGenerator` |
| Implementations    | **[Jimple Library Usage Graph Generator](https://github.com/cafejojo/schaapi/tree/master/modules/pipeline/jimple-library-usage-graph-generator)**<br>Generates library usage graphs for Java using [Soot](https://github.com/Sable/soot) Jimple control flow graphs. |

#### 1.3 Find Usage Patterns across all Projects
| | |
| ------------------ | ------------- |
| Description        | Find common library usage patterns across usage graphs |
| Interface          | `org.cafejojo.schaapi.pipeline.PatternDetector` |
| Implementations    | **[PrefixSpan Pattern Detector](https://github.com/cafejojo/schaapi/tree/master/modules/pipeline/prefix-span-pattern-detector)**<br>Identifies frequent sequential patterns in graphs, with the [PrefixSpan](https://ieeexplore.ieee.org/abstract/document/1339268/) algorithm. |

#### 1.4 Filter Found Patterns
| | |
| ------------------ | ------------- |
| Description        | Filter relevant library usage patterns |
| Interface          | `org.cafejojo.schaapi.pipeline.PatternFilterRule` |
| Implementations    | **[Jimple Pattern Filters](https://github.com/cafejojo/schaapi/tree/master/modules/pipeline/jimple-pattern-filter)**<br>Collection of filters that work with [Soot](https://github.com/Sable/soot) Jimple based library usage graphs. |

#### 1.5 Generate Tests
| | |
| ------------------ | ------------- |
| Description        | Generate regressions tests for library usage patterns |
| Interface          | `org.cafejojo.schaapi.pipeline.TestGenerator` |
| Implementations    | **[Jimple EvoSuite Test Generator](https://github.com/cafejojo/schaapi/tree/master/modules/pipeline/jimple-evosuite-test-generator)**<br>Generates testable classes based on [Soot](https://github.com/Sable/soot) Jimple based patterns, and generates tests for those using [EvoSuite](http://www.evosuite.org/). |

### 2 Continuous Integration
| Stage                      | Description |
| -------------------------- | --- |
| **2.1 Execute Tests**      | Run generated tests against new library versions |
| **2.2 Notify Developers**  | Notify library developer of possibly affected users |
| **2.3 Notify Users**       | Notify library users of breaking changes |

#### 2.1 Execute Tests
| | |
| ------------------ | ------------- |
| Description        | Run generated tests against new library versions |
| Interface          | `--` |
| Implementations    | |

#### 2.2 Notify Developers
| | |
| ------------------ | ------------- |
| Description        | Notify library developer of possibly affected users |
| Interface          | `--` |
| Implementations    | |

#### 2.3 Notify Users
| | |
| ------------------ | ------------- |
| Description        | Notify library users of breaking changes |
| Interface          | `--` |
| Implementations    | |

## Changelog
Please see [releases](../../releases) for more information on what has changed recently.

## Testing
``` bash
$ ./gradlew check
```

## Contributing
Please see [CONTRIBUTING](CONTRIBUTING.md) for details.

## Security
If you discover any security related issues, please email cafejojo@casperboone.nl instead of using the issue tracker.

## Credits
- [Joël Abrahams](https://github.com/jsabrahams)
- [Georgios Andreadis](https://github.com/gandreadis)
- [Casper Boone](https://github.com/casperboone)
- [Felix Dekker](https://github.com/fwdekker)
- [All Contributors](../../contributors)

## License
The MIT License (MIT). Please see the [license file](LICENSE) for more information.
