{:uploads-dir "/tmp/ae-uploads"
 :nlp {:max-sentence-length 1000
       :timeout 5000}
 :datomic {:uri "datomic:dev://localhost:4334/ae"
           :memcache-uri "localhost:11211"}
 :elastic-search {:uri "http://localhost:9200"
                  :index "documents"}
 :raven {:dsn "<sentry dsn>"
         :logger "raven-clj/analytics-engine"}
 :auth {:server-url "http://localhost:3000/"}
 :metrics {:source "drone"}}

