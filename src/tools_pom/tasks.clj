;
; Copyright © 2021 Peter Monks
;
; Licensed under the Apache License, Version 2.0 (the "License");
; you may not use this file except in compliance with the License.
; You may obtain a copy of the License at
;
;     http://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.
;
; SPDX-License-Identifier: Apache-2.0
;

(ns tools-pom.tasks
  "Clojure tools.deps tasks related to comprehensive pom.xml files.

  All of the build tasks return the opts hash map they were passed
  (unlike some of the functions in clojure.tools.build.api)."
  (:require [clojure.java.io         :as io]
            [clojure.data.xml        :as xml]
            [tools-convenience.api   :as tc]
            [camel-snake-kebab.core  :as csk]
            [clj-xml-validation.core :as xmlv]))

; As of v1.10 this should be in core...
(defmethod print-method java.time.Instant [^java.time.Instant inst writer]
  (print-method (java.util.Date/from inst) writer))

(defn- pom-keyword
  "Converts a regular Clojure keyword into a POM-4.0.0-namespaced keyword."
  [kw]
  (keyword "xmlns.http%3A%2F%2Fmaven.apache.org%2FPOM%2F4.0.0" (csk/->camelCase (name kw))))

(defn- build-pom
  "Converts the given 'element' into a POM structure in clojure.data.xml compatible EDN format."
  [elem]
  (cond
    (map-entry?  elem)             [(pom-keyword (key elem)) (build-pom (val elem))]
    (map?        elem)             (map build-pom elem)
    (sequential? elem)             (mapcat #(hash-map (pom-keyword (first elem)) (build-pom %)) (rest elem))
    (= java.util.Date (type elem)) (str (.toInstant ^java.util.Date elem))
    :else                          (str elem)))

(defn pom
  "Writes out a pom file. opts includes:

  :lib          -- opt: a symbol identifying your project e.g. 'com.github.yourusername/yourproject
  :version      -- opt: a string containing the version of your project e.g. \"1.0.0-SNAPSHOT\"
  :pom-file     -- opt: the name of the file to write to (defaults to \"./pom.xml\")
  :write-pom    -- opt: a flag indicating whether to invoke \"clojure -Spom\" after generating the basic pom (i.e. adding dependencies and repositories from your deps.edn file) (defaults to false)
  :validate-pom -- opt: a flag indicating whether to validate the generated pom.xml file after it's been constructed (defaults to false)
  :pom          -- opt: a map containing any other POM elements (see https://maven.apache.org/pom.html for details), using standard Clojure :keyword keys

  Here is an example :pom map that will generate a valid pom.xml:

  :pom {:description      \"Description of your project e.g. your project's short description on GitHub.\"
        :url              \"https://github.com/yourusername/yourproject\"
        :licenses         [:license   {:name \"Apache License 2.0\" :url \"http://www.apache.org/licenses/LICENSE-2.0.html\"}] ; Note first element is a tag
        :developers       [:developer {:id \"yourusername\" :name \"yourname\" :email \"youremail@emailservice.com\"}]         ; And here
        :scm              {:url                  \"https://github.com/yourusername/yourproject\"
                           :connection           \"scm:git:git://github.com/yourusername/yourproject.git\"
                           :developer-connection \"scm:git:ssh://git@github.com/yourusername/yourproject.git\"}
        :issue-management {:system \"github\" :url \"https://github.com/yourusername/yourproject/issues\"}}))"
  [opts]
  (tc/ensure-command "clojure")
  (println "Generating comprehensive pom.xml...")
  (let [pom-file (get opts :pom-file "./pom.xml")
        pom-in   (merge (when (:lib     opts) {:group-id (namespace (:lib opts)) :artifact-id (name (:lib opts)) :name (name (:lib opts))})
                        (when (:version opts) {:version (:version opts)})
                        (:pom opts))   ; Merge user-supplied values last, so that they always take precedence
        pom-out  [(pom-keyword :project) {:xmlns                         "http://maven.apache.org/POM/4.0.0"
                                          (keyword "xmlns:xsi")          "http://www.w3.org/2001/XMLSchema-instance"
                                          (keyword "xsi:schemaLocation") "http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd"}
                   (concat [[(pom-keyword :model-version) "4.0.0"]]
                            (build-pom pom-in))]
        pom-xml  (xml/sexp-as-element pom-out)]
    (with-open [pom-writer (io/writer pom-file)]
      (xml/emit pom-xml pom-writer :encoding "UTF8"))
    (when (:write-pom opts)
      (tc/exec "clojure -Srepro -Spom"))   ; tools.build/write-pom is nowhere as useful as clojure -Spom but the latter doesn't have an API so we just fork it instead..
    (when (:validate-pom opts)
      (let [is-pom?           (xmlv/create-validation-fn (io/reader "https://maven.apache.org/xsd/maven-4.0.0.xsd"))
            validation-result (is-pom? (slurp pom-file))]
        (when-not (xmlv/valid? validation-result)
          (println "Generated pom.xml is not valid; check your input data. Errors:")
          (doall (map #(println "  Line" (get % :line-number "?") "column" (str (get % :column-number "?") ":") (:message %))  (xmlv/errors validation-result)))))))
  opts)
