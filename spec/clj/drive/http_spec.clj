(ns drive.http-spec
  (:require [c3kit.apron.log :as log]
            [drive.spec-helperc :as spec-helperc]
            [org.httpkit.server :as server]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.cookies :refer [wrap-cookies]]
            [ring.middleware.head :refer [wrap-head]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [ring.middleware.nested-params :refer [wrap-nested-params]]
            [ring.middleware.not-modified :refer [wrap-not-modified]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.resource :refer [wrap-resource]]
            [speclj.core :refer :all]
            [drive.http :as sut]))

(declare handler)

(describe "HTTP"
  (with-stubs)
  (spec-helperc/capture-logs-around)

  (it "middleware wrapping"
    (with-redefs [wrap-keyword-params   (stub :wrap-keyword-params {:return :wrap-keyword-params-handler})
                  wrap-multipart-params (stub :wrap-multipart-params {:return :wrap-multipart-params-handler})
                  wrap-nested-params    (stub :wrap-nested-params {:return :wrap-nested-params-handler})
                  wrap-params           (stub :wrap-params {:return :wrap-params-handler})
                  wrap-cookies          (stub :wrap-cookies {:return :wrap-cookies-handler})
                  wrap-resource         (stub :wrap-resource {:return :wrap-resource-handler})
                  wrap-content-type     (stub :wrap-content-type {:return :wrap-content-type-handler})
                  wrap-not-modified     (stub :wrap-not-modified {:return :wrap-not-modified-handler})
                  wrap-head             (stub :wrap-head {:return :wrap-head-handler})]
      (should= :wrap-head-handler (sut/wrap-routes :routes-handler))
      (should-have-invoked :wrap-keyword-params {:with [:routes-handler]})
      (should-have-invoked :wrap-multipart-params {:with [:wrap-keyword-params-handler ]})
      (should-have-invoked :wrap-nested-params {:with [:wrap-multipart-params-handler]})
      (should-have-invoked :wrap-params {:with [:wrap-nested-params-handler]})
      (should-have-invoked :wrap-cookies {:with [:wrap-params-handler]})
      (should-have-invoked :wrap-resource {:with [:wrap-cookies-handler "public"]})
      (should-have-invoked :wrap-content-type {:with [:wrap-resource-handler]})
      (should-have-invoked :wrap-not-modified {:with [:wrap-content-type-handler]})
      (should-have-invoked :wrap-head {:with [:wrap-not-modified-handler]})))

  (context "service"

    (redefs-around [server/run-server (stub :server/run-server {:return :stop-fn})])

    (it "configuration"
      (should= #'sut/start (requiring-resolve (:start sut/service)))
      (should= #'sut/stop (requiring-resolve (:stop sut/service))))

    (it "starts the server on port 8080"
      (let [app (sut/start {:foo :bar})]
        (should= {:http/stop :stop-fn :foo :bar} app)
        (should-have-invoked :server/run-server {:with [sut/root-handler {:port 8080}]})
        (should= "Starting HTTP server: http://localhost:8080" (log/captured-logs-str))))

    (it "stops the server with a stop function"
      (let [app (sut/stop {:foo :bar :http/stop (stub :http/stop)})]
        (should= {:foo :bar} app)
        (should-have-invoked :http/stop {:with [:timeout 1000]})
        (should= "Stopping HTTP server" (log/captured-logs-str))))

    (it "stops the server without a stop function"
      (let [app (sut/stop {:foo :bar :http/stop nil})]
        (should= {:foo :bar} app)
        (should= "Stopping HTTP server" (log/captured-logs-str))))
    )

  )