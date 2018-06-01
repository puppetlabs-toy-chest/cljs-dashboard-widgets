(defproject puppetlabs/cljs-dashboard-widgets "0.1.1-SNAPSHOT"
  :description "A clojurescript library with widgets useful for metrics dashboards.  Plays nicely with trapperkeeper-status and trapperkeeper-metrics."
  :url "https://github.com/puppetlabs/cljs-dashboard-widgets"
  :license {:name "Apache License, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}

  :source-paths ["src/cljs"]

  :dependencies [[org.clojure/clojure "1.7.0"]

                 ;; transitive dependencies
                 [org.clojure/clojurescript "1.7.228"]
                 ;; end transitive dependencies

                 [cljsjs/d3 "3.5.7-1"]
                 [cljsjs/react "0.13.3-1"]
                 [reagent "0.5.1"]
                 [com.andrewmcveigh/cljs-time "0.4.0"]
                 [org.clojure/core.async "0.2.374"]]

  :pedantic? :abort

  :lein-release {:scm         :git
                 :deploy-via  :lein-deploy}

  :repositories [["releases" "https://artifactory.delivery.puppetlabs.net/artifactory/clojure-releases__local/"]
                 ["snapshots" "https://artifactory.delivery.puppetlabs.net/artifactory/clojure-snapshots__local/"]]

  :deploy-repositories [["releases" {:url "https://clojars.org/repo"
                                     :username :env/clojars_jenkins_username
                                     :password :env/clojars_jenkins_password
                                     :sign-releases false}]]

  :plugins [[lein-cljsbuild "1.1.2" :exclusions [org.clojure/clojure]]]

  :min-lein-version "2.5.0"

  :cljsbuild {:builds {:app {:source-paths ["src/cljs"]
                             :compiler {:output-to     "target/js-resources/puppetlabs/metrics/dashboard/public/js/app.js"
                                        :output-dir    "target/js-resources/puppetlabs/metrics/dashboard/public/js/out"
                                        :asset-path   "js/out"
                                        :optimizations :none
                                        :pretty-print  true}}}}

  :profiles {:dev     {:source-paths ["dev/clj"]
                       :resource-paths ["target/js-resources"]
                       :repl-options {:init-ns          user}

                       :dependencies [
                                      ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
                                      ;; transitive dependencies
                                      [org.clojure/tools.macro "0.1.5"]
                                      [org.clojure/tools.cli "0.3.1"]
                                      [prismatic/schema "0.4.3"]
                                      [prismatic/plumbing "0.4.3"]
                                      [clj-time "0.7.0"]

                                      [org.codehaus.plexus/plexus-utils "3.0.15"]
                                      [org.apache.maven.wagon/wagon-provider-api "2.7"]
                                      [org.apache.httpcomponents/httpclient "4.3.5"]
                                      [commons-codec "1.10"]

                                      [puppetlabs/kitchensink "1.2.0"]
                                      ;; end transitive dependencies
                                      ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

                                      [puppetlabs/trapperkeeper "1.1.0"]
                                      [puppetlabs/trapperkeeper-webserver-jetty9 "1.3.0"]
                                      [puppetlabs/comidi "0.2.1"]

                                      [org.clojure/tools.namespace "0.2.10"]
                                      [leiningen "2.5.1"]
                                      [cljs-http "0.1.39"]]

                       :plugins      [[lein-figwheel "0.5.0-6" :exclusions [org.clojure/clojurescript
                                                                            org.codehaus.plexus/plexus-utils]]]

                       :figwheel     {:http-server-root "puppetlabs/metrics/dashboard/public"
                                      :server-port      3449
                                      :repl             false}

                       :cljsbuild    {:builds {:app {:source-paths ["dev/cljs"]
                                                     :compiler     {:main       "puppetlabs.metrics.dashboard.demo.dev"
                                                                    :source-map true}}
                                               }}}

             ;; This is just here for reference, it really wouldn't make any sense
             ;; to build an uberjar for this project.
             :uberjar {:cljsbuild {:jar    true
                                   :builds {:app
                                            {:compiler
                                                           {:main "puppetlabs.metrics.dashboard.demo.prod"}}}}}})
