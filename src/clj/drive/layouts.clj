(ns drive.layouts
  (:require [c3kit.apron.utilc :as utilc]
            [c3kit.wire.api :as api]
            [drive.config :as config]
            [c3kit.wire.jwt :as jwt]
            [ring.util.response :as response]
            [c3kit.wire.assets :as assets]
            [hiccup.page :as page]))

(defn main-script [payload]
  (let [transit (pr-str (utilc/->transit payload))]
    (str "<script type=\"text/javascript\">\n//<![CDATA[\n"
         "drive.main.main(" transit ");"
         "\n//]]>\n</script>")))

(defn root-hiccup [payload]
  (list
    [:head
     [:title "CloDrive"]
     [:meta {:charset "utf-8"}]
     [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0, minimum-scale=1.0"}]
     (if config/development?
       (list
         (page/include-js "/cljs/goog/base.js")
         (page/include-js "/cljs/drive_dev.js"))
       (page/include-js (assets/add-fingerprint "/cljs/drive.js")))
     (page/include-css (assets/add-fingerprint "/css/drive.css"))]
    [:body
     [:div#app-root]
     (main-script payload)]))

(defn payload->html-content [payload]
  (page/html5 (root-hiccup payload)))

(defn doc-root [payload]
  (-> (payload->html-content payload)
      response/response
      (response/content-type "text/html")
      (response/charset "UTF-8")))

(defn build-client-payload [request]
  {:config {
            :anti-forgery-token (jwt/client-id request)
            :ws-csrf-token      (jwt/client-id request)
            :api-version        (api/version)
            :host               config/host
            }})

(defn web-client [request]
  (-> request build-client-payload doc-root))
