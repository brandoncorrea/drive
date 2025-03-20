(ns drive.routes-spec
  (:require [c3kit.wire.ajax :as ajax]
            [drive.routes :as sut]
            [speclj.core :refer :all]))

(declare handler)

(defmacro check-route [path method handler]
  (require `~(symbol (namespace handler)))
  `(let [stub-key# ~(keyword handler)]
     (with-redefs [~handler (stub stub-key#)]
       (sut/handler {:uri ~path :request-method ~method})
       (should-have-invoked stub-key#))))

(defmacro test-route [path method handler]
  `(it ~path
     (check-route ~path ~method ~handler)))

(describe "Routes"
  (with-stubs)

  (test-route "/" :get drive.layouts/web-client)

  (it "ajax not found"
    (let [response (sut/handler {:uri "/ajax/foo" :request-method :get})]
      (should= 200 (:status response))
      (should= :fail (ajax/status response))
      (should= "API not found: /ajax/foo" (ajax/first-flash-text response))))

  (context "wrap-prefix"

    (with handler (sut/wrap-prefix (stub :handler {:return :response}) "/pre" (constantly :not-found)))

    (it "no match"
      (should-be-nil (@handler {:path-info "/blah"})))

    (it "matches on path-info"
      (let [request  {:path-info "/pre/foo"}
            response (@handler request)]
        (should= :response response)
        (should-have-invoked :handler {:with [{:path-info "/foo"}]})))

    (it "matches on uri"
      (let [request  {:uri "/pre/foo"}
            response (@handler request)]
        (should= :response response)
        (should-have-invoked :handler {:with [{:path-info "/foo" :uri "/pre/foo"}]})))

    (it "path-info takes precedence over uri"
      (let [request  {:path-info "/pre/foo" :uri "/pre/foo/bar"}
            response (@handler request)]
        (should= :response response)
        (should-have-invoked :handler {:with [{:path-info "/foo" :uri "/pre/foo/bar"}]})))

    )

  )
