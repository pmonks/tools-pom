| | | |
|---:|:---:|:---:|
| [**main**](https://github.com/pmonks/tools-pom/tree/main) | [![CI](https://github.com/pmonks/tools-pom/workflows/CI/badge.svg?branch=main)](https://github.com/pmonks/tools-pom/actions?query=workflow%3ACI+branch%3Amain) | [![Dependencies](https://github.com/pmonks/tools-pom/workflows/dependencies/badge.svg?branch=main)](https://github.com/pmonks/tools-pom/actions?query=workflow%3Adependencies+branch%3Amain) |
| [**dev**](https://github.com/pmonks/tools-pom/tree/dev) | [![CI](https://github.com/pmonks/tools-pom/workflows/CI/badge.svg?branch=dev)](https://github.com/pmonks/tools-pom/actions?query=workflow%3ACI+branch%3Adev) | [![Dependencies](https://github.com/pmonks/tools-pom/workflows/dependencies/badge.svg?branch=dev)](https://github.com/pmonks/tools-pom/actions?query=workflow%3Adependencies+branch%3Adev) |

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

This project uses the [git-flow branching strategy](https://nvie.com/posts/a-successful-git-branching-model/), with the caveat that the permanent branches are called `main` and `dev`, and any changes to the `main` branch are considered a release and auto-deployed (JARs to Clojars, API docs to GitHub Pages, etc.).

For this reason, **all development must occur either in branch `dev`, or (preferably) in temporary branches off of `dev`.**  All PRs from forked repos must also be submitted against `dev`; the `main` branch is **only** updated from `dev` via PRs created by the core development team.  All other changes submitted to `main` will be rejected.

### Build Tasks

`tools-pom` uses [`tools.build`](https://clojure.org/guides/tools_build). You can get a list of available tasks by running:

```
clojure -A:deps -T:build help/doc
```

Of particular interest are:

* `clojure -T:build test` - run the unit tests
* `clojure -T:build lint` - run the linters (clj-kondo and eastwood)
* `clojure -T:build ci` - run the full CI suite (check for outdated dependencies, run the unit tests, run the linters)
* `clojure -T:build install` - build the JAR and install it locally (e.g. so you can test it with downstream code)

Please note that the `deploy` task is restricted to the core development team (and will not function if you run it yourself).

## License

Copyright © 2021 Peter Monks

Distributed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

SPDX-License-Identifier: [Apache-2.0](https://spdx.org/licenses/Apache-2.0)
