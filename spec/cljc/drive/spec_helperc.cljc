(ns drive.spec-helperc
  (:require [speclj.core #?(:clj :refer :cljs :refer-macros) [around it]]
            [c3kit.apron.log :as log]))

(defn capture-logs-around []
  (around [it] (log/capture-logs (it))))
