;; .dir-locals.el
((java-mode . ((eval . (setq-local lsp-java-java-path
                                   (string-trim (shell-command-to-string "mise which java")))))))
