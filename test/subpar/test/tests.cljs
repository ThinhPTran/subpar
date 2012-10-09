(ns subpar.test.tests
  (:use [subpar.core :only [parse
                            get-opening-delimiter-index
                            get-closing-delimiter-index
                            in-comment?
                            in-code?
                            in-string?
                            doublequote
                            get-start-of-next-list
                            backward-up
                            forward
                            forward-slurp
                            forward-barf
                            backward-slurp
                            backward-barf
                            forward-delete
                            backward-delete
                            count-lines
                            close-expression]]))

(defn ^:export arr= [a b] (= (into [] a) (into [] b)))

(defn run []
  (assert (= 4 (count-lines "\n\n\n\n" 0 2)))
  (assert (= 3 (count-lines "0\n\n\n\n" 0 2)))
  (assert (= 2 (count-lines "01\n\n\n\n" 0 2)))
  (assert (= 1 (count-lines "012\n\n\n\n" 0 2)))

  (assert (= -1 (get-opening-delimiter-index " ()"      0)))
  (assert (= -1 (get-opening-delimiter-index " ()"      1)))
  (assert (= 1  (get-opening-delimiter-index " ()"      2)))
  (assert (= -1 (get-opening-delimiter-index " ()"      3)))
  (assert (= -1 (get-opening-delimiter-index " () []"   3)))
  (assert (= -1 (get-opening-delimiter-index " () []"   4)))
  (assert (= 4  (get-opening-delimiter-index " () []"   5)))
  (assert (= -1 (get-opening-delimiter-index " () []"   6)))
  (assert (= 1  (get-opening-delimiter-index " ([a] )"  2)))
  (assert (= 2  (get-opening-delimiter-index " ([a] )"  3)))
  (assert (= 5  (get-opening-delimiter-index "([a]){b}" 6)))
  (assert (= 1  (get-opening-delimiter-index " (;a\nb)" 5)))
  
  (assert (= 3  (get-closing-delimiter-index " ()"          0)))
  (assert (= 3  (get-closing-delimiter-index " ()"          1)))
  (assert (= 2  (get-closing-delimiter-index " ()"          2)))
  (assert (= 3  (get-closing-delimiter-index " ()"          3)))
  (assert (= 6  (get-closing-delimiter-index " () []"       3)))
  (assert (= 6  (get-closing-delimiter-index " () []"       4)))
  (assert (= 5  (get-closing-delimiter-index " () []"       5)))
  (assert (= 6  (get-closing-delimiter-index " () []"       6)))
  (assert (= 6  (get-closing-delimiter-index " ([a] )"      2)))
  (assert (= 4  (get-closing-delimiter-index " ([a] )"      3)))
  (assert (= 7  (get-closing-delimiter-index "([a]){b}"     6)))
  (assert (= 10 (get-closing-delimiter-index " (;a\nb () )" 5)))
  
  (assert (= false (in-comment? (parse "a;b") 0)))
  (assert (= false (in-comment? (parse "a;b") 1)))
  (assert (= true  (in-comment? (parse "a;b") 2)))
  (assert (= true  (in-comment? (parse "a;b\nc") 3)))
  (assert (= false (in-comment? (parse "a;b\nc") 4)))
  (assert (= true  (in-comment? (parse "a;\"b\"") 3)))
  
  (assert (= true  (in-code? (parse "a;b") 0)))
  (assert (= true  (in-code? (parse "a;b") 1)))
  (assert (= false (in-code? (parse "a;b") 2)))
  (assert (= true  (in-code? (parse "a;b\nc") 4)))
  (assert (= false (in-code? (parse "a;\"b\"") 3)))
  
  (assert (= false (in-string? (parse "a;\"b\"") 0)))
  (assert (= false (in-string? (parse "a;\"b\"") 3)))
  (assert (= false (in-string? (parse "a \"b\"") 2)))
  (assert (= true  (in-string? (parse "a \"b\"") 3)))
  (assert (= true  (in-string? (parse "a \"b\"") 4)))

  (assert (= 0 (doublequote "" 0)))
  (assert (= 0 (doublequote "  " 1)))
  (assert (= 0 (doublequote "\"\"" 0)))
  (assert (= 2 (doublequote "\"\"" 1)))
  (assert (= 1 (doublequote "\" \"" 1)))
  (assert (= 1 (doublequote "\" \\\" \"" 2)))
  (assert (= 2 (doublequote "\" \\\" \"" 3)))
  (assert (= 0 (doublequote "; \" \"" 0)))
  (assert (= 3 (doublequote "; \" \"" 1)))
  (assert (= 3 (doublequote "; \" \"" 2)))
  (assert (= 3 (doublequote "; \" \"" 3)))
  (assert (= 3 (doublequote "; \" \"" 4)))

  (assert (= false (get-start-of-next-list ""           0)))
  (assert (= false (get-start-of-next-list " "          0)))
  (assert (= false (get-start-of-next-list "()  "       2)))
  (assert (= false (get-start-of-next-list "()"         1)))
  (assert (= 0     (get-start-of-next-list "() "        0)))
  (assert (= false (get-start-of-next-list ";()"        0)))
  (assert (= false (get-start-of-next-list ";[]"        0)))
  (assert (= false (get-start-of-next-list ";{}"        0)))
  (assert (= false (get-start-of-next-list ";\"\""      0)))
  (assert (= 1     (get-start-of-next-list " () "       0)))
  (assert (= 1     (get-start-of-next-list " [] "       0)))
  (assert (= 1     (get-start-of-next-list " {} "       0)))
  (assert (= 1     (get-start-of-next-list " \"\" "     0)))
  (assert (= false (get-start-of-next-list ";\"\""      0)))
  (assert (= false (get-start-of-next-list ";\"\""      0)))
  (assert (= false (get-start-of-next-list "();a\n()"   1)))
  (assert (= 5     (get-start-of-next-list "();a\n()"   2)))
  (assert (= 2     (get-start-of-next-list "( [] [])"   1)))
  (assert (= 5     (get-start-of-next-list "(aaa []())" 1)))

  (assert (= 0  (backward-up ""              0)))
  (assert (= 0  (backward-up " "             0)))
  (assert (= 1  (backward-up " "             1)))
  (assert (= 1  (backward-up " ( )"          2)))
  (assert (= 3  (backward-up " ()"           3)))
  (assert (= 5  (backward-up " ()\n;\n"      5)))
  (assert (= 3  (backward-up " ( [ ])"       4)))
  (assert (= 3  (backward-up " ( [ asdf])"   7)))
  (assert (= 3  (backward-up " ( [ asdf])"   9)))
  (assert (= 1  (backward-up " ( [ asdf])"   10)))
  (assert (= 11 (backward-up " ( [ asdf])"   11)))
  (assert (= 13 (backward-up " ( [ asdf])  " 13)))

  (assert (= 0  (forward ""               0)))
  (assert (= 1  (forward " "              0)))
  (assert (= 3  (forward " ()"            0)))
  (assert (= 3  (forward "\n()"           0)))
  (assert (= 11 (forward " (asdf (a))"    0)))
  (assert (= 11 (forward " (asdf (a))"    1)))
  (assert (= 6  (forward " (asdf (a))"    2)))
  (assert (= 6  (forward " (asdf (a))"    3)))
  (assert (= 10 (forward " (asdf (a))"    6)))
  (assert (= 6  (forward "((ab ) )"       1)))
  (assert (= 4  (forward "((ab ) )"       2)))
  (assert (= 6  (forward "((ab ) )"       4)))
  (assert (= 13 (forward ";a\n[asdf {a}]" 0)))
  (assert (= 5  (forward " asdf "         0)))
  (assert (= 5  (forward " asdf "         2)))
  (assert (= 9  (forward "( a ;b\n c)"    3)))
  (assert (= 4  (forward "\"\\n\""    0)))

  (assert (arr= (array \) 1 4 1)  (forward-slurp "() a"          1)))
  (assert (arr= (array \) 1 6 1)  (forward-slurp "() (a)"        1)))
  (assert (arr= (array \) 1 8 1)  (forward-slurp "() (a b)"      1)))
  (assert (arr= (array \) 1 10 2) (forward-slurp "();c\n(a b)"   1)))
  (assert (arr= (array)           (forward-slurp "() "           2)))
  (assert (arr= (array)           (forward-slurp " () "          0)))
  (assert (arr= (array \) 1 8 1)  (forward-slurp "() \"a b\""    1)))
  (assert (arr= (array)           (forward-slurp "({a \"b\"} c)" 6)))
  (assert (arr= (array \) 4 7 1)  (forward-slurp "(abc) a"       2)))

  (assert (arr= (array \( 3 1 1) (backward-slurp " a () "          4)))
  (assert (arr= (array \( 2 0 1) (backward-slurp "a () "           3)))
  (assert (arr= (array)          (backward-slurp "a () "           2)))
  (assert (arr= (array \( 6 1 1) (backward-slurp " [ab] (c d) "    9)))
  (assert (arr= (array \( 6 1 1) (backward-slurp " {ab} (c d) "    8)))
  (assert (arr= (array \( 7 1 1) (backward-slurp " (a b) (c d) "   8)))
  (assert (arr= (array \( 7 1 1) (backward-slurp " \"a b\" (c d) " 8)))
  (assert (arr= (array)          (backward-slurp "(a [{}])"        5)))

  (assert (= 0 (forward-delete ""        0)))
  (assert (= 0 (forward-delete "a"       1)))
  (assert (= 1 (forward-delete "a"       0)))
  (assert (= 3 (forward-delete "[]"      0)))
  (assert (= 2 (forward-delete "[]"      1)))
  (assert (= 0 (forward-delete "[a]"     2)))
  (assert (= 0 (forward-delete "[ ]"     2)))
  (assert (= 4 (forward-delete "( )"     0)))
  (assert (= 4 (forward-delete "(a)"     0))) 
  (assert (= 4 (forward-delete "\"a\""   0))) 
  (assert (= 3 (forward-delete "\"\""    0))) 
  (assert (= 0 (forward-delete "\" \""   2))) 
  (assert (= 3 (forward-delete "\\a"     0)))
  (assert (= 2 (forward-delete "\\a"     1)))
  (assert (= 3 (forward-delete "\"\\a\"" 1)))
  (assert (= 2 (forward-delete "\"\\a\"" 2)))

  (assert (= 0 (backward-delete ""       0)))
  (assert (= 0 (backward-delete " "      0)))
  (assert (= 1 (backward-delete " "      1)))
  (assert (= 0 (backward-delete "( )"    1)))
  (assert (= 4 (backward-delete "( )"    3)))
  (assert (= 3 (backward-delete "()"     2)))
  (assert (= 4 (backward-delete "(asdf)" 6)))
  (assert (= 2 (backward-delete "\\a"    1)))
  (assert (= 3 (backward-delete "\\a"    2)))
  (assert (= 2 (backward-delete "\"\""   1)))
  (assert (= 3 (backward-delete "\"\""   2)))
  (assert (= 2 (backward-delete "\"\\\"" 2)))
  (assert (= 3 (backward-delete "\"\\\"" 3)))

  (assert (arr= (array)                (backward-barf "" 0)))
  (assert (arr= (array)                (backward-barf "()" 1)))
  (assert (arr= (array \( 0 2 true 1)  (backward-barf "(a)" 1)))
  (assert (arr= (array \( 0 3 false 1) (backward-barf "(a b)" 1)))
  (assert (arr= (array \( 0 3 false 2) (backward-barf "(a\nb)" 1)))
  (assert (arr= (array)                (backward-barf "(a b)" 5)))
  (assert (arr= (array)                (backward-barf "(a b) " 5)))
  (assert (arr= (array \[ 3 5 true 1)  (backward-barf "(a [b]) " 4)))
  
  (assert (arr= (array)                  (forward-barf "" 0)))
  (assert (arr= (array)                  (forward-barf "()" 1)))
  (assert (arr= (array \) 2 1 true 1 0)  (forward-barf "(a)" 1)))
  (assert (arr= (array \) 4 2 false 1 0) (forward-barf "(a b)" 1)))
  (assert (arr= (array \) 4 2 false 2 0) (forward-barf "(a\nb)" 1)))
  (assert (arr= (array)                  (forward-barf "(a b)" 5)))
  (assert (arr= (array)                  (forward-barf "(a b) " 5)))
  (assert (arr= (array \] 5 4 true 1 3)  (forward-barf "(a [b]) " 4)))

  (assert (arr= (array true 1 4 2)       (close-expression "[   ]" 1)))
)