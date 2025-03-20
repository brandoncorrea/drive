(ns drive.routes
  (:require [c3kit.apron.util :as util]
            [c3kit.wire.ajax :as ajax]
            [clojure.string :as str]
            [compojure.core :as compojure :refer [defroutes routes]]))

(defn wrap-prefix [handler prefix not-found-handler]
  (let [prefix-handler (some-fn handler not-found-handler)]
    (fn [{:keys [path-info uri] :as request}]
      (let [path (or path-info uri)]
        (when (str/starts-with? path prefix)
          (-> (assoc request :path-info (subs path (count prefix)))
              prefix-handler))))))

(def resolve-handler (memoize util/resolve-var))

(defn lazy-handle [handler-sym request]
  (let [handler (resolve-handler handler-sym)]
    (handler request)))

(defmacro lazy-routes [table]
  `(routes
     ~@(for [[[path method] handler-sym] table]
         (compojure/compile-route method path 'req `((lazy-handle '~handler-sym ~'req))))))

(def ajax-routes
  (-> (lazy-routes
        {

         })
      (wrap-prefix "/ajax" ajax/api-not-found-handler)))

(def web-routes
  (lazy-routes
    {
     ["/" :get] drive.layouts/web-client
     }))

(defroutes handler
  web-routes
  ajax-routes
  )