(ns drive.main-spec
  (:require [c3kit.apron.app :as app]
            [c3kit.apron.log :as log]
            [drive.config :as config]
            [drive.http :as http]
            [drive.main :as sut]
            [drive.spec-helperc :as spec-helperc]
            [speclj.core :refer :all]))

(describe "Main"
  (with-stubs)
  (spec-helperc/capture-logs-around)
  (redefs-around [shutdown-agents       :shutdown-agents
                  sut/add-shutdown-hook (stub :add-shutdown-hook)
                  app/start-env         (stub :app/start-env)
                  app/start!            (stub :app/start!)
                  app/stop!             (stub :app/stop!)])

  (it "reports starting"
    (with-redefs [config/environment "the-environment"]
      (sut/-main)
      (should-contain "----- STARTING CloDrive -----" (log/captured-logs-str))
      (should-contain "environment: the-environment" (log/captured-logs-str))))

  (it "adds shutdown hooks for services and agents"
    (sut/-main)
    (should-have-invoked :add-shutdown-hook {:with [:shutdown-agents]})
    (should-have-invoked :add-shutdown-hook {:with [sut/stop-all]}))

  (it "stops all services"
    (let [services [sut/env http/service]]
      (sut/stop-all)
      (should-have-invoked :app/stop! {:with [services]})))

  (it "starts all services"
    (let [services [sut/env http/service]]
      (sut/-main)
      (should-have-invoked :app/start! {:with [services]})))

  (context "env"

    (it "configuration"
      (should= 'drive.main/start-env (:start sut/env))
      (should= 'c3kit.apron.app/stop-env (:stop sut/env)))

    (it "starts env"
      (sut/start-env :the-app)
      (should-have-invoked :app/start-env {:with [:the-app "bwa.env" "BWA_ENV"]}))

    )
  )
