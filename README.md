# cljs-dashboard-widgets
A library of clojurescript widgets and utility code, intended for use in building developer / metrics dashboards.

To run:

* start a Clojure REPL
* run `(go)`
* run `(start-figwheel)`
* open a browser to localhost:8080

```
$ lein repl

...
Commands:

=> (metrics-help)   ;; prints this message
=> (go)             ;; Starts webserver
=> (reset)          ;; Reloads all clj code and restarts webserver
=> (start-figwheel) ;; starts figwheel, to dynamically
                    ;;  recompile cljs code and send it to
                    ;;  the browser
=> (browser-repl)   ;; starts cljs repl, may need to reload
                    ;;  browser to attach
user=> (go)
...
user=> (start-figwheel)
```
