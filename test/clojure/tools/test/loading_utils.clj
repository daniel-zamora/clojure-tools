(ns clojure.tools.test.loading-utils
  (:import [java.io ByteArrayInputStream File Reader]
           [java.util.zip ZipEntry])
  (:use clojure.test
        clojure.tools.loading-utils)
  (:require [clojure.java.classpath :as classpath]
            [clojure.tools.file-utils :as file-utils]
            [controllers.home-controller :as home-controller]))
        
(deftest test-system-class-loader
  (let [test-class-loader (system-class-loader)]
    (is (not (nil? test-class-loader)))
    (is (instance? ClassLoader test-class-loader))))

(defn
#^{ :doc "Converts the given string into an input stream. Assumes the character incoding is UTF-8." }
  string-as-input-stream [string]
  (new ByteArrayInputStream (. string getBytes "UTF-8")))

(deftest test-seq-input-stream
  (let [test-input-stream (seq-input-stream (string-as-input-stream "test"))]
    (is (not (nil? test-input-stream)))
    (is (= (count test-input-stream) 4)))
  (let [test-input-stream (seq-input-stream (string-as-input-stream "test") 3)]
    (is (not (nil? test-input-stream)))
    (is (= (count test-input-stream) 3))))

(deftest test-byte-array-input-stream
  (let [test-input-stream (byte-array-input-stream (string-as-input-stream "test"))]
    (is (not (nil? test-input-stream)))
    (is (= (count test-input-stream) 4))
    (is (instance? Byte (first test-input-stream))))
  (let [test-input-stream (byte-array-input-stream (string-as-input-stream "test") 3)]
    (is (not (nil? test-input-stream)))
    (is (= (count test-input-stream) 3))
    (is (instance? Byte (first test-input-stream)))))

(deftest test-string-input-stream
  (is (= (string-input-stream (string-as-input-stream "test")) "test"))
  (is (= (string-input-stream (string-as-input-stream "")) "")))

(deftest test-dashes-to-underscores
  (is (= (dashes-to-underscores "test") "test"))
  (is (= (dashes-to-underscores "test-this") "test_this"))
  (is (= (dashes-to-underscores "test-this-now") "test_this_now"))
  (is (= (dashes-to-underscores "test_this") "test_this"))
  (is (= (dashes-to-underscores "") ""))
  (is (= (dashes-to-underscores nil) nil)))
  
(deftest test-underscores-to-dashes
  (is (= (underscores-to-dashes "test") "test"))
  (is (= (underscores-to-dashes "test_this") "test-this"))
  (is (= (underscores-to-dashes "test_this_now") "test-this-now"))
  (is (= (underscores-to-dashes "test-this") "test-this"))
  (is (= (underscores-to-dashes "") ""))
  (is (= (underscores-to-dashes nil) nil)))
 
 (deftest test-file-separator
  (is (not (nil? (file-separator)))))
 
(deftest test-slashes-to-dots
  (is (= (slashes-to-dots "test") "test"))
  (is (= (slashes-to-dots "test/this") "test.this"))
  (is (= (slashes-to-dots "test\\this") "test.this"))
  (is (= (slashes-to-dots "test/this/now") "test.this.now"))
  (is (= (slashes-to-dots "test\\this\\now") "test.this.now"))
  (is (= (slashes-to-dots "test.this") "test.this"))
  (is (= (slashes-to-dots "") ""))
  (is (= (slashes-to-dots nil) nil)))

(deftest test-dots-to-slashes
  (let [separator (file-separator)]
    (is (= (dots-to-slashes "foo.bar") (str "foo" separator "bar")))
    (is (= (dots-to-slashes "foo.bar.bat") (str "foo" separator "bar" separator "bat"))))
  (is (= (dots-to-slashes "foo") "foo"))
  (is (= (dots-to-slashes "") ""))
  (is (= (dots-to-slashes nil) nil)))
  
(deftest test-clj-file?
  (is (clj-file? (new File (file-utils/user-directory) "test/controllers/home_controller.clj")))
  (is (not (clj-file? (new File "test.txt"))))
  (is (not (clj-file? (new File "test/")))))

(deftest test-clj-file-to-symbol-string
  (is (= (clj-file-to-symbol-string "test.clj") "test"))
  (is (= (clj-file-to-symbol-string "test_this.clj") "test-this"))
  (is (= (clj-file-to-symbol-string "test_this_now.clj") "test-this-now"))
  (is (= (clj-file-to-symbol-string "test-this.clj") "test-this"))
  (is (= (clj-file-to-symbol-string "test_this") "test-this"))
  (is (= (clj-file-to-symbol-string (str "parent" (file-separator) "test_this")) "parent.test-this"))
  (is (= (clj-file-to-symbol-string "") ""))
  (is (= (clj-file-to-symbol-string nil) nil)))
  
(deftest test-symbol-string-to-clj-file
  (is (= (symbol-string-to-clj-file "test") "test.clj"))
  (is (= (symbol-string-to-clj-file "test-this") "test_this.clj"))
  (is (= (symbol-string-to-clj-file "test-this-now") "test_this_now.clj"))
  (is (= (symbol-string-to-clj-file "test_this") "test_this.clj"))
  (is (= (symbol-string-to-clj-file "parent.test-this") (str "parent" (file-separator) "test_this.clj")))
  (is (= (symbol-string-to-clj-file "") ""))
  (is (= (symbol-string-to-clj-file nil) nil)))

(deftest test-file-namespace
  (let [classpath-parent-dir (new File "app")]
    (is (= "views.home.index" 
      (file-namespace classpath-parent-dir (new File classpath-parent-dir "views/home/index.clj"))))
    (is (= "views.test-controller.test-view" 
      (file-namespace classpath-parent-dir (new File classpath-parent-dir "views/test_controller/test_view.clj"))))
    (is (nil? (file-namespace classpath-parent-dir nil)))
    (is (= "app.views.home.index" (file-namespace nil (new File classpath-parent-dir "views/home/index.clj"))))))

(deftest test-namespace-string-for-file
  (is (= (namespace-string-for-file "test/util" "test_loading_utils.clj") "test.util.test-loading-utils"))
  (is (= (namespace-string-for-file nil "test_loading_utils.clj") "test-loading-utils"))
  (is (= (namespace-string-for-file "test/util" nil) nil)))

(deftest test-entry-in-directory?
  (is (not (entry-in-directory? (ZipEntry. "foo/bar/baz") "foo/bar/baz")))
  (is (entry-in-directory? (ZipEntry. "foo/bar/baz") "foo/bar/"))
  (is (entry-in-directory? (ZipEntry. "foo/bar/baz") "foo/"))
  (is (entry-in-directory? (ZipEntry. "foo/bar/baz") ""))
  (is (not (entry-in-directory? nil "foo/bar/")))
  (is (not (entry-in-directory? (ZipEntry. "foo/bar/baz") nil))))

(defn
  clojure-jar []
  (some
    #(when (.endsWith (.getName %1) "clojure-1.2.1.jar") %1)
    (classpath/classpath-jarfiles)))

(deftest test-directory-zip-entries
  (let [clojure-entries (directory-zip-entries (clojure-jar) "clojure/xml/")]
    (is clojure-entries)
    (is (> (count clojure-entries) 0))))

(deftest test-class-path-zip-entries
  (let [clojure-entries (class-path-zip-entries "clojure/xml/")]
    (is clojure-entries)
    (is (> (count clojure-entries) 0))))

(deftest test-all-class-path-jar-file-names
  (let [clojure-contrib-entries (all-class-path-jar-file-names "clojure/java")]
    (is clojure-contrib-entries)
    (is (> (count clojure-contrib-entries) 0))))