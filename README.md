| | | |
|---:|:---:|:---:|
| [**main**](https://github.com/pmonks/tools-pom/tree/main) | [![Lint](https://github.com/pmonks/tools-pom/workflows/lint/badge.svg?branch=main)](https://github.com/pmonks/tools-pom/actions?query=workflow%3Alint) | [![Dependencies](https://github.com/pmonks/tools-pom/workflows/dependencies/badge.svg?branch=main)](https://github.com/pmonks/tools-pom/actions?query=workflow%3Adependencies) |
| [**dev**](https://github.com/pmonks/tools-pom/tree/dev)  | [![Lint](https://github.com/pmonks/tools-pom/workflows/lint/badge.svg?branch=dev)](https://github.com/pmonks/tools-pom/actions?query=workflow%3Alint) | [![Dependencies](https://github.com/pmonks/tools-pom/workflows/dependencies/badge.svg?branch=dev)](https://github.com/pmonks/tools-pom/actions?query=workflow%3Adependencies) |

[![Latest Version](https://img.shields.io/clojars/v/com.github.pmonks/tools-pom)](https://clojars.org/com.github.pmonks/tools-pom/) [![Open Issues](https://img.shields.io/github/issues/pmonks/tools-pom.svg)](https://github.com/pmonks/tools-pom/issues) [![License](https://img.shields.io/github/license/pmonks/tools-pom.svg)](https://github.com/pmonks/tools-pom/blob/main/LICENSE)


# tools-pom

A Clojure [tools.build](https://github.com/clojure/tools.build) task library related to the generation of comprehensive pom.xml files (beyond the limited set of POM elements tools.build/tools.deps generates).

## Tasks

1. `pom` - generate a comprehensive `pom.xml` file from EDN (which can come from anywhere - stored in your `build.clj`, `deps.edn` or a separate file, or synthesised on the fly in your build tool script).

Note that the `pom` task is entirely data-driven, so if your input data includes elements that are not valid in a [Maven POM](https://maven.apache.org/guides/introduction/introduction-to-the-pom.html), the resulting file will be invalid.  You can check your input data by enabling the `:validate-pom` flag in the options that get passed to the task.

## Using the library

### Dependency

Express a maven dependency in your `deps.edn`, for a build tool alias:

```edn
  :aliases
    :build
      {:deps       {com.github.pmonks/tools-pom {:mvn/version "LATEST_CLOJARS_VERSION"}}
       :ns-default your.build.ns}
```

### Require the namespace

```clojure
(ns your.build.ns
  (:require [tools-pom.tasks :as pom]))
```

### Add comprehensive `pom` information and a `pom` build task to your build

```clojure
(def lib       'com.github.yourusername/yourproject)
(def version   (format "1.0.%s" (b/git-count-revs nil)))

(defn- set-opts
  [opts]
  (assoc opts
         :lib          lib
         :version      version
         :write-pom    true
         :validate-pom true
         ; Note: this EDN can come from anywhere - you could externalise it into a separate edn file (e.g. pom.edn), synthesise it from information elsewhere in your project, or whatever other scheme you like
         :pom          {:description      "Description of your project e.g. your project's GitHub \"short description\"."
                        :url              "https://github.com/yourusername/yourproject"
                        :licenses         [:license   {:name "Apache License 2.0" :url "http://www.apache.org/licenses/LICENSE-2.0.html"}] ; Note first element is a tag
                        :developers       [:developer {:id "yourusername" :name "yourname" :email "youremail@emailservice.com"}]           ; And here
                        :scm              {:url                  "https://github.com/yourusername/yourproject"
                                           :connection           "scm:git:git://github.com/yourusername/yourproject.git"
                                           :developer-connection "scm:git:ssh://git@github.com/yourusername/yourproject.git"}
                        :issue-management {:system "github" :url "https://github.com/yourusername/yourproject/issues"}}))

(defn pom
  "Construct a comprehensive pom.xml file for this project"
  [opts]
  (-> opts
      (set-opts)
      (pom/pom)))
```

### API Documentation

[API documentation is available here](https://pmonks.github.io/tools-pom/).

## Contributor Information

[Contributing Guidelines](https://github.com/pmonks/tools-pom/blob/main/.github/CONTRIBUTING.md)

[Bug Tracker](https://github.com/pmonks/tools-pom/issues)

[Code of Conduct](https://github.com/pmonks/tools-pom/blob/main/.github/CODE_OF_CONDUCT.md)

### Developer Workflow

The repository has two permanent branches: `main` and `dev`.  **All development must occur either in branch `dev`, or (preferably) in feature branches off of `dev`.**  All PRs must also be submitted against `dev`; the `main` branch is **only** updated from `dev` via PRs created by the core development team.  All other changes submitted to `main` will be rejected.

This model allows otherwise unrelated changes to be batched up in the `dev` branch, integration tested there, and then released en masse to the `main` branch, which will trigger automated generation and deployment of the release (Codox docs to github.io, JARs to Clojars, etc.).

## License

Copyright © 2021 Peter Monks

Distributed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

SPDX-License-Identifier: [Apache-2.0](https://spdx.org/licenses/Apache-2.0)
