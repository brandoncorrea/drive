(ns drive.http
  (:require [c3kit.apron.app :as app]
            [c3kit.apron.corec :as ccc]
            [c3kit.apron.log :as log]
            [drive.routes :as routes]
            [org.httpkit.server :as server]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.cookies :refer [wrap-cookies]]
            [ring.middleware.head :refer [wrap-head]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [ring.middleware.nested-params :refer [wrap-nested-params]]
            [ring.middleware.not-modified :refer [wrap-not-modified]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.resource :refer [wrap-resource]]))

(def PORT 8080)

(defn wrap-routes [routes-handler]
  (-> routes-handler
      wrap-keyword-params
      wrap-multipart-params
      wrap-nested-params
      wrap-params
      wrap-cookies
      (wrap-resource "public")
      wrap-content-type
      wrap-not-modified
      wrap-head))

(defonce root-handler
  (wrap-routes routes/handler))

(defn start [app]
  (log/info (str "Starting HTTP server: http://localhost:" PORT))
  (let [stop-fn (server/run-server root-handler {:port PORT})]
    (assoc app :http/stop stop-fn)))

(defn stop [app]
  (log/info "Stopping HTTP server")
  (some-> (:http/stop app) (ccc/invoke :timeout 1000))
  (dissoc app :http/stop))

(def service (app/service 'drive.http/start 'drive.http/stop))
