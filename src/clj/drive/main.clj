(ns drive.main
  (:require [c3kit.apron.app :as app]
            [c3kit.apron.log :as log]
            [drive.http :as http]
            [drive.config :as config]))

(defn start-env [app] (app/start-env app "bwa.env" "BWA_ENV"))

(def env (app/service 'drive.main/start-env 'c3kit.apron.app/stop-env))

(def services [env http/service])

(defn stop-all [] (app/stop! services))
(defn start-all [] (app/start! services))

(defn add-shutdown-hook [handler]
  (.addShutdownHook (Runtime/getRuntime) (Thread. handler)))

(defn -main []
  (log/report "----- STARTING CloDrive -----")
  (log/report "environment:" config/environment)
  (run! add-shutdown-hook [stop-all shutdown-agents])
  (start-all))