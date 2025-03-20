(ns drive.config
  (:require [c3kit.apron.app :as app]
            [c3kit.apron.env :as env]
            [c3kit.apron.time :as time]))

(def environment (app/find-env "BWA_ENV"))

(def jwt
  {:cookie-name "bwa-token"
   :secret      (env/env "JWT_SECRET")
   :lifespan    (time/hours (* 24 7))})

(def base
  {:jwt jwt})

(def development
  (merge
    base
    {:host      "http://localhost:8080"
     :log-level :trace}))

(def production
  (merge
    base
    {:host      "https://drive.bwawan.com"
     :log-level :info}))

(def development? (= "development" environment))

(def env
  (if development?
    development
    production))

(def host (:host env))
