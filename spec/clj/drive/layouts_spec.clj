(ns drive.layouts-spec
  (:require [c3kit.wire.api :as api]
            [drive.config :as config]
            [speclj.core :refer :all]
            [drive.layouts :as sut]))

(describe "Layouts"
  (with-stubs)

  (it "responds with HTML content"
    (let [response (sut/web-client {})]
      (should= 200 (:status response))
      (should= "text/html; charset=UTF-8" (get-in response [:headers "Content-Type"]))))

  (it "includes CSS"
    (let [response (sut/web-client {})]
      (should-contain "/css/drive.css" (:body response))))

  (it "includes dev scripts when in development"
    (with-redefs [config/development? true]
      (let [response (sut/web-client {})]
        (should-contain "/cljs/goog/base.js" (:body response))
        (should-contain "/cljs/drive_dev.js" (:body response))
        (should-not-contain "/cljs/drive.js" (:body response)))))

  (it "excludes dev scripts when in non-dev environments"
    (with-redefs [config/development? false]
      (let [response (sut/web-client {})]
        (should-not-contain "/cljs/goog/base.js" (:body response))
        (should-not-contain "/cljs/drive_dev.js" (:body response))
        (should-contain "/cljs/drive.js" (:body response)))))

  (it "includes main CDATA script"
    (let [response (sut/web-client {})]
      (should-contain "<script type=\"text/javascript\">\n//<![CDATA[\ndrive.main.main(" (:body response))
      (should-contain ");\n//]]>\n</script>" (:body response))))

  (it "passes config down to client"
    (with-redefs [sut/doc-root (stub :sut/doc-root)
                  api/version  (constantly "version-1")
                  config/host  "the-host"]
      (let [payload {:config {:anti-forgery-token "the-client-id"
                              :ws-csrf-token      "the-client-id"
                              :api-version        "version-1"
                              :host               "the-host"}}]

        (sut/web-client {:jwt/payload {:client-id "the-client-id"}})
        (should-have-invoked :sut/doc-root {:with [payload]}))))
  )