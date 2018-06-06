(defproject puppetlabs/cljs-dashboard-widgets "0.1.1-SNAPSHOT"
  :description "A clojurescript library with widgets useful for metrics dashboards.  Plays nicely with trapperkeeper-status and trapperkeeper-metrics."
  :url "https://github.com/puppetlabs/cljs-dashboard-widgets"
  :license {:name "Apache License, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}

  :source-paths ["src/cljs"]

  :parent-project {:coords [puppetlabs/clj-parent "2.0.1"]
                   :inherit [:managed-dependencies]}

  :dependencies [[org.clojure/clojure "1.9.0"]

                 ;; transitive dependencies
                 [org.clojure/clojurescript "1.10.238"]
                 ;; end transitive dependencies
                 [com.fasterxml.jackson.core/jackson-core]
                 [cljsjs/d3 "4.12.0-0"]
                 [cljsjs/react "0.13.3-1"]
                 [reagent "0.5.1"]
                 [com.andrewmcveigh/cljs-time "0.4.0"]
                 [org.clojure/core.async]]

  :pedantic? :abort

  :lein-release {:scm         :git
                 :deploy-via  :lein-deploy}

  :repositories [["releases" "https://artifactory.delivery.puppetlabs.net/artifactory/clojure-releases__local/"]
                 ["snapshots" "https://artifactory.delivery.puppetlabs.net/artifactory/clojure-snapshots__local/"]]

  :deploy-repositories [["releases" {:url "https://clojars.org/repo"
                                     :username :env/clojars_jenkins_username
                                     :password :env/clojars_jenkins_password
                                     :sign-releases false}]]

  :plugins [[lein-cljsbuild "1.1.2" :exclusions [org.clojure/clojure]]
            [lein-parent "0.3.1"]]

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
                                      [org.clojure/tools.macro]
                                      [org.clojure/tools.cli]
                                      [prismatic/schema]
                                      [prismatic/plumbing]
                                      [clj-time]

                                      [org.codehaus.plexus/plexus-utils "3.0.15"]
                                      [org.apache.maven.wagon/wagon-provider-api]
                                      [org.apache.httpcomponents/httpclient]
                                      [commons-codec]

                                      [org.clojure/tools.analyzer "0.6.9"]
                                      [org.clojure/tools.analyzer.jvm "0.7.0"]
                                      [puppetlabs/kitchensink]
                                      [com.cemerick/piggieback "0.2.1"]
                                      ;; end transitive dependencies
                                      ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

                                      [puppetlabs/trapperkeeper]
                                      [puppetlabs/trapperkeeper-webserver-jetty9]
                                      [puppetlabs/comidi]
                                      [org.clojure/tools.nrepl "0.2.13"]
                                      [org.clojure/tools.namespace]

                                      [cljs-http "0.1.39"]
                                      [figwheel-sidecar "0.5.4-6" :exclusions [org.clojure/clojure]]]

                       :plugins      [[lein-figwheel "0.5.0-6" :exclusions [commons-codec
                                                                            org.clojure/clojure
                                                                            commons-io]]]

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
                                                           {:main "puppetlabs.metrics.dashboard.demo.prod"}}}}}}
  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]})
