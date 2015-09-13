# cljs-dashboard-widgets
A library of clojurescript widgets and utility code, intended for use in building developer / metrics dashboards.

## Contents

At present the library contains two widgets:

* `metrics-box`: A box that displays a line graph of a series of data points (as seen in the PuppetDB dashboard)
* `sortable-table`: A simple table, built using the `reagent` library.  Take a list of maps representing your table data, assign it to a reagent atom, and bind the table to the atom.  The widget will render the data, with column headers that can be clicked on to re-sort the table.  Modifying the value of the atom will cause the table to be updated automatically.

## Demo

The project includes a sample app that you can run to see the widgets in action, and to see how to wire them up to a data source on the server.

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

## Support

We use the
[Trapperkeeper project on JIRA](https://tickets.puppetlabs.com/browse/TK)
for tickets on cljs-dashboard-widgets, although Github issues are welcome too.
